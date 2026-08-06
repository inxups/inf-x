package com.pixulse.infx.item;

import com.pixulse.infx.data.agriculture.AgricultureData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/** MITE manure fertilizes farmland and grows legal small mushrooms into giant mushrooms. */
public final class ManureItem extends Item {
    public ManureItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);

        BlockPos farmlandPos = farmlandTarget(level, clickedPos, clickedState);
        if (farmlandPos != null) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            AgricultureData data = AgricultureData.get((ServerLevel) level);
            boolean fertilized = data.fertilize(farmlandPos, level.getGameTime());
            if (!fertilized) {
                return InteractionResult.PASS;
            }
            consume(context);
            return InteractionResult.SUCCESS_SERVER;
        }

        BlockPos mushroomPos = mushroomTarget(level, clickedPos, clickedState, context.getClickedFace());
        if (mushroomPos == null) {
            return InteractionResult.PASS;
        }
        BlockState mushroomState = level.getBlockState(mushroomPos);
        if (!isLegalMushroomTarget(level, mushroomPos, mushroomState)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        MushroomBlock mushroom = (MushroomBlock) mushroomState.getBlock();
        if (!mushroom.growMushroom((ServerLevel) level, mushroomPos, mushroomState, level.getRandom())) {
            return InteractionResult.PASS;
        }
        consume(context);
        return InteractionResult.SUCCESS_SERVER;
    }

    private static @Nullable BlockPos farmlandTarget(Level level, BlockPos clickedPos, BlockState clickedState) {
        if (clickedState.is(Blocks.FARMLAND)) {
            return clickedPos;
        }
        if (!(clickedState.getBlock() instanceof CropBlock)) {
            return null;
        }
        BlockPos below = clickedPos.below();
        return level.getBlockState(below).is(Blocks.FARMLAND) ? below : null;
    }

    private static @Nullable BlockPos mushroomTarget(
            Level level, BlockPos clickedPos, BlockState clickedState, Direction clickedFace) {
        if (clickedState.getBlock() instanceof MushroomBlock) {
            return clickedPos;
        }
        if (clickedFace != Direction.UP
                || (!clickedState.is(Blocks.GRASS_BLOCK) && !clickedState.is(Blocks.MYCELIUM))) {
            return null;
        }
        BlockPos above = clickedPos.above();
        return level.getBlockState(above).getBlock() instanceof MushroomBlock ? above : null;
    }

    private static boolean isLegalMushroomTarget(Level level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof MushroomBlock)) {
            return false;
        }
        BlockState below = level.getBlockState(pos.below());
        if (state.is(Blocks.RED_MUSHROOM)) {
            return below.is(Blocks.GRASS_BLOCK) && level.canSeeSky(pos);
        }
        return state.is(Blocks.BROWN_MUSHROOM)
                && below.is(Blocks.MYCELIUM)
                && !level.canSeeSky(pos);
    }

    private static void consume(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.hasInfiniteMaterials()) {
            context.getItemInHand().shrink(1);
        }
    }
}
