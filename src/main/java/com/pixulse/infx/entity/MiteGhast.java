package com.pixulse.infx.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Ghast replacement retaining a one-hundred-block ranged awareness radius. */
public final class MiteGhast extends Ghast implements MiteMob {
    public MiteGhast(EntityType<? extends Ghast> type, Level level) {
        super(type, level);
        // MITE ghasts are worth double the base experience.
        xpReward = 10;
    }

    public static AttributeSupplier.Builder attributes() {
        return Ghast.createAttributes();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** MITE ghast cries carry at twice the modern volume. */
    @Override
    protected float getSoundVolume() {
        return 10.0F;
    }
}
