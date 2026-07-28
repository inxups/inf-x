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
import org.jspecify.annotations.Nullable;

public final class MiteArrowItem extends ArrowItem {
    private final EquipmentKey key;

    public MiteArrowItem(EquipmentKey key, Properties properties) {
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
    public AbstractArrow createArrow(
            Level level, ItemStack stack, LivingEntity owner, @Nullable ItemStack weapon) {
        Arrow arrow = new Arrow(level, owner, stack.copyWithCount(1), weapon);
        if (!stack.has(DataComponents.INTANGIBLE_PROJECTILE)) {
            arrow.pickup = AbstractArrow.Pickup.ALLOWED;
        }
        arrow.setBaseDamage(baseDamage());
        return arrow;
    }

    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack stack, Direction direction) {
        Arrow arrow = new Arrow(level, position.x(), position.y(), position.z(), stack.copyWithCount(1), null);
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        arrow.setBaseDamage(baseDamage());
        return arrow;
    }
}
