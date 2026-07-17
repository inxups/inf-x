package com.pixulse.infx.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class R196ShearsItem extends ShearsItem {
    private final R196EquipmentKey key;

    public R196ShearsItem(R196EquipmentKey key, Properties properties) {
        super(properties);
        this.key = key;
    }

    public R196EquipmentKey key() {
        return key;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity owner) {
        R196ToolItem.applyMiningWear(key, stack, level, state, pos, owner);
        return stack.has(DataComponents.TOOL);
    }
}
