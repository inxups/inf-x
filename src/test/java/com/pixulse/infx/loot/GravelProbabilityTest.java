package com.pixulse.infx.loot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.EnumMap;
import java.util.Random;
import org.junit.jupiter.api.Test;

class GravelProbabilityTest {
    @Test
    void seededStatisticalSampleMatchesEveryR196Branch() {
        int samples = 2_624_400;
        Random random = new Random(196L);
        EnumMap<GravelDrop, Integer> counts = new EnumMap<>(GravelDrop.class);
        for (int i = 0; i < samples; i++) {
            counts.merge(GravelDropSelector.select(0, random::nextInt), 1, Integer::sum);
        }
        assertRate(counts, GravelDrop.GRAVEL, samples, 3.0 / 4.0, .003);
        assertRate(counts, GravelDrop.FLINT_CHIP, samples, 5.0 / 32.0, .003);
        assertRate(counts, GravelDrop.FLINT, samples, 1.0 / 96.0, .001);
        assertRate(counts, GravelDrop.COPPER_NUGGET, samples, 1.0 / 18.0, .002);
        assertRate(counts, GravelDrop.SILVER_NUGGET, samples, 1.0 / 54.0, .001);
        assertRate(counts, GravelDrop.GOLD_NUGGET, samples, 1.0 / 162.0, .0005);
        assertRate(counts, GravelDrop.OBSIDIAN_SHARD, samples, 1.0 / 486.0, .00025);
        assertRate(counts, GravelDrop.EMERALD_SHARD, samples, 1.0 / 1458.0, .00012);
        assertRate(counts, GravelDrop.DIAMOND_SHARD, samples, 1.0 / 4374.0, .00007);
        assertRate(counts, GravelDrop.MITHRIL_NUGGET, samples, 1.0 / 13122.0, .00004);
        assertRate(counts, GravelDrop.ADAMANTIUM_NUGGET, samples, 1.0 / 26244.0, .00003);
        assertEquals(samples, counts.values().stream().mapToInt(Integer::intValue).sum());
    }

    private static void assertRate(
            EnumMap<GravelDrop, Integer> counts,
            GravelDrop drop,
            int samples,
            double expected,
            double tolerance) {
        assertEquals(expected, counts.getOrDefault(drop, 0) / (double) samples, tolerance, drop.name());
    }
}
