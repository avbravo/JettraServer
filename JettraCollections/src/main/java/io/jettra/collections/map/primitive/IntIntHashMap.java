package io.jettra.collections.map.primitive;

import io.jettra.collections.api.IntIntMap;
import io.jettra.collections.api.IntIntProcedure;
import io.jettra.collections.util.HashUtils;

import java.util.Arrays;

/**
 * Open-addressing primitive map from int keys to int values.
 * Uses parallel primitive arrays (int[] keys, int[] values) with backward-shift deletion.
 *
 * Memory footprint comparison:
 * - java.util.HashMap&lt;Integer, Integer&gt;:
 *   Each entry requires:
 *   - Integer key object: 16-24 bytes
 *   - Integer value object: 16-24 bytes
 *   - HashMap.Node object: 32 bytes
 *   - Entry pointer in table: 4-8 bytes
 *   Total: ~68 to 88+ bytes per entry!
 *
 * - IntIntHashMap:
 *   - Key (primitive int): 4 bytes
 *   - Value (primitive int): 4 bytes
 *   Total at 70% load factor: ~11.4 bytes per entry (87% RAM reduction!).
 */
public class IntIntHashMap implements IntIntMap {

    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.7f;
    private static final int FREE_KEY = 0;

    private int[] keys;
    private int[] values;
    private int mask;
    private int size;
    private boolean hasFreeKey;
    private int freeValue;
    private int threshold;
    private final float loadFactor;

    public IntIntHashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public IntIntHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public IntIntHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) throw new IllegalArgumentException("Illegal capacity: " + initialCapacity);
        if (loadFactor <= 0 || Float.isNaN(loadFactor) || loadFactor >= 1.0f) {
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
        }
        int capacity = HashUtils.nextPowerOfTwo(Math.max(4, initialCapacity));
        this.loadFactor = loadFactor;
        this.keys = new int[capacity];
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
    public boolean containsKey(int key) {
        if (key == FREE_KEY) return hasFreeKey;
        int[] kTab = keys;
        int m = mask;
        int idx = HashUtils.hash(key) & m;
        int curr = kTab[idx];

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
    public int get(int key) {
        return getIfAbsent(key, 0);
    }

    @Override
    public int getIfAbsent(int key, int defaultValue) {
        if (key == FREE_KEY) {
            return hasFreeKey ? freeValue : defaultValue;
        }
        int[] kTab = keys;
        int m = mask;
        int idx = HashUtils.hash(key) & m;
        int curr = kTab[idx];

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
    public int put(int key, int value) {
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

        int[] kTab = keys;
        int m = mask;
        int idx = HashUtils.hash(key) & m;
        int curr = kTab[idx];

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
    public int removeKey(int key) {
        return removeKeyIfAbsent(key, 0);
    }

    @Override
    public int removeKeyIfAbsent(int key, int defaultValue) {
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

        int[] kTab = keys;
        int m = mask;
        int idx = HashUtils.hash(key) & m;
        int curr = kTab[idx];

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
        int[] kTab = keys;
        int[] vTab = values;
        int m = mask;
        int last = pos;
        int current;

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
        int[] oldKeys = keys;
        int[] oldValues = values;
        int[] newKeys = new int[newCapacity];
        int[] newValues = new int[newCapacity];
        int newMask = newCapacity - 1;

        for (int i = 0; i < oldKeys.length; i++) {
            int k = oldKeys[i];
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
    public void forEachKeyValue(IntIntProcedure procedure) {
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
    public int[] keys() {
        int[] result = new int[size];
        int idx = 0;
        if (hasFreeKey) {
            result[idx++] = FREE_KEY;
        }
        for (int k : keys) {
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
