package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.event.StructureSafetyEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.world.level.biome.Biomes;
import org.junit.jupiter.api.Test;

class WorldgenRulesTest {
    @Test
    void riverClimateRoutingProducesAllThreeR196Variants() {
        assertEquals(RiverBiomes.DESERT_RIVER, RiverBiomes.select(.8F, .1F));
        assertEquals(RiverBiomes.JUNGLE_RIVER, RiverBiomes.select(.3F, .6F));
        assertEquals(RiverBiomes.SWAMP_RIVER, RiverBiomes.select(-.2F, .6F));
        assertNull(RiverBiomes.select(0.0F, 0.0F));
    }

    @Test
    void deepDarkAndMushroomFieldsAreExcludedFromOverworldGeneration() {
        HolderLookup.Provider registries = VanillaRegistries.createLookup();
        var biomes = registries.lookupOrThrow(Registries.BIOME);

        assertTrue(RiverBiomes.isRemovedFromOverworld(biomes.getOrThrow(Biomes.DEEP_DARK)));
        assertTrue(RiverBiomes.isRemovedFromOverworld(biomes.getOrThrow(Biomes.MUSHROOM_FIELDS)));
        assertFalse(RiverBiomes.isRemovedFromOverworld(biomes.getOrThrow(Biomes.PLAINS)));
    }

    @Test
    void pyramidWallTorchesFaceFromEachChestTowardTheCenter() {
        BlockPos center = new BlockPos(10, 20, 10);
        assertEquals(Direction.EAST, StructureSafetyEvents.towardCenter(new BlockPos(8, 19, 10), center));
        assertEquals(Direction.WEST, StructureSafetyEvents.towardCenter(new BlockPos(12, 19, 10), center));
        assertEquals(Direction.SOUTH, StructureSafetyEvents.towardCenter(new BlockPos(10, 19, 8), center));
        assertEquals(Direction.NORTH, StructureSafetyEvents.towardCenter(new BlockPos(10, 19, 12), center));
    }
}
