package com.pixulse.infx.entity;

import com.pixulse.infx.mixin.world.entity.animal.equine.HorseAccessor;
import com.pixulse.infx.registry.InfXEntityTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Donkey;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Markings;
import net.minecraft.world.entity.animal.equine.Mule;
import net.minecraft.world.entity.animal.equine.Variant;
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
 * Does <strong>not</strong> use livestock needs/disease (matches the original: horse is not EntityLivestock).
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

    /**
     * InfX wild horses rear and refuse any food for 4000 ticks after accepting one,
     * unless the food actually healed them (then they eat freely until full).
     */
    @Override
    protected boolean handleEating(@NonNull Player player, @NonNull ItemStack itemStack) {
        if (!isTamed()
                && level() instanceof ServerLevel serverLevel
                && Livestock.isHorseFeedBlocked(this, serverLevel.getGameTime())) {
            makeMad();
            return false;
        }
        float healthBefore = getHealth();
        boolean ate = super.handleEating(player, itemStack);
        if (ate
                && !isTamed()
                && level() instanceof ServerLevel serverLevel
                && (getHealth() <= healthBefore || getHealth() >= getMaxHealth())) {
            Livestock.markHorseFed(this, serverLevel.getGameTime());
        }
        return ate;
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
        if (partner instanceof Donkey) {
            // Horse x donkey keeps the vanilla mule, inheriting the parents' attributes.
            Mule baby = EntityType.MULE.create(level, EntitySpawnReason.BREEDING);
            if (baby != null) {
                this.setOffspringAttributes(partner, baby);
            }
            return baby;
        }
        // InfX foals inherit the coat/markings table and the parents' attributes like the vanilla
        // horse instead of spawning a blank coat with base stats.
        Horse horsePartner = (Horse) partner;
        Horse baby = InfXEntityTypes.INFX_HORSE.get().create(level, EntitySpawnReason.BREEDING);
        if (baby != null) {
            int selectSkin = this.random.nextInt(9);
            Variant variant;
            if (selectSkin < 4) {
                variant = this.getVariant();
            } else if (selectSkin < 8) {
                variant = horsePartner.getVariant();
            } else {
                variant = Util.getRandom(Variant.values(), this.random);
            }
            int selectMarking = this.random.nextInt(5);
            Markings markings;
            if (selectMarking < 2) {
                markings = this.getMarkings();
            } else if (selectMarking < 4) {
                markings = horsePartner.getMarkings();
            } else {
                markings = Util.getRandom(Markings.values(), this.random);
            }
            ((HorseAccessor) baby).infx$setVariantAndMarkings(variant, markings);
            this.setOffspringAttributes(partner, baby);
        }
        return baby;
    }
}
