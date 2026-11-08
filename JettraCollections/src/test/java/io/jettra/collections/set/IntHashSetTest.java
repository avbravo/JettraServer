package io.jettra.collections.set;

import io.jettra.collections.api.IntSet;
import io.jettra.collections.set.primitive.IntHashSet;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import java.util.Arrays;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class IntHashSetTest {

    @Test
    public void testAddContainsAndSize() {
        IntHashSet set = new IntHashSet();
        assertTrue(set.add(10));
        assertTrue(set.add(20));
        assertTrue(set.add(0)); // Zero test
        assertFalse(set.add(10)); // Duplicate
        assertEquals(3, set.size());

        assertTrue(set.contains(10));
        assertTrue(set.contains(20));
        assertTrue(set.contains(0));
        assertFalse(set.contains(30));
    }

    @Test
    public void testRemoveAndBackwardShift() {
        IntHashSet set = new IntHashSet(8);
        for (int i = 0; i < 50; i++) {
            set.add(i);
        }
        assertEquals(50, set.size());

        for (int i = 0; i < 25; i++) {
            assertTrue(set.remove(i));
        }
        assertEquals(25, set.size());

        for (int i = 0; i < 25; i++) {
            assertFalse(set.contains(i));
        }
        for (int i = 25; i < 50; i++) {
            assertTrue(set.contains(i));
        }
    }

    @Test
    public void testRehash() {
        IntHashSet set = new IntHashSet(4);
        for (int i = 1; i <= 1000; i++) {
            set.add(i);
        }
        assertEquals(1000, set.size());
        for (int i = 1; i <= 1000; i++) {
            assertTrue(set.contains(i));
        }
    }

    @Test
    public void testImmutableSet() {
        IntHashSet set = IntHashSet.of(5, 3, 8, 1, 3);
        IntSet immutable = set.toImmutable();
        assertEquals(4, immutable.size());
        assertTrue(immutable.contains(5));
        assertTrue(immutable.contains(8));
        assertFalse(immutable.contains(99));
        assertThrows(UnsupportedOperationException.class, () -> immutable.add(10));
        assertThrows(UnsupportedOperationException.class, () -> immutable.remove(5));

        int[] arr = immutable.toArray();
        int[] sorted = arr.clone();
        Arrays.sort(sorted);
        assertArrayEquals(sorted, arr); // Backed by sorted array!
    }
}
