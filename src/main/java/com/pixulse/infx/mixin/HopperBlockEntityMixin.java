package com.pixulse.infx.mixin;

import com.pixulse.infx.furnace.FurnaceHeatAccess;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Pops accumulated recipe XP only after a hopper successfully extracts a furnace output. */
@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin {
    @Inject(method = "tryTakeInItemFromSlot", at = @At("RETURN"))
    private static void infx$popFurnaceExperience(
            Hopper hopper,
            Container container,
            int slot,
            Direction direction,
            CallbackInfoReturnable<Boolean> callback) {
        if (callback.getReturnValue()
                && slot == 2
                && container instanceof AbstractFurnaceBlockEntity furnace
                && furnace.getLevel() instanceof ServerLevel level
                && furnace instanceof FurnaceHeatAccess access) {
            access.infx$popAutomationExperience(level);
        }
    }
}
