package com.pixulse.infx.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

/**
 * Slow, heavily armoured earth elemental. Players must use a stone-mining tool
 * (pickaxe / war hammer); explosions always land.
 */
public final class R196EarthElemental extends IronGolem implements Enemy, R196Mob {
    public R196EarthElemental(EntityType<? extends IronGolem> type, Level level) {
        super(type, level);
        setPlayerCreated(false);
        // MITE earth elementals are worth triple the base experience.
        xpReward = 15;
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

    /**
     * MITE elementals are plain hunters: unlike iron golems they never defend villages, offer
     * flowers or fight other monsters — they chase players and villagers and break doors.
     */
    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new BreakDoorGoal(this, difficulty -> true));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Villager.class, true));
    }

    /** MITE elementals hit for their flat attack value with no iron-golem launch or damage roll. */
    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        DamageSource source = damageSources().mobAttack(this);
        boolean hurt = target.hurtServer(level, source, (float) getAttributeValue(Attributes.ATTACK_DAMAGE));
        if (hurt) {
            EnchantmentHelper.doPostAttackEffects(level, target, source);
            playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
        }
        return hurt;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (!R196MobDamageRules.earthElementalAccepts(source)) {
            return false;
        }
        return super.hurtServer(level, source, damage);
    }
}
