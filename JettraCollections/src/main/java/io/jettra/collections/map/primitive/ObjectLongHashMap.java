package io.jettra.collections.map.primitive;

import io.jettra.collections.api.ObjectLongMap;
import io.jettra.collections.api.ObjectLongProcedure;
import io.jettra.collections.util.HashUtils;

import java.util.Arrays;

/**
 * Open-addressing primitive map from generic Object keys to primitive long values.
 * Zero boxing for values and zero entry wrapper nodes.
 */
public class ObjectLongHashMap<K> implements ObjectLongMap<K> {

    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.7f;
    private static final Object NULL_KEY = new Object();

    private Object[] keys;
    private long[] values;
    private int mask;
    private int size;
    private int threshold;
    private final float loadFactor;

    public ObjectLongHashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public ObjectLongHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public ObjectLongHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) throw new IllegalArgumentException("Illegal capacity: " + initialCapacity);
        if (loadFactor <= 0 || Float.isNaN(loadFactor) || loadFactor >= 1.0f) {
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
        }
        int capacity = HashUtils.nextPowerOfTwo(Math.max(4, initialCapacity));
        this.loadFactor = loadFactor;
        this.keys = new Object[capacity];
        this.values = new long[capacity];
        this.mask = capacity - 1;
        this.threshold = (int) (capacity * loadFactor);
        this.size = 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean containsKey(Object key) {
        Object k = maskNull(key);
        Object[] kTab = keys;
        int m = mask;
        int idx = HashUtils.hash(k) & m;
        Object curr = kTab[idx];

        while (curr != null) {
            if (curr.equals(k)) return true;
            idx = (idx + 1) & m;
            curr = kTab[idx];
        }
        return false;
    }

    @Override
    public long get(K key) {
        return getIfAbsent(key, 0L);
    }

    @Override
    public long getIfAbsent(K key, long defaultValue) {
        Object k = maskNull(key);
        Object[] kTab = keys;
        int m = mask;
        int idx = HashUtils.hash(k) & m;
        Object curr = kTab[idx];

        while (curr != null) {
            if (curr.equals(k)) {
                return values[idx];
            }
            idx = (idx + 1) & m;
            curr = kTab[idx];
        }
        return defaultValue;
    }

    @Override
    public long put(K key, long value) {
        Object k = maskNull(key);
        Object[] kTab = keys;
        int m = mask;
        int idx = HashUtils.hash(k) & m;
        Object curr = kTab[idx];

        while (curr != null) {
            if (curr.equals(k)) {
                long oldVal = values[idx];
                values[idx] = value;
                return oldVal;
            }
            idx = (idx + 1) & m;
            curr = kTab[idx];
        }

        kTab[idx] = k;
        values[idx] = value;
        size++;
        if (size > threshold) {
            rehash(keys.length << 1);
        }
        return 0L;
    }

    @Override
    public long removeKey(K key) {
        return removeKeyIfAbsent(key, 0L);
    }

    @Override
    public long removeKeyIfAbsent(K key, long defaultValue) {
        Object k = maskNull(key);
        Object[] kTab = keys;
        int m = mask;
        int idx = HashUtils.hash(k) & m;
        Object curr = kTab[idx];

        while (curr != null) {
            if (curr.equals(k)) {
                long oldVal = values[idx];
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
        Object[] kTab = keys;
        long[] vTab = values;
        int m = mask;
        int last = pos;
        Object current;

        while (true) {
            pos = (pos + 1) & m;
            current = kTab[pos];
            if (current == null) {
                kTab[last] = null;
                vTab[last] = 0L;
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
        Object[] oldKeys = keys;
        long[] oldValues = values;
        Object[] newKeys = new Object[newCapacity];
        long[] newValues = new long[newCapacity];
        int newMask = newCapacity - 1;

        for (int i = 0; i < oldKeys.length; i++) {
            Object k = oldKeys[i];
            if (k != null) {
                int idx = HashUtils.hash(k) & newMask;
                while (newKeys[idx] != null) {
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
        Arrays.fill(keys, null);
        Arrays.fill(values, 0L);
        size = 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void forEachKeyValue(ObjectLongProcedure<? super K> procedure) {
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] != null) {
                procedure.value((K) unmaskNull(keys[i]), values[i]);
            }
        }
    }

    private static Object maskNull(Object o) {
        return o == null ? NULL_KEY : o;
    }

    private static Object unmaskNull(Object o) {
        return o == NULL_KEY ? null : o;
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
