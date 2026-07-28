package com.pixulse.infx.entity;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

/** R196's material-speed breaking for closed wooden portals, including doors, trapdoors and gates. */
final class MiteEarthBreakDoorGoal extends Goal {
    private final EarthElemental elemental;
    private BlockPos doorPos = BlockPos.ZERO;
    private int breakTime;
    private int lastProgress = -1;
    private int requiredTime;

    MiteEarthBreakDoorGoal(EarthElemental elemental) {
        this.elemental = elemental;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!(elemental.level() instanceof ServerLevel level)
                || level.getDifficulty() != Difficulty.HARD
                || !level.getGameRules().get(GameRules.MOB_GRIEFING)
                || !GoalUtils.hasGroundPathNavigation(elemental)
                || !elemental.horizontalCollision) {
            return false;
        }
        Path path = elemental.getNavigation().getPath();
        if (path != null && !path.isDone()) {
            for (int i = 0; i < Math.min(path.getNextNodeIndex() + 2, path.getNodeCount()); i++) {
                Node node = path.getNode(i);
                BlockPos candidate = new BlockPos(node.x, node.y + 1, node.z);
                if (isNear(candidate) && setPortal(candidate)) {
                    return true;
                }
                candidate = candidate.below();
                if (isNear(candidate) && setPortal(candidate)) {
                    return true;
                }
            }
        }
        BlockPos candidate = elemental.blockPosition().above();
        return (isNear(candidate) && setPortal(candidate))
                || (isNear(candidate.below()) && setPortal(candidate.below()));
    }

    @Override
    public void start() {
        breakTime = 0;
        lastProgress = -1;
        BlockState state = elemental.level().getBlockState(doorPos);
        requiredTime = elemental.doorBreakTicks(state.is(BlockTags.WOODEN_DOORS));
    }

    @Override
    public boolean canContinueToUse() {
        if (!(elemental.level() instanceof ServerLevel level)) {
            return false;
        }
        return breakTime <= requiredTime
                && isClosedBreakablePortal(doorPos)
                && doorPos.closerToCenterThan(elemental.position(), 2.0)
                && level.getGameRules().get(GameRules.MOB_GRIEFING)
                && net.neoforged.neoforge.common.CommonHooks.canEntityDestroy(level, doorPos, elemental);
    }

    @Override
    public void stop() {
        elemental.level().destroyBlockProgress(elemental.getId(), doorPos, -1);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (elemental.getRandom().nextInt(20) == 0) {
            elemental.level().levelEvent(1019, doorPos, 0);
            elemental.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        }
        breakTime++;
        int progress = (int) ((float) breakTime / requiredTime * 10.0F);
        if (progress != lastProgress) {
            elemental.level().destroyBlockProgress(elemental.getId(), doorPos, progress);
            lastProgress = progress;
        }
        if (breakTime == requiredTime && elemental.level() instanceof ServerLevel level) {
            BlockState state = level.getBlockState(doorPos);
            level.removeBlock(doorPos, false);
            level.levelEvent(1021, doorPos, 0);
            level.levelEvent(2001, doorPos, Block.getId(state));
        }
    }

    private boolean isNear(BlockPos candidate) {
        return elemental.distanceToSqr(candidate.getX() + 0.5, candidate.getY(), candidate.getZ() + 0.5) <= 2.25;
    }

    private boolean setPortal(BlockPos candidate) {
        BlockState state = elemental.level().getBlockState(candidate);
        if (state.getBlock() instanceof DoorBlock && state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
            candidate = candidate.below();
            state = elemental.level().getBlockState(candidate);
        }
        if (!isClosedBreakablePortal(state)
                || !(elemental.level() instanceof ServerLevel level)
                || !net.neoforged.neoforge.common.CommonHooks.canEntityDestroy(level, candidate, elemental)) {
            return false;
        }
        doorPos = candidate;
        return true;
    }

    private boolean isClosedBreakablePortal(BlockPos pos) {
        return isClosedBreakablePortal(elemental.level().getBlockState(pos));
    }

    private boolean isClosedBreakablePortal(BlockState state) {
        if (state.getBlock() instanceof DoorBlock) {
            return state.is(BlockTags.WOODEN_DOORS) && !state.getValue(DoorBlock.OPEN);
        }
        if (state.getBlock() instanceof TrapDoorBlock) {
            return state.is(BlockTags.WOODEN_TRAPDOORS) && !state.getValue(TrapDoorBlock.OPEN);
        }
        return state.getBlock() instanceof FenceGateBlock
                && state.is(BlockTags.FENCE_GATES)
                && elemental.getTarget() instanceof net.minecraft.world.entity.player.Player
                && !state.getValue(FenceGateBlock.OPEN);
    }
}
