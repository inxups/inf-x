package com.pixulse.infx.mixin.world.level.levelgen;

import com.pixulse.infx.InfiniteXDevMode;
import com.pixulse.infx.world.WorldDimensionSelectionPolicy;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * WorldDimensions has no event or public merge hook before datapack dimensions override a selected
 * world preset. Test mode must prefer that preset so flat and other development worlds are real.
 */
@Mixin(WorldDimensions.class)
public abstract class WorldDimensionsMixin {
    @Shadow
    public abstract Map<ResourceKey<LevelStem>, LevelStem> dimensions();

    @ModifyVariable(method = "bake", at = @At("HEAD"), argsOnly = true)
    private Registry<LevelStem> infx$preferSelectedOverworldInDevMode(Registry<LevelStem> datapackDimensions) {
        return WorldDimensionSelectionPolicy.resolve(
                datapackDimensions, this.dimensions(), InfiniteXDevMode.isServerEnabled());
    }
}
