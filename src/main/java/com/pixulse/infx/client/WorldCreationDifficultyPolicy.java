package com.pixulse.infx.client;

import com.pixulse.infx.InfiniteXTestMode;
import com.pixulse.infx.world.WorldCreationLockProfile;
import net.minecraft.world.Difficulty;

/** Resolves the locked difficulty before vanilla coerces Hardcore world creation to Hard. */
public final class WorldCreationDifficultyPolicy {
    private WorldCreationDifficultyPolicy() {}

    public static Difficulty resolve(Difficulty vanillaDifficulty, boolean hardcore) {
        return !InfiniteXTestMode.isEnabled() && hardcore ? WorldCreationLockProfile.difficulty() : vanillaDifficulty;
    }
}
