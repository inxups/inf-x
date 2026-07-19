package com.pixulse.infx.loot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NetherGravelMappingTest {
    @Test
    void netherGravelMapsMetalAndGemBranchesToR196Peers() {
        for (GravelDrop drop : new GravelDrop[]{
                GravelDrop.COPPER_NUGGET,
                GravelDrop.SILVER_NUGGET,
                GravelDrop.MITHRIL_NUGGET,
                GravelDrop.ADAMANTIUM_NUGGET}) {
            assertEquals(GravelDrop.GOLD_NUGGET, GravelLootModifier.netherDrop(drop));
        }
        for (GravelDrop drop : new GravelDrop[]{
                GravelDrop.OBSIDIAN_SHARD,
                GravelDrop.EMERALD_SHARD,
                GravelDrop.DIAMOND_SHARD}) {
            assertEquals(GravelDrop.NETHER_QUARTZ_SHARD, GravelLootModifier.netherDrop(drop));
        }
        assertEquals(GravelDrop.FLINT_CHIP, GravelLootModifier.netherDrop(GravelDrop.FLINT_CHIP));
    }
}
