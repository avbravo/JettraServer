# Guía de Uso de @FluxModelToRecordConversor

La anotación `@FluxModelToRecordConversor` permite generar en tiempo de compilación una clase encargada de convertir objetos de tipo Model (generalmente clases utilizadas para la capa de vista o presentación) a sus correspondientes Records y viceversa. Esta funcionalidad es exclusiva de **JettraFlux** y se integra sin problemas en la interfaz web para automatizar el mapeo de datos.

El procesador de anotaciones buscará los campos de la clase Model y los mapeará contra los atributos del Record basándose en los nombres de los atributos o en los métodos _getter/setter_ disponibles.

## ¿Qué hace exactamente el procesador?

Al aplicar `@FluxModelToRecordConversor` sobre una clase de modelo, se genera una nueva clase conversora (por ejemplo, `PersonModelConverter` para un modelo llamado `PersonModel` o un record destino `Person`) anotada con `@ApplicationScoped`.

Esta clase generada provee dos métodos:
1. `toModel(Record record)`: Instancia un nuevo objeto del modelo y le asigna los valores extraídos del Record.
2. `toRecord(Model model)`: Instancia y retorna el Record pasando los valores del modelo por el constructor del Record.

## Ejemplo de Uso

### 1. Definición del Record (Entidad de datos inmutable)

Primero, define el _record_ que representará tus datos en el dominio:

```java
import io.jettra.rules.validations.Min;
import io.jettra.rules.validations.NotNull;
import io.jettra.rules.validations.Email;

public record Person(
    @NotNull String name, 
    @Email String email,
    @Min(value = 0) Integer age
) { }
```

### 2. Definición del Model (Vista)

A continuación, define el modelo que se enlazará a tu interfaz usando JettraFlux. Aplica la anotación `@FluxModelToRecordConversor` para que se genere su respectivo conversor.

```java
import io.jettra.wui.core.annotations.JettraViewModel;
import io.jettra.core.flux.FluxModelToRecordConversor;
import io.jettra.rules.validations.Min;
import io.jettra.rules.validations.NotNull;
import io.jettra.rules.validations.Email;
import io.jettra.wui.core.annotations.PropertiesLabel;

@JettraViewModel
@FluxModelToRecordConversor // <-- Genera PersonModelConverter
public class PersonModel {

    @NotNull
    @PropertiesLabel(value = "person.name", label = "Nombre")
    public String name = "name";
    
    @Email
    @PropertiesLabel(value = "person.email", label = "Email")
    public String email = "email";
    
    @Min(value = 0)
    @PropertiesLabel(value = "person.age", label = "Edad")
    public Integer age = 0;
}
```

> **Nota**: El procesador infiere el nombre del `Record` automáticamente eliminando el sufijo `Model` de la clase. En el ejemplo, `PersonModel` mapeará al record `Person`. Si el record tuviera un nombre distinto o se encontrara en un paquete distinto que no se puede inferir, puedes especificar la meta usando el atributo `goal` de la anotación: `@FluxModelToRecordConversor(goal = EntidadPerson.class)`.

### 3. Clase Generada en Tiempo de Compilación

Al compilar el proyecto, se generará la clase `PersonModelConverter`. Internamente se verá de manera similar a lo siguiente:

```java
import io.jettra.scoped.ApplicationScoped;

@ApplicationScoped
public class PersonModelConverter {

    public PersonModel toModel(Person record) {
        if (record == null) {
            return null;
        }
        PersonModel model = new PersonModel();
        model.name = record.name();
        model.email = record.email();
        model.age = record.age();
        return model;
    }

    public Person toRecord(PersonModel model) {
        if (model == null) {
            return null;
        }
        return new Person(
            model.name,
            model.email,
            model.age
        );
    }
}
```

*(El procesador genera el código utilizando reflexiones o acceso directo a campos públicos o `getters/setters` según aplique en el modelo)*

### 4. Integración en Handlers (Ej. RenderedCrudHandler)

Esta clase puede ser inyectada y/o utilizada internamente dentro de los _handlers_ generados por `@RenderedCrud` y la interfaz Web de JettraFlux para hacer el puente de forma automática entre el envío de datos desde un formulario Web (`Model`) hacia una base de datos o API que requiera un `Record`.

Por ejemplo, si inyectas este conversor en un servicio:

```java
import io.jettra.core.inject.annotation.Inject;

public class PersonService {
    @Inject
    private PersonModelConverter converter;
    
    public void save(PersonModel personModel) {
        Person person = converter.toRecord(personModel);
        // Lógica para guardar el record...
    }
}
```
