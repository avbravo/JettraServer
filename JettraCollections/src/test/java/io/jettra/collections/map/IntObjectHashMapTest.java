package io.jettra.collections.map;

import io.jettra.collections.map.primitive.IntObjectHashMap;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class IntObjectHashMapTest {

    @Test
    public void testBasicOperations() {
        IntObjectHashMap<String> map = new IntObjectHashMap<>();
        map.put(1, "One");
        map.put(2, "Two");
        map.put(0, "Zero");

        assertEquals(3, map.size());
        assertEquals("One", map.get(1));
        assertEquals("Two", map.get(2));
        assertEquals("Zero", map.get(0));
        assertNull(map.get(999));

        assertEquals("Zero", map.removeKey(0));
        assertFalse(map.containsKey(0));
        assertEquals(2, map.size());
    }

    @Test
    public void testGetIfAbsentPut() {
        IntObjectHashMap<String> map = new IntObjectHashMap<>();
        String val = map.getIfAbsentPut(42, key -> "ValueFor" + key);
        assertEquals("ValueFor42", val);
        assertEquals(1, map.size());
        assertEquals("ValueFor42", map.get(42));
    }
}
