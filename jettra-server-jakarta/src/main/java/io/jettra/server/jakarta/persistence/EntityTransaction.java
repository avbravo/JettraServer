package io.jettra.server.jakarta.persistence;

public interface EntityTransaction {
    void begin();
    void commit();
    void rollback();
    boolean isActive();
}
