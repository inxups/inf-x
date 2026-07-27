package com.pixulse.infx.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
