package com.pixulse.infx.item;

import com.pixulse.infx.data.harvest.ToolWearApplication;
import com.pixulse.infx.data.harvest.InfxMiningRules;

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
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.jspecify.annotations.NonNull;

public class ToolItem extends Item {
    private final EquipmentKey key;

    public ToolItem(EquipmentKey key, Properties properties) {
        super(properties);
        this.key = key;
    }

    public EquipmentKey key() {
        return key;
    }

    @Override
    public @NonNull InteractionResult useOn(@NonNull UseOnContext context) {
        return key.type().useAction().useOn(context);
    }

    @Override
    public boolean canPerformAction(@NonNull ItemInstance stack, @NonNull ItemAbility ability) {
        if (ability == ItemAbilities.SWORD_SWEEP) {
            return key.type().supportsSweepAttack();
        }
        return key.type().useAction().canPerformAction(stack, ability);
    }

    @Override
    public float getDestroySpeed(@NonNull ItemStack stack, @NonNull BlockState state) {
        return InfxMiningRules.destroySpeed(key, state);
    }

    @Override
    public boolean isCorrectToolForDrops(@NonNull ItemStack stack, @NonNull BlockState state) {
        return InfxMiningRules.canHarvest(key, state);
    }

    @Override
    public boolean mineBlock(@NonNull ItemStack stack, @NonNull Level level, @NonNull BlockState state, @NonNull BlockPos pos, @NonNull LivingEntity owner) {
        applyMiningWear(key, stack, level, state, pos, owner);
        return stack.has(DataComponents.TOOL);
    }

    static void applyMiningWear(
            EquipmentKey key,
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
    public void postHurtEnemy(@NonNull ItemStack stack, @NonNull LivingEntity target, @NonNull LivingEntity attacker) {
    }
}
