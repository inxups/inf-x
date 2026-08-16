package com.pixulse.infx.mixin.world.entity.monster;

import com.pixulse.infx.entity.InfxSkeleton;
import com.pixulse.infx.entity.MonsterTactics;
import com.pixulse.infx.registry.InfXItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.skeleton.Bogged;
import net.minecraft.world.entity.monster.skeleton.Parched;
import net.minecraft.world.entity.monster.skeleton.Stray;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * InfX removes the vanilla spawn equipment of every zombie and skeleton family member that has
 * no INFX replacement variant (husks, drowned, zombie villagers, strays, bogged, parched and
 * wither skeletons keep the vanilla types). The INFX replacements override the same method
 * themselves, so this cancel only affects those vanilla entities.
 * <p>
 * Strays, bogged and parched share the ordinary InfX skeleton's current-day bow-or-melee spawn
 * profile. Wither skeletons retain their special InfX iron sword; zombies and drowned stay bare.
 */
@Mixin({Zombie.class, Drowned.class, AbstractSkeleton.class, WitherSkeleton.class})
abstract class VanillaMobEquipmentMixin {
    @Inject(method = "populateDefaultEquipmentSlots", at = @At("HEAD"), cancellable = true)
    private void infx$noVanillaSpawnEquipment(
            RandomSource random, DifficultyInstance difficulty, CallbackInfo callback) {
        Mob self = (Mob) (Object) this;
        if (self instanceof Stray || self instanceof Bogged || self instanceof Parched) {
            float tension = self.level() instanceof ServerLevel level
                    ? MonsterTactics.difficultyTension(level, self.blockPosition())
                    : 0.0F;
            InfxSkeleton.OrdinarySkeletonWeapon weapon =
                    InfxSkeleton.ordinarySpawnWeapon(random.nextFloat(), tension);
            self.setItemSlot(
                    EquipmentSlot.MAINHAND,
                    InfXItems.catalog()
                            .equipment(weapon.material(), weapon.type())
                            .holder()
                            .toStack());
        }
        callback.cancel();
    }
}
