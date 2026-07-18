package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.block.AdamantiumWorkbenchBlock;
import com.pixulse.infx.block.AncientMetalWorkbenchBlock;
import com.pixulse.infx.block.CopperWorkbenchBlock;
import com.pixulse.infx.block.ClayFurnaceBlock;
import com.pixulse.infx.block.FlintWorkbenchBlock;
import com.pixulse.infx.block.GoldWorkbenchBlock;
import com.pixulse.infx.block.IronWorkbenchBlock;
import com.pixulse.infx.block.MithrilWorkbenchBlock;
import com.pixulse.infx.block.ObsidianWorkbenchBlock;
import com.pixulse.infx.block.R196FurnaceBlock;
import com.pixulse.infx.block.SandstoneFurnaceBlock;
import com.pixulse.infx.block.SilverWorkbenchBlock;
import com.pixulse.infx.block.TieredWorkbenchBlock;
import com.pixulse.infx.crafting.BenchTier;
import java.util.List;

import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(InfiniteX.MOD_ID);

    public static final DeferredBlock<ClayFurnaceBlock> CLAY_FURNACE = BLOCKS.registerBlock(
            "clay_furnace",
            ClayFurnaceBlock::new,
            properties -> properties
                    .mapColor(MapColor.CLAY)
                    .strength(0.5F)
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

    public static final List<DeferredBlock<? extends R196FurnaceBlock>> FURNACES =
            List.of(CLAY_FURNACE, SANDSTONE_FURNACE);

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
