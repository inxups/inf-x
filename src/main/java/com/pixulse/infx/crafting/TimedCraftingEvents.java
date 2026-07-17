package com.pixulse.infx.crafting;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class TimedCraftingEvents {
    private TimedCraftingEvents() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(TimedCraftingEvents::onPlayerTick);
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player
                && player.containerMenu instanceof TimedCraftingMenu timedMenu) {
            timedMenu.infx$tickTimedCrafting(player);
        }
    }
}
