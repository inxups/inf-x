package com.pixulse.infx.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Hostile cave bats. Vampire variants heal on contact; Nightwings blind their prey. */
public final class R196Bat extends Bat implements Enemy, R196Mob {
    public enum Variant {
        VAMPIRE,
        NIGHTWING,
        GIANT_VAMPIRE
    }

    private int attackCooldown;

    public R196Bat(EntityType<? extends Bat> type, Level level) {
        super(type, level);
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
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (attackCooldown > 0) {
            attackCooldown--;
        }

        Player target = level.getNearestPlayer(this, 32.0);
        if (target == null || target.isCreative() || target.isSpectator()) {
            return;
        }

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
                }
            }
            attackCooldown = 20;
        }

        if (variant() == Variant.NIGHTWING && level.isBrightOutside() && level.canSeeSky(blockPosition())) {
            igniteForSeconds(4.0F);
        } else if (variant() == Variant.NIGHTWING && tickCount % 40 == 0 && getHealth() < getMaxHealth()) {
            heal(1.0F);
        }
    }
}
