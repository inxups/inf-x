package com.pixulse.infx.item;

import com.pixulse.infx.data.harvest.MiteMiningRules;
import com.pixulse.infx.registry.tag.InfXBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.CommonHooks;
import org.jspecify.annotations.NonNull;

/**
 * R196 material shears: right-click cuts shears-effective blocks and shearable entities, while
 * left-click is normal melee.
 */
public final class MiteShearsItem extends ShearsItem {
    private final EquipmentKey key;

    public MiteShearsItem(EquipmentKey key, Properties properties) {
        super(properties);
        this.key = key;
    }

    public EquipmentKey key() {
        return key;
    }

    @Override
    public float getDestroySpeed(@NonNull ItemStack stack, @NonNull BlockState state) {
        return MiteMiningRules.destroySpeed(key, state);
    }

    @Override
    public boolean isCorrectToolForDrops(@NonNull ItemStack stack, @NonNull BlockState state) {
        return MiteMiningRules.canHarvest(key, state);
    }

    @Override
    public @NonNull InteractionResult useOn(@NonNull UseOnContext context) {
        InteractionResult vanilla = super.useOn(context);
        if (vanilla.consumesAction()) {
            return vanilla;
        }

        BlockPos pos = context.getClickedPos();
        BlockState state = context.getLevel().getBlockState(pos);
        if (!state.is(InfXBlockTags.effectiveWith(MiningFamily.SHEARS))) {
            return vanilla;
        }

        Player player = context.getPlayer();
        if (player == null) {
            return vanilla;
        }
        if (!context.getLevel().mayInteract(player, pos)
                || !player.mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand())
                || player.blockActionRestricted(context.getLevel(), pos, player.gameMode())) {
            return InteractionResult.FAIL;
        }
        if (context.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (CommonHooks.fireBlockBreak(context.getLevel(), player.gameMode(), player, pos, state).isCanceled()) {
            return InteractionResult.FAIL;
        }

        return shearBlock(context, player, state) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    @Override
    public boolean mineBlock(@NonNull ItemStack stack, @NonNull Level level, @NonNull BlockState state, @NonNull BlockPos pos, @NonNull LivingEntity owner) {
        ToolItem.applyMiningWear(key, stack, level, state, pos, owner);
        return stack.has(DataComponents.TOOL);
    }

    private static boolean shearBlock(UseOnContext context, Player player, BlockState state) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Block block = state.getBlock();
        var blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
        BlockState destroyedState = block.playerWillDestroy(level, pos, state, player);
        ItemStack stack = context.getItemInHand();
        ItemStack originalStack = stack.copy();

        if (player.preventsBlockDrops()) {
            return removeBlock(level, pos, destroyedState, player, false, originalStack);
        }

        boolean canHarvest = destroyedState.canHarvestBlock(level, pos, player);

        stack.mineBlock(level, destroyedState, pos, player);
        boolean destroyed = removeBlock(level, pos, destroyedState, player, canHarvest, originalStack);
        if (destroyed && canHarvest) {
            block.playerDestroy(level, player, pos, destroyedState, blockEntity, originalStack);
        }
        return destroyed;
    }

    private static boolean removeBlock(
            Level level,
            BlockPos pos,
            BlockState state,
            Player player,
            boolean canHarvest,
            ItemStack tool) {
        boolean destroyed = state.onDestroyedByPlayer(
                level,
                pos,
                player,
                tool.copy(),
                canHarvest,
                level.getFluidState(pos));
        if (destroyed) {
            state.getBlock().destroy(level, pos, state);
        }
        return destroyed;
    }
}
