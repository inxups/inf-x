package com.pixulse.infx.client;

import com.pixulse.infx.recipe.TimedCraftingMenu;

/** Smooths server-authoritative crafting ticks without predicting through pauses. */
public final class CraftingProgressSmoother {
    static final long INTERPOLATION_NANOS = 50_000_000L;
    static final long COMPLETION_HOLD_NANOS = 50_000_000L;

    private boolean initialized;
    private int lastProgress;
    private int lastRequired;
    private int lastCycleSequence;
    private boolean lastRunning;
    private float displayed;
    private float animationFrom;
    private float animationTo;
    private long animationStartNanos;
    private long animationEndNanos;
    private Phase phase = Phase.IDLE;
    private float queuedTarget;
    private boolean queuedActive;

    public float sample(TimedCraftingMenu menu) {
        return sample(
                menu.infx$progressTicks(),
                menu.infx$requiredTicks(),
                menu.infx$isRunning(),
                menu.infx$cycleSequence(),
                System.nanoTime());
    }

    float sample(int progress, int required, boolean running, int cycleSequence, long nowNanos) {
        float target = normalized(progress, required, running);
        boolean active = running && required > 0;
        if (!initialized) {
            initialized = true;
            lastProgress = progress;
            lastRequired = required;
            lastRunning = running;
            lastCycleSequence = cycleSequence;
            queuedTarget = target;
            queuedActive = active;
            snap(target);
            return displayed;
        }

        float current = advance(nowNanos);
        int previousProgress = lastProgress;
        int previousRequired = lastRequired;
        boolean previousRunning = lastRunning;
        boolean cycleCompleted = cycleSequence != lastCycleSequence;
        boolean progressChanged = progress != previousProgress;
        boolean requiredChanged = required != previousRequired;

        lastProgress = progress;
        lastRequired = required;
        lastRunning = running;
        lastCycleSequence = cycleSequence;
        queuedTarget = target;
        queuedActive = active;

        if (cycleCompleted && previousRunning && previousRequired > 0) {
            begin(current, 1.0F, nowNanos, INTERPOLATION_NANOS, Phase.COMPLETING);
            return advance(nowNanos);
        }
        if (phase == Phase.COMPLETING || phase == Phase.HOLDING) {
            return advance(nowNanos);
        }
        if (!active) {
            snap(0.0F);
            return displayed;
        }
        if (requiredChanged || progress < previousProgress) {
            snap(target);
            return displayed;
        }
        if (progressChanged) {
            begin(current, target, nowNanos, INTERPOLATION_NANOS, Phase.INTERPOLATING);
        }
        return advance(nowNanos);
    }

    public static PixelFill splitPixels(float progress, int width) {
        if (width <= 0) {
            throw new IllegalArgumentException("width must be positive");
        }
        float scaled = clamp(progress) * width;
        int fullPixels = Math.min(width, (int) Math.floor(scaled));
        float nextPixelAlpha = fullPixels == width ? 0.0F : clamp(scaled - fullPixels);
        return new PixelFill(fullPixels, nextPixelAlpha);
    }

    private float advance(long nowNanos) {
        while (true) {
            switch (phase) {
                case IDLE -> {
                    return displayed;
                }
                case INTERPOLATING, COMPLETING -> {
                    if (nowNanos < animationEndNanos) {
                        float delta = (float) (nowNanos - animationStartNanos)
                                / (animationEndNanos - animationStartNanos);
                        displayed = animationFrom + (animationTo - animationFrom) * clamp(delta);
                        return displayed;
                    }
                    displayed = animationTo;
                    if (phase == Phase.INTERPOLATING) {
                        phase = Phase.IDLE;
                        continue;
                    }
                    phase = Phase.HOLDING;
                    animationStartNanos = animationEndNanos;
                    animationEndNanos = animationStartNanos + COMPLETION_HOLD_NANOS;
                }
                case HOLDING -> {
                    if (nowNanos < animationEndNanos) {
                        displayed = 1.0F;
                        return displayed;
                    }
                    long resumeNanos = animationEndNanos;
                    displayed = 0.0F;
                    phase = Phase.IDLE;
                    if (queuedActive && queuedTarget > 0.0F) {
                        begin(
                                0.0F,
                                queuedTarget,
                                resumeNanos,
                                INTERPOLATION_NANOS,
                                Phase.INTERPOLATING);
                    }
                }
            }
        }
    }

    private void begin(float from, float to, long startNanos, long durationNanos, Phase nextPhase) {
        displayed = clamp(from);
        animationFrom = displayed;
        animationTo = clamp(to);
        animationStartNanos = startNanos;
        animationEndNanos = startNanos + durationNanos;
        phase = nextPhase;
    }

    private void snap(float progress) {
        displayed = clamp(progress);
        animationFrom = displayed;
        animationTo = displayed;
        phase = Phase.IDLE;
    }

    private static float normalized(int progress, int required, boolean running) {
        return !running || required <= 0 ? 0.0F : clamp((float) progress / required);
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    public record PixelFill(int fullPixels, float nextPixelAlpha) {}

    private enum Phase {
        IDLE,
        INTERPOLATING,
        COMPLETING,
        HOLDING
    }
}
