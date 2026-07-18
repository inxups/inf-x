package com.pixulse.infx.registry;

import com.mojang.serialization.Codec;
import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.material.R196Quality;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, InfiniteX.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> NOCKED_ARROW_MATERIAL =
            COMPONENTS.registerComponentType("nocked_arrow_material", builder -> builder
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .cacheEncoding());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<R196Quality>> QUALITY =
            COMPONENTS.registerComponentType("quality", builder -> builder
                    .persistent(R196Quality.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(R196Quality.CODEC))
                    .cacheEncoding());

    private ModDataComponents() {}

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
