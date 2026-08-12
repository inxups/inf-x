package com.pixulse.infx.data.harvest;

/** Accumulates server mining progress when the per-tick destroy speed can change mid-session. */
public final class MiningProgressAccumulator {
    private boolean active;
    private float progress;

    public void start(float initialProgress) {
        active = true;
        progress = initialProgress;
    }

    public boolean isActive() {
        return active;
    }

    public float advance(float progressThisTick) {
        progress += progressThisTick;
        return progress;
    }

    public float progress() {
        return progress;
    }

    public void reset() {
        active = false;
        progress = 0.0F;
    }
}
