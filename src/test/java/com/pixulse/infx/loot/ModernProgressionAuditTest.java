package com.pixulse.infx.loot;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.player.ModernContentAuditEvents;
import com.pixulse.infx.registry.InfXItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ModernProgressionAuditTest {
    @BeforeAll
    static void bindConversionTargetComponents() {
        bindTestComponents(ancientMetal(EquipmentType.SWORD), damageableComponents());
    }

    @Test
    void onlySpawnBonusAndPiglinBarteringAreEmpty() {
        assertTrue(ModernContentAuditEvents.isExplicitlyDisabledLootTable(
                Identifier.withDefaultNamespace("chests/spawn_bonus_chest")));
        assertTrue(ModernContentAuditEvents.isExplicitlyDisabledLootTable(
                Identifier.withDefaultNamespace("gameplay/piglin_bartering")));
        for (String path : new String[] {
            "chests/ancient_city",
            "chests/bastion_treasure",
            "chests/buried_treasure",
            "chests/end_city_treasure",
            "chests/igloo_chest",
            "chests/pillager_outpost",
            "chests/ruined_portal",
            "chests/shipwreck_treasure",
            "chests/trial_chambers/reward",
            "chests/trial_chambers/reward_ominous",
            "chests/underwater_ruin_small",
            "chests/village/village_toolsmith",
            "chests/woodland_mansion",
            "chests/trail_ruins_common"}) {
            assertFalse(ModernContentAuditEvents.isExplicitlyDisabledLootTable(
                    Identifier.withDefaultNamespace(path)), path);
        }
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

    @Test
    void vanillaEquipmentConvertsToAncientMetalFamilies() {
        assertConverted(Items.IRON_PICKAXE, EquipmentType.PICKAXE);
        assertConverted(Items.DIAMOND_SWORD, EquipmentType.SWORD);
        assertConverted(Items.IRON_HELMET, EquipmentType.HELMET);
        assertConverted(Items.DIAMOND_CHESTPLATE, EquipmentType.CHESTPLATE);
        assertConverted(Items.CHAINMAIL_LEGGINGS, EquipmentType.CHAINMAIL_LEGGINGS);
        assertConverted(Items.IRON_HORSE_ARMOR, EquipmentType.HORSE_ARMOR);
        assertConverted(Items.DIAMOND_SPEAR, EquipmentType.SWORD);
        assertConverted(Items.IRON_NAUTILUS_ARMOR, EquipmentType.HORSE_ARMOR);
    }

    @Test
    void equipmentConversionPreservesCompatibleStackData() {
        Item target = ancientMetal(EquipmentType.SWORD);
        bindTestComponents(target);
        ItemStack source = testStack(Items.DIAMOND_SWORD, 3);
        source.setDamageValue(4);
        source.set(DataComponents.CUSTOM_NAME, Component.literal("found in a chest"));
        Holder<Enchantment> testEnchantment = Holder.direct(new Enchantment(
                Component.literal("test enchantment"),
                Enchantment.definition(
                        HolderSet.direct(Holder.direct(Items.DIAMOND)),
                        1,
                        3,
                        Enchantment.constantCost(1),
                        Enchantment.constantCost(1),
                        1,
                        EquipmentSlotGroup.MAINHAND),
                HolderSet.empty(),
                DataComponentMap.EMPTY));
        source.enchant(testEnchantment, 2);
        assertEquals(2, source.getEnchantments().getLevel(testEnchantment));

        ItemStack converted = ModernProgressionLootFilter.convertEquipment(source);
        assertEquals(
                InfXItems.catalog().equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.SWORD).holder().value(),
                converted.getItem());
        assertEquals(3, converted.getCount());
        assertEquals(4, converted.getDamageValue());
        assertEquals(Component.literal("found in a chest"), converted.get(DataComponents.CUSTOM_NAME));
        assertEquals(2, converted.getEnchantments().getLevel(testEnchantment));
    }

    @Test
    void miteProgressionUsesMiteDayAndHeightBoundaries() {
        Identifier axe = Identifier.fromNamespaceAndPath("infx", "ancient_metal_axe");
        Identifier hoe = Identifier.fromNamespaceAndPath("infx", "copper_hoe");
        Identifier mattock = Identifier.fromNamespaceAndPath("infx", "copper_mattock");
        Identifier pickaxe = Identifier.fromNamespaceAndPath("infx", "ancient_metal_pickaxe");
        Identifier ingot = Identifier.fromNamespaceAndPath("infx", "silver_ingot");
        Identifier coin = Identifier.fromNamespaceAndPath("infx", "silver_coin");
        Identifier nugget = Identifier.fromNamespaceAndPath("infx", "silver_nugget");
        Identifier rod = Identifier.fromNamespaceAndPath("infx", "ancient_metal_fishing_rod");

        assertTrue(MiteProgressionLootFilter.isLocked(axe, 9, 64));
        assertFalse(MiteProgressionLootFilter.isLocked(axe, 10, 64));
        assertTrue(MiteProgressionLootFilter.isLocked(hoe, 10, 47));
        assertFalse(MiteProgressionLootFilter.isLocked(hoe, 10, 48));
        assertTrue(MiteProgressionLootFilter.isLocked(mattock, 9, 64));
        assertTrue(MiteProgressionLootFilter.isLocked(pickaxe, 19, 64));
        assertFalse(MiteProgressionLootFilter.isLocked(pickaxe, 20, 64));
        assertTrue(MiteProgressionLootFilter.isLocked(ingot, 19, 64));
        assertTrue(MiteProgressionLootFilter.isLocked(coin, 19, 64));
        assertFalse(MiteProgressionLootFilter.isLocked(nugget, 19, 64));
        assertTrue(MiteProgressionLootFilter.isLocked(rod, 20, 47));
        assertFalse(MiteProgressionLootFilter.isLocked(rod, 20, 48));
    }

    private static void assertConverted(Item item, EquipmentType type) {
        bindTestComponents(ancientMetal(type));
        ItemStack converted = ModernProgressionLootFilter.convertEquipment(testStack(item, 2));
        assertEquals(
                InfXItems.catalog().equipment(InfxMaterial.ANCIENT_METAL, type).holder().value(),
                converted.getItem());
        assertEquals(2, converted.getCount());
    }

    private static ItemStack testStack(Item item, int count) {
        DataComponentMap prototype = DataComponentMap.builder()
                .set(DataComponents.MAX_DAMAGE, 100)
                .set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY)
                .build();
        return new ItemStack(Holder.direct(item, prototype), count);
    }

    private static Item ancientMetal(EquipmentType type) {
        return InfXItems.catalog().equipment(InfxMaterial.ANCIENT_METAL, type).holder().value();
    }

    private static void bindTestComponents(Item item) {
        bindTestComponents(item, DataComponentMap.EMPTY);
    }

    private static void bindTestComponents(Item item, DataComponentMap components) {
        if (!item.builtInRegistryHolder().areComponentsBound()) {
            item.builtInRegistryHolder().bindComponents(components);
        }
    }

    private static DataComponentMap damageableComponents() {
        return DataComponentMap.builder()
                .set(DataComponents.MAX_DAMAGE, 100)
                .build();
    }
}
