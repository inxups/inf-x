package com.pixulse.infx.world;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.config.InfXConfig;
import com.pixulse.infx.registry.InfXBiomeModifiers;
import com.pixulse.infx.registry.InfXEntityTypes;
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
import org.jspecify.annotations.NonNull;

/** Atomically replaces modern biome spawn lists with the INFX ecology. */
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

    /**
     * Vanilla Overworld spawn entries InfX re-adds with its own weights. These are the only entries
     * removed (instead of wiping the whole table) so third-party spawn-table entries survive; every
     * InfX-owned type is {@code INFX_*} and never collides.
     */
    private static final Set<EntityType<?>> OVERWORLD_MONSTER_REMOVED = Set.of(
            EntityType.SPIDER, EntityType.ZOMBIE, EntityType.SKELETON,
            EntityType.CREEPER, EntityType.SLIME, EntityType.ENDERMAN);

    /**
     * Vanilla farm/companion animals InfX replaces with {@code INFX_*} equivalents, plus donkey
     * which is dropped by the horse rewrite and the mushroom-field mooshroom the ecology clears.
     * Their vanilla CREATURE entries must go or the herd (or mushroom-isle wildlife) would double.
     */
    private static final Set<EntityType<?>> OVERWORLD_CREATURE_REMOVED = Set.of(
            EntityType.SHEEP, EntityType.PIG, EntityType.CHICKEN, EntityType.COW,
            EntityType.HORSE, EntityType.DONKEY, EntityType.WOLF, EntityType.OCELOT,
            EntityType.MOOSHROOM);

    /** Vanilla fish InfX replaces with {@code INFX_*} equivalents per biome habitat. */
    private static final Set<EntityType<?>> OVERWORLD_WATER_AMBIENT_REMOVED = Set.of(
            EntityType.COD, EntityType.SALMON, EntityType.PUFFERFISH, EntityType.TROPICAL_FISH);

    /** Vanilla cave bat InfX replaces with the INFX bat family. */
    private static final Set<EntityType<?>> OVERWORLD_AMBIENT_REMOVED = Set.of(EntityType.BAT);

    @Override
    public void modify(@NonNull Holder<Biome> biome, @NonNull Phase phase, ModifiableBiomeInfo.BiomeInfo.@NonNull Builder builder) {
        if (phase != Phase.MODIFY) return;
        MobSpawnSettingsBuilder spawns = builder.getMobSpawnSettings();
        boolean wipe = InfXConfig.INSTANCE.mobs.wipeOtherSpawnTables.getValue();
        if (biome.is(BiomeTags.IS_OVERWORLD)) {
            if (wipe) {
                clearNaturalSpawns(spawns);
            } else {
                removeSpawnTypes(spawns, MobCategory.MONSTER, OVERWORLD_MONSTER_REMOVED);
                removeSpawnTypes(spawns, MobCategory.CREATURE, OVERWORLD_CREATURE_REMOVED);
                removeSpawnTypes(spawns, MobCategory.WATER_AMBIENT, OVERWORLD_WATER_AMBIENT_REMOVED);
                removeSpawnTypes(spawns, MobCategory.WATER_CREATURE, Set.of(EntityType.SQUID));
                removeSpawnTypes(spawns, MobCategory.AMBIENT, OVERWORLD_AMBIENT_REMOVED);
            }
            addOverworldSpawns(biome, spawns);
        } else if (biome.is(BiomeTags.IS_NETHER)) {
            clearNaturalSpawns(spawns);
            add(spawns, EntityType.GHAST, 50, 1, 2);
            add(spawns, EntityType.ZOMBIFIED_PIGLIN, 100, 1, 4);
            add(spawns, EntityType.MAGMA_CUBE, 10, 4, 4);
            add(spawns, InfXEntityTypes.EARTH_ELEMENTAL.get(), 40, 1, 1);
        } else if (biome.is(BiomeTags.IS_END)) {
            clearNaturalSpawns(spawns);
            add(spawns, EntityType.ENDERMAN, 100, 4, 4);
            add(spawns, InfXEntityTypes.EARTH_ELEMENTAL.get(), 20, 1, 4);
        }
    }

    private static void addOverworldSpawns(Holder<Biome> biome, MobSpawnSettingsBuilder spawns) {
        add(spawns, InfXEntityTypes.INFX_BAT.get(), 100, 8, 8);
        add(spawns, InfXEntityTypes.VAMPIRE_BAT.get(), 20, 8, 8);
        add(spawns, InfXEntityTypes.NIGHTWING.get(), 4, 1, 4);
        if (biome.is(Biomes.MUSHROOM_FIELDS)) return;

        add(spawns, EntityType.SPIDER, 80, 1, 2);
        add(spawns, EntityType.ZOMBIE, 100, 1, 4);
        add(spawns, EntityType.SKELETON, 100, 1, 4);
        add(spawns, EntityType.CREEPER, 100, 1, 2);
        add(spawns, EntityType.SLIME, 100, 1, 4);
        add(spawns, EntityType.ENDERMAN, 10, 1, 4);
        add(spawns, InfXEntityTypes.GHOUL.get(), 10, 1, 1);
        add(spawns, InfXEntityTypes.WIGHT.get(), 10, 1, 1);
        add(spawns, InfXEntityTypes.INVISIBLE_STALKER.get(), 10, 1, 1);
        add(spawns, InfXEntityTypes.DEMON_SPIDER.get(), 10, 1, 1);
        add(spawns, InfXEntityTypes.HELLHOUND.get(), 10, 1, 2);
        add(spawns, InfXEntityTypes.WOOD_SPIDER.get(), 20, 1, 1);
        add(spawns, InfXEntityTypes.SHADOW.get(), 10, 1, 1);
        add(spawns, InfXEntityTypes.REVENANT.get(), 10, 1, 1);
        add(spawns, InfXEntityTypes.EARTH_ELEMENTAL.get(), 10, 1, 1);
        add(spawns, InfXEntityTypes.CLAY_GOLEM.get(), 50, 1, 1);
        add(spawns, InfXEntityTypes.JELLY.get(), 30, 1, 4);
        add(spawns, InfXEntityTypes.BLOB.get(), 30, 1, 4);
        add(spawns, InfXEntityTypes.OOZE.get(), 20, 1, 4);
        add(spawns, InfXEntityTypes.PUDDING.get(), 30, 1, 4);
        add(spawns, InfXEntityTypes.BONE_LORD.get(), 5, 1, 1);
        add(spawns, InfXEntityTypes.PHASE_SPIDER.get(), 40, 1, 4);
        if (isAny(biome, JUNGLES)) add(spawns, InfXEntityTypes.BLACK_WIDOW_SPIDER.get(), 10, 1, 1);
        if (biome.is(Biomes.SWAMP) || biome.is(Biomes.MANGROVE_SWAMP)) {
            add(spawns, EntityType.SLIME, 10, 1, 1);
        }
        add(spawns, EntityType.SQUID, 10, 4, 4);
        addFishSpawns(biome, spawns);

        if (!isAny(biome, ANIMAL_BIOMES)) return;
        add(spawns, InfXEntityTypes.INFX_SHEEP.get(), 10, 1, 1);
        add(spawns, InfXEntityTypes.INFX_PIG.get(), 10, 1, 1);
        add(spawns, InfXEntityTypes.INFX_CHICKEN.get(), 10, 1, 1);
        add(spawns, InfXEntityTypes.INFX_COW.get(), 10, 1, 1);
        if (biome.is(BiomeTags.IS_FOREST) && !isAny(biome, SNOW_BIOMES)) {
            add(spawns, InfXEntityTypes.INFX_WOLF.get(), 10, 1, 3);
        }
        if (biome.is(BiomeTags.IS_TAIGA)) {
            add(spawns, InfXEntityTypes.INFX_WOLF.get(), 10, 1, 3);
            add(spawns, InfXEntityTypes.DIRE_WOLF.get(), 5, 1, 3);
        }
        if (isAny(biome, SNOW_BIOMES)) {
            add(spawns, InfXEntityTypes.INFX_WOLF.get(), 4, 1, 3);
            add(spawns, InfXEntityTypes.DIRE_WOLF.get(), 1, 1, 3);
        }
        if (isAny(biome, PLAINS)) add(spawns, InfXEntityTypes.INFX_HORSE.get(), 5, 1, 2);
        if (isAny(biome, JUNGLES)) {
            add(spawns, InfXEntityTypes.INFX_OCELOT.get(), 10, 1, 1);
            add(spawns, InfXEntityTypes.INFX_CHICKEN.get(), 10, 1, 1);
        }
    }

    /** Mirrors vanilla fish habitats while every natural entry uses the InfiniteX entity type. */
    private static void addFishSpawns(Holder<Biome> biome, MobSpawnSettingsBuilder spawns) {
        if (biome.is(BiomeTags.IS_RIVER)) {
            add(spawns, InfXEntityTypes.INFX_SALMON.get(), 5, 1, 5);
            return;
        }
        if (biome.is(Biomes.LUSH_CAVES) || biome.is(Biomes.MANGROVE_SWAMP)) {
            add(spawns, InfXEntityTypes.INFX_TROPICAL_FISH.get(), 25, 8, 8);
            return;
        }
        if (biome.is(Biomes.WARM_OCEAN)) {
            add(spawns, InfXEntityTypes.INFX_PUFFERFISH.get(), 15, 1, 3);
            add(spawns, InfXEntityTypes.INFX_TROPICAL_FISH.get(), 25, 8, 8);
            return;
        }
        if (isAny(biome, LUKEWARM_OCEANS)) {
            add(spawns, InfXEntityTypes.INFX_COD.get(), biome.is(Biomes.DEEP_LUKEWARM_OCEAN) ? 8 : 15, 3, 6);
            add(spawns, InfXEntityTypes.INFX_PUFFERFISH.get(), 5, 1, 3);
            add(spawns, InfXEntityTypes.INFX_TROPICAL_FISH.get(), 25, 8, 8);
            return;
        }
        if (isAny(biome, COLD_OCEANS)) {
            add(spawns, InfXEntityTypes.INFX_COD.get(), 15, 3, 6);
            add(spawns, InfXEntityTypes.INFX_SALMON.get(), 15, 1, 5);
            return;
        }
        if (isAny(biome, FROZEN_OCEANS)) {
            add(spawns, InfXEntityTypes.INFX_SALMON.get(), 15, 1, 5);
            return;
        }
        if (isAny(biome, NORMAL_OCEANS)) {
            add(spawns, InfXEntityTypes.INFX_COD.get(), 10, 3, 6);
        }
    }

    private static void clearNaturalSpawns(MobSpawnSettingsBuilder spawns) {
        for (MobCategory category : NATURAL_CATEGORIES) {
            spawns.getSpawner(category).removeIf(entry -> true);
        }
    }

    /**
     * Removes only the given entity types from one category, preserving every other entry —
     * including third-party mods' spawn-table additions. Used on the Overworld where InfX re-adds
     * a known vanilla subset instead of atomically replacing the ecology.
     */
    private static void removeSpawnTypes(
            MobSpawnSettingsBuilder spawns, MobCategory category, Set<EntityType<?>> removed) {
        spawns.getSpawner(category)
                .removeIf(entry -> removed.contains(entry.value().type()));
    }

    /**
     * Whether an Overworld spawn-table entry of the given type is one InfX re-adds and therefore
     * removes (when not wiping). Third-party types and vanilla animal/fish/ambient entries survive.
     */
    static boolean isOverworldReplacedType(MobCategory category, EntityType<?> type) {
        if (category == MobCategory.MONSTER) {
            return OVERWORLD_MONSTER_REMOVED.contains(type);
        }
        if (category == MobCategory.CREATURE) {
            return OVERWORLD_CREATURE_REMOVED.contains(type);
        }
        if (category == MobCategory.WATER_AMBIENT) {
            return OVERWORLD_WATER_AMBIENT_REMOVED.contains(type);
        }
        if (category == MobCategory.AMBIENT) {
            return OVERWORLD_AMBIENT_REMOVED.contains(type);
        }
        return category == MobCategory.WATER_CREATURE && type == EntityType.SQUID;
    }

    private static void add(
            MobSpawnSettingsBuilder spawns, EntityType<?> type, int weight, int minimum, int maximum) {
        spawns.addSpawn(type.getCategory(), weight, new MobSpawnSettings.SpawnerData(type, minimum, maximum));
    }

    private static boolean isAny(Holder<Biome> biome, Set<ResourceKey<Biome>> keys) {
        return keys.stream().anyMatch(biome::is);
    }

    @Override
    public @NonNull MapCodec<? extends BiomeModifier> codec() {
        return InfXBiomeModifiers.INFX_SPAWNS.get();
    }
}
