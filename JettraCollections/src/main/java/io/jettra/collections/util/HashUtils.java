package io.jettra.collections.util;

/**
 * High-performance hash code mixing and utility routines.
 * Uses MurmurHash3 finalization mix to eliminate clustering in open-addressing tables.
 */
public final class HashUtils {

    private HashUtils() {
    }

    /**
     * 32-bit integer finalization mix from MurmurHash3.
     */
    public static int hash(int k) {
        k ^= k >>> 16;
        k *= 0x85ebca6b;
        k ^= k >>> 13;
        k *= 0xc2b2ae35;
        k ^= k >>> 16;
        return k;
    }

    /**
     * 64-bit long finalization mix from MurmurHash3.
     */
    public static int hash(long k) {
        k ^= k >>> 33;
        k *= 0xff51afd7ed558ccdL;
        k ^= k >>> 33;
        k *= 0xc4ceb9fe1a85ec53L;
        k ^= k >>> 33;
        return (int) (k ^ (k >>> 32));
    }

    /**
     * Object hash with mixing.
     */
    public static int hash(Object o) {
        return o == null ? 0 : hash(o.hashCode());
    }

    /**
     * Calculates next power of two >= n.
     */
    public static int nextPowerOfTwo(int n) {
        if (n <= 0) {
            return 1;
        }
        if (n >= (1 << 30)) {
            return 1 << 30;
        }
        return Integer.highestOneBit(n - 1) << 1;
    }
}
