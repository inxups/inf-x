package com.pixulse.infx.entity;

import com.pixulse.infx.registry.ModEntityTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import com.pixulse.infx.registry.ModMobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.level.Level;

/** Swamp-hut miniboss witch with a curse and occasional wolf summons. */
public final class R196Witch extends Witch implements R196Mob {
    public R196Witch(EntityType<? extends Witch> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Witch.createAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.removeAllGoals(goal -> goal instanceof RangedAttackGoal);
        goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 60, 64.0F));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        var target = getTarget();
        if (target == null) {
            return;
        }
        if (tickCount % 200 == 0) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 0), this);
            target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 160, 0), this);
            target.addEffect(new MobEffectInstance(ModMobEffects.WITCH_CURSE, 600, 0), this);
        }
        if (tickCount % 300 == 0 && random.nextFloat() < 0.35F) {
            EntityType<R196Wolf> type = random.nextBoolean()
                    ? ModEntityTypes.HELLHOUND.get()
                    : ModEntityTypes.DIRE_WOLF.get();
            R196Wolf wolf = type.create(level, EntitySpawnReason.MOB_SUMMONED);
            if (wolf != null) {
                wolf.snapTo(getX() + random.nextInt(5) - 2, getY(), getZ() + random.nextInt(5) - 2, getYRot(), 0.0F);
                if (level.noCollision(wolf)) {
                    level.addFreshEntity(wolf);
                    wolf.setTarget(target);
                }
            }
        }
    }
}
