package com.pixulse.infx.item;

import com.pixulse.infx.InfiniteX;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

/** Cancels left-click melee with R196 shears; right-click attack is allowed via a re-entry flag. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class ShearsEvents {
    private ShearsEvents() {}

    @SubscribeEvent
    private static void cancelLeftClickShearsAttack(AttackEntityEvent event) {
        if (MiteShearsItem.isRightClickAttack()) {
            return;
        }
        if (event.getEntity().getMainHandItem().getItem() instanceof MiteShearsItem) {
            event.setCanceled(true);
        }
    }
}
