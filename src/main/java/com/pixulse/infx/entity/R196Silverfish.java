package com.pixulse.infx.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
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
    private int nextFizzTick;

    public R196Silverfish(EntityType<? extends Silverfish> type, Level level) {
        super(type, level);
        // MITE silverfish variants are worth double the base experience.
        xpReward = 10;
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
    protected void registerGoals() {
        super.registerGoals();
        // MITE silverfish never burrow into blocks; they notice players only within 8 blocks
        // but actively hunt animals and villagers.
        goalSelector.removeAllGoals(goal -> goal.getClass().getSimpleName().equals("SilverfishMergeWithStoneGoal"));
        targetSelector.removeAllGoals(goal -> goal instanceof NearestAttackableTargetGoal<?>);
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                this, Player.class, 10, true, false, (target, level) -> distanceToSqr(target) <= 64.0));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Animal.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Villager.class, true));
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hurt = super.doHurtTarget(level, target);
        if (hurt && target instanceof LivingEntity living) {
            if (variant() == Variant.COPPERSPINE) {
                living.addEffect(new MobEffectInstance(MobEffects.POISON, 480, 0), this);
            } else if (variant() == Variant.HOARY) {
                living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 5), this);
            }
        }
        return hurt;
    }

    /** MITE netherspawn can be safely killed with snowballs, which modern snowballs cannot do. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (variant() == Variant.NETHERSPAWN
                && source.getDirectEntity() instanceof Snowball
                && source.getEntity() instanceof Player) {
            damage = Math.max(damage, 1.0F);
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        // MITE netherspawn fizz while wet every 2-8 ticks, always losing a point in water and
        // one roll in four in rain — roughly one point every five ticks when submerged.
        if (variant() == Variant.NETHERSPAWN && level() instanceof ServerLevel level
                && isInWaterOrRain() && tickCount >= nextFizzTick) {
            nextFizzTick = tickCount + 2 + random.nextInt(7);
            playSound(SoundEvents.FIRE_EXTINGUISH, 0.7F, 1.6F + (random.nextFloat() - random.nextFloat()) * 0.4F);
            if (isInWater() || random.nextInt(4) == 0) {
                hurtServer(level, damageSources().drown(), 1.0F);
            }
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
