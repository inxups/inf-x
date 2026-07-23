package com.pixulse.infx.entity;

import com.pixulse.infx.equipment.R196CorrosionType;
import com.pixulse.infx.tag.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Block contact rules from MITE's pepsin and acid gelatinous cubes. */
public final class R196GelatinousCubeRules {
    public static final int IMMUNE = -1;
    public static final int INSTANT = 0;
    public static final int GRADUAL_TICKS = 400;

    private R196GelatinousCubeRules() {}

    public static int dissolvePeriod(BlockState state, R196CorrosionType type) {
        if (state.isAir()) {
            return IMMUNE;
        }
        if (type == R196CorrosionType.PEPSIN) {
            return state.is(ModTags.Blocks.PEPSIN_DISSOLVABLE) ? GRADUAL_TICKS : IMMUNE;
        }
        if (state.is(ModTags.Blocks.ACID_DISSOLVES_GRADUALLY)) {
            return GRADUAL_TICKS;
        }
        if (state.is(ModTags.Blocks.ACID_DISSOLVES_INSTANTLY)) {
            return INSTANT;
        }
        if (state.is(BlockTags.STONE_BUTTONS)
                || state.is(Blocks.REDSTONE_WIRE)
                || state.is(BlockTags.SNOW)
                || !state.getFluidState().isEmpty()) {
            return IMMUNE;
        }
        return state.isSolid() ? IMMUNE : INSTANT;
    }

    public static int dissolvePeriod(Level level, BlockPos pos, R196CorrosionType type) {
        return dissolvePeriod(level.getBlockState(pos), type);
    }

    public static boolean dissolveOnContact(
            ServerLevel level, BlockPos pos, R196CorrosionType type, Direction contactedFace) {
        BlockState state = level.getBlockState(pos);
        if (type == R196CorrosionType.ACID
                && (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MYCELIUM))
                && (contactedFace == null || contactedFace == Direction.UP)) {
            return level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
        }
        if (dissolvePeriod(state, type) != INSTANT) {
            return false;
        }
        return level.destroyBlock(pos, false);
    }
}
