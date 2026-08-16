package com.pixulse.infx.mixin.world.level;

import com.pixulse.infx.world.MoonPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MITE: blood-moon days bring rain to every overworld biome, bypassing the hot-biome
 * no-precipitation gate so undead stay unburned and crops get watered even in deserts.
 */
@Mixin(Level.class)
public abstract class LevelBloodMoonRainMixin {
    @Inject(method = "precipitationAt", at = @At("HEAD"), cancellable = true)
    private void infx$bloodMoonPrecipitation(BlockPos pos, CallbackInfoReturnable<Biome.Precipitation> cir) {
        Level level = (Level) (Object) this;
        if (!MoonPhase.BLOOD.isActiveInOverworld(level)
                || !level.isRaining()
                || !level.canSeeSky(pos)
                || level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).getY() > pos.getY()) {
            return;
        }
        Biome biome = level.getBiome(pos).value();
        cir.setReturnValue(MoonPhase.bloodMoonPrecipitation(biome, pos, level.getSeaLevel()));
    }
}
