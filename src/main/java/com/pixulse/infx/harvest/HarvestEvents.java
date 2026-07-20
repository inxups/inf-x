package com.pixulse.infx.harvest;

import java.util.Optional;

import com.pixulse.infx.tag.ModTags;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class HarvestEvents {
    private HarvestEvents() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(EventPriority.HIGHEST, HarvestEvents::enforceRestrictions);
        gameBus.addListener(EventPriority.HIGHEST, HarvestEvents::applyHarvestCapability);
        gameBus.addListener(HarvestEvents::applyBreakSpeedRules);
    }

    private static void enforceRestrictions(BreakBlockEvent event) {
        Player player = event.getPlayer();
        BlockState state = event.getState();
        if (!isAllowed(player, state)) {
            event.setCanceled(true);
            if (!event.getLevel().isClientSide()) {
                event.setNotifyClient(true);
            }
        }
    }

    private static void applyHarvestCapability(PlayerEvent.HarvestCheck event) {
        event.setCanHarvest(isAllowed(event.getEntity(), event.getTargetBlock()));
    }

    private static void applyBreakSpeedRules(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (!isAllowed(player, event.getState())) {
            event.setNewSpeed(0.0F);
            return;
        }
        float multiplier = HarvestSpeedRules.multiplier(
                player.experienceLevel,
                player.isInWater(),
                !player.onGround(),
                player.getFoodData().getFoodLevel() <= 0,
                HarvestSpeedRules.isParalyzed(player),
                HarvestSpeedRules.isInCobweb(player));
        event.setNewSpeed(event.getNewSpeed() * multiplier);
    }

    private static boolean isAllowed(Player player, BlockState state) {
        ItemStack tool = player.getMainHandItem();
        return HarvestPolicy.allows(
                player.getAbilities().instabuild,
                state.is(ModTags.Blocks.PORTABLE_HAND_HARVEST),
                tool.isCorrectToolForDrops(state),
                highestToolTier(tool).map(HarvestTier::level).orElse(0),
                HarvestRequirements.requiredLevel(state));
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
