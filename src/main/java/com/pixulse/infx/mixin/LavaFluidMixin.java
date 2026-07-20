package com.pixulse.infx.mixin;

import com.pixulse.infx.entity.R196MonsterEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.LavaFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Restores R196's source-lava fire-elemental spawn hook at the vanilla random-tick boundary. */
@Mixin(LavaFluid.class)
public abstract class LavaFluidMixin {
    @Inject(method = "randomTick", at = @At("HEAD"))
    private void infx$spawnFireElemental(
            ServerLevel level, BlockPos pos, FluidState state, RandomSource random, CallbackInfo callback) {
        R196MonsterEvents.trySpawnFireElemental(level, pos, state, random);
    }
}
