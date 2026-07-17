package com.pixulse.infx.crafting;

import java.util.Objects;

public final class TimedCraftingState {
    private String activeRecipeId = "";
    private int progressTicks;
    private int requiredTicks;
    private boolean running;

    public boolean start(String recipeId, int ticks) {
        Objects.requireNonNull(recipeId, "recipeId");
        if (recipeId.isBlank()) {
            throw new IllegalArgumentException("recipeId must not be blank");
        }
        if (ticks <= 0) {
            throw new IllegalArgumentException("required ticks must be positive");
        }
        if (running && activeRecipeId.equals(recipeId) && requiredTicks == ticks) {
            return false;
        }
        activeRecipeId = recipeId;
        progressTicks = 0;
        requiredTicks = ticks;
        running = true;
        return true;
    }

    public TickResult tick(String currentRecipeId, boolean hasFood, boolean benchValid, boolean ingredientsAvailable) {
        if (!running) {
            return TickResult.IDLE;
        }
        if (!activeRecipeId.equals(currentRecipeId) || !benchValid || !ingredientsAvailable) {
            reset();
            return TickResult.RESET;
        }
        if (!hasFood) {
            return TickResult.PAUSED;
        }
        progressTicks++;
        if (progressTicks >= requiredTicks) {
            progressTicks = 0;
            return TickResult.COMPLETED;
        }
        return TickResult.PROGRESSED;
    }

    public void reset() {
        activeRecipeId = "";
        progressTicks = 0;
        requiredTicks = 0;
        running = false;
    }

    public String activeRecipeId() {
        return activeRecipeId;
    }

    public int progressTicks() {
        return progressTicks;
    }

    public int requiredTicks() {
        return requiredTicks;
    }

    public boolean isRunning() {
        return running;
    }

    public enum TickResult {
        IDLE,
        PAUSED,
        PROGRESSED,
        COMPLETED,
        RESET
    }
}
