package com.pixulse.infx.item;

import com.pixulse.infx.harvest.MiteMiningRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * R196 material shears: no right-click block stance; melee is on right-click with a short cooldown.
 * Left-click entity attacks are cancelled by {@link ShearsEvents}.
 */
public final class MiteShearsItem extends ShearsItem {
    /** Short post-attack cooldown so right-click melee is not free spam (0.5 s). */
    public static final int ATTACK_COOLDOWN_TICKS = 10;

    private static final ThreadLocal<Boolean> RIGHT_CLICK_ATTACK = ThreadLocal.withInitial(() -> false);

    private final EquipmentKey key;

    public MiteShearsItem(EquipmentKey key, Properties properties) {
        super(properties);
        this.key = key;
    }

    public EquipmentKey key() {
        return key;
    }

    static boolean isRightClickAttack() {
        return Boolean.TRUE.equals(RIGHT_CLICK_ATTACK.get());
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return MiteMiningRules.destroySpeed(key, state);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return MiteMiningRules.canHarvest(key, state);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity owner) {
        ToolItem.applyMiningWear(key, stack, level, state, pos, owner);
        return stack.has(DataComponents.TOOL);
    }

    @Override
    public InteractionResult interactLivingEntity(
            ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        InteractionResult shear = super.interactLivingEntity(stack, player, entity, hand);
        if (shear.consumesAction()) {
            return shear;
        }
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }
        if (!entity.isAttackable() || entity.skipAttackInteraction(player)) {
            return InteractionResult.PASS;
        }
        if (player.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        RIGHT_CLICK_ATTACK.set(true);
        try {
            player.attack(entity);
        } finally {
            RIGHT_CLICK_ATTACK.set(false);
        }
        player.getCooldowns().addCooldown(stack, ATTACK_COOLDOWN_TICKS);
        return InteractionResult.SUCCESS;
    }
}
