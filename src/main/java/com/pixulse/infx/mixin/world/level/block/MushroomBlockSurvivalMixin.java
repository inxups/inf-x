package com.pixulse.infx.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MITE-aligned brown mushroom placement gate. Vanilla lets mushrooms survive on any
 * {@code #minecraft:overrides_mushroom_light_requirement} block (dirt/grass/mycelium/…) in
 * any light and rejects farmland outright; MITE instead requires stone/gravel/dirt/
 * farmland/mycelium below, a covered (indoor) position and light ≤ 13. Injecting
 * {@link MushroomBlock#canSurvive} moves the gate before placement (vanilla
 * {@code BlockItem.canPlace} checks {@code canSurvive}), so compliant spots plant cleanly
 * and non-compliant ones fail without the ghost-block-then-revert flicker. Red mushrooms and
 * the nether fungi (which extend MushroomBlock) fall through to vanilla.
 */
@Mixin(MushroomBlock.class)
public abstract class MushroomBlockSurvivalMixin {
    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void infx$miteBrownMushroomSurvival(
            BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> callback) {
        if (!state.is(Blocks.BROWN_MUSHROOM)) {
            return;
        }
        BlockState below = level.getBlockState(pos.below());
        boolean legalSoil = below.is(Blocks.STONE)
                || below.is(Blocks.GRAVEL)
                || below.is(Blocks.DIRT)
                || below.is(Blocks.MYCELIUM)
                || below.getBlock() instanceof FarmlandBlock;
        if (!legalSoil) {
            callback.setReturnValue(false);
            return;
        }
        boolean indoor = level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()) > pos.getY();
        if (!indoor) {
            callback.setReturnValue(false);
            return;
        }
        if (level.getRawBrightness(pos, 0) > 13) {
            callback.setReturnValue(false);
            return;
        }
        callback.setReturnValue(true);
    }
}
