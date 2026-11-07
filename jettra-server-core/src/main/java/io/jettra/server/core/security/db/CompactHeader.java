package io.jettra.server.core.security.db;

import java.io.Serializable;

/**
 * Cabecera binaria compacta para registros persistidos en JettraSecurityDB.
 */
public class CompactHeader implements Serializable {
    private static final long serialVersionUID = 1L;

    private String version = "1.0";
    private long timestamp = System.currentTimeMillis();
    private String nodeInfo = "local";

    public CompactHeader() {}

    public CompactHeader(String version, String nodeInfo) {
        this.version = version;
        this.nodeInfo = nodeInfo;
        this.timestamp = System.currentTimeMillis();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getNodeInfo() {
        return nodeInfo;
    }

    public void setNodeInfo(String nodeInfo) {
        this.nodeInfo = nodeInfo;
    }
}

