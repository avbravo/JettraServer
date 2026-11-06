package io.jettra.server.jakarta.mail;

import java.util.Properties;

/**
 * Cliente y modelo de mensajes para Jakarta Mail 2.1.
 */
public class MailClient {

    public record MailMessage(String from, String to, String subject, String body) {}

    private final Properties properties;

    public MailClient(Properties properties) {
        this.properties = properties != null ? properties : new Properties();
    }

    public void send(MailMessage message) {
        // En JettraServer registra el despacho del mensaje por Virtual Threads
        System.out.println("[Jakarta Mail] Enviando correo de <" + message.from() + "> para <" + message.to() + ">: " + message.subject());
    }

    public Properties getProperties() {
        return properties;
    }
}
