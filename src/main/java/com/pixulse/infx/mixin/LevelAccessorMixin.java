package com.pixulse.infx.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.pixulse.infx.server.ExtremeDifficulty;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Preserves Extreme in level data while presenting Hard to vanilla and NeoForge gameplay logic.
 *
 * <p>This is necessary because both systems exhaustively switch on Minecraft's four built-in
 * difficulties. The serialized value remains available through {@code LevelData#getDifficulty()}.
 */
@Mixin(LevelAccessor.class)
public interface LevelAccessorMixin {
    @ModifyReturnValue(method = "getDifficulty", at = @At("RETURN"))
    private Difficulty infx$useHardGameplayValues(Difficulty difficulty) {
        return ExtremeDifficulty.gameplayDifficulty(difficulty);
    }
}
