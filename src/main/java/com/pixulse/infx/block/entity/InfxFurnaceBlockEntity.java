package com.pixulse.infx.block.entity;

import com.pixulse.infx.registry.InfXBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

public final class InfxFurnaceBlockEntity extends AbstractFurnaceBlockEntity {
    public InfxFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(InfXBlockEntityTypes.FURNACE.get(), pos, state, RecipeType.SMELTING);
    }

    @Override
    protected @NonNull Component getDefaultName() {
        String path = BuiltInRegistries.BLOCK.getKey(getBlockState().getBlock()).getPath();
        return Component.translatable("container.infx." + path);
    }

    @Override
    protected @NonNull AbstractContainerMenu createMenu(int containerId, @NonNull Inventory inventory) {
        return new FurnaceMenu(containerId, inventory, this, dataAccess);
    }
}
