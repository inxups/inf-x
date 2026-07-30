package com.pixulse.infx.item;

import com.pixulse.infx.data.harvest.InfxMiningRules;
import com.pixulse.infx.registry.tag.InfXBlockTags;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.CommonHooks;
import org.jspecify.annotations.NonNull;

/**
 * INFX material shears: right-click cuts shears-effective blocks and shearable entities, while
 * left-click is normal melee.
 */
public final class InfxShearsItem extends ShearsItem {
    private static final ThreadLocal<ShearingContext> RIGHT_CLICK_SHEARING = new ThreadLocal<>();

    private final EquipmentKey key;

    public InfxShearsItem(EquipmentKey key, Properties properties) {
        super(properties);
        this.key = key;
    }

    public EquipmentKey key() {
        return key;
    }

    static boolean isRightClickShearing(LevelAccessor level, BlockPos pos) {
        ShearingContext context = RIGHT_CLICK_SHEARING.get();
        return context != null && context.level() == level && context.pos().equals(pos);
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
        return withRightClickShearing(
                        context.getLevel(),
                        pos,
                        () -> !CommonHooks.fireBlockBreak(context.getLevel(), player.gameMode(), player, pos, state)
                                        .isCanceled()
                                && shearBlock(context, player, state))
                ? InteractionResult.SUCCESS
                : InteractionResult.FAIL;
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
            dropShearedBlock(level, pos, player, block, destroyedState, blockEntity, originalStack);
        }
        if (destroyed) {
            level.playSound(null, pos, SoundEvents.SHEARS_SNIP, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return destroyed;
    }

    private static void dropShearedBlock(
            Level level,
            BlockPos pos,
            Player player,
            Block block,
            BlockState state,
            BlockEntity blockEntity,
            ItemStack tool) {
        ItemStack silkTouchTool = tool.copy();
        silkTouchTool.enchant(
                level.registryAccess()
                        .lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(Enchantments.SILK_TOUCH),
                1);

        block.playerDestroy(level, player, pos, state, blockEntity, silkTouchTool);
    }

    private static boolean withRightClickShearing(Level level, BlockPos pos, BooleanSupplier action) {
        ShearingContext previous = RIGHT_CLICK_SHEARING.get();
        RIGHT_CLICK_SHEARING.set(new ShearingContext(level, pos.immutable()));
        try {
            return action.getAsBoolean();
        } finally {
            if (previous == null) {
                RIGHT_CLICK_SHEARING.remove();
            } else {
                RIGHT_CLICK_SHEARING.set(previous);
            }
        }
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

    private record ShearingContext(LevelAccessor level, BlockPos pos) {}
}
