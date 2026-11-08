package io.jettra.collections.map.primitive;

import io.jettra.collections.api.LongIntMap;
import io.jettra.collections.api.LongIntProcedure;
import io.jettra.collections.util.HashUtils;

import java.util.Arrays;

/**
 * Open-addressing primitive map from long keys to int values.
 * Zero boxing for keys and values, and zero node entry objects.
 * Highly optimized for page free space tracking, offset indices, and physical slot mapping.
 */
public class LongIntHashMap implements LongIntMap {

    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.7f;
    private static final long FREE_KEY = 0L;

    private long[] keys;
    private int[] values;
    private int mask;
    private int size;
    private boolean hasFreeKey;
    private int freeValue;
    private int threshold;
    private final float loadFactor;

    public LongIntHashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public LongIntHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public LongIntHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) throw new IllegalArgumentException("Illegal capacity: " + initialCapacity);
        if (loadFactor <= 0 || Float.isNaN(loadFactor) || loadFactor >= 1.0f) {
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
        }
        int capacity = HashUtils.nextPowerOfTwo(Math.max(4, initialCapacity));
        this.loadFactor = loadFactor;
        this.keys = new long[capacity];
        this.values = new int[capacity];
        this.mask = capacity - 1;
        this.threshold = (int) (capacity * loadFactor);
        this.size = 0;
        this.hasFreeKey = false;
        this.freeValue = 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean containsKey(long key) {
        if (key == FREE_KEY) return hasFreeKey;
        long[] kTab = keys;
        int m = mask;
        int idx = HashUtils.hash(key) & m;
        long curr = kTab[idx];

        while (curr != FREE_KEY) {
            if (curr == key) return true;
            idx = (idx + 1) & m;
            curr = kTab[idx];
        }
        return false;
    }

    @Override
    public boolean containsValue(int value) {
        if (hasFreeKey && freeValue == value) return true;
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] != FREE_KEY && values[i] == value) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int get(long key) {
        return getIfAbsent(key, 0);
    }

    @Override
    public int getIfAbsent(long key, int defaultValue) {
        if (key == FREE_KEY) {
            return hasFreeKey ? freeValue : defaultValue;
        }
        long[] kTab = keys;
        int m = mask;
        int idx = HashUtils.hash(key) & m;
        long curr = kTab[idx];

        while (curr != FREE_KEY) {
            if (curr == key) {
                return values[idx];
            }
            idx = (idx + 1) & m;
            curr = kTab[idx];
        }
        return defaultValue;
    }

    @Override
    public int put(long key, int value) {
        if (key == FREE_KEY) {
            int oldVal = freeValue;
            freeValue = value;
            if (!hasFreeKey) {
                hasFreeKey = true;
                size++;
                if (size > threshold) rehash(keys.length << 1);
            }
            return oldVal;
        }

        long[] kTab = keys;
        int m = mask;
        int idx = HashUtils.hash(key) & m;
        long curr = kTab[idx];

        while (curr != FREE_KEY) {
            if (curr == key) {
                int oldVal = values[idx];
                values[idx] = value;
                return oldVal;
            }
            idx = (idx + 1) & m;
            curr = kTab[idx];
        }

        kTab[idx] = key;
        values[idx] = value;
        size++;
        if (size > threshold) {
            rehash(keys.length << 1);
        }
        return 0;
    }

    @Override
    public int removeKey(long key) {
        return removeKeyIfAbsent(key, 0);
    }

    @Override
    public int removeKeyIfAbsent(long key, int defaultValue) {
        if (key == FREE_KEY) {
            if (hasFreeKey) {
                hasFreeKey = false;
                size--;
                int oldVal = freeValue;
                freeValue = 0;
                return oldVal;
            }
            return defaultValue;
        }

        long[] kTab = keys;
        int m = mask;
        int idx = HashUtils.hash(key) & m;
        long curr = kTab[idx];

        while (curr != FREE_KEY) {
            if (curr == key) {
                int oldVal = values[idx];
                shiftKeysAndValues(idx);
                size--;
                return oldVal;
            }
            idx = (idx + 1) & m;
            curr = kTab[idx];
        }
        return defaultValue;
    }

    private void shiftKeysAndValues(int pos) {
        long[] kTab = keys;
        int[] vTab = values;
        int m = mask;
        int last = pos;
        long current;

        while (true) {
            pos = (pos + 1) & m;
            current = kTab[pos];
            if (current == FREE_KEY) {
                kTab[last] = FREE_KEY;
                vTab[last] = 0;
                return;
            }
            int slot = HashUtils.hash(current) & m;
            if (last <= pos ? (last >= slot || slot > pos) : (last >= slot && slot > pos)) {
                kTab[last] = current;
                vTab[last] = vTab[pos];
                last = pos;
            }
        }
    }

    private void rehash(int newCapacity) {
        long[] oldKeys = keys;
        int[] oldValues = values;
        long[] newKeys = new long[newCapacity];
        int[] newValues = new int[newCapacity];
        int newMask = newCapacity - 1;

        for (int i = 0; i < oldKeys.length; i++) {
            long k = oldKeys[i];
            if (k != FREE_KEY) {
                int idx = HashUtils.hash(k) & newMask;
                while (newKeys[idx] != FREE_KEY) {
                    idx = (idx + 1) & newMask;
                }
                newKeys[idx] = k;
                newValues[idx] = oldValues[i];
            }
        }

        this.keys = newKeys;
        this.values = newValues;
        this.mask = newMask;
        this.threshold = (int) (newCapacity * loadFactor);
    }

    @Override
    public void clear() {
        Arrays.fill(keys, FREE_KEY);
        Arrays.fill(values, 0);
        hasFreeKey = false;
        freeValue = 0;
        size = 0;
    }

    @Override
    public void forEachKeyValue(LongIntProcedure procedure) {
        if (hasFreeKey) {
            procedure.value(FREE_KEY, freeValue);
        }
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] != FREE_KEY) {
                procedure.value(keys[i], values[i]);
            }
        }
    }

    @Override
    public long[] keys() {
        long[] result = new long[size];
        int idx = 0;
        if (hasFreeKey) {
            result[idx++] = FREE_KEY;
        }
        for (long k : keys) {
            if (k != FREE_KEY) {
                result[idx++] = k;
            }
        }
        return result;
    }

    @Override
    public int[] values() {
        int[] result = new int[size];
        int idx = 0;
        if (hasFreeKey) {
            result[idx++] = freeValue;
        }
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] != FREE_KEY) {
                result[idx++] = values[i];
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        boolean[] first = {true};
        forEachKeyValue((k, v) -> {
            if (!first[0]) sb.append(", ");
            sb.append(k).append("=").append(v);
            first[0] = false;
        });
        return sb.append('}').toString();
    }
}
