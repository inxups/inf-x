package com.pixulse.infx.mixin;

import net.minecraft.world.level.block.CakeBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** CakeBlock has no public hook for changing its seven-slice hard-coded boundary. */
@Mixin(CakeBlock.class)
abstract class CakeBlockMixin {
    @ModifyConstant(method = "eat", constant = @Constant(intValue = 6))
    private static int infx$sixServings(int vanillaLastBite) {
        return 5;
    }
}
