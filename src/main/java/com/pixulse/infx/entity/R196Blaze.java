package com.pixulse.infx.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.level.Level;

/**
 * Blaze replacement with R196's six-point melee damage and MITE vulnerability rules.
 *
 * <p>Water/snowballs always hurt. Ordinary hits require a non-fire enchanted weapon.
 * Fire-aspect / flame weapons are treated as fire. Attacker ignition must not cancel valid hits.
 */
public final class R196Blaze extends Blaze implements R196Mob {
    public R196Blaze(EntityType<? extends Blaze> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Blaze.createAttributes()
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.MOVEMENT_SPEED, 0.70)
                .add(Attributes.ATTACK_DAMAGE, 6.0);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (R196MobDamageRules.blazeAccepts(level, source)) {
            return super.hurtServer(level, source, damage);
        }
        return false;
    }
}
