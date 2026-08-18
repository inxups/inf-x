package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.junit.jupiter.api.Test;

class SpawnsBiomeModifierCompatTest {
    @Test
    void overworldSelectiveRemovalTouchesOnlyInfxReaddedTypes() {
        // InfX re-adds these vanilla monster types with its own weights, so it removes the
        // vanilla entries to avoid duplicates.
        assertTrue(SpawnsBiomeModifier.isOverworldReplacedType(MobCategory.MONSTER, EntityType.SPIDER));
        assertTrue(SpawnsBiomeModifier.isOverworldReplacedType(MobCategory.MONSTER, EntityType.ZOMBIE));
        assertTrue(SpawnsBiomeModifier.isOverworldReplacedType(MobCategory.MONSTER, EntityType.SKELETON));
        assertTrue(SpawnsBiomeModifier.isOverworldReplacedType(MobCategory.MONSTER, EntityType.CREEPER));
        assertTrue(SpawnsBiomeModifier.isOverworldReplacedType(MobCategory.MONSTER, EntityType.SLIME));
        assertTrue(SpawnsBiomeModifier.isOverworldReplacedType(MobCategory.MONSTER, EntityType.ENDERMAN));
        assertTrue(SpawnsBiomeModifier.isOverworldReplacedType(MobCategory.WATER_CREATURE, EntityType.SQUID));
        // Vanilla farm/companion animals replaced by INFX_* equivalents.
        assertTrue(SpawnsBiomeModifier.isOverworldReplacedType(MobCategory.CREATURE, EntityType.SHEEP));
        assertTrue(SpawnsBiomeModifier.isOverworldReplacedType(MobCategory.CREATURE, EntityType.PIG));
        assertTrue(SpawnsBiomeModifier.isOverworldReplacedType(MobCategory.CREATURE, EntityType.CHICKEN));
        assertTrue(SpawnsBiomeModifier.isOverworldReplacedType(MobCategory.CREATURE, EntityType.COW));
        assertTrue(SpawnsBiomeModifier.isOverworldReplacedType(MobCategory.CREATURE, EntityType.HORSE));
        assertTrue(SpawnsBiomeModifier.isOverworldReplacedType(MobCategory.CREATURE, EntityType.WOLF));
        assertTrue(SpawnsBiomeModifier.isOverworldReplacedType(MobCategory.CREATURE, EntityType.OCELOT));
        // Mushroom-isle wildlife cleared alongside the herd.
        assertTrue(SpawnsBiomeModifier.isOverworldReplacedType(MobCategory.CREATURE, EntityType.MOOSHROOM));
        // Vanilla cave bat replaced by the INFX bat family.
        assertTrue(SpawnsBiomeModifier.isOverworldReplacedType(MobCategory.AMBIENT, EntityType.BAT));
        // Vanilla fish replaced by INFX_* per habitat.
        assertTrue(SpawnsBiomeModifier.isOverworldReplacedType(MobCategory.WATER_AMBIENT, EntityType.COD));
        assertTrue(SpawnsBiomeModifier.isOverworldReplacedType(MobCategory.WATER_AMBIENT, EntityType.SALMON));
        assertTrue(SpawnsBiomeModifier.isOverworldReplacedType(MobCategory.WATER_AMBIENT, EntityType.PUFFERFISH));
        assertTrue(SpawnsBiomeModifier.isOverworldReplacedType(MobCategory.WATER_AMBIENT, EntityType.TROPICAL_FISH));
    }

    @Test
    void thirdPartyAndUnreaddedVanillaEntriesSurvive() {
        // A third-party mod's custom monster is never an InfX re-added type.
        assertFalse(SpawnsBiomeModifier.isOverworldReplacedType(MobCategory.MONSTER, EntityType.PIG));
        // Vanilla monster entries InfX does NOT re-add (witch, drowned, zombie villager) survive.
        assertFalse(SpawnsBiomeModifier.isOverworldReplacedType(MobCategory.MONSTER, EntityType.WITCH));
        assertFalse(SpawnsBiomeModifier.isOverworldReplacedType(MobCategory.MONSTER, EntityType.DROWNED));
        // AMBIENT bat is InfX-replaced; rabbit/fox/frog survive.
        assertFalse(SpawnsBiomeModifier.isOverworldReplacedType(MobCategory.AXOLOTLS, EntityType.AXOLOTL));
        assertFalse(SpawnsBiomeModifier.isOverworldReplacedType(MobCategory.CREATURE, EntityType.RABBIT));
        assertFalse(SpawnsBiomeModifier.isOverworldReplacedType(MobCategory.CREATURE, EntityType.FOX));
    }
}
