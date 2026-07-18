package com.pixulse.infx.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/** One of sixteen persistent rune patterns used at the four corners of a rune gate. */
public final class RuneStoneBlock extends Block {
    public static final IntegerProperty RUNE = IntegerProperty.create("rune", 0, 15);

    public RuneStoneBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(RUNE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RUNE);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit) {
        int rune = (state.getValue(RUNE) + 1) & 15;
        if (!level.isClientSide()) {
            level.setBlockAndUpdate(pos, state.setValue(RUNE, rune));
            player.sendOverlayMessage(Component.translatable("message.infx.rune_selected", rune));
        }
        return InteractionResult.SUCCESS;
    }
}
