package com.pixulse.infx.item;

import com.pixulse.infx.InfiniteX;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

/** Keeps material-shears block cutting exclusive to their right-click action. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class ShearsEvents {
    private ShearsEvents() {}

    @SubscribeEvent
    public static void preventLeftClickBlockBreaking(BreakBlockEvent event) {
        if (event.getPlayer().getMainHandItem().getItem() instanceof MiteShearsItem
                && !MiteShearsItem.isRightClickShearing(event.getLevel(), event.getPos())) {
            event.setCanceled(true);
            event.setNotifyClient(true);
        }
    }
}
