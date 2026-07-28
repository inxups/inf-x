package com.pixulse.infx.mixin;

import com.pixulse.infx.item.enchantment.EnchantmentRules;
import com.pixulse.infx.item.enchantment.Enchantments;
import com.pixulse.infx.registry.ModEnchantments;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * FishingHook exposes no event before its lure timer is chosen. R196 baiting changes that timer
 * by 10% per level, so this scoped redirect changes only the lure-delay random roll.
 */
@Mixin(FishingHook.class)
abstract class FishingHookMixin {
    @Redirect(
            method = "catchingFish",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Mth;nextInt(Lnet/minecraft/util/RandomSource;II)I",
                    ordinal = 2))
    private int infx$reduceLureDelay(RandomSource random, int minimum, int maximum) {
        int delay = Mth.nextInt(random, minimum, maximum);
        FishingHook hook = (FishingHook) (Object) this;
        Player player = hook.getPlayerOwner();
        if (player == null) return delay;
        int baiting = Enchantments.level(player.level(), player.getMainHandItem(), ModEnchantments.BAITING);
        if (baiting == 0) {
            baiting = Enchantments.level(player.level(), player.getOffhandItem(), ModEnchantments.BAITING);
        }
        return EnchantmentRules.baitingLureDelay(delay, baiting);
    }
}
