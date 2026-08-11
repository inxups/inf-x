package com.pixulse.infx.mixin.world.item.component;

import com.mojang.datafixers.util.Either;
import com.pixulse.infx.item.ItemReach;
import java.util.Collection;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.PiercingWeapon;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Keeps piercing attacks on the same height-aware melee rule as ordinary player attacks. */
@Mixin(PiercingWeapon.class)
public abstract class PiercingWeaponReachMixin {
    @Redirect(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getHitEntitiesAlong("
                            + "Lnet/minecraft/world/entity/Entity;"
                            + "Lnet/minecraft/world/item/component/AttackRange;"
                            + "Ljava/util/function/Predicate;"
                            + "Lnet/minecraft/world/level/ClipContext$Block;)"
                            + "Lcom/mojang/datafixers/util/Either;"))
    private Either<BlockHitResult, Collection<EntityHitResult>> getHitEntitiesAlong(
            Entity attacker,
            AttackRange attackRange,
            Predicate<Entity> matching,
            ClipContext.Block blockClipType) {
        return ItemReach.getMeleeHitEntitiesAlong(attacker, attackRange, matching, blockClipType);
    }
}
