package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.world.InfXUnderworldLargeCaveCarver;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registers custom carvers used by InfiniteX world generation. */
public final class InfXCarvers {
    private static final DeferredRegister<WorldCarver<?>> CARVERS =
            DeferredRegister.create(Registries.CARVER, InfiniteX.MOD_ID);

    public static final DeferredHolder<WorldCarver<?>, InfXUnderworldLargeCaveCarver> UNDERWORLD_LARGE_CAVE =
            CARVERS.register("underworld_large_cave", InfXUnderworldLargeCaveCarver::new);

    private InfXCarvers() {}

    public static void register(IEventBus modBus) {
        CARVERS.register(modBus);
    }
}
