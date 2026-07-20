package com.pixulse.infx.registry;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.world.R196SpawnsBiomeModifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Codecs for data-driven biome modifications that need atomic replacement semantics. */
public final class ModBiomeModifiers {
    private static final DeferredRegister<MapCodec<? extends BiomeModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, InfiniteX.MOD_ID);

    public static final DeferredHolder<
                    MapCodec<? extends BiomeModifier>, MapCodec<R196SpawnsBiomeModifier>>
            R196_SPAWNS = SERIALIZERS.register("r196_spawns", () -> R196SpawnsBiomeModifier.CODEC);

    private ModBiomeModifiers() {}

    public static void register(IEventBus modBus) {
        SERIALIZERS.register(modBus);
    }
}
