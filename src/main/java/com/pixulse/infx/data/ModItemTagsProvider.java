package com.pixulse.infx.data;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.*;
import com.pixulse.infx.item.material.MiteMaterial;
import com.pixulse.infx.registry.InfXItems;
import com.pixulse.infx.registry.tag.InfXItemTags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.Blocks;

final class ModItemTagsProvider extends KeyTagsProvider<Item> {
    ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.ITEM, lookupProvider, InfiniteX.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        tag(InfXItemTags.BINDINGS).add(itemKey(Items.STRING)).add(InfXItems.SINEW.getKey());
        tag(InfXItemTags.R196_SILK_TOUCH_ENCHANTABLE).add(itemKey(Items.SHEARS));
        tag(InfXItemTags.FURNACE_FUELS_HEAT_2)
                .add(itemKey(Items.COAL))
                .add(itemKey(Blocks.COAL_BLOCK.asItem()));
        var waterBuckets = tag(InfXItemTags.WATER_BUCKETS).add(itemKey(Items.WATER_BUCKET));
        var milkBuckets = tag(InfXItemTags.MILK_BUCKETS).add(itemKey(Items.MILK_BUCKET));
        for (MiteMaterial material : InfXItems.BUCKET_MATERIALS) {
            waterBuckets.add(InfXItems.bucket(material, MiteBucketItem.Contents.WATER)
                    .getKey());
            milkBuckets.add(InfXItems.bucket(material, MiteBucketItem.Contents.MILK)
                    .getKey());
        }
        tag(InfXItemTags.SMELTING_INPUTS_HEAT_2)
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
                .add(InfXItems.SILVER_ORE.getKey());
        tag(InfXItemTags.SMELTING_INPUTS_HEAT_3).add(InfXItems.MITHRIL_ORE.getKey());
        tag(InfXItemTags.SMELTING_INPUTS_HEAT_4).add(InfXItems.ADAMANTIUM_ORE.getKey());
        tag(InfXItemTags.GELATINOUS_SPHERES).add(
                InfXItems.GREEN_GELATINOUS_SPHERE.getKey(),
                InfXItems.OCHRE_GELATINOUS_SPHERE.getKey(),
                InfXItems.CRIMSON_GELATINOUS_SPHERE.getKey(),
                InfXItems.GRAY_GELATINOUS_SPHERE.getKey(),
                InfXItems.BLACK_GELATINOUS_SPHERE.getKey());
        addCurseTags();

        for (Catalog.RawEntry entry : InfXItems.catalog().rawEntries()) {
            entry.definition().material().ifPresent(material -> add(InfXItemTags.material(material), entry));
        }
        addRepairTags();

        for (Catalog.EquipmentEntry entry : InfXItems.catalog().equipmentEntries()) {
            add(InfXItemTags.material(entry.key().material()), entry);
            add(InfXItemTags.equipmentType(entry.key().type()), entry);
            addFamilyTags(entry);
            addEnchantmentTags(entry);
            addR196EnchantmentTags(entry);
            addHarvestTierTag(entry);
        }
    }

    private void addCurseTags() {
        var animalProducts = tag(InfXItemTags.CURSE_ANIMAL_PRODUCTS)
                .addTag(ItemTags.MEAT)
                .addTag(ItemTags.EGGS)
                .add(
                        itemKey(Items.SPIDER_EYE),
                        itemKey(Items.COD),
                        itemKey(Items.COOKED_COD),
                        itemKey(Items.SALMON),
                        itemKey(Items.COOKED_SALMON),
                        itemKey(Items.TROPICAL_FISH),
                        itemKey(Items.PUFFERFISH),
                        itemKey(Items.MILK_BUCKET),
                        itemKey(Items.HONEY_BOTTLE),
                        itemKey(Items.CAKE),
                        itemKey(Items.PUMPKIN_PIE),
                        itemKey(Items.RABBIT_STEW),
                        InfXItems.MILK_BOWL.getKey(),
                        InfXItems.CEREAL_PORRIDGE.getKey(),
                        InfXItems.CREAM_OF_MUSHROOM_SOUP.getKey(),
                        InfXItems.CREAM_OF_VEGETABLE_SOUP.getKey(),
                        InfXItems.CHICKEN_SOUP.getKey(),
                        InfXItems.BEEF_STEW.getKey(),
                        InfXItems.CHEESE.getKey(),
                        InfXItems.MASHED_POTATO.getKey(),
                        InfXItems.ICE_CREAM.getKey(),
                        InfXItems.WORM.getKey(),
                        InfXItems.COOKED_WORM.getKey());
        for (MiteMaterial material : InfXItems.BUCKET_MATERIALS) {
            animalProducts.add(InfXItems.bucket(material, MiteBucketItem.Contents.MILK)
                    .getKey());
        }

        tag(InfXItemTags.CURSE_PLANT_PRODUCTS)
                .add(
                        itemKey(Items.APPLE),
                        itemKey(Items.GOLDEN_APPLE),
                        itemKey(Items.ENCHANTED_GOLDEN_APPLE),
                        itemKey(Items.MUSHROOM_STEW),
                        itemKey(Items.SUSPICIOUS_STEW),
                        itemKey(Items.BREAD),
                        itemKey(Items.SUGAR),
                        itemKey(Items.COOKIE),
                        itemKey(Items.MELON_SLICE),
                        itemKey(Items.WHEAT_SEEDS),
                        itemKey(Items.PUMPKIN_SEEDS),
                        itemKey(Items.MELON_SEEDS),
                        itemKey(Items.BEETROOT_SEEDS),
                        itemKey(Items.NETHER_WART),
                        itemKey(Items.CARROT),
                        itemKey(Items.POTATO),
                        itemKey(Items.BAKED_POTATO),
                        itemKey(Items.POISONOUS_POTATO),
                        itemKey(Items.GOLDEN_CARROT),
                        itemKey(Items.BEETROOT),
                        itemKey(Items.BEETROOT_SOUP),
                        itemKey(Items.SWEET_BERRIES),
                        itemKey(Items.GLOW_BERRIES),
                        itemKey(Items.CHORUS_FRUIT),
                        itemKey(Items.DRIED_KELP),
                        itemKey(Items.CAKE),
                        itemKey(Items.PUMPKIN_PIE),
                        itemKey(Items.RABBIT_STEW),
                        itemKey(Items.BROWN_MUSHROOM),
                        itemKey(Items.RED_MUSHROOM),
                        InfXItems.DOUGH.getKey(),
                        InfXItems.SALAD.getKey(),
                        InfXItems.BLUEBERRIES.getKey(),
                        InfXItems.BLUEBERRY_PORRIDGE.getKey(),
                        InfXItems.CEREAL_PORRIDGE.getKey(),
                        InfXItems.CHOCOLATE.getKey(),
                        InfXItems.PUMPKIN_SOUP.getKey(),
                        InfXItems.CREAM_OF_MUSHROOM_SOUP.getKey(),
                        InfXItems.ONION.getKey(),
                        InfXItems.VEGETABLE_SOUP.getKey(),
                        InfXItems.CREAM_OF_VEGETABLE_SOUP.getKey(),
                        InfXItems.CHICKEN_SOUP.getKey(),
                        InfXItems.BEEF_STEW.getKey(),
                        InfXItems.ORANGE.getKey(),
                        InfXItems.FRUIT_ICE.getKey(),
                        InfXItems.MASHED_POTATO.getKey(),
                        InfXItems.ICE_CREAM.getKey(),
                        InfXItems.BANANA.getKey());

        var drinks = tag(InfXItemTags.CURSE_DRINKS).add(
                itemKey(Items.POTION),
                itemKey(Items.MILK_BUCKET),
                itemKey(Items.HONEY_BOTTLE),
                itemKey(Items.OMINOUS_BOTTLE),
                itemKey(Items.BEETROOT_SOUP),
                InfXItems.WATER_BOWL.getKey(),
                InfXItems.MILK_BOWL.getKey(),
                InfXItems.PUMPKIN_SOUP.getKey(),
                InfXItems.CREAM_OF_MUSHROOM_SOUP.getKey(),
                InfXItems.VEGETABLE_SOUP.getKey(),
                InfXItems.CREAM_OF_VEGETABLE_SOUP.getKey(),
                InfXItems.CHICKEN_SOUP.getKey());
        for (MiteMaterial material : InfXItems.BUCKET_MATERIALS) {
            drinks.add(InfXItems.bucket(material, MiteBucketItem.Contents.MILK)
                    .getKey());
        }
    }

    private void addRepairTags() {
        for (MiteMaterial material : MiteMaterial.values()) {
            TagAppender<ResourceKey<Item>, Item> repairs = tag(InfXItemTags.repairMaterial(material));
            switch (material) {
                case LEATHER -> repairs.add(itemKey(Items.LEATHER));
                case WOOD -> repairs.addTag(ItemTags.PLANKS);
                case FLINT -> repairs.add(itemKey(Items.FLINT));
                case OBSIDIAN -> repairs.add(itemKey(Items.OBSIDIAN));
                case COPPER -> repairs.add(itemKey(Items.COPPER_NUGGET));
                case GOLD -> repairs.add(itemKey(Items.GOLD_NUGGET));
                case RUSTED_IRON, IRON -> repairs.add(itemKey(Items.IRON_NUGGET));
                case SILVER -> repairs.add(InfXItems.catalog().raw("silver_nugget").holder().getKey());
                case ANCIENT_METAL ->
                    repairs.add(InfXItems.catalog().raw("ancient_metal_nugget").holder().getKey());
                case MITHRIL -> repairs.add(InfXItems.catalog().raw("mithril_nugget").holder().getKey());
                case ADAMANTIUM ->
                    repairs.add(InfXItems.catalog().raw("adamantium_nugget").holder().getKey());
            }
        }
    }

    private void addFamilyTags(Catalog.EquipmentEntry entry) {
        EquipmentType type = entry.key().type();
        switch (type.miningFamily()) {
            case PICKAXE -> add(ItemTags.PICKAXES, entry);
            case SHOVEL -> add(ItemTags.SHOVELS, entry);
            case AXE -> add(ItemTags.AXES, entry);
            case HOE -> add(ItemTags.HOES, entry);
            case SWORD -> add(ItemTags.SWORDS, entry);
            case NONE, SCYTHE, CUDGEL, SHEARS -> {
            }
        }
        if (type == EquipmentType.ARROW) {
            add(ItemTags.ARROWS, entry);
        }
    }

    private void addEnchantmentTags(Catalog.EquipmentEntry entry) {
        EquipmentType type = entry.key().type();
        if (entry.key().durability() > 0) {
            add(ItemTags.DURABILITY_ENCHANTABLE, entry);
        }
        if (type.category() == EquipmentCategory.TOOL
                && type.miningFamily() != MiningFamily.NONE) {
            add(ItemTags.MINING_ENCHANTABLE, entry);
        }
        boolean melee = (type.category() == EquipmentCategory.TOOL
                        || type.category() == EquipmentCategory.WEAPON)
                && type != EquipmentType.FISHING_ROD
                && type != EquipmentType.BOW
                && type != EquipmentType.ARROW;
        if (melee) {
            add(ItemTags.MELEE_WEAPON_ENCHANTABLE, entry);
            add(ItemTags.WEAPON_ENCHANTABLE, entry);
            add(ItemTags.SHARP_WEAPON_ENCHANTABLE, entry);
        }
        if (type == EquipmentType.FISHING_ROD) {
            add(ItemTags.FISHING_ENCHANTABLE, entry);
        }
        if (type == EquipmentType.BOW) {
            add(ItemTags.BOW_ENCHANTABLE, entry);
        }
        if (type.armorForm() == EquipmentType.ArmorForm.PLATE
                || type.armorForm() == EquipmentType.ArmorForm.CHAIN) {
            add(ItemTags.ARMOR_ENCHANTABLE, entry);
            add(ItemTags.EQUIPPABLE_ENCHANTABLE, entry);
            addArmorSlotTags(entry, type.armorType().orElseThrow());
        }
    }

    private void addR196EnchantmentTags(Catalog.EquipmentEntry entry) {
        EquipmentType type = entry.key().type();
        MiteMaterial material = entry.key().material();
        if (isDurabilityEnchantable(type, material)) {
            add(InfXItemTags.R196_DURABILITY_ENCHANTABLE, entry);
        }
        if (type == EquipmentType.SWORD) {
            add(InfXItemTags.R196_DISARMING_ENCHANTABLE, entry);
        }
        if (type == EquipmentType.KNIFE || type == EquipmentType.DAGGER) {
            add(InfXItemTags.R196_BUTCHERING_ENCHANTABLE, entry);
        }
        if (type == EquipmentType.CUDGEL || type == EquipmentType.WAR_HAMMER) {
            add(InfXItemTags.R196_STUNNING_ENCHANTABLE, entry);
        }
        if ((type == EquipmentType.SWORD || type == EquipmentType.SCYTHE)
                && material != MiteMaterial.SILVER
                && material != MiteMaterial.MITHRIL) {
            add(InfXItemTags.R196_VAMPIRISM_ENCHANTABLE, entry);
        }
        if (type == EquipmentType.SWORD
                || type == EquipmentType.BATTLE_AXE
                || type == EquipmentType.SCYTHE) {
            add(InfXItemTags.R196_SLAUGHTER_ENCHANTABLE, entry);
        }
        if (type == EquipmentType.BATTLE_AXE) {
            add(InfXItemTags.R196_CLEAVING_ENCHANTABLE, entry);
        }
        if (type == EquipmentType.HOE
                || type == EquipmentType.MATTOCK
                || type == EquipmentType.SCYTHE) {
            add(InfXItemTags.R196_HARVESTING_ENCHANTABLE, entry);
            add(InfXItemTags.R196_FERTILITY_ENCHANTABLE, entry);
        }
        if (type == EquipmentType.PICKAXE) {
            add(InfXItemTags.R196_PENETRATION_ENCHANTABLE, entry);
        }
        if (type == EquipmentType.AXE || type == EquipmentType.BATTLE_AXE) {
            add(InfXItemTags.R196_TREE_FELLING_ENCHANTABLE, entry);
        }
        if (type == EquipmentType.PICKAXE || type == EquipmentType.SHOVEL) {
            add(InfXItemTags.R196_FORTUNE_ENCHANTABLE, entry);
        }
        if (type == EquipmentType.LEGGINGS || type == EquipmentType.CHAINMAIL_LEGGINGS) {
            add(InfXItemTags.R196_FREE_MOVEMENT_ENCHANTABLE, entry);
        }
        if (type == EquipmentType.CHESTPLATE || type == EquipmentType.CHAINMAIL_CHESTPLATE) {
            add(InfXItemTags.R196_CHEST_ARMOR_ENCHANTABLE, entry);
        }
        addVanillaMiteEnchantmentTags(entry, type, material);
    }

    /** Item targets for the vanilla-derived MITE enchantments, following MITE's class checks. */
    private void addVanillaMiteEnchantmentTags(
            Catalog.EquipmentEntry entry, EquipmentType type, MiteMaterial material) {
        // MITE ItemKnife extends ItemDagger extends ItemSword, so "instanceof ItemSword"
        // checks (bane of arthropods, fire aspect, looting) cover all three.
        boolean swordFamily = type == EquipmentType.SWORD
                || type == EquipmentType.DAGGER
                || type == EquipmentType.KNIFE;
        if (swordFamily) {
            add(InfXItemTags.R196_SWORD_FAMILY_ENCHANTABLE, entry);
        }
        if (swordFamily || type == EquipmentType.CUDGEL) {
            add(InfXItemTags.R196_LOOTING_ENCHANTABLE, entry);
        }
        if (type == EquipmentType.WAR_HAMMER) {
            add(InfXItemTags.R196_SMITE_ENCHANTABLE, entry);
        }
        if (type == EquipmentType.CUDGEL || type == EquipmentType.WAR_HAMMER) {
            add(InfXItemTags.R196_KNOCKBACK_ENCHANTABLE, entry);
        }
        // MITE efficiency: pickaxe class (war hammers excluded), the axe family, shovels
        // (mattock extends ItemShovel) and hoes.
        if (type == EquipmentType.PICKAXE
                || type == EquipmentType.HATCHET
                || type == EquipmentType.AXE
                || type == EquipmentType.BATTLE_AXE
                || type == EquipmentType.SHOVEL
                || type == EquipmentType.MATTOCK
                || type == EquipmentType.HOE) {
            add(InfXItemTags.R196_EFFICIENCY_ENCHANTABLE, entry);
        }
        // MITE silk touch: exact pickaxe/shovel classes plus shears, knives and daggers.
        if (type == EquipmentType.PICKAXE
                || type == EquipmentType.SHOVEL
                || type == EquipmentType.KNIFE
                || type == EquipmentType.DAGGER) {
            add(InfXItemTags.R196_SILK_TOUCH_ENCHANTABLE, entry);
        }
        if (type == EquipmentType.CHESTPLATE) {
            add(InfXItemTags.R196_THORNS_ENCHANTABLE, entry);
        }
        if ((type == EquipmentType.CHESTPLATE || type == EquipmentType.LEGGINGS)
                && material.has(MiteMaterial.Flag.METAL)) {
            add(InfXItemTags.R196_SOLID_METAL_TORSO_ENCHANTABLE, entry);
        }
    }

    private static boolean isDurabilityEnchantable(EquipmentType type, MiteMaterial material) {
        if (type.armorForm() == EquipmentType.ArmorForm.PLATE
                && material.has(MiteMaterial.Flag.METAL)) {
            return true;
        }
        return switch (type) {
            case CUDGEL, PICKAXE, SHOVEL, HATCHET, AXE, HOE, BATTLE_AXE, FISHING_ROD, BOW -> true;
            default -> false;
        };
    }

    private void addArmorSlotTags(Catalog.EquipmentEntry entry, ArmorType armorType) {
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

    private void addHarvestTierTag(Catalog.EquipmentEntry entry) {
        if (entry.key().type().miningFamily() == MiningFamily.NONE) {
            return;
        }
        entry.key().material().harvestTier()
                .ifPresent(tier -> add(InfXItemTags.toolTier(tier), entry));
    }

    private void add(TagKey<Item> tag, Catalog.RawEntry entry) {
        tag(tag).add(entry.holder().getKey());
    }

    private void add(TagKey<Item> tag, Catalog.EquipmentEntry entry) {
        tag(tag).add(entry.holder().getKey());
    }

    private static ResourceKey<Item> itemKey(Item item) {
        return BuiltInRegistries.ITEM.getResourceKey(item).orElseThrow();
    }
}
