package com.pixulse.infx.mixin.world.level.levelgen;

import com.pixulse.infx.world.LavaLakeRules;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prevents aquifer lakes that are open to the sky from becoming lava, so the overworld
 * surface no longer generates exposed lava lakes while fully buried cave lava lakes keep
 * generating. The fluid-type decision has no public extension point; the aquifer's
 * preliminary-surface sampling is only reachable through the chunk-level noise router.
 */
@Mixin(targets = "net.minecraft.world.level.levelgen.Aquifer$NoiseBasedAquifer")
public abstract class NoiseBasedAquiferMixin {
    @Shadow
    private NoiseChunk noiseChunk;

    @Inject(method = "computeFluidType", at = @At("HEAD"), cancellable = true)
    private void infx$keepSurfaceLavaLakesOut(
            int x, int y, int z, Aquifer.FluidStatus globalFluid, int fluidSurfaceLevel,
            CallbackInfoReturnable<BlockState> callback) {
        if (LavaLakeRules.isSurfaceLake(this.noiseChunk.preliminarySurfaceLevel(x, z), fluidSurfaceLevel)) {
            callback.setReturnValue(globalFluid.fluidType());
        }
    }
}
