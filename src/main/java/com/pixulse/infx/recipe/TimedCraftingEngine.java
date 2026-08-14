package com.pixulse.infx.recipe;

import java.util.List;
import java.util.Optional;

import com.pixulse.infx.block.RuneStoneBlock;
import com.pixulse.infx.config.InfXConfig;
import com.pixulse.infx.item.CoinItem;
import com.pixulse.infx.item.equipment.QualitySystem;
import com.pixulse.infx.item.material.Quality;
import com.pixulse.infx.registry.InfXAttachments;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.EventHooks;

public final class TimedCraftingEngine {
    private TimedCraftingEngine() {}

    public static boolean refreshResult(TimedCraftingMenu timedMenu, ServerPlayer player, boolean clearWhenMissing) {
        AbstractContainerMenu menu = asContainerMenu(timedMenu);
        timedMenu.infx$setExperienceCost(0);
        timedMenu.infx$setLogicalResultCount(0);
        Optional<CraftingMatch> match = findRecipe(timedMenu, player.level());
        if (match.isEmpty()) {
            timedMenu.infx$setHasTimedResult(false);
            timedMenu.infx$setSelectedRune(0);
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
        int logicalResultCount = 0;
        if (result.setRecipeUsed(player, holder.holder())) {
            CraftingResult assembledResult = holder.assemble(timedMenu.infx$craftingContainer().asCraftInput());
            ItemStack assembled = assembledResult.stack();
            if (!assembled.isEmpty() && assembled.isItemEnabled(player.level().enabledFeatures())) {
                boolean witchClumsiness = CraftingEnvironment.hasWitchClumsiness(player);
                boolean clumsy = witchClumsiness || CraftingEnvironment.hasEnchantedClumsiness(player);
                int code = QualitySystem.clampCode(
                        assembled,
                        player,
                        holder.profile().difficulty(),
                        timedMenu.infx$selectedQualityCode(),
                        clumsy,
                        witchClumsiness);
                timedMenu.infx$setSelectedQualityCode(code);
                QualitySystem.applySelectedQuality(assembled, code);
                applySelectedRune(timedMenu, assembled);
                timedMenu.infx$setExperienceCost(craftingExperienceCost(
                        assembled, assembledResult.totalCount(), holder.profile().difficulty(), code, clumsy));
                preview = assembled;
                logicalResultCount = assembledResult.totalCount();
            }
        }

        timedMenu.infx$setLogicalResultCount(logicalResultCount);
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
        if (!canPayExperience(player, timedMenu.infx$experienceCost())) {
            return;
        }
        float adjustedDifficulty = QualitySystem.adjustedDifficulty(
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

    public static void cycleResult(TimedCraftingMenu timedMenu, ServerPlayer player) {
        Optional<CraftingMatch> match = findRecipe(timedMenu, player.level());
        if (match.isEmpty()) {
            timedMenu.infx$setSelectedQualityCode(QualitySystem.AVERAGE_CODE);
            timedMenu.infx$setSelectedRune(0);
            return;
        }
        CraftingMatch holder = match.orElseThrow();
        CraftingResult assembledResult = holder.assemble(timedMenu.infx$craftingContainer().asCraftInput());
        ItemStack output = assembledResult.stack();
        if (RuneStoneBlock.isRuneStone(output)) {
            timedMenu.infx$setSelectedRune(RuneStoneBlock.nextRune(timedMenu.infx$selectedRune()));
            timedMenu.infx$resetTimedCrafting();
            refreshResult(timedMenu, player, true);
            return;
        }
        if (!QualitySystem.supportsQuality(output)) {
            return;
        }
        boolean witchClumsiness = CraftingEnvironment.hasWitchClumsiness(player);
        boolean clumsy = witchClumsiness || CraftingEnvironment.hasEnchantedClumsiness(player);
        int code = QualitySystem.cycleCode(
                output,
                player,
                holder.profile().difficulty(),
                timedMenu.infx$selectedQualityCode(),
                clumsy,
                witchClumsiness);
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
                !InfXConfig.INSTANCE.survival.enabled.getValue()
                        || !InfXConfig.INSTANCE.survival.craftingRequiresFoodEnergy.getValue()
                        || player.getData(InfXAttachments.SURVIVAL).hasFoodEnergy(),
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
        // Every crafting recipe now uses the standard CRAFTING type; the INFX
        // profile (difficulty, workbench tier) comes from the data-driven
        // recipe rules with InfxCraftingRules inference as the fallback.
        //
        // Restored vanilla recipes share the crafting grid with INFX recipes
        // for items that have both (buckets, armor, anvils, chains, ...).
        // Recipes with an explicit rule win such ties so that the INFX item
        // stays craftable; unmatched recipes keep the map (ID) order.
        List<CraftingMatch> matches = level.recipeAccess()
                .recipeMap()
                .getRecipesFor(RecipeType.CRAFTING, input, level)
                .map(holder -> CraftingMatch.vanilla(holder, input))
                .filter(match -> timedMenu.infx$benchTier().supports(match.profile().requiredBench()))
                .toList();
        for (CraftingMatch match : matches) {
            if (RecipeRules.ruleFor(match.holder().id()).isPresent()) {
                return Optional.of(match);
            }
        }
        return matches.stream().findFirst();
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

        CraftingResult assembledResult = holder.assemble(input);
        ItemStack output = assembledResult.stack();
        if (assembledResult.isEmpty()) {
            timedMenu.infx$resetTimedCrafting();
            return;
        }
        int coinExperience = coinExperience(input);
        boolean witchClumsiness = CraftingEnvironment.hasWitchClumsiness(player);
        boolean clumsy = witchClumsiness || CraftingEnvironment.hasEnchantedClumsiness(player);
        int qualityCode = QualitySystem.clampCode(
                output,
                player,
                holder.profile().difficulty(),
                timedMenu.infx$selectedQualityCode(),
                clumsy,
                witchClumsiness);
        QualitySystem.applySelectedQuality(output, qualityCode);
        applySelectedRune(timedMenu, output);
        int craftingCost = craftingExperienceCost(
                output, assembledResult.totalCount(), holder.profile().difficulty(), qualityCode, clumsy);
        timedMenu.infx$setExperienceCost(craftingCost);
        if (!canPayExperience(player, craftingCost)) {
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
        output.onCraftedBy(player, assembledResult.totalCount());
        List<ItemStack> outputStacks = assembledResult.split();
        for (ItemStack outputStack : outputStacks) {
            EventHooks.firePlayerCraftingEvent(player, outputStack, craftSlots);
        }
        timedMenu.infx$resultContainer().setRecipeUsed(holder.holder());
        timedMenu.infx$resultContainer().awardUsedRecipes(player, inputsForCriterion);
        if (craftingCost > 0) {
            player.giveExperiencePoints(-craftingCost);
        }

        consumeInputsAndReturnContainers(player, craftSlots, positioned, remaining);
        for (ItemStack outputStack : outputStacks) {
            player.getInventory().placeItemBackInInventory(outputStack);
        }
        if (coinExperience > 0) {
            player.giveExperiencePoints(coinExperience);
        }

        boolean stillSameRecipe = refreshResult(timedMenu, player, true)
                && findRecipe(timedMenu, player.level())
                        .map(next -> next.holder().id().equals(holder.holder().id()))
                        .orElse(false);
        if (stillSameRecipe && canPayExperience(player, timedMenu.infx$experienceCost())) {
            float adjustedDifficulty = QualitySystem.adjustedDifficulty(
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

    private static int craftingExperienceCost(
            ItemStack output,
            int outputCount,
            float difficulty,
            int qualityCode,
            boolean clumsy) {
        Quality quality = QualitySystem.fromCode(qualityCode);
        int qualityCost = QualitySystem.experienceCost(difficulty, quality, clumsy);
        if (!(output.getItem() instanceof CoinItem coin)) {
            return qualityCost;
        }
        return Math.addExact(qualityCost, Math.multiplyExact(coin.experienceValue(), outputCount));
    }

    private static boolean canPayExperience(Player player, int cost) {
        return cost <= 0 || player.totalExperience >= cost;
    }

    private static int coinExperience(CraftingInput input) {
        int experience = 0;
        for (ItemStack stack : input.items()) {
            if (stack.getItem() instanceof CoinItem coin) {
                experience += coin.experienceValue();
            }
        }
        return experience;
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

    private static void applySelectedRune(TimedCraftingMenu timedMenu, ItemStack stack) {
        if (RuneStoneBlock.isRuneStone(stack)) {
            RuneStoneBlock.applyRune(stack, timedMenu.infx$selectedRune());
        } else {
            timedMenu.infx$setSelectedRune(0);
        }
    }

    private static AbstractContainerMenu asContainerMenu(TimedCraftingMenu menu) {
        return (AbstractContainerMenu) menu;
    }

    private static String recipeId(ResourceKey<Recipe<?>> id) {
        return id.identifier().toString();
    }

    private static CraftingResult displayResult(CraftingRecipe recipe) {
        for (RecipeDisplay display : recipe.display()) {
            Optional<ItemStackTemplate> template = displayTemplate(display.result());
            if (template.isPresent()) {
                CraftingResult result = CraftingResult.fromTemplate(template.orElseThrow());
                if (!result.isEmpty()) {
                    return result;
                }
            }
        }
        return CraftingResult.EMPTY;
    }

    private static Optional<ItemStackTemplate> displayTemplate(SlotDisplay display) {
        if (display instanceof SlotDisplay.ItemStackSlotDisplay itemStackDisplay) {
            return Optional.of(itemStackDisplay.stack());
        }
        if (display instanceof SlotDisplay.Composite composite) {
            for (SlotDisplay child : composite.contents()) {
                Optional<ItemStackTemplate> template = displayTemplate(child);
                if (template.isPresent()) {
                    return template;
                }
            }
        }
        if (display instanceof SlotDisplay.WithRemainder remainder) {
            return displayTemplate(remainder.input());
        }
        return Optional.empty();
    }

    private record CraftingResult(ItemStack stack, int totalCount) {
        private static final CraftingResult EMPTY = new CraftingResult(ItemStack.EMPTY, 0);

        private static CraftingResult fromStackStrict(ItemStack output) {
            if (output == null || output.isEmpty()) {
                return EMPTY;
            }
            int totalCount = output.getCount();
            int maxStackSize = Math.max(1, output.getMaxStackSize());
            if (totalCount > maxStackSize) {
                return EMPTY;
            }
            ItemStack preview = output.copy();
            return preview.isEmpty() ? EMPTY : new CraftingResult(preview, totalCount);
        }

        private static CraftingResult fromTemplate(ItemStackTemplate template) {
            if (template == null || template.count() <= 0) {
                return EMPTY;
            }
            ItemStack single = template.apply(1, DataComponentPatch.EMPTY);
            if (single.isEmpty()) {
                return EMPTY;
            }
            int maxStackSize = Math.max(1, single.getMaxStackSize());
            int previewCount = Math.min(template.count(), maxStackSize);
            ItemStack preview = template.apply(previewCount, DataComponentPatch.EMPTY);
            return preview.isEmpty() ? EMPTY : new CraftingResult(preview, template.count());
        }

        private boolean isEmpty() {
            return stack.isEmpty() || totalCount <= 0;
        }

        private List<ItemStack> split() {
            if (isEmpty()) {
                return List.of();
            }
            int maxStackSize = Math.max(1, stack.getMaxStackSize());
            int remaining = totalCount;
            List<ItemStack> result = new java.util.ArrayList<>((totalCount + maxStackSize - 1) / maxStackSize);
            while (remaining > 0) {
                int count = Math.min(remaining, maxStackSize);
                result.add(stack.copyWithCount(count));
                remaining -= count;
            }
            return List.copyOf(result);
        }
    }

    private record CraftingMatch(RecipeHolder<CraftingRecipe> holder, CraftingProfile profile) {
        static CraftingMatch vanilla(
                RecipeHolder<CraftingRecipe> holder,
                CraftingInput input) {
            return new CraftingMatch(holder, RecipeRules.profile(holder, input));
        }

        boolean matches(CraftingInput input, ServerLevel level) {
            return holder.value().matches(input, level);
        }

        CraftingResult assemble(CraftingInput input) {
            if (holder.value() instanceof ShapedRecipe || holder.value() instanceof ShapelessRecipe) {
                CraftingResult displayed = displayResult(holder.value());
                if (!displayed.isEmpty()) {
                    return displayed;
                }
            }
            try {
                // Special/dynamic recipes do not expose a stable original
                // output template. Keep strict validation for them: only a
                // result that already fits the InfX stack limit is safe to
                // deliver.
                CraftingResult assembled = CraftingResult.fromStackStrict(holder.value().assemble(input));
                if (!assembled.isEmpty()) {
                    return assembled;
                }
            } catch (RuntimeException ignored) {
                // A static recipe can legitimately fail strict ItemStack
                // validation when its vanilla result exceeds InfX's limit;
                // the display template below retains the original quantity.
            }
            if (holder.value() instanceof ShapedRecipe || holder.value() instanceof ShapelessRecipe) {
                return displayResult(holder.value());
            }
            return CraftingResult.EMPTY;
        }

        NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
            return holder.value().getRemainingItems(input);
        }
    }
}
