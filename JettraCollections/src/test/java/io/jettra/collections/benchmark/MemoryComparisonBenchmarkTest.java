package io.jettra.collections.benchmark;

import io.jettra.collections.list.primitive.IntArrayList;
import io.jettra.collections.map.primitive.IntIntHashMap;
import io.jettra.collections.memory.MemoryFootprint;
import io.jettra.collections.set.primitive.IntHashSet;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static io.jettra.test.core.JettraAssert.assertTrue;

@NotRequiresRunningServer
public class MemoryComparisonBenchmarkTest {

    @Test
    public void testFootprintComparison() {
        int count = 100_000;

        long jcfList = MemoryFootprint.estimateArrayListOfInteger(count);
        long jetList = MemoryFootprint.estimateIntArrayList(count);
        double listSavings = (double) (jcfList - jetList) / jcfList;

        long jcfSet = MemoryFootprint.estimateHashSetOfInteger(count);
        long jetSet = MemoryFootprint.estimateIntHashSet(count);
        double setSavings = (double) (jcfSet - jetSet) / jcfSet;

        long jcfMap = MemoryFootprint.estimateHashMapOfInteger(count);
        long jetMap = MemoryFootprint.estimateIntIntHashMap(count);
        double mapSavings = (double) (jcfMap - jetMap) / jcfMap;

        System.out.printf("[BENCHMARK 100k items]%n List savings: %.2f%%%n Set savings: %.2f%%%n Map savings: %.2f%%%n",
                listSavings * 100, setSavings * 100, mapSavings * 100);

        assertTrue(listSavings > 0.70, "List memory savings should be > 70%");
        assertTrue(setSavings > 0.75, "Set memory savings should be > 75%");
        assertTrue(mapSavings > 0.75, "Map memory savings should be > 75%");
    }

    @Test
    public void testThroughputIntListVsArrayList() {
        int n = 500_000;

        // Warmup & Jettra
        long t0 = System.nanoTime();
        IntArrayList jetList = new IntArrayList(n);
        for (int i = 0; i < n; i++) {
            jetList.add(i);
        }
        long sumJet = jetList.sum();
        long tJet = System.nanoTime() - t0;

        // JCF
        t0 = System.nanoTime();
        ArrayList<Integer> jcfList = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            jcfList.add(i); // boxing occurs here
        }
        long sumJcf = 0;
        for (int v : jcfList) { // unboxing occurs here
            sumJcf += v;
        }
        long tJcf = System.nanoTime() - t0;

        assertTrue(sumJet == sumJcf);
        System.out.printf("[THROUGHPUT 500k inserts+sum]%n Jettra: %.2f ms%n JCF:    %.2f ms%n Speedup: %.2fx%n",
                tJet / 1_000_000.0, tJcf / 1_000_000.0, (double) tJcf / tJet);
    }
}
