package com.pixulse.infx.item;

import com.pixulse.infx.registry.ModDataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class R196BowItem extends BowItem {
    private final R196EquipmentKey key;

    public R196BowItem(R196EquipmentKey key, Properties properties) {
        super(properties);
        this.key = key;
    }

    public R196EquipmentKey key() {
        return key;
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
