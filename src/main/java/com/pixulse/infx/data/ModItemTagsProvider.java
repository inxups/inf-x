package com.pixulse.infx.data;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.R196Catalog;
import com.pixulse.infx.item.R196EquipmentCategory;
import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.item.R196MiningFamily;
import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.registry.ModItems;
import com.pixulse.infx.tag.ModTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

final class ModItemTagsProvider extends ItemTagsProvider {
    ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, InfiniteX.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        tag(ModTags.Items.BINDINGS).add(itemKey(Items.STRING)).add(ModItems.SINEW.getKey());
        tag(ModTags.Items.FURNACE_FUELS_HEAT_2)
                .add(itemKey(Items.COAL))
                .add(itemKey(Blocks.COAL_BLOCK.asItem()));
        tag(ModTags.Items.SMELTING_INPUTS_HEAT_2)
                .add(itemKey(Items.RAW_COPPER))
                .add(itemKey(Items.RAW_GOLD))
                .add(itemKey(Items.RAW_IRON))
                .add(itemKey(Blocks.COPPER_ORE.asItem()))
                .add(itemKey(Blocks.DEEPSLATE_COPPER_ORE.asItem()))
                .add(itemKey(Blocks.GOLD_ORE.asItem()))
                .add(itemKey(Blocks.DEEPSLATE_GOLD_ORE.asItem()))
                .add(itemKey(Blocks.IRON_ORE.asItem()))
                .add(itemKey(Blocks.DEEPSLATE_IRON_ORE.asItem()))
                .add(itemKey(Blocks.REDSTONE_ORE.asItem()))
                .add(itemKey(Blocks.DEEPSLATE_REDSTONE_ORE.asItem()))
                .add(itemKey(Blocks.LAPIS_ORE.asItem()))
                .add(itemKey(Blocks.DEEPSLATE_LAPIS_ORE.asItem()))
                .add(itemKey(Blocks.EMERALD_ORE.asItem()))
                .add(itemKey(Blocks.DEEPSLATE_EMERALD_ORE.asItem()))
                .add(itemKey(Blocks.DIAMOND_ORE.asItem()))
                .add(itemKey(Blocks.DEEPSLATE_DIAMOND_ORE.asItem()))
                .add(itemKey(Blocks.NETHER_QUARTZ_ORE.asItem()))
                .add(itemKey(Blocks.SANDSTONE.asItem()))
                .add(ModItems.SILVER_ORE.getKey());
        tag(ModTags.Items.SMELTING_INPUTS_HEAT_3).add(ModItems.MITHRIL_ORE.getKey());
        tag(ModTags.Items.SMELTING_INPUTS_HEAT_4).add(ModItems.ADAMANTIUM_ORE.getKey());

        for (R196Catalog.RawEntry entry : ModItems.catalog().rawEntries()) {
            entry.definition().material().ifPresent(material -> add(ModTags.Items.material(material), entry));
        }
        addRepairTags();

        for (R196Catalog.EquipmentEntry entry : ModItems.catalog().equipmentEntries()) {
            add(ModTags.Items.material(entry.key().material()), entry);
            add(ModTags.Items.equipmentType(entry.key().type()), entry);
            addFamilyTags(entry);
            addEnchantmentTags(entry);
            addHarvestTierTag(entry);
        }
    }

    private void addRepairTags() {
        for (R196Material material : R196Material.values()) {
            TagAppender<Item> repairs = tag(ModTags.Items.repairMaterial(material));
            switch (material) {
                case LEATHER -> repairs.add(itemKey(Items.LEATHER));
                case WOOD -> repairs.addTag(ItemTags.PLANKS);
                case FLINT -> repairs.add(itemKey(Items.FLINT));
                case OBSIDIAN -> repairs.add(itemKey(Items.OBSIDIAN));
                case COPPER -> repairs.add(itemKey(Items.COPPER_NUGGET));
                case GOLD -> repairs.add(itemKey(Items.GOLD_NUGGET));
                case RUSTED_IRON, IRON -> repairs.add(itemKey(Items.IRON_NUGGET));
                case SILVER -> repairs.add(ModItems.catalog().raw("silver_nugget").holder().getKey());
                case ANCIENT_METAL ->
                    repairs.add(ModItems.catalog().raw("ancient_metal_nugget").holder().getKey());
                case MITHRIL -> repairs.add(ModItems.catalog().raw("mithril_nugget").holder().getKey());
                case ADAMANTIUM ->
                    repairs.add(ModItems.catalog().raw("adamantium_nugget").holder().getKey());
            }
        }
    }

    private void addFamilyTags(R196Catalog.EquipmentEntry entry) {
        R196EquipmentType type = entry.key().type();
        switch (type.miningFamily()) {
            case PICKAXE -> add(ItemTags.PICKAXES, entry);
            case SHOVEL -> add(ItemTags.SHOVELS, entry);
            case AXE -> add(ItemTags.AXES, entry);
            case HOE -> add(ItemTags.HOES, entry);
            case SWORD -> add(ItemTags.SWORDS, entry);
            case NONE, SCYTHE, CUDGEL, SHEARS -> {
            }
        }
        if (type == R196EquipmentType.ARROW) {
            add(ItemTags.ARROWS, entry);
        }
    }

    private void addEnchantmentTags(R196Catalog.EquipmentEntry entry) {
        R196EquipmentType type = entry.key().type();
        if (entry.key().durability() > 0) {
            add(ItemTags.DURABILITY_ENCHANTABLE, entry);
        }
        if (type.category() == R196EquipmentCategory.TOOL
                && type.miningFamily() != R196MiningFamily.NONE) {
            add(ItemTags.MINING_ENCHANTABLE, entry);
        }
        boolean melee = (type.category() == R196EquipmentCategory.TOOL
                        || type.category() == R196EquipmentCategory.WEAPON)
                && type != R196EquipmentType.FISHING_ROD
                && type != R196EquipmentType.BOW
                && type != R196EquipmentType.ARROW;
        if (melee) {
            add(ItemTags.MELEE_WEAPON_ENCHANTABLE, entry);
            add(ItemTags.WEAPON_ENCHANTABLE, entry);
            add(ItemTags.SHARP_WEAPON_ENCHANTABLE, entry);
        }
        if (type == R196EquipmentType.FISHING_ROD) {
            add(ItemTags.FISHING_ENCHANTABLE, entry);
        }
        if (type == R196EquipmentType.BOW) {
            add(ItemTags.BOW_ENCHANTABLE, entry);
        }
        if (type.armorForm() == R196EquipmentType.ArmorForm.PLATE
                || type.armorForm() == R196EquipmentType.ArmorForm.CHAIN) {
            add(ItemTags.ARMOR_ENCHANTABLE, entry);
            add(ItemTags.EQUIPPABLE_ENCHANTABLE, entry);
            addArmorSlotTags(entry, type.armorType().orElseThrow());
        }
    }

    private void addArmorSlotTags(R196Catalog.EquipmentEntry entry, ArmorType armorType) {
        switch (armorType) {
            case HELMET -> {
                add(ItemTags.HEAD_ARMOR, entry);
                add(ItemTags.HEAD_ARMOR_ENCHANTABLE, entry);
            }
            case CHESTPLATE -> {
                add(ItemTags.CHEST_ARMOR, entry);
                add(ItemTags.CHEST_ARMOR_ENCHANTABLE, entry);
            }
            case LEGGINGS -> {
                add(ItemTags.LEG_ARMOR, entry);
                add(ItemTags.LEG_ARMOR_ENCHANTABLE, entry);
            }
            case BOOTS -> {
                add(ItemTags.FOOT_ARMOR, entry);
                add(ItemTags.FOOT_ARMOR_ENCHANTABLE, entry);
            }
            case BODY -> throw new IllegalArgumentException("Unexpected player armor type: " + entry.path());
        }
    }

    private void addHarvestTierTag(R196Catalog.EquipmentEntry entry) {
        if (entry.key().type().miningFamily() == R196MiningFamily.NONE) {
            return;
        }
        entry.key().material().harvestTier()
                .ifPresent(tier -> add(ModTags.Items.toolTier(tier), entry));
    }

    private void add(TagKey<Item> tag, R196Catalog.RawEntry entry) {
        tag(tag).add(entry.holder().getKey());
    }

    private void add(TagKey<Item> tag, R196Catalog.EquipmentEntry entry) {
        tag(tag).add(entry.holder().getKey());
    }

    private static ResourceKey<Item> itemKey(Item item) {
        return BuiltInRegistries.ITEM.getResourceKey(item).orElseThrow();
    }
}
