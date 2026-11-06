package io.jettra.server.jakarta.persistence;

import io.jettra.server.core.storage.JettraStorageEngine;
import java.util.concurrent.atomic.AtomicBoolean;

public class JettraEntityManagerFactory implements EntityManagerFactory {

    private final JettraStorageEngine<String, Object> storageEngine;
    private final AtomicBoolean open = new AtomicBoolean(true);

    public JettraEntityManagerFactory(JettraStorageEngine<String, Object> storageEngine) {
        this.storageEngine = storageEngine;
    }

    @Override
    public EntityManager createEntityManager() {
        if (!open.get()) throw new IllegalStateException("EntityManagerFactory cerrado");
        return new JettraEntityManager(storageEngine);
    }

    @Override
    public boolean isOpen() {
        return open.get();
    }

    @Override
    public void close() {
        open.set(false);
    }
}
