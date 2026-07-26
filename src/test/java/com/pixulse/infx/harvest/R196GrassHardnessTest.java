package com.pixulse.infx.harvest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class R196GrassHardnessTest {
    @Test
    void locksTheMiteTallGrassHardness() {
        assertEquals(0.02F, R196GrassHardness.TALL_GRASS_HARDNESS);
    }

    @Test
    void mapsTheFourModernFormsOfMiteTallGrass() {
        assertTrue(R196GrassHardness.appliesTo(Identifier.withDefaultNamespace("short_grass")));
        assertTrue(R196GrassHardness.appliesTo(Identifier.withDefaultNamespace("tall_grass")));
        assertTrue(R196GrassHardness.appliesTo(Identifier.withDefaultNamespace("fern")));
        assertTrue(R196GrassHardness.appliesTo(Identifier.withDefaultNamespace("large_fern")));
    }

    @Test
    void leavesGrassBlockAndUnrelatedPlantsOnTheirOwnMappings() {
        assertFalse(R196GrassHardness.appliesTo(Identifier.withDefaultNamespace("grass_block")));
        assertFalse(R196GrassHardness.appliesTo(Identifier.withDefaultNamespace("short_dry_grass")));
        assertFalse(R196GrassHardness.appliesTo(Identifier.fromNamespaceAndPath("infx", "short_grass")));
    }
}
