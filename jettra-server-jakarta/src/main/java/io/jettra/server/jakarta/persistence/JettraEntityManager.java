package io.jettra.server.jakarta.persistence;

import io.jettra.server.core.storage.JettraStorageEngine;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementación de EntityManager de Jakarta JPA respaldada directamente por el SPI JettraStorageEngine.
 * Permite que cualquier entidad JPA resida en JVM_RAM, DISK_MEMORY (LSM) o OFF_HEAP (Páginas Ranuradas).
 */
public class JettraEntityManager implements EntityManager {

    private final JettraStorageEngine<String, Object> storageEngine;
    private final EntityTransaction transaction;
    private final AtomicBoolean open = new AtomicBoolean(true);
    private static final AtomicLong ID_GENERATOR = new AtomicLong(1000);

    public JettraEntityManager(JettraStorageEngine<String, Object> storageEngine) {
        this.storageEngine = storageEngine;
        this.transaction = new JettraEntityTransaction(this);
    }

    @Override
    public void persist(Object entity) {
        ensureOpen();
        Objects.requireNonNull(entity, "La entidad a persistir no puede ser nula");
        Class<?> clazz = entity.getClass();
        Field idField = resolveIdField(clazz);
        Object idVal = getFieldValue(idField, entity);

        if (idVal == null && idField.isAnnotationPresent(PersistenceAnnotations.GeneratedValue.class)) {
            if (idField.getType() == Long.class || idField.getType() == long.class) {
                idVal = ID_GENERATOR.incrementAndGet();
            } else if (idField.getType() == Integer.class || idField.getType() == int.class) {
                idVal = (int) ID_GENERATOR.incrementAndGet();
            } else if (idField.getType() == String.class) {
                idVal = UUID.randomUUID().toString();
            }
            setFieldValue(idField, entity, idVal);
        }

        if (idVal == null) {
            throw new IllegalArgumentException("La entidad de tipo " + clazz.getName() + " tiene @Id nulo y no tiene @GeneratedValue");
        }

        String key = buildStorageKey(clazz, idVal);
        storageEngine.put(key, entity);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T merge(T entity) {
        ensureOpen();
        persist(entity);
        return entity;
    }

    @Override
    public void remove(Object entity) {
        ensureOpen();
        Objects.requireNonNull(entity, "La entidad no puede ser nula");
        Class<?> clazz = entity.getClass();
        Field idField = resolveIdField(clazz);
        Object idVal = getFieldValue(idField, entity);
        if (idVal != null) {
            String key = buildStorageKey(clazz, idVal);
            storageEngine.remove(key);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        ensureOpen();
        Objects.requireNonNull(entityClass, "entityClass no puede ser nula");
        Objects.requireNonNull(primaryKey, "primaryKey no puede ser nula");
        String key = buildStorageKey(entityClass, primaryKey);
        Object found = storageEngine.get(key);
        if (found != null && entityClass.isInstance(found)) {
            return (T) found;
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> findAll(Class<T> entityClass) {
        ensureOpen();
        Objects.requireNonNull(entityClass, "entityClass no puede ser nula");
        String prefix = entityClass.getSimpleName() + ":";
        List<T> result = new ArrayList<>();
        for (String k : storageEngine.keySet()) {
            if (k.startsWith(prefix)) {
                Object val = storageEngine.get(k);
                if (val != null && entityClass.isInstance(val)) {
                    result.add((T) val);
                }
            }
        }
        return result;
    }

    @Override
    public EntityTransaction getTransaction() {
        return transaction;
    }

    @Override
    public void flush() {
        ensureOpen();
        storageEngine.flush();
    }

    @Override
    public void clear() {
        ensureOpen();
    }

    @Override
    public boolean isOpen() {
        return open.get();
    }

    @Override
    public void close() {
        open.set(false);
    }

    private void ensureOpen() {
        if (!open.get()) {
            throw new IllegalStateException("EntityManager ya ha sido cerrado");
        }
    }

    private static String buildStorageKey(Class<?> clazz, Object idVal) {
        return clazz.getSimpleName() + ":" + idVal;
    }

    private static Field resolveIdField(Class<?> clazz) {
        Class<?> curr = clazz;
        while (curr != null && curr != Object.class) {
            for (Field f : curr.getDeclaredFields()) {
                if (f.isAnnotationPresent(PersistenceAnnotations.Id.class)) {
                    f.setAccessible(true);
                    return f;
                }
            }
            curr = curr.getSuperclass();
        }
        throw new IllegalArgumentException("La entidad " + clazz.getName() + " no posee ningún campo anotado con @Id");
    }

    private static Object getFieldValue(Field f, Object instance) {
        try {
            f.setAccessible(true);
            return f.get(instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setFieldValue(Field f, Object instance, Object val) {
        try {
            f.setAccessible(true);
            f.set(instance, val);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static class JettraEntityTransaction implements EntityTransaction {
        private final JettraEntityManager em;
        private boolean active = false;

        public JettraEntityTransaction(JettraEntityManager em) {
            this.em = em;
        }

        @Override
        public void begin() {
            if (active) throw new IllegalStateException("Transacción ya activa");
            active = true;
        }

        @Override
        public void commit() {
            if (!active) throw new IllegalStateException("No hay transacción activa");
            em.flush();
            active = false;
        }

        @Override
        public void rollback() {
            if (!active) throw new IllegalStateException("No hay transacción activa");
            active = false;
        }

        @Override
        public boolean isActive() {
            return active;
        }
    }
}
