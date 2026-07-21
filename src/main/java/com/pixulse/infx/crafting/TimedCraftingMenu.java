package com.pixulse.infx.crafting;

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
    int DATA_COUNT = 6;

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
        infx$syncCraftingData();
    }

    default void infx$syncCraftingData() {
        TimedCraftingState state = infx$craftingState();
        ContainerData data = infx$craftingData();
        data.set(DATA_PROGRESS, state.progressTicks());
        data.set(DATA_REQUIRED, state.requiredTicks());
        data.set(DATA_RUNNING, state.isRunning() ? 1 : 0);
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

    default int infx$scaledProgress(int width) {
        int required = infx$requiredTicks();
        return required <= 0 ? 0 : Math.min(width, infx$progressTicks() * width / required);
    }
}
