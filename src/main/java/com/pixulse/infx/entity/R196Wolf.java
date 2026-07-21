package com.pixulse.infx.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Dire wolves retain wolf taming, while hellhounds remain permanently wild and hostile. */
public final class R196Wolf extends Wolf implements Enemy, R196Mob {
    public enum Variant {
        HELLHOUND,
        DIRE_WOLF
    }

    public R196Wolf(EntityType<? extends Wolf> type, Level level) {
        super(type, level);
    }

    public Variant variant() {
        return R196EntityVariant.path(this).equals("dire_wolf") ? Variant.DIRE_WOLF : Variant.HELLHOUND;
    }

    static double maximumHealth(Variant variant, boolean tamed) {
        if (variant == Variant.HELLHOUND) {
            return 20.0;
        }
        return tamed ? 24.0 : 16.0;
    }

    static double followRange(Variant variant, boolean tamed) {
        return variant == Variant.DIRE_WOLF && tamed ? 32.0 : 16.0;
    }

    public static AttributeSupplier.Builder attributes(Variant variant) {
        return Wolf.createAttributes()
                .add(Attributes.MAX_HEALTH, maximumHealth(variant, false))
                .add(Attributes.MOVEMENT_SPEED, 0.40)
                .add(Attributes.ATTACK_DAMAGE, variant == Variant.HELLHOUND ? 4.0 : 5.0)
                .add(Attributes.FOLLOW_RANGE, followRange(variant, false));
    }

    @Override
    public void setTame(boolean tame, boolean applySideEffects) {
        super.setTame(variant() == Variant.DIRE_WOLF && tame, applySideEffects);
        if (variant() != Variant.DIRE_WOLF) {
            return;
        }

        var health = getAttribute(Attributes.MAX_HEALTH);
        var range = getAttribute(Attributes.FOLLOW_RANGE);
        if (health == null || range == null) {
            return;
        }

        double maximumHealth = maximumHealth(Variant.DIRE_WOLF, isTame());
        double oldMaximumHealth = health.getBaseValue();
        health.setBaseValue(maximumHealth);
        range.setBaseValue(followRange(Variant.DIRE_WOLF, isTame()));
        if (maximumHealth > oldMaximumHealth) {
            setHealth(getHealth() + (float) (maximumHealth - oldMaximumHealth));
        } else if (getHealth() > maximumHealth) {
            setHealth((float) maximumHealth);
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(
                this,
                Player.class,
                10,
                true,
                false,
                (target, level) -> !isTame()));
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hurt = super.doHurtTarget(level, target);
        if (hurt && variant() == Variant.HELLHOUND && random.nextFloat() < 0.4F) {
            target.igniteForSeconds(1 + random.nextInt(8));
        }
        return hurt;
    }

    @Override
    public boolean fireImmune() {
        return variant() == Variant.HELLHOUND || super.fireImmune();
    }
}
