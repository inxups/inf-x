package com.pixulse.infx.harvest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.data.harvest.PlantHardness;
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
    void mapsTheDryBushesAndNetherVines() {
        for (String path : new String[] {
                "short_dry_grass",
                "tall_dry_grass",
                "dead_bush",
                "bush",
                "firefly_bush",
                "weeping_vines",
                "weeping_vines_plant",
                "twisting_vines",
                "twisting_vines_plant"}) {
            Identifier id = Identifier.withDefaultNamespace(path);
            assertTrue(PlantHardness.appliesTo(id), path + " must have a InfX plant hardness");
            assertEquals(PlantHardness.TALL_GRASS_HARDNESS, PlantHardness.destroyTime(id), path);
        }
    }

    @Test
    void leavesUnmappedPlantsOnTheirOwnMappings() {
        assertFalse(PlantHardness.appliesTo(Identifier.withDefaultNamespace("grass_block")));
        assertFalse(PlantHardness.appliesTo(Identifier.fromNamespaceAndPath("infx", "sugar_cane")));
    }
}
