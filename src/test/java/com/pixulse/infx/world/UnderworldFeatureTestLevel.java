package com.pixulse.infx.world;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.dimension.DimensionType;

/** Minimal in-memory world-generation level for feature behavior tests. */
final class UnderworldFeatureTestLevel {
    private static final BlockState DEFAULT_STATE = Blocks.STONE.defaultBlockState();
    private static final Holder<Biome> TEST_BIOME = Holder.direct(
            new Biome.BiomeBuilder()
                    .hasPrecipitation(false)
                    .temperature(0.5F)
                    .downfall(0.0F)
                    .specialEffects(new BiomeSpecialEffects.Builder().waterColor(4_159_204).build())
                    .mobSpawnSettings(MobSpawnSettings.EMPTY)
                    .generationSettings(BiomeGenerationSettings.EMPTY)
                    .build());
    /** Overworld-like dimension used only for {@link DimensionType#hasSkyLight}/{@code hasCeiling} reads. */
    private static final DimensionType TEST_DIMENSION_TYPE = new DimensionType(
            false,
            true,
            false,
            false,
            1.0,
            -64,
            384,
            384,
            BlockTags.INFINIBURN_OVERWORLD,
            0.0F,
            new DimensionType.MonsterSettings(UniformInt.of(0, 7), 0),
            DimensionType.Skybox.OVERWORLD,
            CardinalLighting.Type.DEFAULT,
            EnvironmentAttributeMap.EMPTY,
            HolderSet.empty(),
            Optional.empty());

    private final long seed;
    private final Map<BlockPos, BlockState> blocks = new HashMap<>();
    private final List<BlockPos> scheduledTicks = new ArrayList<>();
    private final WorldGenLevel world = (WorldGenLevel) Proxy.newProxyInstance(
            WorldGenLevel.class.getClassLoader(), new Class<?>[] {WorldGenLevel.class}, this::invoke);

    UnderworldFeatureTestLevel(long seed) {
        this.seed = seed;
    }

    WorldGenLevel world() {
        return this.world;
    }

    BlockState blockAt(BlockPos pos) {
        return this.blocks.getOrDefault(pos.immutable(), DEFAULT_STATE);
    }

    void setRaw(BlockPos pos, BlockState state) {
        this.blocks.put(pos.immutable(), state);
    }

    boolean wasScheduled(BlockPos pos) {
        return this.scheduledTicks.contains(pos.immutable());
    }

    private Object invoke(Object proxy, java.lang.reflect.Method method, Object[] arguments) {
        return switch (method.getName()) {
            case "getBlockState" -> this.blockAt((BlockPos) arguments[0]);
            case "isEmptyBlock" -> this.blockAt((BlockPos) arguments[0]).isAir();
            case "setBlock" -> {
                this.setRaw((BlockPos) arguments[0], (BlockState) arguments[1]);
                yield true;
            }
            case "scheduleTick" -> {
                this.scheduledTicks.add(((BlockPos) arguments[0]).immutable());
                yield null;
            }
            case "getBiome" -> throw new AssertionError(
                    "World-generation features must use uncached biome sampling");
            case "getUncachedNoiseBiome" -> TEST_BIOME;
            case "getSeed" -> this.seed;
            case "getMinY" -> Underworld.MIN_Y;
            case "getMaxY" -> Underworld.MAX_Y_EXCLUSIVE;
            case "getHeight" -> Underworld.MAX_Y_EXCLUSIVE - 1; // uniform stone terrain covers every column
            case "dimensionType" -> TEST_DIMENSION_TYPE;
            case "ensureCanWrite" -> true;
            case "toString" -> "UnderworldFeatureTestLevel";
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == arguments[0];
            default -> defaultValue(method.getReturnType());
        };
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0.0F;
        }
        if (type == double.class) {
            return 0.0D;
        }
        if (type == char.class) {
            return '\0';
        }
        return null;
    }
}
