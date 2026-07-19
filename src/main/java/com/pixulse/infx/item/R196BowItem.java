package com.pixulse.infx.item;

import com.pixulse.infx.registry.ModDataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.projectile.Projectile;
import org.jspecify.annotations.Nullable;
import com.pixulse.infx.enchantment.R196Enchantments;
import com.pixulse.infx.registry.ModEnchantments;

public final class R196BowItem extends BowItem {
    private final R196EquipmentKey key;

    public R196BowItem(R196EquipmentKey key, Properties properties) {
        super(properties);
        this.key = key;
    }

    public R196EquipmentKey key() {
        return key;
    }

    public float velocityMultiplier() {
        return velocityMultiplier(key.material());
    }

    public static float velocityMultiplier(com.pixulse.infx.material.R196Material material) {
        return switch (material) {
            case ANCIENT_METAL -> 1.10F;
            case MITHRIL -> 1.25F;
            default -> 1.0F;
        };
    }

    @Override
    protected void shootProjectile(
            LivingEntity shooter,
            Projectile projectile,
            int index,
            float power,
            float uncertainty,
            float angle,
            @Nullable LivingEntity targetOverride) {
        ItemStack bow = shooter.getMainHandItem();
        int precision = R196Enchantments.level(shooter.level(), bow, ModEnchantments.PRECISION);
        int recovery = R196Enchantments.level(shooter.level(), bow, ModEnchantments.RECOVERY);
        int poisoning = R196Enchantments.level(shooter.level(), bow, ModEnchantments.POISONING);
        if (recovery > 0) projectile.getPersistentData().putInt("infx_recovery_enchantment", recovery);
        if (poisoning > 0) projectile.getPersistentData().putInt("infx_poisoning_enchantment", poisoning);
        super.shootProjectile(
                shooter,
                projectile,
                index,
                power * velocityMultiplier(),
                uncertainty * Math.max(0.15F, 1.0F - precision * 0.2F),
                angle,
                targetOverride);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack bow = player.getItemInHand(hand);
        ItemStack projectile = player.getProjectile(bow);
        if (projectile.getItem() instanceof R196ArrowItem arrow) {
            bow.set(ModDataComponents.NOCKED_ARROW_MATERIAL.get(), arrow.key().material().path());
        } else {
            bow.remove(ModDataComponents.NOCKED_ARROW_MATERIAL.get());
        }
        InteractionResult result = super.use(level, player, hand);
        if (!result.consumesAction()) {
            bow.remove(ModDataComponents.NOCKED_ARROW_MATERIAL.get());
        }
        return result;
    }

    @Override
    public boolean releaseUsing(ItemStack bow, Level level, LivingEntity entity, int remainingTime) {
        try {
            int quickness = R196Enchantments.level(level, bow, ModEnchantments.QUICKNESS);
            int duration = getUseDuration(bow, entity);
            int used = Math.max(0, duration - remainingTime);
            int adjustedRemaining = duration - Math.round(used * (1.0F + quickness * 0.2F));
            return super.releaseUsing(bow, level, entity, adjustedRemaining);
        } finally {
            bow.remove(ModDataComponents.NOCKED_ARROW_MATERIAL.get());
        }
    }
}
