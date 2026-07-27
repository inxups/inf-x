package com.pixulse.infx.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Hostile cave bats. Vampire variants heal on contact; Nightwings blind their prey. */
public final class R196Bat extends Bat implements Enemy, R196Mob {
    private static final int ATTACK_COOLDOWN_TICKS = 20;
    private static final int FEED_COOLDOWN_TICKS = 1_200;
    public enum Variant {
        VAMPIRE,
        NIGHTWING,
        GIANT_VAMPIRE
    }

    private int attackCooldown;
    private int feedCooldown;
    private @Nullable LivingEntity prey;

    public R196Bat(EntityType<? extends Bat> type, Level level) {
        super(type, level);
        // MITE experience: vampire bats 5, giant vampires and nightwings 10.
        xpReward = variant() == Variant.VAMPIRE ? 5 : 10;
    }

    public Variant variant() {
        return switch (R196EntityVariant.path(this)) {
            case "nightwing" -> Variant.NIGHTWING;
            case "giant_vampire_bat" -> Variant.GIANT_VAMPIRE;
            default -> Variant.VAMPIRE;
        };
    }

    public static AttributeSupplier.Builder attributes(Variant variant) {
        boolean giant = variant == Variant.GIANT_VAMPIRE;
        return Bat.createAttributes()
                .add(Attributes.MAX_HEALTH, giant ? 6.0 : 3.0)
                .add(Attributes.ATTACK_DAMAGE, giant ? 2.0 : 1.0)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    /** MITE nightwings only fall to silver, magic or sunlight. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (variant() == Variant.NIGHTWING && !R196MobDamageRules.silverMagicGateAccepts(source)) {
            return false;
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (attackCooldown > 0) {
            attackCooldown--;
        }
        if (variant() == Variant.VAMPIRE && feedCooldown > 0) {
            if (getHealth() < getMaxHealth()) {
                feedCooldown = 0;
            } else {
                feedCooldown--;
            }
        }

        if (tickCount % 20 == 0 || prey == null || !prey.isAlive() || prey.isRemoved()) {
            prey = findPrey(level);
        }
        LivingEntity target = prey;
        if (target != null) {
            setResting(false);
            Vec3 direction = target.getEyePosition().subtract(position());
            double distance = direction.length();
            if (distance > 0.001) {
                double speed = variant() == Variant.GIANT_VAMPIRE ? 0.22 : 0.28;
                setDeltaMovement(getDeltaMovement().scale(0.65).add(direction.normalize().scale(speed)));
            }

            if (distance <= getBbWidth() + target.getBbWidth() + 0.35 && attackCooldown == 0) {
                float before = target.getHealth();
                if (doHurtTarget(level, target)) {
                    float dealt = Math.max(0.0F, before - target.getHealth());
                    if (variant() == Variant.NIGHTWING) {
                        target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0), this);
                    } else {
                        heal(dealt);
                        if (variant() == Variant.VAMPIRE && getHealth() >= getMaxHealth()) {
                            feedCooldown = feedCooldownTicks();
                        }
                    }
                }
                attackCooldown = attackCooldownTicks();
            }
        }

        if (variant() == Variant.NIGHTWING) {
            if (level.isBrightOutside() && level.canSeeSky(blockPosition()) && !level.isRaining()) {
                // MITE nightwings take 1000 sunlight damage: certain death.
                hurtServer(level, damageSources().genericKill(), 1000.0F);
            } else if (tickCount % 40 == 0) {
                int darknessHeal = (int) ((0.4F - getLightLevelDependentMagicValue()) * 10.0F);
                if (darknessHeal > 0) {
                    heal(darknessHeal);
                }
            }
        }
    }

    /** MITE bats hunt the closest non-creative player, animal or villager within 32 blocks. */
    private @Nullable LivingEntity findPrey(ServerLevel level) {
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity candidate : level.getEntitiesOfClass(
                LivingEntity.class, getBoundingBox().inflate(32.0), entity -> entity.isAlive() && isPrey(entity))) {
            double distance = distanceToSqr(candidate);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        return best;
    }

    private boolean isPrey(LivingEntity entity) {
        if (entity instanceof Player player) {
            return !player.isCreative() && !player.isSpectator();
        }
        return !restrictsPreyToPlayers() && (entity instanceof Animal || entity instanceof Villager);
    }

    private boolean restrictsPreyToPlayers() {
        return variant() == Variant.VAMPIRE && feedCooldown > 0;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (feedCooldown > 0) {
            output.putInt("R196VampireBatFeedCooldown", feedCooldown);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        feedCooldown = Math.max(0, input.getIntOr("R196VampireBatFeedCooldown", 0));
    }

    static int attackCooldownTicks() {
        return ATTACK_COOLDOWN_TICKS;
    }

    /** MITE vampire bats need a full minute of full health before resuming animal feeding. */
    static int feedCooldownTicks() {
        return FEED_COOLDOWN_TICKS;
    }
}
