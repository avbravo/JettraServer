package io.jettra.collections.set;

import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import java.util.Iterator;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class UnifiedSetTest {

    @Test
    public void testBasicOperations() {
        UnifiedSet<String> set = new UnifiedSet<>();
        assertTrue(set.add("Alpha"));
        assertTrue(set.add("Beta"));
        assertTrue(set.add("Gamma"));
        assertFalse(set.add("Alpha")); // Duplicate
        assertEquals(3, set.size());

        assertTrue(set.contains("Alpha"));
        assertTrue(set.contains("Beta"));
        assertFalse(set.contains("Delta"));

        assertTrue(set.remove("Beta"));
        assertEquals(2, set.size());
        assertFalse(set.contains("Beta"));
    }

    @Test
    public void testNullKey() {
        UnifiedSet<String> set = new UnifiedSet<>();
        assertTrue(set.add(null));
        assertEquals(1, set.size());
        assertTrue(set.contains(null));
        assertTrue(set.remove(null));
        assertFalse(set.contains(null));
        assertEquals(0, set.size());
    }

    @Test
    public void testIterator() {
        UnifiedSet<String> set = UnifiedSet.of("A", "B", "C");
        int count = 0;
        for (String s : set) {
            assertNotNull(s);
            count++;
        }
        assertEquals(3, count);
    }
}
