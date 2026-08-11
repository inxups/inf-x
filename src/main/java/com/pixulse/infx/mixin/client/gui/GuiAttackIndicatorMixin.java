package com.pixulse.infx.mixin.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Keeps the crosshair attack indicator on the same INFX melee predicate as actual attacks. */
@Mixin(Gui.class)
public abstract class GuiAttackIndicatorMixin {
    @Shadow @Final private Minecraft minecraft;

    @Redirect(
            method = "extractCrosshair",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private Object attackRange(ItemStack stack, DataComponentType<?> type) {
        if (type == DataComponents.ATTACK_RANGE && minecraft.player != null) {
            return minecraft.player.getAttackRangeWith(stack);
        }
        return stack.get(type);
    }
}
