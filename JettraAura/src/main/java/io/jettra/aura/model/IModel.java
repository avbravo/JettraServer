package io.jettra.aura.model;

import java.io.Serializable;

/**
 * Interfaz fundamental para modelos de datos en JettraAura.
 * Separa la lógica de presentación de los componentes del modelo de datos subyacente.
 * Inspirado en el diseño desacoplado de Apache Wicket pero optimizado para Java 25.
 *
 * @param <T> Tipo de objeto encapsulado en el modelo
 */
public interface IModel<T> extends Serializable {

    /**
     * Retorna el objeto representado por el modelo.
     */
    T getObject();

    /**
     * Actualiza el objeto representado por el modelo.
     */
    void setObject(T object);

    /**
     * Libera cualquier referencia transitoria pesada al finalizar el ciclo de petición.
     */
    default void detach() {}
}

