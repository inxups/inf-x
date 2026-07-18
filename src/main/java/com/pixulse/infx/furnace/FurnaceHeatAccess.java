package com.pixulse.infx.furnace;

public interface FurnaceHeatAccess {
    int infx$currentHeat();

    void infx$setCurrentHeat(int heat);

    int infx$litTimeRemaining();

    void infx$setLitTimeRemaining(int ticks);

    void infx$setCookingTimer(int ticks);
}
