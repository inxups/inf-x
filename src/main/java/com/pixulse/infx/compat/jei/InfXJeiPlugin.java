package com.pixulse.infx.compat.jei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.block.TieredWorkbenchBlock;
import com.pixulse.infx.event.client.ClientEvents;
import com.pixulse.infx.screen.gui.TimedWorkbenchScreen;
import com.pixulse.infx.recipe.BenchTier;
import com.pixulse.infx.recipe.RecipeRules;
import com.pixulse.infx.screen.menu.TimedWorkbenchMenu;
import com.pixulse.infx.registry.InfXItems;
import com.pixulse.infx.registry.InfXMenus;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.NonNull;

@JeiPlugin
public final class InfXJeiPlugin implements IModPlugin {
    private static final Identifier PLUGIN_ID = InfiniteX.id("jei_plugin");

    @Override
    public @NonNull Identifier getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerItemSubtypes(@NonNull ISubtypeRegistration registration) {
        registerRuneStoneSubtypes(
                registration,
                InfXItems.MITHRIL_RUNE_STONE.get(),
                InfXItems.ADAMANTIUM_RUNE_STONE.get());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new TimedCraftingRecipeCategory(guiHelper, BenchTier.HAND, Items.CRAFTING_TABLE),
                new TimedCraftingRecipeCategory(
                        guiHelper, BenchTier.FLINT, InfXItems.STRIPPED_LOG_WORKBENCHES.getFirst().get()),
                new TimedCraftingRecipeCategory(
                        guiHelper, BenchTier.OBSIDIAN, InfXItems.STRIPPED_LOG_WORKBENCHES.get(1).get()),
                new TimedCraftingRecipeCategory(guiHelper, BenchTier.COPPER, InfXItems.COPPER_WORKBENCH.get()),
                new TimedCraftingRecipeCategory(guiHelper, BenchTier.IRON, InfXItems.IRON_WORKBENCH.get()),
                new TimedCraftingRecipeCategory(
                        guiHelper, BenchTier.ANCIENT_METAL, InfXItems.ANCIENT_METAL_WORKBENCH.get()),
                new TimedCraftingRecipeCategory(guiHelper, BenchTier.MITHRIL, InfXItems.MITHRIL_WORKBENCH.get()),
                new TimedCraftingRecipeCategory(
                        guiHelper, BenchTier.ADAMANTIUM, InfXItems.ADAMANTIUM_WORKBENCH.get()));
    }

    @Override
    public void registerRecipes(@NonNull IRecipeRegistration registration) {
        Map<BenchTier, List<RecipeHolder<CraftingRecipe>>> recipesByBench =
                new EnumMap<>(BenchTier.class);
        for (BenchTier benchTier : BenchTier.values()) {
            if (benchTier.isRecipeTier()) {
                recipesByBench.put(benchTier, new ArrayList<>());
            }
        }

        for (RecipeHolder<CraftingRecipe> holder : ClientEvents.timedCraftingRecipes()) {
            recipesByBench
                    .get(RecipeRules.displayProfile(holder).requiredBench().recipeTier())
                    .add(holder);
        }

        int recipeCount = recipesByBench.values().stream().mapToInt(List::size).sum();
        InfiniteX.LOGGER.debug("Registering {} timed crafting recipes with JEI", recipeCount);
        for (BenchTier benchTier : BenchTier.values()) {
            if (benchTier.isRecipeTier()) {
                registration.addRecipes(
                        TimedCraftingJeiTypes.forBench(benchTier), recipesByBench.get(benchTier));
            }
        }
    }

    @Override
    public void registerRecipeCatalysts(@NonNull IRecipeCatalystRegistration registration) {
        for (BenchTier requiredTier : BenchTier.values()) {
            if (!requiredTier.isRecipeTier()) {
                continue;
            }
            ItemLike[] workbenches = Arrays.stream(BenchTier.values())
                    .filter(BenchTier::isWorkbench)
                    .filter(benchTier -> benchTier.supports(requiredTier))
                    .flatMap(benchTier -> InfXItems.WORKBENCHES.stream()
                            .filter(item -> item.get().getBlock() instanceof TieredWorkbenchBlock workbench
                                    && workbench.tier() == benchTier)
                            .map(item -> (ItemLike) item.get()))
                    .toArray(ItemLike[]::new);
            registration.addCraftingStation(TimedCraftingJeiTypes.forBench(requiredTier), workbenches);
        }
    }

    @Override
    public void registerRecipeTransferHandlers(@NonNull IRecipeTransferRegistration registration) {
        for (BenchTier benchTier : BenchTier.values()) {
            if (!benchTier.isWorkbench()) {
                continue;
            }
            for (BenchTier requiredTier : BenchTier.values()) {
                if (requiredTier.isRecipeTier() && benchTier.supports(requiredTier)) {
                    addTransferHandler(
                            registration,
                            InfXMenus.workbench(benchTier).get(),
                            TimedCraftingJeiTypes.forBench(requiredTier));
                }
            }
        }
        addHandCraftingTransferHandler(registration);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(TimedWorkbenchScreen.class, new IGuiContainerHandler<>() {
            @Override
            public @NonNull List<IGuiClickableArea> getGuiClickableAreas(
                    TimedWorkbenchScreen screen, double mouseX, double mouseY) {
                BenchTier benchTier = screen.getMenu().infx$benchTier();
                IRecipeType<?>[] recipeTypes = Arrays.stream(BenchTier.values())
                        .filter(BenchTier::isRecipeTier)
                        .filter(benchTier::supports)
                        .map(TimedCraftingJeiTypes::forBench)
                        .toArray(IRecipeType<?>[]::new);
                return List.of(IGuiClickableArea.createBasic(90, 35, 24, 16, recipeTypes));
            }
        });
    }

    private static void addTransferHandler(
            IRecipeTransferRegistration registration,
            net.minecraft.world.inventory.MenuType<TimedWorkbenchMenu> menuType,
            IRecipeHolderType<CraftingRecipe> recipeType) {
        registration.addRecipeTransferHandler(
                TimedWorkbenchMenu.class,
                menuType,
                recipeType,
                1,
                9,
                10,
                36);
    }

    /**
     * Hand-tier recipes are crafted in the player's own 2x2 inventory grid.
     * JEI's vanilla player handler only covers its CRAFTING type, so INFX
     * registers its own equivalent for the {@code infx:hand_crafting} type;
     * otherwise the transfer button silently fails for the inventory screen.
     */
    private static void addHandCraftingTransferHandler(IRecipeTransferRegistration registration) {
        IRecipeTransferHandlerHelper helper = registration.getTransferHelper();
        IRecipeTransferHandler<InventoryMenu, RecipeHolder<CraftingRecipe>> delegate =
                helper.createUnregisteredRecipeTransferHandler(helper.createBasicRecipeTransferInfo(
                        InventoryMenu.class,
                        null,
                        TimedCraftingJeiTypes.HAND,
                        1,
                        4,
                        9,
                        36));
        registration.addRecipeTransferHandler(
                new PlayerGridRecipeTransferHandler(helper, delegate), TimedCraftingJeiTypes.HAND);
    }

    static void registerRuneStoneSubtypes(ISubtypeRegistration registration, Item... runeStones) {
        for (Item runeStone : runeStones) {
            registration.registerFromDataComponentTypes(runeStone, DataComponents.BLOCK_STATE);
        }
    }
}
