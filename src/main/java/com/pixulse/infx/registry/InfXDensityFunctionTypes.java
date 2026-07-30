package com.pixulse.infx.registry;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.world.InfXShiftedYDensityFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class InfXDensityFunctionTypes {
    private static final DeferredRegister<MapCodec<? extends DensityFunction>> TYPES =
            DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, InfiniteX.MOD_ID);

    public static final DeferredHolder<
                    MapCodec<? extends DensityFunction>, MapCodec<InfXShiftedYDensityFunction>>
            SHIFTED_Y = TYPES.register("shifted_y", () -> InfXShiftedYDensityFunction.CODEC.codec());

    private InfXDensityFunctionTypes() {}

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }
}
