package com.pixulse.infx.world;

import com.mojang.serialization.Codec;
import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.registry.InfXEntityTypes;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructurePiece;

/** InfX-style Underworld monster rooms with Longdead spawners and dedicated loot. */
public final class InfXUnderworldDungeonFeature extends Feature<NoneFeatureConfiguration> {
    private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();

    public InfXUnderworldDungeonFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        Predicate<BlockState> replaceable = Feature.isReplaceable(BlockTags.FEATURES_CANNOT_REPLACE);
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        WorldGenLevel level = context.level();
        int xRadius = random.nextInt(2) + 2;
        int zRadius = random.nextInt(2) + 2;
        int minX = -xRadius - 1;
        int maxX = xRadius + 1;
        int minZ = -zRadius - 1;
        int maxZ = zRadius + 1;
        int entrances = 0;

        for (int dx = minX; dx <= maxX; dx++) {
            for (int dy = -1; dy <= 4; dy++) {
                for (int dz = minZ; dz <= maxZ; dz++) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    if ((dy == -1 || dy == 4) && !level.getBlockState(pos).isSolid()) {
                        return false;
                    }
                    if ((dx == minX || dx == maxX || dz == minZ || dz == maxZ)
                            && dy == 0
                            && level.isEmptyBlock(pos)
                            && level.isEmptyBlock(pos.above())) {
                        entrances++;
                    }
                }
            }
        }

        if (!hasValidEntranceCount(entrances)) {
            return false;
        }

        for (int dx = minX; dx <= maxX; dx++) {
            for (int dy = 3; dy >= -1; dy--) {
                for (int dz = minZ; dz <= maxZ; dz++) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    if (dx == minX || dy == -1 || dz == minZ || dx == maxX || dy == 4 || dz == maxZ) {
                        if (pos.getY() >= level.getMinY() && !level.getBlockState(pos.below()).isSolid()) {
                            level.setBlock(pos, AIR, 2);
                        } else if (state.isSolid() && !state.is(Blocks.CHEST)) {
                            this.safeSetBlock(
                                    level,
                                    pos,
                                    dy == -1 && random.nextInt(4) != 0
                                            ? Blocks.MOSSY_COBBLESTONE.defaultBlockState()
                                            : Blocks.COBBLESTONE.defaultBlockState(),
                                    replaceable);
                        }
                    } else if (!state.is(Blocks.CHEST) && !state.is(Blocks.SPAWNER)) {
                        this.safeSetBlock(level, pos, AIR, replaceable);
                    }
                }
            }
        }

        for (int chest = 0; chest < 2; chest++) {
            for (int attempt = 0; attempt < 3; attempt++) {
                BlockPos chestPos = new BlockPos(
                        origin.getX() + random.nextInt(xRadius * 2 + 1) - xRadius,
                        origin.getY(),
                        origin.getZ() + random.nextInt(zRadius * 2 + 1) - zRadius);
                if (!level.isEmptyBlock(chestPos) || solidHorizontalNeighbors(level, chestPos) != 1) {
                    continue;
                }

                this.safeSetBlock(level, chestPos, StructurePiece.reorient(level, chestPos, Blocks.CHEST.defaultBlockState()), replaceable);
                RandomizableContainer.setBlockEntityLootTable(level, random, chestPos, Underworld.DUNGEON_LOOT);
                break;
            }
        }

        this.safeSetBlock(level, origin, Blocks.SPAWNER.defaultBlockState(), replaceable);
        if (level.getBlockEntity(origin) instanceof SpawnerBlockEntity spawner) {
            spawner.setEntityId(randomSpawnerEntity(random), random);
        } else {
            InfiniteX.LOGGER.error(
                    "Failed to fetch Underworld dungeon spawner at ({}, {}, {})",
                    origin.getX(),
                    origin.getY(),
                    origin.getZ());
        }
        return true;
    }

    static boolean hasValidEntranceCount(int entrances) {
        return entrances >= 1 && entrances <= 5;
    }

    static boolean isGuardianRoll(int roll) {
        if (roll < 0 || roll >= 6) {
            throw new IllegalArgumentException("Underworld dungeon roll must be in [0, 6): " + roll);
        }
        return roll == 0;
    }

    private static int solidHorizontalNeighbors(WorldGenLevel level, BlockPos pos) {
        int neighbors = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(pos.relative(direction)).isSolid()) {
                neighbors++;
            }
        }
        return neighbors;
    }

    private static EntityType<?> randomSpawnerEntity(RandomSource random) {
        return isGuardianRoll(random.nextInt(6))
                ? InfXEntityTypes.LONGDEAD_GUARDIAN.get()
                : InfXEntityTypes.LONGDEAD.get();
    }
}
