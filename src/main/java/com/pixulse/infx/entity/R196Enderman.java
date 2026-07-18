package com.pixulse.infx.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/** Enderman replacement with pearl awareness and projectile damage support. */
public final class R196Enderman extends EnderMan implements R196Mob {
    public R196Enderman(EntityType<? extends EnderMan> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return EnderMan.createAttributes();
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
                (target, level) -> target instanceof Player player && player.getInventory().contains(
                        stack -> stack.is(Items.ENDER_PEARL) || stack.is(Items.ENDER_EYE))));
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.is(DamageTypeTags.IS_PROJECTILE) && source.getEntity() instanceof LivingEntity attacker) {
            DamageSource direct = attacker instanceof Player player
                    ? level.damageSources().playerAttack(player)
                    : level.damageSources().mobAttack(attacker);
            return super.hurtServer(level, direct, damage);
        }
        return super.hurtServer(level, source, damage);
    }
}
