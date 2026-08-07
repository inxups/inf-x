package com.pixulse.infx.mixin;

import com.pixulse.infx.InfiniteXTestMode;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Vanilla only re-checks the attack range when the held item carries an ATTACK_RANGE component, so
 * empty-hand and component-less items attack as far as the entity interaction raycast (2.5 blocks).
 * Falls back to {@link net.minecraft.world.entity.LivingEntity#getAttackRangeWith} so INFX's 1.5-block
 * empty-hand attack reach is actually enforced, while right-click interaction keeps its 2.5-block reach.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftStartAttackMixin {
    @Redirect(
            method = "startAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private Object infx$fallbackAttackRange(ItemStack stack, DataComponentType<?> type) {
        Object value = stack.get(type);
        if (!InfiniteXTestMode.isEnabled()
                && type == DataComponents.ATTACK_RANGE
                && value == null
                && ((Minecraft) (Object) this).player != null) {
            return ((Minecraft) (Object) this).player.getAttackRangeWith(stack);
        }
        return value;
    }
}
