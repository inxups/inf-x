package com.pixulse.infx.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.level.ChunkEvent;

/** Adds the four R196 safety torches above newly generated desert-pyramid treasure chests. */
public final class R196StructureSafetyEvents {
    private R196StructureSafetyEvents() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(R196StructureSafetyEvents::onChunkLoad);
    }

    private static void onChunkLoad(ChunkEvent.Load event) {
        if (!event.isNewChunk() || !(event.getLevel() instanceof ServerLevel level)) return;
        LevelChunk chunk = event.getChunk();
        level.getServer().execute(() -> addDesertPyramidTorches(level, chunk));
    }

    static int addDesertPyramidTorches(ServerLevel level, LevelChunk chunk) {
        var desertPyramid = level.registryAccess()
                .lookupOrThrow(Registries.STRUCTURE)
                .getOrThrow(BuiltinStructures.DESERT_PYRAMID)
                .value();
        int placed = 0;
        for (var blockEntity : chunk.getBlockEntities().values()) {
            if (!(blockEntity instanceof ChestBlockEntity chest)) continue;
            BlockPos chestPos = chest.getBlockPos();
            StructureStart start = level.structureManager().getStructureWithPieceAt(chestPos, desertPyramid);
            if (start == StructureStart.INVALID_START) continue;
            BlockPos torchPos = chestPos.above();
            if (!level.getBlockState(torchPos).canBeReplaced()) continue;
            BlockPos center = start.getBoundingBox().getCenter();
            Direction facing = towardCenter(chestPos, center);
            var torch = Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, facing);
            if (torch.canSurvive(level, torchPos) && level.setBlock(torchPos, torch, 3)) placed++;
        }
        return placed;
    }

    public static Direction towardCenter(BlockPos chest, BlockPos center) {
        int dx = center.getX() - chest.getX();
        int dz = center.getZ() - chest.getZ();
        if (Math.abs(dx) > Math.abs(dz)) return dx >= 0 ? Direction.EAST : Direction.WEST;
        return dz >= 0 ? Direction.SOUTH : Direction.NORTH;
    }
}
