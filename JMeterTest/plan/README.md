# Plan JMeter para Pruebas de Rendimiento y Concurrencia de JettraDB

Este directorio contiene el plan de pruebas de carga y estrés para evaluar el rendimiento de **JettraDB** con múltiples usuarios simultáneos.

## Archivo del Plan
- **`JettraDB_Concurrent_Users_Benchmark.jmx`**: Plan de prueba de Apache JMeter configurado para pruebas de alta concurrencia.

---

## Características del Plan de Pruebas

1. **Autenticación Multi-Usuario / Token JWT**:
   - Cada hilo (usuario virtual) ejecuta `POST /api/auth/login` con credenciales de administrador (`admin`/`admin`).
   - Extrae dinámicamente el `token` Bearer para autorizar las solicitudes posteriores de forma segura e independiente.

2. **Carga Mixta de Alto Rendimiento (Lectura / Escritura Multi-Modelo)**:
   - **Document Engine (Write)**: Inserción de documentos JSON con IDs únicos, transacciones y payloads aleatorios (`POST /api/document/bench_coll`).
   - **Document Engine (Read)**: Lectura puntual por ID de los documentos generados (`GET /api/document/bench_coll/{id}`).
   - **Key-Value Engine (Write & Read)**: Operaciones clave-valor concurrentes sobre el namespace `system_db` (`POST` y `GET /api/model/KEYVALUE/system_db/{key}`).
   - **TimeSeries Engine (Ingestion Stream)**: Ingestión masiva de métricas de telemetría/sensores con marcas de tiempo (`POST /api/model/TIMESERIES/system_db/{timestamp}`).

3. **Control y Pacing**:
   - Temporizador gaussiano (`GaussianRandomTimer`) que simula pausas de usuario realistas configurables.
   - Aserciones automáticas en cada sampler (códigos HTTP 200).

---

## Parámetros Configurables

El plan utiliza funciones `${__P(param, default)}`, lo que permite sobreescribir cualquier variable desde la línea de comandos sin tener que modificar el archivo XML:

| Parámetro | Valor por Defecto | Descripción |
| :--- | :--- | :--- |
| `host` | `localhost` | Host o dirección IP de la instancia de JettraDB |
| `port` | `8086` | Puerto REST API del servidor (por defecto 8086 en JettraDB) |
| `threads` | `50` | Número de usuarios/hilos concurrentes simultáneos |
| `rampup` | `10` | Tiempo de rampa de subida (segundos) |
| `duration` | `60` | Duración total de la prueba en segundos |
| `database` | `system_db` | Base de datos sobre la que se ejecutan las operaciones |
| `admin_user` | `admin` | Usuario administrador |
| `admin_pass` | `admin` | Contraseña del usuario administrador |

---

## Cómo Ejecutar las Pruebas

### 1. Iniciar JettraDB
Asegúrate de que el servidor JettraDB esté iniciado (desde NetBeans o ejecutando `./mvn-jettra` o `mvn exec:exec`).

### 2. Ejecución en Modo Línea de Comandos (CLI / Headless - Recomendado para pruebas de carga reales)
```bash
cd /home/avbravo/NetBeansProjects/jettrastack_local/JettraWorkspace/JMeter/plan

# Ejecutar con 50 usuarios concurrentes durante 60 segundos
jmeter -n -t JettraDB_Concurrent_Users_Benchmark.jmx \
       -l resultados.jtl \
       -e -o reporte_html

# Ejecutar con 200 usuarios concurrentes y rampa de 20s
jmeter -n -t JettraDB_Concurrent_Users_Benchmark.jmx \
       -Jthreads=200 \
       -Jrampup=20 \
       -Jduration=120 \
       -l resultados_200_usuarios.jtl \
       -e -o reporte_200_html
```

## Visualizadores y Gráficas Disponibles

El plan incluye múltiples componentes visuales interactivos que se despliegan directamente en la interfaz gráfica de JMeter o mediante reportes web:

1. 📊 **Gráfica de Resultados en Tiempo Real (`Graph Results`)**:
   - Curvas continuas de Throughput (verde), Tiempo Medio (azul), Mediana (violeta) y Desviación estándar (rojo).
2. 📉 **Gráfica de Tiempos de Respuesta (`Response Time Graph`)**:
   - Gráfica de líneas que muestra la evolución temporal de la latencia por cada endpoint (`Document`, `KeyValue`, `TimeSeries`, `Auth`).
3. 📶 **Gráfica Agregada de Barras (`Aggregate Graph`)**:
   - Barras comparativas de percentiles 90%, 95%, 99%, promedio y tiempos máximos.
4. 📋 **Tabla de Resultados en Tiempo Real (`View Results in Table`)**:
   - Muestra cada transacción individual con indicadores de estado, tiempo en milisegundos y tamaño transferido.
5. 🔍 **Árbol de Resultados (`View Results Tree`)**:
   - Inspección visual de cuerpos de solicitud y respuesta (JSON).

---

## Generación del Dashboard Web Interactivo con Gráficas (HTML)

Puedes generar en cualquier momento un reporte HTML con gráficas interactivas basadas en Chart.js / Flot ejecutando el script provisto:

```bash
cd /home/avbravo/NetBeansProjects/jettrastack_local/JettraWorkspace/JMeter/plan
./generate_dashboard.sh
```

Esto procesará el archivo CSV más reciente y generará el panel web en:
`file:///home/avbravo/NetBeansProjects/jettrastack_local/JettraWorkspace/JMeter/plan/reporte_dashboard_html/index.html`

El panel incluye:
- Gráficas de APDEX (Índice de satisfacción de rendimiento)
- Gráficas de peticiones por segundo (Throughput over time)
- Distribución de percentiles de respuesta
- Hilos/usuarios activos simultáneamente en el tiempo
- Tiempos de conexión y latencia vs tiempo

