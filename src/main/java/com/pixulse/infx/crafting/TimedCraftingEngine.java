package com.pixulse.infx.crafting;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.EventHooks;
import com.pixulse.infx.equipment.R196QualitySystem;

public final class TimedCraftingEngine {
    private TimedCraftingEngine() {}

    public static boolean refreshResult(TimedCraftingMenu timedMenu, ServerPlayer player, boolean clearWhenMissing) {
        AbstractContainerMenu menu = asContainerMenu(timedMenu);
        Optional<CraftingMatch> match = findRecipe(timedMenu, player.level());
        if (match.isEmpty()) {
            timedMenu.infx$setHasTimedResult(false);
            timedMenu.infx$resetTimedCrafting();
            if (clearWhenMissing) {
                setPreview(menu, player, timedMenu.infx$resultContainer(), null, ItemStack.EMPTY);
            }
            return false;
        }

        CraftingMatch holder = match.orElseThrow();
        String recipeId = recipeId(holder.holder().id());
        TimedCraftingState state = timedMenu.infx$craftingState();
        if (state.isRunning() && !state.activeRecipeId().equals(recipeId)) {
            state.reset();
        }

        ResultContainer result = timedMenu.infx$resultContainer();
        ItemStack preview = ItemStack.EMPTY;
        if (result.setRecipeUsed(player, holder.holder())) {
            ItemStack assembled = holder.assemble(timedMenu.infx$craftingContainer().asCraftInput());
            if (assembled.isItemEnabled(player.level().enabledFeatures())) {
                int code = R196QualitySystem.clampCode(
                        assembled,
                        player,
                        holder.profile().difficulty(),
                        timedMenu.infx$selectedQualityCode(),
                        CraftingEnvironment.hasClumsiness(player));
                timedMenu.infx$setSelectedQualityCode(code);
                R196QualitySystem.applySelectedQuality(assembled, code);
                preview = assembled;
            }
        }

        timedMenu.infx$setHasTimedResult(!preview.isEmpty());
        timedMenu.infx$syncCraftingData();
        setPreview(menu, player, result, holder.holder(), preview);
        return !preview.isEmpty();
    }

    public static void start(TimedCraftingMenu timedMenu, ServerPlayer player) {
        if (!timedMenu.infx$hasTimedResult() || !timedMenu.infx$isCraftingContextValid(player)) {
            return;
        }
        Optional<CraftingMatch> match = findRecipe(timedMenu, player.level());
        if (match.isEmpty()) {
            timedMenu.infx$resetTimedCrafting();
            return;
        }
        CraftingMatch holder = match.orElseThrow();
        float adjustedDifficulty = R196QualitySystem.adjustedDifficulty(
                holder.profile().difficulty(), timedMenu.infx$selectedQualityCode());
        boolean clumsy = CraftingEnvironment.hasClumsiness(player);
        int requiredTicks = CraftingTimeCalculator.requiredTicks(
                adjustedDifficulty,
                player.experienceLevel,
                timedMenu.infx$benchTier(),
                holder.profile().materialGated(),
                clumsy);
        timedMenu.infx$craftingState().start(recipeId(holder.holder().id()), requiredTicks);
        timedMenu.infx$syncCraftingData();
    }

    public static void cycleQuality(TimedCraftingMenu timedMenu, ServerPlayer player) {
        Optional<CraftingMatch> match = findRecipe(timedMenu, player.level());
        if (match.isEmpty()) {
            timedMenu.infx$setSelectedQualityCode(R196QualitySystem.AVERAGE_CODE);
            return;
        }
        CraftingMatch holder = match.orElseThrow();
        ItemStack output = holder.assemble(timedMenu.infx$craftingContainer().asCraftInput());
        int code = R196QualitySystem.cycleCode(
                output,
                player,
                holder.profile().difficulty(),
                timedMenu.infx$selectedQualityCode(),
                CraftingEnvironment.hasClumsiness(player));
        timedMenu.infx$setSelectedQualityCode(code);
        timedMenu.infx$resetTimedCrafting();
        refreshResult(timedMenu, player, true);
    }

    public static void tick(TimedCraftingMenu timedMenu, ServerPlayer player) {
        long gameTime = player.level().getGameTime();
        if (timedMenu.infx$lastCraftingTick() == gameTime) {
            return;
        }
        timedMenu.infx$setLastCraftingTick(gameTime);

        TimedCraftingState state = timedMenu.infx$craftingState();
        if (!state.isRunning()) {
            timedMenu.infx$syncCraftingData();
            return;
        }

        Optional<CraftingMatch> match = findRecipe(timedMenu, player.level());
        String currentRecipeId = match.map(holder -> recipeId(holder.holder().id())).orElse("");
        boolean sameRecipe = match.isPresent() && currentRecipeId.equals(state.activeRecipeId());
        TimedCraftingState.TickResult result = state.tick(
                currentRecipeId,
                player.getFoodData().getFoodLevel() > 0,
                timedMenu.infx$isCraftingContextValid(player),
                sameRecipe);

        if (result == TimedCraftingState.TickResult.COMPLETED) {
            complete(timedMenu, player, match.orElseThrow());
        }
        timedMenu.infx$syncCraftingData();
    }

    private static Optional<CraftingMatch> findRecipe(
            TimedCraftingMenu timedMenu, ServerLevel level) {
        CraftingInput input = timedMenu.infx$craftingContainer().asCraftInput();
        Optional<CraftingMatch> explicit = level.recipeAccess()
                .recipeMap()
                .getRecipesFor(com.pixulse.infx.registry.ModRecipes.CRAFTING.get(), input, level)
                .filter(holder -> timedMenu.infx$benchTier().supports(holder.value().requiredBench()))
                .findFirst()
                .map(holder -> CraftingMatch.explicit(holder, input));
        if (explicit.isPresent()) {
            return explicit;
        }

        return level.recipeAccess()
                .recipeMap()
                .getRecipesFor(RecipeType.CRAFTING, input, level)
                .map(holder -> CraftingMatch.vanilla(holder, input))
                .filter(match -> timedMenu.infx$benchTier().supports(match.profile().requiredBench()))
                .findFirst();
    }

    private static void complete(
            TimedCraftingMenu timedMenu,
            ServerPlayer player,
            CraftingMatch holder) {
        CraftingContainer craftSlots = timedMenu.infx$craftingContainer();
        CraftingInput.Positioned positioned = craftSlots.asPositionedCraftInput();
        CraftingInput input = positioned.input();
        if (!holder.matches(input, player.level())) {
            timedMenu.infx$resetTimedCrafting();
            return;
        }

        ItemStack output = holder.assemble(input);
        if (output.isEmpty()) {
            timedMenu.infx$resetTimedCrafting();
            return;
        }
        boolean clumsy = CraftingEnvironment.hasClumsiness(player);
        int qualityCode = R196QualitySystem.clampCode(
                output,
                player,
                holder.profile().difficulty(),
                timedMenu.infx$selectedQualityCode(),
                clumsy);
        R196QualitySystem.applySelectedQuality(output, qualityCode);
        var quality = R196QualitySystem.fromCode(qualityCode);
        int qualityCost = R196QualitySystem.experienceCost(holder.profile().difficulty(), quality, clumsy);
        if (qualityCost > player.totalExperience) {
            timedMenu.infx$resetTimedCrafting();
            refreshResult(timedMenu, player, true);
            return;
        }

        NonNullList<ItemStack> remaining;
        CommonHooks.setCraftingPlayer(player);
        try {
            remaining = holder.getRemainingItems(input);
        } finally {
            CommonHooks.setCraftingPlayer(null);
        }

        List<ItemStack> inputsForCriterion = craftSlots.getItems();
        output.onCraftedBy(player, output.getCount());
        EventHooks.firePlayerCraftingEvent(player, output, craftSlots);
        timedMenu.infx$resultContainer().setRecipeUsed(holder.holder());
        timedMenu.infx$resultContainer().awardUsedRecipes(player, inputsForCriterion);
        if (qualityCost > 0) {
            player.giveExperiencePoints(-qualityCost);
        }

        consumeInputsAndReturnContainers(player, craftSlots, positioned, remaining);
        CraftingOutputDistributor.giveOrDrop(
                output,
                player.getInventory()::add,
                stack -> player.drop(stack, false));

        boolean stillSameRecipe = refreshResult(timedMenu, player, true)
                && findRecipe(timedMenu, player.level())
                        .map(next -> next.holder().id().equals(holder.holder().id()))
                        .orElse(false);
        if (stillSameRecipe) {
            float adjustedDifficulty = R196QualitySystem.adjustedDifficulty(
                    holder.profile().difficulty(), timedMenu.infx$selectedQualityCode());
            int requiredTicks = CraftingTimeCalculator.requiredTicks(
                    adjustedDifficulty,
                    player.experienceLevel,
                    timedMenu.infx$benchTier(),
                    holder.profile().materialGated(),
                    clumsy);
            timedMenu.infx$craftingState().start(recipeId(holder.holder().id()), requiredTicks);
        } else {
            timedMenu.infx$resetTimedCrafting();
        }
    }

    private static void consumeInputsAndReturnContainers(
            Player player,
            CraftingContainer craftSlots,
            CraftingInput.Positioned positioned,
            NonNullList<ItemStack> remaining) {
        CraftingInput input = positioned.input();
        for (int y = 0; y < input.height(); y++) {
            for (int x = 0; x < input.width(); x++) {
                int gridSlot = x + positioned.left() + (y + positioned.top()) * craftSlots.getWidth();
                ItemStack gridStack = craftSlots.getItem(gridSlot);
                ItemStack remainder = remaining.get(x + y * input.width());
                if (!gridStack.isEmpty()) {
                    craftSlots.removeItem(gridSlot, 1);
                    gridStack = craftSlots.getItem(gridSlot);
                }
                if (remainder.isEmpty()) {
                    continue;
                }
                if (gridStack.isEmpty()) {
                    craftSlots.setItem(gridSlot, remainder);
                } else if (ItemStack.isSameItemSameComponents(gridStack, remainder)) {
                    remainder.grow(gridStack.getCount());
                    craftSlots.setItem(gridSlot, remainder);
                } else if (!player.getInventory().add(remainder)) {
                    player.drop(remainder, false);
                }
            }
        }
    }

    private static void setPreview(
            AbstractContainerMenu menu,
            ServerPlayer player,
            ResultContainer resultContainer,
            RecipeHolder<?> holder,
            ItemStack preview) {
        resultContainer.setRecipeUsed(holder);
        resultContainer.setItem(0, preview);
        menu.setRemoteSlot(0, preview);
        player.connection.send(new ClientboundContainerSetSlotPacket(
                menu.containerId, menu.incrementStateId(), 0, preview));
    }

    private static AbstractContainerMenu asContainerMenu(TimedCraftingMenu menu) {
        return (AbstractContainerMenu) menu;
    }

    private static String recipeId(ResourceKey<Recipe<?>> id) {
        return id.identifier().toString();
    }

    private record CraftingMatch(RecipeHolder<?> holder, CraftingProfile profile) {
        static CraftingMatch explicit(
                RecipeHolder<TimedCraftingRecipe> holder,
                CraftingInput input) {
            TimedCraftingRecipe recipe = holder.value();
            CraftingProfile profile = new CraftingProfile(
                    recipe.requiredBench(), recipe.difficulty(input), recipe.materialGated());
            return new CraftingMatch(holder, profile);
        }

        static CraftingMatch vanilla(
                RecipeHolder<CraftingRecipe> holder,
                CraftingInput input) {
            return new CraftingMatch(holder, MiteCraftingRules.profile(holder.value(), input));
        }

        boolean matches(CraftingInput input, ServerLevel level) {
            Recipe<?> recipe = holder.value();
            if (recipe instanceof TimedCraftingRecipe timed) {
                return timed.matches(input, level);
            }
            return ((CraftingRecipe) recipe).matches(input, level);
        }

        ItemStack assemble(CraftingInput input) {
            Recipe<?> recipe = holder.value();
            if (recipe instanceof TimedCraftingRecipe timed) {
                return timed.assemble(input);
            }
            return ((CraftingRecipe) recipe).assemble(input);
        }

        NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
            Recipe<?> recipe = holder.value();
            if (recipe instanceof TimedCraftingRecipe timed) {
                return timed.getRemainingItems(input);
            }
            return ((CraftingRecipe) recipe).getRemainingItems(input);
        }
    }
}
