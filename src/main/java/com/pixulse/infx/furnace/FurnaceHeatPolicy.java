package com.pixulse.infx.furnace;

import com.pixulse.infx.block.R196FurnaceBlock;
import com.pixulse.infx.tag.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class FurnaceHeatPolicy {
    public static final int HEAT_WOOD = 1;
    public static final int HEAT_COAL = 2;
    public static final int HEAT_LAVA = 3;
    public static final int HEAT_BLAZE = 4;

    private FurnaceHeatPolicy() {}

    public static int maximumHeat(BlockState state) {
        if (state.is(Blocks.FURNACE)) {
            return HEAT_COAL;
        }
        if (state.getBlock() instanceof R196FurnaceBlock furnace) {
            return furnace.maximumHeat();
        }
        return 0;
    }

    public static int fuelHeat(ItemStack fuel, int burnTime) {
        if (burnTime <= 0 || fuel.isEmpty()) {
            return 0;
        }
        if (fuel.is(Items.BLAZE_ROD)) {
            return HEAT_BLAZE;
        }
        if (fuel.is(Items.LAVA_BUCKET)) {
            return HEAT_LAVA;
        }
        if (fuel.is(ModTags.Items.FURNACE_FUELS_HEAT_2)) {
            return HEAT_COAL;
        }
        return HEAT_WOOD;
    }

    public static int requiredHeat(ItemStack input) {
        if (input.isEmpty()) {
            return 0;
        }
        return input.is(ModTags.Items.SMELTING_INPUTS_HEAT_2) ? HEAT_COAL : HEAT_WOOD;
    }

    public static boolean isMouthBlocked(BlockGetter level, BlockPos pos, BlockState state) {
        if (maximumHeat(state) == 0 || !state.hasProperty(AbstractFurnaceBlock.FACING)) {
            return false;
        }
        Direction facing = state.getValue(AbstractFurnaceBlock.FACING);
        BlockPos frontPos = pos.relative(facing);
        return level.getBlockState(frontPos).isFaceSturdy(level, frontPos, facing.getOpposite());
    }
}
