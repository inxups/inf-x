package com.pixulse.infx.crafting;

import com.pixulse.infx.registry.ModRecipes;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class TimedCraftingEvents {
    private TimedCraftingEvents() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(TimedCraftingEvents::onPlayerTick);
        gameBus.addListener(TimedCraftingEvents::onDatapackSync);
    }

    private static void onDatapackSync(OnDatapackSyncEvent event) {
        event.sendRecipes(ModRecipes.CRAFTING.get());
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player
                && player.containerMenu instanceof TimedCraftingMenu timedMenu) {
            timedMenu.infx$tickTimedCrafting(player);
        }
    }
}
