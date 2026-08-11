package com.pixulse.infx.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * InfX sweet berry bushes fruit at the same rate as the INFX blueberry bush: a 2.5% chance
 * per random tick and only at full sky brightness, replacing the vanilla 20%-chance-at-9+
 * brightness rule.
 */
@Mixin(SweetBerryBushBlock.class)
public abstract class SweetBerryBushBlockGrowthMixin {
    private static final float GROWTH_CHANCE = 0.025F;

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void infx$blueberryGrowthRate(
            BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo callback) {
        int age = state.getValue(SweetBerryBushBlock.AGE);
        if (age < SweetBerryBushBlock.MAX_AGE
                && level.getRawBrightness(pos.above(), 0) == 15
                && random.nextFloat() < GROWTH_CHANCE) {
            BlockState grown = state.setValue(SweetBerryBushBlock.AGE, age + 1);
            level.setBlock(pos, grown, 2);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(grown));
        }
        callback.cancel();
    }
}
