package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.world.LargeCaveCarver;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class InfinityXWorldCarvers {
    private static final DeferredRegister<WorldCarver<?>> CARVERS =
            DeferredRegister.create(Registries.CARVER, InfiniteX.MOD_ID);

    public static final DeferredHolder<WorldCarver<?>, LargeCaveCarver> LARGE_CAVE =
            CARVERS.register("large_cave", LargeCaveCarver::new);

    private InfinityXWorldCarvers() {}

    public static void register(IEventBus modBus) {
        CARVERS.register(modBus);
    }
}
