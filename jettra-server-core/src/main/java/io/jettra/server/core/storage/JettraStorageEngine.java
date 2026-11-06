package io.jettra.server.core.storage;

import java.io.Closeable;
import java.util.Collection;
import java.util.Set;

/**
 * SPI unificado para motores de persistencia en JettraServer.
 * Permite intercambiar de forma transparente la estrategia de almacenamiento entre:
 * JVM_RAM, DISK_MEMORY y OFF_HEAP.
 *
 * @param <K> Tipo de la clave (usualmente String o Long)
 * @param <V> Tipo del valor
 */
public interface JettraStorageEngine<K, V> extends Closeable {

    /**
     * Retorna el modo operativo de este motor.
     */
    StorageMode getMode();

    /**
     * Inserta o reemplaza una entrada.
     */
    void put(K key, V value);

    /**
     * Obtiene el valor asociado a la clave.
     */
    V get(K key);

    /**
     * Elimina una clave y retorna el valor previo (si aplica).
     */
    V remove(K key);

    /**
     * Verifica la existencia de una clave.
     */
    boolean containsKey(K key);

    /**
     * Retorna el conjunto de claves registradas.
     */
    Set<K> keySet();

    /**
     * Retorna la colección de valores almacenados.
     */
    Collection<V> values();

    /**
     * Retorna el número de elementos persistidos.
     */
    long size();

    /**
     * Elimina todas las entradas del motor.
     */
    void clear();

    /**
     * Fuerza el vaciado de buffers o páginas pendientes a disco/memoria.
     */
    void flush();

    /**
     * Obtiene las métricas de monitoreo en tiempo real.
     */
    StorageMetrics getMetrics();

    /**
     * Cierra los recursos nativos o descriptores abiertos.
     */
    @Override
    void close();
}
