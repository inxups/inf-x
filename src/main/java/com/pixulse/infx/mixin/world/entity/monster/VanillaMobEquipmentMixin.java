package com.pixulse.infx.mixin.world.entity.monster;

import com.pixulse.infx.entity.InfxSkeleton;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.registry.InfXItems;
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
 * Strays, bogged and parched share the ordinary InfX skeleton's wooden bow-or-club spawn split.
 * Wither skeletons retain their special InfX iron sword; zombies and drowned stay bare.
 */
@Mixin({Zombie.class, Drowned.class, AbstractSkeleton.class, WitherSkeleton.class})
abstract class VanillaMobEquipmentMixin {
    @Inject(method = "populateDefaultEquipmentSlots", at = @At("HEAD"), cancellable = true)
    private void infx$noVanillaSpawnEquipment(
            RandomSource random, DifficultyInstance difficulty, CallbackInfo callback) {
        Mob self = (Mob) (Object) this;
        if (self instanceof Stray || self instanceof Bogged || self instanceof Parched) {
            self.setItemSlot(
                    EquipmentSlot.MAINHAND,
                    InfXItems.catalog()
                            .equipment(InfxMaterial.WOOD, InfxSkeleton.ordinarySpawnWeapon(random.nextFloat()))
                            .holder()
                            .toStack());
        } else if (self instanceof WitherSkeleton) {
            self.setItemSlot(
                    EquipmentSlot.MAINHAND,
                    InfXItems.catalog().equipment(InfxMaterial.IRON, EquipmentType.SWORD).holder().toStack());
        }
        callback.cancel();
    }
}
