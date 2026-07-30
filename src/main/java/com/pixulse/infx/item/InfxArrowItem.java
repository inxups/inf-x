package com.pixulse.infx.item;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class InfxArrowItem extends ArrowItem {
    private final EquipmentKey key;

    public InfxArrowItem(EquipmentKey key, Properties properties) {
        super(properties);
        this.key = key;
    }

    public EquipmentKey key() {
        return key;
    }

    public double baseDamage() {
        return key.arrowBaseDamage();
    }

    @Override
    public @NonNull AbstractArrow createArrow(
            @NonNull Level level, ItemStack stack, @NonNull LivingEntity owner, @Nullable ItemStack weapon) {
        Arrow arrow = new Arrow(level, owner, stack.copyWithCount(1), weapon);
        if (!stack.has(DataComponents.INTANGIBLE_PROJECTILE)) {
            arrow.pickup = AbstractArrow.Pickup.ALLOWED;
        }
        arrow.setBaseDamage(baseDamage());
        return arrow;
    }

    @Override
    public @NonNull Projectile asProjectile(@NonNull Level level, Position position, ItemStack stack, @NonNull Direction direction) {
        Arrow arrow = new Arrow(level, position.x(), position.y(), position.z(), stack.copyWithCount(1), null);
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        arrow.setBaseDamage(baseDamage());
        return arrow;
    }
}
