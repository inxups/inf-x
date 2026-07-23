package com.pixulse.infx.mixin;

import com.pixulse.infx.world.MiteUnderworldStrata;
import com.pixulse.infx.world.Underworld;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Surface rules cannot express MITE's seeded per-column boundary thickness. */
@Mixin(NoiseBasedChunkGenerator.class)
abstract class NoiseBasedChunkGeneratorMixin {
    @Inject(
            method = "buildSurface("
                    + "Lnet/minecraft/server/level/WorldGenRegion;"
                    + "Lnet/minecraft/world/level/StructureManager;"
                    + "Lnet/minecraft/world/level/levelgen/RandomState;"
                    + "Lnet/minecraft/world/level/chunk/ChunkAccess;)V",
            at = @At("TAIL"))
    private void infx$applyMiteUnderworldStrata(
            WorldGenRegion region,
            StructureManager structureManager,
            RandomState randomState,
            ChunkAccess chunk,
            CallbackInfo callback) {
        if (!region.getLevel().dimension().equals(Underworld.LEVEL)) return;
        MiteUnderworldStrata.apply(region.getSeed(), chunk);
    }
}
