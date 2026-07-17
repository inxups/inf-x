package com.pixulse.infx.item;

import com.pixulse.infx.harvest.ToolWearApplication;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class R196ToolItem extends Item {
    private final float baseDecayRate;

    public R196ToolItem(float baseDecayRate, Properties properties) {
        super(properties);
        this.baseDecayRate = baseDecayRate;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity owner) {
        if (!level.isClientSide()) {
            ToolWearApplication.afterHarvestSnapshot(
                    state.getDestroySpeed(level, pos),
                    baseDecayRate,
                    damage -> stack.hurtAndBreak(damage, owner, EquipmentSlot.MAINHAND));
        }
        return true;
    }
}
