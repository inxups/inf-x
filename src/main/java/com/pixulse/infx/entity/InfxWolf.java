package com.pixulse.infx.entity;

import com.pixulse.infx.registry.InfXSounds;
import com.pixulse.infx.world.MoonPhase;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

/** Dire wolves retain wolf taming, while hellhounds remain permanently wild and hostile. */
public final class InfxWolf extends Wolf implements Enemy, InfxMob {
    public enum Variant {
        HELLHOUND,
        DIRE_WOLF
    }

    public InfxWolf(EntityType<? extends Wolf> type, Level level) {
        super(type, level);
    }

    /** MITE experience: hellhounds are worth triple, dire wolves double the base value. */
    @Override
    public int getBaseExperienceReward(@NonNull ServerLevel level) {
        return variant() == Variant.HELLHOUND ? 15 : 10;
    }

    public Variant variant() {
        return EntityVariant.path(this).equals("dire_wolf") ? Variant.DIRE_WOLF : Variant.HELLHOUND;
    }

    static double maximumHealth(Variant variant, boolean tamed) {
        if (variant == Variant.HELLHOUND) {
            return 20.0;
        }
        return tamed ? 24.0 : 16.0;
    }

    static double followRange(Variant variant, boolean tamed) {
        return variant == Variant.DIRE_WOLF && tamed ? 32.0 : 16.0;
    }

    public static AttributeSupplier.Builder attributes(Variant variant) {
        return Wolf.createAttributes()
                .add(Attributes.MAX_HEALTH, maximumHealth(variant, false))
                .add(Attributes.MOVEMENT_SPEED, 0.40)
                .add(Attributes.ATTACK_DAMAGE, variant == Variant.HELLHOUND ? 4.0 : 5.0)
                .add(Attributes.FOLLOW_RANGE, followRange(variant, false));
    }

    @Override
    public boolean isWithinMeleeAttackRange(@NonNull LivingEntity target) {
        return AttackRanges.withinWolfReach(this, target);
    }

    @Override
    public void setTame(boolean tame, boolean applySideEffects) {
        super.setTame(variant() == Variant.DIRE_WOLF && tame, applySideEffects);
        if (variant() != Variant.DIRE_WOLF) {
            return;
        }

        var health = getAttribute(Attributes.MAX_HEALTH);
        var range = getAttribute(Attributes.FOLLOW_RANGE);
        if (health == null || range == null) {
            return;
        }

        double maximumHealth = maximumHealth(Variant.DIRE_WOLF, isTame());
        double oldMaximumHealth = health.getBaseValue();
        health.setBaseValue(maximumHealth);
        range.setBaseValue(followRange(Variant.DIRE_WOLF, isTame()));
        if (maximumHealth > oldMaximumHealth) {
            setHealth(getHealth() + (float) (maximumHealth - oldMaximumHealth));
        } else if (getHealth() > maximumHealth) {
            setHealth((float) maximumHealth);
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // MITE: only hellhounds hunt players on sight; dire wolves are near-neutral (see aiStep).
        if (variant() == Variant.HELLHOUND) {
            targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(
                    this,
                    Player.class,
                    10,
                    true,
                    false,
                    (target, level) -> true));
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        // MITE dire wolves snap at players within 4 blocks at 0.4% per tick, sparing pups,
        // breeding pairs and blue-moon nights.
        if (variant() == Variant.DIRE_WOLF
                && !isTame()
                && !isBaby()
                && getTarget() == null
                && !isInLove()
                && level() instanceof ServerLevel level
                && !MoonPhase.BLUE.isActiveInOverworldAtNight(level)
                && random.nextFloat() < 0.004F) {
            Player near = level.getNearestPlayer(this, 4.0);
            if (near != null && !near.isCreative() && !near.isSpectator()) {
                setTarget(near);
            }
        }
    }

    /** MITE: untamed wolves fade out after two minutes; hellhounds despawn like any monster. */
    @Override
    public boolean removeWhenFarAway(double distance) {
        return variant() == Variant.HELLHOUND || (!isTame() && tickCount > 2400);
    }

    /** MITE wolves shrug off half the damage from non-player, non-arrow attackers. */
    @Override
    public boolean hurtServer(@NonNull ServerLevel level, DamageSource source, float damage) {
        if (source.getEntity() != null
                && !(source.getEntity() instanceof Player)
                && !(source.getDirectEntity() instanceof AbstractArrow)) {
            damage = (damage + 1.0F) / 2.0F;
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    protected @NonNull SoundEvent getAmbientSound() {
        return variant() == Variant.HELLHOUND ? InfXSounds.HELLHOUND_AMBIENT.get() : super.getAmbientSound();
    }

    @Override
    protected @NonNull SoundEvent getHurtSound(@NonNull DamageSource source) {
        return variant() == Variant.HELLHOUND ? InfXSounds.HELLHOUND_HURT.get() : super.getHurtSound(source);
    }

    @Override
    protected @NonNull SoundEvent getDeathSound() {
        return variant() == Variant.HELLHOUND ? InfXSounds.HELLHOUND_DEATH.get() : super.getDeathSound();
    }

    @Override
    public boolean doHurtTarget(@NonNull ServerLevel level, @NonNull Entity target) {
        boolean hurt = super.doHurtTarget(level, target);
        if (hurt && variant() == Variant.HELLHOUND && random.nextFloat() < 0.4F) {
            playSound(InfXSounds.HELLHOUND_BREATH.get(), 4.0F, 1.0F);
            target.igniteForSeconds(1 + random.nextInt(8));
        }
        return hurt;
    }

    @Override
    public boolean fireImmune() {
        return variant() == Variant.HELLHOUND || super.fireImmune();
    }
}
