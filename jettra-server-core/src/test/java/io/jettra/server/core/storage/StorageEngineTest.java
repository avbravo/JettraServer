package io.jettra.server.core.storage;

import io.jettra.server.core.config.ServerConfig;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.Test;
import io.jettra.test.core.JettraAssert;
import java.io.File;
import java.nio.file.Path;

/**
 * Pruebas unitarias de persistencia para los tres modos de almacenamiento de JettraServer.
 * Utiliza 100% JettraTest sin ninguna dependencia de JUnit.
 */
public class StorageEngineTest {

    @Test
    @DisplayName("Verificar persistencia en modo JVM_RAM")
    public void testJvmRamStorage() {
        JvmRamStorageEngine<String, String> engine = new JvmRamStorageEngine<>();
        JettraAssert.assertEquals(StorageMode.JVM_RAM, engine.getMode(), "El modo debe ser JVM_RAM");

        engine.put("user:101", "Alice");
        engine.put("user:102", "Bob");

        JettraAssert.assertTrue(engine.containsKey("user:101"), "Debe contener user:101");
        JettraAssert.assertEquals("Alice", engine.get("user:101"), "El valor debe ser Alice");
        JettraAssert.assertEquals(2L, engine.size(), "El tamaño debe ser 2");

        String removed = engine.remove("user:101");
        JettraAssert.assertEquals("Alice", removed, "El valor eliminado debe ser Alice");
        JettraAssert.assertFalse(engine.containsKey("user:101"), "No debe contener user:101");
        JettraAssert.assertEquals(1L, engine.size(), "El tamaño debe ser 1");

        StorageMetrics metrics = engine.getMetrics();
        JettraAssert.assertEquals(StorageMode.JVM_RAM, metrics.mode(), "Métricas deben reflejar JVM_RAM");
        JettraAssert.assertTrue(metrics.readOperations() > 0, "Debe registrar lecturas");

        engine.close();
    }

    @Test
    @DisplayName("Verificar persistencia en modo OFF_HEAP con Páginas Ranuradas")
    public void testOffHeapSlottedPageStorage() {
        OffHeapSlottedPageEngine<String, String> engine = new OffHeapSlottedPageEngine<>(32 * 1024, String.class);
        JettraAssert.assertEquals(StorageMode.OFF_HEAP, engine.getMode(), "El modo debe ser OFF_HEAP");

        engine.put("doc:01", "Contenido Off-Heap en Panama FFM");
        engine.put("doc:02", "Segundo documento de alta velocidad");

        JettraAssert.assertTrue(engine.containsKey("doc:01"), "Debe contener doc:01");
        JettraAssert.assertEquals("Contenido Off-Heap en Panama FFM", engine.get("doc:01"), "Debe recuperar contenido idéntico");
        JettraAssert.assertEquals(2L, engine.size(), "El tamaño debe ser 2");

        String prev = engine.remove("doc:01");
        JettraAssert.assertEquals("Contenido Off-Heap en Panama FFM", prev, "Valor eliminado correcto");
        JettraAssert.assertFalse(engine.containsKey("doc:01"), "doc:01 no debe existir");

        StorageMetrics metrics = engine.getMetrics();
        JettraAssert.assertEquals(StorageMode.OFF_HEAP, metrics.mode(), "Modo OFF_HEAP");
        JettraAssert.assertTrue(metrics.memoryUsedBytes() > 0, "Debe reportar memoria nativa asignada");

        engine.close();
    }

    @Test
    @DisplayName("Verificar persistencia en modo DISK_MEMORY con JettraMemory LSM")
    public void testDiskMemoryStorage() {
        Path tempDir = Path.of(System.getProperty("java.io.tmpdir"), "jettra-disk-test-" + System.currentTimeMillis());
        DiskMemoryStorageEngine<String, String> engine = new DiskMemoryStorageEngine<>(tempDir, String.class);
        JettraAssert.assertEquals(StorageMode.DISK_MEMORY, engine.getMode(), "El modo debe ser DISK_MEMORY");

        engine.put("entity:55", "Datos Persistentes en Disco Off-Heap");
        JettraAssert.assertTrue(engine.containsKey("entity:55"), "Debe contener la clave");
        JettraAssert.assertEquals("Datos Persistentes en Disco Off-Heap", engine.get("entity:55"), "Valor recuperado de disco");

        engine.flush();
        JettraAssert.assertEquals(1L, engine.size(), "El tamaño debe ser 1");

        engine.close();

        // Limpiar directorio temporal
        deleteDir(tempDir.toFile());
    }

    @Test
    @DisplayName("Verificar StorageEngineFactory e inicialización dinámica")
    public void testStorageEngineFactory() {
        ServerConfig config = new ServerConfig();
        config.setStorageMode(StorageMode.JVM_RAM);

        JettraStorageEngine<String, Object> engine = StorageEngineFactory.initDefaultEngine(config);
        JettraAssert.assertEquals(StorageMode.JVM_RAM, engine.getMode(), "Debe ser JVM_RAM");

        engine.put("config:env", "production");
        JettraAssert.assertEquals("production", engine.get("config:env"), "Debe recuperar valor");

        // Conmutar a OFF_HEAP
        StorageEngineFactory.switchDefaultEngine(StorageMode.OFF_HEAP, config);
        JettraStorageEngine<String, Object> newEngine = StorageEngineFactory.getDefaultEngine();
        JettraAssert.assertEquals(StorageMode.OFF_HEAP, newEngine.getMode(), "Nuevo motor debe ser OFF_HEAP");
        JettraAssert.assertEquals("production", newEngine.get("config:env"), "Datos deben migrarse en la conmutación");

        StorageEngineFactory.closeAll();
    }

    private static void deleteDir(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) deleteDir(f);
            }
        }
        dir.delete();
    }
}
