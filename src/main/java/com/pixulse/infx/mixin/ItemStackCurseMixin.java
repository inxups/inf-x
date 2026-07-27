package com.pixulse.infx.mixin;

import com.pixulse.infx.curse.R196CurseManager;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** ItemStack exposes no event for changing durability input before Unbreaking is evaluated. */
@Mixin(ItemStack.class)
abstract class ItemStackCurseMixin {
    @ModifyVariable(
            method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0)
    private int infx$doubleDurabilityDamage(
            int amount,
            int originalAmount,
            ServerLevel level,
            @Nullable LivingEntity owner,
            Consumer<Item> onBreak) {
        return owner == null ? amount : R196CurseManager.durabilityDamage(amount, owner);
    }

    @ModifyVariable(
            method = "hurtWithoutBreaking(ILnet/minecraft/world/entity/player/Player;)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0)
    private int infx$doubleNonbreakingDurabilityDamage(int amount, int originalAmount, Player owner) {
        return R196CurseManager.durabilityDamage(amount, owner);
    }
}
