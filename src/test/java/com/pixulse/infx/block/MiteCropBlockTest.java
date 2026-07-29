package com.pixulse.infx.block;

import static org.junit.jupiter.api.Assertions.assertSame;

import com.pixulse.infx.registry.InfXBlocks;
import org.junit.jupiter.api.Test;

class MiteCropBlockTest {
    @Test
    void pickedCropUsesItsOwnSeed() {
        for (var entry : InfXBlocks.MITE_CROPS) {
            MiteCropBlock crop = entry.get();
            assertSame(crop.type().seed(), crop.getBaseSeedId(), crop.type().name());
        }
    }
}
