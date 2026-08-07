package com.pixulse.infx.world;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

/** Modern ore-shaped gravel that keeps InfX's support requirement for every block. */
public final class InfXUnderworldSupportedGravelFeature extends OreFeature {
    public InfXUnderworldSupportedGravelFeature(Codec<OreConfiguration> codec) {
        super(codec);
    }

    @Override
    protected boolean doPlace(
            WorldGenLevel level,
            RandomSource random,
            OreConfiguration config,
            double x0,
            double x1,
            double z0,
            double z1,
            double y0,
            double y1,
            int xStart,
            int yStart,
            int zStart,
            int sizeXZ,
            int sizeY) {
        int placed = 0;
        BitSet tested = new BitSet(sizeXZ * sizeY * sizeXZ);
        BlockPos.MutableBlockPos orePos = new BlockPos.MutableBlockPos();
        int size = config.size;
        double[] data = new double[size * 4];

        for (int i = 0; i < size; i++) {
            float step = (float) i / size;
            double xx = Mth.lerp(step, x0, x1);
            double yy = Mth.lerp(step, y0, y1);
            double zz = Mth.lerp(step, z0, z1);
            double spread = random.nextDouble() * size / 16.0;
            double radius = ((Mth.sin((float) Math.PI * step) + 1.0F) * spread + 1.0) / 2.0;
            data[i * 4] = xx;
            data[i * 4 + 1] = yy;
            data[i * 4 + 2] = zz;
            data[i * 4 + 3] = radius;
        }

        for (int first = 0; first < size - 1; first++) {
            if (data[first * 4 + 3] <= 0.0) {
                continue;
            }
            for (int second = first + 1; second < size; second++) {
                if (data[second * 4 + 3] <= 0.0) {
                    continue;
                }
                double dx = data[first * 4] - data[second * 4];
                double dy = data[first * 4 + 1] - data[second * 4 + 1];
                double dz = data[first * 4 + 2] - data[second * 4 + 2];
                double dr = data[first * 4 + 3] - data[second * 4 + 3];
                if (dr * dr > dx * dx + dy * dy + dz * dz) {
                    if (dr > 0.0) {
                        data[second * 4 + 3] = -1.0;
                    } else {
                        data[first * 4 + 3] = -1.0;
                    }
                }
            }
        }

        try (BulkSectionAccess sections = new BulkSectionAccess(level)) {
            for (int i = 0; i < size; i++) {
                double radius = data[i * 4 + 3];
                if (radius < 0.0) {
                    continue;
                }
                double xx = data[i * 4];
                double yy = data[i * 4 + 1];
                double zz = data[i * 4 + 2];
                int minX = Math.max(Mth.floor(xx - radius), xStart);
                int minY = Math.max(Mth.floor(yy - radius), yStart);
                int minZ = Math.max(Mth.floor(zz - radius), zStart);
                int maxX = Math.max(Mth.floor(xx + radius), minX);
                int maxY = Math.max(Mth.floor(yy + radius), minY);
                int maxZ = Math.max(Mth.floor(zz + radius), minZ);

                for (int x = minX; x <= maxX; x++) {
                    double xd = (x + 0.5 - xx) / radius;
                    if (xd * xd >= 1.0) {
                        continue;
                    }
                    for (int y = minY; y <= maxY; y++) {
                        double yd = (y + 0.5 - yy) / radius;
                        if (xd * xd + yd * yd >= 1.0) {
                            continue;
                        }
                        for (int z = minZ; z <= maxZ; z++) {
                            double zd = (z + 0.5 - zz) / radius;
                            if (xd * xd + yd * yd + zd * zd >= 1.0 || level.isOutsideBuildHeight(y)) {
                                continue;
                            }
                            int bitSetIndex = x - xStart + (y - yStart) * sizeXZ + (z - zStart) * sizeXZ * sizeY;
                            if (tested.get(bitSetIndex)) {
                                continue;
                            }
                            tested.set(bitSetIndex);
                            orePos.set(x, y, z);
                            if (!level.ensureCanWrite(orePos) || !isSupported(level.getBlockState(orePos.below()))) {
                                continue;
                            }
                            LevelChunkSection section = sections.getSection(orePos);
                            if (section == null) {
                                continue;
                            }
                            int sectionX = SectionPos.sectionRelative(x);
                            int sectionY = SectionPos.sectionRelative(y);
                            int sectionZ = SectionPos.sectionRelative(z);
                            BlockState current = section.getBlockState(sectionX, sectionY, sectionZ);
                            for (OreConfiguration.TargetBlockState target : config.targetStates) {
                                if (!canPlaceOre(current, sections::getBlockState, random, config, target, orePos)) {
                                    continue;
                                }
                                section.setBlockState(sectionX, sectionY, sectionZ, target.state, false);
                                placed++;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return placed > 0;
    }

    static boolean isSupported(BlockState below) {
        return below.isSolid();
    }
}
