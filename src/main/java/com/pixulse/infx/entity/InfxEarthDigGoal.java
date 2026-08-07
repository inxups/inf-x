package com.pixulse.infx.entity;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * InfX's attacking-digger behavior for earth elementals.
 *
 * <p>Unlike an ordinary sight-line blocker, InfX considers the target's support, upper body and
 * lower body while the navigator cannot get into striking range. This lets an elemental dig a
 * route through a ledge instead of idling below it.
 */
final class InfxEarthDigGoal extends Goal {
    private static final int INITIAL_COOLOFF = EarthElemental.INITIAL_DIG_COOLOFF;
    private static final int FOLLOW_UP_PAUSE = 10;

    private final EarthElemental elemental;
    private @Nullable BlockPos pendingPos;

    InfxEarthDigGoal(EarthElemental elemental) {
        this.elemental = elemental;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        pendingPos = null;
        if (!(elemental.level() instanceof ServerLevel level) || !canDigForTarget(level)) {
            return false;
        }
        BlockPos active = elemental.diggingPosition();
        if (active != null) {
            return pauseTicks() > 0 || canDigAt(level, active);
        }
        if (elemental.getRandom().nextInt(20) != 0) {
            return false;
        }
        pendingPos = findDigTarget(level, elemental.getTarget());
        return pendingPos != null;
    }

    @Override
    public void start() {
        if (pendingPos != null && elemental.level() instanceof ServerLevel level
                && elemental.diggingPosition() == null) {
            elemental.beginDigging(level, pendingPos, INITIAL_COOLOFF, 0);
        }
        pendingPos = null;
    }

    @Override
    public boolean canContinueToUse() {
        if (!(elemental.level() instanceof ServerLevel level) || !canDigForTarget(level)) {
            return false;
        }
        BlockPos pos = elemental.diggingPosition();
        LivingEntity target = elemental.getTarget();
        if (pos == null || target == null) {
            return false;
        }
        if (pauseTicks() > 0) {
            return pauseTicks() != 1 || !couldGetCloserByPathing(target);
        }
        if (!canDigAt(level, pos) || canStrikeTarget(target)) {
            return false;
        }
        return elemental.tickCount % 10 != 0 || !couldHitTargetByPathing(target);
    }

    @Override
    public void stop() {
        if (elemental.level() instanceof ServerLevel level) {
            elemental.stopDigging(level);
        }
        pendingPos = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (!(elemental.level() instanceof ServerLevel level) || !canDigForTarget(level)) {
            if (elemental.level() instanceof ServerLevel serverLevel) {
                elemental.stopDigging(serverLevel);
            }
            return;
        }
        BlockPos pos = elemental.diggingPosition();
        LivingEntity target = elemental.getTarget();
        if (pos == null || target == null) {
            return;
        }

        var data = elemental.getPersistentData();
        int pause = pauseTicks();
        if (pause > 0) {
            data.putInt(EarthElemental.DIG_PAUSE, pause - 1);
            return;
        }
        if (!canDigAt(level, pos)) {
            elemental.stopDigging(level);
            return;
        }

        int cooloff = data.getIntOr(EarthElemental.DIG_COOLOFF, INITIAL_COOLOFF);
        if (cooloff == 10) {
            elemental.swing(InteractionHand.MAIN_HAND);
        }
        if (cooloff > 0) {
            data.putInt(EarthElemental.DIG_COOLOFF, cooloff - 1);
            return;
        }

        BlockState state = level.getBlockState(pos);
        int nextCooloff = elemental.blockDigCooloff(state, pos);
        int progress = data.getIntOr(EarthElemental.DIG_PROGRESS, -1) + 1;
        data.putInt(EarthElemental.DIG_COOLOFF, nextCooloff);
        if (progress < 10) {
            data.putInt(EarthElemental.DIG_PROGRESS, progress);
            level.destroyBlockProgress(elemental.getId(), pos, progress);
            return;
        }

        if (!level.destroyBlock(pos, true, elemental)) {
            elemental.stopDigging(level);
            return;
        }
        level.destroyBlockProgress(elemental.getId(), pos, -1);
        continueAfterBreaking(level, pos, nextCooloff);
    }

    private boolean canDigForTarget(ServerLevel level) {
        LivingEntity target = elemental.getTarget();
        return target != null
                && target.isAlive()
                && level.getGameRules().get(GameRules.MOB_GRIEFING)
                && !elemental.blockPosition().equals(target.blockPosition());
    }

    private @Nullable BlockPos findDigTarget(ServerLevel level, @Nullable LivingEntity target) {
        if (target == null) {
            return null;
        }
        double distance = elemental.distanceTo(target);
        if (distance > 16.0) {
            return null;
        }

        int footY = elemental.blockPosition().getY();
        if (distance * distance > 2.0) {
            BlockPos targetPos = target.blockPosition();
            for (int y = targetPos.getY() - 1; y >= footY; y--) {
                BlockPos candidate = new BlockPos(targetPos.getX(), y, targetPos.getZ());
                if (canDigAt(level, candidate)) {
                    return candidate;
                }
            }
        }

        if (distance > maximumDigDistance(level, target)
                || !elemental.getNavigation().isDone()
                || canStrikeTarget(target)) {
            return null;
        }

        Vec3 targetCenter = target.getBoundingBox().getCenter();
        if (isPassable(level, targetHeadBlock(target).above())) {
            BlockPos candidate = findRayCandidate(
                    level, attackPoint(), targetCenter.add(0.0, 1.0, 0.0), target);
            if (candidate != null) {
                return candidate;
            }
        }

        BlockPos candidate = findRayCandidate(level, attackPoint(), targetCenter, target);
        if (candidate != null) {
            return candidate;
        }
        return findLegRayCandidate(level, targetCenter, target);
    }

    private double maximumDigDistance(ServerLevel level, LivingEntity target) {
        Vec3 overhead = elemental.getEyePosition().add(0.0, 1.0, 0.0);
        boolean hasClearOverheadLine = isPassable(level, BlockPos.containing(overhead))
                && hasClearPhysicalLine(level, overhead, target.getEyePosition());
        if (hasClearOverheadLine) {
            return 8.0;
        }
        return elemental.isBloodMoonFrenzied() ? 6.0 : 4.0;
    }

    private @Nullable BlockPos findRayCandidate(
            ServerLevel level, Vec3 origin, Vec3 target, LivingEntity attackTarget) {
        BlockHitResult hit = level.clip(new ClipContext(
                origin, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, elemental));
        if (hit.getType() != HitResult.Type.BLOCK || isRestrictedRayBlock(level, hit.getBlockPos(), attackTarget)) {
            return null;
        }
        return findCandidateAtOrBelow(level, hit.getBlockPos());
    }

    private @Nullable BlockPos findLegRayCandidate(
            ServerLevel level, Vec3 target, LivingEntity attackTarget) {
        Vec3 leg = elemental.position().add(0.0, elemental.getBbHeight() * 0.25, 0.0);
        BlockHitResult hit = level.clip(new ClipContext(
                leg, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, elemental));
        if (hit.getType() != HitResult.Type.BLOCK
                || isRestrictedRayBlock(level, hit.getBlockPos(), attackTarget)
                || !isPassableOrFalling(level, hit.getBlockPos().above())) {
            return null;
        }
        return canDigAt(level, hit.getBlockPos()) ? hit.getBlockPos() : null;
    }

    private @Nullable BlockPos findCandidateAtOrBelow(ServerLevel level, BlockPos hit) {
        int footY = elemental.blockPosition().getY();
        for (int y = hit.getY() + 1; y >= footY; y--) {
            BlockPos candidate = new BlockPos(hit.getX(), y, hit.getZ());
            if (canDigAt(level, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private void continueAfterBreaking(ServerLevel level, BlockPos destroyed, int cooloff) {
        BlockPos next = null;
        BlockPos above = destroyed.above();
        if (isFallingOrUnstable(level, above)) {
            // InfX pauses for a falling block to settle into the just-cleared cell.
            next = destroyed;
        } else if (!level.getBlockState(above).isAir()) {
            if (destroyed.getY() == elemental.blockPosition().getY() && canDigAt(level, above)) {
                next = above;
            } else {
                BlockPos below = destroyed.below();
                if (canDigAt(level, below)) {
                    next = below;
                }
            }
        } else if (destroyed.getY() == elemental.blockPosition().getY() + 1
                && !isPassable(level, elemental.blockPosition().above(2))) {
            BlockPos below = destroyed.below();
            if (canDigAt(level, below)) {
                next = below;
            }
        }

        if (next == null) {
            elemental.stopDigging(level);
        } else {
            elemental.beginDigging(level, next, cooloff, FOLLOW_UP_PAUSE);
        }
    }

    private boolean canDigAt(ServerLevel level, BlockPos pos) {
        Vec3 bodyCenter = elemental.position().add(0.0, elemental.getBbHeight() * 0.5, 0.0);
        return Vec3.atCenterOf(pos).distanceToSqr(bodyCenter) <= 3.25
                && elemental.canDestroyBlock(level, pos)
                && hasPhysicalReach(level, pos);
    }

    private boolean hasPhysicalReach(ServerLevel level, BlockPos pos) {
        Vec3 center = Vec3.atCenterOf(pos);
        return rayReaches(level, attackPoint(), center, pos)
                || rayReaches(level, elemental.position().add(0.0, elemental.getBbHeight() * 0.25, 0.0), center, pos);
    }

    private boolean rayReaches(ServerLevel level, Vec3 origin, Vec3 target, BlockPos expected) {
        BlockHitResult hit = level.clip(new ClipContext(
                origin, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, elemental));
        return hit.getType() != HitResult.Type.BLOCK || hit.getBlockPos().equals(expected);
    }

    private boolean canStrikeTarget(LivingEntity target) {
        return elemental.hasLineOfSight(target) && elemental.isWithinMeleeAttackRange(target);
    }

    private boolean couldGetCloserByPathing(LivingEntity target) {
        Path path = elemental.getNavigation().createPath(target, 16);
        if (path == null || path.getEndNode() == null) {
            return false;
        }
        Vec3 end = Vec3.atBottomCenterOf(path.getEndNode().asBlockPos());
        return end.distanceTo(target.position()) < elemental.distanceTo(target) - 2.0;
    }

    private boolean couldHitTargetByPathing(LivingEntity target) {
        if (!(elemental.level() instanceof ServerLevel level)) {
            return false;
        }
        Path path = elemental.getNavigation().createPath(target, 16);
        if (path == null || path.getEndNode() == null) {
            return false;
        }
        Vec3 end = Vec3.atBottomCenterOf(path.getEndNode().asBlockPos());
        return end.distanceTo(target.position()) <= 1.0
                && hasClearPhysicalLine(level, end, target.getBoundingBox().getCenter());
    }

    private boolean isRestrictedRayBlock(ServerLevel level, BlockPos pos, LivingEntity target) {
        return level.getBlockState(pos).is(BlockTags.FENCES) && !(target instanceof Player);
    }

    private Vec3 attackPoint() {
        return elemental.position().add(0.0, elemental.getBbHeight() * 0.75, 0.0);
    }

    private static BlockPos targetHeadBlock(LivingEntity target) {
        return BlockPos.containing(target.getX(), target.getY() + target.getBbHeight() + 0.0001, target.getZ());
    }

    private boolean hasClearPhysicalLine(ServerLevel level, Vec3 origin, Vec3 target) {
        BlockHitResult hit = level.clip(new ClipContext(
                origin, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, elemental));
        return hit.getType() != HitResult.Type.BLOCK;
    }

    private int pauseTicks() {
        return elemental.getPersistentData().getIntOr(EarthElemental.DIG_PAUSE, 0);
    }

    private static boolean isPassable(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }

    private static boolean isPassableOrFalling(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getCollisionShape(level, pos).isEmpty()
                || state.getBlock() instanceof FallingBlock
                || state.is(Blocks.CACTUS)
                || state.is(Blocks.SNOW);
    }

    private static boolean isFallingOrUnstable(ServerLevel level, BlockPos pos) {
        return !level.getBlockState(pos).isAir() && isPassableOrFalling(level, pos);
    }
}
