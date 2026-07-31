package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

class InfXUnderworldLargeCaveCarverTest {
    @Test
    void macroCentersRemainStableAcrossZeroAndNegativeCoordinates() {
        assertTrue(InfXUnderworldLargeCaveCarver.isMacroCenter(new ChunkPos(8, 8)));
        assertFalse(InfXUnderworldLargeCaveCarver.isMacroCenter(new ChunkPos(7, 8)));
        assertTrue(InfXUnderworldLargeCaveCarver.isMacroCenter(new ChunkPos(-8, -8)));
        assertFalse(InfXUnderworldLargeCaveCarver.isMacroCenter(new ChunkPos(-9, -8)));
        assertTrue(InfXUnderworldLargeCaveCarver.isMacroCenter(new ChunkPos(-24, 8)));
        assertFalse(InfXUnderworldLargeCaveCarver.isMacroCenter(new ChunkPos(-16, 8)));
    }

    @Test
    void theMainDomeUsesTheConfiguredVerticalBoundsAndFourSideWings() {
        int lowerRelativeY = Underworld.LARGE_CAVE_MIN_Y - Underworld.LARGE_CAVE_CENTER_Y;
        int upperRelativeY = Underworld.LARGE_CAVE_MAX_Y - Underworld.LARGE_CAVE_CENTER_Y;

        assertFalse(InfXUnderworldLargeCaveCarver.isInsideCave(0, lowerRelativeY, 0));
        assertTrue(InfXUnderworldLargeCaveCarver.isInsideCave(0, lowerRelativeY + 1, 0));
        assertTrue(InfXUnderworldLargeCaveCarver.isInsideCave(0, upperRelativeY - 1, 0));
        assertFalse(InfXUnderworldLargeCaveCarver.isInsideCave(0, upperRelativeY, 0));
        assertTrue(InfXUnderworldLargeCaveCarver.isInsideCave(95, 0, 0));
        assertTrue(InfXUnderworldLargeCaveCarver.isInsideCave(127, 0, 0));
        assertTrue(InfXUnderworldLargeCaveCarver.isInsideCave(-127, 0, 0));
        assertFalse(InfXUnderworldLargeCaveCarver.isInsideCave(128, 0, 0));
    }

    @Test
    void onlyStoneDeepslateAndTheConfiguredInternalBedrockCanBeCarved() {
        assertTrue(InfXUnderworldLargeCaveCarver.canReplace(Blocks.STONE.defaultBlockState(), 0));
        assertTrue(InfXUnderworldLargeCaveCarver.canReplace(Blocks.DEEPSLATE.defaultBlockState(), 0));
        assertTrue(InfXUnderworldLargeCaveCarver.canReplace(Blocks.BEDROCK.defaultBlockState(), -24));
        assertFalse(InfXUnderworldLargeCaveCarver.canReplace(Blocks.BEDROCK.defaultBlockState(), -25));
        assertFalse(InfXUnderworldLargeCaveCarver.canReplace(Blocks.BEDROCK.defaultBlockState(), -8));
        assertFalse(InfXUnderworldLargeCaveCarver.canReplace(Blocks.AIR.defaultBlockState(), 0));
        assertFalse(InfXUnderworldLargeCaveCarver.canReplace(Blocks.WATER.defaultBlockState(), 0));
    }

    @Test
    void carvingChecksTheTargetBiomeAtEachBlockColumn() {
        HolderOwner<Biome> owner = new HolderOwner<>() {};
        Holder<Biome> deepDark = Holder.Reference.createStandAlone(owner, Underworld.DEEP_DARK_BIOME);
        Holder<Biome> ordinary = Holder.Reference.createStandAlone(owner, Underworld.BIOME);
        Function<BlockPos, Holder<Biome>> deepDarkGetter = position -> deepDark;
        Function<BlockPos, Holder<Biome>> ordinaryGetter = position -> ordinary;

        assertTrue(InfXUnderworldLargeCaveCarver.isDeepDark(deepDarkGetter, 0, 0));
        assertFalse(InfXUnderworldLargeCaveCarver.isDeepDark(ordinaryGetter, 0, 0));
    }
}
