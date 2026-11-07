package io.jettra.server.core.security.db;

import io.jettra.server.core.security.entity.JAccreditation;
import io.jettra.server.core.security.entity.JCredential;
import io.jettra.server.core.security.entity.JRole;
import io.jettra.server.core.security.entity.JUser;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Motor de persistencia nativo embebido de objetos para seguridad en JettraServer (JettraSecurityDB).
 * Almacena entidades serializadas en carpetas por colección db/securitydb/<coleccion>/<id>.jdb
 * con control estricto de concurrencia mediante cerrojos de proceso (FileLock) y de JVM (ReentrantReadWriteLock).
 */
public class JettraSecurityDB {

    private static final String DEFAULT_DIR = System.getProperty("user.dir") + File.separator + "db" + File.separator + "securitydb";
    private final String baseDir;
    private static final ConcurrentHashMap<Class<?>, ReentrantReadWriteLock> locks = new ConcurrentHashMap<>();

    public JettraSecurityDB() {
        this(System.getProperty("jettra.securitydb.path", DEFAULT_DIR));
    }

    public JettraSecurityDB(String baseDir) {
        this.baseDir = (baseDir != null && !baseDir.isBlank()) ? baseDir : DEFAULT_DIR;
        init();
    }

    private void init() {
        File dir = new File(baseDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public String getBaseDir() {
        return baseDir;
    }

    private ReentrantReadWriteLock getLock(Class<?> clazz) {
        return locks.computeIfAbsent(clazz, k -> new ReentrantReadWriteLock(true));
    }

    private File getCollectionDir(Class<?> clazz) {
        File dir = new File(baseDir + File.separator + clazz.getSimpleName().toLowerCase());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public <T extends Serializable> void save(String id, T entity) {
        Class<?> clazz = entity.getClass();
        ReentrantReadWriteLock.WriteLock writeLock = getLock(clazz).writeLock();
        writeLock.lock();
        FileOutputStream fos = null;
        FileLock fileLock = null;
        try {
            File dir = getCollectionDir(clazz);
            File file = new File(dir, id + ".jdb");

            fos = new FileOutputStream(file);
            FileChannel channel = fos.getChannel();
            fileLock = channel.lock();

            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(new CompactHeader("1.0", "jettra-server"));
            oos.writeObject(entity);
            oos.flush();
        } catch (IOException e) {
            throw new RuntimeException("Error persistiendo entidad en JettraSecurityDB: " + id, e);
        } finally {
            if (fileLock != null) {
                try {
                    fileLock.release();
                } catch (IOException ignored) {}
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ignored) {}
            }
            writeLock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Serializable> Optional<T> findById(Class<T> clazz, String id) {
        ReentrantReadWriteLock.ReadLock readLock = getLock(clazz).readLock();
        readLock.lock();
        FileInputStream fis = null;
        FileLock fileLock = null;
        try {
            File dir = getCollectionDir(clazz);
            File file = new File(dir, id + ".jdb");
            if (!file.exists()) {
                return Optional.empty();
            }

            fis = new FileInputStream(file);
            FileChannel channel = fis.getChannel();
            fileLock = channel.lock(0L, Long.MAX_VALUE, true);

            ObjectInputStream ois = new ObjectInputStream(fis);
            ois.readObject(); // Lee CompactHeader
            T entity = readEntity(ois, clazz);
            return Optional.of(entity);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Error leyendo entidad de JettraSecurityDB: " + id, e);
        } finally {
            if (fileLock != null) {
                try {
                    fileLock.release();
                } catch (IOException ignored) {}
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ignored) {}
            }
            readLock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Serializable> List<T> findAll(Class<T> clazz) {
        ReentrantReadWriteLock.ReadLock readLock = getLock(clazz).readLock();
        readLock.lock();
        try {
            File dir = getCollectionDir(clazz);
            List<T> results = new ArrayList<>();

            File[] files = dir.listFiles((d, name) -> name.endsWith(".jdb"));
            if (files != null) {
                for (File file : files) {
                    FileInputStream fis = null;
                    FileLock fileLock = null;
                    try {
                        fis = new FileInputStream(file);
                        FileChannel channel = fis.getChannel();
                        fileLock = channel.lock(0L, Long.MAX_VALUE, true);

                        ObjectInputStream ois = new ObjectInputStream(fis);
                        ois.readObject(); // Lee CompactHeader
                        T entity = readEntity(ois, clazz);
                        results.add(entity);
                    } catch (FileNotFoundException ignored) {
                    } catch (Exception e) {
                        System.err.println("[JettraSecurityDB] Aviso leyendo archivo: " + file.getName() + " -> " + e.getMessage());
                    } finally {
                        if (fileLock != null) {
                            try {
                                fileLock.release();
                            } catch (IOException ignored) {}
                        }
                        if (fis != null) {
                            try {
                                fis.close();
                            } catch (IOException ignored) {}
                        }
                    }
                }
            }
            return results;
        } finally {
            readLock.unlock();
        }
    }

    public <T extends Serializable> void delete(Class<T> clazz, String id) {
        ReentrantReadWriteLock.WriteLock writeLock = getLock(clazz).writeLock();
        writeLock.lock();
        try {
            File dir = getCollectionDir(clazz);
            File file = new File(dir, id + ".jdb");
            if (file.exists()) {
                file.delete();
            }
        } finally {
            writeLock.unlock();
        }
    }

    public <T extends Serializable> List<T> search(Class<T> clazz, Predicate<T> predicate) {
        return findAll(clazz).stream().filter(predicate).collect(Collectors.toList());
    }

    public <T extends Serializable> List<T> search(Class<T> clazz, String query) {
        return search(clazz, JettraQueryParser.parse(query, clazz));
    }

    @SuppressWarnings("unchecked")
    private <T extends Serializable> T readEntity(ObjectInputStream input, Class<T> clazz)
            throws IOException, ClassNotFoundException {
        Object entity = input.readObject();
        if (clazz.isInstance(entity)) {
            return clazz.cast(entity);
        }

        Object migrated = migrateLegacyEntity(entity);
        if (clazz.isInstance(migrated)) {
            return clazz.cast(migrated);
        }

        throw new InvalidObjectException("Tipo de entidad inesperada: " + entity.getClass().getName()
                + "; esperado: " + clazz.getName());
    }

    private Object migrateLegacyEntity(Object entity) throws InvalidObjectException {
        if (entity == null) return null;
        String clsName = entity.getClass().getName();
        try {
            if (clsName.endsWith(".JRole")) {
                UUID id = (UUID) entity.getClass().getMethod("id").invoke(entity);
                String name = (String) entity.getClass().getMethod("name").invoke(entity);
                Boolean active = (Boolean) entity.getClass().getMethod("active").invoke(entity);
                return new JRole(id, name, active);
            }
            if (clsName.endsWith(".JUser")) {
                UUID id = (UUID) entity.getClass().getMethod("id").invoke(entity);
                String firstName = (String) entity.getClass().getMethod("firstName").invoke(entity);
                String lastName = (String) entity.getClass().getMethod("lastName").invoke(entity);
                String email = (String) entity.getClass().getMethod("email").invoke(entity);
                String phone = (String) entity.getClass().getMethod("phone").invoke(entity);
                Boolean active = (Boolean) entity.getClass().getMethod("active").invoke(entity);
                return new JUser(id, firstName, lastName, email, phone, active, Set.of());
            }
            if (clsName.endsWith(".JCredential")) {
                UUID id = (UUID) entity.getClass().getMethod("id").invoke(entity);
                Object legacyUser = entity.getClass().getMethod("jUser").invoke(entity);
                String username = (String) entity.getClass().getMethod("username").invoke(entity);
                String passwordHash = (String) entity.getClass().getMethod("passwordHash").invoke(entity);
                Boolean active = (Boolean) entity.getClass().getMethod("active").invoke(entity);
                return new JCredential(id, (JUser) migrateLegacyEntity(legacyUser), username, passwordHash, active, null);
            }
            if (clsName.endsWith(".JAccreditation")) {
                UUID id = (UUID) entity.getClass().getMethod("id").invoke(entity);
                Object legacyRole = entity.getClass().getMethod("jRole").invoke(entity);
                String feature = (String) entity.getClass().getMethod("feature").invoke(entity);
                Boolean active = (Boolean) entity.getClass().getMethod("active").invoke(entity);
                return new JAccreditation(id, (JRole) migrateLegacyEntity(legacyRole), feature, active);
            }
        } catch (Exception e) {
            throw new InvalidObjectException("Error migrando entidad legacy " + clsName + ": " + e.getMessage());
        }
        throw new InvalidObjectException("Entidad de seguridad no compatible: " + clsName);
    }
}

