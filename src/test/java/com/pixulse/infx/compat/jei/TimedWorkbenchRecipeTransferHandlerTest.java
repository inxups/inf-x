package com.pixulse.infx.compat.jei;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.pixulse.infx.recipe.BenchTier;
import com.pixulse.infx.screen.menu.TimedWorkbenchMenu;

import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

class TimedWorkbenchRecipeTransferHandlerTest {
    @Test
    void reportsMenuTypeIndependentMetadata() {
        TimedWorkbenchRecipeTransferHandler handler = handlerFor(BenchTier.COPPER);

        assertEquals(TimedWorkbenchMenu.class, handler.getContainerClass());
        assertEquals(Optional.empty(), handler.getMenuType());
        assertEquals(TimedCraftingJeiTypes.COPPER, handler.getRecipeType());
    }

    @Test
    void hidesTransferWhenOpenBenchIsTooWeak() {
        RecordingHelper helper = new RecordingHelper();

        IRecipeTransferError error =
                TimedWorkbenchRecipeTransferHandler.tierGate(helper, BenchTier.COPPER, BenchTier.ADAMANTIUM);

        assertNotNull(error);
        assertEquals(
                IRecipeTransferError.Type.INTERNAL,
                error.getType(),
                "non-user-facing errors hide the JEI transfer button");
        assertNull(TimedWorkbenchRecipeTransferHandler.tierGate(helper, BenchTier.COPPER, BenchTier.COPPER));
        assertNull(TimedWorkbenchRecipeTransferHandler.tierGate(helper, BenchTier.IRON, BenchTier.COPPER));
        assertNull(TimedWorkbenchRecipeTransferHandler.tierGate(helper, BenchTier.ADAMANTIUM, BenchTier.MITHRIL));
    }

    @Test
    void registersOneMenuTypeIndependentHandlerPerRecipeTier() {
        RecordingRegistration registration = new RecordingRegistration();

        InfXJeiPlugin.registerWorkbenchTransferHandlers(registration);

        Set<IRecipeType<?>> expectedRecipeTypes = Arrays.stream(BenchTier.values())
                .filter(BenchTier::isRecipeTier)
                .map(TimedCraftingJeiTypes::forBench)
                .collect(Collectors.toSet());
        assertEquals(expectedRecipeTypes.size(), registration.registered.size());
        assertEquals(
                expectedRecipeTypes,
                registration.registered.stream()
                        .map(Registered::recipeType)
                        .collect(Collectors.toSet()));
        assertTrue(
                registration.registered.stream()
                        .allMatch(entry -> entry.containerClass == TimedWorkbenchMenu.class),
                "every workbench tier must share the single TimedWorkbenchMenu container class");
        assertTrue(
                registration.registered.stream().allMatch(entry -> entry.menuType.isEmpty()),
                "the handler must match any workbench menu type, not a single tier");
    }

    private static TimedWorkbenchRecipeTransferHandler handlerFor(BenchTier requiredTier) {
        return new TimedWorkbenchRecipeTransferHandler(
                new RecordingHelper(), requiredTier, new RecordingDelegate());
    }

    private record Registered(
            Class<?> containerClass, Optional<? extends MenuType<?>> menuType, IRecipeType<?> recipeType) {}

    private static final class RecordingRegistration implements IRecipeTransferRegistration {
        private final List<Registered> registered = new ArrayList<>();
        private final RecordingHelper helper = new RecordingHelper();

        @Override
        public @NonNull IJeiHelpers getJeiHelpers() {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NonNull IRecipeTransferHandlerHelper getTransferHelper() {
            return helper;
        }

        @Override
        public <C extends AbstractContainerMenu, R> void addRecipeTransferHandler(
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
        public <C extends AbstractContainerMenu, R> void addRecipeTransferHandler(
                @NonNull IRecipeTransferInfo<C, R> transferInfo) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <C extends AbstractContainerMenu, R> void addRecipeTransferHandler(
                @NonNull IRecipeTransferHandler<C, R> handler, @NonNull IRecipeType<R> recipeType) {
            registered.add(new Registered(
                    handler.getContainerClass(), handler.getMenuType(), recipeType));
        }

        @Override
        public <C extends AbstractContainerMenu> void addUniversalRecipeTransferHandler(
                @NonNull IUniversalRecipeTransferHandler<C> handler) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class RecordingDelegate
            implements IRecipeTransferHandler<TimedWorkbenchMenu, RecipeHolder<CraftingRecipe>> {
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
            return TimedCraftingJeiTypes.COPPER;
        }

        @Override
        public IRecipeTransferError transferRecipe(
                TimedWorkbenchMenu container,
                RecipeHolder<CraftingRecipe> recipe,
                IRecipeSlotsView recipeSlots,
                Player player,
                boolean maxTransfer,
                boolean doTransfer) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class RecordingHelper implements IRecipeTransferHandlerHelper {
        private boolean serverSupport = true;

        @Override
        public @NonNull IRecipeTransferError createInternalError() {
            return () -> IRecipeTransferError.Type.INTERNAL;
        }

        @Override
        public @NonNull IRecipeTransferError createUserErrorWithTooltip(@NonNull Component tooltip) {
            return () -> IRecipeTransferError.Type.USER_FACING;
        }

        @Override
        public @NonNull IRecipeTransferError createUserErrorForMissingSlots(
                @NonNull Component tooltip,
                java.util.@NonNull Collection<IRecipeSlotView> missingSlots) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <C extends AbstractContainerMenu, R> @NonNull IRecipeTransferInfo<C, R>
                createBasicRecipeTransferInfo(
                        @NonNull Class<? extends C> containerClass,
                        MenuType<C> menuType,
                        @NonNull IRecipeType<R> recipeType,
                        int recipeSlotStart,
                        int recipeSlotCount,
                        int inventorySlotStart,
                        int inventorySlotCount) {
            return null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <C extends AbstractContainerMenu, R> @NonNull IRecipeTransferHandler<C, R>
                createUnregisteredRecipeTransferHandler(@NonNull IRecipeTransferInfo<C, R> transferInfo) {
            return (IRecipeTransferHandler<C, R>) new RecordingDelegate();
        }

        @Override
        public @NonNull IRecipeSlotsView createRecipeSlotsView(@NonNull List<IRecipeSlotView> slots) {
            throw new UnsupportedOperationException();
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
