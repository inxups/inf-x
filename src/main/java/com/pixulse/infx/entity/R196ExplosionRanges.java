package com.pixulse.infx.entity;

import java.util.OptionalDouble;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.level.Explosion;

/** Internal mapping for R196 explosions whose entity radius differs from their block power. */
public final class R196ExplosionRanges {
    static final double CREEPER_ENTITY_RADIUS = 4.4;
    static final double INFERNAL_CREEPER_ENTITY_RADIUS = 8.8;
    static final double NETHERSPAWN_ENTITY_RADIUS = 4.0;
    static final double GHAST_FIREBALL_ENTITY_RADIUS = 4.0;

    private R196ExplosionRanges() {}

    public static OptionalDouble entityRadius(Explosion explosion) {
        return entityRadius(explosion.getDirectSourceEntity());
    }

    static OptionalDouble entityRadius(Entity source) {
        if (source instanceof R196Creeper creeper) {
            return OptionalDouble.of(creeperEntityRadius(creeper.variant(), creeper.isPowered()));
        }
        if (source instanceof R196Silverfish silverfish
                && silverfish.variant() == R196Silverfish.Variant.NETHERSPAWN) {
            return OptionalDouble.of(NETHERSPAWN_ENTITY_RADIUS);
        }
        if (source instanceof LargeFireball fireball && fireball.getOwner() instanceof R196Ghast) {
            return OptionalDouble.of(GHAST_FIREBALL_ENTITY_RADIUS);
        }
        return OptionalDouble.empty();
    }

    static double creeperEntityRadius(R196Creeper.Variant variant, boolean powered) {
        double radius = variant == R196Creeper.Variant.INFERNAL
                ? INFERNAL_CREEPER_ENTITY_RADIUS
                : CREEPER_ENTITY_RADIUS;
        return powered ? radius * 2.0 : radius;
    }

    public static float damageAmount(Explosion explosion, Entity entity, float exposure, double entityRadius) {
        double distance = Math.sqrt(entity.distanceToSqr(explosion.center())) / entityRadius;
        double power = (1.0 - distance) * exposure;
        return (float) ((power * power + power) * 0.5 * 7.0 * entityRadius + 1.0);
    }
}
