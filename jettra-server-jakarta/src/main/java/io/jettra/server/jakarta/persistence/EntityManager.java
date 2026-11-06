package io.jettra.server.jakarta.persistence;

import java.util.List;

public interface EntityManager extends AutoCloseable {
    void persist(Object entity);
    <T> T merge(T entity);
    void remove(Object entity);
    <T> T find(Class<T> entityClass, Object primaryKey);
    <T> List<T> findAll(Class<T> entityClass);
    EntityTransaction getTransaction();
    void flush();
    void clear();
    boolean isOpen();
    @Override
    void close();
}
