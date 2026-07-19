package com.pixulse.infx.server;

import com.pixulse.infx.InfiniteX;
import java.util.Objects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.jspecify.annotations.Nullable;

/** Owns the independent fifth Minecraft difficulty installed by {@code DifficultyMixin}. */
public final class ExtremeDifficulty {
    public static final String NAME = "extreme";
    private static @Nullable Difficulty value;

    private ExtremeDifficulty() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(ExtremeDifficulty::onServerStarted);
    }

    public static Difficulty value() {
        Difficulty.values();
        return Objects.requireNonNull(value, "Extreme difficulty was not installed by its mixin");
    }

    public static boolean isExtreme(Difficulty difficulty) {
        return difficulty == value;
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

    private static void onServerStarted(ServerStartedEvent event) {
        apply(event.getServer());
        InfiniteX.LOGGER.info("InfiniteX Extreme difficulty is active and locked");
    }
}
