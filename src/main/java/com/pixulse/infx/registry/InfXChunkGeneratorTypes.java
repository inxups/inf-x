package com.pixulse.infx.registry;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.world.InfXUnderworldChunkGenerator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registers the generator codec used only by InfiniteX's Underworld dimension. */
public final class InfXChunkGeneratorTypes {
    private static final DeferredRegister<MapCodec<? extends ChunkGenerator>> TYPES =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, InfiniteX.MOD_ID);

    public static final DeferredHolder<
                    MapCodec<? extends ChunkGenerator>, MapCodec<InfXUnderworldChunkGenerator>>
            UNDERWORLD = TYPES.register("underworld", () -> InfXUnderworldChunkGenerator.CODEC);

    private InfXChunkGeneratorTypes() {}

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }
}
