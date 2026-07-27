package com.pixulse.infx.harvest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class R196PlantHardnessTest {
    @Test
    void locksTheMappedPlantHardnessValues() {
        assertEquals(0.02F, R196PlantHardness.TALL_GRASS_HARDNESS);
        assertEquals(0.02F, R196PlantHardness.SUGAR_CANE_HARDNESS);
    }

    @Test
    void mapsTheModernFormsOfTallGrassAndSugarCane() {
        assertTrue(R196PlantHardness.appliesTo(Identifier.withDefaultNamespace("short_grass")));
        assertTrue(R196PlantHardness.appliesTo(Identifier.withDefaultNamespace("tall_grass")));
        assertTrue(R196PlantHardness.appliesTo(Identifier.withDefaultNamespace("fern")));
        assertTrue(R196PlantHardness.appliesTo(Identifier.withDefaultNamespace("large_fern")));
        Identifier sugarCane = Identifier.withDefaultNamespace("sugar_cane");
        assertTrue(R196PlantHardness.appliesTo(sugarCane));
        assertEquals(R196PlantHardness.SUGAR_CANE_HARDNESS, R196PlantHardness.destroyTime(sugarCane));
    }

    @Test
    void leavesUnmappedPlantsOnTheirOwnMappings() {
        assertFalse(R196PlantHardness.appliesTo(Identifier.withDefaultNamespace("grass_block")));
        assertFalse(R196PlantHardness.appliesTo(Identifier.withDefaultNamespace("short_dry_grass")));
        assertFalse(R196PlantHardness.appliesTo(Identifier.fromNamespaceAndPath("infx", "sugar_cane")));
    }
}
