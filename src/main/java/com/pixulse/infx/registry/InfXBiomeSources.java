package com.pixulse.infx.registry;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.world.InfXUnderworldBiomeSource;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registers biome source codecs used by InfiniteX dimensions. */
public final class InfXBiomeSources {
    private static final DeferredRegister<MapCodec<? extends BiomeSource>> TYPES =
            DeferredRegister.create(Registries.BIOME_SOURCE, InfiniteX.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends BiomeSource>, MapCodec<InfXUnderworldBiomeSource>>
            UNDERWORLD = TYPES.register(
                    "underworld_biome_source",
                    () -> InfXUnderworldBiomeSource.CODEC);

    private InfXBiomeSources() {}

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }
}
