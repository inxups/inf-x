package com.pixulse.infx.mixin;

import com.pixulse.infx.harvest.HarvestEvents;
import com.pixulse.infx.harvest.MiningInputRules;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Routes an unmineable block through the same attack branch as a left click on air. */
@Mixin(Minecraft.class)
public abstract class MinecraftMiningInputMixin {
    @Redirect(
            method = {"startAttack", "continueAttack"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/HitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;"))
    private HitResult.Type infx$invalidMiningTargetActsAsMiss(HitResult hitResult) {
        HitResult.Type original = hitResult.getType();
        if (original != HitResult.Type.BLOCK || !(hitResult instanceof BlockHitResult blockHit)) {
            return original;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return original;
        }
        var pos = blockHit.getBlockPos();
        var state = minecraft.level.getBlockState(pos);
        return MiningInputRules.attackTargetType(
                original,
                HarvestEvents.hasDestroyProgress(minecraft.player, state, pos));
    }
}
