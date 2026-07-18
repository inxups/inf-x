package com.pixulse.infx.registry;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.loot.GravelLootModifier;
import com.pixulse.infx.loot.GlassShardLootModifier;
import com.pixulse.infx.loot.UnderworldDungeonLootModifier;
import com.pixulse.infx.loot.ModernProgressionLootFilter;

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
    public static final DeferredHolder<
                    MapCodec<? extends IGlobalLootModifier>, MapCodec<GlassShardLootModifier>>
            GLASS_SHARDS = SERIALIZERS.register("glass_shards", () -> GlassShardLootModifier.CODEC);
    public static final DeferredHolder<
                    MapCodec<? extends IGlobalLootModifier>, MapCodec<UnderworldDungeonLootModifier>>
            UNDERWORLD_DUNGEON =
                    SERIALIZERS.register("underworld_dungeon", () -> UnderworldDungeonLootModifier.CODEC);
    public static final DeferredHolder<
                    MapCodec<? extends IGlobalLootModifier>, MapCodec<ModernProgressionLootFilter>>
            MODERN_PROGRESSION_FILTER = SERIALIZERS.register(
                    "modern_progression_filter", () -> ModernProgressionLootFilter.CODEC);

    private ModLootModifiers() {}

    public static void register(IEventBus modBus) {
        SERIALIZERS.register(modBus);
    }
}
