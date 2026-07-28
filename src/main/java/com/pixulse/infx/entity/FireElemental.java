package com.pixulse.infx.entity;

import com.pixulse.infx.registry.InfinityXSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * MITE fire elemental: melee, fireproof, lava-healing, and only vulnerable to water, snowballs,
 * and non-fire enchanted weapons — not a blaze fireball clone.
 */
public final class FireElemental extends Blaze implements MiteMob {
    private static final int WATER_TICK_INTERVAL = 40;
    private static final float LAVA_HEAL_AMOUNT = 4.0F;
    private static final float WATER_DAMAGE = 1.0F;
    private static final int MELEE_IGNITE_SECONDS = 6;
    private static final int EXPERIENCE_MULTIPLIER = 3;

    private int ticksUntilNextFireSound;
    private int ticksUntilNextFizzSound;

    public FireElemental(EntityType<? extends Blaze> type, Level level) {
        super(type, level);
        this.xpReward = 5 * EXPERIENCE_MULTIPLIER;
    }

    public static AttributeSupplier.Builder attributes() {
        return Blaze.createAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    @Override
    public boolean isWithinMeleeAttackRange(LivingEntity target) {
        return AttackRanges.withinNewAiReach(this, target);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0));
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0, 0.0F));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        // MITE fire elementals only retaliate and hunt players; villagers are not sought out.
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        // Skip Blaze hover offset; MITE fire elementals are ground melee mobs.
    }

    @Override
    public void aiStep() {
        if (level() instanceof ServerLevel serverLevel) {
            tickMiteEffects(serverLevel);
        }
        // isSilent() suppresses Blaze BLAZE_BURN; explicit MITE fire/fizz playback below.
        super.aiStep();
    }

    private void tickMiteEffects(ServerLevel serverLevel) {
        if (isInWaterOrRain()) {
            if (shouldApplyWaterAttrition(tickCount, true)) {
                hurtServer(serverLevel, damageSources().drown(), WATER_DAMAGE);
            }
            if (--ticksUntilNextFizzSound <= 0) {
                playMiteSound(
                        SoundEvents.FIRE_EXTINGUISH,
                        0.7F,
                        1.6F + (random.nextFloat() - random.nextFloat()) * 0.4F);
                if (random.nextInt(4) == 0) {
                    playMiteSound(InfinityXSounds.FIRE_ELEMENTAL_SIZZLE.get(), 1.0F, 1.0F);
                }
                ticksUntilNextFizzSound = random.nextInt(7) + 2;
                if (random.nextInt(isInWater() ? 1 : 4) == 0) {
                    hurtServer(serverLevel, damageSources().drown(), WATER_DAMAGE);
                }
            }
        } else if (--ticksUntilNextFireSound <= 0) {
            playMiteSound(
                    SoundEvents.FIRE_AMBIENT,
                    1.0F + random.nextFloat(),
                    random.nextFloat() * 0.7F + 0.3F);
            ticksUntilNextFireSound = random.nextInt(21) + 30;
            if (isInLava()) {
                heal(LAVA_HEAL_AMOUNT);
            }
        }
    }

    /** The fixed-interval water drain must never run while a fire elemental is dry. */
    static boolean shouldApplyWaterAttrition(int tickCount, boolean inWaterOrRain) {
        return inWaterOrRain && tickCount % WATER_TICK_INTERVAL == 0;
    }

    private void playMiteSound(SoundEvent sound, float volume, float pitch) {
        // Use level().playSound directly so isSilent() does not suppress MITE fire/fizz.
        level().playSound(null, getX(), getY(), getZ(), sound, SoundSource.HOSTILE, volume, pitch);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (!MobDamageRules.fireElementalAccepts(level, source)) {
            return false;
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hurt = super.doHurtTarget(level, target);
        if (hurt) {
            target.igniteForSeconds(MELEE_IGNITE_SECONDS);
        }
        return hurt;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return null;
    }

    @Override
    public boolean isOnFire() {
        return true;
    }

    @Override
    public boolean isSensitiveToWater() {
        // MITE water attrition is fully modelled in tickMiteEffects; the modern per-tick
        // water damage would roughly double the drain in rain.
        return false;
    }

    @Override
    public boolean isSilent() {
        // Suppress Blaze client BLAZE_BURN loop; MITE fire/fizz uses direct level playSound.
        return true;
    }
}
