package io.jettra.collections;

import io.jettra.collections.list.primitive.IntArrayList;
import io.jettra.collections.map.primitive.IntIntHashMap;
import io.jettra.collections.memory.MemoryLayoutReport;
import io.jettra.collections.set.primitive.IntHashSet;

/**
 * Interactive demonstration of JettraCollections memory layout and operations.
 */
public class JettraCollectionsDemo {

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("   JETTRACOLLECTIONS - HIGH-PERFORMANCE MEMORY CONTAINERS        ");
        System.out.println("   Java 25 Primitive & Zero-Allocation Open-Addressing Library  ");
        System.out.println("=================================================================\n");

        // 1. Memory Analysis Report for 1,000,000 items
        System.out.println(MemoryLayoutReport.generateReport(1_000_000));

        // 2. Functional Primitive Demo
        System.out.println("\n--- 1. Primitive IntArrayList Operations ---");
        IntArrayList list = IntArrayList.of(10, 25, 30, 45, 50, 65, 70, 85, 90, 100);
        System.out.println("Original list: " + list);
        System.out.println("Sum: " + list.sum() + ", Min: " + list.min().orElse(0) + ", Max: " + list.max().orElse(0));
        System.out.println("Filter (evens): " + list.select(x -> x % 2 == 0));
        System.out.println("Transformed (* 2): " + list.collect(x -> x * 2));

        // 3. IntHashSet Demo
        System.out.println("\n--- 2. Primitive IntHashSet Operations ---");
        IntHashSet set = new IntHashSet();
        set.add(42);
        set.add(100);
        set.add(0); // Zero support
        set.add(42); // Duplicate
        System.out.println("Set size: " + set.size() + " (contains 42: " + set.contains(42) + ", contains 99: " + set.contains(99) + ")");
        System.out.println("Set contents: " + set);

        // 4. IntIntHashMap Demo
        System.out.println("\n--- 3. Primitive IntIntHashMap Operations ---");
        IntIntHashMap map = new IntIntHashMap();
        map.put(101, 5500);
        map.put(102, 7200);
        map.put(0, 9999);
        System.out.println("Map size: " + map.size());
        System.out.println("Employee 101 Salary: $" + map.get(101));
        System.out.println("Employee 0 Salary: $" + map.get(0));
        System.out.println("Map iteration: ");
        map.forEachKeyValue((k, v) -> System.out.println("   [ID: " + k + " -> Value: " + v + "]"));

        System.out.println("\n>>> JettraCollections loaded successfully!");
    }
}
