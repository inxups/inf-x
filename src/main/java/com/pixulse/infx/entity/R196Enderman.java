package com.pixulse.infx.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
        return EnderMan.createAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.ATTACK_DAMAGE, 10.0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // MITE: a pearl or ender eye in hand angers instantly; pearls buried in the inventory
        // only draw attention at about one roll in 2000 per pearl per tick. The goal re-checks
        // every ~10 ticks, so 200 here keeps the per-tick odds.
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(
                this,
                Player.class,
                10,
                true,
                false,
                (target, level) -> {
                    if (!(target instanceof Player player)) {
                        return false;
                    }
                    if (isPearlLike(player.getMainHandItem()) || isPearlLike(player.getOffhandItem())) {
                        return true;
                    }
                    int pearls = player.getInventory().countItem(Items.ENDER_PEARL)
                            + player.getInventory().countItem(Items.ENDER_EYE);
                    return pearls > 0 && random.nextInt(200) < pearls;
                }));
    }

    private static boolean isPearlLike(net.minecraft.world.item.ItemStack stack) {
        return stack.is(Items.ENDER_PEARL) || stack.is(Items.ENDER_EYE);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.is(DamageTypeTags.IS_PROJECTILE) && source.getEntity() instanceof LivingEntity attacker) {
            DamageSource direct = attacker instanceof Player player
                    ? level.damageSources().playerAttack(player)
                    : level.damageSources().mobAttack(attacker);
            boolean hurt = super.hurtServer(level, direct, damage);
            if (hurt) {
                // MITE endermen take the projectile hit, then drop aggression and blink away.
                setTarget(null);
                for (int attempt = 0; attempt < 64 && !teleport(); attempt++) {}
            }
            return hurt;
        }
        return super.hurtServer(level, source, damage);
    }
}
