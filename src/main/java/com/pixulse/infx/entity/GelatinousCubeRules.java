package com.pixulse.infx.entity;

import com.pixulse.infx.equipment.CorrosionType;
import com.pixulse.infx.tag.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Block contact rules from MITE's pepsin and acid gelatinous cubes. */
public final class GelatinousCubeRules {
    public static final int IMMUNE = -1;
    public static final int INSTANT = 0;
    public static final int GRADUAL_TICKS = 400;

    private GelatinousCubeRules() {}

    public static int dissolvePeriod(BlockState state, CorrosionType type) {
        if (state.isAir()) {
            return IMMUNE;
        }
        if (type == CorrosionType.PEPSIN) {
            return state.is(ModTags.Blocks.PEPSIN_DISSOLVABLE) ? GRADUAL_TICKS : IMMUNE;
        }
        // Acid oozes scorch living ground into dirt on contact. Check this before the
        // solid-block fallback so grass does not incorrectly remain immune.
        if (isAcidScorchableGround(state, type)) {
            return INSTANT;
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

    public static int dissolvePeriod(Level level, BlockPos pos, CorrosionType type) {
        return dissolvePeriod(level.getBlockState(pos), type);
    }

    static boolean isAcidScorchableGround(BlockState state, CorrosionType type) {
        return type == CorrosionType.ACID && (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MYCELIUM));
    }

    public static boolean dissolveOnContact(
            ServerLevel level, BlockPos pos, CorrosionType type, Direction contactedFace) {
        BlockState state = level.getBlockState(pos);
        if (isAcidScorchableGround(state, type)
                && (contactedFace == null || contactedFace == Direction.UP)) {
            return level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
        }
        if (dissolvePeriod(state, type) != INSTANT) {
            return false;
        }
        return level.destroyBlock(pos, false);
    }
}
