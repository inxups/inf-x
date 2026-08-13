package com.pixulse.infx.compat.jei;

import java.util.List;
import java.util.Optional;

import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.NonNull;

/**
 * JEI transfer into the 2x2 hand-crafting grid of the player inventory for
 * the INFX hand tier. JEI's vanilla handler is hardcoded to its own CRAFTING
 * recipe type, so the INFX {@code infx:hand_crafting} category has no
 * handler and its transfer button silently fails while the inventory is
 * open. This mirrors JEI's player-inventory behavior: only the four
 * top-left cells of the 3x3 recipe layout may carry ingredients, everything
 * else is delegated to the standard slot-based transfer.
 */
final class PlayerGridRecipeTransferHandler
        implements IRecipeTransferHandler<InventoryMenu, RecipeHolder<CraftingRecipe>> {
    private static final IntSet GRID_SLOT_INDEXES = IntSet.of(0, 1, 3, 4);

    private final IRecipeTransferHandlerHelper handlerHelper;
    private final IRecipeTransferHandler<InventoryMenu, RecipeHolder<CraftingRecipe>> delegate;

    PlayerGridRecipeTransferHandler(
            IRecipeTransferHandlerHelper handlerHelper,
            IRecipeTransferHandler<InventoryMenu, RecipeHolder<CraftingRecipe>> delegate) {
        this.handlerHelper = handlerHelper;
        this.delegate = delegate;
    }

    @Override
    public @NonNull Class<? extends InventoryMenu> getContainerClass() {
        return InventoryMenu.class;
    }

    @Override
    public @NonNull Optional<MenuType<InventoryMenu>> getMenuType() {
        return Optional.empty();
    }

    @Override
    public @NonNull IRecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
        return TimedCraftingJeiTypes.HAND;
    }

    @Override
    public IRecipeTransferError transferRecipe(
            InventoryMenu container,
            RecipeHolder<CraftingRecipe> recipe,
            IRecipeSlotsView recipeSlots,
            Player player,
            boolean maxTransfer,
            boolean doTransfer) {
        if (!handlerHelper.recipeTransferHasServerSupport()) {
            return handlerHelper.createUserErrorWithTooltip(
                    Component.translatable("jei.tooltip.error.recipe.transfer.no.server"));
        }
        List<IRecipeSlotView> inputSlots = recipeSlots.getSlotViews(RecipeIngredientRole.INPUT);
        if (!validateIngredientsFitPlayerGrid(inputSlots)) {
            return handlerHelper.createUserErrorWithTooltip(Component.translatable(
                    "jei.tooltip.error.recipe.transfer.too.large.player.inventory"));
        }
        List<IRecipeSlotView> gridSlots = GRID_SLOT_INDEXES.intStream().mapToObj(inputSlots::get).toList();
        IRecipeSlotsView gridSlotsView = handlerHelper.createRecipeSlotsView(gridSlots);
        return delegate.transferRecipe(container, recipe, gridSlotsView, player, maxTransfer, doTransfer);
    }

    private static boolean validateIngredientsFitPlayerGrid(List<IRecipeSlotView> inputSlots) {
        for (int index = 0; index < inputSlots.size(); index++) {
            if (!GRID_SLOT_INDEXES.contains(index) && !inputSlots.get(index).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
