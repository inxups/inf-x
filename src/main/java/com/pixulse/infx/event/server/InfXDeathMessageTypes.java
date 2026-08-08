package com.pixulse.infx.event.server;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DeathMessageType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.common.damagesource.IDeathMessageProvider;

/**
 * Registers InfX's custom poison {@link DeathMessageType} through NeoForge's supported enum
 * extension mechanism ({@code enumextender.json}). The {@code minecraft:magic} damage type is
 * pointed at it by the data-generated {@code data/minecraft/damage_type/magic.json} override, so
 * no mixin is needed to swap the death message of poisoned victims.
 */
public final class InfXDeathMessageTypes {
    private static final IDeathMessageProvider POISON_MESSAGE_PROVIDER =
            (entity, lastEntry, mostSignificantFall) -> {
                DamageSource source = lastEntry.source();
                if (source.is(DamageTypes.MAGIC)
                        && entity.isDeadOrDying()
                        && entity.hasEffect(MobEffects.POISON)) {
                    return Component.translatable("death.infx.poison", entity.getDisplayName());
                }
                return IDeathMessageProvider.DEFAULT.getDeathMessage(
                        entity, lastEntry, mostSignificantFall);
            };

    /** Linked by {@code META-INF/enumextender.json}; must be declared after the provider above. */
    public static final EnumProxy<DeathMessageType> POISON = new EnumProxy<>(
            DeathMessageType.class, "infx:poison", POISON_MESSAGE_PROVIDER);

    private InfXDeathMessageTypes() {}
}
