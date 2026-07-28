package com.pixulse.infx.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pixulse.infx.item.MiteBowItem;
import com.pixulse.infx.material.MiteMaterial;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BehaviorRulesTest {
    @Test
    void allArrowRecoveryRatesMatchR196() {
        Map<MiteMaterial, Float> expected = Map.ofEntries(
                Map.entry(MiteMaterial.FLINT, .30F),
                Map.entry(MiteMaterial.OBSIDIAN, .40F),
                Map.entry(MiteMaterial.COPPER, .60F),
                Map.entry(MiteMaterial.SILVER, .60F),
                Map.entry(MiteMaterial.RUSTED_IRON, .50F),
                Map.entry(MiteMaterial.GOLD, .50F),
                Map.entry(MiteMaterial.IRON, .70F),
                Map.entry(MiteMaterial.ANCIENT_METAL, .80F),
                Map.entry(MiteMaterial.MITHRIL, .80F),
                Map.entry(MiteMaterial.ADAMANTIUM, .90F));
        expected.forEach((material, chance) ->
                assertEquals(chance, EquipmentBehaviors.recoveryChance(material), 0.0001F));
        assertEquals(.44F, EquipmentBehaviors.recoveryChance(MiteMaterial.FLINT, 1), 0.0001F);
        assertEquals(1.0F, EquipmentBehaviors.recoveryChance(MiteMaterial.FLINT, 5), 0.0001F);
    }

    @Test
    void reinforcedBowVelocityRatesMatchR196() {
        assertEquals(1.0F, MiteBowItem.velocityMultiplier(MiteMaterial.WOOD));
        assertEquals(1.10F, MiteBowItem.velocityMultiplier(MiteMaterial.ANCIENT_METAL));
        assertEquals(1.25F, MiteBowItem.velocityMultiplier(MiteMaterial.MITHRIL));
    }

    @Test
    void armorProtectionIsFullUntilHalfDurabilityThenLinear() {
        assertEquals(1.0F, EquipmentBehaviors.armorDurabilityFactor(0, 100));
        assertEquals(1.0F, EquipmentBehaviors.armorDurabilityFactor(50, 100));
        assertEquals(.98F, EquipmentBehaviors.armorDurabilityFactor(51, 100), .0001F);
        assertEquals(.02F, EquipmentBehaviors.armorDurabilityFactor(99, 100), .0001F);
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
                MiteMaterial.LEATHER, 160, 2.0F, true, false));
        assertEquals(20, EquipmentBehaviors.corrosionDamage(
                MiteMaterial.IRON, 2400, 2.0F, false, true));
        assertEquals(0, EquipmentBehaviors.corrosionDamage(
                MiteMaterial.ADAMANTIUM, 80_000, 20.0F, false, true));
    }
}
