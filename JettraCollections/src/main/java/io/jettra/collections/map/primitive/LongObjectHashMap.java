package io.jettra.collections.map.primitive;

import io.jettra.collections.api.LongObjectMap;
import io.jettra.collections.api.LongObjectProcedure;
import io.jettra.collections.util.HashUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.LongFunction;

/**
 * Open-addressing primitive map from long keys to generic Object values.
 * Zero boxing for keys and zero entry wrapper nodes.
 */
public class LongObjectHashMap<V> implements LongObjectMap<V> {

    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.7f;
    private static final long FREE_KEY = 0L;

    private long[] keys;
    private Object[] values;
    private int mask;
    private int size;
    private boolean hasFreeKey;
    private V freeValue;
    private int threshold;
    private final float loadFactor;

    public LongObjectHashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public LongObjectHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public LongObjectHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) throw new IllegalArgumentException("Illegal capacity: " + initialCapacity);
        if (loadFactor <= 0 || Float.isNaN(loadFactor) || loadFactor >= 1.0f) {
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
        }
        int capacity = HashUtils.nextPowerOfTwo(Math.max(4, initialCapacity));
        this.loadFactor = loadFactor;
        this.keys = new long[capacity];
        this.values = new Object[capacity];
        this.mask = capacity - 1;
        this.threshold = (int) (capacity * loadFactor);
        this.size = 0;
        this.hasFreeKey = false;
        this.freeValue = null;
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
    public boolean containsValue(Object value) {
        if (hasFreeKey && Objects.equals(freeValue, value)) return true;
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] != FREE_KEY && Objects.equals(values[i], value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(long key) {
        return getIfAbsent(key, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public V getIfAbsent(long key, V defaultValue) {
        if (key == FREE_KEY) {
            return hasFreeKey ? freeValue : defaultValue;
        }
        long[] kTab = keys;
        int m = mask;
        int idx = HashUtils.hash(key) & m;
        long curr = kTab[idx];

        while (curr != FREE_KEY) {
            if (curr == key) {
                return (V) values[idx];
            }
            idx = (idx + 1) & m;
            curr = kTab[idx];
        }
        return defaultValue;
    }

    @Override
    public V getIfAbsentPut(long key, LongFunction<? extends V> factory) {
        V existing = get(key);
        if (existing != null || containsKey(key)) {
            return existing;
        }
        V created = factory.apply(key);
        put(key, created);
        return created;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V put(long key, V value) {
        if (key == FREE_KEY) {
            V oldVal = freeValue;
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
                V oldVal = (V) values[idx];
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
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V removeKey(long key) {
        if (key == FREE_KEY) {
            if (hasFreeKey) {
                hasFreeKey = false;
                size--;
                V oldVal = freeValue;
                freeValue = null;
                return oldVal;
            }
            return null;
        }

        long[] kTab = keys;
        int m = mask;
        int idx = HashUtils.hash(key) & m;
        long curr = kTab[idx];

        while (curr != FREE_KEY) {
            if (curr == key) {
                V oldVal = (V) values[idx];
                shiftKeysAndValues(idx);
                size--;
                return oldVal;
            }
            idx = (idx + 1) & m;
            curr = kTab[idx];
        }
        return null;
    }

    private void shiftKeysAndValues(int pos) {
        long[] kTab = keys;
        Object[] vTab = values;
        int m = mask;
        int last = pos;
        long current;

        while (true) {
            pos = (pos + 1) & m;
            current = kTab[pos];
            if (current == FREE_KEY) {
                kTab[last] = FREE_KEY;
                vTab[last] = null;
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
        Object[] oldValues = values;
        long[] newKeys = new long[newCapacity];
        Object[] newValues = new Object[newCapacity];
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
        Arrays.fill(values, null);
        hasFreeKey = false;
        freeValue = null;
        size = 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void forEachKeyValue(LongObjectProcedure<? super V> procedure) {
        if (hasFreeKey) {
            procedure.value(FREE_KEY, freeValue);
        }
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] != FREE_KEY) {
                procedure.value(keys[i], (V) values[i]);
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
    public Object[] values() {
        Object[] result = new Object[size];
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
