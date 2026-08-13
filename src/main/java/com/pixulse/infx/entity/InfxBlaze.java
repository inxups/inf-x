package com.pixulse.infx.entity;

import java.util.EnumSet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

/**
 * Blaze replacement with INFX's six-point melee damage and InfX vulnerability rules.
 *
 * <p>Water/snowballs always hurt. Ordinary hits require a non-fire enchanted weapon.
 * Fire-aspect / flame weapons are treated as fire. Attacker ignition must not cancel valid hits.
 */
public final class InfxBlaze extends Blaze implements InfxMob {
    // Mirrors the vanilla charged flag, whose setter is private, for the burn-while-charging look.
    private static final EntityDataAccessor<Boolean> DATA_CHARGED =
            SynchedEntityData.defineId(InfxBlaze.class, EntityDataSerializers.BOOLEAN);

    public InfxBlaze(EntityType<? extends Blaze> type, Level level) {
        super(type, level);
        // InfX blazes are worth four times the base experience.
        xpReward = 20;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NonNull Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_CHARGED, false);
    }

    @Override
    public boolean isOnFire() {
        return entityData.get(DATA_CHARGED);
    }

    void setCharged(boolean charged) {
        entityData.set(DATA_CHARGED, charged);
    }

    public static AttributeSupplier.Builder attributes() {
        // EntityBlaze uses the legacy fixed-throttle AI and does not override its
        // movement input.  Retain the modern Blaze baseline instead of copying the old
        // SharedMonsterAttributes default (0.7) into the modern movement scale.
        return Blaze.createAttributes()
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // Swap the vanilla fireball cadence (60/6/100) for the much faster 20/6/20 cycle.
        goalSelector.removeAllGoals(goal -> goal.getClass().getSimpleName().equals("BlazeAttackGoal"));
        goalSelector.addGoal(4, new InfxFireballGoal(this));
    }

    @Override
    public boolean hurtServer(@NonNull ServerLevel level, @NonNull DamageSource source, float damage) {
        if (MobDamageRules.blazeAccepts(level, source)) {
            return super.hurtServer(level, source, damage);
        }
        return false;
    }

    /** InfX cycle: 20-tick charge, three fireballs six ticks apart, 20-tick cooldown, 30-block firing range. */
    private static final class InfxFireballGoal extends Goal {
        private static final int CHARGE_TICKS = 20;
        private static final int VOLLEY_INTERVAL = 6;
        private static final int COOLDOWN_TICKS = 20;
        private static final double FIRE_RANGE_SQUARED = 30.0 * 30.0;

        private final InfxBlaze blaze;
        private int attackStep;
        private int attackTime;
        private int lastSeen;

        InfxFireballGoal(InfxBlaze blaze) {
            this.blaze = blaze;
            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = blaze.getTarget();
            return target != null
                    && target.isAlive()
                    && blaze.canAttack(target)
                    && MonsterEvents.withinFollowRange(blaze, target);
        }

        @Override
        public void start() {
            attackStep = 0;
        }

        @Override
        public void stop() {
            blaze.setCharged(false);
            lastSeen = 0;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            attackTime--;
            LivingEntity target = blaze.getTarget();
            if (target == null || !MonsterEvents.withinFollowRange(blaze, target)) {
                blaze.setTarget(null);
                return;
            }
            boolean seen = blaze.getSensing().hasLineOfSight(target);
            lastSeen = seen ? 0 : lastSeen + 1;
            double distance = blaze.distanceToSqr(target);
            if (distance < 4.0) {
                if (!seen) {
                    return;
                }
                if (attackTime <= 0) {
                    attackTime = 20;
                    blaze.doHurtTarget((ServerLevel) blaze.level(), target);
                }
                blaze.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.0);
            } else if (distance < FIRE_RANGE_SQUARED && seen) {
                double xd = target.getX() - blaze.getX();
                double yd = target.getY(0.5) - blaze.getY(0.5);
                double zd = target.getZ() - blaze.getZ();
                if (attackTime <= 0) {
                    attackStep++;
                    if (attackStep == 1) {
                        attackTime = CHARGE_TICKS;
                        blaze.setCharged(true);
                    } else if (attackStep <= 4) {
                        attackTime = VOLLEY_INTERVAL;
                    } else {
                        attackTime = COOLDOWN_TICKS;
                        attackStep = 0;
                        blaze.setCharged(false);
                    }
                    if (attackStep > 1) {
                        double spread = Math.sqrt(Math.sqrt(distance)) * 0.5;
                        if (!blaze.isSilent()) {
                            blaze.level().levelEvent(null, 1018, blaze.blockPosition(), 0);
                        }
                        Vec3 direction = new Vec3(
                                blaze.getRandom().triangle(xd, 2.297 * spread),
                                yd,
                                blaze.getRandom().triangle(zd, 2.297 * spread));
                        SmallFireball fireball = new SmallFireball(blaze.level(), blaze, direction.normalize());
                        fireball.setPos(fireball.getX(), blaze.getY(0.5) + 0.5, fireball.getZ());
                        blaze.level().addFreshEntity(fireball);
                    }
                }
                blaze.getLookControl().setLookAt(target, 10.0F, 10.0F);
            } else if (lastSeen < 5) {
                blaze.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.0);
            }
            super.tick();
        }
    }
}
