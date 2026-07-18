package com.pixulse.infx.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** R196 squid replacement that hunts swimmers and inflicts a paralysis-like snare. */
public final class R196Squid extends Squid implements R196Mob {
    private int attackCooldown;

    public R196Squid(EntityType<? extends Squid> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Squid.createAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel level)) {
            return;
        }
        if (attackCooldown > 0) {
            attackCooldown--;
        }
        Player target = level.getNearestPlayer(this, 24.0);
        if (target == null
                || !target.isInWater()
                || !level.getFluidState(target.blockPosition().below()).is(FluidTags.WATER)
                || target.isCreative()
                || target.isSpectator()) {
            return;
        }

        Vec3 delta = target.getEyePosition().subtract(position());
        double distance = delta.length();
        if (distance > 0.001) {
            setDeltaMovement(getDeltaMovement().scale(0.5).add(delta.normalize().scale(0.18)));
        }
        if (distance < 1.5 && attackCooldown == 0 && doHurtTarget(level, target)) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 5), this);
            target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 80, 3), this);
            attackCooldown = 20;
        }
    }
}
