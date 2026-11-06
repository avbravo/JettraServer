package io.jettra.server.jakarta.transaction;

/**
 * Gestor de transacciones atómicas para el ecosistema JettraServer.
 */
public class JettraTransactionManager implements UserTransaction {

    private final ThreadLocal<Integer> currentStatus = ThreadLocal.withInitial(() -> STATUS_NO_TRANSACTION);

    @Override
    public void begin() {
        if (currentStatus.get() == STATUS_ACTIVE) {
            throw new IllegalStateException("Una transacción ya se encuentra activa en este hilo");
        }
        currentStatus.set(STATUS_ACTIVE);
    }

    @Override
    public void commit() {
        if (currentStatus.get() != STATUS_ACTIVE) {
            throw new IllegalStateException("No existe transacción activa para confirmar");
        }
        currentStatus.set(STATUS_COMMITTED);
    }

    @Override
    public void rollback() {
        if (currentStatus.get() != STATUS_ACTIVE) {
            throw new IllegalStateException("No existe transacción activa para revertir");
        }
        currentStatus.set(STATUS_ROLLEDBACK);
    }

    @Override
    public int getStatus() {
        return currentStatus.get();
    }

    @Override
    public void setRollbackOnly() {
        currentStatus.set(STATUS_ROLLEDBACK);
    }
}
