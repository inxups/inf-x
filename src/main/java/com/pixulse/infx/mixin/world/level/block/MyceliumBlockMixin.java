package com.pixulse.infx.mixin.world.level.block;

import com.pixulse.infx.world.MoonPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.MyceliumBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;

/**
 * MITE-aligned mycelium self-maintenance, replacing the vanilla {@code SpreadingSnowyBlock} spread:
 * water above reverts to dirt; 75% of ticks do nothing; in a dim (≤ 13) covered spot it grows a
 * brown mushroom on top (1/256, capped at 2 nearby mushrooms) or spreads to adjacent dirt/grass/
 * farmland; in bright daylight outdoors it reverts to dirt. Overworld mushroom-island surface
 * mycelium reverting during the day is MITE-expected behavior.
 */
@Mixin(MyceliumBlock.class)
public abstract class MyceliumBlockMixin {
    // Plain override (merged by Mixin over the inherited SpreadingSnowyBlock.randomTick).
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getFluidState(pos.above()).is(FluidTags.WATER)) {
            level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
            return;
        }
        if (random.nextInt(4) > 0) {
            return;
        }
        boolean dark = level.getRawBrightness(pos.above(), 0) <= 13;
        // Mycelium is itself motion-blocking, so the heightmap reports its own top face; the +1
        // offset distinguishes "mycelium under an open column" from "mycelium under cover".
        boolean indoor = level.dimensionType().hasCeiling()
                || level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()) > pos.getY() + 1;
        if (dark && indoor) {
            if (random.nextInt(256) == 0 && level.isEmptyBlock(pos.above())) {
                int mushrooms = 0;
                for (BlockPos p : BlockPos.betweenClosed(pos.offset(-4, -2, -4), pos.offset(4, 2, 4))) {
                    if (level.getBlockState(p).getBlock() instanceof MushroomBlock && ++mushrooms > 2) {
                        return;
                    }
                }
                level.setBlockAndUpdate(pos.above(), Blocks.BROWN_MUSHROOM.defaultBlockState());
                return;
            }
            for (int i = 0; i < 8; i++) {
                int dx = random.nextInt(3) - 1;
                int dy = random.nextInt(5) - 2;
                int dz = random.nextInt(3) - 1;
                // MITE only tries the straight neighbours (±x/±z on the same level) plus the
                // vertical ±2 offsets, skipping the pure-diagonal cells.
                if (dy >= -1 && dy <= 1 && dx != 0 && dz != 0) {
                    continue;
                }
                BlockPos target = pos.offset(dx, dy, dz);
                BlockState targetState = level.getBlockState(target);
                if ((targetState.is(Blocks.DIRT) || targetState.is(Blocks.GRASS_BLOCK) || targetState.is(Blocks.FARMLAND))
                        && level.getRawBrightness(target.above(), 0) <= 13
                        && level.getFluidState(target.above()).isEmpty()
                        && !level.getBlockState(target.above()).isFaceSturdy(level, target.above(), Direction.DOWN)) {
                    level.setBlockAndUpdate(target, Blocks.MYCELIUM.defaultBlockState());
                    return;
                }
            }
        } else if (!MoonPhase.isNight(level) && !level.isRainingAt(pos.above())) {
            level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
        }
    }
}
