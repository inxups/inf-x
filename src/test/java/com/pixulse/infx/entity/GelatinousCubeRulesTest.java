package com.pixulse.infx.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.item.equipment.CorrosionType;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

class GelatinousCubeRulesTest {
    @Test
    void acidOozeCorrodesGrassIntoDirtOnContact() {
        assertEquals(
                GelatinousCubeRules.INSTANT,
                GelatinousCubeRules.dissolvePeriod(
                        Blocks.GRASS_BLOCK.defaultBlockState(), CorrosionType.ACID));
        assertEquals(
                GelatinousCubeRules.INSTANT,
                GelatinousCubeRules.dissolvePeriod(
                        Blocks.MYCELIUM.defaultBlockState(), CorrosionType.ACID));
    }

    @Test
    void pepsinSlimesLeaveGrassUntouched() {
        assertEquals(
                GelatinousCubeRules.IMMUNE,
                GelatinousCubeRules.dissolvePeriod(
                        Blocks.GRASS_BLOCK.defaultBlockState(), CorrosionType.PEPSIN));
    }

    @Test
    void onlyAcidCubesScorchLivingGround() {
        assertTrue(GelatinousCubeRules.isAcidScorchableGround(
                Blocks.GRASS_BLOCK.defaultBlockState(), CorrosionType.ACID));
        assertTrue(GelatinousCubeRules.isAcidScorchableGround(
                Blocks.MYCELIUM.defaultBlockState(), CorrosionType.ACID));
        assertFalse(GelatinousCubeRules.isAcidScorchableGround(
                Blocks.GRASS_BLOCK.defaultBlockState(), CorrosionType.PEPSIN));
        assertFalse(GelatinousCubeRules.isAcidScorchableGround(
                Blocks.DIRT.defaultBlockState(), CorrosionType.ACID));
    }
}
