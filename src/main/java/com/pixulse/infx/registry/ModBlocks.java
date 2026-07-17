package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.block.CopperWorkbenchBlock;
import com.pixulse.infx.block.FlintWorkbenchBlock;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(InfiniteX.MOD_ID);

    public static final DeferredBlock<FlintWorkbenchBlock> FLINT_WORKBENCH = BLOCKS.registerBlock(
            "flint_workbench",
            FlintWorkbenchBlock::new,
            properties -> properties.mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD));

    public static final DeferredBlock<CopperWorkbenchBlock> COPPER_WORKBENCH = BLOCKS.registerBlock(
            "copper_workbench",
            CopperWorkbenchBlock::new,
            properties -> properties.mapColor(MapColor.COLOR_ORANGE).strength(3.0F).sound(SoundType.WOOD));

    private ModBlocks() {}

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
