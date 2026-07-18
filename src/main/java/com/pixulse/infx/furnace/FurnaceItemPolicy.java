package com.pixulse.infx.furnace;

import com.pixulse.infx.registry.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseTorchBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.LilyPadBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class FurnaceItemPolicy {
    private FurnaceItemPolicy() {}

    public static boolean acceptsLargeItems(BlockState state) {
        return !state.is(ModBlocks.CLAY_FURNACE.get());
    }

    public static boolean canPlaceItem(BlockState state, ItemStack stack) {
        return acceptsLargeItems(state) || !isLargeItem(stack);
    }

    public static boolean isLargeItem(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem blockItem) {
            return isLargeBlock(blockItem.getBlock());
        }
        return stack.getItem() instanceof BoatItem;
    }

    static boolean isLargeBlock(Block block) {
        return !(block instanceof BaseTorchBlock)
                && !(block instanceof SaplingBlock)
                && !(block instanceof FlowerBlock)
                && !(block instanceof TallGrassBlock)
                && !(block instanceof MushroomBlock)
                && !(block instanceof ButtonBlock)
                && !(block instanceof LilyPadBlock)
                && !(block instanceof VineBlock);
    }
}
