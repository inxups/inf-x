package com.pixulse.infx.repair;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pixulse.infx.item.R196EquipmentType;
import org.junit.jupiter.api.Test;

class R196RepairRulesTest {
    @Test
    void componentCostsDistinguishToolsPlateChainAndMetalBows() {
        assertEquals(6, R196RepairPlan.fullRepairCost(R196EquipmentType.PICKAXE));
        assertEquals(10, R196RepairPlan.fullRepairCost(R196EquipmentType.HELMET));
        assertEquals(5, R196RepairPlan.fullRepairCost(R196EquipmentType.CHAINMAIL_HELMET));
        assertEquals(2, R196RepairPlan.fullRepairCost(R196EquipmentType.BOW));
        assertEquals(1, R196RepairPlan.fullRepairCost(R196EquipmentType.FISHING_ROD));
    }

    @Test
    void armorAndBowDurabilityUseTheR196AnvilWearScale() {
        assertEquals(100, R196RepairPlan.anvilDamageFor(R196EquipmentType.PICKAXE, 100));
        assertEquals(20_000, R196RepairPlan.anvilDamageFor(R196EquipmentType.HELMET, 100));
        assertEquals(20_000, R196RepairPlan.anvilDamageFor(R196EquipmentType.CHAINMAIL_HELMET, 100));
        assertEquals(20_000, R196RepairPlan.anvilDamageFor(R196EquipmentType.BOW, 100));
        assertEquals(2_200, R196RepairPlan.anvilDamageFor(R196EquipmentType.FISHING_ROD, 100));
    }
}
