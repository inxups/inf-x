package com.pixulse.infx.world;

import com.pixulse.infx.block.UnderworldPortalBlock;
import com.pixulse.infx.registry.ModBlocks;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.portal.PortalShape;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Converts every R196 ordinary or rune-gate frame into the server-authoritative portal block. */
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
        if (level.dimension() != Level.OVERWORLD
                && level.dimension() != Underworld.LEVEL
                && level.dimension() != Level.NETHER) {
            return false;
        }
        shape.createPortalBlocks(level);
        replaceConnectedPortal(level, origin);
        return level.getBlockState(origin).is(ModBlocks.UNDERWORLD_PORTAL.get());
    }

    public static boolean tryCreateUnderworldPortal(ServerLevel level, BlockPos origin, PortalShape shape) {
        if (level.dimension() != Level.OVERWORLD) {
            return false;
        }
        shape.createPortalBlocks(level);
        if (!frameRestsOnBottomBedrock(level, origin)) {
            return false;
        }
        replaceConnectedPortal(level, origin);
        return level.getBlockState(origin).is(ModBlocks.UNDERWORLD_PORTAL.get());
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
        var originState = level.getBlockState(origin);
        if (!originState.hasProperty(NetherPortalBlock.AXIS)
                || (!originState.is(Blocks.NETHER_PORTAL)
                        && !originState.is(ModBlocks.UNDERWORLD_PORTAL.get()))) {
            return false;
        }
        Direction.Axis axis = originState.getValue(NetherPortalBlock.AXIS);
        Direction horizontal = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        BlockPos bottom = origin.immutable();
        while (samePortal(level, bottom.below(), axis)) bottom = bottom.below();
        BlockPos first = bottom;
        while (samePortal(level, first.relative(horizontal.getOpposite()), axis)) {
            first = first.relative(horizontal.getOpposite());
        }
        int width = 0;
        while (width < PortalShape.MAX_WIDTH && samePortal(level, first.relative(horizontal, width), axis)) {
            width++;
        }
        for (int offset = -1; offset <= width; offset++) {
            if (support.test(first.relative(horizontal, offset).below(2))) return true;
        }
        return false;
    }

    private static boolean samePortal(
            ServerLevel level, BlockPos pos, Direction.Axis axis) {
        var state = level.getBlockState(pos);
        return (state.is(Blocks.NETHER_PORTAL) || state.is(ModBlocks.UNDERWORLD_PORTAL.get()))
                && state.getValue(NetherPortalBlock.AXIS) == axis;
    }

    static void replaceConnectedPortal(ServerLevel level, BlockPos origin) {
        var originState = level.getBlockState(origin);
        if (!originState.is(Blocks.NETHER_PORTAL)) {
            return;
        }
        Direction.Axis axis = originState.getValue(NetherPortalBlock.AXIS);
        boolean runeGate = UnderworldPortalBlock.hasRuneGate(level, origin);
        Direction horizontal = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        pending.add(origin.immutable());
        while (!pending.isEmpty()) {
            BlockPos pos = pending.removeFirst();
            if (!visited.add(pos)) {
                continue;
            }
            var state = level.getBlockState(pos);
            if (!state.is(Blocks.NETHER_PORTAL) || state.getValue(NetherPortalBlock.AXIS) != axis) {
                continue;
            }
            level.setBlock(
                    pos,
                    ModBlocks.UNDERWORLD_PORTAL.get()
                            .defaultBlockState()
                            .setValue(NetherPortalBlock.AXIS, axis)
                            .setValue(UnderworldPortalBlock.RUNE_GATE, runeGate),
                    18);
            pending.add(pos.above());
            pending.add(pos.below());
            pending.add(pos.relative(horizontal));
            pending.add(pos.relative(horizontal.getOpposite()));
        }
    }

    public static void refreshRuneGateAppearance(ServerLevel level, BlockPos runePos) {
        Set<BlockPos> updated = new HashSet<>();
        for (BlockPos pos : BlockPos.betweenClosed(runePos.offset(-4, -5, -4), runePos.offset(4, 2, 4))) {
            var state = level.getBlockState(pos);
            if (!state.is(ModBlocks.UNDERWORLD_PORTAL.get()) || !updated.add(pos.immutable())) {
                continue;
            }
            setConnectedRuneGateState(level, pos, UnderworldPortalBlock.hasRuneGate(level, pos), updated);
        }
    }

    private static void setConnectedRuneGateState(
            ServerLevel level, BlockPos origin, boolean runeGate, Set<BlockPos> updated) {
        Direction.Axis axis = level.getBlockState(origin).getValue(NetherPortalBlock.AXIS);
        Direction horizontal = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        pending.add(origin.immutable());
        while (!pending.isEmpty()) {
            BlockPos pos = pending.removeFirst();
            var state = level.getBlockState(pos);
            if (!state.is(ModBlocks.UNDERWORLD_PORTAL.get())
                    || state.getValue(NetherPortalBlock.AXIS) != axis) {
                continue;
            }
            updated.add(pos);
            if (state.getValue(UnderworldPortalBlock.RUNE_GATE) != runeGate) {
                level.setBlock(pos, state.setValue(UnderworldPortalBlock.RUNE_GATE, runeGate), 18);
            }
            for (BlockPos neighbour : List.of(
                    pos.above(),
                    pos.below(),
                    pos.relative(horizontal),
                    pos.relative(horizontal.getOpposite()))) {
                if (!updated.contains(neighbour)) pending.add(neighbour);
            }
        }
    }
}
