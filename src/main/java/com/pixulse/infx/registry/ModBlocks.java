package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.block.AdamantiumWorkbenchBlock;
import com.pixulse.infx.block.AncientMetalWorkbenchBlock;
import com.pixulse.infx.block.CopperWorkbenchBlock;
import com.pixulse.infx.block.ClayFurnaceBlock;
import com.pixulse.infx.block.FlintWorkbenchBlock;
import com.pixulse.infx.block.GoldWorkbenchBlock;
import com.pixulse.infx.block.HardenedClayFurnaceBlock;
import com.pixulse.infx.block.IronWorkbenchBlock;
import com.pixulse.infx.block.LargeClayOvenBlock;
import com.pixulse.infx.block.MithrilWorkbenchBlock;
import com.pixulse.infx.block.NetherrackFurnaceBlock;
import com.pixulse.infx.block.ObsidianFurnaceBlock;
import com.pixulse.infx.block.ObsidianWorkbenchBlock;
import com.pixulse.infx.block.R196PortalBlock;
import com.pixulse.infx.block.R196FurnaceBlock;
import com.pixulse.infx.block.SandstoneFurnaceBlock;
import com.pixulse.infx.block.SilverWorkbenchBlock;
import com.pixulse.infx.block.TieredWorkbenchBlock;
import com.pixulse.infx.block.MetalAnvilBlock;
import com.pixulse.infx.block.UnderworldPortalBlock;
import com.pixulse.infx.block.RuneStoneBlock;
import com.pixulse.infx.block.R196EnchantingTableBlock;
import com.pixulse.infx.block.R196SafeBlock;
import com.pixulse.infx.block.WitherwoodBlock;
import com.pixulse.infx.menu.R196EnchantmentMenu;
import com.pixulse.infx.crafting.BenchTier;
import com.pixulse.infx.material.R196Material;
import java.util.List;
import java.util.Map;

import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(InfiniteX.MOD_ID);

    public static final DeferredBlock<Block> SILVER_ORE = BLOCKS.registerSimpleBlock(
            "silver_ore",
            properties -> properties
                    .mapColor(MapColor.METAL)
                    .strength(2.5F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());
    public static final DeferredBlock<Block> MITHRIL_ORE = BLOCKS.registerSimpleBlock(
            "mithril_ore",
            properties -> properties
                    .mapColor(MapColor.DIAMOND)
                    .strength(3.5F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());
    public static final DeferredBlock<Block> ADAMANTIUM_ORE = BLOCKS.registerSimpleBlock(
            "adamantium_ore",
            properties -> properties
                    .mapColor(MapColor.EMERALD)
                    .strength(4.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());
    public static final DeferredBlock<InfestedBlock> INFESTED_NETHERRACK = BLOCKS.registerBlock(
            "infested_netherrack",
            properties -> new InfestedBlock(net.minecraft.world.level.block.Blocks.NETHERRACK, properties),
            properties -> properties.ofFullCopy(net.minecraft.world.level.block.Blocks.NETHERRACK));

    public static final List<DeferredBlock<Block>> ORES = List.of(SILVER_ORE, MITHRIL_ORE, ADAMANTIUM_ORE);

    public static final DeferredBlock<FlowerBlock> ROSE = flower("rose");
    public static final DeferredBlock<FlowerBlock> ORCHID = flower("orchid");
    public static final DeferredBlock<FlowerBlock> ALLIUM = flower("allium");
    public static final DeferredBlock<FlowerBlock> TULIP = flower("tulip");
    public static final DeferredBlock<FlowerBlock> DAHLIA = flower("dahlia");
    public static final DeferredBlock<FlowerBlock> DAISY = flower("daisy");
    public static final List<DeferredBlock<FlowerBlock>> R196_FLOWERS =
            List.of(ROSE, ORCHID, ALLIUM, TULIP, DAHLIA, DAISY);

    public static final DeferredBlock<ColoredFallingBlock> NETHER_GRAVEL = BLOCKS.registerBlock(
            "nether_gravel",
            properties -> new ColoredFallingBlock(new ColorRGBA(0xFF6B5548), properties),
            properties -> properties
                    .mapColor(MapColor.NETHER)
                    .strength(0.6F)
                    .sound(SoundType.GRAVEL));
    public static final DeferredBlock<WitherwoodBlock> WITHERWOOD = BLOCKS.registerBlock(
            "witherwood",
            WitherwoodBlock::new,
            properties -> properties
                    .mapColor(MapColor.COLOR_BLACK)
                    .replaceable()
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(Block.OffsetType.XZ));
    public static final DeferredBlock<Block> CORE = BLOCKS.registerSimpleBlock(
            "core",
            properties -> properties
                    .mapColor(MapColor.FIRE)
                    .strength(-1.0F, 3_600_000.0F)
                    .sound(SoundType.BASALT)
                    .lightLevel(state -> 12));
    public static final DeferredBlock<SlabBlock> SNOW_SLAB = BLOCKS.registerBlock(
            "snow_slab",
            SlabBlock::new,
            properties -> properties
                    .mapColor(MapColor.SNOW)
                    .strength(0.2F)
                    .sound(SoundType.SNOW));
    public static final List<DeferredBlock<? extends Block>> FULLTEXT_BLOCKS =
            List.of(NETHER_GRAVEL, WITHERWOOD, CORE);
    public static final List<DeferredBlock<? extends Block>> MITE_RECIPE_BLOCKS = List.of(SNOW_SLAB);

    public static final DeferredBlock<UnderworldPortalBlock> UNDERWORLD_PORTAL = BLOCKS.registerBlock(
            "underworld_portal",
            UnderworldPortalBlock::new,
            properties -> properties.ofFullCopy(net.minecraft.world.level.block.Blocks.NETHER_PORTAL));
    public static final DeferredBlock<R196PortalBlock> NETHER_PORTAL = BLOCKS.registerBlock(
            "nether_portal",
            properties -> new R196PortalBlock(R196PortalBlock.PortalType.NETHER, properties),
            properties -> properties.ofFullCopy(net.minecraft.world.level.block.Blocks.NETHER_PORTAL));
    public static final DeferredBlock<R196PortalBlock> RETURN_SPAWN_PORTAL = BLOCKS.registerBlock(
            "return_spawn_portal",
            properties -> new R196PortalBlock(R196PortalBlock.PortalType.RETURN_SPAWN, properties),
            properties -> properties.ofFullCopy(net.minecraft.world.level.block.Blocks.NETHER_PORTAL));

    public static final DeferredBlock<Block> MANTLE = BLOCKS.registerSimpleBlock(
            "mantle",
            properties -> properties
                    .mapColor(MapColor.FIRE)
                    .strength(-1.0F, 3_600_000.0F)
                    .sound(SoundType.BASALT)
                    .lightLevel(state -> 12));
    public static final DeferredBlock<RuneStoneBlock> MITHRIL_RUNE_STONE = BLOCKS.registerBlock(
            "mithril_rune_stone",
            RuneStoneBlock::new,
            properties -> properties
                    .mapColor(MapColor.DIAMOND)
                    .strength(8.0F, 1_200.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops());
    public static final DeferredBlock<RuneStoneBlock> ADAMANTIUM_RUNE_STONE = BLOCKS.registerBlock(
            "adamantium_rune_stone",
            RuneStoneBlock::new,
            properties -> properties
                    .mapColor(MapColor.EMERALD)
                    .strength(12.0F, 3_600.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops());
    public static final List<DeferredBlock<? extends Block>> WORLD_BLOCKS =
            List.of(MANTLE, MITHRIL_RUNE_STONE, ADAMANTIUM_RUNE_STONE);

    public static final DeferredBlock<R196EnchantingTableBlock> EMERALD_ENCHANTING_TABLE = BLOCKS.registerBlock(
            "emerald_enchanting_table",
            properties -> new R196EnchantingTableBlock(R196EnchantmentMenu.Kind.EMERALD, properties),
            properties -> properties.ofFullCopy(net.minecraft.world.level.block.Blocks.ENCHANTING_TABLE));
    public static final DeferredBlock<R196EnchantingTableBlock> DIAMOND_ENCHANTING_TABLE = BLOCKS.registerBlock(
            "diamond_enchanting_table",
            properties -> new R196EnchantingTableBlock(R196EnchantmentMenu.Kind.DIAMOND, properties),
            properties -> properties.ofFullCopy(net.minecraft.world.level.block.Blocks.ENCHANTING_TABLE));
    public static final List<DeferredBlock<R196EnchantingTableBlock>> ENCHANTING_TABLES =
            List.of(EMERALD_ENCHANTING_TABLE, DIAMOND_ENCHANTING_TABLE);

    public static final DeferredBlock<R196SafeBlock> COPPER_SAFE = metalSafe(R196Material.COPPER, MapColor.COLOR_ORANGE, 4.0F);
    public static final DeferredBlock<R196SafeBlock> SILVER_SAFE = metalSafe(R196Material.SILVER, MapColor.METAL, 5.0F);
    public static final DeferredBlock<R196SafeBlock> GOLD_SAFE = metalSafe(R196Material.GOLD, MapColor.GOLD, 5.0F);
    public static final DeferredBlock<R196SafeBlock> IRON_SAFE = metalSafe(R196Material.IRON, MapColor.METAL, 6.0F);
    public static final DeferredBlock<R196SafeBlock> ANCIENT_METAL_SAFE = metalSafe(R196Material.ANCIENT_METAL, MapColor.COLOR_BROWN, 8.0F);
    public static final DeferredBlock<R196SafeBlock> MITHRIL_SAFE = metalSafe(R196Material.MITHRIL, MapColor.DIAMOND, 12.0F);
    public static final DeferredBlock<R196SafeBlock> ADAMANTIUM_SAFE = metalSafe(R196Material.ADAMANTIUM, MapColor.EMERALD, 50.0F);
    public static final List<DeferredBlock<R196SafeBlock>> METAL_SAFES = List.of(
            COPPER_SAFE, SILVER_SAFE, GOLD_SAFE, IRON_SAFE, ANCIENT_METAL_SAFE, MITHRIL_SAFE, ADAMANTIUM_SAFE);

    public static final DeferredBlock<Block> SILVER_BLOCK = metalStorageBlock("silver_block", MapColor.METAL, 4.0F);
    public static final DeferredBlock<Block> ANCIENT_METAL_BLOCK =
            metalStorageBlock("ancient_metal_block", MapColor.COLOR_BROWN, 5.0F);
    public static final DeferredBlock<Block> MITHRIL_BLOCK =
            metalStorageBlock("mithril_block", MapColor.DIAMOND, 6.0F);
    public static final DeferredBlock<Block> ADAMANTIUM_BLOCK =
            metalStorageBlock("adamantium_block", MapColor.EMERALD, 8.0F);
    public static final List<DeferredBlock<Block>> METAL_STORAGE_BLOCKS =
            List.of(SILVER_BLOCK, ANCIENT_METAL_BLOCK, MITHRIL_BLOCK, ADAMANTIUM_BLOCK);

    public static final DeferredBlock<MetalAnvilBlock> COPPER_ANVIL = metalAnvil(R196Material.COPPER, MapColor.COLOR_ORANGE);
    public static final DeferredBlock<MetalAnvilBlock> SILVER_ANVIL = metalAnvil(R196Material.SILVER, MapColor.METAL);
    public static final DeferredBlock<MetalAnvilBlock> GOLD_ANVIL = metalAnvil(R196Material.GOLD, MapColor.GOLD);
    public static final DeferredBlock<MetalAnvilBlock> IRON_ANVIL = metalAnvil(R196Material.IRON, MapColor.METAL);
    public static final DeferredBlock<MetalAnvilBlock> ANCIENT_METAL_ANVIL =
            metalAnvil(R196Material.ANCIENT_METAL, MapColor.COLOR_BROWN);
    public static final DeferredBlock<MetalAnvilBlock> MITHRIL_ANVIL = metalAnvil(R196Material.MITHRIL, MapColor.DIAMOND);
    public static final DeferredBlock<MetalAnvilBlock> ADAMANTIUM_ANVIL =
            metalAnvil(R196Material.ADAMANTIUM, MapColor.EMERALD);
    public static final List<DeferredBlock<MetalAnvilBlock>> METAL_ANVILS = List.of(
            COPPER_ANVIL,
            SILVER_ANVIL,
            GOLD_ANVIL,
            IRON_ANVIL,
            ANCIENT_METAL_ANVIL,
            MITHRIL_ANVIL,
            ADAMANTIUM_ANVIL);
    private static final Map<R196Material, DeferredBlock<MetalAnvilBlock>> METAL_ANVIL_BY_MATERIAL =
            createMetalAnvilMap();

    public static final DeferredBlock<ClayFurnaceBlock> CLAY_FURNACE = BLOCKS.registerBlock(
            "clay_furnace",
            ClayFurnaceBlock::new,
            properties -> properties
                    .mapColor(MapColor.CLAY)
                    .strength(0.5F)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> state.getValue(AbstractFurnaceBlock.LIT) ? 13 : 0));

    public static final DeferredBlock<LargeClayOvenBlock> LARGE_CLAY_OVEN = BLOCKS.registerBlock(
            "large_clay_oven",
            LargeClayOvenBlock::new,
            properties -> properties
                    .mapColor(MapColor.CLAY)
                    .strength(0.75F)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> state.getValue(AbstractFurnaceBlock.LIT) ? 13 : 0));

    public static final DeferredBlock<SandstoneFurnaceBlock> SANDSTONE_FURNACE = BLOCKS.registerBlock(
            "sandstone_furnace",
            SandstoneFurnaceBlock::new,
            properties -> properties
                    .mapColor(MapColor.SAND)
                    .strength(1.0F)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> state.getValue(AbstractFurnaceBlock.LIT) ? 13 : 0));

    public static final DeferredBlock<HardenedClayFurnaceBlock> HARDENED_CLAY_FURNACE =
            BLOCKS.registerBlock(
                    "hardened_clay_furnace",
                    HardenedClayFurnaceBlock::new,
                    properties -> properties
                            .mapColor(MapColor.TERRACOTTA_ORANGE)
                            .strength(1.0F)
                            .sound(SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .lightLevel(state -> state.getValue(AbstractFurnaceBlock.LIT) ? 13 : 0));

    public static final DeferredBlock<ObsidianFurnaceBlock> OBSIDIAN_FURNACE = BLOCKS.registerBlock(
            "obsidian_furnace",
            ObsidianFurnaceBlock::new,
            properties -> properties
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(4.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> state.getValue(AbstractFurnaceBlock.LIT) ? 13 : 0));

    public static final DeferredBlock<NetherrackFurnaceBlock> NETHERRACK_FURNACE = BLOCKS.registerBlock(
            "netherrack_furnace",
            NetherrackFurnaceBlock::new,
            properties -> properties
                    .mapColor(MapColor.NETHER)
                    .strength(8.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> state.getValue(AbstractFurnaceBlock.LIT) ? 13 : 0));

    public static final List<DeferredBlock<? extends R196FurnaceBlock>> FURNACES =
            List.of(
                    CLAY_FURNACE,
                    LARGE_CLAY_OVEN,
                    SANDSTONE_FURNACE,
                    HARDENED_CLAY_FURNACE,
                    OBSIDIAN_FURNACE,
                    NETHERRACK_FURNACE);

    public static final DeferredBlock<FlintWorkbenchBlock> FLINT_WORKBENCH = BLOCKS.registerBlock(
            "flint_workbench",
            FlintWorkbenchBlock::new,
            properties -> properties.mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD));

    public static final DeferredBlock<CopperWorkbenchBlock> COPPER_WORKBENCH = BLOCKS.registerBlock(
            "copper_workbench",
            CopperWorkbenchBlock::new,
            properties -> properties.mapColor(MapColor.COLOR_ORANGE).strength(3.0F).sound(SoundType.WOOD));

    public static final DeferredBlock<SilverWorkbenchBlock> SILVER_WORKBENCH = BLOCKS.registerBlock(
            "silver_workbench",
            SilverWorkbenchBlock::new,
            properties -> properties.mapColor(MapColor.METAL).strength(3.0F).sound(SoundType.WOOD));

    public static final DeferredBlock<GoldWorkbenchBlock> GOLD_WORKBENCH = BLOCKS.registerBlock(
            "gold_workbench",
            GoldWorkbenchBlock::new,
            properties -> properties.mapColor(MapColor.GOLD).strength(3.0F).sound(SoundType.WOOD));

    public static final DeferredBlock<IronWorkbenchBlock> IRON_WORKBENCH = BLOCKS.registerBlock(
            "iron_workbench",
            IronWorkbenchBlock::new,
            properties -> properties.mapColor(MapColor.METAL).strength(3.0F).sound(SoundType.WOOD));

    public static final DeferredBlock<AncientMetalWorkbenchBlock> ANCIENT_METAL_WORKBENCH = BLOCKS.registerBlock(
            "ancient_metal_workbench",
            AncientMetalWorkbenchBlock::new,
            properties -> properties.mapColor(MapColor.COLOR_BROWN).strength(3.0F).sound(SoundType.WOOD));

    public static final DeferredBlock<MithrilWorkbenchBlock> MITHRIL_WORKBENCH = BLOCKS.registerBlock(
            "mithril_workbench",
            MithrilWorkbenchBlock::new,
            properties -> properties.mapColor(MapColor.DIAMOND).strength(3.0F).sound(SoundType.WOOD));

    public static final DeferredBlock<AdamantiumWorkbenchBlock> ADAMANTIUM_WORKBENCH = BLOCKS.registerBlock(
            "adamantium_workbench",
            AdamantiumWorkbenchBlock::new,
            properties -> properties.mapColor(MapColor.EMERALD).strength(3.0F).sound(SoundType.WOOD));

    public static final DeferredBlock<ObsidianWorkbenchBlock> OBSIDIAN_WORKBENCH = BLOCKS.registerBlock(
            "obsidian_workbench",
            ObsidianWorkbenchBlock::new,
            properties -> properties.mapColor(MapColor.COLOR_BLACK).strength(2.5F).sound(SoundType.WOOD));

    public static final List<DeferredBlock<? extends TieredWorkbenchBlock>> WORKBENCHES = List.of(
            FLINT_WORKBENCH,
            COPPER_WORKBENCH,
            SILVER_WORKBENCH,
            GOLD_WORKBENCH,
            IRON_WORKBENCH,
            ANCIENT_METAL_WORKBENCH,
            MITHRIL_WORKBENCH,
            ADAMANTIUM_WORKBENCH,
            OBSIDIAN_WORKBENCH);

    private ModBlocks() {}

    private static DeferredBlock<FlowerBlock> flower(String name) {
        return BLOCKS.registerBlock(
                name,
                properties -> new FlowerBlock(MobEffects.SATURATION, 0.35F, properties),
                properties -> properties
                        .mapColor(MapColor.PLANT)
                        .replaceable()
                        .noCollision()
                        .instabreak()
                        .sound(SoundType.GRASS)
                        .offsetType(Block.OffsetType.XZ));
    }

    private static DeferredBlock<Block> metalStorageBlock(String name, MapColor color, float strength) {
        return BLOCKS.registerSimpleBlock(
                name,
                properties -> properties
                        .mapColor(color)
                        .strength(strength, 6.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops());
    }

    private static DeferredBlock<R196SafeBlock> metalSafe(R196Material material, MapColor color, float strength) {
        return BLOCKS.registerBlock(
                material.path() + "_safe",
                properties -> new R196SafeBlock(material, properties),
                properties -> properties
                        .mapColor(color)
                        .strength(strength, material == R196Material.ADAMANTIUM ? 3_600.0F : strength * 8.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops());
    }

    private static DeferredBlock<MetalAnvilBlock> metalAnvil(R196Material material, MapColor color) {
        return BLOCKS.registerBlock(
                material.path() + "_anvil",
                properties -> new MetalAnvilBlock(material, properties),
                properties -> properties
                        .mapColor(color)
                        .strength(5.0F, 1_200.0F)
                        .sound(SoundType.ANVIL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion());
    }

    private static Map<R196Material, DeferredBlock<MetalAnvilBlock>> createMetalAnvilMap() {
        return Map.of(
                R196Material.COPPER, COPPER_ANVIL,
                R196Material.SILVER, SILVER_ANVIL,
                R196Material.GOLD, GOLD_ANVIL,
                R196Material.IRON, IRON_ANVIL,
                R196Material.ANCIENT_METAL, ANCIENT_METAL_ANVIL,
                R196Material.MITHRIL, MITHRIL_ANVIL,
                R196Material.ADAMANTIUM, ADAMANTIUM_ANVIL);
    }

    public static DeferredBlock<MetalAnvilBlock> metalAnvil(R196Material material) {
        DeferredBlock<MetalAnvilBlock> anvil = METAL_ANVIL_BY_MATERIAL.get(material);
        if (anvil == null) {
            throw new IllegalArgumentException("No metal anvil for " + material);
        }
        return anvil;
    }

    public static DeferredBlock<? extends TieredWorkbenchBlock> workbench(BenchTier tier) {
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
            case HAND -> throw new IllegalArgumentException("Hand crafting has no workbench block");
        };
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
