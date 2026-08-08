package com.pixulse.infx.mixin.world.entity.projectile;

import com.pixulse.infx.item.enchantment.EnchantmentRules;
import com.pixulse.infx.item.enchantment.Enchantments;
import com.pixulse.infx.registry.InfXEnchantments;
import com.pixulse.infx.world.FishingRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * FishingHook exposes no event before its lure timer is chosen. INFX baiting changes that timer
 * by 10% per level and the InfX bite timing scales it with the time of day, rain and worm bait,
 * so this scoped redirect changes only the lure-delay random roll.
 */
@Mixin(FishingHook.class)
abstract class FishingHookMixin {
    @Redirect(
            method = "catchingFish",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Mth;nextInt(Lnet/minecraft/util/RandomSource;II)I",
                    ordinal = 2))
    private int infx$LureDelay(RandomSource random, int minimum, int maximum) {
        int delay = Mth.nextInt(random, minimum, maximum);
        FishingHook hook = (FishingHook) (Object) this;
        Player player = hook.getPlayerOwner();
        if (player == null) return delay;
        if (player.level() instanceof ServerLevel level) {
            delay = FishingRules.lureDelay(level, player, delay);
        }
        int baiting = Enchantments.level(player.level(), player.getMainHandItem(), InfXEnchantments.BAITING);
        if (baiting == 0) {
            baiting = Enchantments.level(player.level(), player.getOffhandItem(), InfXEnchantments.BAITING);
        }
        return EnchantmentRules.baitingLureDelay(delay, baiting);
    }
}
