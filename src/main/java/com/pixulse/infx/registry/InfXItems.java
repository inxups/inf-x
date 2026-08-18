package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.Catalog;
import com.pixulse.infx.item.InfxBucketItem;
import com.pixulse.infx.item.InfxCarrotOnAStickItem;
import com.pixulse.infx.item.InfxWarpedFungusOnAStickItem;
import com.pixulse.infx.item.EquipmentKey;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.GelatinousSphereItem;
import com.pixulse.infx.item.InfxMobBucketItem;
import com.pixulse.infx.item.MobBucketKind;
import com.pixulse.infx.item.InfxSolidBucketItem;
import com.pixulse.infx.item.ToolItem;
import com.pixulse.infx.item.RuneStoneItem;
import com.pixulse.infx.block.RuneStoneBlock;
import com.pixulse.infx.item.material.InfxMaterial;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class InfXItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(InfiniteX.MOD_ID);

    public static final DeferredItem<BlockItem> SILVER_ORE =
            ITEMS.registerSimpleBlockItem(InfXBlocks.SILVER_ORE);
    public static final DeferredItem<BlockItem> MITHRIL_ORE =
            ITEMS.registerSimpleBlockItem(InfXBlocks.MITHRIL_ORE);
    public static final DeferredItem<BlockItem> ADAMANTIUM_ORE =
            ITEMS.registerSimpleBlockItem(InfXBlocks.ADAMANTIUM_ORE, Item.Properties::fireResistant);
    public static final DeferredItem<BlockItem> DEEPSLATE_SILVER_ORE =
            ITEMS.registerSimpleBlockItem(InfXBlocks.DEEPSLATE_SILVER_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_MITHRIL_ORE =
            ITEMS.registerSimpleBlockItem(InfXBlocks.DEEPSLATE_MITHRIL_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_ADAMANTIUM_ORE = ITEMS.registerSimpleBlockItem(
            InfXBlocks.DEEPSLATE_ADAMANTIUM_ORE, Item.Properties::fireResistant);

    public static final List<DeferredItem<BlockItem>> ORES = List.of(
            SILVER_ORE,
            DEEPSLATE_SILVER_ORE,
            MITHRIL_ORE,
            DEEPSLATE_MITHRIL_ORE,
            ADAMANTIUM_ORE,
            DEEPSLATE_ADAMANTIUM_ORE);

    public static final DeferredItem<BlockItem> SILVER_BLOCK = ITEMS.registerSimpleBlockItem(InfXBlocks.SILVER_BLOCK);
    public static final DeferredItem<BlockItem> ANCIENT_METAL_BLOCK =
            ITEMS.registerSimpleBlockItem(InfXBlocks.ANCIENT_METAL_BLOCK);
    public static final DeferredItem<BlockItem> MITHRIL_BLOCK = ITEMS.registerSimpleBlockItem(InfXBlocks.MITHRIL_BLOCK);
    public static final DeferredItem<BlockItem> ADAMANTIUM_BLOCK =
            ITEMS.registerSimpleBlockItem(InfXBlocks.ADAMANTIUM_BLOCK, Item.Properties::fireResistant);
    public static final List<DeferredItem<BlockItem>> METAL_STORAGE_BLOCKS =
            List.of(SILVER_BLOCK, ANCIENT_METAL_BLOCK, MITHRIL_BLOCK, ADAMANTIUM_BLOCK);

    public static final DeferredItem<BlockItem> MANTLE = ITEMS.registerSimpleBlockItem(InfXBlocks.MANTLE);
    public static final DeferredItem<BlockItem> MITHRIL_RUNE_STONE = ITEMS.registerItem(
            "mithril_rune_stone",
            properties -> new RuneStoneItem(InfXBlocks.MITHRIL_RUNE_STONE.get(), properties),
            properties -> properties
                    .useBlockDescriptionPrefix()
                    .component(DataComponents.BLOCK_STATE, RuneStoneBlock.itemState(0)));
    public static final DeferredItem<BlockItem> ADAMANTIUM_RUNE_STONE = ITEMS.registerItem(
            "adamantium_rune_stone",
            properties -> new RuneStoneItem(InfXBlocks.ADAMANTIUM_RUNE_STONE.get(), properties),
            properties -> properties
                    .useBlockDescriptionPrefix()
                    .fireResistant()
                    .component(DataComponents.BLOCK_STATE, RuneStoneBlock.itemState(0)));
    public static final List<DeferredItem<BlockItem>> WORLD_BLOCKS =
            List.of(MANTLE, MITHRIL_RUNE_STONE, ADAMANTIUM_RUNE_STONE);
    public static final DeferredItem<BlockItem> GRAVEL = ITEMS.registerSimpleBlockItem(InfXBlocks.GRAVEL);
    public static final DeferredItem<BlockItem> NETHER_GRAVEL = ITEMS.registerSimpleBlockItem(InfXBlocks.NETHER_GRAVEL);
    public static final DeferredItem<BlockItem> WITHERWOOD = ITEMS.registerSimpleBlockItem(InfXBlocks.WITHERWOOD);
    public static final DeferredItem<BlockItem> BLUEBERRY_BUSH =
            ITEMS.registerSimpleBlockItem(InfXBlocks.BLUEBERRY_BUSH, properties -> properties.stacksTo(8));
    public static final DeferredItem<BlockItem> CORE = ITEMS.registerSimpleBlockItem(InfXBlocks.CORE);
    public static final DeferredItem<BlockItem> SNOW_SLAB = ITEMS.registerSimpleBlockItem(InfXBlocks.SNOW_SLAB);
    public static final List<DeferredItem<BlockItem>> FULLTEXT_BLOCKS =
            List.of(GRAVEL, NETHER_GRAVEL, WITHERWOOD, BLUEBERRY_BUSH, CORE);
    public static final List<DeferredItem<BlockItem>> INFX_RECIPE_BLOCKS = List.of(SNOW_SLAB);
    public static final DeferredItem<BlockItem> EMERALD_ENCHANTING_TABLE =
            ITEMS.registerSimpleBlockItem(InfXBlocks.EMERALD_ENCHANTING_TABLE);
    public static final DeferredItem<BlockItem> DIAMOND_ENCHANTING_TABLE =
            ITEMS.registerSimpleBlockItem(InfXBlocks.DIAMOND_ENCHANTING_TABLE);
    public static final List<DeferredItem<BlockItem>> ENCHANTING_TABLES =
            List.of(EMERALD_ENCHANTING_TABLE, DIAMOND_ENCHANTING_TABLE);
    public static final List<DeferredItem<BlockItem>> METAL_SAFES = InfXBlocks.METAL_SAFES.stream()
            .map(ITEMS::registerSimpleBlockItem)
            .toList();

    public static final List<DeferredItem<BlockItem>> METAL_ANVILS = InfXBlocks.METAL_ANVILS.stream()
            .map(anvil -> ITEMS.registerItem(
                    anvil.getId().getPath(),
                    properties -> new BlockItem(anvil.get(), properties),
                    properties -> {
                        Item.Properties configured = properties
                                .useBlockDescriptionPrefix()
                                .durability(anvil.get().maximumDamage());
                        return anvil.get().material().has(InfxMaterial.Flag.LAVA_SAFE)
                                ? configured.fireResistant()
                                : configured;
                    }))
            .toList();

    public static final DeferredItem<BlockItem> COPPER_WORKBENCH =
            ITEMS.registerSimpleBlockItem(InfXBlocks.COPPER_WORKBENCH);
    public static final DeferredItem<BlockItem> SILVER_WORKBENCH =
            ITEMS.registerSimpleBlockItem(InfXBlocks.SILVER_WORKBENCH);
    public static final DeferredItem<BlockItem> GOLD_WORKBENCH =
            ITEMS.registerSimpleBlockItem(InfXBlocks.GOLD_WORKBENCH);
    public static final DeferredItem<BlockItem> IRON_WORKBENCH =
            ITEMS.registerSimpleBlockItem(InfXBlocks.IRON_WORKBENCH);
    public static final DeferredItem<BlockItem> ANCIENT_METAL_WORKBENCH =
            ITEMS.registerSimpleBlockItem(InfXBlocks.ANCIENT_METAL_WORKBENCH);
    public static final DeferredItem<BlockItem> MITHRIL_WORKBENCH =
            ITEMS.registerSimpleBlockItem(InfXBlocks.MITHRIL_WORKBENCH);
    public static final DeferredItem<BlockItem> ADAMANTIUM_WORKBENCH =
            ITEMS.registerSimpleBlockItem(InfXBlocks.ADAMANTIUM_WORKBENCH);
    public static final List<DeferredItem<BlockItem>> STRIPPED_LOG_WORKBENCHES = InfXBlocks.STRIPPED_LOG_WORKBENCHES.stream()
            .flatMap(workbench -> Stream.of(
                    ITEMS.registerSimpleBlockItem(workbench.flint()),
                    ITEMS.registerSimpleBlockItem(workbench.obsidian())))
            .toList();
    public static final DeferredItem<BlockItem> CLAY_FURNACE =
            ITEMS.registerSimpleBlockItem(InfXBlocks.CLAY_FURNACE, properties -> properties.stacksTo(1));
    public static final DeferredItem<BlockItem> SANDSTONE_FURNACE =
            ITEMS.registerSimpleBlockItem(InfXBlocks.SANDSTONE_FURNACE, properties -> properties.stacksTo(1));
    public static final DeferredItem<BlockItem> HARDENED_CLAY_FURNACE =
            ITEMS.registerSimpleBlockItem(InfXBlocks.HARDENED_CLAY_FURNACE, properties -> properties.stacksTo(1));
    public static final DeferredItem<BlockItem> OBSIDIAN_FURNACE =
            ITEMS.registerSimpleBlockItem(InfXBlocks.OBSIDIAN_FURNACE, properties -> properties.stacksTo(1));
    public static final DeferredItem<BlockItem> NETHERRACK_FURNACE =
            ITEMS.registerSimpleBlockItem(InfXBlocks.NETHERRACK_FURNACE, properties -> properties.stacksTo(1));

    public static final List<DeferredItem<BlockItem>> WORKBENCHES =
            Stream.concat(
                            Stream.of(
                                    COPPER_WORKBENCH,
                                    SILVER_WORKBENCH,
                                    GOLD_WORKBENCH,
                                    IRON_WORKBENCH,
                                    ANCIENT_METAL_WORKBENCH,
                                    MITHRIL_WORKBENCH,
                                    ADAMANTIUM_WORKBENCH),
                            STRIPPED_LOG_WORKBENCHES.stream())
                    .toList();

    public static final List<DeferredItem<BlockItem>> FURNACES = List.of(
            CLAY_FURNACE,
            SANDSTONE_FURNACE,
            HARDENED_CLAY_FURNACE,
            OBSIDIAN_FURNACE,
            NETHERRACK_FURNACE);

    public static final DeferredItem<Item> FLOUR = simple("flour");
    public static final DeferredItem<Item> WATER_BOWL = drinkContainer("water_bowl");
    public static final DeferredItem<Item> DOUGH = food("dough", 1, 0.1F);
    public static final DeferredItem<Item> SALAD = bowlFood("salad", 5, 0.6F);
    public static final DeferredItem<Item> BLUEBERRIES = food("blueberries", 2, 0.2F);
    public static final DeferredItem<Item> BLUEBERRY_PORRIDGE = bowlFood("blueberry_porridge", 6, 0.7F);
    public static final DeferredItem<Item> MILK_BOWL = bowlFood("milk_bowl", 4, 0.5F, Consumables.MILK_BUCKET);
    public static final DeferredItem<Item> CEREAL_PORRIDGE = bowlFood("cereal_porridge", 6, 0.7F);
    public static final DeferredItem<Item> CHOCOLATE = food("chocolate", 5, 0.6F);
    public static final DeferredItem<Item> PUMPKIN_SOUP = bowlFood("pumpkin_soup", 6, 0.6F);
    public static final DeferredItem<Item> CREAM_OF_MUSHROOM_SOUP = bowlFood("cream_of_mushroom_soup", 7, 0.8F);
    public static final DeferredItem<Item> ONION = ITEMS.registerItem(
            "onion",
            properties -> new BlockItem(InfXBlocks.INFX_ONION.get(), properties),
            properties -> properties
                    .stacksTo(16)
                    .food(new FoodProperties.Builder()
                            .nutrition(2)
                            .saturationModifier(0.2F)
                            .build()));
    public static final DeferredItem<Item> VEGETABLE_SOUP = bowlFood("vegetable_soup", 7, 0.8F);
    public static final DeferredItem<Item> CREAM_OF_VEGETABLE_SOUP = bowlFood("cream_of_vegetable_soup", 8, 0.9F);
    public static final DeferredItem<Item> CHICKEN_SOUP = bowlFood("chicken_soup", 8, 0.9F);
    public static final DeferredItem<Item> BEEF_STEW = bowlFood("beef_stew", 10, 1.0F);
    public static final DeferredItem<Item> ORANGE = food("orange", 4, 0.4F);
    public static final DeferredItem<Item> FRUIT_ICE = bowlFood("fruit_ice", 4, 0.5F);
    public static final DeferredItem<Item> CHEESE = food("cheese", 5, 0.7F);
    public static final DeferredItem<Item> MASHED_POTATO = bowlFood("mashed_potato", 7, 0.8F);
    public static final DeferredItem<Item> ICE_CREAM = bowlFood("ice_cream", 6, 0.8F);
    public static final DeferredItem<Item> BANANA = food("banana", 4, 0.5F);
    public static final DeferredItem<Item> WORM = food("worm", 1, 0.1F);
    public static final DeferredItem<Item> COOKED_WORM = food("cooked_worm", 3, 0.4F);

    public static final List<DeferredItem<Item>> INFX_FOODS = List.of(
            DOUGH,
            SALAD,
            BLUEBERRIES,
            BLUEBERRY_PORRIDGE,
            MILK_BOWL,
            CEREAL_PORRIDGE,
            CHOCOLATE,
            PUMPKIN_SOUP,
            CREAM_OF_MUSHROOM_SOUP,
            ONION,
            VEGETABLE_SOUP,
            CREAM_OF_VEGETABLE_SOUP,
            CHICKEN_SOUP,
            BEEF_STEW,
            ORANGE,
            FRUIT_ICE,
            CHEESE,
            MASHED_POTATO,
            ICE_CREAM,
            BANANA,
            WORM,
            COOKED_WORM);

    public static final List<InfxMaterial> BUCKET_MATERIALS = List.of(
            InfxMaterial.COPPER,
            InfxMaterial.SILVER,
            InfxMaterial.GOLD,
            InfxMaterial.IRON,
            InfxMaterial.ANCIENT_METAL,
            InfxMaterial.MITHRIL,
            InfxMaterial.ADAMANTIUM);
    private static final Map<InfxMaterial, EnumMap<InfxBucketItem.Contents, DeferredItem<InfxBucketItem>>>
            BUCKETS_BY_MATERIAL = new EnumMap<>(InfxMaterial.class);
    public static final List<DeferredItem<InfxBucketItem>> INFX_BUCKETS = registerBuckets();

    private static final Map<InfxMaterial, EnumMap<MobBucketKind, DeferredItem<InfxMobBucketItem>>>
            MOB_BUCKETS_BY_MATERIAL = new EnumMap<>(InfxMaterial.class);
    public static final List<DeferredItem<InfxMobBucketItem>> INFX_MOB_BUCKETS = registerMobBuckets();

    private static final Map<InfxMaterial, DeferredItem<InfxSolidBucketItem>> POWDER_SNOW_BUCKETS_BY_MATERIAL =
            new EnumMap<>(InfxMaterial.class);
    public static final List<DeferredItem<InfxSolidBucketItem>> INFX_POWDER_SNOW_BUCKETS =
            registerPowderSnowBuckets();

    public static final DeferredItem<Item> BOTTLE_OF_DISENCHANTING = ITEMS.registerItem(
            "bottle_of_disenchanting",
            Item::new,
            properties -> properties
                    .stacksTo(1)
                    .component(DataComponents.CONSUMABLE, Consumables.MILK_BUCKET)
                    .usingConvertsTo(Items.GLASS_BOTTLE));
    public static final DeferredItem<Item> RECORD_UNDERWORLD = record("record_underworld", InfXJukeboxSongs.UNDERWORLD);
    public static final DeferredItem<Item> RECORD_DESCENT = record("record_descent", InfXJukeboxSongs.DESCENT);
    public static final DeferredItem<Item> RECORD_WANDERER = record("record_wanderer", InfXJukeboxSongs.WANDERER);
    public static final DeferredItem<Item> RECORD_LEGENDS = record("record_legends", InfXJukeboxSongs.LEGENDS);
    public static final List<DeferredItem<Item>> INFX_RECORDS =
            List.of(RECORD_UNDERWORLD, RECORD_DESCENT, RECORD_WANDERER, RECORD_LEGENDS);

    public static final DeferredItem<GelatinousSphereItem> GREEN_GELATINOUS_SPHERE = gelatinousSphere(
            "green_gelatinous_sphere", GelatinousSphereItem.Color.GREEN);
    public static final DeferredItem<GelatinousSphereItem> OCHRE_GELATINOUS_SPHERE = gelatinousSphere(
            "ochre_gelatinous_sphere", GelatinousSphereItem.Color.OCHRE);
    public static final DeferredItem<GelatinousSphereItem> CRIMSON_GELATINOUS_SPHERE = gelatinousSphere(
            "crimson_gelatinous_sphere", GelatinousSphereItem.Color.CRIMSON);
    public static final DeferredItem<GelatinousSphereItem> GRAY_GELATINOUS_SPHERE = gelatinousSphere(
            "gray_gelatinous_sphere", GelatinousSphereItem.Color.GRAY);
    public static final DeferredItem<GelatinousSphereItem> BLACK_GELATINOUS_SPHERE = gelatinousSphere(
            "black_gelatinous_sphere", GelatinousSphereItem.Color.BLACK);
    public static final List<DeferredItem<GelatinousSphereItem>> GELATINOUS_SPHERES = List.of(
            GREEN_GELATINOUS_SPHERE,
            OCHRE_GELATINOUS_SPHERE,
            CRIMSON_GELATINOUS_SPHERE,
            GRAY_GELATINOUS_SPHERE,
            BLACK_GELATINOUS_SPHERE);

    /** One 26.2-style spawn egg for every INFX mob entity (excludes gelatinous_sphere projectile). */
    public static final List<DeferredItem<SpawnEggItem>> SPAWN_EGGS = registerSpawnEggs();

    /** InfX carrot-on-a-stick hook materials, mirroring the fishing-rod material set. */
    public static final List<InfxMaterial> FISHING_HOOK_MATERIALS = List.of(
            InfxMaterial.FLINT,
            InfxMaterial.OBSIDIAN,
            InfxMaterial.COPPER,
            InfxMaterial.SILVER,
            InfxMaterial.GOLD,
            InfxMaterial.IRON,
            InfxMaterial.ANCIENT_METAL,
            InfxMaterial.MITHRIL,
            InfxMaterial.ADAMANTIUM);

    /** InfX carrot on a stick per fishing-hook material, boosting the ridden pig. */
    public static final Map<InfxMaterial, DeferredItem<InfxCarrotOnAStickItem>> CARROT_ON_A_STICKS =
            registerCarrotOnASticks();

    /** Modern warped-fungus-on-a-stick per fishing-hook material, steering striders. */
    public static final Map<InfxMaterial, DeferredItem<InfxWarpedFungusOnAStickItem>> WARPED_FUNGUS_ON_A_STICKS =
            registerWarpedFungusOnASticks();

    private static final Catalog CATALOG = Catalog.register(ITEMS);

    public static final DeferredItem<Item> FLINT_CHIP = CATALOG.raw("flint_chip").holderAs(Item.class);
    public static final DeferredItem<Item> SINEW = CATALOG.raw("sinew").holderAs(Item.class);
    public static final DeferredItem<Item> OBSIDIAN_SHARD = CATALOG.raw("obsidian_shard").holderAs(Item.class);
    public static final DeferredItem<Item> DIAMOND_SHARD = CATALOG.raw("diamond_shard").holderAs(Item.class);
    public static final DeferredItem<Item> EMERALD_SHARD = CATALOG.raw("emerald_shard").holderAs(Item.class);
    public static final DeferredItem<Item> SILVER_NUGGET = CATALOG.raw("silver_nugget").holderAs(Item.class);
    public static final DeferredItem<Item> MITHRIL_NUGGET = CATALOG.raw("mithril_nugget").holderAs(Item.class);
    public static final DeferredItem<Item> ADAMANTIUM_NUGGET = CATALOG.raw("adamantium_nugget").holderAs(Item.class);
    public static final DeferredItem<Item> SILVER_INGOT = CATALOG.raw("silver_ingot").holderAs(Item.class);
    public static final DeferredItem<Item> ANCIENT_METAL_INGOT =
            CATALOG.raw("ancient_metal_ingot").holderAs(Item.class);
    public static final DeferredItem<Item> MITHRIL_INGOT = CATALOG.raw("mithril_ingot").holderAs(Item.class);
    public static final DeferredItem<Item> ADAMANTIUM_INGOT =
            CATALOG.raw("adamantium_ingot").holderAs(Item.class);
    public static final DeferredItem<Item> RAW_SILVER = CATALOG.raw("raw_silver").holderAs(Item.class);
    public static final DeferredItem<Item> RAW_MITHRIL = CATALOG.raw("raw_mithril").holderAs(Item.class);
    public static final DeferredItem<Item> RAW_ADAMANTIUM = CATALOG.raw("raw_adamantium").holderAs(Item.class);
    public static final DeferredItem<ToolItem> FLINT_HATCHET =
            CATALOG.equipment(InfxMaterial.FLINT, EquipmentType.HATCHET).holderAs(ToolItem.class);
    public static final DeferredItem<ToolItem> FLINT_SHOVEL =
            CATALOG.equipment(InfxMaterial.FLINT, EquipmentType.SHOVEL).holderAs(ToolItem.class);
    public static final DeferredItem<ToolItem> FLINT_AXE =
            CATALOG.equipment(InfxMaterial.FLINT, EquipmentType.AXE).holderAs(ToolItem.class);
    public static final DeferredItem<ToolItem> COPPER_PICKAXE =
            CATALOG.equipment(InfxMaterial.COPPER, EquipmentType.PICKAXE).holderAs(ToolItem.class);
    public static final DeferredItem<ToolItem> COPPER_SHOVEL =
            CATALOG.equipment(InfxMaterial.COPPER, EquipmentType.SHOVEL).holderAs(ToolItem.class);
    public static final DeferredItem<ToolItem> COPPER_AXE =
            CATALOG.equipment(InfxMaterial.COPPER, EquipmentType.AXE).holderAs(ToolItem.class);
    public static final DeferredItem<ToolItem> COPPER_HOE =
            CATALOG.equipment(InfxMaterial.COPPER, EquipmentType.HOE).holderAs(ToolItem.class);
    public static final DeferredItem<ToolItem> COPPER_SWORD =
            CATALOG.equipment(InfxMaterial.COPPER, EquipmentType.SWORD).holderAs(ToolItem.class);
    public static final DeferredItem<ToolItem> IRON_PICKAXE =
            CATALOG.equipment(InfxMaterial.IRON, EquipmentType.PICKAXE).holderAs(ToolItem.class);
    public static final DeferredItem<ToolItem> IRON_SHOVEL =
            CATALOG.equipment(InfxMaterial.IRON, EquipmentType.SHOVEL).holderAs(ToolItem.class);
    public static final DeferredItem<ToolItem> IRON_AXE =
            CATALOG.equipment(InfxMaterial.IRON, EquipmentType.AXE).holderAs(ToolItem.class);
    public static final DeferredItem<ToolItem> IRON_HOE =
            CATALOG.equipment(InfxMaterial.IRON, EquipmentType.HOE).holderAs(ToolItem.class);
    public static final DeferredItem<ToolItem> IRON_SWORD =
            CATALOG.equipment(InfxMaterial.IRON, EquipmentType.SWORD).holderAs(ToolItem.class);

    private InfXItems() {}

    private static DeferredItem<Item> simple(String path) {
        return ITEMS.registerItem(path, Item::new, properties -> properties.stacksTo(16));
    }

    private static DeferredItem<Item> drinkContainer(String path) {
        return ITEMS.registerItem(
                path,
                Item::new,
                properties -> properties
                        .stacksTo(8)
                        .usingConvertsTo(Items.BOWL)
                        .component(DataComponents.CONSUMABLE, Consumables.DEFAULT_DRINK));
    }

    private static DeferredItem<Item> food(String path, int nutrition, float saturation) {
        return ITEMS.registerItem(
                path,
                Item::new,
                properties -> properties
                        .stacksTo(16)
                        .food(new FoodProperties.Builder()
                                .nutrition(nutrition)
                                .saturationModifier(saturation)
                                .build()));
    }

    private static DeferredItem<Item> bowlFood(String path, int nutrition, float saturation) {
        return bowlFood(path, nutrition, saturation, Consumables.DEFAULT_FOOD);
    }

    private static DeferredItem<Item> bowlFood(
            String path, int nutrition, float saturation, Consumable consumable) {
        return ITEMS.registerItem(
                path,
                Item::new,
                properties -> properties
                        .stacksTo(8)
                        .usingConvertsTo(Items.BOWL)
                        .food(new FoodProperties.Builder()
                                .nutrition(nutrition)
                                .saturationModifier(saturation)
                                .build(), consumable));
    }

    private static List<DeferredItem<InfxBucketItem>> registerBuckets() {
        List<DeferredItem<InfxBucketItem>> registered = new ArrayList<>();
        for (InfxMaterial material : BUCKET_MATERIALS) {
            EnumMap<InfxBucketItem.Contents, DeferredItem<InfxBucketItem>> variants =
                    new EnumMap<>(InfxBucketItem.Contents.class);
            BUCKETS_BY_MATERIAL.put(material, variants);
            for (InfxBucketItem.Contents contents : InfxBucketItem.Contents.values()) {
                DeferredItem<InfxBucketItem> bucket = ITEMS.registerItem(
                        contents.path(material),
                        properties -> new InfxBucketItem(
                                material,
                                contents,
                                () -> bucket(material, InfxBucketItem.Contents.EMPTY).value(),
                                () -> bucket(material, InfxBucketItem.Contents.WATER).value(),
                                () -> bucket(material, InfxBucketItem.Contents.LAVA).value(),
                                properties),
                        properties -> {
                            Item.Properties configured = properties.stacksTo(
                                    contents == InfxBucketItem.Contents.EMPTY ? 8 : 1);
                            if (contents == InfxBucketItem.Contents.WATER
                                    || contents == InfxBucketItem.Contents.LAVA
                                    || contents == InfxBucketItem.Contents.MILK) {
                                // InfX ItemVessel#setContainerItem: a filled vessel used in crafting
                                // returns the matching empty bucket as its remainder. Stone buckets are
                                // exempt: their 1:1 conversion recipe already yields the empty bucket,
                                // and a remainder would return a second one.
                                configured.craftRemainder(
                                        bucket(material, InfxBucketItem.Contents.EMPTY).value());
                            }
                            if (contents == InfxBucketItem.Contents.MILK) {
                                configured.component(DataComponents.CONSUMABLE, Consumables.MILK_BUCKET);
                            }
                            return material == InfxMaterial.ADAMANTIUM
                                    ? configured.fireResistant()
                                    : configured;
                        });
                variants.put(contents, bucket);
                registered.add(bucket);
            }
        }
        return List.copyOf(registered);
    }

    private static List<DeferredItem<InfxMobBucketItem>> registerMobBuckets() {
        List<DeferredItem<InfxMobBucketItem>> registered = new ArrayList<>();
        for (InfxMaterial material : BUCKET_MATERIALS) {
            EnumMap<MobBucketKind, DeferredItem<InfxMobBucketItem>> variants =
                    new EnumMap<>(MobBucketKind.class);
            MOB_BUCKETS_BY_MATERIAL.put(material, variants);
            for (MobBucketKind kind : MobBucketKind.values()) {
                DeferredItem<InfxMobBucketItem> bucket = ITEMS.registerItem(
                        kind.path(material),
                        properties -> new InfxMobBucketItem(
                                material,
                                kind,
                                () -> bucket(material, InfxBucketItem.Contents.EMPTY).value(),
                                properties),
                        properties -> {
                            Item.Properties configured = properties
                                    .stacksTo(1)
                                    .component(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY);
                            // InfX ItemVessel#setContainerItem: a filled vessel used in crafting
                            // returns the matching empty bucket as its remainder.
                            configured.craftRemainder(bucket(material, InfxBucketItem.Contents.EMPTY).value());
                            if (kind.food() != null) {
                                configured.component(DataComponents.FOOD, kind.food());
                            }
                            return material == InfxMaterial.ADAMANTIUM
                                    ? configured.fireResistant()
                                    : configured;
                        });
                variants.put(kind, bucket);
                registered.add(bucket);
            }
        }
        return List.copyOf(registered);
    }

    private static List<DeferredItem<InfxSolidBucketItem>> registerPowderSnowBuckets() {
        List<DeferredItem<InfxSolidBucketItem>> registered = new ArrayList<>();
        for (InfxMaterial material : BUCKET_MATERIALS) {
            DeferredItem<InfxSolidBucketItem> bucket = ITEMS.registerItem(
                    "powder_snow_" + material.path() + "_bucket",
                    properties -> new InfxSolidBucketItem(
                            material,
                            Blocks.POWDER_SNOW,
                            SoundEvents.BUCKET_EMPTY_POWDER_SNOW,
                            () -> bucket(material, InfxBucketItem.Contents.EMPTY).value(),
                            properties),
                    properties -> {
                        Item.Properties configured = properties.stacksTo(1);
                        // InfX ItemVessel#setContainerItem: a filled vessel used in crafting
                        // returns the matching empty bucket as its remainder.
                        configured.craftRemainder(bucket(material, InfxBucketItem.Contents.EMPTY).value());
                        return material == InfxMaterial.ADAMANTIUM
                                ? configured.fireResistant()
                                : configured;
                    });
            POWDER_SNOW_BUCKETS_BY_MATERIAL.put(material, bucket);
            registered.add(bucket);
        }
        return List.copyOf(registered);
    }

    private static DeferredItem<Item> record(
            String path, net.minecraft.resources.ResourceKey<net.minecraft.world.item.JukeboxSong> song) {
        return ITEMS.registerItem(
                path,
                Item::new,
                properties -> properties.stacksTo(1).rarity(Rarity.UNCOMMON).jukeboxPlayable(song));
    }

    private static DeferredItem<GelatinousSphereItem> gelatinousSphere(
            String path, GelatinousSphereItem.Color color) {
        return ITEMS.registerItem(
                path,
                properties -> new GelatinousSphereItem(color, properties),
                properties -> properties.stacksTo(16));
    }

    private static List<DeferredItem<SpawnEggItem>> registerSpawnEggs() {
        List<DeferredItem<SpawnEggItem>> eggs = new ArrayList<>();
        for (DeferredHolder<EntityType<?>, ? extends EntityType<?>> type : InfXEntityTypes.ALL) {
            String path = type.getId().getPath() + "_spawn_egg";
            eggs.add(ITEMS.registerItem(
                    path,
                    SpawnEggItem::new,
                    properties -> properties.stacksTo(16).spawnEgg(type.get())));
        }
        return List.copyOf(eggs);
    }

    private static Map<InfxMaterial, DeferredItem<InfxCarrotOnAStickItem>> registerCarrotOnASticks() {
        Map<InfxMaterial, DeferredItem<InfxCarrotOnAStickItem>> sticks = new LinkedHashMap<>();
        for (InfxMaterial material : FISHING_HOOK_MATERIALS) {
            sticks.put(
                    material,
                    ITEMS.registerItem(
                            material.path() + "_carrot_on_a_stick",
                            properties -> new InfxCarrotOnAStickItem(material, properties),
                            properties -> properties.stacksTo(1).durability(EquipmentKey.fishingRodDurability(material))));
        }
        return Map.copyOf(sticks);
    }

    private static Map<InfxMaterial, DeferredItem<InfxWarpedFungusOnAStickItem>> registerWarpedFungusOnASticks() {
        Map<InfxMaterial, DeferredItem<InfxWarpedFungusOnAStickItem>> sticks = new LinkedHashMap<>();
        for (InfxMaterial material : FISHING_HOOK_MATERIALS) {
            sticks.put(
                    material,
                    ITEMS.registerItem(
                            material.path() + "_warped_fungus_on_a_stick",
                            properties -> new InfxWarpedFungusOnAStickItem(material, properties),
                            properties -> properties.stacksTo(1).durability(EquipmentKey.fishingRodDurability(material))));
        }
        return Map.copyOf(sticks);
    }

    public static DeferredItem<InfxBucketItem> bucket(InfxMaterial material, InfxBucketItem.Contents contents) {
        EnumMap<InfxBucketItem.Contents, DeferredItem<InfxBucketItem>> variants = BUCKETS_BY_MATERIAL.get(material);
        if (variants == null || !variants.containsKey(contents)) {
            throw new IllegalArgumentException("No bucket registered for " + material + " / " + contents);
        }
        return variants.get(contents);
    }

    public static DeferredItem<InfxMobBucketItem> mobBucket(InfxMaterial material, MobBucketKind kind) {
        EnumMap<MobBucketKind, DeferredItem<InfxMobBucketItem>> variants = MOB_BUCKETS_BY_MATERIAL.get(material);
        if (variants == null || !variants.containsKey(kind)) {
            throw new IllegalArgumentException("No mob bucket registered for " + material + " / " + kind);
        }
        return variants.get(kind);
    }

    public static DeferredItem<InfxSolidBucketItem> powderSnowBucket(InfxMaterial material) {
        DeferredItem<InfxSolidBucketItem> bucket = POWDER_SNOW_BUCKETS_BY_MATERIAL.get(material);
        if (bucket == null) {
            throw new IllegalArgumentException("No powder snow bucket registered for " + material);
        }
        return bucket;
    }

    public static Catalog catalog() {
        return CATALOG;
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
