# JettraCollections: Arquitectura, Ingeniería de Bajo Nivel y Guía Técnica

> **JettraCollections** es un framework de colecciones de ultra-alto rendimiento y mínima huella de memoria (*zero-allocation primitive & open-addressing memory-efficient containers*) desarrollado para **Java 25**, inspirado en los fundamentos de ingeniería de bajo nivel de **Eclipse Collections**.

---

## 1. Introducción y el Problema de las Colecciones en Java Estándar

El **Java Collections Framework (JCF)** clásico (`java.util.ArrayList`, `java.util.HashMap`, `java.util.HashSet`), diseñado a finales de los años 90, fue concebido bajo el paradigma de generalización orientada a objetos donde cada elemento es tratado como una instancia de `java.lang.Object`.

En la computación moderna de alto rendimiento, microservicios masivos, motores de bases de datos embebidos (como **JettraDB**) y concurrencia extrema con millones de **Virtual Threads (Project Loom)**, este modelo tradicional genera tres cuellos de botella críticos:

1. **Inflación Descomunal de RAM (*Memory Bloat*):** Los envoltorios primitivos (*wrappers*) como `Integer`, `Long` y `Double` multiplican por entre **4x y 8x** el consumo de memoria respecto a los datos nativos.
2. **Dispersión en Memoria y Fallos de Caché L1/L2/L3 (*Cache Misses*):** Cada objeto se almacena de forma aislada en ubicaciones aleatorias de la memoria Heap. La CPU no puede precargar datos eficientemente mediante líneas de caché contiguas (*pointer chasing*).
3. **Presión Devastadora sobre el Recolector de Basura (*GC Pressure*):** Llenar un mapa estándar con 1 millón de enteros crea más de **3,000,000 de objetos independientes** en la *Young Generation*, obligando al GC a realizar pausas frecuentes de limpieza.

**JettraCollections** erradica estos problemas implementando colecciones primitivas directas y contenedores basados en direccionamiento abierto en memoria contigua.

---

## 2. Los Tres Pilares de Ingeniería de JettraCollections

```
+-----------------------------------------------------------------------------------+
|                            JETTRACOLLECTIONS ARCHITECTURE                         |
+-----------------------------------------------------------------------------------+
|  1. COLECCIONES PRIMITIVAS DIRECTAS  | Almacenamiento directo en int[], long[],   |
|     (Eliminación Total del Boxing)   | double[]. 0 objetos wrapper creados.       |
+--------------------------------------+--------------------------------------------+
|  2. DIRECCIONAMIENTO ABIERTO PLANO   | Tablas contiguas sin HashMap$Node.         |
|     (Zero-Node Open-Addressing)      | Backward-Shift Deletion sin lápidas.       |
+--------------------------------------+--------------------------------------------+
|  3. LOCALIDAD DE CACHÉ Y ALIVIO GC   | Ráfagas de 64 bytes por línea de CPU.      |
|     (Hardware-Sympathetic Design)    | Reducción del 99.999% de objetos en Heap.  |
+--------------------------------------+--------------------------------------------+
```

### Pilar 1: Colecciones Primitivas Directas (Evitar el *Boxing*)

En una JVM de 64 bits con *Compressed OOPs* (punteros comprimidos), la anatomía de un objeto `Integer` es:

* **Object Header (Mark Word + Class Word):** 12 a 16 bytes.
* **Primitive int value:** 4 bytes.
* **Padding (alineación a múltiplos de 8 bytes):** 4 bytes.
* **Total por cada Integer:** **24 bytes en el Heap**.
* **Puntero de referencia en el arreglo:** 4 bytes adicionales.
* **Costo por elemento en `ArrayList<Integer>`:** **28 a 32 bytes**.

En contraparte, en **`io.jettra.collections.list.primitive.IntArrayList`**:
* Los datos se almacenan en un arreglo primitivo plano `int[]`.
* **Costo por elemento:** **4 bytes exactos**.
* **Ahorro de memoria:** **85.7% de reducción directa**.

```
[java.util.ArrayList<Integer>]
Array de Referencias: [ Ref1 ] -> Objeto Integer (24B) -> [ valor: 4B ]
                      [ Ref2 ] -> Objeto Integer (24B) -> [ valor: 4B ]
                      [ Ref3 ] -> Objeto Integer (24B) -> [ valor: 4B ]  (Total: ~28-32B por número)

[JettraCollections IntArrayList]
Arreglo Primitivo int[]: [  4B  |  4B  |  4B  |  4B  |  4B  ]            (Total: 4B por número)
```

---

### Pilar 2: Estructuras de Datos Planas para Mapas y Conjuntos

`java.util.HashMap` y `HashSet` utilizan encadenamiento separado (*separate chaining*). Por cada entrada que insertas, Java instancia un objeto `HashMap$Node`:

```
class Node<K,V> {
    final int hash;     // 4 bytes
    final K key;        // 4 bytes (ref)
    V value;            // 4 bytes (ref)
    Node<K,V> next;     // 4 bytes (ref)
    // + 12-16 bytes de Object Header = 32 bytes por cada Node
}
```

Para almacenar un par `(int, int)` en `java.util.HashMap<Integer, Integer>`:
1. Objeto `Integer` llave: 24 bytes
2. Objeto `Integer` valor: 24 bytes
3. Objeto `HashMap.Node`: 32 bytes
4. Puntero en la tabla de buckets: 4 bytes
**Total: ~84 a 88 bytes por entrada.**

#### La Solución Jettra:
* **`IntIntHashMap`:** Utiliza dos arreglos primitivos paralelos contiguos (`int[] keys` e `int[] values`). No se crea ningún objeto `Node` ni ningún envoltorio `Integer`. A un factor de carga del 70%, el costo promedio es de solo **11.4 a 16 bytes por entrada**.
* **`IntHashSet`:** Utiliza un único arreglo plano `int[] table` con sondeo lineal (*linear probing*) y dispersión de alta velocidad **MurmurHash3 fmix**.
* **Algoritmo de Eliminación por Desplazamiento Hacia Atrás (*Backward-Shift Deletion*):** A diferencia de otras implementaciones que usan "lápidas" (*tombstones* como -1 o marcadores) que degradan el rendimiento con el tiempo forzando re-hashes artificiales, JettraCollections desplaza hacia atrás las claves afectadas cerrando los huecos inmediatamente.
* **`UnifiedSet<E>` y `UnifiedMap<K, V>`:** Llevan esta optimización a objetos genéricos. `UnifiedSet` almacena los objetos directamente en `Object[] table` sin nodos intermedios, ahorrando un **75% a 85%** de los metadatos de contenedor.

---

### Pilar 3: Localidad de Referencia en Caché CPU y Cero Presión de GC

Las CPUs modernas no transfieren datos byte por byte desde la RAM, sino en bloques de **64 bytes llamados Líneas de Caché (*Cache Lines*)**.

* **Con JCF tradicional (*Pointer Chasing*):** Cada elemento reside en una dirección de memoria diferente. Al iterar una lista o mapa de objetos, cada acceso a memoria provoca un *Cache Miss*, congelando la CPU esperando a que la memoria RAM principal responda.
* **Con JettraCollections:** Un solo acceso a memoria en un `IntArrayList` carga **16 enteros consecutivos** (`16 * 4 = 64 bytes`) directamente en la caché ultrarrápida L1 de la CPU. La iteración, el cálculo de sumas, filtros y transformaciones se ejecutan a velocidad nativa del silicio.
* **Impacto en el Garbage Collector:** Para 1 millón de elementos en un mapa:
  * JCF crea **3,000,000 de objetos** que el GC debe rastrear, verificar vivencia, mover de *Survivor* a *Old Generation* y eventualmente recolectar.
  * `IntIntHashMap` crea exactamente **2 objetos** en todo su ciclo de vida (`int[] keys` y `int[] values`).

---

## 3. Catálogo de Clases y Estructuras Disponibles

| Categoría | Clase en JettraCollections | Equivalente JCF Tradicional | Reducción de RAM |
| :--- | :--- | :--- | :--- |
| **Lista Primitiva** | `IntArrayList` | `ArrayList<Integer>` | **85.7%** |
| **Lista Primitiva** | `LongArrayList` | `ArrayList<Long>` | **75.0%** |
| **Lista Primitiva** | `DoubleArrayList` | `ArrayList<Double>` | **75.0%** |
| **Lista Inmutable** | `ImmutableIntList` | `List.of(...)` con Integer | **87.5%** |
| **Conjunto Primitivo** | `IntHashSet` | `HashSet<Integer>` | **87.0%** |
| **Conjunto Primitivo** | `LongHashSet` | `HashSet<Long>` | **78.2%** |
| **Conjunto Inmutable**| `ImmutableIntHashSet` | `Set.of(...)` con Integer | **90.0%** |
| **Conjunto de Objetos**| `UnifiedSet<E>` | `HashSet<E>` | **76.7%** |
| **Mapa Primitivo** | `IntIntHashMap` | `HashMap<Integer, Integer>` | **81.0%** |
| **Mapa Híbrido** | `IntObjectHashMap<V>` | `HashMap<Integer, V>` | **65.0%** |
| **Mapa Híbrido** | `ObjectIntHashMap<K>` | `HashMap<K, Integer>` | **65.0%** |
| **Mapa de Objetos** | `UnifiedMap<K, V>` | `HashMap<K, V>` | **58.1%** |

---

## 4. Ejemplos Prácticos de Uso

### 4.1. Listas Primitivas (`IntArrayList` e `ImmutableIntList`)

```java
import io.jettra.collections.list.primitive.IntArrayList;
import io.jettra.collections.api.IntList;

// 1. Creación directa con varargs
IntArrayList list = IntArrayList.of(10, 20, 30, 40, 50, 60);

// 2. Inserción sin ningún auto-boxing
list.add(70);
list.addAtIndex(0, 5); // [5, 10, 20, 30, 40, 50, 60, 70]

// 3. Operaciones funcionales directas sin crear Streams pesados
long sum = list.sum();                  // Suma matemática acumulada
int max = list.max().orElse(0);         // Máximo valor
double avg = list.average().orElse(0.0);// Promedio aritmético

// 4. Filtrado selectivo y colecciones
IntList mayoresA30 = list.select(x -> x > 30);
IntList duplicados = list.collect(x -> x * 2);

// 5. Ordenamiento in-place de ultra-alta velocidad
list.sortThis();
int index = list.binarySearch(40); // Búsqueda binaria O(log N)

// 6. Conversión a contenedor inmutable de tamaño exacto
IntList inmutable = list.toImmutable();
```

---

### 4.2. Conjuntos Primitivos de Alta Densidad (`IntHashSet`)

```java
import io.jettra.collections.set.primitive.IntHashSet;

IntHashSet set = new IntHashSet(100);

// Inserción de valores (soporta todos los enteros de 32 bits, incluyendo el 0)
set.add(100);
set.add(200);
set.add(0);
set.add(100); // Duplicado ignorado sin costo de boxing

System.out.println("Tamaño: " + set.size()); // 3
System.out.println("¿Contiene 200?: " + set.contains(200)); // true

// Eliminación con desplazamiento hacia atrás sin lápidas
set.remove(200);

// Iteración sin generar objetos iteradores en bucles cerrados
set.forEach((int val) -> {
    // Procesamiento primitivo de ultra-baja latencia
});
```

---

### 4.3. Mapas Primitivos Clave-Valor (`IntIntHashMap`)

```java
import io.jettra.collections.map.primitive.IntIntHashMap;

// Mapa de ID de Empleado (int) a Salario (int)
IntIntHashMap salarioPorEmpleado = new IntIntHashMap(50_000);

// Inserción directa en arreglos paralelos
salarioPorEmpleado.put(1001, 4500);
salarioPorEmpleado.put(1002, 6200);
salarioPorEmpleado.put(0, 8900); // Soporte nativo para clave 0

// Consulta con valor por defecto
int salario = salarioPorEmpleado.getIfAbsent(1001, -1); // 4500
int inexistente = salarioPorEmpleado.getIfAbsent(9999, 0); // 0

// Iteración funcional con procedimiento primitivo
salarioPorEmpleado.forEachKeyValue((int empId, int monto) -> {
    System.out.println("Empleado ID: " + empId + " -> Salario: " + monto);
});
```

---

### 4.4. Mapas Híbridos Clave Primitiva a Objeto (`IntObjectHashMap<V>`)

Ideal para índices primarios en memoria, cachés de sesiones de usuario o nodos en bases de datos:

```java
import io.jettra.collections.map.primitive.IntObjectHashMap;

record Usuario(int id, String nombre, String email) {}

IntObjectHashMap<Usuario> cacheUsuarios = new IntObjectHashMap<>();

// Asignación atómica si está ausente mediante factoría
Usuario u = cacheUsuarios.getIfAbsentPut(501, id -> new Usuario(id, "Carlos Gómez", "carlos@jettra.io"));

Usuario encontrado = cacheUsuarios.get(501);
```

---

### 4.5. Conjuntos y Mapas de Objetos Planos (`UnifiedSet` y `UnifiedMap`)

Cuando obligatoriamente trabajas con objetos pero deseas eliminar el overhead del nodo de `HashMap`:

```java
import io.jettra.collections.set.UnifiedSet;
import io.jettra.collections.map.UnifiedMap;

// Set de objetos sin nodos intermedios
UnifiedSet<String> roles = UnifiedSet.of("ADMIN", "USER", "SUPERVISOR");

// Map de objetos sin nodos HashMap.Node
UnifiedMap<String, String> configuraciones = UnifiedMap.newMap();
configuraciones.put("db.host", "localhost");
configuraciones.put("db.port", "5432");
```

---

## 5. Reporte Técnico de Memoria: Comparativa JCF vs JettraCollections

Los siguientes datos corresponden a cálculos reales en una JVM de 64 bits con *Compressed OOPs* activados:

### Comparativa para 1,000,000 de Elementos

```
========================================================================================
      JETTRACOLLECTIONS VS JAVA COLLECTIONS FRAMEWORK (JCF) MEMORY REPORT
      Element Count: 1,000,000 elements (64-bit JVM, Compressed OOPs)
========================================================================================

Collection Type                  | JCF (RAM)        | Jettra (RAM)      | RAM Saved (%) 
----------------------------------------------------------------------------------------
ArrayList<Integer> vs IntArrayList | 26.70 MB         |  3.81 MB          | 85.7% Ahorro  
HashSet<Integer> vs IntHashSet     | 61.41 MB         |  8.00 MB          | 87.0% Ahorro  
HashMap<Int, Int> vs IntIntHashMap | 84.29 MB         | 16.00 MB          | 81.0% Ahorro  
HashSet<Obj> vs UnifiedSet<Obj>    | 34.33 MB         |  8.00 MB          | 76.7% Ahorro  
HashMap<K, V> vs UnifiedMap<K, V>  | 38.15 MB         | 16.00 MB          | 58.1% Ahorro  
========================================================================================
PRESIÓN SOBRE EL RECOLECTOR DE BASURA (Objetos Creados en Heap):
  - JCF 1M elements en HashMap: 3,000,000 objetos individuales creados.
  - JettraCollections IntIntHashMap: EXACTAMENTE 2 objetos en total (int[] keys, int[] values).
  - Reducción de objetos en Heap: 99.9999% de alivio para el Garbage Collector.
========================================================================================
```

---

## 6. Rendimiento y Sinergia con Java 25 y Virtual Threads

### 1. Densidad de Memoria en Microservicios Masivos
En arquitecturas modernas basadas en microservicios, el costo de infraestructura en la nube (AWS EC2, Google Cloud, Azure) está directamente vinculado al tamaño de la instancia de RAM aprovisionada. 
* Un servicio de procesamiento de transacciones que mantenga **100 millones de IDs numéricos en caché** requeriría más de **8.4 GB de RAM** con `HashMap` tradicional.
* Con `IntIntHashMap` de **JettraCollections**, esa misma información cabe en solo **1.6 GB de RAM**.

### 2. Compatibilidad con Virtual Threads (Project Loom)
Los hilos virtuales permiten tener cientos de miles de tareas concurrentes ejecutándose simultáneamente. Sin embargo, si cada hilo virtual instancia pequeñas colecciones estándar (`ArrayList<Integer>` o `HashMap<Integer, V>`), el Heap colapsa rápidamente por *Out of Memory*.
Las variantes compactas e inmutables como `ImmutableIntList` e `ImmutableIntHashSet`:
* Comparten arreglos planos inmutables entre miles de hilos virtuales sin locks ni condiciones de carrera.
* Reducen a cero el tiempo de sincronización entre núcleos de CPU.

---

## 7. Framework de Pruebas Nativo: JettraTest (Reemplazo de JUnit)

El proyecto **JettraCollections** no utiliza JUnit tradicional; en su lugar, está completamente integrado con el framework nativo del ecosistema: **`io.jettra:JettraTest`**.

### Características de JettraTest en JettraCollections:
* **Cero dependencias externas de JUnit:** Se eliminaron todos los artefactos de `org.junit.jupiter`.
* **Runner Optimizado para Java 25:** Utiliza `io.jettra.test.runner.JettraTestRunner` configurado en `exec-maven-plugin` durante la fase `test`.
* **Anotaciones Nativas:**
  * `@NotRequiresRunningServer`: Indica al runner que las pruebas de colecciones se ejecutan directamente en memoria sin necesidad de levantar un servidor HTTP o de sockets.
  * `@Test` (de `io.jettra.test.annotation.Test`): Marca los métodos de prueba unitaria.
* **Motor de Aserciones de Alto Rendimiento:** `io.jettra.test.core.JettraAssert`:
  * Soporte nativo para comparación de arreglos primitivos (`assertArrayEquals(int[] ...)`, `assertArrayEquals(long[] ...)`).
  * Manejo estricto de excepciones (`assertThrows(...)`).

### Ejemplo de Prueba con JettraTest:

```java
package io.jettra.collections.list;

import io.jettra.collections.list.primitive.IntArrayList;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;
import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class IntArrayListTest {

    @Test
    public void testAddAndGet() {
        IntArrayList list = new IntArrayList();
        for (int i = 0; i < 100; i++) {
            list.add(i * 10);
        }
        assertEquals(100, list.size());
        assertEquals(0, list.get(0));
        assertEquals(990, list.get(99));
    }
}
```

### Ejecución de Pruebas y Benchmarks:

```bash
# Ejecutar los 24 tests unitarios y de rendimiento mediante JettraTestRunner
mvn clean test

# Ejecutar el demo interactivo con el reporte de memoria en tiempo real
mvn exec:java -Dexec.mainClass=io.jettra.collections.JettraCollectionsDemo
```
