package com.pixulse.infx.mixin.world.level.storage;

import com.pixulse.infx.world.AllowCommandsAccess;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Restores the command-permission mutator that IntegratedServer gained after 26.1.2. */
@Mixin(PrimaryLevelData.class)
public abstract class PrimaryLevelDataMixin implements AllowCommandsAccess {
    @Shadow private LevelSettings settings;

    @Override
    public void infx$setAllowCommands(boolean allowCommands) {
        LevelSettings current = this.settings;
        this.settings = new LevelSettings(
                current.levelName(),
                current.gameType(),
                current.difficultySettings(),
                allowCommands,
                current.dataConfiguration(),
                current.lifecycle());
    }
}
