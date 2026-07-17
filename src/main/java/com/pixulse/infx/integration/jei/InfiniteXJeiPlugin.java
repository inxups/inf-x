package com.pixulse.infx.integration.jei;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.client.ClientEvents;
import com.pixulse.infx.client.TimedWorkbenchScreen;
import com.pixulse.infx.crafting.BenchTier;
import com.pixulse.infx.crafting.TimedCraftingRecipe;
import com.pixulse.infx.menu.TimedWorkbenchMenu;
import com.pixulse.infx.registry.ModItems;
import com.pixulse.infx.registry.ModMenus;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;

@JeiPlugin
public final class InfiniteXJeiPlugin implements IModPlugin {
    private static final Identifier PLUGIN_ID = InfiniteX.id("jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new TimedCraftingRecipeCategory(guiHelper, BenchTier.HAND, Items.CRAFTING_TABLE),
                new TimedCraftingRecipeCategory(guiHelper, BenchTier.FLINT, ModItems.FLINT_WORKBENCH.get()),
                new TimedCraftingRecipeCategory(guiHelper, BenchTier.COPPER, ModItems.COPPER_WORKBENCH.get()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Map<BenchTier, List<RecipeHolder<TimedCraftingRecipe>>> recipesByBench =
                new EnumMap<>(BenchTier.class);
        for (BenchTier benchTier : BenchTier.values()) {
            recipesByBench.put(benchTier, new ArrayList<>());
        }

        for (RecipeHolder<TimedCraftingRecipe> holder : ClientEvents.timedCraftingRecipes()) {
            recipesByBench.get(holder.value().requiredBench()).add(holder);
        }

        int recipeCount = recipesByBench.values().stream().mapToInt(List::size).sum();
        InfiniteX.LOGGER.debug("Registering {} timed crafting recipes with JEI", recipeCount);
        for (BenchTier benchTier : BenchTier.values()) {
            registration.addRecipes(
                    TimedCraftingJeiTypes.forBench(benchTier), recipesByBench.get(benchTier));
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(
                TimedCraftingJeiTypes.HAND,
                ModItems.FLINT_WORKBENCH.get(),
                ModItems.COPPER_WORKBENCH.get());
        registration.addCraftingStation(
                TimedCraftingJeiTypes.FLINT,
                ModItems.FLINT_WORKBENCH.get(),
                ModItems.COPPER_WORKBENCH.get());
        registration.addCraftingStation(
                TimedCraftingJeiTypes.COPPER,
                ModItems.COPPER_WORKBENCH.get());
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        addTransferHandler(registration, ModMenus.FLINT_WORKBENCH.get(), TimedCraftingJeiTypes.HAND);
        addTransferHandler(registration, ModMenus.FLINT_WORKBENCH.get(), TimedCraftingJeiTypes.FLINT);
        addTransferHandler(registration, ModMenus.COPPER_WORKBENCH.get(), TimedCraftingJeiTypes.HAND);
        addTransferHandler(registration, ModMenus.COPPER_WORKBENCH.get(), TimedCraftingJeiTypes.FLINT);
        addTransferHandler(registration, ModMenus.COPPER_WORKBENCH.get(), TimedCraftingJeiTypes.COPPER);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(TimedWorkbenchScreen.class, new IGuiContainerHandler<>() {
            @Override
            public List<IGuiClickableArea> getGuiClickableAreas(
                    TimedWorkbenchScreen screen, double mouseX, double mouseY) {
                if (screen.getMenu().infx$benchTier() == BenchTier.FLINT) {
                    return List.of(IGuiClickableArea.createBasic(
                            90, 35, 24, 16, TimedCraftingJeiTypes.HAND, TimedCraftingJeiTypes.FLINT));
                }
                return List.of(IGuiClickableArea.createBasic(
                        90,
                        35,
                        24,
                        16,
                        TimedCraftingJeiTypes.HAND,
                        TimedCraftingJeiTypes.FLINT,
                        TimedCraftingJeiTypes.COPPER));
            }
        });
    }

    private static void addTransferHandler(
            IRecipeTransferRegistration registration,
            net.minecraft.world.inventory.MenuType<TimedWorkbenchMenu> menuType,
            IRecipeHolderType<TimedCraftingRecipe> recipeType) {
        registration.addRecipeTransferHandler(
                TimedWorkbenchMenu.class,
                menuType,
                recipeType,
                1,
                9,
                10,
                36);
    }
}
