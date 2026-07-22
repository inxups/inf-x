package com.pixulse.infx.mixin;

import com.pixulse.infx.server.ExtremeDifficulty;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Makes explicitly constructed regional difficulty use the same Hard baseline as a level lookup.
 *
 * <p>Vanilla obtains this value through {@code LevelAccessor#getDifficulty()}, but public APIs
 * can construct a {@link DifficultyInstance} from serialized world data directly.
 */
@Mixin(DifficultyInstance.class)
public abstract class DifficultyInstanceMixin {
    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
    private static Difficulty infx$useHardGameplayBase(Difficulty difficulty) {
        return ExtremeDifficulty.gameplayDifficulty(difficulty);
    }
}
