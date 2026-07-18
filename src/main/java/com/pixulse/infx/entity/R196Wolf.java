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

/** Hellhounds and Dire Wolves retain wolf taming while remaining dangerous when wild. */
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

    public static AttributeSupplier.Builder attributes(Variant variant) {
        return Wolf.createAttributes()
                .add(Attributes.MAX_HEALTH, variant == Variant.HELLHOUND ? 20.0 : 16.0)
                .add(Attributes.MOVEMENT_SPEED, variant == Variant.HELLHOUND ? 0.40 : 0.32)
                .add(Attributes.ATTACK_DAMAGE, variant == Variant.HELLHOUND ? 4.0 : 5.0)
                .add(Attributes.FOLLOW_RANGE, 40.0);
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
