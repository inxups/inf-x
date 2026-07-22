package com.pixulse.infx.world;

import com.pixulse.infx.registry.ModBlocks;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * MITE R196's Underworld mantle and bedrock pass, adapted to InfiniteX's taller dimension.
 *
 * <p>The original pass runs after terrain generation, so it cannot be expressed by a 26.2 surface rule:
 * both boundary thickness and the four lower bedrock strata depend on per-column random values and
 * legacy octave noise.</p>
 */
public final class MiteUnderworldStrata {
    static final int LEGACY_TERRAIN_START_Y = 120;
    static final int CHUNK_SIDE_LENGTH = 16;
    private static final int COLUMN_COUNT = CHUNK_SIDE_LENGTH * CHUNK_SIDE_LENGTH;
    private static final int LOWER_STRATA_CELL_COUNT = COLUMN_COUNT * LEGACY_TERRAIN_START_Y;
    private static final byte NONE = 0;
    private static final byte MANTLE = 1;
    private static final byte BEDROCK = 2;
    private static final ConcurrentHashMap<Long, LegacyNoiseSet> NOISES_BY_WORLD_SEED = new ConcurrentHashMap<>();

    private MiteUnderworldStrata() {}

    public static void apply(long worldSeed, ChunkAccess chunk) {
        StrataPlan plan = plan(worldSeed, chunk.getPos());
        int minY = chunk.getMinY();
        int height = chunk.getHeight();
        int lowerHeight = Math.min(LEGACY_TERRAIN_START_Y, height);
        int topY = minY + height - 1;
        ChunkPos chunkPos = chunk.getPos();
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

        for (int localX = 0; localX < CHUNK_SIDE_LENGTH; localX++) {
            int blockX = chunkPos.getBlockX(localX);
            for (int localZ = 0; localZ < CHUNK_SIDE_LENGTH; localZ++) {
                int blockZ = chunkPos.getBlockZ(localZ);
                for (int relativeY = 0; relativeY < lowerHeight; relativeY++) {
                    byte replacement = plan.replacementAt(localX, localZ, relativeY);
                    if (replacement == NONE) continue;
                    chunk.setBlockState(
                            blockPos.set(blockX, minY + relativeY, blockZ),
                            replacement == MANTLE
                                    ? ModBlocks.MANTLE.get().defaultBlockState()
                                    : Blocks.BEDROCK.defaultBlockState());
                }

                int thickness = plan.boundaryThicknessAt(localX, localZ);
                for (int offset = 0; offset < thickness && offset < height; offset++) {
                    chunk.setBlockState(
                            blockPos.set(blockX, topY - offset, blockZ),
                            Blocks.BEDROCK.defaultBlockState());
                }
            }
        }
    }

    static StrataPlan plan(long worldSeed, ChunkPos chunkPos) {
        LegacyNoiseSet noiseSet = NOISES_BY_WORLD_SEED.computeIfAbsent(worldSeed, LegacyNoiseSet::new);
        StrataNoise strataNoise = noiseSet.sample(chunkPos);
        long columnSeed = miteHashedWorldSeed(worldSeed) * (long) miteChunkHash(chunkPos.x(), chunkPos.z());
        Random columnRandom = new Random(columnSeed);
        StrataPlan plan = new StrataPlan();
        int[] chanceIndex = {chunkPos.x() * 2653 + chunkPos.z() * 6714631};

        for (int localX = 0; localX < CHUNK_SIDE_LENGTH; localX++) {
            for (int localZ = 0; localZ < CHUNK_SIDE_LENGTH; localZ++) {
                int column = columnIndex(localX, localZ);
                int thickness = columnRandom.nextInt(3) + 1;
                plan.setBoundaryThickness(localX, localZ, thickness);

                for (int relativeY = 0; relativeY < LEGACY_TERRAIN_START_Y; relativeY++) {
                    if (relativeY < thickness) {
                        plan.setReplacement(localX, localZ, relativeY, MANTLE);
                    } else if (isInternalBedrock(relativeY, column, strataNoise, noiseSet.chanceIn2, chanceIndex)) {
                        plan.setReplacement(localX, localZ, relativeY, BEDROCK);
                    }
                }
            }
        }
        return plan;
    }

    private static boolean isInternalBedrock(
            int relativeY,
            int column,
            StrataNoise noise,
            boolean[] chanceIn2,
            int[] chanceIndex) {
        double firstWidth = Math.max(noise.firstA[column], noise.firstB[column]);
        firstWidth += positiveContribution(noise.firstABump[column], 0.25);
        firstWidth += positiveContribution(noise.firstBBump[column], 0.125);
        firstWidth += positiveContribution(noise.firstCBump[column], 0.125);
        if (noise.fourthBump[column] > 0.0) {
            firstWidth += noise.fourthBump[column] * 0.09375 + 0.125;
        }
        if (firstWidth > 0.0 && relativeY - 3 <= firstWidth * 7.0) return true;

        if (isWithinSecondaryStratum(
                relativeY - 32,
                noise.second[column] - firstWidth * 1.5,
                noise.secondBump[column],
                chanceIn2,
                chanceIndex)) {
            return true;
        }
        if (isWithinSecondaryStratum(
                relativeY - 72,
                noise.third[column] - noise.fourth[column] * 0.375 + 0.5,
                noise.thirdBump[column],
                chanceIn2,
                chanceIndex)) {
            return true;
        }
        return isWithinSecondaryStratum(
                relativeY - 96,
                noise.fourth[column] - noise.third[column] * 0.375 + 0.5,
                noise.fourthBump[column],
                chanceIn2,
                chanceIndex);
    }

    private static boolean isWithinSecondaryStratum(
            int distanceFromCenter,
            double width,
            double bump,
            boolean[] chanceIn2,
            int[] chanceIndex) {
        if (width <= 0.0) return false;
        if (distanceFromCenter > 0 && bump > 0.0) {
            width += bump * 0.25 + 0.25;
        }
        if (distanceFromCenter < 0) {
            chanceIndex[0]++;
            if (chanceIn2[chanceIndex[0] & 32767]) distanceFromCenter++;
            distanceFromCenter = -distanceFromCenter;
        }
        return distanceFromCenter <= width * 2.0;
    }

    private static double positiveContribution(double value, double scale) {
        return value > 0.0 ? value * scale : 0.0;
    }

    private static long miteHashedWorldSeed(long worldSeed) {
        Random random = new Random(worldSeed);
        random.nextInt();
        return random.nextLong();
    }

    private static int miteChunkHash(int chunkX, int chunkZ) {
        int hash = 17;
        hash = hash * 31 + chunkX;
        return hash * 31 + chunkZ;
    }

    private static int columnIndex(int localX, int localZ) {
        return localZ + localX * CHUNK_SIDE_LENGTH;
    }

    static final class StrataPlan {
        private final byte[] replacements = new byte[LOWER_STRATA_CELL_COUNT];
        private final byte[] boundaryThicknesses = new byte[COLUMN_COUNT];

        int boundaryThicknessAt(int localX, int localZ) {
            return this.boundaryThicknesses[columnIndex(localX, localZ)];
        }

        boolean hasMantleAt(int localX, int localZ, int relativeY) {
            return this.replacementAt(localX, localZ, relativeY) == MANTLE;
        }

        boolean hasBedrockAt(int localX, int localZ, int relativeY) {
            return this.replacementAt(localX, localZ, relativeY) == BEDROCK;
        }

        private byte replacementAt(int localX, int localZ, int relativeY) {
            return this.replacements[replacementIndex(localX, localZ, relativeY)];
        }

        private void setBoundaryThickness(int localX, int localZ, int thickness) {
            this.boundaryThicknesses[columnIndex(localX, localZ)] = (byte) thickness;
        }

        private void setReplacement(int localX, int localZ, int relativeY, byte replacement) {
            this.replacements[replacementIndex(localX, localZ, relativeY)] = replacement;
        }

        private static int replacementIndex(int localX, int localZ, int relativeY) {
            return (columnIndex(localX, localZ) * LEGACY_TERRAIN_START_Y) + relativeY;
        }
    }

    private static final class LegacyNoiseSet {
        private final LegacyOctaveNoise firstA;
        private final LegacyOctaveNoise firstB;
        private final LegacyOctaveNoise second;
        private final LegacyOctaveNoise third;
        private final LegacyOctaveNoise fourth;
        private final LegacyOctaveNoise firstABump;
        private final LegacyOctaveNoise firstBBump;
        private final LegacyOctaveNoise firstCBump;
        private final LegacyOctaveNoise secondBump;
        private final LegacyOctaveNoise thirdBump;
        private final LegacyOctaveNoise fourthBump;
        private final boolean[] chanceIn2;

        private LegacyNoiseSet(long worldSeed) {
            Random random = new Random(worldSeed);
            // MITE constructs these seven unrelated Nether octave stacks before the strata stacks.
            consumeLegacyOctaves(random, 16);
            consumeLegacyOctaves(random, 16);
            consumeLegacyOctaves(random, 8);
            consumeLegacyOctaves(random, 4);
            consumeLegacyOctaves(random, 4);
            consumeLegacyOctaves(random, 10);
            consumeLegacyOctaves(random, 16);
            this.firstA = new LegacyOctaveNoise(random, 4);
            this.firstB = new LegacyOctaveNoise(random, 4);
            this.second = new LegacyOctaveNoise(random, 4);
            this.third = new LegacyOctaveNoise(random, 4);
            this.fourth = new LegacyOctaveNoise(random, 4);
            this.firstABump = new LegacyOctaveNoise(random, 4);
            this.firstBBump = new LegacyOctaveNoise(random, 4);
            this.firstCBump = new LegacyOctaveNoise(random, 4);
            this.secondBump = new LegacyOctaveNoise(random, 4);
            this.thirdBump = new LegacyOctaveNoise(random, 4);
            this.fourthBump = new LegacyOctaveNoise(random, 4);
            this.chanceIn2 = createChanceIn2(worldSeed);
        }

        private StrataNoise sample(ChunkPos chunkPos) {
            int blockX = chunkPos.getMinBlockX();
            int blockZ = chunkPos.getMinBlockZ();
            return new StrataNoise(
                    this.firstA.sample(blockX, blockZ, 0.03125),
                    this.firstB.sample(blockX, blockZ, 0.03125),
                    this.second.sample(blockX, blockZ, 0.0625),
                    this.third.sample(blockX, blockZ, 0.0625),
                    this.fourth.sample(blockX, blockZ, 0.0625),
                    this.firstABump.sample(blockX, blockZ, 0.125),
                    this.firstBBump.sample(blockX, blockZ, 0.25),
                    this.firstCBump.sample(blockX, blockZ, 0.5),
                    this.secondBump.sample(blockX, blockZ, 1.0),
                    this.thirdBump.sample(blockX, blockZ, 1.0),
                    this.fourthBump.sample(blockX, blockZ, 1.0));
        }

        private static void consumeLegacyOctaves(Random random, int octaveCount) {
            for (int octave = 0; octave < octaveCount; octave++) {
                random.nextDouble();
                random.nextDouble();
                random.nextDouble();
                for (int index = 0; index < 256; index++) {
                    random.nextInt(256 - index);
                }
            }
        }

        private static boolean[] createChanceIn2(long worldSeed) {
            boolean[] chanceIn2 = new boolean[32768];
            Random random = new Random(worldSeed);
            for (int index = 0; index < chanceIn2.length; index++) {
                int randomA = random.nextInt() & Integer.MAX_VALUE;
                random.nextInt();
                random.nextFloat();
                random.nextFloat();
                random.nextDouble();
                chanceIn2[index] = (randomA & 1) == 0;
            }
            return chanceIn2;
        }
    }

    private record StrataNoise(
            double[] firstA,
            double[] firstB,
            double[] second,
            double[] third,
            double[] fourth,
            double[] firstABump,
            double[] firstBBump,
            double[] firstCBump,
            double[] secondBump,
            double[] thirdBump,
            double[] fourthBump) {}

    private static final class LegacyOctaveNoise {
        private final LegacyPerlinNoise[] octaves;

        private LegacyOctaveNoise(Random random, int octaveCount) {
            this.octaves = new LegacyPerlinNoise[octaveCount];
            for (int octave = 0; octave < octaveCount; octave++) {
                this.octaves[octave] = new LegacyPerlinNoise(random);
            }
        }

        private double[] sample(int blockX, int blockZ, double horizontalScale) {
            double[] values = new double[COLUMN_COUNT];
            double frequency = 1.0;
            for (LegacyPerlinNoise octave : this.octaves) {
                double x = blockX * frequency * horizontalScale;
                double z = blockZ * frequency * horizontalScale;
                long xFloor = (long) Math.floor(x);
                long zFloor = (long) Math.floor(z);
                x -= xFloor;
                z -= zFloor;
                x += xFloor % 16777216L;
                z += zFloor % 16777216L;
                octave.add2d(
                        values,
                        x,
                        z,
                        horizontalScale * frequency,
                        horizontalScale * frequency,
                        frequency);
                frequency /= 2.0;
            }
            return values;
        }
    }

    private static final class LegacyPerlinNoise {
        private final int[] permutations = new int[512];
        private final double xOffset;
        private final double zOffset;

        private LegacyPerlinNoise(Random random) {
            this.xOffset = random.nextDouble() * 256.0;
            random.nextDouble();
            this.zOffset = random.nextDouble() * 256.0;
            for (int index = 0; index < 256; index++) {
                this.permutations[index] = index;
            }
            for (int index = 0; index < 256; index++) {
                int swapIndex = random.nextInt(256 - index) + index;
                int value = this.permutations[index];
                this.permutations[index] = this.permutations[swapIndex];
                this.permutations[swapIndex] = value;
                this.permutations[index + 256] = this.permutations[index];
            }
        }

        private void add2d(
                double[] values,
                double originX,
                double originZ,
                double xScale,
                double zScale,
                double noiseScale) {
            int index = 0;
            double amplitude = 1.0 / noiseScale;
            for (int xIndex = 0; xIndex < CHUNK_SIDE_LENGTH; xIndex++) {
                double x = originX + xIndex * xScale + this.xOffset;
                int xFloor = floor(x);
                int xPermutation = xFloor & 255;
                x -= xFloor;
                double xFade = fade(x);
                for (int zIndex = 0; zIndex < CHUNK_SIDE_LENGTH; zIndex++) {
                    double z = originZ + zIndex * zScale + this.zOffset;
                    int zFloor = floor(z);
                    int zPermutation = zFloor & 255;
                    z -= zFloor;
                    double zFade = fade(z);
                    int hashX0 = this.permutations[xPermutation];
                    int hashX0Z0 = this.permutations[hashX0] + zPermutation;
                    int hashX1 = this.permutations[xPermutation + 1];
                    int hashX1Z0 = this.permutations[hashX1] + zPermutation;
                    double lower = lerp(
                            xFade,
                            grad2d(this.permutations[hashX0Z0], x, z),
                            grad3d(this.permutations[hashX1Z0], x - 1.0, 0.0, z));
                    double upper = lerp(
                            xFade,
                            grad3d(this.permutations[hashX0Z0 + 1], x, 0.0, z - 1.0),
                            grad3d(this.permutations[hashX1Z0 + 1], x - 1.0, 0.0, z - 1.0));
                    values[index++] += lerp(zFade, lower, upper) * amplitude;
                }
            }
        }

        private static int floor(double value) {
            int floor = (int) value;
            return value < floor ? floor - 1 : floor;
        }

        private static double fade(double value) {
            return value * value * value * (value * (value * 6.0 - 15.0) + 10.0);
        }

        private static double lerp(double amount, double start, double end) {
            return start + amount * (end - start);
        }

        private static double grad2d(int hash, double x, double z) {
            int gradient = hash & 15;
            double first = (1 - ((gradient & 8) >> 3)) * x;
            double second = gradient < 4 ? 0.0 : (gradient != 12 && gradient != 14 ? z : x);
            return ((gradient & 1) == 0 ? first : -first) + ((gradient & 2) == 0 ? second : -second);
        }

        private static double grad3d(int hash, double x, double y, double z) {
            int gradient = hash & 15;
            double first = gradient < 8 ? x : y;
            double second = gradient < 4 ? y : (gradient != 12 && gradient != 14 ? z : x);
            return ((gradient & 1) == 0 ? first : -first) + ((gradient & 2) == 0 ? second : -second);
        }
    }
}
