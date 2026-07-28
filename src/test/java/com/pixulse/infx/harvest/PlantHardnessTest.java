package com.pixulse.infx.harvest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class PlantHardnessTest {
    @Test
    void locksTheMappedPlantHardnessValues() {
        assertEquals(0.02F, PlantHardness.TALL_GRASS_HARDNESS);
        assertEquals(0.02F, PlantHardness.SUGAR_CANE_HARDNESS);
    }

    @Test
    void mapsTheModernFormsOfTallGrassAndSugarCane() {
        assertTrue(PlantHardness.appliesTo(Identifier.withDefaultNamespace("short_grass")));
        assertTrue(PlantHardness.appliesTo(Identifier.withDefaultNamespace("tall_grass")));
        assertTrue(PlantHardness.appliesTo(Identifier.withDefaultNamespace("fern")));
        assertTrue(PlantHardness.appliesTo(Identifier.withDefaultNamespace("large_fern")));
        Identifier sugarCane = Identifier.withDefaultNamespace("sugar_cane");
        assertTrue(PlantHardness.appliesTo(sugarCane));
        assertEquals(PlantHardness.SUGAR_CANE_HARDNESS, PlantHardness.destroyTime(sugarCane));
    }

    @Test
    void leavesUnmappedPlantsOnTheirOwnMappings() {
        assertFalse(PlantHardness.appliesTo(Identifier.withDefaultNamespace("grass_block")));
        assertFalse(PlantHardness.appliesTo(Identifier.withDefaultNamespace("short_dry_grass")));
        assertFalse(PlantHardness.appliesTo(Identifier.fromNamespaceAndPath("infx", "sugar_cane")));
    }
}
