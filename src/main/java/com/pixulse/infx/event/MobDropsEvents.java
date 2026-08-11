package com.pixulse.infx.event;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.material.Quality;
import com.pixulse.infx.registry.InfXDataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/**
 * InfX wither skeletons always drop their worn InfX iron sword. 26.1.2 removed the
 * vanilla equipment-drop mechanic, so the drop is added through the public drop event.
 */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class MobDropsEvents {
    private MobDropsEvents() {}

    @SubscribeEvent
    public static void dropWornWitherSkeletonSword(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof WitherSkeleton witherSkeleton)) {
            return;
        }
        ItemStack weapon = witherSkeleton.getMainHandItem();
        if (weapon.isEmpty()) {
            return;
        }
        weapon.set(InfXDataComponents.QUALITY.get(), Quality.POOR);
        event.getDrops().add(new ItemEntity(
                witherSkeleton.level(),
                witherSkeleton.getX(),
                witherSkeleton.getY(),
                witherSkeleton.getZ(),
                weapon));
        witherSkeleton.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
    }
}
