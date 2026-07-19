package com.pixulse.infx.world;

import com.mojang.datafixers.util.Pair;
import com.pixulse.infx.InfiniteX;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;

/** Climate routing for desert, jungle and swamp river variants. */
public final class R196RiverBiomes {
    public static final ResourceKey<Biome> DESERT_RIVER = key("desert_river");
    public static final ResourceKey<Biome> JUNGLE_RIVER = key("jungle_river");
    public static final ResourceKey<Biome> SWAMP_RIVER = key("swamp_river");

    private R196RiverBiomes() {}

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME, InfiniteX.id(name));
    }

    public static MultiNoiseBiomeSource createSource(HolderGetter<Biome> biomes) {
        List<Pair<Climate.ParameterPoint, Holder<Biome>>> values =
                new MultiNoiseBiomeSourceParameterList(MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD, biomes)
                        .parameters()
                        .values()
                        .stream()
                        .flatMap(pair -> replaceRiver(pair.getFirst(), pair.getSecond(), biomes))
                        .toList();
        return MultiNoiseBiomeSource.createFromList(new Climate.ParameterList<>(values));
    }

    private static Stream<Pair<Climate.ParameterPoint, Holder<Biome>>> replaceRiver(
            Climate.ParameterPoint point, Holder<Biome> original, HolderGetter<Biome> biomes) {
        if (!original.is(Biomes.RIVER.identifier())) return Stream.of(Pair.of(point, original));

        // The modern Overworld preset collapses non-frozen rivers into one broad
        // temperature/humidity point. Split that point so the three R196 river
        // biomes are reachable from the actual climate sampler rather than only
        // existing as unused registry entries.
        return Stream.of(
                Pair.of(withClimate(point, .55F, 1.0F, -1.0F, .1999F), biomes.getOrThrow(DESERT_RIVER)),
                Pair.of(withClimate(point, .15F, 1.0F, .20F, 1.0F), biomes.getOrThrow(JUNGLE_RIVER)),
                Pair.of(withClimate(point, -.45F, .1499F, .20F, 1.0F), biomes.getOrThrow(SWAMP_RIVER)),
                Pair.of(withClimate(point, -.45F, .5499F, -1.0F, .1999F), original));
    }

    private static Climate.ParameterPoint withClimate(
            Climate.ParameterPoint original,
            float minimumTemperature,
            float maximumTemperature,
            float minimumHumidity,
            float maximumHumidity) {
        return new Climate.ParameterPoint(
                Climate.Parameter.span(minimumTemperature, maximumTemperature),
                Climate.Parameter.span(minimumHumidity, maximumHumidity),
                original.continentalness(),
                original.erosion(),
                original.depth(),
                original.weirdness(),
                original.offset());
    }

    public static ResourceKey<Biome> select(float temperature, float humidity) {
        if (temperature >= 0.55F && humidity < 0.20F) return DESERT_RIVER;
        if (temperature >= 0.15F && humidity >= 0.20F) return JUNGLE_RIVER;
        if (humidity >= 0.20F) return SWAMP_RIVER;
        return null;
    }
}
