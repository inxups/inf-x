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
        gameBus.addListener(HarvestEvents::applyBreakSpeedRules);
    }

    private static void enforceRestrictions(BreakBlockEvent event) {
        Player player = event.getPlayer();
        BlockState state = event.getState();
        ItemStack tool = player.getMainHandItem();

        boolean allowed = state.is(ModTags.Blocks.PORTABLE_HAND_HARVEST) || HarvestPolicy.allows(
                player.getAbilities().instabuild,
                state.is(ModTags.Blocks.RESTRICTED_HARVEST),
                tool.isCorrectToolForDrops(state),
                highestToolTier(tool),
                highestRequiredTier(state));

        if (!allowed) {
            event.setCanceled(true);
            if (!event.getLevel().isClientSide()) {
                event.setNotifyClient(true);
            }
        }
    }

    private static void applyBreakSpeedRules(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        float multiplier = HarvestSpeedRules.multiplier(
                player.experienceLevel,
                player.isInWater(),
                !player.onGround(),
                player.getFoodData().getFoodLevel() <= 0,
                HarvestSpeedRules.isParalyzed(player),
                HarvestSpeedRules.isInCobweb(player));
        event.setNewSpeed(event.getOriginalSpeed() * multiplier);
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

    private static Optional<HarvestTier> highestRequiredTier(BlockState state) {
        for (int index = HarvestTier.values().length - 1; index >= 0; index--) {
            HarvestTier tier = HarvestTier.values()[index];
            if (state.is(ModTags.Blocks.requiredTier(tier))) {
                return Optional.of(tier);
            }
        }
        return Optional.empty();
    }
}
