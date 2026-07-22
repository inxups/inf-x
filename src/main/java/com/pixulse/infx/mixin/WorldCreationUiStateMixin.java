package com.pixulse.infx.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.pixulse.infx.client.WorldCreationDifficultyPolicy;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.world.Difficulty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Replaces vanilla's hardcoded Hardcore-to-Hard return because world creation has no public difficulty hook.
 */
@Mixin(WorldCreationUiState.class)
public abstract class WorldCreationUiStateMixin {
    @ModifyReturnValue(method = "getDifficulty", at = @At("RETURN"))
    private Difficulty infx$keepHardcoreWorldCreationExtreme(Difficulty vanillaDifficulty) {
        return WorldCreationDifficultyPolicy.resolve(
                vanillaDifficulty, ((WorldCreationUiState) (Object) this).isHardcore());
    }
}
