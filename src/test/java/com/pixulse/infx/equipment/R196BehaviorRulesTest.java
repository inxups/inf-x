package com.pixulse.infx.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pixulse.infx.item.R196BowItem;
import com.pixulse.infx.material.R196Material;
import java.util.Map;
import org.junit.jupiter.api.Test;

class R196BehaviorRulesTest {
    @Test
    void allArrowRecoveryRatesMatchR196() {
        Map<R196Material, Float> expected = Map.ofEntries(
                Map.entry(R196Material.FLINT, .30F),
                Map.entry(R196Material.OBSIDIAN, .40F),
                Map.entry(R196Material.COPPER, .60F),
                Map.entry(R196Material.SILVER, .60F),
                Map.entry(R196Material.RUSTED_IRON, .50F),
                Map.entry(R196Material.GOLD, .50F),
                Map.entry(R196Material.IRON, .70F),
                Map.entry(R196Material.ANCIENT_METAL, .80F),
                Map.entry(R196Material.MITHRIL, .80F),
                Map.entry(R196Material.ADAMANTIUM, .90F));
        expected.forEach((material, chance) ->
                assertEquals(chance, R196EquipmentBehaviors.recoveryChance(material), 0.0001F));
    }

    @Test
    void reinforcedBowVelocityRatesMatchR196() {
        assertEquals(1.0F, R196BowItem.velocityMultiplier(R196Material.WOOD));
        assertEquals(1.10F, R196BowItem.velocityMultiplier(R196Material.ANCIENT_METAL));
        assertEquals(1.25F, R196BowItem.velocityMultiplier(R196Material.MITHRIL));
    }

    @Test
    void armorProtectionIsFullUntilHalfDurabilityThenLinear() {
        assertEquals(1.0F, R196EquipmentBehaviors.armorDurabilityFactor(0, 100));
        assertEquals(1.0F, R196EquipmentBehaviors.armorDurabilityFactor(50, 100));
        assertEquals(.98F, R196EquipmentBehaviors.armorDurabilityFactor(51, 100), .0001F);
        assertEquals(.02F, R196EquipmentBehaviors.armorDurabilityFactor(99, 100), .0001F);
        assertEquals(1.0F, R196EquipmentBehaviors.armorDurabilityFactor(0, 0));
    }
}
