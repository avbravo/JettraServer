package io.jettra.server.jakarta.transaction;

/**
 * Coordinador de transacciones Jakarta Transactions (JTA 2.0).
 */
public interface UserTransaction {
    void begin() throws Exception;
    void commit() throws Exception;
    void rollback() throws Exception;
    int getStatus();
    void setRollbackOnly() throws Exception;

    int STATUS_ACTIVE = 0;
    int STATUS_COMMITTED = 1;
    int STATUS_ROLLEDBACK = 2;
    int STATUS_NO_TRANSACTION = 3;
}
