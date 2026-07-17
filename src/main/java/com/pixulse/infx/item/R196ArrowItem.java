package com.pixulse.infx.item;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class R196ArrowItem extends ArrowItem {
    private final R196EquipmentKey key;

    public R196ArrowItem(R196EquipmentKey key, Properties properties) {
        super(properties);
        this.key = key;
    }

    public R196EquipmentKey key() {
        return key;
    }

    public double baseDamage() {
        return key.arrowBaseDamage();
    }

    @Override
    public AbstractArrow createArrow(
            Level level, ItemStack stack, LivingEntity owner, @Nullable ItemStack weapon) {
        Arrow arrow = new Arrow(level, owner, stack.copyWithCount(1), weapon);
        arrow.setBaseDamage(baseDamage());
        return arrow;
    }

    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack stack, Direction direction) {
        Arrow arrow = new Arrow(level, position.x(), position.y(), position.z(), stack.copyWithCount(1), null);
        arrow.pickup = AbstractArrow.Pickup.ALLOWED;
        arrow.setBaseDamage(baseDamage());
        return arrow;
    }
}
