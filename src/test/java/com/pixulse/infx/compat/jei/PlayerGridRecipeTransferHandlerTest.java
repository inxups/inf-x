package com.pixulse.infx.compat.jei;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

class PlayerGridRecipeTransferHandlerTest {
    @Test
    void delegatesOnlyTheFourPlayerGridSlots() {
        RecordingHelper helper = new RecordingHelper();
        RecordingDelegate delegate = new RecordingDelegate();
        PlayerGridRecipeTransferHandler handler = new PlayerGridRecipeTransferHandler(helper, delegate);

        RecipeHolder<CraftingRecipe> recipe = new RecipeHolder<>(null, null);
        List<IRecipeSlotView> inputs = slotViewsWithItemsAt(0, 1, 3, 4);
        handler.transferRecipe(null, recipe, new FakeSlotsView(inputs), null, false, false);

        assertNotNull(delegate.view);
        assertEquals(4, delegate.view.getSlotViews().size());
        assertSame(inputs.get(0), delegate.view.getSlotViews().get(0));
        assertSame(inputs.get(1), delegate.view.getSlotViews().get(1));
        assertSame(inputs.get(3), delegate.view.getSlotViews().get(2));
        assertSame(inputs.get(4), delegate.view.getSlotViews().get(3));
        assertEquals(TimedCraftingJeiTypes.HAND, handler.getRecipeType());
        assertEquals(InventoryMenu.class, handler.getContainerClass());
        assertEquals(Optional.empty(), handler.getMenuType());
    }

    @Test
    void rejectsIngredientsOutsideThePlayerGrid() {
        RecordingHelper helper = new RecordingHelper();
        RecordingDelegate delegate = new RecordingDelegate();
        PlayerGridRecipeTransferHandler handler = new PlayerGridRecipeTransferHandler(helper, delegate);

        List<IRecipeSlotView> inputs = slotViewsWithItemsAt(2);
        IRecipeTransferError error =
                handler.transferRecipe(null, null, new FakeSlotsView(inputs), null, false, false);

        assertNotNull(error);
        assertEquals("jei.tooltip.error.recipe.transfer.too.large.player.inventory", helper.lastTooltipKey);
        assertTrue(delegate.transfers.isEmpty(), "oversized recipes must not reach the slot-based delegate");
    }

    @Test
    void noServerSupportReportsTheVanillaTooltip() {
        RecordingHelper helper = new RecordingHelper(false);
        RecordingDelegate delegate = new RecordingDelegate();
        PlayerGridRecipeTransferHandler handler = new PlayerGridRecipeTransferHandler(helper, delegate);

        IRecipeTransferError error =
                handler.transferRecipe(null, null, new FakeSlotsView(List.of()), null, false, false);

        assertNotNull(error);
        assertEquals("jei.tooltip.error.recipe.transfer.no.server", helper.lastTooltipKey);
        assertTrue(delegate.transfers.isEmpty());
    }

    private static List<IRecipeSlotView> slotViewsWithItemsAt(int... indexes) {
        List<IRecipeSlotView> slots = new ArrayList<>();
        for (int index = 0; index < 9; index++) {
            boolean hasItem = false;
            for (int occupied : indexes) {
                hasItem |= occupied == index;
            }
            slots.add(hasItem ? new FakeSlotView(true) : new FakeSlotView(false));
        }
        return slots;
    }

    private static final class FakeSlotsView implements IRecipeSlotsView {
        private final List<IRecipeSlotView> inputs;

        private FakeSlotsView(List<IRecipeSlotView> inputs) {
            this.inputs = inputs;
        }

        @Override
        public @NonNull List<IRecipeSlotView> getSlotViews() {
            return inputs;
        }

        @Override
        public @NonNull List<IRecipeSlotView> getSlotViews(@NonNull RecipeIngredientRole role) {
            return role == RecipeIngredientRole.INPUT ? inputs : List.of();
        }
    }

    private static final class FakeSlotView implements IRecipeSlotView {
        private final boolean hasItem;

        private FakeSlotView(boolean hasItem) {
            this.hasItem = hasItem;
        }

        @Override
        public @NonNull Stream<ITypedIngredient<?>> getAllIngredients() {
            return Stream.empty();
        }

        @Override
        public @NonNull List<ITypedIngredient<?>> getAllIngredientsList() {
            return List.of();
        }

        @Override
        public @NonNull Optional<ITypedIngredient<?>> getDisplayedIngredient() {
            return Optional.empty();
        }

        @Override
        public @NonNull RecipeIngredientRole getRole() {
            return RecipeIngredientRole.INPUT;
        }

        @Override
        public void drawHighlight(@NonNull GuiGraphicsExtractor graphics, int color) {}

        @Override
        public @NonNull Optional<String> getSlotName() {
            return Optional.empty();
        }

        @Override
        public boolean isEmpty() {
            return !hasItem;
        }
    }

    private static final class RecordingDelegate
            implements IRecipeTransferHandler<InventoryMenu, RecipeHolder<CraftingRecipe>> {
        private final List<IRecipeSlotsView> transfers = new ArrayList<>();
        private IRecipeSlotsView view;

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
            view = recipeSlots;
            transfers.add(recipeSlots);
            return null;
        }
    }

    private static final class RecordingHelper implements IRecipeTransferHandlerHelper {
        private final boolean serverSupport;
        private String lastTooltipKey;
        private IRecipeSlotsView lastCreatedView;

        private RecordingHelper() {
            this(true);
        }

        private RecordingHelper(boolean serverSupport) {
            this.serverSupport = serverSupport;
        }

        @Override
        public @NonNull IRecipeTransferError createInternalError() {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NonNull IRecipeTransferError createUserErrorWithTooltip(@NonNull Component tooltip) {
            lastTooltipKey = tooltip.getString();
            return new IRecipeTransferError() {
                @Override
                public @NonNull Type getType() {
                    return Type.USER_FACING;
                }
            };
        }

        @Override
        public @NonNull IRecipeTransferError createUserErrorForMissingSlots(
                @NonNull Component tooltip, java.util.@NonNull Collection<IRecipeSlotView> missingSlots) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <C extends AbstractContainerMenu, R> @NonNull IRecipeTransferInfo<C, R> createBasicRecipeTransferInfo(
                @NonNull Class<? extends C> containerClass,
                MenuType<C> menuType,
                @NonNull IRecipeType<R> recipeType,
                int recipeSlotStart,
                int recipeSlotCount,
                int inventorySlotStart,
                int inventorySlotCount) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <C extends AbstractContainerMenu, R> @NonNull IRecipeTransferHandler<C, R>
                createUnregisteredRecipeTransferHandler(@NonNull IRecipeTransferInfo<C, R> transferInfo) {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NonNull IRecipeSlotsView createRecipeSlotsView(@NonNull List<IRecipeSlotView> slots) {
            lastCreatedView = new FakeSlotsView(slots);
            return lastCreatedView;
        }

        @Override
        public boolean recipeTransferHasServerSupport() {
            return serverSupport;
        }

        @Override
        public java.util.@NonNull Map<Integer, net.minecraft.world.item.crafting.display.SlotDisplay>
                getGuiSlotIndexToIngredientMap(@NonNull RecipeHolder<CraftingRecipe> recipe) {
            throw new UnsupportedOperationException();
        }
    }
}
