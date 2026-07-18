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
