package com.pixulse.infx.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.pixulse.infx.world.UnderworldPortalRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Allows fire to run vanilla portal detection in the custom Underworld dimension. */
@Mixin(BaseFireBlock.class)
public abstract class BaseFireBlockMixin {
    @ModifyReturnValue(method = "inPortalDimension", at = @At("RETURN"))
    private static boolean infx$allowUnderworldPortalIgnition(boolean vanillaAllows, Level level) {
        return UnderworldPortalRules.allowsFirePortalIgnition(vanillaAllows, level.dimension());
    }
}
