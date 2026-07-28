package com.pixulse.infx.event;

import com.pixulse.infx.data.harvest.HarvestPolicy;
import com.pixulse.infx.data.harvest.HarvestRequirements;
import com.pixulse.infx.data.harvest.HarvestSpeedRules;
import com.pixulse.infx.data.harvest.HarvestTier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;

import java.util.Optional;

import com.pixulse.infx.block.SafeBlock;
import com.pixulse.infx.block.entity.SafeBlockEntity;
import com.pixulse.infx.registry.tag.InfXBlockTags;
import com.pixulse.infx.registry.tag.InfXItemTags;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class HarvestEvents {
    private HarvestEvents() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void rejectInvalidMiningInput(PlayerInteractEvent.LeftClickBlock event) {
        if ((event.getAction() == PlayerInteractEvent.LeftClickBlock.Action.START
                        || event.getAction() == PlayerInteractEvent.LeftClickBlock.Action.CLIENT_HOLD)
                && !hasDestroyProgress(
                        event.getEntity(),
                        event.getLevel().getBlockState(event.getPos()),
                        event.getPos())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void enforceRestrictions(BreakBlockEvent event) {
        Player player = event.getPlayer();
        BlockState state = event.getState();
        if (!isAllowed(player, state, event.getPos())) {
            event.setCanceled(true);
            if (!event.getLevel().isClientSide()) {
                event.setNotifyClient(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void applyHarvestCapability(PlayerEvent.HarvestCheck event) {
        event.setCanHarvest(isAllowed(event.getEntity(), event.getTargetBlock(), event.getPos()));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void applyBreakSpeedRules(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        BlockState state = event.getState();
        BlockPos pos = event.getPosition().orElse(null);
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
                && (player.getAbilities().instabuild
                        || state.getDestroyProgress(player, player.level(), pos) > 0.0F);
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
        if (!state.is(InfXBlockTags.PORTABLE_HAND_HARVEST)) {
            return false;
        }
        if (!(state.getBlock() instanceof SafeBlock)) {
            return true;
        }
        return pos != null
                && player.level().getBlockEntity(pos) instanceof SafeBlockEntity safe
                && safe.isPortableTo(player);
    }

    private static Optional<HarvestTier> highestToolTier(ItemStack tool) {
        for (int index = HarvestTier.values().length - 1; index >= 0; index--) {
            HarvestTier tier = HarvestTier.values()[index];
            if (tool.is(InfXItemTags.toolTier(tier))) {
                return Optional.of(tier);
            }
        }
        return Optional.empty();
    }

}
