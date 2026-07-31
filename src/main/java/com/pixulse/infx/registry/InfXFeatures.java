package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.world.InfXUnderworldDungeonFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
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

    private InfXFeatures() {}

    public static void register(IEventBus modBus) {
        FEATURES.register(modBus);
    }
}
