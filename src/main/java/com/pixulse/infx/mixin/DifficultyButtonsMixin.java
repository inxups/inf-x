package com.pixulse.infx.mixin;

import net.minecraft.client.gui.screens.options.DifficultyButtons;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Keeps the locked in-game difficulty control aligned with the serialized Extreme label. */
@Mixin(DifficultyButtons.class)
public abstract class DifficultyButtonsMixin {
    @Redirect(
            method = "create",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getDifficulty()Lnet/minecraft/world/Difficulty;"))
    private static Difficulty infx$showSerializedDifficultyOnCreate(Level level) {
        return level.getLevelData().getDifficulty();
    }

    @Redirect(
            method = "refresh",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getDifficulty()Lnet/minecraft/world/Difficulty;"))
    private Difficulty infx$showSerializedDifficultyOnRefresh(Level level) {
        return level.getLevelData().getDifficulty();
    }
}
