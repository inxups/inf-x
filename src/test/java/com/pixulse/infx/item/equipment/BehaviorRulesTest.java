package com.pixulse.infx.item.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.item.InfxBowItem;
import com.pixulse.infx.item.material.InfxMaterial;
import java.util.Map;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class BehaviorRulesTest {
    @Test
    void extendedTooltipsOnlyAppearInTestMode() {
        assertTrue(EquipmentBehaviors.shouldAddExtendedTooltips(true));
        assertFalse(EquipmentBehaviors.shouldAddExtendedTooltips(false));
    }

    @Test
    void hookMaterialsCoverVanillaStickItems() {
        assertEquals(InfxMaterial.IRON, EquipmentBehaviors.fishingRodHookMaterial(Items.FISHING_ROD));
        assertEquals(
                InfxMaterial.IRON,
                EquipmentBehaviors.fishingRodHookMaterial(Items.CARROT_ON_A_STICK));
        assertEquals(
                InfxMaterial.IRON,
                EquipmentBehaviors.fishingRodHookMaterial(Items.WARPED_FUNGUS_ON_A_STICK));
        assertNull(EquipmentBehaviors.fishingRodHookMaterial(Items.STICK));
    }

    @Test
    void allArrowRecoveryRatesMatchR196() {
        Map<InfxMaterial, Float> expected = Map.ofEntries(
                Map.entry(InfxMaterial.FLINT, .30F),
                Map.entry(InfxMaterial.OBSIDIAN, .40F),
                Map.entry(InfxMaterial.COPPER, .60F),
                Map.entry(InfxMaterial.SILVER, .60F),
                Map.entry(InfxMaterial.RUSTED_IRON, .50F),
                Map.entry(InfxMaterial.GOLD, .50F),
                Map.entry(InfxMaterial.IRON, .70F),
                Map.entry(InfxMaterial.ANCIENT_METAL, .80F),
                Map.entry(InfxMaterial.MITHRIL, .80F),
                Map.entry(InfxMaterial.ADAMANTIUM, .90F));
        expected.forEach((material, chance) ->
                assertEquals(chance, EquipmentBehaviors.recoveryChance(material), 0.0001F));
        assertEquals(.44F, EquipmentBehaviors.recoveryChance(InfxMaterial.FLINT, 1), 0.0001F);
        assertEquals(1.0F, EquipmentBehaviors.recoveryChance(InfxMaterial.FLINT, 5), 0.0001F);
    }

    @Test
    void reinforcedBowVelocityRatesMatchR196() {
        assertEquals(1.0F, InfxBowItem.velocityMultiplier(InfxMaterial.WOOD));
        assertEquals(1.10F, InfxBowItem.velocityMultiplier(InfxMaterial.ANCIENT_METAL));
        assertEquals(1.25F, InfxBowItem.velocityMultiplier(InfxMaterial.MITHRIL));
    }

    @Test
    void armorProtectionIsFullUntilHalfDurabilityThenLinear() {
        assertEquals(1.0F, EquipmentBehaviors.armorDurabilityFactor(0, 100));
        assertEquals(1.0F, EquipmentBehaviors.armorDurabilityFactor(50, 100));
        assertEquals(.98F, EquipmentBehaviors.armorDurabilityFactor(51, 100), .0001F);
        assertEquals(0.0F, EquipmentBehaviors.armorDurabilityFactor(99, 100), .0001F);
        assertEquals(1.0F, EquipmentBehaviors.armorDurabilityFactor(0, 0));
    }

    @Test
    void fixedPointArmorAlwaysLeavesAtLeastOneDamage() {
        assertEquals(0.0F, EquipmentBehaviors.fixedArmorReduction(1.0F, 20.0F));
        assertEquals(4.0F, EquipmentBehaviors.fixedArmorReduction(5.0F, 20.0F));
        assertEquals(3.0F, EquipmentBehaviors.fixedArmorReduction(10.0F, 3.0F));
        assertEquals(5.0F, EquipmentBehaviors.fixedArmorReduction(20.0F, 5.0F));
        assertEquals(10.0F, EquipmentBehaviors.fixedArmorReduction(20.0F, 10.0F));
    }

    @Test
    void elementalCorrosionDestroysLeatherAcceleratesMetalAndExemptsAdamantium() {
        assertEquals(160, EquipmentBehaviors.corrosionDamage(
                InfxMaterial.LEATHER, 160, 2.0F, true, false));
        assertEquals(20, EquipmentBehaviors.corrosionDamage(
                InfxMaterial.IRON, 2400, 2.0F, false, true));
        assertEquals(0, EquipmentBehaviors.corrosionDamage(
                InfxMaterial.ADAMANTIUM, 80_000, 20.0F, false, true));
    }
}
