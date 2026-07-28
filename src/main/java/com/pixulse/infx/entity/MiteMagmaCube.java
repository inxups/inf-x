package com.pixulse.infx.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.level.Level;

/**
 * The R196 magma cube only accepts player melee from stone-mining tools (pickaxe / war hammer),
 * plus snowballs, water and explosions — matching MITE's stone-effective tool gate.
 */
public final class MiteMagmaCube extends MagmaCube implements MiteMob {
    private static final double MOVEMENT_SPEED = 0.20;

    private int nextFizzTick;

    public MiteMagmaCube(EntityType<? extends MagmaCube> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return MagmaCube.createAttributes()
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED);
    }

    static double attackDamageForSize(int size) {
        return size * 2.0;
    }

    static double armorForSize(int size) {
        return size * 2.0;
    }

    static double movementSpeedForSize(int size) {
        return MOVEMENT_SPEED;
    }

    static int experienceForSize(int size) {
        return size * 3;
    }

    @Override
    public void setSize(int size, boolean updateHealth) {
        super.setSize(size, updateHealth);
        int actualSize = getSize();
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(movementSpeedForSize(actualSize));
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(attackDamageForSize(actualSize));
        getAttribute(Attributes.ARMOR).setBaseValue(armorForSize(actualSize));
        this.xpReward = experienceForSize(actualSize);
    }

    @Override
    protected float getAttackDamage() {
        return (float) attackDamageForSize(getSize());
    }

    /** MITE jump cadence: 40-120 ticks at rest, an effective 20 while chasing (60 / 3). */
    @Override
    protected int getJumpDelay() {
        return getTarget() != null ? 60 : random.nextInt(81) + 40;
    }

    /** MITE magma cubes set landed targets on fire for size×3 seconds at a size×2-in-10 chance. */
    @Override
    protected void dealDamage(LivingEntity target) {
        float health = target.getHealth();
        super.dealDamage(target);
        if (target.getHealth() < health && random.nextInt(10) < getSize() * 2) {
            target.igniteForSeconds(getSize() * 3.0F);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (!MobDamageRules.magmaCubeAccepts(source)) {
            return false;
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        // MITE magma cubes fizz while wet and take a point of water damage on a 1-in-4 roll in
        // water (1-in-16 in rain), rechecked every 2-8 ticks.
        if (level() instanceof ServerLevel level && isInWaterOrRain() && tickCount >= nextFizzTick) {
            nextFizzTick = tickCount + 2 + random.nextInt(7);
            playSound(SoundEvents.FIRE_EXTINGUISH, 0.7F, 1.6F + (random.nextFloat() - random.nextFloat()) * 0.4F);
            if (random.nextInt(isInWater() ? 4 : 16) == 0) {
                hurtServer(level, damageSources().drown(), 1.0F);
            }
        }
    }
}
