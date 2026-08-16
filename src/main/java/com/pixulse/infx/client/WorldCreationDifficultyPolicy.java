package com.pixulse.infx.client;

import com.pixulse.infx.InfiniteXDevMode;
import com.pixulse.infx.world.WorldCreationLockProfile;
import net.minecraft.world.Difficulty;

/** Resolves the locked difficulty before vanilla coerces Hardcore world creation to Hard. */
public final class WorldCreationDifficultyPolicy {
    private WorldCreationDifficultyPolicy() {}

    public static Difficulty resolve(Difficulty vanillaDifficulty, boolean hardcore) {
        return !InfiniteXDevMode.isClientEnabled() && hardcore ? WorldCreationLockProfile.difficulty() : vanillaDifficulty;
    }
}
