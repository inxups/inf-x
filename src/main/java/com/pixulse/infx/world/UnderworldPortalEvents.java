package com.pixulse.infx.world;

import com.pixulse.infx.block.R196PortalBlock;
import com.pixulse.infx.block.R196PortalBlock.PortalType;
import com.pixulse.infx.block.UnderworldPortalBlock;
import com.pixulse.infx.registry.ModBlocks;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jspecify.annotations.Nullable;

/** Converts each ordinary frame into a portal block with one fixed destination family. */
public final class UnderworldPortalEvents {
    private UnderworldPortalEvents() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(UnderworldPortalEvents::onPortalSpawn);
    }

    private static void onPortalSpawn(BlockEvent.PortalSpawnEvent event) {
        if (event.getLevel() instanceof ServerLevel level
                && tryCreateR196Portal(level, event.getPos(), event.getPortalSize())) {
            event.setCanceled(true);
        }
    }

    public static boolean tryCreateR196Portal(ServerLevel level, BlockPos origin, PortalShape shape) {
        if (!supportsOrdinaryPortals(level.dimension())) {
            return false;
        }
        // Frame support checks need portal surfaces; clear them if conversion cannot finish.
        shape.createPortalBlocks(level);
        PortalType portalType = UnderworldPortalBlock.hasRuneGate(level, origin)
                ? PortalType.UNDERWORLD
                : portalTypeFor(level, origin);
        if (portalType == null) {
            clearConnectedPortal(level, origin);
            return false;
        }
        replaceConnectedPortal(level, origin, portalType);
        if (!level.getBlockState(origin).is(portalBlock(portalType))) {
            clearConnectedPortal(level, origin);
            return false;
        }
        return true;
    }

    public static boolean tryCreateUnderworldPortal(ServerLevel level, BlockPos origin, PortalShape shape) {
        if (!level.dimension().equals(Level.OVERWORLD)) {
            return false;
        }
        // frameRestsOnBottomBedrock inspects portal faces, so create first then validate.
        shape.createPortalBlocks(level);
        if (!frameRestsOnBottomBedrock(level, origin)) {
            clearConnectedPortal(level, origin);
            return false;
        }
        replaceConnectedPortal(level, origin, PortalType.UNDERWORLD);
        if (!level.getBlockState(origin).is(ModBlocks.UNDERWORLD_PORTAL.get())) {
            clearConnectedPortal(level, origin);
            return false;
        }
        return true;
    }

    /** Selects a block type once at creation time instead of routing later from nearby portal faces. */
    public static @Nullable PortalType portalTypeFor(ServerLevel level, BlockPos origin) {
        return portalTypeFor(
                level.dimension(),
                frameRestsOnBottomBedrock(level, origin),
                frameTouchesMantle(level, origin));
    }

    public static @Nullable PortalType portalTypeFor(
            net.minecraft.resources.ResourceKey<Level> dimension, boolean bottomBedrock, boolean mantle) {
        if (dimension.equals(Level.OVERWORLD)) {
            return bottomBedrock ? PortalType.UNDERWORLD : PortalType.RETURN_SPAWN;
        }
        if (dimension.equals(Underworld.LEVEL)) {
            return mantle ? PortalType.NETHER : PortalType.UNDERWORLD;
        }
        return dimension.equals(Level.NETHER) ? PortalType.NETHER : null;
    }

    public static boolean frameRestsOnBottomBedrock(ServerLevel level, BlockPos origin) {
        return frameHasSupport(level, origin, pos -> pos.getY() == level.getMinY()
                && level.getBlockState(pos).is(Blocks.BEDROCK));
    }

    public static boolean frameTouchesMantle(ServerLevel level, BlockPos origin) {
        return frameHasSupport(level, origin, pos -> level.getBlockState(pos).is(ModBlocks.MANTLE.get()));
    }

    private static boolean frameHasSupport(
            ServerLevel level,
            BlockPos origin,
            java.util.function.Predicate<BlockPos> support) {
        BlockState originState = level.getBlockState(origin);
        if (!isPortalSurface(originState)) {
            return false;
        }
        Direction.Axis axis = originState.getValue(NetherPortalBlock.AXIS);
        Block source = originState.getBlock();
        Direction horizontal = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        BlockPos bottom = origin.immutable();
        while (samePortal(level, bottom.below(), axis, source)) bottom = bottom.below();
        BlockPos first = bottom;
        while (samePortal(level, first.relative(horizontal.getOpposite()), axis, source)) {
            first = first.relative(horizontal.getOpposite());
        }
        int width = 0;
        while (width < PortalShape.MAX_WIDTH && samePortal(level, first.relative(horizontal, width), axis, source)) {
            width++;
        }
        for (int offset = -1; offset <= width; offset++) {
            if (support.test(first.relative(horizontal, offset).below(2))) return true;
        }
        return false;
    }

    public static R196PortalBlock portalBlock(PortalType portalType) {
        return switch (portalType) {
            case UNDERWORLD -> ModBlocks.UNDERWORLD_PORTAL.get();
            case NETHER -> ModBlocks.NETHER_PORTAL.get();
            case RETURN_SPAWN -> ModBlocks.RETURN_SPAWN_PORTAL.get();
        };
    }

    public static boolean isR196Portal(BlockState state) {
        return state.is(ModBlocks.UNDERWORLD_PORTAL.get())
                || state.is(ModBlocks.NETHER_PORTAL.get())
                || state.is(ModBlocks.RETURN_SPAWN_PORTAL.get());
    }

    /** Replaces only the source block type, so adjacent portal families cannot join together. */
    public static void replaceConnectedPortal(ServerLevel level, BlockPos origin, PortalType portalType) {
        BlockState originState = level.getBlockState(origin);
        if (!isPortalSurface(originState)) {
            return;
        }
        replaceConnectedPortal(level, origin, originState.getBlock(), portalType, null);
    }

    /** Removes portal faces created for a failed conversion so no orphan interior remains. */
    public static void clearConnectedPortal(ServerLevel level, BlockPos origin) {
        BlockState originState = level.getBlockState(origin);
        if (!isPortalSurface(originState)) {
            return;
        }
        Block source = originState.getBlock();
        Direction.Axis axis = originState.getValue(NetherPortalBlock.AXIS);
        Direction horizontal = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        pending.add(origin.immutable());
        while (!pending.isEmpty()) {
            BlockPos pos = pending.removeFirst();
            BlockState state = level.getBlockState(pos);
            if (!state.is(source)
                    || !state.hasProperty(NetherPortalBlock.AXIS)
                    || state.getValue(NetherPortalBlock.AXIS) != axis
                    || !visited.add(pos)) {
                continue;
            }
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 18);
            pending.add(pos.above());
            pending.add(pos.below());
            pending.add(pos.relative(horizontal));
            pending.add(pos.relative(horizontal.getOpposite()));
        }
    }

    private static void replaceConnectedPortal(
            ServerLevel level,
            BlockPos origin,
            Block source,
            PortalType portalType,
            @Nullable Set<BlockPos> updated) {
        BlockState originState = level.getBlockState(origin);
        if (!originState.is(source) || !originState.hasProperty(NetherPortalBlock.AXIS)) {
            return;
        }
        Direction.Axis axis = originState.getValue(NetherPortalBlock.AXIS);
        boolean runeGate = portalType == PortalType.UNDERWORLD && UnderworldPortalBlock.hasRuneGate(level, origin);
        Direction horizontal = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        Set<BlockPos> visited = updated != null ? updated : new HashSet<>();
        pending.add(origin.immutable());
        while (!pending.isEmpty()) {
            BlockPos pos = pending.removeFirst();
            BlockState state = level.getBlockState(pos);
            if (!state.is(source)
                    || !state.hasProperty(NetherPortalBlock.AXIS)
                    || state.getValue(NetherPortalBlock.AXIS) != axis) {
                continue;
            }
            if (!visited.add(pos)) {
                continue;
            }
            level.setBlock(pos, portalState(portalType, axis, runeGate), 18);
            pending.add(pos.above());
            pending.add(pos.below());
            pending.add(pos.relative(horizontal));
            pending.add(pos.relative(horizontal.getOpposite()));
        }
    }

    public static void refreshRuneGateAppearance(ServerLevel level, BlockPos runePos) {
        Set<BlockPos> updated = new HashSet<>();
        for (BlockPos pos : BlockPos.betweenClosed(runePos.offset(-4, -5, -4), runePos.offset(4, 2, 4))) {
            BlockState state = level.getBlockState(pos);
            if (!isR196Portal(state) || updated.contains(pos)) {
                continue;
            }
            PortalType portalType = UnderworldPortalBlock.hasRuneGate(level, pos)
                    ? PortalType.UNDERWORLD
                    : portalTypeFor(level, pos);
            if (portalType != null) {
                replaceConnectedPortal(level, pos, state.getBlock(), portalType, updated);
            }
        }
    }

    private static BlockState portalState(PortalType portalType, Direction.Axis axis, boolean runeGate) {
        BlockState state = portalBlock(portalType)
                .defaultBlockState()
                .setValue(NetherPortalBlock.AXIS, axis);
        return portalType == PortalType.UNDERWORLD
                ? state.setValue(UnderworldPortalBlock.RUNE_GATE, runeGate)
                : state;
    }

    private static boolean supportsOrdinaryPortals(net.minecraft.resources.ResourceKey<Level> dimension) {
        return dimension.equals(Level.OVERWORLD)
                || dimension.equals(Underworld.LEVEL)
                || dimension.equals(Level.NETHER);
    }

    private static boolean isPortalSurface(BlockState state) {
        return state.hasProperty(NetherPortalBlock.AXIS)
                && (state.is(Blocks.NETHER_PORTAL) || isR196Portal(state));
    }

    private static boolean samePortal(
            ServerLevel level, BlockPos pos, Direction.Axis axis, Block source) {
        BlockState state = level.getBlockState(pos);
        return state.is(source)
                && state.hasProperty(NetherPortalBlock.AXIS)
                && state.getValue(NetherPortalBlock.AXIS) == axis;
    }
}
