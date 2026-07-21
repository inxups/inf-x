package com.pixulse.infx.harvest;

import java.util.Optional;

import com.pixulse.infx.block.R196SafeBlock;
import com.pixulse.infx.block.entity.R196SafeBlockEntity;
import com.pixulse.infx.tag.ModTags;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jspecify.annotations.Nullable;

public final class HarvestEvents {
    private HarvestEvents() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(EventPriority.HIGHEST, HarvestEvents::rejectInvalidMiningInput);
        gameBus.addListener(EventPriority.HIGHEST, HarvestEvents::enforceRestrictions);
        gameBus.addListener(EventPriority.HIGHEST, HarvestEvents::applyHarvestCapability);
        gameBus.addListener(EventPriority.LOWEST, HarvestEvents::applyBreakSpeedRules);
    }

    private static void rejectInvalidMiningInput(PlayerInteractEvent.LeftClickBlock event) {
        if ((event.getAction() == PlayerInteractEvent.LeftClickBlock.Action.START
                        || event.getAction() == PlayerInteractEvent.LeftClickBlock.Action.CLIENT_HOLD)
                && !hasDestroyProgress(
                        event.getEntity(),
                        event.getLevel().getBlockState(event.getPos()),
                        event.getPos())) {
            event.setCanceled(true);
        }
    }

    private static void enforceRestrictions(BreakBlockEvent event) {
        Player player = event.getPlayer();
        BlockState state = event.getState();
        if (!isAllowed(player, state, event.getPos())) {
            event.setCanceled(true);
            if (!event.getLevel().isClientSide()) {
                event.setNotifyClient(true);
            }
        }
    }

    private static void applyHarvestCapability(PlayerEvent.HarvestCheck event) {
        event.setCanHarvest(isAllowed(event.getEntity(), event.getTargetBlock(), event.getPos()));
    }

    private static void applyBreakSpeedRules(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        BlockState state = event.getState();
        @Nullable BlockPos pos = event.getPosition().orElse(null);
        if (!isAllowed(player, state, pos)) {
            event.setNewSpeed(0.0F);
            return;
        }
        float hardness = pos == null ? -1.0F : state.getDestroySpeed(player.level(), pos);
        event.setNewSpeed(HarvestSpeedRules.adjustedBreakSpeed(
                player,
                event.getNewSpeed(),
                hardness,
                isPortable(player, state, pos)));
    }

    public static boolean hasDestroyProgress(Player player, BlockState state, BlockPos pos) {
        return !state.isAir()
                && state.getDestroyProgress(player, player.level(), pos) > 0.0F;
    }

    private static boolean isAllowed(Player player, BlockState state, @Nullable BlockPos pos) {
        ItemStack tool = player.getMainHandItem();
        return HarvestPolicy.allows(
                player.getAbilities().instabuild,
                isPortable(player, state, pos),
                tool.isCorrectToolForDrops(state),
                highestToolTier(tool).map(HarvestTier::level).orElse(0),
                HarvestRequirements.requiredLevel(state));
    }

    private static boolean isPortable(Player player, BlockState state, @Nullable BlockPos pos) {
        if (!state.is(ModTags.Blocks.PORTABLE_HAND_HARVEST)) {
            return false;
        }
        if (!(state.getBlock() instanceof R196SafeBlock)) {
            return true;
        }
        return pos != null
                && player.level().getBlockEntity(pos) instanceof R196SafeBlockEntity safe
                && safe.isPortableTo(player);
    }

    private static Optional<HarvestTier> highestToolTier(ItemStack tool) {
        for (int index = HarvestTier.values().length - 1; index >= 0; index--) {
            HarvestTier tier = HarvestTier.values()[index];
            if (tool.is(ModTags.Items.toolTier(tier))) {
                return Optional.of(tier);
            }
        }
        return Optional.empty();
    }

}
