package com.pixulse.infx.world;

import com.pixulse.infx.registry.ModBlocks;
import java.util.ArrayDeque;
import java.util.HashSet;
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

/** Converts only bottom-bedrock Overworld frames into Underworld portals. */
public final class UnderworldPortalEvents {
    private UnderworldPortalEvents() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(UnderworldPortalEvents::onPortalSpawn);
    }

    private static void onPortalSpawn(BlockEvent.PortalSpawnEvent event) {
        if (event.getLevel() instanceof ServerLevel level
                && tryCreateUnderworldPortal(level, event.getPos(), event.getPortalSize())) {
            event.setCanceled(true);
        }
    }

    public static boolean tryCreateUnderworldPortal(ServerLevel level, BlockPos origin, PortalShape shape) {
        if (level.dimension() != Level.OVERWORLD || !frameRestsOnBottomBedrock(level, origin)) {
            return false;
        }
        shape.createPortalBlocks(level);
        replaceConnectedPortal(level, origin);
        return level.getBlockState(origin).is(ModBlocks.UNDERWORLD_PORTAL.get());
    }

    private static boolean frameRestsOnBottomBedrock(ServerLevel level, BlockPos origin) {
        for (int y = origin.getY() - 1; y >= level.getMinY(); y--) {
            BlockPos frame = new BlockPos(origin.getX(), y, origin.getZ());
            if (level.getBlockState(frame).isAir()) {
                continue;
            }
            BlockPos bedrock = frame.below();
            return level.getBlockState(frame).is(Blocks.OBSIDIAN)
                    && bedrock.getY() == level.getMinY()
                    && level.getBlockState(bedrock).is(Blocks.BEDROCK);
        }
        return false;
    }

    private static void replaceConnectedPortal(ServerLevel level, BlockPos origin) {
        var originState = level.getBlockState(origin);
        if (!originState.is(Blocks.NETHER_PORTAL)) {
            return;
        }
        Direction.Axis axis = originState.getValue(NetherPortalBlock.AXIS);
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
                    ModBlocks.UNDERWORLD_PORTAL.get().defaultBlockState().setValue(NetherPortalBlock.AXIS, axis),
                    18);
            pending.add(pos.above());
            pending.add(pos.below());
            pending.add(pos.relative(horizontal));
            pending.add(pos.relative(horizontal.getOpposite()));
        }
    }
}
