package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.world.InfXUnderworldBrownMushroomFeature;
import com.pixulse.infx.world.InfXUnderworldDungeonFeature;
import com.pixulse.infx.world.InfXUnderworldLiquidSourceFeature;
import com.pixulse.infx.world.InfXUnderworldMyceliumFeature;
import com.pixulse.infx.world.InfXUnderworldSupportedGravelFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registers world features implemented by InfiniteX. */
public final class InfXFeatures {
    private static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, InfiniteX.MOD_ID);

    public static final DeferredHolder<Feature<?>, InfXUnderworldDungeonFeature> UNDERWORLD_DUNGEON =
            FEATURES.register(
                    "underworld_dungeon",
                    () -> new InfXUnderworldDungeonFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, InfXUnderworldMyceliumFeature> UNDERWORLD_MYCELIUM =
            FEATURES.register(
                    "underworld_mycelium",
                    () -> new InfXUnderworldMyceliumFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, InfXUnderworldBrownMushroomFeature> UNDERWORLD_BROWN_MUSHROOM =
            FEATURES.register(
                    "underworld_brown_mushroom",
                    () -> new InfXUnderworldBrownMushroomFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, InfXUnderworldLiquidSourceFeature> UNDERWORLD_LIQUID_SOURCE =
            FEATURES.register(
                    "underworld_liquid_source",
                    () -> new InfXUnderworldLiquidSourceFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, InfXUnderworldSupportedGravelFeature> UNDERWORLD_SUPPORTED_GRAVEL =
            FEATURES.register(
                    "underworld_supported_gravel",
                    () -> new InfXUnderworldSupportedGravelFeature(OreConfiguration.CODEC));

    private InfXFeatures() {}

    public static void register(IEventBus modBus) {
        FEATURES.register(modBus);
    }
}
