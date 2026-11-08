package io.jettra.collections.map;

import io.jettra.collections.map.primitive.IntIntHashMap;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class IntIntHashMapTest {

    @Test
    public void testPutGetAndContains() {
        IntIntHashMap map = new IntIntHashMap();
        map.put(1, 100);
        map.put(2, 200);
        map.put(0, 500); // Key 0 test

        assertEquals(3, map.size());
        assertEquals(100, map.get(1));
        assertEquals(200, map.get(2));
        assertEquals(500, map.get(0));
        assertEquals(-1, map.getIfAbsent(999, -1));

        assertTrue(map.containsKey(1));
        assertTrue(map.containsKey(0));
        assertFalse(map.containsKey(3));

        assertTrue(map.containsValue(100));
        assertTrue(map.containsValue(500));
        assertFalse(map.containsValue(999));
    }

    @Test
    public void testUpdateAndRemove() {
        IntIntHashMap map = new IntIntHashMap();
        map.put(10, 50);
        assertEquals(50, map.put(10, 60)); // update
        assertEquals(60, map.get(10));

        map.put(0, 111);
        assertEquals(111, map.removeKey(0));
        assertFalse(map.containsKey(0));

        assertEquals(60, map.removeKey(10));
        assertFalse(map.containsKey(10));
        assertEquals(0, map.size());
    }

    @Test
    public void testLargeScaleRehash() {
        IntIntHashMap map = new IntIntHashMap(4);
        for (int i = 0; i < 2000; i++) {
            map.put(i, i * 2);
        }
        assertEquals(2000, map.size());

        for (int i = 0; i < 2000; i++) {
            assertEquals(i * 2, map.get(i));
        }

        for (int i = 0; i < 1000; i++) {
            assertEquals(i * 2, map.removeKey(i));
        }
        assertEquals(1000, map.size());

        for (int i = 1000; i < 2000; i++) {
            assertEquals(i * 2, map.get(i));
        }
    }

    @Test
    public void testIteration() {
        IntIntHashMap map = new IntIntHashMap();
        map.put(1, 10);
        map.put(2, 20);
        map.put(0, 30);

        int[] count = {0};
        int[] sumKeys = {0};
        int[] sumVals = {0};
        map.forEachKeyValue((k, v) -> {
            count[0]++;
            sumKeys[0] += k;
            sumVals[0] += v;
        });

        assertEquals(3, count[0]);
        assertEquals(3, sumKeys[0]);
        assertEquals(60, sumVals[0]);
    }
}
