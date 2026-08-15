package com.pixulse.infx.compat.jei;

import java.util.Optional;

import com.pixulse.infx.recipe.BenchTier;
import com.pixulse.infx.screen.menu.TimedWorkbenchMenu;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.NonNull;

/**
 * JEI transfer into the 3x3 grid of an INFX workbench. Every workbench tier
 * shares the single {@link TimedWorkbenchMenu} container, and JEI keys
 * transfer handlers by (container class, recipe type) with only one handler
 * per key, so the menu type is left empty to match any workbench and the
 * required bench tier is enforced here instead: benches too weak for the
 * recipe hide the transfer button rather than dumping items into a grid
 * that cannot craft.
 */
final class TimedWorkbenchRecipeTransferHandler
        implements IRecipeTransferHandler<TimedWorkbenchMenu, RecipeHolder<CraftingRecipe>> {
    private final IRecipeTransferHandlerHelper handlerHelper;
    private final BenchTier requiredTier;
    private final IRecipeTransferHandler<TimedWorkbenchMenu, RecipeHolder<CraftingRecipe>> delegate;

    TimedWorkbenchRecipeTransferHandler(
            IRecipeTransferHandlerHelper handlerHelper,
            BenchTier requiredTier,
            IRecipeTransferHandler<TimedWorkbenchMenu, RecipeHolder<CraftingRecipe>> delegate) {
        this.handlerHelper = handlerHelper;
        this.requiredTier = requiredTier;
        this.delegate = delegate;
    }

    @Override
    public @NonNull Class<? extends TimedWorkbenchMenu> getContainerClass() {
        return TimedWorkbenchMenu.class;
    }

    @Override
    public @NonNull Optional<MenuType<TimedWorkbenchMenu>> getMenuType() {
        return Optional.empty();
    }

    @Override
    public @NonNull IRecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
        return TimedCraftingJeiTypes.forBench(requiredTier);
    }

    @Override
    public IRecipeTransferError transferRecipe(
            TimedWorkbenchMenu container,
            RecipeHolder<CraftingRecipe> recipe,
            IRecipeSlotsView recipeSlots,
            Player player,
            boolean maxTransfer,
            boolean doTransfer) {
        if (!handlerHelper.recipeTransferHasServerSupport()) {
            return handlerHelper.createUserErrorWithTooltip(
                    Component.translatable("jei.tooltip.error.recipe.transfer.no.server"));
        }
        IRecipeTransferError tierGate = tierGate(handlerHelper, container.infx$benchTier(), requiredTier);
        if (tierGate != null) {
            return tierGate;
        }
        return delegate.transferRecipe(container, recipe, recipeSlots, player, maxTransfer, doTransfer);
    }

    /**
     * Returns an INTERNAL error (which hides the transfer button) when the
     * open workbench is too weak for the recipe tier, otherwise null.
     */
    static IRecipeTransferError tierGate(
            IRecipeTransferHandlerHelper handlerHelper, BenchTier openTier, BenchTier requiredTier) {
        return openTier.supports(requiredTier) ? null : handlerHelper.createInternalError();
    }
}
