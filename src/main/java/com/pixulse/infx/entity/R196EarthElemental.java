package com.pixulse.infx.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Slow, heavily armoured earth elemental with R196's high melee damage. */
public final class R196EarthElemental extends IronGolem implements Enemy, R196Mob {
    public R196EarthElemental(EntityType<? extends IronGolem> type, Level level) {
        super(type, level);
        setPlayerCreated(false);
    }

    public static AttributeSupplier.Builder attributes() {
        return IronGolem.createAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.FOLLOW_RANGE, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.20)
                .add(Attributes.ATTACK_DAMAGE, 12.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.getEntity() instanceof Player player && !player.getMainHandItem().is(ItemTags.PICKAXES)) {
            return false;
        }
        return super.hurtServer(level, source, damage);
    }
}
