package com.pixulse.infx.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.equipment.R196CorrosionType;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

class R196GelatinousCubeRulesTest {
    @Test
    void acidOozeCorrodesGrassIntoDirtOnContact() {
        assertEquals(
                R196GelatinousCubeRules.INSTANT,
                R196GelatinousCubeRules.dissolvePeriod(
                        Blocks.GRASS_BLOCK.defaultBlockState(), R196CorrosionType.ACID));
        assertEquals(
                R196GelatinousCubeRules.INSTANT,
                R196GelatinousCubeRules.dissolvePeriod(
                        Blocks.MYCELIUM.defaultBlockState(), R196CorrosionType.ACID));
    }

    @Test
    void pepsinSlimesLeaveGrassUntouched() {
        assertEquals(
                R196GelatinousCubeRules.IMMUNE,
                R196GelatinousCubeRules.dissolvePeriod(
                        Blocks.GRASS_BLOCK.defaultBlockState(), R196CorrosionType.PEPSIN));
    }

    @Test
    void onlyAcidCubesScorchLivingGround() {
        assertTrue(R196GelatinousCubeRules.isAcidScorchableGround(
                Blocks.GRASS_BLOCK.defaultBlockState(), R196CorrosionType.ACID));
        assertTrue(R196GelatinousCubeRules.isAcidScorchableGround(
                Blocks.MYCELIUM.defaultBlockState(), R196CorrosionType.ACID));
        assertFalse(R196GelatinousCubeRules.isAcidScorchableGround(
                Blocks.GRASS_BLOCK.defaultBlockState(), R196CorrosionType.PEPSIN));
        assertFalse(R196GelatinousCubeRules.isAcidScorchableGround(
                Blocks.DIRT.defaultBlockState(), R196CorrosionType.ACID));
    }
}
