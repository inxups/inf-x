package com.pixulse.infx.mixin.world.entity.player;

import com.pixulse.infx.InfiniteXTestMode;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * InfX replaces the vanilla 4.5/3.0 interaction bases with 2.5 blocks each while the vanilla
 * player attribute builder is still mutable, so held-tool reach bonuses apply on top.
 */
@Mixin(Player.class)
public abstract class InfXPlayerAttributesMixin {
    @Inject(method = "createAttributes", at = @At("RETURN"))
    private static void createAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> callback) {
        if (InfiniteXTestMode.isEnabled()) return;
        callback.getReturnValue()
                .add(Attributes.BLOCK_INTERACTION_RANGE, 2.5)
                .add(Attributes.ENTITY_INTERACTION_RANGE, 2.5);
    }
}
