package com.pixulse.infx.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.pixulse.infx.material.R196Material;
import java.util.Map;
import org.junit.jupiter.api.Test;

class R196EquipmentKeyTest {
    private static R196EquipmentKey key(R196Material material, R196EquipmentType type) {
        return new R196EquipmentKey(material, type);
    }

    @Test
    void toolFormulasCoverLowMiddleAndHighTiers() {
        assertEquals(400, key(R196Material.FLINT, R196EquipmentType.HATCHET).durability());
        assertEquals(9_600, key(R196Material.IRON, R196EquipmentType.PICKAXE).durability());
        assertEquals(512_000, key(R196Material.ADAMANTIUM, R196EquipmentType.WAR_HAMMER).durability());
        assertEquals(2.5F, key(R196Material.FLINT, R196EquipmentType.HATCHET).miningSpeed());
        assertEquals(8.0F, key(R196Material.IRON, R196EquipmentType.PICKAXE).miningSpeed());
        assertEquals(9.0F, key(R196Material.ADAMANTIUM, R196EquipmentType.WAR_HAMMER).miningSpeed());
        assertEquals(3.0F, key(R196Material.FLINT, R196EquipmentType.HATCHET).meleeDamage());
        assertEquals(6.0F, key(R196Material.IRON, R196EquipmentType.PICKAXE).meleeDamage());
        assertEquals(8.0F, key(R196Material.ADAMANTIUM, R196EquipmentType.WAR_HAMMER).meleeDamage());
    }

    @Test
    void wearAndReachMatchR196TypeProfiles() {
        assertEquals(4.0F / 3.0F, R196EquipmentType.HATCHET.blockDecay());
        assertEquals(.4F, R196EquipmentType.MATTOCK.blockDecay());
        assertEquals(2.0F / 3.0F, R196EquipmentType.WAR_HAMMER.blockDecay());
        assertEquals(133, key(R196Material.FLINT, R196EquipmentType.HATCHET).attackWear());
        assertEquals(75, key(R196Material.COPPER, R196EquipmentType.BATTLE_AXE).attackWear());
        assertEquals(66, key(R196Material.IRON, R196EquipmentType.WAR_HAMMER).attackWear());
        assertEquals(1.0F, R196EquipmentType.SCYTHE.reachBonus());
        assertEquals(.25F, R196EquipmentType.KNIFE.reachBonus());
    }

    @Test
    void modernAttackSpeedAnalogsAreLiteral() {
        Map<R196EquipmentType, Float> expected = Map.ofEntries(
                Map.entry(R196EquipmentType.PICKAXE, -2.8F),
                Map.entry(R196EquipmentType.SHOVEL, -3.0F),
                Map.entry(R196EquipmentType.MATTOCK, -3.0F),
                Map.entry(R196EquipmentType.HATCHET, -3.2F),
                Map.entry(R196EquipmentType.AXE, -3.1F),
                Map.entry(R196EquipmentType.BATTLE_AXE, -3.1F),
                Map.entry(R196EquipmentType.WAR_HAMMER, -3.4F),
                Map.entry(R196EquipmentType.CLUB, -3.4F),
                Map.entry(R196EquipmentType.CUDGEL, -3.4F),
                Map.entry(R196EquipmentType.HOE, -1.0F),
                Map.entry(R196EquipmentType.SCYTHE, -1.0F),
                Map.entry(R196EquipmentType.SWORD, -2.4F),
                Map.entry(R196EquipmentType.DAGGER, -2.4F),
                Map.entry(R196EquipmentType.KNIFE, -2.4F));
        expected.forEach((type, speed) -> assertEquals(speed, type.attackSpeedModifier(), type.path()));
        assertFalse(R196EquipmentType.SHEARS.hasAttackSpeedModifier());
        assertFalse(R196EquipmentType.BOW.hasAttackSpeedModifier());
    }

    @Test
    void bowArrowAndFishingValuesMatchR196() {
        assertEquals(32, key(R196Material.WOOD, R196EquipmentType.BOW).durability());
        assertEquals(64, key(R196Material.ANCIENT_METAL, R196EquipmentType.BOW).durability());
        assertEquals(128, key(R196Material.MITHRIL, R196EquipmentType.BOW).durability());
        assertEquals(3, key(R196Material.FLINT, R196EquipmentType.FISHING_ROD).durability());
        assertEquals(16, key(R196Material.IRON, R196EquipmentType.FISHING_ROD).durability());
        assertEquals(512, key(R196Material.ADAMANTIUM, R196EquipmentType.FISHING_ROD).durability());
        assertEquals(1.0, key(R196Material.FLINT, R196EquipmentType.ARROW).arrowBaseDamage());
        assertEquals(2.5, key(R196Material.IRON, R196EquipmentType.ARROW).arrowBaseDamage());
        assertEquals(3.5, key(R196Material.ADAMANTIUM, R196EquipmentType.ARROW).arrowBaseDamage());
    }

    @Test
    void armorDurabilityAndFractionalProtectionMatchR196() {
        R196EquipmentKey copperHelmet = key(R196Material.COPPER, R196EquipmentType.HELMET);
        R196EquipmentKey copperChainHelmet = key(R196Material.COPPER, R196EquipmentType.CHAINMAIL_HELMET);
        assertEquals(40, copperHelmet.durability());
        assertEquals(20, copperChainHelmet.durability());
        assertEquals(35.0F / 24.0F, copperHelmet.armorProtection());
        assertEquals(25.0F / 24.0F, copperChainHelmet.armorProtection());

        float plateSum = R196EquipmentType.platePieces().stream()
                .map(type -> key(R196Material.MITHRIL, type))
                .map(R196EquipmentKey::armorProtection)
                .reduce(0.0F, Float::sum);
        float chainSum = R196EquipmentType.chainPieces().stream()
                .map(type -> key(R196Material.MITHRIL, type))
                .map(R196EquipmentKey::armorProtection)
                .reduce(0.0F, Float::sum);
        assertEquals(9.0F, plateSum, 1.0E-6F);
        assertEquals(7.0F, chainSum, 1.0E-6F);
        assertEquals(7.0F, key(R196Material.ADAMANTIUM, R196EquipmentType.HORSE_ARMOR).armorProtection());
    }

    @Test
    void approvedNameExceptionsAndEquipmentAssetsAreStable() {
        assertEquals("InfiniteX Copper Pickaxe", key(R196Material.COPPER, R196EquipmentType.PICKAXE).englishName());
        assertEquals("InfiniteX 铜镐", key(R196Material.COPPER, R196EquipmentType.PICKAXE).chineseName());
        assertEquals("Bow", key(R196Material.WOOD, R196EquipmentType.BOW).englishName());
        assertEquals("短木棒", key(R196Material.WOOD, R196EquipmentType.CUDGEL).chineseName());
        assertEquals("木棒", key(R196Material.WOOD, R196EquipmentType.CLUB).chineseName());
        assertEquals("Fishing Rod", key(R196Material.ADAMANTIUM, R196EquipmentType.FISHING_ROD).englishName());
        assertEquals("Gold Horse Armor", key(R196Material.GOLD, R196EquipmentType.HORSE_ARMOR).englishName());
        assertEquals("infx:mithril_chainmail",
                key(R196Material.MITHRIL, R196EquipmentType.CHAINMAIL_CHESTPLATE)
                        .equipmentAsset().identifier().toString());
    }

    @Test
    void ordinaryToolBehaviorsAreMappedWithoutVanillaConstructorMutation() {
        assertEquals(R196UseAction.AXE, R196EquipmentType.HATCHET.useAction());
        assertEquals(R196UseAction.AXE, R196EquipmentType.AXE.useAction());
        assertEquals(R196UseAction.AXE, R196EquipmentType.BATTLE_AXE.useAction());
        assertEquals(R196UseAction.SHOVEL, R196EquipmentType.SHOVEL.useAction());
        assertEquals(R196UseAction.HOE, R196EquipmentType.HOE.useAction());
        assertEquals(R196UseAction.MATTOCK, R196EquipmentType.MATTOCK.useAction());
        assertEquals(133, key(R196Material.FLINT, R196EquipmentType.HATCHET).damageForBreaking(1.0F));
        assertEquals(20, key(R196Material.COPPER, R196EquipmentType.MATTOCK).damageForBreaking(.5F));
        assertEquals(0, key(R196Material.IRON, R196EquipmentType.PICKAXE).damageForBreaking(0.0F));
    }
}
