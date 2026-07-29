package com.pixulse.infx.item;

import com.pixulse.infx.InfiniteX;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/** Keeps material-shears block drops exclusive to their right-click cutting action. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class ShearsEvents {
    private ShearsEvents() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void suppressLeftClickDrops(BlockDropsEvent event) {
        if (event.getBreaker() instanceof Player
                && event.getTool().getItem() instanceof MiteShearsItem
                && !MiteShearsItem.isRightClickShearing(event.getLevel(), event.getPos())) {
            event.setCanceled(true);
        }
    }
}
