package com.pixulse.infx.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TimedCraftingStateTest {
    @Test
    void clickStartsAndARepeatedClickDoesNotResetTheSameRecipe() {
        TimedCraftingState state = new TimedCraftingState();

        assertTrue(state.start("infx:test", 3));
        assertEquals(TimedCraftingState.TickResult.PROGRESSED, state.tick("infx:test", true, true, true));
        assertFalse(state.start("infx:test", 3));
        assertEquals(1, state.progressTicks());
    }

    @Test
    void zeroFoodPausesWithoutLosingProgress() {
        TimedCraftingState state = runningState(3);
        state.tick("infx:test", true, true, true);

        assertEquals(TimedCraftingState.TickResult.PAUSED, state.tick("infx:test", false, true, true));
        assertEquals(1, state.progressTicks());
        assertTrue(state.isRunning());
    }

    @Test
    void recipeChangeResetsAllState() {
        TimedCraftingState state = runningState(3);

        assertEquals(TimedCraftingState.TickResult.RESET, state.tick("infx:other", true, true, true));
        assertReset(state);
    }

    @Test
    void invalidBenchOrMissingIngredientsResetsAllState() {
        TimedCraftingState invalidBench = runningState(3);
        TimedCraftingState missingIngredients = runningState(3);

        assertEquals(TimedCraftingState.TickResult.RESET, invalidBench.tick("infx:test", true, false, true));
        assertEquals(TimedCraftingState.TickResult.RESET, missingIngredients.tick("infx:test", true, true, false));
        assertReset(invalidBench);
        assertReset(missingIngredients);
    }

    @Test
    void completionKeepsRunningAndStartsTheNextCycleAtZero() {
        TimedCraftingState state = runningState(2);

        assertEquals(TimedCraftingState.TickResult.PROGRESSED, state.tick("infx:test", true, true, true));
        assertEquals(TimedCraftingState.TickResult.COMPLETED, state.tick("infx:test", true, true, true));
        assertTrue(state.isRunning());
        assertEquals(0, state.progressTicks());
        assertEquals("infx:test", state.activeRecipeId());
    }

    @Test
    void closeExplicitlyResetsProgress() {
        TimedCraftingState state = runningState(3);
        state.tick("infx:test", true, true, true);

        state.reset();

        assertReset(state);
    }

    private static TimedCraftingState runningState(int requiredTicks) {
        TimedCraftingState state = new TimedCraftingState();
        state.start("infx:test", requiredTicks);
        return state;
    }

    private static void assertReset(TimedCraftingState state) {
        assertFalse(state.isRunning());
        assertEquals("", state.activeRecipeId());
        assertEquals(0, state.progressTicks());
        assertEquals(0, state.requiredTicks());
    }
}
