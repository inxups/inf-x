package com.pixulse.infx.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.level.Level;

/** Explosive, venomous and paralyzing R196 silverfish variants. */
public final class R196Silverfish extends Silverfish implements R196Mob {
    public enum Variant {
        NETHERSPAWN,
        COPPERSPINE,
        HOARY
    }

    private boolean exploded;

    public R196Silverfish(EntityType<? extends Silverfish> type, Level level) {
        super(type, level);
    }

    public Variant variant() {
        return switch (R196EntityVariant.path(this)) {
            case "copperspine" -> Variant.COPPERSPINE;
            case "hoary_silverfish" -> Variant.HOARY;
            default -> Variant.NETHERSPAWN;
        };
    }

    public static AttributeSupplier.Builder attributes() {
        return Silverfish.createAttributes()
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.MOVEMENT_SPEED, 0.60)
                .add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hurt = super.doHurtTarget(level, target);
        if (hurt && target instanceof LivingEntity living) {
            if (variant() == Variant.COPPERSPINE) {
                living.addEffect(new MobEffectInstance(MobEffects.POISON, 480, 0), this);
            } else if (variant() == Variant.HOARY) {
                living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 5), this);
                living.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 50, 3), this);
            }
        }
        return hurt;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (variant() == Variant.NETHERSPAWN && level() instanceof ServerLevel level
                && isInWaterOrRain() && tickCount % 40 == 0) {
            hurtServer(level, damageSources().drown(), 1.0F);
        }
    }

    @Override
    public void die(DamageSource source) {
        if (!exploded && variant() == Variant.NETHERSPAWN && level() instanceof ServerLevel level
                && !isInWaterOrRain() && !(source.getDirectEntity() instanceof Snowball)) {
            exploded = true;
            level.explode(this, getX(), getY() + getBbHeight() * 0.25, getZ(), 1.0F, Level.ExplosionInteraction.MOB);
        }
        super.die(source);
    }
}
