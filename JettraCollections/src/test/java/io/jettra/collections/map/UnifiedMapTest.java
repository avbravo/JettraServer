package io.jettra.collections.map;

import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import java.util.Map;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class UnifiedMapTest {

    @Test
    public void testMapOperations() {
        UnifiedMap<String, Integer> map = new UnifiedMap<>();
        assertNull(map.put("Alice", 100));
        assertNull(map.put("Bob", 200));
        assertNull(map.put("Charlie", 300));
        assertEquals(100, map.put("Alice", 150)); // update

        assertEquals(3, map.size());
        assertEquals(150, map.get("Alice"));
        assertEquals(200, map.get("Bob"));
        assertNull(map.get("David"));

        assertTrue(map.containsKey("Charlie"));
        assertTrue(map.containsValue(200));

        assertEquals(200, map.remove("Bob"));
        assertFalse(map.containsKey("Bob"));
        assertEquals(2, map.size());
    }

    @Test
    public void testNullKey() {
        UnifiedMap<String, String> map = new UnifiedMap<>();
        map.put(null, "NullValue");
        assertEquals(1, map.size());
        assertTrue(map.containsKey(null));
        assertEquals("NullValue", map.get(null));
        assertEquals("NullValue", map.remove(null));
        assertFalse(map.containsKey(null));
    }

    @Test
    public void testEntrySet() {
        UnifiedMap<String, Integer> map = new UnifiedMap<>();
        map.put("K1", 1);
        map.put("K2", 2);

        int count = 0;
        int sum = 0;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            count++;
            sum += entry.getValue();
        }
        assertEquals(2, count);
        assertEquals(3, sum);
    }
}
