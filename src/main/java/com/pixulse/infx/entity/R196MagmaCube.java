package com.pixulse.infx.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.cubemob.MagmaCube;
import net.minecraft.world.level.Level;

/**
 * The R196 magma cube only accepts player melee from stone-mining tools (pickaxe / war hammer),
 * plus snowballs, water and explosions — matching MITE's stone-effective tool gate.
 */
public final class R196MagmaCube extends MagmaCube implements R196Mob {
    private static final double MOVEMENT_SPEED = 0.20;

    public R196MagmaCube(EntityType<? extends MagmaCube> type, Level level) {
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

    @Override
    public void setSize(int size, boolean updateHealth) {
        super.setSize(size, updateHealth);
        int actualSize = getSize();
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(movementSpeedForSize(actualSize));
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(attackDamageForSize(actualSize));
        getAttribute(Attributes.ARMOR).setBaseValue(armorForSize(actualSize));
    }

    @Override
    protected float getAttackDamage() {
        return (float) attackDamageForSize(getSize());
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (!R196MobDamageRules.magmaCubeAccepts(source)) {
            return false;
        }
        return super.hurtServer(level, source, damage);
    }
}
