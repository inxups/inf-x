package com.pixulse.infx.world;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.registry.InfinityXBiomeModifiers;
import com.pixulse.infx.registry.InfinityXEntityTypes;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.MobSpawnSettingsBuilder;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

/** Atomically replaces modern biome spawn lists with the R196 ecology. */
public final class SpawnsBiomeModifier implements BiomeModifier {
    public static final MapCodec<SpawnsBiomeModifier> CODEC = MapCodec.unit(SpawnsBiomeModifier::new);

    private static final Set<ResourceKey<Biome>> ANIMAL_BIOMES = Set.of(
            Biomes.PLAINS,
            Biomes.SUNFLOWER_PLAINS,
            Biomes.FOREST,
            Biomes.FLOWER_FOREST,
            Biomes.BIRCH_FOREST,
            Biomes.OLD_GROWTH_BIRCH_FOREST,
            Biomes.DARK_FOREST,
            Biomes.PALE_GARDEN,
            Biomes.TAIGA,
            Biomes.SNOWY_TAIGA,
            Biomes.OLD_GROWTH_PINE_TAIGA,
            Biomes.OLD_GROWTH_SPRUCE_TAIGA,
            Biomes.JUNGLE,
            Biomes.SPARSE_JUNGLE,
            Biomes.BAMBOO_JUNGLE,
            Biomes.SWAMP,
            Biomes.MANGROVE_SWAMP,
            Biomes.SAVANNA,
            Biomes.SAVANNA_PLATEAU,
            Biomes.WINDSWEPT_SAVANNA,
            Biomes.SNOWY_PLAINS,
            Biomes.ICE_SPIKES,
            Biomes.GROVE,
            Biomes.MEADOW,
            Biomes.CHERRY_GROVE,
            Biomes.WINDSWEPT_HILLS,
            Biomes.WINDSWEPT_FOREST,
            Biomes.WINDSWEPT_GRAVELLY_HILLS,
            Biomes.BADLANDS,
            Biomes.WOODED_BADLANDS,
            Biomes.ERODED_BADLANDS,
            Biomes.JAGGED_PEAKS,
            Biomes.FROZEN_PEAKS,
            Biomes.STONY_PEAKS);
    private static final Set<ResourceKey<Biome>> JUNGLES =
            Set.of(Biomes.JUNGLE, Biomes.SPARSE_JUNGLE, Biomes.BAMBOO_JUNGLE);
    private static final Set<ResourceKey<Biome>> SNOW_BIOMES =
            Set.of(Biomes.SNOWY_PLAINS, Biomes.ICE_SPIKES, Biomes.GROVE);
    private static final Set<ResourceKey<Biome>> PLAINS =
            Set.of(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS);
    private static final Set<ResourceKey<Biome>> NORMAL_OCEANS =
            Set.of(Biomes.OCEAN, Biomes.DEEP_OCEAN);
    private static final Set<ResourceKey<Biome>> LUKEWARM_OCEANS =
            Set.of(Biomes.LUKEWARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN);
    private static final Set<ResourceKey<Biome>> COLD_OCEANS =
            Set.of(Biomes.COLD_OCEAN, Biomes.DEEP_COLD_OCEAN);
    private static final Set<ResourceKey<Biome>> FROZEN_OCEANS =
            Set.of(Biomes.FROZEN_OCEAN, Biomes.DEEP_FROZEN_OCEAN);
    private static final List<MobCategory> NATURAL_CATEGORIES = List.of(
            MobCategory.MONSTER,
            MobCategory.CREATURE,
            MobCategory.AMBIENT,
            MobCategory.AXOLOTLS,
            MobCategory.UNDERGROUND_WATER_CREATURE,
            MobCategory.WATER_CREATURE,
            MobCategory.WATER_AMBIENT);

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.MODIFY) return;
        MobSpawnSettingsBuilder spawns = builder.getMobSpawnSettings();
        if (biome.is(BiomeTags.IS_OVERWORLD)) {
            clearNaturalSpawns(spawns);
            addOverworldSpawns(biome, spawns);
        } else if (biome.is(BiomeTags.IS_NETHER)) {
            clearNaturalSpawns(spawns);
            add(spawns, EntityType.GHAST, 50, 1, 2);
            add(spawns, EntityType.ZOMBIFIED_PIGLIN, 100, 1, 4);
            add(spawns, EntityType.MAGMA_CUBE, 10, 4, 4);
            add(spawns, InfinityXEntityTypes.EARTH_ELEMENTAL.get(), 40, 1, 1);
        } else if (biome.is(BiomeTags.IS_END)) {
            clearNaturalSpawns(spawns);
            add(spawns, EntityType.ENDERMAN, 100, 4, 4);
            add(spawns, InfinityXEntityTypes.EARTH_ELEMENTAL.get(), 20, 1, 4);
        }
    }

    private static void addOverworldSpawns(Holder<Biome> biome, MobSpawnSettingsBuilder spawns) {
        add(spawns, EntityType.BAT, 100, 8, 8);
        add(spawns, InfinityXEntityTypes.VAMPIRE_BAT.get(), 20, 8, 8);
        add(spawns, InfinityXEntityTypes.NIGHTWING.get(), 4, 1, 4);
        if (biome.is(Biomes.MUSHROOM_FIELDS)) return;

        add(spawns, EntityType.SPIDER, 80, 1, 2);
        add(spawns, EntityType.ZOMBIE, 100, 1, 4);
        add(spawns, EntityType.SKELETON, 100, 1, 4);
        add(spawns, EntityType.CREEPER, 100, 1, 2);
        add(spawns, EntityType.SLIME, 100, 1, 4);
        add(spawns, EntityType.ENDERMAN, 10, 1, 4);
        add(spawns, InfinityXEntityTypes.GHOUL.get(), 10, 1, 1);
        add(spawns, InfinityXEntityTypes.WIGHT.get(), 10, 1, 1);
        add(spawns, InfinityXEntityTypes.INVISIBLE_STALKER.get(), 10, 1, 1);
        add(spawns, InfinityXEntityTypes.DEMON_SPIDER.get(), 10, 1, 1);
        add(spawns, InfinityXEntityTypes.HELLHOUND.get(), 10, 1, 2);
        add(spawns, InfinityXEntityTypes.WOOD_SPIDER.get(), 20, 1, 1);
        add(spawns, InfinityXEntityTypes.SHADOW.get(), 10, 1, 1);
        add(spawns, InfinityXEntityTypes.REVENANT.get(), 10, 1, 1);
        add(spawns, InfinityXEntityTypes.EARTH_ELEMENTAL.get(), 10, 1, 1);
        add(spawns, InfinityXEntityTypes.CLAY_GOLEM.get(), 50, 1, 1);
        add(spawns, InfinityXEntityTypes.JELLY.get(), 30, 1, 4);
        add(spawns, InfinityXEntityTypes.BLOB.get(), 30, 1, 4);
        add(spawns, InfinityXEntityTypes.OOZE.get(), 20, 1, 4);
        add(spawns, InfinityXEntityTypes.PUDDING.get(), 30, 1, 4);
        add(spawns, InfinityXEntityTypes.BONE_LORD.get(), 5, 1, 1);
        add(spawns, InfinityXEntityTypes.PHASE_SPIDER.get(), 5, 1, 4);
        if (isAny(biome, JUNGLES)) add(spawns, InfinityXEntityTypes.BLACK_WIDOW_SPIDER.get(), 10, 1, 1);
        if (biome.is(Biomes.SWAMP) || biome.is(Biomes.MANGROVE_SWAMP)) {
            add(spawns, EntityType.SLIME, 10, 1, 1);
        }
        add(spawns, EntityType.SQUID, 10, 4, 4);
        addFishSpawns(biome, spawns);

        if (!isAny(biome, ANIMAL_BIOMES)) return;
        add(spawns, InfinityXEntityTypes.R196_SHEEP.get(), 10, 1, 1);
        add(spawns, InfinityXEntityTypes.R196_PIG.get(), 10, 1, 1);
        add(spawns, InfinityXEntityTypes.R196_CHICKEN.get(), 10, 1, 1);
        add(spawns, InfinityXEntityTypes.R196_COW.get(), 10, 1, 1);
        if (biome.is(BiomeTags.IS_FOREST) && !isAny(biome, SNOW_BIOMES)) {
            add(spawns, InfinityXEntityTypes.R196_WOLF.get(), 10, 1, 3);
        }
        if (biome.is(BiomeTags.IS_TAIGA)) {
            add(spawns, InfinityXEntityTypes.R196_WOLF.get(), 10, 1, 3);
            add(spawns, InfinityXEntityTypes.DIRE_WOLF.get(), 5, 1, 3);
        }
        if (isAny(biome, SNOW_BIOMES)) {
            add(spawns, InfinityXEntityTypes.R196_WOLF.get(), 4, 1, 3);
            add(spawns, InfinityXEntityTypes.DIRE_WOLF.get(), 1, 1, 3);
        }
        if (isAny(biome, PLAINS)) add(spawns, InfinityXEntityTypes.R196_HORSE.get(), 5, 1, 2);
        if (isAny(biome, JUNGLES)) {
            add(spawns, InfinityXEntityTypes.R196_OCELOT.get(), 10, 1, 1);
            add(spawns, InfinityXEntityTypes.R196_CHICKEN.get(), 10, 1, 1);
        }
    }

    /** Mirrors vanilla fish habitats while every natural entry uses the InfiniteX entity type. */
    private static void addFishSpawns(Holder<Biome> biome, MobSpawnSettingsBuilder spawns) {
        if (biome.is(BiomeTags.IS_RIVER)) {
            add(spawns, InfinityXEntityTypes.R196_SALMON.get(), 5, 1, 5);
            return;
        }
        if (biome.is(Biomes.LUSH_CAVES) || biome.is(Biomes.MANGROVE_SWAMP)) {
            add(spawns, InfinityXEntityTypes.R196_TROPICAL_FISH.get(), 25, 8, 8);
            return;
        }
        if (biome.is(Biomes.WARM_OCEAN)) {
            add(spawns, InfinityXEntityTypes.R196_PUFFERFISH.get(), 15, 1, 3);
            add(spawns, InfinityXEntityTypes.R196_TROPICAL_FISH.get(), 25, 8, 8);
            return;
        }
        if (isAny(biome, LUKEWARM_OCEANS)) {
            add(spawns, InfinityXEntityTypes.R196_COD.get(), biome.is(Biomes.DEEP_LUKEWARM_OCEAN) ? 8 : 15, 3, 6);
            add(spawns, InfinityXEntityTypes.R196_PUFFERFISH.get(), 5, 1, 3);
            add(spawns, InfinityXEntityTypes.R196_TROPICAL_FISH.get(), 25, 8, 8);
            return;
        }
        if (isAny(biome, COLD_OCEANS)) {
            add(spawns, InfinityXEntityTypes.R196_COD.get(), 15, 3, 6);
            add(spawns, InfinityXEntityTypes.R196_SALMON.get(), 15, 1, 5);
            return;
        }
        if (isAny(biome, FROZEN_OCEANS)) {
            add(spawns, InfinityXEntityTypes.R196_SALMON.get(), 15, 1, 5);
            return;
        }
        if (isAny(biome, NORMAL_OCEANS)) {
            add(spawns, InfinityXEntityTypes.R196_COD.get(), 10, 3, 6);
        }
    }

    private static void clearNaturalSpawns(MobSpawnSettingsBuilder spawns) {
        for (MobCategory category : NATURAL_CATEGORIES) {
            spawns.getSpawner(category).removeIf(entry -> true);
        }
    }

    private static void add(
            MobSpawnSettingsBuilder spawns, EntityType<?> type, int weight, int minimum, int maximum) {
        spawns.addSpawn(type.getCategory(), weight, new MobSpawnSettings.SpawnerData(type, minimum, maximum));
    }

    private static boolean isAny(Holder<Biome> biome, Set<ResourceKey<Biome>> keys) {
        return keys.stream().anyMatch(biome::is);
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return InfinityXBiomeModifiers.R196_SPAWNS.get();
    }
}
