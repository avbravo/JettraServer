package io.jettra.server.jakarta.jms;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Motor ligero de mensajería asíncrona compatible con Jakarta Messaging (JMS 3.1).
 */
public class JmsBroker {

    public interface Destination {
        String getName();
    }

    public record Queue(String name) implements Destination {
        @Override public String getName() { return name; }
    }

    public record Topic(String name) implements Destination {
        @Override public String getName() { return name; }
    }

    public record JmsTextMessage(String text, Map<String, Object> properties) {}

    private final Map<String, BlockingQueue<JmsTextMessage>> queues = new ConcurrentHashMap<>();

    public Queue createQueue(String name) {
        queues.computeIfAbsent(name, k -> new LinkedBlockingQueue<>());
        return new Queue(name);
    }

    public void send(Queue queue, JmsTextMessage message) {
        BlockingQueue<JmsTextMessage> q = queues.computeIfAbsent(queue.name(), k -> new LinkedBlockingQueue<>());
        q.offer(message);
    }

    public JmsTextMessage receive(Queue queue, long timeoutMillis) throws InterruptedException {
        BlockingQueue<JmsTextMessage> q = queues.computeIfAbsent(queue.name(), k -> new LinkedBlockingQueue<>());
        return q.poll(timeoutMillis, TimeUnit.MILLISECONDS);
    }

    public int queueSize(Queue queue) {
        BlockingQueue<JmsTextMessage> q = queues.get(queue.name());
        return q != null ? q.size() : 0;
    }
}
