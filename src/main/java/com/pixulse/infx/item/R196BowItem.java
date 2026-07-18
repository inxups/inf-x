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
        super.shootProjectile(
                shooter,
                projectile,
                index,
                power * velocityMultiplier(),
                uncertainty,
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
            return super.releaseUsing(bow, level, entity, remainingTime);
        } finally {
            bow.remove(ModDataComponents.NOCKED_ARROW_MATERIAL.get());
        }
    }
}
