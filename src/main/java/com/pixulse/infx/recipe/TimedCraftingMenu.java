package com.pixulse.infx.recipe;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;

public interface TimedCraftingMenu {
    int DATA_PROGRESS = 0;
    int DATA_REQUIRED = 1;
    int DATA_RUNNING = 2;
    int DATA_TIMED_RESULT = 3;
    int DATA_QUALITY = 4;
    int DATA_RUNE = 5;
    int DATA_CYCLE_SEQUENCE = 6;
    int DATA_EXPERIENCE_COST = 7;
    /** Logical recipe output count, which may exceed the physical stack in the result slot. */
    int DATA_RESULT_COUNT = 8;
    int DATA_COUNT = 9;

    BenchTier infx$benchTier();

    CraftingContainer infx$craftingContainer();

    ResultContainer infx$resultContainer();

    TimedCraftingState infx$craftingState();

    ContainerData infx$craftingData();

    boolean infx$isCraftingContextValid(Player player);

    boolean infx$hasTimedResult();

    void infx$setHasTimedResult(boolean hasTimedResult);

    long infx$lastCraftingTick();

    void infx$setLastCraftingTick(long gameTime);

    default int infx$resultSlotIndex() {
        return 0;
    }

    default void infx$startTimedCrafting(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            TimedCraftingEngine.start(this, serverPlayer);
        }
    }

    default int infx$selectedQualityCode() {
        return infx$craftingData().get(DATA_QUALITY);
    }

    default void infx$setSelectedQualityCode(int code) {
        infx$craftingData().set(DATA_QUALITY, code);
    }

    default int infx$selectedRune() {
        return infx$craftingData().get(DATA_RUNE);
    }

    default void infx$setSelectedRune(int rune) {
        infx$craftingData().set(DATA_RUNE, rune);
    }

    default int infx$experienceCost() {
        return infx$craftingData().get(DATA_EXPERIENCE_COST);
    }

    default void infx$setExperienceCost(int cost) {
        infx$craftingData().set(DATA_EXPERIENCE_COST, Math.max(0, cost));
    }

    /**
     * Returns the full recipe output count.  The result slot itself always
     * contains a legal stack, so this value is used for the client decoration
     * and for delivery after timed crafting completes.
     */
    default int infx$logicalResultCount() {
        return infx$craftingData().get(DATA_RESULT_COUNT);
    }

    default void infx$setLogicalResultCount(int count) {
        infx$craftingData().set(DATA_RESULT_COUNT, Math.max(0, count));
    }

    default void infx$cycleResult(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            TimedCraftingEngine.cycleResult(this, serverPlayer);
        }
    }

    default void infx$tickTimedCrafting(ServerPlayer player) {
        TimedCraftingEngine.tick(this, player);
    }

    default void infx$resetTimedCrafting() {
        infx$craftingState().reset();
        infx$setExperienceCost(0);
        infx$setLogicalResultCount(0);
        infx$syncCraftingData();
    }

    default void infx$syncCraftingData() {
        TimedCraftingState state = infx$craftingState();
        ContainerData data = infx$craftingData();
        data.set(DATA_PROGRESS, state.progressTicks());
        data.set(DATA_REQUIRED, state.requiredTicks());
        data.set(DATA_RUNNING, state.isRunning() ? 1 : 0);
        data.set(DATA_CYCLE_SEQUENCE, state.cycleSequence());
    }

    default int infx$progressTicks() {
        return infx$craftingData().get(DATA_PROGRESS);
    }

    default int infx$requiredTicks() {
        return infx$craftingData().get(DATA_REQUIRED);
    }

    default boolean infx$isRunning() {
        return infx$craftingData().get(DATA_RUNNING) != 0;
    }

    default int infx$cycleSequence() {
        return infx$craftingData().get(DATA_CYCLE_SEQUENCE);
    }

    default int infx$scaledProgress(int width) {
        int required = infx$requiredTicks();
        return required <= 0 ? 0 : Math.min(width, infx$progressTicks() * width / required);
    }
}
