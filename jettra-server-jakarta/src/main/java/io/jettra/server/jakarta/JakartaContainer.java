package io.jettra.server.jakarta;

import io.jettra.server.core.JettraServer;
import io.jettra.server.jakarta.cdi.CdiContainer;
import io.jettra.server.jakarta.concurrency.ManagedExecutorService;
import io.jettra.server.jakarta.jms.JmsBroker;
import io.jettra.server.jakarta.json.Jsonb;
import io.jettra.server.jakarta.mail.MailClient;
import io.jettra.server.jakarta.persistence.EntityManagerFactory;
import io.jettra.server.jakarta.rest.RestDispatcher;
import io.jettra.server.jakarta.servlet.ServletBridge;
import io.jettra.server.jakarta.servlet.ServletContext;
import io.jettra.server.jakarta.transaction.JettraTransactionManager;
import io.jettra.server.jakarta.websocket.WebSocketManager;
import java.util.Properties;

/**
 * Contenedor Maestro que orquesta el conjunto completo de especificaciones Jakarta EE dentro de JettraServer.
 */
public class JakartaContainer {

    private final JettraServer server;
    private final RestDispatcher restDispatcher;
    private final CdiContainer cdiContainer;
    private final EntityManagerFactory entityManagerFactory;
    private final JettraTransactionManager transactionManager;
    private final ServletBridge servletBridge;
    private final WebSocketManager webSocketManager;
    private final ManagedExecutorService executorService;
    private final JmsBroker jmsBroker;
    private final Jsonb jsonb;
    private final MailClient mailClient;

    public JakartaContainer(JettraServer server) {
        this.server = server;
        this.restDispatcher = new RestDispatcher(server.getRouter());
        this.cdiContainer = new CdiContainer();
        this.entityManagerFactory = EntityManagerFactory.create(server.storage());
        this.transactionManager = new JettraTransactionManager();
        this.servletBridge = new ServletBridge(server.getRouter(), new ServletContext(server.getConfig().getContextPath()));
        this.webSocketManager = new WebSocketManager();
        this.executorService = new ManagedExecutorService();
        this.jmsBroker = new JmsBroker();
        this.jsonb = Jsonb.create();
        this.mailClient = new MailClient(new Properties());

        // Registrar servicios de infraestructura en el contenedor CDI
        cdiContainer.registerSingleton(JettraServer.class, server);
        cdiContainer.registerSingleton(EntityManagerFactory.class, entityManagerFactory);
        cdiContainer.registerSingleton(JettraTransactionManager.class, transactionManager);
        cdiContainer.registerSingleton(ManagedExecutorService.class, executorService);
        cdiContainer.registerSingleton(JmsBroker.class, jmsBroker);
        cdiContainer.registerSingleton(Jsonb.class, jsonb);
    }

    public static JakartaContainer attach(JettraServer server) {
        return new JakartaContainer(server);
    }

    public JettraServer getServer() { return server; }
    public RestDispatcher getRestDispatcher() { return restDispatcher; }
    public CdiContainer getCdiContainer() { return cdiContainer; }
    public EntityManagerFactory getEntityManagerFactory() { return entityManagerFactory; }
    public JettraTransactionManager getTransactionManager() { return transactionManager; }
    public ServletBridge getServletBridge() { return servletBridge; }
    public WebSocketManager getWebSocketManager() { return webSocketManager; }
    public ManagedExecutorService getExecutorService() { return executorService; }
    public JmsBroker getJmsBroker() { return jmsBroker; }
    public Jsonb getJsonb() { return jsonb; }
    public MailClient getMailClient() { return mailClient; }
}
