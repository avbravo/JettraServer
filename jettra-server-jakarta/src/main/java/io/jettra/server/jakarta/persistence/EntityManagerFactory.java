package io.jettra.server.jakarta.persistence;

import io.jettra.server.core.storage.JettraStorageEngine;
import io.jettra.server.core.storage.StorageEngineFactory;

public interface EntityManagerFactory extends AutoCloseable {
    EntityManager createEntityManager();
    boolean isOpen();
    @Override
    void close();

    static EntityManagerFactory create() {
        return new JettraEntityManagerFactory(StorageEngineFactory.getDefaultEngine());
    }

    static EntityManagerFactory create(JettraStorageEngine<String, Object> storageEngine) {
        return new JettraEntityManagerFactory(storageEngine);
    }
}
