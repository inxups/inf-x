package com.pixulse.infx.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.level.Level;

/** Vanilla slime replacement and the four corrosive R196 gelatinous cubes. */
public final class R196Slime extends Slime implements R196Mob {
    public enum Variant {
        SLIME(1),
        JELLY(2),
        BLOB(3),
        OOZE(3),
        PUDDING(4);

        private final int damageMultiplier;

        Variant(int damageMultiplier) {
            this.damageMultiplier = damageMultiplier;
        }
    }

    public R196Slime(EntityType<? extends Slime> type, Level level) {
        super(type, level);
    }

    public Variant variant() {
        return switch (R196EntityVariant.path(this)) {
            case "jelly" -> Variant.JELLY;
            case "blob" -> Variant.BLOB;
            case "ooze" -> Variant.OOZE;
            case "pudding" -> Variant.PUDDING;
            default -> Variant.SLIME;
        };
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.FOLLOW_RANGE, 16.0);
    }

    static double attackDamageForSize(Variant variant, int size) {
        return size * variant.damageMultiplier;
    }

    @Override
    public void setSize(int size, boolean updateHealth) {
        if (variant() == Variant.OOZE) {
            size = Math.min(size, 2);
        }
        super.setSize(size, updateHealth);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(attackDamageForSize(variant(), getSize()));
    }

    @Override
    protected boolean isDealsDamage() {
        return isEffectiveAi();
    }

    @Override
    protected void dealDamage(LivingEntity target) {
        float health = target.getHealth();
        super.dealDamage(target);
        if (target.getHealth() >= health) {
            return;
        }
        switch (variant()) {
            case JELLY -> target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 160, 1), this);
            case BLOB -> target.igniteForSeconds(4.0F);
            case OOZE -> target.addEffect(new MobEffectInstance(MobEffects.POISON, 160, 0), this);
            case PUDDING -> {
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 240, 1), this);
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 240, 0), this);
            }
            case SLIME -> {
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if ((variant() == Variant.OOZE || variant() == Variant.PUDDING)
                && source.getEntity() != null
                && !source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)
                && !source.is(net.minecraft.tags.DamageTypeTags.WITCH_RESISTANT_TO)) {
            damage *= 0.25F;
        }
        return super.hurtServer(level, source, damage);
    }
}
