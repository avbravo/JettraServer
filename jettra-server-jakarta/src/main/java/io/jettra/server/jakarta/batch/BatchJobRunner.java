package io.jettra.server.jakarta.batch;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Motor ligero para trabajos en lote Jakarta Batch 2.1.
 */
public class BatchJobRunner {

    public enum BatchStatus {
        STARTING, STARTED, COMPLETED, FAILED
    }

    public record JobExecution(long executionId, String jobName, BatchStatus status) {}

    public interface ItemReader<T> {
        T readItem() throws Exception;
    }

    public interface ItemProcessor<I, O> {
        O processItem(I item) throws Exception;
    }

    public interface ItemWriter<O> {
        void writeItems(List<O> items) throws Exception;
    }

    private static final AtomicLong ID_SEQ = new AtomicLong(1);

    public static <I, O> JobExecution executeChunk(String jobName, ItemReader<I> reader, ItemProcessor<I, O> processor, ItemWriter<O> writer, int chunkSize) {
        long execId = ID_SEQ.incrementAndGet();
        try {
            java.util.List<O> chunk = new java.util.ArrayList<>(chunkSize);
            I item;
            while ((item = reader.readItem()) != null) {
                O processed = processor.processItem(item);
                if (processed != null) {
                    chunk.add(processed);
                }
                if (chunk.size() >= chunkSize) {
                    writer.writeItems(chunk);
                    chunk.clear();
                }
            }
            if (!chunk.isEmpty()) {
                writer.writeItems(chunk);
            }
            return new JobExecution(execId, jobName, BatchStatus.COMPLETED);
        } catch (Exception e) {
            return new JobExecution(execId, jobName, BatchStatus.FAILED);
        }
    }
}
