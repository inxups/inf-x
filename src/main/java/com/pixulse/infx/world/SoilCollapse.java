package com.pixulse.infx.world;

import com.pixulse.infx.config.InfXConfig;
import com.pixulse.infx.mixin.world.entity.item.FallingBlockEntityAccessor;
import com.pixulse.infx.registry.tag.InfXBlockTags;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

/** Delayed collapse and slope sliding for blocks selected by the soil tags. */
public final class SoilCollapse {
    private static final Direction[] HORIZONTAL = {
        Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };
    private static final Map<ServerLevel, Set<Long>> ARMED_SLOPE_DELAYS = new WeakHashMap<>();

    private SoilCollapse() {}

    public static void schedule(ServerLevel level, BlockPos pos) {
        if (!needsSlopeDelay(level, pos)) disarmSlopeDelay(level, pos);
        schedule(level, pos, InfXConfig.INSTANCE.world.soilCollapseDelayTicks.getValue());
    }

    public static int collapseDelay(Level level, BlockPos pos) {
        int delay = InfXConfig.INSTANCE.world.soilCollapseDelayTicks.getValue();
        return needsSlopeDelay(level, pos)
                ? delay + InfXConfig.INSTANCE.world.soilSlopeCollapseExtraDelayTicks.getValue()
                : delay;
    }

    public static void schedule(ServerLevel level, BlockPos pos, int delayTicks) {
        if (!enabled() || !level.hasChunkAt(pos)) return;
        BlockState state = level.getBlockState(pos);
        if (!state.is(InfXBlockTags.GRAVITY_SOILS)) {
            disarmSlopeDelay(level, pos);
            return;
        }
        level.scheduleTick(pos, state.getBlock(), Math.max(0, delayTicks));
    }

    public static void cancelPendingDelay(ServerLevel level, BlockPos pos) {
        disarmSlopeDelay(level, pos);
    }

    public static void disturbAround(ServerLevel level, BlockPos changedPos) {
        schedule(level, changedPos.above());
        for (Direction direction : HORIZONTAL) schedule(level, changedPos.relative(direction));
    }

    public static void onScheduledTick(ServerLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).is(InfXBlockTags.GRAVITY_SOILS)) {
            disarmSlopeDelay(level, pos);
            return;
        }
        if (needsSlopeDelay(level, pos)) {
            int extraDelay = InfXConfig.INSTANCE.world.soilSlopeCollapseExtraDelayTicks.getValue();
            if (extraDelay > 0 && armSlopeDelay(level, pos)) {
                schedule(level, pos, extraDelay);
                return;
            }
        } else {
            disarmSlopeDelay(level, pos);
        }
        disarmSlopeDelay(level, pos);
        collapse(level, pos);
    }

    public static boolean collapse(ServerLevel level, BlockPos pos) {
        if (!enabled() || !level.hasChunkAt(pos)) return false;
        BlockState state = level.getBlockState(pos);
        if (!state.is(InfXBlockTags.GRAVITY_SOILS) || pos.getY() <= level.getMinY()) return false;

        boolean fallsStraightDown = canFallThrough(level, pos.below());
        if (!fallsStraightDown && (level.getBlockState(pos.below()).is(Blocks.SCAFFOLDING)
                || !canFallThrough(level, pos.above())
                || slideDirection(level, pos) == null
                || level.getRandom().nextInt(3) != 0)) {
            return false;
        }

        spawn(level, pos, state);
        disarmSlopeDelay(level, pos);
        disturbAround(level, pos);
        return true;
    }

    private static boolean needsSlopeDelay(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(InfXBlockTags.GRAVITY_SOILS)
                && !canFallThrough(level, pos.below())
                && !level.getBlockState(pos.below()).is(Blocks.SCAFFOLDING)
                && canFallThrough(level, pos.above())
                && slideDirection(level, pos) != null;
    }

    private static boolean armSlopeDelay(ServerLevel level, BlockPos pos) {
        return ARMED_SLOPE_DELAYS.computeIfAbsent(level, ignored -> new HashSet<>()).add(pos.asLong());
    }

    private static void disarmSlopeDelay(ServerLevel level, BlockPos pos) {
        Set<Long> armed = ARMED_SLOPE_DELAYS.get(level);
        if (armed == null) return;
        armed.remove(pos.asLong());
        if (armed.isEmpty()) ARMED_SLOPE_DELAYS.remove(level);
    }

    @Nullable
    public static Direction slideDirection(Level level, BlockPos pos) {
        List<Direction> available = availableSlideDirections(level, pos);
        return available.isEmpty() ? null : available.get(Math.floorMod(Long.hashCode(pos.asLong()), available.size()));
    }

    @Nullable
    public static BlockPos reserveSlideTarget(Level level, BlockPos pos, FallingBlockEntity entity) {
        List<Direction> available = availableSlideDirections(level, pos);
        if (available.isEmpty()) return null;
        int start = Math.floorMod(Long.hashCode(pos.asLong()), available.size());
        for (int offset = 0; offset < available.size(); offset++) {
            Direction direction = available.get((start + offset) % available.size());
            BlockPos target = pos.relative(direction);
            if (isSlideTargetAllocated(level, target, entity)) continue;
            ((FallingBlockTargetAccess) entity).setAllocatedTarget(target);
            return target;
        }
        return null;
    }

    public static void releaseSlideTarget(FallingBlockEntity entity) {
        ((FallingBlockTargetAccess) entity).setAllocatedTarget(null);
    }

    public static boolean canFallThrough(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return FallingBlock.isFree(state)
                || !level.getFluidState(pos).isEmpty() && state.getCollisionShape(level, pos).isEmpty();
    }

    public static void settle(FallingBlockEntity entity) {
        if (!(entity.level() instanceof ServerLevel level)) return;
        releaseSlideTarget(entity);
        BlockState state = entity.getBlockState();
        BlockPos pos = findPlacementPos(level, entity.blockPosition(), state);
        if (pos != null) {
            if (state.hasProperty(BlockStateProperties.WATERLOGGED)
                    && level.getFluidState(pos).getType() == Fluids.WATER) {
                state = state.setValue(BlockStateProperties.WATERLOGGED, true);
            }
            if (level.setBlock(pos, state, Block.UPDATE_ALL)) {
                entity.discard();
                disturbAround(level, pos);
                return;
            }
        }
        drop(entity);
    }

    public static void drop(FallingBlockEntity entity) {
        releaseSlideTarget(entity);
        if (entity.isRemoved()) return;
        Block block = entity.getBlockState().getBlock();
        if (entity.level() instanceof ServerLevel level
                && entity.dropItem
                && level.getGameRules().get(GameRules.ENTITY_DROPS)) {
            entity.callOnBrokenAfterFall(block, entity.blockPosition());
            entity.spawnAtLocation(level, block);
        }
        entity.discard();
    }

    public static boolean usesLandslideBehavior(BlockState state) {
        return state.is(InfXBlockTags.SLIDING_FALLING_BLOCKS);
    }

    private static boolean isSlideTargetAllocated(Level level, BlockPos target, FallingBlockEntity owner) {
        AABB searchArea = new AABB(
                target.getX() - 2.0D, level.getMinY(), target.getZ() - 2.0D,
                target.getX() + 3.0D, level.getMaxY(), target.getZ() + 3.0D);
        return level.getEntitiesOfClass(FallingBlockEntity.class, searchArea,
                        entity -> entity != owner && !entity.isRemoved())
                .stream()
                .anyMatch(entity -> entity instanceof FallingBlockTargetAccess access
                        && target.equals(access.getAllocatedTarget()));
    }

    private static List<Direction> availableSlideDirections(Level level, BlockPos pos) {
        if (level.getBlockState(pos.below()).is(Blocks.SCAFFOLDING)) return List.of();
        List<Direction> available = new ArrayList<>(HORIZONTAL.length);
        for (Direction direction : HORIZONTAL) {
            BlockPos side = pos.relative(direction);
            if (canFallThrough(level, side) && canFallThrough(level, side.below())) available.add(direction);
        }
        return available;
    }

    @Nullable
    private static BlockPos findPlacementPos(ServerLevel level, BlockPos landingPos, BlockState fallingState) {
        BlockPos pos = landingPos;
        while (pos.getY() < level.getMaxY()) {
            BlockState replaced = level.getBlockState(pos);
            boolean replaceable = replaced.canBeReplaced(
                    new DirectionalPlaceContext(level, pos, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
            if (replaceable && !canFallThrough(level, pos.below()) && fallingState.canSurvive(level, pos)) {
                return pos;
            }
            if (!replaced.is(InfXBlockTags.GRAVITY_SOILS)) return null;
            pos = pos.above();
        }
        return null;
    }

    private static void spawn(ServerLevel level, BlockPos pos, BlockState state) {
        BlockState fallingState = state.hasProperty(BlockStateProperties.WATERLOGGED)
                ? state.setValue(BlockStateProperties.WATERLOGGED, false)
                : state;
        FallingBlockEntity entity = new FallingBlockEntity(EntityType.FALLING_BLOCK, level);
        ((FallingBlockEntityAccessor) entity).setBlockState(fallingState);
        double x = pos.getX() + 0.5D;
        double y = pos.getY();
        double z = pos.getZ() + 0.5D;
        entity.blocksBuilding = true;
        entity.setPos(x, y, z);
        entity.setDeltaMovement(0.0D, 0.0D, 0.0D);
        entity.xo = entity.xOld = x;
        entity.yo = entity.yOld = y;
        entity.zo = entity.zOld = z;
        entity.setStartPos(pos);
        level.setBlock(pos, state.getFluidState().createLegacyBlock(), Block.UPDATE_ALL);
        level.addFreshEntity(entity);
    }

    private static boolean enabled() {
        return InfXConfig.INSTANCE.world.enabled.getValue()
                && InfXConfig.INSTANCE.world.soilCollapse.getValue();
    }
}
