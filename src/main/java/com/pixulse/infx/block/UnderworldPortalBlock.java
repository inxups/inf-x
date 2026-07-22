package com.pixulse.infx.block;

import com.pixulse.infx.registry.ModBlocks;
import com.pixulse.infx.world.Underworld;
import com.pixulse.infx.world.UnderworldPortalEvents;
import com.pixulse.infx.progression.ProgressionEvents;
import com.pixulse.infx.material.R196Material;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class UnderworldPortalBlock extends NetherPortalBlock {
    private static final String RETURN_POS = "infx_underworld_return_pos";
    private static final int MIN_PORTAL_WIDTH = 2;
    private static final int MAX_PORTAL_WIDTH = 21;
    private static final int MIN_PORTAL_HEIGHT = 3;
    private static final int MAX_PORTAL_HEIGHT = 21;
    public static final BooleanProperty RUNE_GATE = BooleanProperty.create("rune_gate");

    public UnderworldPortalBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(RUNE_GATE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(RUNE_GATE);
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

    /** Mirrors NetherPortalBlock's structural check while accepting the replacement portal state. */
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
        return state.is(this) || state.is(Blocks.NETHER_PORTAL);
    }

    private static boolean isPortalFrame(BlockState state, LevelReader level, BlockPos pos) {
        return state.isPortalFrame(level, pos) || state.getBlock() instanceof RuneStoneBlock;
    }

    private record PortalInterior(int height, int portalBlocks) {}

    @Override
    public @Nullable TeleportTransition getPortalDestination(
            ServerLevel currentLevel, Entity entity, BlockPos portalEntryPos) {
        Optional<RuneGate> runeGate = findRuneGate(currentLevel, portalEntryPos);
        if (runeGate.isPresent()) {
            return runeTransition(currentLevel, entity, runeGate.get());
        }

        PortalRoute route = routeFor(
                currentLevel.dimension(),
                UnderworldPortalEvents.frameRestsOnBottomBedrock(currentLevel, portalEntryPos),
                UnderworldPortalEvents.frameTouchesMantle(currentLevel, portalEntryPos));
        if (route == PortalRoute.OVERWORLD_SPAWN) {
            TeleportTransition.PostTeleportTransition post = TeleportTransition.PLAY_PORTAL_SOUND
                    .then(Entity::setPortalCooldown);
            if (entity instanceof ServerPlayer player) {
                return TeleportTransition.createDefault(player, post);
            }
            return new TeleportTransition(
                    currentLevel,
                    Vec3.atBottomCenterOf(currentLevel.getRespawnData().pos()),
                    Vec3.ZERO,
                    entity.getYRot(),
                    entity.getXRot(),
                    post);
        }

        boolean fromOverworld = currentLevel.dimension().equals(Level.OVERWORLD);
        boolean fromUnderworld = currentLevel.dimension().equals(Underworld.LEVEL);
        var targetDimension = switch (route) {
            case UNDERWORLD -> Underworld.LEVEL;
            case OVERWORLD -> Level.OVERWORLD;
            case NETHER -> Level.NETHER;
            case OVERWORLD_SPAWN, NONE -> null;
        };
        if (targetDimension == null) return null;
        ServerLevel targetLevel = currentLevel.getServer().getLevel(targetDimension);
        if (targetLevel == null) {
            return null;
        }

        BlockPos preferred;
        if (fromUnderworld && targetDimension.equals(Level.OVERWORLD)) {
            preferred = entity.getPersistentData()
                    .getLong(RETURN_POS)
                    .map(BlockPos::of)
                    .orElse(BlockPos.containing(entity.position()));
        } else {
            preferred = BlockPos.containing(entity.position());
            if (fromOverworld) entity.getPersistentData().putLong(RETURN_POS, preferred.asLong());
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

    public static PortalRoute routeFor(
            net.minecraft.resources.ResourceKey<Level> dimension,
            boolean bottomBedrock,
            boolean mantle) {
        if (dimension.equals(Level.OVERWORLD)) {
            return bottomBedrock ? PortalRoute.UNDERWORLD : PortalRoute.OVERWORLD_SPAWN;
        }
        if (dimension.equals(Underworld.LEVEL)) {
            return mantle ? PortalRoute.NETHER : PortalRoute.OVERWORLD;
        }
        return dimension.equals(Level.NETHER) ? PortalRoute.UNDERWORLD : PortalRoute.NONE;
    }

    public enum PortalRoute {
        OVERWORLD_SPAWN,
        UNDERWORLD,
        OVERWORLD,
        NETHER,
        NONE
    }

    private static Optional<RuneGate> findRuneGate(ServerLevel level, BlockPos portal) {
        List<RuneEntry> runes = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(portal.offset(-4, -2, -4), portal.offset(4, 5, 4))) {
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof RuneStoneBlock)) continue;
            int adjacentFrame = 0;
            for (Direction direction : Direction.values()) {
                if (level.getBlockState(pos.relative(direction)).is(Blocks.OBSIDIAN)) adjacentFrame++;
            }
            if (adjacentFrame >= 2) runes.add(new RuneEntry(pos.immutable(), state));
        }
        if (runes.size() != 4) return Optional.empty();
        boolean adamantium = runes.stream().allMatch(entry -> entry.state().is(ModBlocks.ADAMANTIUM_RUNE_STONE.get()));
        boolean mithril = runes.stream().allMatch(entry -> entry.state().is(ModBlocks.MITHRIL_RUNE_STONE.get()));
        if (!adamantium && !mithril) return Optional.empty();
        runes.sort(Comparator.comparingInt((RuneEntry entry) -> entry.pos().getY())
                .thenComparingInt(entry -> entry.pos().getX())
                .thenComparingInt(entry -> entry.pos().getZ()));
        int signature = 0;
        for (RuneEntry entry : runes) signature = signature << 4 | entry.state().getValue(RuneStoneBlock.RUNE);
        return Optional.of(new RuneGate(
                adamantium ? R196Material.ADAMANTIUM : R196Material.MITHRIL,
                signature));
    }

    public static boolean hasRuneGate(ServerLevel level, BlockPos portal) {
        return findRuneGate(level, portal).isPresent();
    }

    private static TeleportTransition runeTransition(ServerLevel level, Entity entity, RuneGate gate) {
        int orientationGroup = switch (entity.getDirection()) {
            case EAST, NORTH -> 0;
            case WEST, SOUTH -> 1;
            default -> 0;
        };
        BlockPos destination = null;
        for (int attempt = 0; attempt < 5; attempt++) {
            Vec3 offset = runeDestinationOffset(gate.material(), gate.signature(), orientationGroup, attempt);
            int x = Math.clamp((int) Math.round(offset.x), -29_999_000, 29_999_000);
            int z = Math.clamp((int) Math.round(offset.z), -29_999_000, 29_999_000);
            int y = level.dimension().equals(Level.OVERWORLD)
                    ? level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z)
                    : entity.blockPosition().getY();
            BlockPos preferred = new BlockPos(x, y, z);
            if (attempt < 4 && level.getBiome(preferred).is(BiomeTags.IS_OCEAN)) continue;
            destination = findRuneArrival(level, preferred);
            break;
        }
        TeleportTransition.PostTeleportTransition post = TeleportTransition.PLAY_PORTAL_SOUND
                .then(TeleportTransition.PLACE_PORTAL_TICKET)
                .then(Entity::setPortalCooldown)
                .then(arrived -> {
                    if (arrived instanceof ServerPlayer player) {
                        ProgressionEvents.award(player, "runegate", "used_runegate");
                    }
                });
        return new TeleportTransition(
                level,
                Vec3.atBottomCenterOf(destination),
                Vec3.ZERO,
                entity.getYRot(),
                entity.getXRot(),
                post);
    }

    public static Vec3 runeDestinationOffset(
            R196Material material, int signature, int orientationGroup, int attempt) {
        long mixed = mix64(Integer.toUnsignedLong(signature)
                ^ (long) orientationGroup * 0x9E3779B97F4A7C15L
                ^ (long) attempt * 0xD1B54A32D192ED03L
                ^ (material == R196Material.ADAMANTIUM ? 0x94D049BB133111EBL : 0x369DEA0F31A53F85L));
        double minimum = material == R196Material.ADAMANTIUM ? 20_000.0 : 2_500.0;
        double span = material == R196Material.ADAMANTIUM ? 20_000.0 : 2_500.0;
        double unit = ((mixed >>> 11) & ((1L << 53) - 1)) / (double) (1L << 53);
        double radius = minimum + span * unit;
        double angle = Math.floorMod(mixed >>> 32, 65_536L) / 65_536.0 * Math.PI * 2.0;
        return new Vec3(Math.cos(angle) * radius, 0.0, Math.sin(angle) * radius);
    }

    private static long mix64(long value) {
        value = (value ^ value >>> 30) * 0xBF58476D1CE4E5B9L;
        value = (value ^ value >>> 27) * 0x94D049BB133111EBL;
        return value ^ value >>> 31;
    }

    private static BlockPos findRuneArrival(ServerLevel level, BlockPos preferred) {
        int minY = level.getMinY() + 2;
        int maxY = level.getMaxY() - 3;
        int start = Math.clamp(preferred.getY(), minY, maxY);
        for (int delta = 0; delta <= Math.min(128, maxY - minY); delta++) {
            for (int y : new int[]{start + delta, start - delta}) {
                if (y < minY || y > maxY) continue;
                BlockPos feet = new BlockPos(preferred.getX(), y, preferred.getZ());
                if (level.getBlockState(feet).isAir()
                        && level.getBlockState(feet.above()).isAir()
                        && level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP)) {
                    return feet;
                }
            }
        }
        BlockPos fallback = new BlockPos(preferred.getX(), start, preferred.getZ());
        level.setBlock(fallback.below(), Blocks.OBSIDIAN.defaultBlockState(), 3);
        level.setBlock(fallback, Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(fallback.above(), Blocks.AIR.defaultBlockState(), 3);
        return fallback;
    }

    private record RuneEntry(BlockPos pos, BlockState state) {}

    private record RuneGate(R196Material material, int signature) {}

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

    private static BlockPos buildPortal(ServerLevel level, BlockPos feet) {
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        BlockState portal = ModBlocks.UNDERWORLD_PORTAL.get().defaultBlockState().setValue(AXIS, Direction.Axis.X);
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
