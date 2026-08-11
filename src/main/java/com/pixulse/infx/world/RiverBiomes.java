package com.pixulse.infx.world;

import com.mojang.datafixers.util.Pair;
import com.pixulse.infx.InfiniteX;
import java.util.ArrayList;
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

/** Biome-based routing for desert, jungle and swamp river variants. */
public final class RiverBiomes {
    public static final ResourceKey<Biome> DESERT_RIVER = key("desert_river");
    public static final ResourceKey<Biome> JUNGLE_RIVER = key("jungle_river");
    public static final ResourceKey<Biome> SWAMP_RIVER = key("swamp_river");

    /** Vanilla valley weirdness slice, matching OverworldBiomeBuilder.addValleys. */
    private static final float VALLEY_WEIRDNESS = 0.05F;
    /** Vanilla temperature cell boundaries, matching OverworldBiomeBuilder.temperatures[1..4]. */
    private static final float[] TEMPERATURE_CELLS = {-0.45F, -0.15F, 0.2F, 0.55F, 1.0F};
    /** Vanilla humidity cell boundaries, matching OverworldBiomeBuilder.humidities. */
    private static final float[] HUMIDITY_CELLS = {-1.0F, -0.35F, -0.1F, 0.1F, 0.3F, 1.0F};

    private RiverBiomes() {}

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME, InfiniteX.id(name));
    }

    public static MultiNoiseBiomeSource createSource(HolderGetter<Biome> biomes) {
        List<Pair<Climate.ParameterPoint, Holder<Biome>>> values =
                new MultiNoiseBiomeSourceParameterList(MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD, biomes)
                        .parameters()
                        .values()
                        .stream()
                        .filter(pair -> !isRemovedFromOverworld(pair.getSecond()))
                        .flatMap(pair -> replaceRiver(pair.getFirst(), pair.getSecond(), biomes))
                        .toList();
        return MultiNoiseBiomeSource.createFromList(new Climate.ParameterList<>(values));
    }

    static boolean isRemovedFromOverworld(Holder<Biome> biome) {
        return biome.is(Biomes.DEEP_DARK) || biome.is(Biomes.MUSHROOM_FIELDS);
    }

    static Stream<Pair<Climate.ParameterPoint, Holder<Biome>>> replaceRiver(
            Climate.ParameterPoint point, Holder<Biome> original, HolderGetter<Biome> biomes) {
        ResourceKey<Biome> biome = original.unwrapKey().orElse(null);
        if (Biomes.RIVER.equals(biome)) {
            return splitRiverCells(point).stream().map(pair -> pair.mapSecond(biomes::getOrThrow));
        }
        ResourceKey<Biome> replacement = replacementFor(point, biome);
        if (replacement != null) {
            return Stream.of(Pair.of(point, biomes.getOrThrow(replacement)));
        }
        return Stream.of(Pair.of(point, original));
    }

    static boolean isValleySlice(Climate.ParameterPoint point) {
        // Climate parameters are stored quantized (x10000), so compare quantized bounds.
        return point.weirdness().min() == Climate.quantizeCoord(-VALLEY_WEIRDNESS)
                && point.weirdness().max() == Climate.quantizeCoord(VALLEY_WEIRDNESS);
    }

    /**
     * Vanilla swamps are valley biomes; the valley floor inside their region becomes the
     * swamp river, while swamp biomes keep generating on the other weirdness slices.
     */
    static ResourceKey<Biome> replacementFor(Climate.ParameterPoint point, ResourceKey<Biome> biome) {
        if (isValleySlice(point) && (Biomes.SWAMP.equals(biome) || Biomes.MANGROVE_SWAMP.equals(biome))) {
            return SWAMP_RIVER;
        }
        return null;
    }

    /**
     * The vanilla preset assigns every non-frozen river to one broad temperature/humidity
     * point. Split that point on the vanilla temperature/humidity cell grid so the river
     * variant follows the biome that generates at that cell: the desert cell yields the
     * desert river, the jungle cells yield the jungle river, and every other cell keeps
     * the vanilla river.
     */
    static List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> splitRiverCells(Climate.ParameterPoint point) {
        List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> result = new ArrayList<>(20);
        for (int temperatureIndex = 0; temperatureIndex < TEMPERATURE_CELLS.length - 1; temperatureIndex++) {
            Climate.Parameter temperature =
                    Climate.Parameter.span(TEMPERATURE_CELLS[temperatureIndex], TEMPERATURE_CELLS[temperatureIndex + 1]);
            for (int humidityIndex = 0; humidityIndex < HUMIDITY_CELLS.length - 1; humidityIndex++) {
                ResourceKey<Biome> variant =
                        riverVariantForCell(temperatureIndex, humidityIndex, Biomes.RIVER);
                result.add(Pair.of(
                        withClimate(
                                point,
                                temperature,
                                Climate.Parameter.span(HUMIDITY_CELLS[humidityIndex], HUMIDITY_CELLS[humidityIndex + 1])),
                        variant));
            }
        }
        return List.copyOf(result);
    }

    static ResourceKey<Biome> riverVariantForCell(int temperatureIndex, int humidityIndex, ResourceKey<Biome> fallback) {
        // Temperature cell 3 is the desert cell (0.55..1.0); desert spans all humidity there.
        if (temperatureIndex == 3) return DESERT_RIVER;
        // Temperature cell 2 (0.2..0.55) with humidity cells 3-4 (0.1..1.0) is the jungle cell.
        if (temperatureIndex == 2 && humidityIndex >= 3) return JUNGLE_RIVER;
        return fallback;
    }

    private static Climate.ParameterPoint withClimate(
            Climate.ParameterPoint original, Climate.Parameter temperature, Climate.Parameter humidity) {
        return new Climate.ParameterPoint(
                temperature,
                humidity,
                original.continentalness(),
                original.erosion(),
                original.depth(),
                original.weirdness(),
                original.offset());
    }

    /** Mirrors the cell classification of {@link #splitRiverCells} without a parameter point. */
    public static ResourceKey<Biome> select(float temperature, float humidity) {
        if (temperature >= 0.55F) return DESERT_RIVER;
        if (temperature >= 0.2F && humidity >= 0.1F) return JUNGLE_RIVER;
        return null;
    }
}
