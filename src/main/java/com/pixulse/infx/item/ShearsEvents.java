package com.pixulse.infx.item;

import com.pixulse.infx.InfiniteX;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

/** Cancels left-click melee with R196 shears; right-click attack is allowed via a re-entry flag. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class ShearsEvents {
    private ShearsEvents() {}

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void cancelLeftClickShearsAttack(AttackEntityEvent event) {
        if (MiteShearsItem.isRightClickAttack()) {
            MiteShearsItem.recordRightClickAttackCancellation(event.isCanceled());
            return;
        }
        if (event.getEntity().getMainHandItem().getItem() instanceof MiteShearsItem) {
            event.setCanceled(true);
        }
    }
}
