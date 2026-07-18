package com.pixulse.infx.loot;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.progression.ModernContentAuditEvents;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class ModernProgressionAuditTest {
    @Test
    void ruinedPortalsAndPiglinBarteringAreEmpty() {
        assertTrue(ModernContentAuditEvents.isExplicitlyDisabledLootTable(
                Identifier.withDefaultNamespace("chests/ruined_portal")));
        assertTrue(ModernContentAuditEvents.isExplicitlyDisabledLootTable(
                Identifier.withDefaultNamespace("gameplay/piglin_bartering")));
        assertTrue(ModernContentAuditEvents.isExplicitlyDisabledLootTable(
                Identifier.withDefaultNamespace("chests/village/village_toolsmith")));
        assertTrue(ModernContentAuditEvents.isExplicitlyDisabledLootTable(
                Identifier.withDefaultNamespace("chests/bastion_treasure")));
        assertTrue(ModernContentAuditEvents.isExplicitlyDisabledLootTable(
                Identifier.withDefaultNamespace("chests/trial_chambers/reward_ominous")));
        assertTrue(ModernContentAuditEvents.isExplicitlyDisabledLootTable(
                Identifier.withDefaultNamespace("chests/shipwreck_treasure")));
        assertFalse(ModernContentAuditEvents.isExplicitlyDisabledLootTable(
                Identifier.fromNamespaceAndPath("infx", "chests/underworld_dungeon")));
    }

    @Test
    void vanillaProgressionGearIsFilteredButInfiniteXGearSurvives() {
        assertTrue(ModernProgressionLootFilter.isForbidden(
                Identifier.withDefaultNamespace("diamond_pickaxe")));
        assertTrue(ModernProgressionLootFilter.isForbidden(
                Identifier.withDefaultNamespace("netherite_ingot")));
        assertTrue(ModernProgressionLootFilter.isForbidden(
                Identifier.withDefaultNamespace("raw_copper")));
        assertTrue(ModernProgressionLootFilter.isForbidden(
                Identifier.withDefaultNamespace("copper_nugget")));
        assertTrue(ModernProgressionLootFilter.isForbidden(
                Identifier.withDefaultNamespace("diamond")));
        assertTrue(ModernProgressionLootFilter.isForbidden(
                Identifier.withDefaultNamespace("blue_bundle")));
        assertFalse(ModernProgressionLootFilter.isForbidden(
                Identifier.fromNamespaceAndPath("infx", "iron_pickaxe")));
        assertFalse(ModernProgressionLootFilter.isForbidden(
                Identifier.withDefaultNamespace("bread")));
    }
}
