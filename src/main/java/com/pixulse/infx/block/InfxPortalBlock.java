package com.pixulse.infx.block;

import com.pixulse.infx.registry.InfXParticles;
import com.pixulse.infx.registry.InfXPoiTypes;
import com.pixulse.infx.world.RunegateTeleportation;
import com.pixulse.infx.world.Underworld;
import com.pixulse.infx.event.UnderworldPortalEvents;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** A portal surface whose block identity fixes its destination family. */
public class InfxPortalBlock extends NetherPortalBlock {
    private static final int NETHER_PORTAL_SEARCH_RADIUS = 16;
    private static final int OTHER_PORTAL_SEARCH_RADIUS = 128;
    private static final int MIN_PORTAL_WIDTH = 2;
    private static final int MAX_PORTAL_WIDTH = 21;
    private static final int MIN_PORTAL_HEIGHT = 3;
    private static final int MAX_PORTAL_HEIGHT = 21;
    private static final PortalSize DEFAULT_PORTAL_SIZE =
            new PortalSize(Direction.Axis.X, MIN_PORTAL_WIDTH, MIN_PORTAL_HEIGHT);
    protected static final int INFX_RUNEGATE_ENTRY_TICKS = 1;

    private final PortalType portalType;

    public InfxPortalBlock(PortalType portalType, BlockBehaviour.Properties properties) {
        super(properties);
        this.portalType = portalType;
    }

    public PortalType portalType() {
        return portalType;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        ParticleOptions particle = portalParticle(state);
        if (particle == ParticleTypes.PORTAL) {
            super.animateTick(state, level, pos, random);
            return;
        }

        if (random.nextInt(100) == 0) {
            level.playLocalSound(
                    pos.getX() + 0.5D,
                    pos.getY() + 0.5D,
                    pos.getZ() + 0.5D,
                    SoundEvents.PORTAL_AMBIENT,
                    SoundSource.BLOCKS,
                    0.5F,
                    random.nextFloat() * 0.4F + 0.8F,
                    false);
        }

        for (int count = 0; count < 4; count++) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            double xd = (random.nextFloat() - 0.5F) * 0.5D;
            double yd = (random.nextFloat() - 0.5F) * 0.5D;
            double zd = (random.nextFloat() - 0.5F) * 0.5D;
            int direction = random.nextInt(2) * 2 - 1;

            if (level.getBlockState(pos.west()).is(this) || level.getBlockState(pos.east()).is(this)) {
                z = pos.getZ() + 0.5D + 0.25D * direction;
                zd = random.nextFloat() * 2.0F * direction;
            } else {
                x = pos.getX() + 0.5D + 0.25D * direction;
                xd = random.nextFloat() * 2.0F * direction;
            }

            level.addParticle(particle, x, y, z, xd, yd, zd);
        }
    }

    /** Selects the particle family without changing vanilla portal particle motion. */
    protected ParticleOptions portalParticle(BlockState state) {
        return switch (portalType) {
            case UNDERWORLD -> InfXParticles.UNDERWORLD_PORTAL.get();
            case NETHER -> InfXParticles.NETHER_PORTAL.get();
            case RETURN_SPAWN -> InfXParticles.RUNEGATE.get();
        };
    }

    @Override
    protected @NonNull BlockState updateShape(
            BlockState state,
            @NonNull LevelReader level,
            @NonNull ScheduledTickAccess ticks,
            @NonNull BlockPos pos,
            Direction direction,
            @NonNull BlockPos neighbourPos,
            @NonNull BlockState neighbourState,
            @NonNull RandomSource random) {
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
    public int getPortalTransitionTime(@NonNull ServerLevel level, @NonNull Entity entity) {
        return portalType == PortalType.RETURN_SPAWN && entity instanceof ServerPlayer
                ? INFX_RUNEGATE_ENTRY_TICKS
                : super.getPortalTransitionTime(level, entity);
    }

    @Override
    public @Nullable TeleportTransition getPortalDestination(
            ServerLevel currentLevel, @NonNull Entity entity, @NonNull BlockPos portalEntryPos) {
        PortalRoute route = routeFor(portalType, currentLevel.dimension());
        if (route == PortalRoute.NONE) {
            return null;
        }
        if (route == PortalRoute.OVERWORLD_SPAWN) {
            TeleportTransition transition = spawnTransition(currentLevel, entity);
            if (entity instanceof ServerPlayer player && RunegateTeleportation.start(player, transition)) {
                return null;
            }
            return transition;
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

        BlockPos preferred = scaledExitPosition(currentLevel, targetLevel, entity);

        BlockPos arrival = findOrCreateArrivalPortal(
                targetLevel, preferred, portalSearchRadius(targetLevel), portalSize(currentLevel, portalEntryPos));
        TeleportTransition.PostTeleportTransition post = TeleportTransition.PLAY_PORTAL_SOUND
                .then(TeleportTransition.PLACE_PORTAL_TICKET)
                .then(Entity::setPortalCooldown);
        return new TeleportTransition(
                targetLevel,
                Vec3.atBottomCenterOf(arrival),
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

    public static PortalRoute routeFor(PortalType portalType, ResourceKey<Level> dimension) {
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

    /** Resolves the dimension reached by a portal family from its current dimension. */
    public static @Nullable ResourceKey<Level> destinationDimension(
            PortalType portalType, ResourceKey<Level> currentDimension) {
        return switch (routeFor(portalType, currentDimension)) {
            case UNDERWORLD -> Underworld.LEVEL;
            case OVERWORLD, OVERWORLD_SPAWN -> Level.OVERWORLD;
            case NETHER -> Level.NETHER;
            case NONE -> null;
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

    private record PortalSize(Direction.Axis axis, int width, int height) {}

    /** Returns the validated source shape so newly created destination gates preserve its usable opening. */
    private PortalSize portalSize(ServerLevel level, BlockPos portal) {
        BlockState state = level.getBlockState(portal);
        if (!state.is(this) || !state.hasProperty(AXIS)) {
            return DEFAULT_PORTAL_SIZE;
        }

        Direction.Axis axis = state.getValue(AXIS);
        if (!hasCompletePortalShape(level, portal, axis)) {
            return DEFAULT_PORTAL_SIZE;
        }

        Direction horizontal = portalHorizontal(axis);
        BlockPos bottom = portal;
        for (int depth = 0; depth < MAX_PORTAL_HEIGHT && isPortalSurface(level, bottom.below(), axis); depth++) {
            bottom = bottom.below();
        }
        BlockPos first = bottom;
        for (int offset = 0;
                offset < MAX_PORTAL_WIDTH && isPortalSurface(level, first.relative(horizontal.getOpposite()), axis);
                offset++) {
            first = first.relative(horizontal.getOpposite());
        }

        int width = 0;
        while (width < MAX_PORTAL_WIDTH && isPortalSurface(level, first.relative(horizontal, width), axis)) {
            width++;
        }
        int height = 0;
        while (height < MAX_PORTAL_HEIGHT && isPortalSurface(level, first.above(height), axis)) {
            height++;
        }
        return width >= MIN_PORTAL_WIDTH && height >= MIN_PORTAL_HEIGHT
                ? new PortalSize(axis, width, height)
                : DEFAULT_PORTAL_SIZE;
    }

    private static BlockPos scaledExitPosition(ServerLevel currentLevel, ServerLevel targetLevel, Entity entity) {
        double scale = DimensionType.getTeleportationScale(currentLevel.dimensionType(), targetLevel.dimensionType());
        return targetLevel.getWorldBorder().clampToBounds(
                entity.getX() * scale, entity.getY(), entity.getZ() * scale);
    }

    /** Applies the 26.1 coordinate-scale rule before a destination portal is selected. */
    public static BlockPos scaledExitPosition(
            DimensionType currentDimension, DimensionType targetDimension, Vec3 entityPosition) {
        double scale = DimensionType.getTeleportationScale(currentDimension, targetDimension);
        return BlockPos.containing(
                entityPosition.x * scale, entityPosition.y, entityPosition.z * scale);
    }

    private static int portalSearchRadius(ServerLevel targetLevel) {
        return portalSearchRadius(targetLevel.dimension());
    }

    /** Keeps the 26.1 Nether-vs-other-dimension search radii explicit for custom portal families. */
    public static int portalSearchRadius(ResourceKey<Level> targetDimension) {
        return targetDimension.equals(Level.NETHER)
                ? NETHER_PORTAL_SEARCH_RADIUS
                : OTHER_PORTAL_SEARCH_RADIUS;
    }

    /** Reuses the nearest compatible portal surface before creating a new destination. */
    public BlockPos findOrCreateArrivalPortal(ServerLevel level, BlockPos preferred) {
        return findOrCreateArrivalPortal(level, preferred, portalSearchRadius(level), DEFAULT_PORTAL_SIZE);
    }

    private BlockPos findOrCreateArrivalPortal(
            ServerLevel level, BlockPos preferred, int searchRadius, PortalSize portalSize) {
        Optional<BlockPos> existing = findExistingArrivalPortal(level, preferred, searchRadius);
        if (existing.isEmpty()) {
            existing = findLegacyArrivalPortal(level, preferred, searchRadius);
        }
        return existing.orElseGet(() -> createArrivalPortal(level, preferred, portalSize));
    }

    private Optional<BlockPos> findExistingArrivalPortal(ServerLevel level, BlockPos preferred, int searchRadius) {
        return findClosestPortal(
                        level,
                        preferred,
                        searchRadius,
                        holder -> holder.is(InfXPoiTypes.forPortal(portalType)),
                        portal -> isReusablePortal(level, portal))
                .map(portal -> findPortalExit(level, portal));
    }

    /** Migrates a compatible vanilla portal only after no dedicated destination was found. */
    private Optional<BlockPos> findLegacyArrivalPortal(ServerLevel level, BlockPos preferred, int searchRadius) {
        return findClosestPortal(
                        level,
                        preferred,
                        searchRadius,
                        holder -> holder.is(PoiTypes.NETHER_PORTAL),
                        portal -> isLegacyPortalFor(level, portal))
                .map(portal -> {
                    UnderworldPortalEvents.replaceConnectedPortal(level, portal, portalType);
                    return portal;
                })
                .filter(portal -> isReusablePortal(level, portal))
                .map(portal -> findPortalExit(level, portal));
    }

    /** Mirrors PortalForcer's loaded-POI lookup and distance ordering for this portal family. */
    private Optional<BlockPos> findClosestPortal(
            ServerLevel level,
            BlockPos preferred,
            int searchRadius,
            Predicate<Holder<PoiType>> poiType,
            Predicate<BlockPos> portalFilter) {
        PoiManager poiManager = level.getPoiManager();
        poiManager.ensureLoadedAndValid(level, preferred, searchRadius);
        WorldBorder worldBorder = level.getWorldBorder();
        return poiManager
                .getInSquare(
                        poiType,
                        preferred,
                        searchRadius,
                        PoiManager.Occupancy.ANY)
                .map(PoiRecord::getPos)
                .filter(worldBorder::isWithinBounds)
                .filter(portalFilter)
                .min(Comparator.<BlockPos>comparingDouble(portal -> portal.distSqr(preferred))
                        .thenComparingInt(Vec3i::getY));
    }

    private boolean isReusablePortal(ServerLevel level, BlockPos portal) {
        BlockState state = level.getBlockState(portal);
        if (!state.is(this)) {
            return false;
        }
        return !(this instanceof UnderworldPortalBlock
                && (state.getValue(UnderworldPortalBlock.RUNE_GATE)
                        || UnderworldPortalBlock.hasRuneGate(level, portal)));
    }

    private boolean isLegacyPortalFor(ServerLevel level, BlockPos portal) {
        return level.getBlockState(portal).is(Blocks.NETHER_PORTAL)
                && UnderworldPortalEvents.portalTypeFor(level, portal) == portalType;
    }

    private BlockPos findPortalExit(ServerLevel level, BlockPos portal) {
        BlockState state = level.getBlockState(portal);
        Direction.Axis axis = state.getValue(AXIS);
        Direction horizontal = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        Direction firstSide = axis == Direction.Axis.X ? Direction.SOUTH : Direction.EAST;
        BlockPos bottom = portal;
        for (int depth = 0; depth < MAX_PORTAL_HEIGHT && isPortalSurface(level, bottom.below(), axis); depth++) {
            bottom = bottom.below();
        }

        for (int height = 0; height < MAX_PORTAL_HEIGHT; height++) {
            for (int width = -MAX_PORTAL_WIDTH; width <= MAX_PORTAL_WIDTH; width++) {
                BlockPos surface = bottom.above(height).relative(horizontal, width);
                if (!isPortalSurface(level, surface, axis)) {
                    continue;
                }
                BlockPos firstExit = surface.relative(firstSide);
                if (isSafePortalExit(level, firstExit)) {
                    return firstExit;
                }
                BlockPos secondExit = surface.relative(firstSide.getOpposite());
                if (isSafePortalExit(level, secondExit)) {
                    return secondExit;
                }
            }
        }
        return portal;
    }

    private boolean isPortalSurface(ServerLevel level, BlockPos pos, Direction.Axis axis) {
        BlockState state = level.getBlockState(pos);
        return state.is(this) && state.getValue(AXIS) == axis;
    }

    private static boolean isSafePortalExit(ServerLevel level, BlockPos feet) {
        return level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP)
                && level.getBlockState(feet).isAir()
                && level.getBlockState(feet.above()).isAir();
    }

    public BlockPos createArrivalPortal(ServerLevel level, BlockPos preferred) {
        return createArrivalPortal(level, preferred, DEFAULT_PORTAL_SIZE);
    }

    /** Creates a destination gate with a shape that is valid for this portal family. */
    public BlockPos createArrivalPortal(
            ServerLevel level, BlockPos preferred, Direction.Axis axis, int width, int height) {
        return createArrivalPortal(level, preferred, clampedPortalSize(axis, width, height));
    }

    private BlockPos createArrivalPortal(ServerLevel level, BlockPos preferred, PortalSize portalSize) {
        return buildPortal(level, findSafePosition(level, preferred, portalSize), portalSize);
    }

    private static PortalSize clampedPortalSize(Direction.Axis axis, int width, int height) {
        Direction.Axis portalAxis = axis == Direction.Axis.Z ? Direction.Axis.Z : Direction.Axis.X;
        return new PortalSize(
                portalAxis,
                Math.clamp(width, MIN_PORTAL_WIDTH, MAX_PORTAL_WIDTH),
                Math.clamp(height, MIN_PORTAL_HEIGHT, MAX_PORTAL_HEIGHT));
    }

    private static BlockPos findSafePosition(ServerLevel level, BlockPos preferred, PortalSize portalSize) {
        int minY = level.getMinY() + 2;
        int maxY = level.getMaxY() - portalSize.height() - 2;
        int preferredY = Math.clamp(preferred.getY(), minY, maxY);
        BlockPos best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos candidate = new BlockPos(preferred.getX() + dx, y, preferred.getZ() + dz);
                    if (isSafe(level, candidate, portalSize)) {
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

    private static boolean isSafe(ServerLevel level, BlockPos feet, PortalSize portalSize) {
        if (!level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP)) {
            return false;
        }
        Direction horizontal = portalHorizontal(portalSize.axis());
        Direction exit = portalExitDirection(portalSize.axis());
        for (int widthOffset = -1; widthOffset <= portalSize.width(); widthOffset++) {
            for (int y = 0; y <= portalSize.height(); y++) {
                if (!level.getBlockState(feet.relative(horizontal, widthOffset).above(y)).isAir()) {
                    return false;
                }
            }
        }
        for (int widthOffset = 0; widthOffset < portalSize.width(); widthOffset++) {
            for (int y = 0; y < portalSize.height(); y++) {
                BlockPos portalPos = feet.relative(horizontal, widthOffset).above(y);
                if (!level.getBlockState(portalPos.relative(exit)).isAir()
                        || !level.getBlockState(portalPos.relative(exit.getOpposite())).isAir()) {
                    return false;
                }
            }
        }
        return true;
    }

    private BlockPos buildPortal(ServerLevel level, BlockPos feet, PortalSize portalSize) {
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        BlockState portal = defaultBlockState().setValue(AXIS, portalSize.axis());
        Direction horizontal = portalHorizontal(portalSize.axis());
        Direction exit = portalExitDirection(portalSize.axis());
        for (int widthOffset = -1; widthOffset <= portalSize.width(); widthOffset++) {
            BlockPos framePos = feet.relative(horizontal, widthOffset);
            for (int platformOffset = -1; platformOffset <= 1; platformOffset++) {
                level.setBlock(framePos.below().relative(exit, platformOffset), obsidian, 3);
            }
            level.setBlock(framePos.above(portalSize.height()), obsidian, 3);
        }
        for (int y = 0; y < portalSize.height(); y++) {
            level.setBlock(feet.relative(horizontal, -1).above(y), obsidian, 3);
            level.setBlock(feet.relative(horizontal, portalSize.width()).above(y), obsidian, 3);
            for (int widthOffset = 0; widthOffset < portalSize.width(); widthOffset++) {
                BlockPos portalPos = feet.relative(horizontal, widthOffset).above(y);
                level.setBlock(portalPos, portal, 18);
                level.setBlock(portalPos.relative(exit), Blocks.AIR.defaultBlockState(), 3);
                level.setBlock(portalPos.relative(exit.getOpposite()), Blocks.AIR.defaultBlockState(), 3);
            }
        }
        return feet.relative(exit);
    }

    private static Direction portalHorizontal(Direction.Axis axis) {
        return axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
    }

    private static Direction portalExitDirection(Direction.Axis axis) {
        return axis == Direction.Axis.X ? Direction.SOUTH : Direction.EAST;
    }
}
