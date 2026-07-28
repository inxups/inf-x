package com.pixulse.infx.recipe;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;

import com.pixulse.infx.registry.InfXRecipes;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class TimedCraftingEvents {
    private TimedCraftingEvents() {}

    @SubscribeEvent

    private static void onDatapackSync(OnDatapackSyncEvent event) {
        event.sendRecipes(InfXRecipes.CRAFTING.get(), RecipeType.CRAFTING);
    }

    @SubscribeEvent

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player
                && player.containerMenu instanceof TimedCraftingMenu timedMenu) {
            timedMenu.infx$tickTimedCrafting(player);
        }
    }
}
