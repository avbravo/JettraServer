package io.jettra.flux.sync;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JettraSyncManager {
    
    public static final long SERVER_START_TIME = System.currentTimeMillis();
    
    public static class SyncInfo {
        public final SyncType type;
        public final String user;
        public final long timestamp;

        public SyncInfo(SyncType type, String user, long timestamp) {
            this.type = type;
            this.user = user;
            this.timestamp = timestamp;
        }
    }

    private static final Map<String, SyncInfo> lastChanges = new ConcurrentHashMap<>();

    public static void notifyChange(String entity, SyncType type, String user) {
        if (entity == null || entity.isEmpty()) return;
        lastChanges.put(entity, new SyncInfo(type, user, System.currentTimeMillis()));
        System.out.println("[JettraSyncManager] Notified " + type + " for " + entity + " by " + user);
    }

    public static SyncInfo getLastChange(String entity) {
        if (entity == null) return null;
        return lastChanges.get(entity);
    }
}
