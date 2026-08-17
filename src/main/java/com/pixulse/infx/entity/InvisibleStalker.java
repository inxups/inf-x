package com.pixulse.infx.entity;

import com.pixulse.infx.registry.InfXSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

/**
 * MITE invisible stalker: a near-silent zombie-shaped hunter that pursues only players and
 * villagers, breaks doors, extinguishes torches, and never burns or converts villagers.
 */
public final class InvisibleStalker extends InfxZombieBase {
    public InvisibleStalker(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
        xpReward = 10;
    }

    public static AttributeSupplier.Builder attributes() {
        return Zombie.createAttributes().add(Attributes.ARMOR, 0.0)
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.FOLLOW_RANGE, 35.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.ATTACK_DAMAGE, 4.0);
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
        return false;
    }

    @Override
    protected boolean zombifiesVillagers() {
        return false;
    }

    @Override
    protected boolean isSilentType() {
        return true;
    }

    /** Stalkers are not ordinary zombies: they pursue only players and villagers. */
    @Override
    protected void addBehaviourGoals() {
        goalSelector.addGoal(3, new ZombieAttackGoal(this, 1.0, false));
        goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 1.0));
        goalSelector.addGoal(1, new InfxSeekLitTorchGoal(this, 200, 1.0));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Villager.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level() instanceof ServerLevel level
                && (getLastHurtByMob() == null || tickCount - getLastHurtByMobTimestamp() > 100)
                && level.getNearestPlayer(this, 4.0) == null) {
            disableNearbyLight(level);
        }
    }

    @Override
    protected @NonNull SoundEvent getAmbientSound() {
        return InfXSounds.INVISIBLE_STALKER_AMBIENT.get();
    }

    @Override
    protected @NonNull SoundEvent getHurtSound(@NonNull DamageSource source) {
        return InfXSounds.INVISIBLE_STALKER_HURT.get();
    }

    @Override
    protected @NonNull SoundEvent getDeathSound() {
        return InfXSounds.INVISIBLE_STALKER_DEATH.get();
    }
}
