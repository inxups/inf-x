package com.pixulse.infx.item;

import com.pixulse.infx.harvest.ToolWearApplication;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbility;

public class R196ToolItem extends Item {
    private final R196EquipmentKey key;

    public R196ToolItem(R196EquipmentKey key, Properties properties) {
        super(properties);
        this.key = key;
    }

    public R196EquipmentKey key() {
        return key;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return key.type().useAction().useOn(context);
    }

    @Override
    public boolean canPerformAction(ItemInstance stack, ItemAbility ability) {
        return key.type().useAction().canPerformAction(stack, ability);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity owner) {
        applyMiningWear(key, stack, level, state, pos, owner);
        return stack.has(DataComponents.TOOL);
    }

    static void applyMiningWear(
            R196EquipmentKey key,
            ItemStack stack,
            Level level,
            BlockState state,
            BlockPos pos,
            LivingEntity owner) {
        if (!level.isClientSide() && state.getDestroySpeed(level, pos) != 0.0F
                && stack.getDestroySpeed(state) > 1.0F) {
            ToolWearApplication.afterHarvestSnapshot(
                    state.getDestroySpeed(level, pos),
                    key.type().blockDecay(state),
                    damage -> stack.hurtAndBreak(damage, owner, EquipmentSlot.MAINHAND));
        }
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
    }
}
