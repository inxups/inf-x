package com.pixulse.infx.item;

import com.pixulse.infx.item.material.MiteMaterial;
import com.pixulse.infx.registry.InfXDataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.projectile.Projectile;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import com.pixulse.infx.item.enchantment.Enchantments;
import com.pixulse.infx.item.enchantment.EnchantmentRules;
import com.pixulse.infx.registry.InfXEnchantments;

public final class MiteBowItem extends BowItem {
    private final EquipmentKey key;

    public MiteBowItem(EquipmentKey key, Properties properties) {
        super(properties);
        this.key = key;
    }

    public EquipmentKey key() {
        return key;
    }

    public float velocityMultiplier() {
        return velocityMultiplier(key.material());
    }

    public static float velocityMultiplier(MiteMaterial material) {
        return switch (material) {
            case ANCIENT_METAL -> 1.10F;
            case MITHRIL -> 1.25F;
            default -> 1.0F;
        };
    }

    @Override
    protected void shootProjectile(
            LivingEntity shooter,
            @NonNull Projectile projectile,
            int index,
            float power,
            float uncertainty,
            float angle,
            @Nullable LivingEntity targetOverride) {
        ItemStack bow = shooter.getMainHandItem();
        int precision = Enchantments.level(shooter.level(), bow, InfXEnchantments.PRECISION);
        int recovery = Enchantments.level(shooter.level(), bow, InfXEnchantments.RECOVERY);
        int poisoning = Enchantments.level(shooter.level(), bow, InfXEnchantments.POISONING);
        int experienceLevel = shooter instanceof Player player ? player.experienceLevel : 0;
        if (recovery > 0) projectile.getPersistentData().putInt("infx_recovery_enchantment", recovery);
        if (poisoning > 0) projectile.getPersistentData().putInt("infx_poisoning_enchantment", poisoning);
        super.shootProjectile(
                shooter,
                projectile,
                index,
                power * velocityMultiplier(),
                uncertainty * EnchantmentRules.precisionUncertaintyMultiplier(experienceLevel, precision),
                angle,
                targetOverride);
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, Player player, @NonNull InteractionHand hand) {
        ItemStack bow = player.getItemInHand(hand);
        ItemStack projectile = player.getProjectile(bow);
        if (projectile.getItem() instanceof MiteArrowItem arrow) {
            bow.set(InfXDataComponents.NOCKED_ARROW_MATERIAL.get(), arrow.key().material().path());
        } else {
            bow.remove(InfXDataComponents.NOCKED_ARROW_MATERIAL.get());
        }
        InteractionResult result = super.use(level, player, hand);
        if (!result.consumesAction()) {
            bow.remove(InfXDataComponents.NOCKED_ARROW_MATERIAL.get());
        }
        return result;
    }

    @Override
    public boolean releaseUsing(@NonNull ItemStack bow, @NonNull Level level, @NonNull LivingEntity entity, int remainingTime) {
        try {
            int quickness = Enchantments.level(level, bow, InfXEnchantments.QUICKNESS);
            int duration = getUseDuration(bow, entity);
            int used = Math.max(0, duration - remainingTime);
            int adjustedRemaining = Math.max(0,
                    duration - EnchantmentRules.quicknessAdjustedUseTicks(used, quickness));
            return super.releaseUsing(bow, level, entity, adjustedRemaining);
        } finally {
            bow.remove(InfXDataComponents.NOCKED_ARROW_MATERIAL.get());
        }
    }
}
