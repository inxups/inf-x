package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.datafixers.util.Pair;
import com.pixulse.infx.event.StructureSafetyEvents;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import org.junit.jupiter.api.Test;

class WorldgenRulesTest {
    @Test
    void riverRoutingFollowsTheAdjacentBiomeCells() {
        assertEquals(RiverBiomes.DESERT_RIVER, RiverBiomes.select(.8F, .1F)); // desert cell: hot
        assertEquals(RiverBiomes.JUNGLE_RIVER, RiverBiomes.select(.3F, .6F)); // jungle cell: warm and humid
        assertEquals(RiverBiomes.JUNGLE_RIVER, RiverBiomes.select(.3F, .15F)); // jungle humidity starts at 0.1
        assertNull(RiverBiomes.select(-.2F, .6F)); // temperate cell keeps the vanilla river
        assertNull(RiverBiomes.select(.3F, 0.0F)); // warm but dry cell keeps the vanilla river
        assertNull(RiverBiomes.select(0.0F, 0.0F));
        // cell classification mirrors select(): desert cell, jungle cells, everything else
        assertEquals(RiverBiomes.DESERT_RIVER, RiverBiomes.riverVariantForCell(3, 0, Biomes.RIVER));
        assertEquals(RiverBiomes.DESERT_RIVER, RiverBiomes.riverVariantForCell(3, 4, Biomes.RIVER));
        assertEquals(RiverBiomes.JUNGLE_RIVER, RiverBiomes.riverVariantForCell(2, 3, Biomes.RIVER));
        assertEquals(RiverBiomes.JUNGLE_RIVER, RiverBiomes.riverVariantForCell(2, 4, Biomes.RIVER));
        assertEquals(Biomes.RIVER, RiverBiomes.riverVariantForCell(2, 2, Biomes.RIVER));
        assertEquals(Biomes.RIVER, RiverBiomes.riverVariantForCell(1, 4, Biomes.RIVER));
        assertEquals(Biomes.RIVER, RiverBiomes.riverVariantForCell(0, 0, Biomes.RIVER));
    }

    @Test
    void valleySwampPointsBecomeTheSwampRiver() {
        HolderLookup.Provider registries = VanillaRegistries.createLookup();
        HolderLookup.RegistryLookup<Biome> biomes = registries.lookupOrThrow(Registries.BIOME);
        List<Pair<Climate.ParameterPoint, Holder<Biome>>> points =
                new MultiNoiseBiomeSourceParameterList(MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD, biomes)
                        .parameters()
                        .values();
        List<Pair<Climate.ParameterPoint, Holder<Biome>>> valleySwamps = points.stream()
                .filter(pair -> pair.getSecond().is(Biomes.SWAMP) || pair.getSecond().is(Biomes.MANGROVE_SWAMP))
                .filter(pair -> RiverBiomes.replacementFor(
                        pair.getFirst(), pair.getSecond().unwrapKey().orElse(null)) != null)
                .toList();
        assertFalse(valleySwamps.isEmpty(), "vanilla places swamps on the valley weirdness slice");
        for (Pair<Climate.ParameterPoint, Holder<Biome>> pair : valleySwamps) {
            assertAll(
                    "valley swamp becomes the swamp river",
                    () -> assertEquals(
                            RiverBiomes.SWAMP_RIVER,
                            RiverBiomes.replacementFor(
                                    pair.getFirst(), pair.getSecond().unwrapKey().orElse(null))));
        }
        long midSliceSwamps = points.stream()
                .filter(pair -> pair.getSecond().is(Biomes.SWAMP))
                .filter(pair -> !RiverBiomes.isValleySlice(pair.getFirst()))
                .count();
        assertTrue(midSliceSwamps > 0, "swamp biomes must survive on other weirdness slices");
    }

    @Test
    void riverPointsSplitByTheBiomeCellGrid() {
        HolderLookup.Provider registries = VanillaRegistries.createLookup();
        HolderLookup.RegistryLookup<Biome> biomes = registries.lookupOrThrow(Registries.BIOME);
        List<Pair<Climate.ParameterPoint, Holder<Biome>>> riverPoints =
                new MultiNoiseBiomeSourceParameterList(MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD, biomes)
                        .parameters()
                        .values()
                        .stream()
                        .filter(pair -> pair.getSecond().is(Biomes.RIVER))
                        .toList();
        assertFalse(riverPoints.isEmpty(), "vanilla places unfrozen rivers on the valley slice");
        for (Pair<Climate.ParameterPoint, Holder<Biome>> pair : riverPoints) {
            List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> split =
                    RiverBiomes.splitRiverCells(pair.getFirst());
            assertAll(
                    "river point split into the 4x5 biome cells",
                    () -> assertEquals(20, split.size()),
                    () -> assertTrue(
                            split.stream().anyMatch(p -> p.getSecond().equals(RiverBiomes.DESERT_RIVER)),
                            "desert cell yields the desert river"),
                    () -> assertTrue(
                            split.stream().anyMatch(p -> p.getSecond().equals(RiverBiomes.JUNGLE_RIVER)),
                            "jungle cells yield the jungle river"),
                    () -> assertTrue(
                            split.stream().anyMatch(p -> p.getSecond().equals(Biomes.RIVER)),
                            "other cells keep the vanilla river"),
                    () -> assertTrue(
                            split.stream()
                                    .filter(p -> p.getSecond().equals(RiverBiomes.DESERT_RIVER))
                                    .allMatch(p -> p.getFirst().temperature().min() == Climate.quantizeCoord(0.55F)),
                            "desert river stays inside the hot temperature cell"),
                    () -> assertTrue(
                            split.stream()
                                    .filter(p -> p.getSecond().equals(RiverBiomes.JUNGLE_RIVER))
                                    .allMatch(p -> p.getFirst().humidity().min() >= Climate.quantizeCoord(0.1F)),
                            "jungle river stays inside the jungle humidity cells"));
        }
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
