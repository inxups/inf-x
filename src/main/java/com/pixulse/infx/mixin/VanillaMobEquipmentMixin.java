package com.pixulse.infx.mixin;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MITE removes the vanilla spawn equipment of every zombie and skeleton family member that has
 * no INFX replacement variant (husks, drowned, zombie villagers, strays and wither skeletons
 * keep the vanilla types). The INFX replacements override the same method themselves, so this
 * cancel only affects those vanilla entities.
 */
@Mixin({Zombie.class, Drowned.class, AbstractSkeleton.class, WitherSkeleton.class})
abstract class VanillaMobEquipmentMixin {
    @Inject(method = "populateDefaultEquipmentSlots", at = @At("HEAD"), cancellable = true)
    private void infx$noVanillaSpawnEquipment(
            RandomSource random, DifficultyInstance difficulty, CallbackInfo callback) {
        callback.cancel();
    }
}
