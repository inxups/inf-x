package com.pixulse.infx.entity;

import com.pixulse.infx.registry.InfXEntityTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * INFX vanilla-wolf replacement (separate from hellhound/dire wolf).
 * Reuses 26.2 wolf model/textures; blood/blue moon rules apply via moon events.
 */
public final class VanillaWolf extends Wolf implements InfxTameableWolf {
    private int tamingCooldown;

    public VanillaWolf(EntityType<? extends Wolf> type, Level level) {
        super(type, level);
    }

    @Override
    public int tamingCooldown() {
        return tamingCooldown;
    }

    @Override
    public void setTamingCooldown(int ticks) {
        tamingCooldown = ticks;
    }

    /** MITE wolves are worth the base experience value. */
    @Override
    public int getBaseExperienceReward(@NonNull ServerLevel level) {
        return 5;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (isTame()) {
            return super.mobInteract(player, hand);
        }
        if (!level().isClientSide() && itemStack.is(Items.BONE) && !isAngry()) {
            itemStack.consume(1, player);
            WolfTaming.attempt(this, WolfTaming.Kind.VANILLA, player, this);
            return InteractionResult.SUCCESS_SERVER;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (tamingCooldown > 0) {
            tamingCooldown--;
        }
    }

    public static AttributeSupplier.Builder attributes() {
        return Wolf.createAttributes()
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    public boolean isWithinMeleeAttackRange(@NonNull LivingEntity target) {
        return AttackRanges.withinWolfReach(this, target);
    }

    /** MITE tamed wolves cap at 12 health and 32 follow range, not the modern 40 health. */
    @Override
    public void setTame(boolean tame, boolean applySideEffects) {
        super.setTame(tame, applySideEffects);
        var health = getAttribute(Attributes.MAX_HEALTH);
        var range = getAttribute(Attributes.FOLLOW_RANGE);
        if (health == null || range == null) {
            return;
        }
        double maximumHealth = isTame() ? 12.0 : 8.0;
        double oldMaximumHealth = health.getBaseValue();
        health.setBaseValue(maximumHealth);
        range.setBaseValue(isTame() ? 32.0 : 16.0);
        if (maximumHealth > oldMaximumHealth) {
            setHealth(getHealth() + (float) (maximumHealth - oldMaximumHealth));
        } else if (getHealth() > maximumHealth) {
            setHealth((float) maximumHealth);
        }
    }

    /** MITE: untamed wolves may fade out after two minutes. */
    @Override
    public boolean removeWhenFarAway(double distance) {
        return !isTame() && tickCount > 2400;
    }

    /** MITE wolves shrug off half the damage from non-player, non-arrow attackers. */
    @Override
    public boolean hurtServer(@NonNull ServerLevel level, DamageSource source, float damage) {
        if (source.getEntity() != null
                && !(source.getEntity() instanceof Player)
                && !(source.getDirectEntity() instanceof AbstractArrow)) {
            damage = (damage + 1.0F) / 2.0F;
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    public @Nullable Wolf getBreedOffspring(@NonNull ServerLevel level, @NonNull AgeableMob partner) {
        return InfXEntityTypes.INFX_WOLF.get().create(level, EntitySpawnReason.BREEDING);
    }
}
