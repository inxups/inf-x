package com.pixulse.infx.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Places InfX's single one-in-four brown mushroom candidate for each decorated chunk. */
public final class InfXUnderworldBrownMushroomFeature extends Feature<NoneFeatureConfiguration> {
    private static final int CHUNK_SIZE = 16;
    private static final int CHUNK_OFFSET = 8;
    private static final BlockState BROWN_MUSHROOM = Blocks.BROWN_MUSHROOM.defaultBlockState();

    public InfXUnderworldBrownMushroomFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        RandomSource random = context.random();
        if (!isBrownMushroomRoll(random.nextInt(Underworld.BROWN_MUSHROOM_CHANCE))) {
            return false;
        }

        BlockPos origin = context.origin();
        int minX = SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(origin.getX()));
        int minZ = SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(origin.getZ()));
        BlockPos mushroomPos = new BlockPos(
                minX + random.nextInt(CHUNK_SIZE) + CHUNK_OFFSET,
                Underworld.TERRAIN_MIN_Y + random.nextInt(Underworld.TERRAIN_HEIGHT),
                minZ + random.nextInt(CHUNK_SIZE) + CHUNK_OFFSET);
        if (InfXUnderworldBiomeAccess.isLushBiome(
                context.level(), mushroomPos.getX(), mushroomPos.getY(), mushroomPos.getZ())) {
            return false;
        }

        WorldGenLevel level = context.level();
        if (!level.isEmptyBlock(mushroomPos) || !BROWN_MUSHROOM.canSurvive(level, mushroomPos)) {
            return false;
        }

        return level.setBlock(mushroomPos, BROWN_MUSHROOM, 2);
    }

    static boolean isBrownMushroomRoll(int roll) {
        return roll == 0;
    }

}
