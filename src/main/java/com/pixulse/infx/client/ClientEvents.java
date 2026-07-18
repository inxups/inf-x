package com.pixulse.infx.client;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.crafting.TimedCraftingRecipe;
import com.pixulse.infx.registry.ModMenus;
import com.pixulse.infx.registry.ModRecipes;

import java.util.Collection;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;

@EventBusSubscriber(modid = InfiniteX.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {
    private static RecipeMap syncedRecipes = RecipeMap.EMPTY;

    private ClientEvents() {}

    @SubscribeEvent
    private static void registerScreens(RegisterMenuScreensEvent event) {
        ModMenus.WORKBENCHES.forEach(menu -> event.register(menu.get(), TimedWorkbenchScreen::new));
    }

    @SubscribeEvent
    private static void receiveRecipes(RecipesReceivedEvent event) {
        if (event.getRecipeTypes().contains(ModRecipes.CRAFTING.get())) {
            syncedRecipes = event.getRecipeMap();
        }
    }

    @SubscribeEvent
    private static void clearRecipes(ClientPlayerNetworkEvent.LoggingOut event) {
        syncedRecipes = RecipeMap.EMPTY;
    }

    public static Collection<RecipeHolder<TimedCraftingRecipe>> timedCraftingRecipes() {
        return syncedRecipes.byType(ModRecipes.CRAFTING.get());
    }
}
