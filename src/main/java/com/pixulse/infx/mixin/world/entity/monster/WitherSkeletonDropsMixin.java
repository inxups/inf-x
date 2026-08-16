package com.pixulse.infx.mixin.world.entity.monster;

import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.equipment.QualitySystem;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.item.material.Quality;
import com.pixulse.infx.registry.InfXItems;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * InfX wither skeletons swap the vanilla stone sword for their InfX iron sword as a worn
 * (poor quality) weapon: the quality component and the reduced 0.75x durability cap are
 * applied at spawn. The sword follows the vanilla 26.1.2 equipment-drop rule: an 8.5%
 * chance on a player kill, dropped with heavy random damage.
 */
@Mixin(WitherSkeleton.class)
public abstract class WitherSkeletonDropsMixin {
    @Inject(method = "finalizeSpawn", at = @At("TAIL"))
    private void infx$giveIronSword(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason spawnReason,
            @Nullable SpawnGroupData groupData,
            CallbackInfoReturnable<SpawnGroupData> callback) {
        ItemStack weapon = InfXItems.catalog().equipment(InfxMaterial.IRON, EquipmentType.SWORD).holder().toStack();
        QualitySystem.applySelectedQuality(weapon, QualitySystem.toCode(Quality.POOR));
        ((LivingEntity) (Object) this).setItemSlot(EquipmentSlot.MAINHAND, weapon);
    }
}
