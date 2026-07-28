package com.pixulse.infx.mixin;

import com.pixulse.infx.data.curse.CurseManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** The travel input is the narrow point that preserves all vanilla movement integrations. */
@Mixin(LivingEntity.class)
abstract class LivingEntityCurseMixin {
    @ModifyVariable(method = "travel", at = @At("HEAD"), argsOnly = true)
    private Vec3 infx$applyEntanglement(Vec3 input) {
        return (Object) this instanceof Player player
                ? CurseManager.entangledInput(player, input)
                : input;
    }
}
