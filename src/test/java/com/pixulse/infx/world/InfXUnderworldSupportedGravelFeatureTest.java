package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

class InfXUnderworldSupportedGravelFeatureTest {
    @Test
    void onlySolidBlocksSupportGravel() {
        assertTrue(InfXUnderworldSupportedGravelFeature.isSupported(Blocks.STONE.defaultBlockState()));
        assertTrue(InfXUnderworldSupportedGravelFeature.isSupported(Blocks.BEDROCK.defaultBlockState()));
        assertFalse(InfXUnderworldSupportedGravelFeature.isSupported(Blocks.AIR.defaultBlockState()));
        assertFalse(InfXUnderworldSupportedGravelFeature.isSupported(Blocks.WATER.defaultBlockState()));
    }
}
