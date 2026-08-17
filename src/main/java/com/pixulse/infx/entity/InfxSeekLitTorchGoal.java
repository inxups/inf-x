package com.pixulse.infx.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import org.jspecify.annotations.Nullable;

/** MITE's dedicated, path-validated torch hunt for shadows and invisible stalkers. */
final class InfxSeekLitTorchGoal extends Goal {
    static final int MAX_CANDIDATES = 8;
    static final int HORIZONTAL_SEARCH_RANGE = 16;
    static final int VERTICAL_SEARCH_RANGE = 4;

    private final Mob mob;
    private final int searchInterval;
    private final double speed;
    private int nextSearchTick;
    private @Nullable Path path;

    InfxSeekLitTorchGoal(Mob mob, int searchInterval, double speed) {
        this.mob = mob;
        this.searchInterval = searchInterval;
        this.speed = speed;
        this.nextSearchTick = searchInterval;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!isSearchDue(mob.tickCount, nextSearchTick) || !canCreateGroundPath()) {
            return false;
        }
        nextSearchTick = mob.tickCount + searchInterval;
        path = findPathToLitTorch();
        return path != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (!mob.getNavigation().isDone()) {
            return true;
        }
        if (mob instanceof InfxZombieBase zombie && mob.level() instanceof ServerLevel level) {
            zombie.disableNearbyLight(level);
        }
        return false;
    }

    @Override
    public void start() {
        if (path != null) {
            mob.getNavigation().moveTo(path, speed);
        }
    }

    @Override
    public void stop() {
        path = null;
        mob.getNavigation().stop();
    }

    static boolean isSearchDue(int tickCount, int nextSearchTick) {
        return tickCount >= nextSearchTick;
    }

    static boolean canCreateGroundPath(boolean onGround, boolean inLiquid) {
        return onGround || inLiquid;
    }

    private boolean canCreateGroundPath() {
        return canCreateGroundPath(mob.onGround(), mob.isInLiquid());
    }

    static boolean isLitTorch(BlockState state) {
        return state.is(Blocks.TORCH)
                || state.is(Blocks.WALL_TORCH)
                || state.is(Blocks.REDSTONE_TORCH)
                || state.is(Blocks.REDSTONE_WALL_TORCH)
                || state.is(Blocks.JACK_O_LANTERN);
    }

    private @Nullable Path findPathToLitTorch() {
        Level level = mob.level();
        BlockPos origin = mob.blockPosition();
        List<BlockPos> candidates = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-HORIZONTAL_SEARCH_RANGE, -VERTICAL_SEARCH_RANGE, -HORIZONTAL_SEARCH_RANGE),
                origin.offset(HORIZONTAL_SEARCH_RANGE, VERTICAL_SEARCH_RANGE, HORIZONTAL_SEARCH_RANGE))) {
            if (isLitTorch(level.getBlockState(pos))) {
                candidates.add(pos.immutable());
            }
        }
        candidates.sort(Comparator.comparingDouble(pos -> pos.distSqr(origin)));
        int limit = Math.min(MAX_CANDIDATES, candidates.size());
        for (int index = 0; index < limit; index++) {
            Path candidatePath = mob.getNavigation().createPath(candidates.get(index), 1);
            Node end = candidatePath == null ? null : candidatePath.getEndNode();
            if (candidatePath != null
                    && candidatePath.canReach()
                    && end != null
                    && isNearLitTorch(level, end.asBlockPos())) {
                return candidatePath;
            }
        }
        return null;
    }

    private boolean isNearLitTorch(Level level, BlockPos pos) {
        int upperOffset = 1 + (int) mob.getBbHeight();
        for (BlockPos candidate : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, upperOffset, 1))) {
            if (isLitTorch(level.getBlockState(candidate))) {
                return true;
            }
        }
        return false;
    }
}
