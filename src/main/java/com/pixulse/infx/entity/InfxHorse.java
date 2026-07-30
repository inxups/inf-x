package com.pixulse.infx.entity;

import com.pixulse.infx.registry.InfXEntityTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * INFX horse: untamed remount cooldown and beef drop only.
 * Does <strong>not</strong> use livestock needs/disease (matches MITE: horse is not EntityLivestock).
 */
public final class InfxHorse extends Horse {
    private static final String GOALS_ADDED = "infx_horse_goals_added";

    public InfxHorse(EntityType<? extends Horse> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return AbstractHorse.createBaseHorseAttributes();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        if (getPersistentData().getBooleanOr(GOALS_ADDED, false)) return;
        // Flee hostiles; untamed horses also keep distance from players (no needs/disease AI).
        goalSelector.addGoal(1, new AvoidEntityGoal<>(
                this,
                Mob.class,
                mob -> mob instanceof Enemy,
                10.0F,
                1.15,
                1.4,
                LivingEntity::isAlive));
        goalSelector.addGoal(3, new AvoidEntityGoal<>(
                this,
                Player.class,
                player -> !isTamed(),
                10.0F,
                1.1,
                1.35,
                entity -> !entity.isSpectator()));
        getPersistentData().putBoolean(GOALS_ADDED, true);
    }

    @Override
    public @NonNull InteractionResult mobInteract(@NonNull Player player, @NonNull InteractionHand hand) {
        if (!isTamed()
                && level() instanceof ServerLevel serverLevel
                && Livestock.isHorseMountBlocked(this, serverLevel.getGameTime())) {
            return InteractionResult.CONSUME;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void removePassenger(@NonNull Entity passenger) {
        super.removePassenger(passenger);
        if (!level().isClientSide()
                && passenger instanceof Player
                && level() instanceof ServerLevel serverLevel) {
            Livestock.markHorseDismount(this, serverLevel.getGameTime());
        }
    }

    @Override
    public void die(@NonNull DamageSource source) {
        if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
            serverLevel.addFreshEntity(new ItemEntity(
                    serverLevel,
                    getX(),
                    getY(),
                    getZ(),
                    new ItemStack(Items.BEEF, 1 + getRandom().nextInt(3))));
        }
        super.die(source);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(@NonNull ServerLevel level, @NonNull AgeableMob partner) {
        return InfXEntityTypes.INFX_HORSE.get().create(level, EntitySpawnReason.BREEDING);
    }
}
