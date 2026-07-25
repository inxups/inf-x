package com.pixulse.infx.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/**
 * Residual global animal hooks that are not owned by R196 replacement entity classes
 * (e.g. iron golem drop rewrite). Livestock behavior lives on {@code R196Cow} etc.
 */
public final class R196AnimalEvents {
    private R196AnimalEvents() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(R196AnimalEvents::onDrops);
    }

    private static void onDrops(LivingDropsEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
        if (event.getEntity() instanceof IronGolem golem) {
            event.getDrops().removeIf(drop -> drop.getItem().is(Items.IRON_INGOT));
            event.getDrops()
                    .add(drop(level, golem, new ItemStack(Items.IRON_NUGGET, 2 + golem.getRandom().nextInt(4))));
        }
    }

    private static ItemEntity drop(ServerLevel level, Entity source, ItemStack stack) {
        return new ItemEntity(level, source.getX(), source.getY(), source.getZ(), stack);
    }
}
