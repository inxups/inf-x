package com.pixulse.infx.world;

import com.pixulse.infx.event.server.ExtremeDifficulty;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;

/** The constrained single-player world profile exposed by InfiniteX's creation screen. */
public final class WorldCreationLockProfile {
    public static final GameType GAME_TYPE = GameType.SURVIVAL;
    public static final boolean ALLOW_COMMANDS = false;
    public static final boolean BONUS_CHEST = false;
    public static final boolean ALLOW_ADVANCED_CONFIGURATION = false;

    private WorldCreationLockProfile() {}

    public static Difficulty difficulty() {
        return ExtremeDifficulty.value();
    }
}
