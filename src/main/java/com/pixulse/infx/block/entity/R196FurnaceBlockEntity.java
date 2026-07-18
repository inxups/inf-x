package com.pixulse.infx.block.entity;

import com.pixulse.infx.registry.ModBlockEntityTypes;
import com.pixulse.infx.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class R196FurnaceBlockEntity extends AbstractFurnaceBlockEntity {
    public R196FurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.FURNACE.get(), pos, state, RecipeType.SMELTING);
    }

    @Override
    protected Component getDefaultName() {
        String path = getBlockState().is(ModBlocks.CLAY_FURNACE.get())
                ? "clay_furnace"
                : "sandstone_furnace";
        return Component.translatable("container.infx." + path);
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new FurnaceMenu(containerId, inventory, this, dataAccess);
    }
}
