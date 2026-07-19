package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.effect.R196MobEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMobEffects {
    private static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, InfiniteX.MOD_ID);

    public static final DeferredHolder<MobEffect, MobEffect> MALNUTRITION =
            EFFECTS.register("malnutrition", () -> new R196MobEffect(MobEffectCategory.HARMFUL, 0x8B7B3E));
    public static final DeferredHolder<MobEffect, MobEffect> WITCH_CURSE =
            EFFECTS.register("witch_curse", () -> new R196MobEffect(MobEffectCategory.HARMFUL, 0x52265F));
    public static final DeferredHolder<MobEffect, MobEffect> INSULIN_RESISTANCE =
            EFFECTS.register("insulin_resistance", () -> new R196MobEffect(MobEffectCategory.HARMFUL, 0xC28B42));
    public static final DeferredHolder<MobEffect, MobEffect> PARALYSIS =
            EFFECTS.register("paralysis", () -> new R196MobEffect(MobEffectCategory.HARMFUL, 0x59606B)
                    .addAttributeModifier(
                            Attributes.MOVEMENT_SPEED,
                            InfiniteX.id("paralysis_speed"),
                            -1.0D,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    private ModMobEffects() {}

    public static void register(IEventBus modBus) {
        EFFECTS.register(modBus);
    }
}
