package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.crafting.BenchTier;
import com.pixulse.infx.item.R196Catalog;
import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.item.R196ToolItem;
import com.pixulse.infx.material.R196Material;
import java.util.List;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.food.FoodProperties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(InfiniteX.MOD_ID);

    public static final DeferredItem<BlockItem> SILVER_ORE =
            ITEMS.registerSimpleBlockItem(ModBlocks.SILVER_ORE);
    public static final DeferredItem<BlockItem> MITHRIL_ORE =
            ITEMS.registerSimpleBlockItem(ModBlocks.MITHRIL_ORE);
    public static final DeferredItem<BlockItem> ADAMANTIUM_ORE =
            ITEMS.registerSimpleBlockItem(ModBlocks.ADAMANTIUM_ORE, properties -> properties.fireResistant());

    public static final List<DeferredItem<BlockItem>> ORES = List.of(SILVER_ORE, MITHRIL_ORE, ADAMANTIUM_ORE);

    public static final DeferredItem<BlockItem> SILVER_BLOCK = ITEMS.registerSimpleBlockItem(ModBlocks.SILVER_BLOCK);
    public static final DeferredItem<BlockItem> ANCIENT_METAL_BLOCK =
            ITEMS.registerSimpleBlockItem(ModBlocks.ANCIENT_METAL_BLOCK);
    public static final DeferredItem<BlockItem> MITHRIL_BLOCK = ITEMS.registerSimpleBlockItem(ModBlocks.MITHRIL_BLOCK);
    public static final DeferredItem<BlockItem> ADAMANTIUM_BLOCK =
            ITEMS.registerSimpleBlockItem(ModBlocks.ADAMANTIUM_BLOCK, properties -> properties.fireResistant());
    public static final List<DeferredItem<BlockItem>> METAL_STORAGE_BLOCKS =
            List.of(SILVER_BLOCK, ANCIENT_METAL_BLOCK, MITHRIL_BLOCK, ADAMANTIUM_BLOCK);

    public static final DeferredItem<BlockItem> MANTLE = ITEMS.registerSimpleBlockItem(ModBlocks.MANTLE);
    public static final DeferredItem<BlockItem> MITHRIL_RUNE_STONE =
            ITEMS.registerSimpleBlockItem(ModBlocks.MITHRIL_RUNE_STONE);
    public static final DeferredItem<BlockItem> ADAMANTIUM_RUNE_STONE =
            ITEMS.registerSimpleBlockItem(ModBlocks.ADAMANTIUM_RUNE_STONE, properties -> properties.fireResistant());
    public static final List<DeferredItem<BlockItem>> WORLD_BLOCKS =
            List.of(MANTLE, MITHRIL_RUNE_STONE, ADAMANTIUM_RUNE_STONE);
    public static final DeferredItem<BlockItem> EMERALD_ENCHANTING_TABLE =
            ITEMS.registerSimpleBlockItem(ModBlocks.EMERALD_ENCHANTING_TABLE);
    public static final DeferredItem<BlockItem> DIAMOND_ENCHANTING_TABLE =
            ITEMS.registerSimpleBlockItem(ModBlocks.DIAMOND_ENCHANTING_TABLE);
    public static final List<DeferredItem<BlockItem>> ENCHANTING_TABLES =
            List.of(EMERALD_ENCHANTING_TABLE, DIAMOND_ENCHANTING_TABLE);
    public static final List<DeferredItem<BlockItem>> METAL_SAFES = ModBlocks.METAL_SAFES.stream()
            .map(ITEMS::registerSimpleBlockItem)
            .toList();

    public static final List<DeferredItem<BlockItem>> METAL_ANVILS = ModBlocks.METAL_ANVILS.stream()
            .map(anvil -> ITEMS.registerItem(
                    anvil.getId().getPath(),
                    properties -> new BlockItem(anvil.get(), properties),
                    properties -> {
                        Item.Properties configured = properties.durability(anvil.get().maximumDamage());
                        return anvil.get().material().has(R196Material.Flag.LAVA_SAFE)
                                ? configured.fireResistant()
                                : configured;
                    }))
            .toList();

    public static final DeferredItem<BlockItem> FLINT_WORKBENCH =
            ITEMS.registerSimpleBlockItem(ModBlocks.FLINT_WORKBENCH);
    public static final DeferredItem<BlockItem> COPPER_WORKBENCH =
            ITEMS.registerSimpleBlockItem(ModBlocks.COPPER_WORKBENCH);
    public static final DeferredItem<BlockItem> SILVER_WORKBENCH =
            ITEMS.registerSimpleBlockItem(ModBlocks.SILVER_WORKBENCH);
    public static final DeferredItem<BlockItem> GOLD_WORKBENCH =
            ITEMS.registerSimpleBlockItem(ModBlocks.GOLD_WORKBENCH);
    public static final DeferredItem<BlockItem> IRON_WORKBENCH =
            ITEMS.registerSimpleBlockItem(ModBlocks.IRON_WORKBENCH);
    public static final DeferredItem<BlockItem> ANCIENT_METAL_WORKBENCH =
            ITEMS.registerSimpleBlockItem(ModBlocks.ANCIENT_METAL_WORKBENCH);
    public static final DeferredItem<BlockItem> MITHRIL_WORKBENCH =
            ITEMS.registerSimpleBlockItem(ModBlocks.MITHRIL_WORKBENCH);
    public static final DeferredItem<BlockItem> ADAMANTIUM_WORKBENCH =
            ITEMS.registerSimpleBlockItem(ModBlocks.ADAMANTIUM_WORKBENCH);
    public static final DeferredItem<BlockItem> OBSIDIAN_WORKBENCH =
            ITEMS.registerSimpleBlockItem(ModBlocks.OBSIDIAN_WORKBENCH);
    public static final DeferredItem<BlockItem> CLAY_FURNACE =
            ITEMS.registerSimpleBlockItem(ModBlocks.CLAY_FURNACE, properties -> properties.stacksTo(1));
    public static final DeferredItem<BlockItem> LARGE_CLAY_OVEN =
            ITEMS.registerSimpleBlockItem(ModBlocks.LARGE_CLAY_OVEN, properties -> properties.stacksTo(1));
    public static final DeferredItem<BlockItem> SANDSTONE_FURNACE =
            ITEMS.registerSimpleBlockItem(ModBlocks.SANDSTONE_FURNACE, properties -> properties.stacksTo(1));
    public static final DeferredItem<BlockItem> HARDENED_CLAY_FURNACE =
            ITEMS.registerSimpleBlockItem(ModBlocks.HARDENED_CLAY_FURNACE, properties -> properties.stacksTo(1));
    public static final DeferredItem<BlockItem> OBSIDIAN_FURNACE =
            ITEMS.registerSimpleBlockItem(ModBlocks.OBSIDIAN_FURNACE, properties -> properties.stacksTo(1));
    public static final DeferredItem<BlockItem> NETHERRACK_FURNACE =
            ITEMS.registerSimpleBlockItem(ModBlocks.NETHERRACK_FURNACE, properties -> properties.stacksTo(1));

    public static final List<DeferredItem<BlockItem>> WORKBENCHES = List.of(
            FLINT_WORKBENCH,
            COPPER_WORKBENCH,
            SILVER_WORKBENCH,
            GOLD_WORKBENCH,
            IRON_WORKBENCH,
            ANCIENT_METAL_WORKBENCH,
            MITHRIL_WORKBENCH,
            ADAMANTIUM_WORKBENCH,
            OBSIDIAN_WORKBENCH);

    public static final List<DeferredItem<BlockItem>> FURNACES = List.of(
            CLAY_FURNACE,
            LARGE_CLAY_OVEN,
            SANDSTONE_FURNACE,
            HARDENED_CLAY_FURNACE,
            OBSIDIAN_FURNACE,
            NETHERRACK_FURNACE);

    public static final DeferredItem<Item> FLOUR = simple("flour");
    public static final DeferredItem<Item> WATER_BOWL = container("water_bowl");
    public static final DeferredItem<Item> DOUGH = food("dough", 1, 0.1F);
    public static final DeferredItem<Item> SALAD = bowlFood("salad", 5, 0.6F);
    public static final DeferredItem<Item> BLUEBERRIES = food("blueberries", 2, 0.2F);
    public static final DeferredItem<Item> BLUEBERRY_PORRIDGE = bowlFood("blueberry_porridge", 6, 0.7F);
    public static final DeferredItem<Item> MILK_BOWL = bowlFood("milk_bowl", 4, 0.5F);
    public static final DeferredItem<Item> CEREAL_PORRIDGE = bowlFood("cereal_porridge", 6, 0.7F);
    public static final DeferredItem<Item> CHOCOLATE = food("chocolate", 5, 0.6F);
    public static final DeferredItem<Item> PUMPKIN_SOUP = bowlFood("pumpkin_soup", 6, 0.6F);
    public static final DeferredItem<Item> CREAM_OF_MUSHROOM_SOUP = bowlFood("cream_of_mushroom_soup", 7, 0.8F);
    public static final DeferredItem<Item> ONION = food("onion", 2, 0.2F);
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

    public static final List<DeferredItem<Item>> R196_FOODS = List.of(
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

    private static final R196Catalog CATALOG = R196Catalog.register(ITEMS);

    public static final DeferredItem<Item> FLINT_CHIP = CATALOG.raw("flint_chip").holderAs(Item.class);
    public static final DeferredItem<Item> SINEW = CATALOG.raw("sinew").holderAs(Item.class);
    public static final DeferredItem<Item> OBSIDIAN_SHARD = CATALOG.raw("obsidian_shard").holderAs(Item.class);
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
    public static final DeferredItem<R196ToolItem> FLINT_HATCHET =
            CATALOG.equipment(R196Material.FLINT, R196EquipmentType.HATCHET).holderAs(R196ToolItem.class);
    public static final DeferredItem<R196ToolItem> FLINT_SHOVEL =
            CATALOG.equipment(R196Material.FLINT, R196EquipmentType.SHOVEL).holderAs(R196ToolItem.class);
    public static final DeferredItem<R196ToolItem> FLINT_AXE =
            CATALOG.equipment(R196Material.FLINT, R196EquipmentType.AXE).holderAs(R196ToolItem.class);
    public static final DeferredItem<R196ToolItem> COPPER_PICKAXE =
            CATALOG.equipment(R196Material.COPPER, R196EquipmentType.PICKAXE).holderAs(R196ToolItem.class);
    public static final DeferredItem<R196ToolItem> COPPER_SHOVEL =
            CATALOG.equipment(R196Material.COPPER, R196EquipmentType.SHOVEL).holderAs(R196ToolItem.class);
    public static final DeferredItem<R196ToolItem> COPPER_AXE =
            CATALOG.equipment(R196Material.COPPER, R196EquipmentType.AXE).holderAs(R196ToolItem.class);
    public static final DeferredItem<R196ToolItem> COPPER_HOE =
            CATALOG.equipment(R196Material.COPPER, R196EquipmentType.HOE).holderAs(R196ToolItem.class);
    public static final DeferredItem<R196ToolItem> COPPER_SWORD =
            CATALOG.equipment(R196Material.COPPER, R196EquipmentType.SWORD).holderAs(R196ToolItem.class);
    public static final DeferredItem<R196ToolItem> IRON_PICKAXE =
            CATALOG.equipment(R196Material.IRON, R196EquipmentType.PICKAXE).holderAs(R196ToolItem.class);
    public static final DeferredItem<R196ToolItem> IRON_SHOVEL =
            CATALOG.equipment(R196Material.IRON, R196EquipmentType.SHOVEL).holderAs(R196ToolItem.class);
    public static final DeferredItem<R196ToolItem> IRON_AXE =
            CATALOG.equipment(R196Material.IRON, R196EquipmentType.AXE).holderAs(R196ToolItem.class);
    public static final DeferredItem<R196ToolItem> IRON_HOE =
            CATALOG.equipment(R196Material.IRON, R196EquipmentType.HOE).holderAs(R196ToolItem.class);
    public static final DeferredItem<R196ToolItem> IRON_SWORD =
            CATALOG.equipment(R196Material.IRON, R196EquipmentType.SWORD).holderAs(R196ToolItem.class);

    private ModItems() {}

    private static DeferredItem<Item> simple(String path) {
        return ITEMS.registerItem(path, Item::new);
    }

    private static DeferredItem<Item> container(String path) {
        return ITEMS.registerItem(path, Item::new, properties -> properties.stacksTo(8));
    }

    private static DeferredItem<Item> food(String path, int nutrition, float saturation) {
        return ITEMS.registerItem(
                path,
                Item::new,
                properties -> properties.food(new FoodProperties.Builder()
                        .nutrition(nutrition)
                        .saturationModifier(saturation)
                        .build()));
    }

    private static DeferredItem<Item> bowlFood(String path, int nutrition, float saturation) {
        return ITEMS.registerItem(
                path,
                Item::new,
                properties -> properties
                        .stacksTo(8)
                        .usingConvertsTo(Items.BOWL)
                        .food(new FoodProperties.Builder()
                                .nutrition(nutrition)
                                .saturationModifier(saturation)
                                .build()));
    }

    public static R196Catalog catalog() {
        return CATALOG;
    }

    public static DeferredItem<BlockItem> workbench(BenchTier tier) {
        return switch (tier) {
            case FLINT -> FLINT_WORKBENCH;
            case COPPER -> COPPER_WORKBENCH;
            case SILVER -> SILVER_WORKBENCH;
            case GOLD -> GOLD_WORKBENCH;
            case IRON -> IRON_WORKBENCH;
            case ANCIENT_METAL -> ANCIENT_METAL_WORKBENCH;
            case MITHRIL -> MITHRIL_WORKBENCH;
            case ADAMANTIUM -> ADAMANTIUM_WORKBENCH;
            case OBSIDIAN -> OBSIDIAN_WORKBENCH;
            case HAND -> throw new IllegalArgumentException("Hand crafting has no workbench item");
        };
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
