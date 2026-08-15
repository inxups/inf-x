package com.pixulse.infx.mixin.world.level.block;

import com.pixulse.infx.world.InfXMushroomGrowth;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MITE-aligned brown/red mushroom growth. Adds the {@code GROWTH} tier property and replaces the
 * vanilla single-stage random tick with the MITE distribution: 1% spread, brown 2% natural giant,
 * brown 7% farmland-to-mycelium conversion. The nether fungi ({@code FungusBlock}) inherit the
 * property (defaulting to 0) but fall through to their vanilla spread here, so they are untouched.
 */
@Mixin(MushroomBlock.class)
public abstract class MushroomBlockGrowthMixin {
    // Plain override (merged by Mixin over the inherited Block.createBlockStateDefinition). Vanilla
    // mushrooms declare no properties, so only the MITE growth tier needs adding here.
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(InfXMushroomGrowth.GROWTH);
    }

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void infx$miteMushroomRandomTick(
            BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        boolean brown = state.is(Blocks.BROWN_MUSHROOM);
        boolean red = state.is(Blocks.RED_MUSHROOM);
        if (!brown && !red) {
            return;
        }
        int ran = random.nextInt(100);
        if (ran == 0) {
            spread(state, level, pos, random, brown);
        } else if (brown && ran < 3) {
            InfXMushroomGrowth.tryGrowGiantMushroom(level, pos, state, random);
        } else if (brown && ran < 10 && InfXMushroomGrowth.canConvertBlockBelowToMycelium(level, pos)) {
            level.setBlock(pos.below(), Blocks.MYCELIUM.defaultBlockState(), 2);
        }
        ci.cancel();
    }

    /** MITE {@code BlockMushroom.updateTick} spread: 9×3×9 count cap, 4-step walk, new growth 0. */
    private static void spread(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, boolean brown) {
        Block block = state.getBlock();
        int max = 5;
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-4, -1, -4), pos.offset(4, 1, 4))) {
            if (level.getBlockState(p).is(block) && --max <= 0) {
                return;
            }
        }
        BlockPos target = pos.offset(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
        for (int i = 0; i < 4; i++) {
            if (level.isEmptyBlock(target) && state.canSurvive(level, target)) {
                pos = target;
            }
            target = pos.offset(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
        }
        if (level.isEmptyBlock(target) && state.canSurvive(level, target)) {
            // MITE: a brown mushroom landing on mycelium spreads only 1/4 of the time.
            if (brown && level.getBlockState(target.below()).is(Blocks.MYCELIUM) && random.nextInt(4) > 0) {
                return;
            }
            level.setBlock(target, state.setValue(InfXMushroomGrowth.GROWTH, 0), 2);
        }
    }
}
