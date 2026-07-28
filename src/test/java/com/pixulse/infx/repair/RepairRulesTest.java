package com.pixulse.infx.repair;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pixulse.infx.item.EquipmentType;
import org.junit.jupiter.api.Test;

class RepairRulesTest {
    @Test
    void componentCostsDistinguishToolsPlateChainAndMetalBows() {
        assertEquals(6, RepairPlan.fullRepairCost(EquipmentType.PICKAXE));
        assertEquals(10, RepairPlan.fullRepairCost(EquipmentType.HELMET));
        assertEquals(5, RepairPlan.fullRepairCost(EquipmentType.CHAINMAIL_HELMET));
        assertEquals(2, RepairPlan.fullRepairCost(EquipmentType.BOW));
        assertEquals(1, RepairPlan.fullRepairCost(EquipmentType.FISHING_ROD));
    }

    @Test
    void armorAndBowDurabilityUseTheR196AnvilWearScale() {
        assertEquals(100, RepairPlan.anvilDamageFor(EquipmentType.PICKAXE, 100));
        assertEquals(20_000, RepairPlan.anvilDamageFor(EquipmentType.HELMET, 100));
        assertEquals(20_000, RepairPlan.anvilDamageFor(EquipmentType.CHAINMAIL_HELMET, 100));
        assertEquals(20_000, RepairPlan.anvilDamageFor(EquipmentType.BOW, 100));
        assertEquals(2_200, RepairPlan.anvilDamageFor(EquipmentType.FISHING_ROD, 100));
    }
}
