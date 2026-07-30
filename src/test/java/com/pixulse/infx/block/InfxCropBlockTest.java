package com.pixulse.infx.block;

import static org.junit.jupiter.api.Assertions.assertSame;

import com.pixulse.infx.registry.InfXBlocks;
import org.junit.jupiter.api.Test;

class InfxCropBlockTest {
    @Test
    void pickedCropUsesItsOwnSeed() {
        for (var entry : InfXBlocks.INFX_CROPS) {
            InfxCropBlock crop = entry.get();
            assertSame(crop.type().seed(), crop.getBaseSeedId(), crop.type().name());
        }
    }
}
