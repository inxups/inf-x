package com.pixulse.infx.entity;

import com.pixulse.infx.registry.InfXSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

/**
 * MITE ghoul: a zombie-family predator that slows victims and heals half its max health from
 * feeding on animals. Its block-digging cooloff is halved in {@link MonsterTactics}.
 */
public final class Ghoul extends InfxZombieBase {
    public Ghoul(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
        xpReward = 10;
    }

    public static AttributeSupplier.Builder attributes() {
        return Zombie.createAttributes().add(Attributes.ARMOR, 0.0)
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.FOLLOW_RANGE, 35.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    @Override
    protected boolean breaksDoors() {
        return true;
    }

    @Override
    protected boolean picksUpLoot() {
        return false;
    }

    @Override
    protected boolean targetsAnimals() {
        return true;
    }

    @Override
    protected boolean zombifiesVillagers() {
        return true;
    }

    @Override
    public boolean doHurtTarget(@NonNull ServerLevel level, @NonNull Entity target) {
        boolean hurt = super.doHurtTarget(level, target);
        if (hurt && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 5), this);
            if (living instanceof Animal && !living.isAlive()) {
                heal(getMaxHealth() * 0.5F);
            }
        }
        return hurt;
    }

    @Override
    protected @NonNull SoundEvent getAmbientSound() {
        return InfXSounds.GHOUL_AMBIENT.get();
    }

    @Override
    protected @NonNull SoundEvent getHurtSound(@NonNull DamageSource source) {
        return InfXSounds.GHOUL_HURT.get();
    }

    @Override
    protected @NonNull SoundEvent getDeathSound() {
        return InfXSounds.GHOUL_DEATH.get();
    }
}
