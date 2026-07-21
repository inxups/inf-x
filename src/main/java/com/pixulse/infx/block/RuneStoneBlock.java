package com.pixulse.infx.block;

import com.pixulse.infx.world.UnderworldPortalEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** One of sixteen persistent rune patterns used at the four corners of a rune gate. */
public final class RuneStoneBlock extends Block {
    public static final int RUNE_COUNT = 16;
    public static final IntegerProperty RUNE = IntegerProperty.create("rune", 0, 15);
    private static final String[] MAGIC_NAMES = {
        "Nul", "Quas", "Por", "An", "Nox", "Flam", "Vas", "Des",
        "Ort", "Tym", "Corp", "Lor", "Mani", "Jux", "Ylem", "Sanct"
    };

    public RuneStoneBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(RUNE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RUNE);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level instanceof ServerLevel serverLevel && !state.is(oldState.getBlock())) {
            UnderworldPortalEvents.refreshRuneGateAppearance(serverLevel, pos);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(
            BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        UnderworldPortalEvents.refreshRuneGateAppearance(level, pos);
    }

    public static boolean isRuneStone(ItemStack stack) {
        return stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof RuneStoneBlock;
    }

    public static int rune(ItemStack stack) {
        Integer rune = stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY).get(RUNE);
        return rune == null ? 0 : rune;
    }

    public static int nextRune(int rune) {
        return Math.floorMod(rune + 1, RUNE_COUNT);
    }

    public static ItemStack applyRune(ItemStack stack, int rune) {
        stack.set(DataComponents.BLOCK_STATE, itemState(rune));
        return stack;
    }

    public static BlockItemStateProperties itemState(int rune) {
        return BlockItemStateProperties.EMPTY.with(RUNE, Math.floorMod(rune, RUNE_COUNT));
    }

    public static String magicName(int rune) {
        return MAGIC_NAMES[Math.floorMod(rune, RUNE_COUNT)];
    }
}
