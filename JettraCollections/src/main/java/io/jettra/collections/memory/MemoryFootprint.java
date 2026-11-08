package io.jettra.collections.memory;

/**
 * Utility for calculating exact JVM memory footprints (64-bit JVM with Compressed OOPs).
 *
 * JVM Object Overhead Reference:
 * - Standard Object Header: 12 bytes mark/class word + 4 bytes padding = 16 bytes.
 * - Array Header: 16 bytes (12 bytes mark/class + 4 bytes length).
 * - Compressed Reference (OOP): 4 bytes.
 * - Primitive int: 4 bytes.
 * - Primitive long / double: 8 bytes.
 * - java.lang.Integer: 16 bytes object header + 4 bytes int + 4 bytes padding = 24 bytes.
 * - java.lang.Long / Double: 16 bytes object header + 8 bytes value = 24 bytes.
 * - java.util.HashMap$Node: 16 bytes header + 4 bytes hash + 3 refs (key, val, next: 12 bytes) = 32 bytes.
 */
public final class MemoryFootprint {

    private MemoryFootprint() {}

    /**
     * Estimated bytes for java.util.ArrayList&lt;Integer&gt; with n elements.
     * Container + element array (4 bytes per ref) + n * java.lang.Integer objects (24 bytes each).
     */
    public static long estimateArrayListOfInteger(int n) {
        if (n == 0) return 24 + 16;
        long arrayHeader = 16;
        long refArray = arrayHeader + ((long) n * 4);
        refArray = align8(refArray);
        long wrapperObjects = (long) n * 24; // 24 bytes per Integer
        long listHeader = 24; // ArrayList object itself
        return listHeader + refArray + wrapperObjects;
    }

    /**
     * Estimated bytes for JettraCollections IntArrayList with n elements.
     * Container + flat int[] array (4 bytes per int). Zero wrapper objects!
     */
    public static long estimateIntArrayList(int n) {
        if (n == 0) return 24 + 16;
        long arrayHeader = 16;
        long primitiveArray = arrayHeader + ((long) n * 4);
        primitiveArray = align8(primitiveArray);
        long listHeader = 24;
        return listHeader + primitiveArray;
    }

    /**
     * Estimated bytes for java.util.HashSet&lt;Integer&gt; with n elements.
     * Backed by HashMap: table of Node refs (capacity * 4), n * Node objects (32 bytes each),
     * n * Integer objects (24 bytes each).
     */
    public static long estimateHashSetOfInteger(int n) {
        if (n == 0) return 48;
        int capacity = tableCapacity(n, 0.75f);
        long tableArray = align8(16 + ((long) capacity * 4));
        long nodes = (long) n * 32;
        long wrappers = (long) n * 24;
        long mapHeader = 48;
        return mapHeader + tableArray + nodes + wrappers;
    }

    /**
     * Estimated bytes for JettraCollections IntHashSet with n elements.
     * Backed by flat int[] table (capacity * 4). Zero Node objects! Zero wrappers!
     */
    public static long estimateIntHashSet(int n) {
        if (n == 0) return 32 + 16;
        int capacity = tableCapacity(n, 0.7f);
        long tableArray = align8(16 + ((long) capacity * 4));
        long setHeader = 32;
        return setHeader + tableArray;
    }

    /**
     * Estimated bytes for java.util.HashMap&lt;Integer, Integer&gt; with n elements.
     * Table of Node refs (capacity * 4), n * Node objects (32 bytes),
     * n * Integer key objects (24 bytes), n * Integer value objects (24 bytes).
     */
    public static long estimateHashMapOfInteger(int n) {
        if (n == 0) return 48;
        int capacity = tableCapacity(n, 0.75f);
        long tableArray = align8(16 + ((long) capacity * 4));
        long nodes = (long) n * 32;
        long keyWrappers = (long) n * 24;
        long valWrappers = (long) n * 24;
        long mapHeader = 48;
        return mapHeader + tableArray + nodes + keyWrappers + valWrappers;
    }

    /**
     * Estimated bytes for JettraCollections IntIntHashMap with n elements.
     * Parallel primitive arrays: int[] keys (capacity * 4) + int[] values (capacity * 4).
     * Zero Node objects! Zero wrappers!
     */
    public static long estimateIntIntHashMap(int n) {
        if (n == 0) return 40 + 32;
        int capacity = tableCapacity(n, 0.7f);
        long keysArray = align8(16 + ((long) capacity * 4));
        long valsArray = align8(16 + ((long) capacity * 4));
        long mapHeader = 40;
        return mapHeader + keysArray + valsArray;
    }

    /**
     * Estimated bytes for UnifiedSet&lt;E&gt; vs java.util.HashSet&lt;E&gt;.
     * UnifiedSet has no Node objects (saves 32 bytes per element container overhead).
     */
    public static long estimateUnifiedSet(int n) {
        if (n == 0) return 32 + 16;
        int capacity = tableCapacity(n, 0.75f);
        long tableArray = align8(16 + ((long) capacity * 4));
        long setHeader = 32;
        return setHeader + tableArray;
    }

    /**
     * Estimated bytes for UnifiedMap&lt;K, V&gt; vs java.util.HashMap&lt;K, V&gt;.
     * UnifiedMap has no Node objects (saves 32 bytes per element container overhead).
     */
    public static long estimateUnifiedMap(int n) {
        if (n == 0) return 32 + 16;
        int capacity = tableCapacity(n, 0.75f);
        long tableArray = align8(16 + ((long) capacity * 8)); // 2 refs per slot (key, value)
        long mapHeader = 32;
        return mapHeader + tableArray;
    }

    private static int tableCapacity(int n, float loadFactor) {
        int cap = (int) Math.ceil(n / loadFactor);
        int highestOneBit = Integer.highestOneBit(cap);
        return cap == highestOneBit ? cap : highestOneBit << 1;
    }

    private static long align8(long bytes) {
        return (bytes + 7) & ~7;
    }
}
