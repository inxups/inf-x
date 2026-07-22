package com.pixulse.infx.block;

import com.pixulse.infx.world.Underworld;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** A portal surface whose block identity fixes its destination family. */
public class R196PortalBlock extends NetherPortalBlock {
    private static final String RETURN_POS = "infx_underworld_return_pos";
    private static final int MIN_PORTAL_WIDTH = 2;
    private static final int MAX_PORTAL_WIDTH = 21;
    private static final int MIN_PORTAL_HEIGHT = 3;
    private static final int MAX_PORTAL_HEIGHT = 21;

    private final PortalType portalType;

    public R196PortalBlock(PortalType portalType, BlockBehaviour.Properties properties) {
        super(properties);
        this.portalType = portalType;
    }

    public PortalType portalType() {
        return portalType;
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess ticks,
            BlockPos pos,
            Direction direction,
            BlockPos neighbourPos,
            BlockState neighbourState,
            RandomSource random) {
        Direction.Axis axis = state.getValue(AXIS);
        boolean wrongAxis = axis != direction.getAxis() && direction.getAxis().isHorizontal();
        return !wrongAxis
                        && !neighbourState.is(this)
                        && !hasCompletePortalShape(level, pos, axis)
                ? Blocks.AIR.defaultBlockState()
                : state;
    }

    /** Mirrors NetherPortalBlock's structural check for one specific portal block type. */
    private boolean hasCompletePortalShape(LevelReader level, BlockPos pos, Direction.Axis axis) {
        Direction rightDirection = axis == Direction.Axis.X ? Direction.WEST : Direction.SOUTH;
        BlockPos bottomLeft = findBottomLeft(level, pos, rightDirection);
        if (bottomLeft == null) {
            return false;
        }

        int width = portalWidth(level, bottomLeft, rightDirection);
        if (width < MIN_PORTAL_WIDTH || width > MAX_PORTAL_WIDTH) {
            return false;
        }

        PortalInterior interior = portalInterior(level, bottomLeft, rightDirection, width);
        return interior.height() >= MIN_PORTAL_HEIGHT
                && interior.height() <= MAX_PORTAL_HEIGHT
                && hasTopFrame(level, bottomLeft, rightDirection, width, interior.height())
                && interior.portalBlocks() == width * interior.height();
    }

    private @Nullable BlockPos findBottomLeft(LevelReader level, BlockPos pos, Direction rightDirection) {
        int minY = Math.max(level.getMinY(), pos.getY() - MAX_PORTAL_HEIGHT);
        while (pos.getY() > minY && isPortalEmpty(level.getBlockState(pos.below()))) {
            pos = pos.below();
        }

        Direction leftDirection = rightDirection.getOpposite();
        int edge = distanceUntilFrame(level, pos, leftDirection) - 1;
        return edge < 0 ? null : pos.relative(leftDirection, edge);
    }

    private int portalWidth(LevelReader level, BlockPos bottomLeft, Direction rightDirection) {
        int width = distanceUntilFrame(level, bottomLeft, rightDirection);
        return width >= MIN_PORTAL_WIDTH && width <= MAX_PORTAL_WIDTH ? width : 0;
    }

    private int distanceUntilFrame(LevelReader level, BlockPos pos, Direction direction) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int distance = 0; distance <= MAX_PORTAL_WIDTH; distance++) {
            cursor.set(pos).move(direction, distance);
            BlockState state = level.getBlockState(cursor);
            if (!isPortalEmpty(state)) {
                return isPortalFrame(state, level, cursor) ? distance : 0;
            }

            cursor.move(Direction.DOWN);
            if (!isPortalFrame(level.getBlockState(cursor), level, cursor)) {
                return 0;
            }
        }
        return 0;
    }

    private PortalInterior portalInterior(
            LevelReader level, BlockPos bottomLeft, Direction rightDirection, int width) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int portalBlocks = 0;
        for (int height = 0; height < MAX_PORTAL_HEIGHT; height++) {
            cursor.set(bottomLeft).move(Direction.UP, height).move(rightDirection, -1);
            if (!isPortalFrame(level.getBlockState(cursor), level, cursor)) {
                return new PortalInterior(height, portalBlocks);
            }

            cursor.set(bottomLeft).move(Direction.UP, height).move(rightDirection, width);
            if (!isPortalFrame(level.getBlockState(cursor), level, cursor)) {
                return new PortalInterior(height, portalBlocks);
            }

            for (int horizontal = 0; horizontal < width; horizontal++) {
                cursor.set(bottomLeft).move(Direction.UP, height).move(rightDirection, horizontal);
                BlockState state = level.getBlockState(cursor);
                if (!isPortalEmpty(state)) {
                    return new PortalInterior(height, portalBlocks);
                }
                if (isPortalBlock(state)) {
                    portalBlocks++;
                }
            }
        }
        return new PortalInterior(MAX_PORTAL_HEIGHT, portalBlocks);
    }

    private boolean hasTopFrame(
            LevelReader level, BlockPos bottomLeft, Direction rightDirection, int width, int height) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int horizontal = 0; horizontal < width; horizontal++) {
            cursor.set(bottomLeft).move(Direction.UP, height).move(rightDirection, horizontal);
            if (!isPortalFrame(level.getBlockState(cursor), level, cursor)) {
                return false;
            }
        }
        return true;
    }

    private boolean isPortalEmpty(BlockState state) {
        return state.isAir() || state.is(BlockTags.FIRE) || isPortalBlock(state);
    }

    private boolean isPortalBlock(BlockState state) {
        return state.is(this);
    }

    private static boolean isPortalFrame(BlockState state, LevelReader level, BlockPos pos) {
        return state.isPortalFrame(level, pos) || state.getBlock() instanceof RuneStoneBlock;
    }

    @Override
    public @Nullable TeleportTransition getPortalDestination(
            ServerLevel currentLevel, Entity entity, BlockPos portalEntryPos) {
        PortalRoute route = routeFor(portalType, currentLevel.dimension());
        if (route == PortalRoute.NONE) {
            return null;
        }
        if (route == PortalRoute.OVERWORLD_SPAWN) {
            return spawnTransition(currentLevel, entity);
        }

        var targetDimension = switch (route) {
            case UNDERWORLD -> Underworld.LEVEL;
            case OVERWORLD -> Level.OVERWORLD;
            case NETHER -> Level.NETHER;
            case OVERWORLD_SPAWN, NONE -> null;
        };
        if (targetDimension == null) {
            return null;
        }
        ServerLevel targetLevel = currentLevel.getServer().getLevel(targetDimension);
        if (targetLevel == null) {
            return null;
        }

        boolean returningToOverworld = route == PortalRoute.OVERWORLD && portalType == PortalType.UNDERWORLD;
        BlockPos preferred = returningToOverworld
                ? entity.getPersistentData()
                        .getLong(RETURN_POS)
                        .map(BlockPos::of)
                        .orElse(BlockPos.containing(entity.position()))
                : BlockPos.containing(entity.position());
        if (route == PortalRoute.UNDERWORLD && currentLevel.dimension().equals(Level.OVERWORLD)) {
            entity.getPersistentData().putLong(RETURN_POS, preferred.asLong());
        }

        BlockPos interior = createArrivalPortal(targetLevel, preferred);
        TeleportTransition.PostTeleportTransition post = TeleportTransition.PLAY_PORTAL_SOUND
                .then(TeleportTransition.PLACE_PORTAL_TICKET)
                .then(Entity::setPortalCooldown);
        return new TeleportTransition(
                targetLevel,
                Vec3.atBottomCenterOf(interior),
                Vec3.ZERO,
                entity.getYRot(),
                entity.getXRot(),
                post);
    }

    private static TeleportTransition spawnTransition(ServerLevel level, Entity entity) {
        TeleportTransition.PostTeleportTransition post = TeleportTransition.PLAY_PORTAL_SOUND
                .then(Entity::setPortalCooldown);
        if (entity instanceof ServerPlayer player) {
            return TeleportTransition.createDefault(player, post);
        }
        return new TeleportTransition(
                level,
                Vec3.atBottomCenterOf(level.getRespawnData().pos()),
                Vec3.ZERO,
                entity.getYRot(),
                entity.getXRot(),
                post);
    }

    public static PortalRoute routeFor(PortalType portalType, net.minecraft.resources.ResourceKey<Level> dimension) {
        return switch (portalType) {
            case UNDERWORLD -> {
                if (dimension.equals(Level.OVERWORLD)) {
                    yield PortalRoute.UNDERWORLD;
                }
                yield dimension.equals(Underworld.LEVEL) ? PortalRoute.OVERWORLD : PortalRoute.NONE;
            }
            case NETHER -> {
                if (dimension.equals(Underworld.LEVEL)) {
                    yield PortalRoute.NETHER;
                }
                yield dimension.equals(Level.NETHER) ? PortalRoute.UNDERWORLD : PortalRoute.NONE;
            }
            case RETURN_SPAWN -> dimension.equals(Level.OVERWORLD)
                    ? PortalRoute.OVERWORLD_SPAWN
                    : PortalRoute.NONE;
        };
    }

    public enum PortalType {
        UNDERWORLD,
        NETHER,
        RETURN_SPAWN
    }

    public enum PortalRoute {
        OVERWORLD_SPAWN,
        UNDERWORLD,
        OVERWORLD,
        NETHER,
        NONE
    }

    private record PortalInterior(int height, int portalBlocks) {}

    public BlockPos createArrivalPortal(ServerLevel level, BlockPos preferred) {
        return buildPortal(level, findSafePosition(level, preferred));
    }

    private static BlockPos findSafePosition(ServerLevel level, BlockPos preferred) {
        int minY = level.getMinY() + 2;
        int maxY = level.getMaxY() - 5;
        int preferredY = Math.clamp(preferred.getY(), minY, maxY);
        BlockPos best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos candidate = new BlockPos(preferred.getX() + dx, y, preferred.getZ() + dz);
                    if (isSafe(level, candidate)) {
                        int distance = Math.abs(y - preferredY) + Math.abs(dx) + Math.abs(dz);
                        if (distance < bestDistance) {
                            best = candidate;
                            bestDistance = distance;
                        }
                    }
                }
            }
        }
        return best != null ? best : new BlockPos(preferred.getX(), preferredY, preferred.getZ());
    }

    private static boolean isSafe(ServerLevel level, BlockPos feet) {
        if (!level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP)) {
            return false;
        }
        for (int x = -1; x <= 2; x++) {
            for (int y = 0; y <= 3; y++) {
                if (!level.getBlockState(feet.offset(x, y, 0)).isAir()) {
                    return false;
                }
            }
        }
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 3; y++) {
                if (!level.getBlockState(feet.offset(x, y, -1)).isAir()
                        || !level.getBlockState(feet.offset(x, y, 1)).isAir()) {
                    return false;
                }
            }
        }
        return true;
    }

    private BlockPos buildPortal(ServerLevel level, BlockPos feet) {
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        BlockState portal = defaultBlockState().setValue(AXIS, Direction.Axis.X);
        int z = feet.getZ();
        for (int x = -1; x <= 2; x++) {
            for (int platformZ = -1; platformZ <= 1; platformZ++) {
                level.setBlock(new BlockPos(feet.getX() + x, feet.getY() - 1, z + platformZ), obsidian, 3);
            }
            level.setBlock(new BlockPos(feet.getX() + x, feet.getY() + 3, z), obsidian, 3);
        }
        for (int y = 0; y < 3; y++) {
            level.setBlock(new BlockPos(feet.getX() - 1, feet.getY() + y, z), obsidian, 3);
            level.setBlock(new BlockPos(feet.getX() + 2, feet.getY() + y, z), obsidian, 3);
            for (int x = 0; x < 2; x++) {
                BlockPos portalPos = new BlockPos(feet.getX() + x, feet.getY() + y, z);
                level.setBlock(portalPos, portal, 18);
                level.setBlock(portalPos.relative(Direction.SOUTH), Blocks.AIR.defaultBlockState(), 3);
                level.setBlock(portalPos.relative(Direction.NORTH), Blocks.AIR.defaultBlockState(), 3);
            }
        }
        return feet.relative(Direction.SOUTH);
    }
}
