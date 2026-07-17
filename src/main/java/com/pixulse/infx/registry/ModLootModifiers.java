package com.pixulse.infx.registry;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.loot.GravelLootModifier;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModLootModifiers {
    private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, InfiniteX.MOD_ID);

    public static final DeferredHolder<
                    MapCodec<? extends IGlobalLootModifier>, MapCodec<GravelLootModifier>>
            GRAVEL = SERIALIZERS.register("gravel", () -> GravelLootModifier.CODEC);

    private ModLootModifiers() {}

    public static void register(IEventBus modBus) {
        SERIALIZERS.register(modBus);
    }
}
