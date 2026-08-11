package com.pixulse.infx.mixin.world.level.block;

import com.pixulse.infx.block.BlueberryBushBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * InfX sweet berry bushes may only be planted in taiga-family biomes. The vanilla bush
 * inherits {@link VegetationBlock#canSurvive}, so the gate is injected there and scoped
 * to sweet berry bushes; the INFX blueberry bush stays unrestricted.
 */
@Mixin(VegetationBlock.class)
public abstract class VegetationBlockSweetBerrySurvivalMixin {
    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void infx$taigaOnlySweetBerries(
            BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> callback) {
        if (state.getBlock() instanceof SweetBerryBushBlock
                && !(state.getBlock() instanceof BlueberryBushBlock)
                && !level.getBiome(pos).is(BiomeTags.IS_TAIGA)) {
            callback.setReturnValue(false);
        }
    }
}
