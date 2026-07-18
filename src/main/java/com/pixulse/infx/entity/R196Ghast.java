package com.pixulse.infx.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Ghast replacement retaining a one-hundred-block ranged awareness radius. */
public final class R196Ghast extends Ghast implements R196Mob {
    public R196Ghast(EntityType<? extends Ghast> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Ghast.createAttributes();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
}
