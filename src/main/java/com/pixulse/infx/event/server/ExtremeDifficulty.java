package com.pixulse.infx.event.server;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.InfiniteXTestMode;
import java.util.Objects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.jspecify.annotations.Nullable;

/** Owns the independent fifth Minecraft difficulty installed by {@code DifficultyMixin}. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class ExtremeDifficulty {
    public static final String NAME = "extreme";
    private static @Nullable Difficulty value;

    private ExtremeDifficulty() {}

    public static Difficulty value() {
        Difficulty.values();
        return Objects.requireNonNull(value, "Extreme difficulty was not installed by its mixin");
    }

    public static boolean isExtreme(Difficulty difficulty) {
        return difficulty == value;
    }

    /**
     * Keeps Extreme serialized as its own difficulty while its current gameplay values match Hard.
     *
     * <p>Future Extreme-specific tuning belongs at explicit call sites instead of changing the
     * serialized enum value's ID or ordinal.
     */
    public static Difficulty gameplayDifficulty(Difficulty difficulty) {
        return isExtreme(difficulty) ? Difficulty.HARD : difficulty;
    }

    /** Applies Extreme directly so hardcore worlds are not coerced back to vanilla Hard. */
    public static void apply(MinecraftServer server) {
        server.getWorldData().setDifficulty(value());
        server.updateMobSpawningFlags();
        server.setDifficultyLocked(true);
    }

    public static boolean isActive(Difficulty difficulty, boolean locked) {
        return isExtreme(difficulty) && locked;
    }

    /** Called from the Difficulty class initializer after the four vanilla values exist. */
    public static void infx$bootstrap(Difficulty difficulty) {
        if (value != null) throw new IllegalStateException("Extreme difficulty was installed twice");
        value = difficulty;
    }

    /** Returns null only while Difficulty is still being initialized. */
    public static @Nullable Difficulty infx$peek() {
        return value;
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        if (InfiniteXTestMode.isEnabled()) return;
        apply(event.getServer());
        InfiniteX.LOGGER.info("InfiniteX Extreme difficulty is active and locked");
    }
}
