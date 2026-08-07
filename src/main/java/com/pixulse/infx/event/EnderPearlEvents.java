package com.pixulse.infx.event;

import com.pixulse.infx.InfiniteX;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;

/**
 * InfX ender pearl rules.
 *
 * <p>A vanilla ender pearl lands deal 5 damage through {@code DamageTypes.ENDER_PEARL}, whose
 * {@code message_id} is {@code fall} — players read it as fall damage. InfX cancels that damage
 * via the NeoForge hook on the pearl's teleport event.
 */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class EnderPearlEvents {
    private EnderPearlEvents() {}

    @SubscribeEvent
    public static void onEnderPearlLand(EntityTeleportEvent.EnderPearl event) {
        event.setAttackDamage(0.0F);
    }
}
