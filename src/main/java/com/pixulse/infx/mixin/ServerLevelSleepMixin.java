package com.pixulse.infx.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.SleepStatus;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Vanilla immediately moves the clock and wakes every player once enough sleepers are present.
 * R196 instead simulates its own sleep ticks, so only the Overworld needs this narrow redirect.
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelSleepMixin {
    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/SleepStatus;areEnoughSleeping(I)Z"))
    private boolean infx$deferOverworldSleepFastForward(SleepStatus sleepStatus, int sleepPercentage) {
        ServerLevel level = (ServerLevel) (Object) this;
        return !level.dimension().equals(Level.OVERWORLD) && sleepStatus.areEnoughSleeping(sleepPercentage);
    }
}
