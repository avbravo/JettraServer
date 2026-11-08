package io.jettra.collections.list;

import io.jettra.collections.api.IntIterator;
import io.jettra.collections.api.IntList;
import io.jettra.collections.list.primitive.IntArrayList;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class IntArrayListTest {

    @Test
    public void testAddAndGet() {
        IntArrayList list = new IntArrayList();
        for (int i = 0; i < 100; i++) {
            list.add(i * 10);
        }
        assertEquals(100, list.size());
        for (int i = 0; i < 100; i++) {
            assertEquals(i * 10, list.get(i));
        }
    }

    @Test
    public void testAddAtIndexAndRemove() {
        IntArrayList list = IntArrayList.of(1, 2, 4, 5);
        list.addAtIndex(2, 3);
        assertArrayEquals(new int[]{1, 2, 3, 4, 5}, list.toArray());

        int removed = list.removeAtIndex(2);
        assertEquals(3, removed);
        assertArrayEquals(new int[]{1, 2, 4, 5}, list.toArray());

        assertTrue(list.remove(4));
        assertFalse(list.remove(99));
        assertArrayEquals(new int[]{1, 2, 5}, list.toArray());
    }

    @Test
    public void testFunctionalOperations() {
        IntArrayList list = IntArrayList.of(1, 2, 3, 4, 5, 6);
        assertEquals(21, list.sum());
        assertEquals(1, list.min().orElse(0));
        assertEquals(6, list.max().orElse(0));
        assertEquals(3.5, list.average().orElse(0.0), 0.001);

        IntList evens = list.select(x -> x % 2 == 0);
        assertArrayEquals(new int[]{2, 4, 6}, evens.toArray());

        IntList odds = list.reject(x -> x % 2 == 0);
        assertArrayEquals(new int[]{1, 3, 5}, odds.toArray());

        IntList doubled = list.collect(x -> x * 2);
        assertArrayEquals(new int[]{2, 4, 6, 8, 10, 12}, doubled.toArray());
    }

    @Test
    public void testSortAndReverse() {
        IntArrayList list = IntArrayList.of(5, 2, 8, 1, 9);
        list.sortThis();
        assertArrayEquals(new int[]{1, 2, 5, 8, 9}, list.toArray());
        assertEquals(2, list.binarySearch(5));

        list.reverseThis();
        assertArrayEquals(new int[]{9, 8, 5, 2, 1}, list.toArray());
    }

    @Test
    public void testIterator() {
        IntArrayList list = IntArrayList.of(10, 20, 30);
        IntIterator it = list.intIterator();
        assertTrue(it.hasNext());
        assertEquals(10, it.nextInt());
        assertTrue(it.hasNext());
        assertEquals(20, it.nextInt());
        assertTrue(it.hasNext());
        assertEquals(30, it.nextInt());
        assertFalse(it.hasNext());
    }

    @Test
    public void testImmutable() {
        IntArrayList list = IntArrayList.of(1, 2, 3);
        IntList immutable = list.toImmutable();
        assertEquals(3, immutable.size());
        assertEquals(2, immutable.get(1));
        assertThrows(UnsupportedOperationException.class, () -> immutable.add(4));
        assertThrows(UnsupportedOperationException.class, () -> immutable.removeAtIndex(0));
    }
}
