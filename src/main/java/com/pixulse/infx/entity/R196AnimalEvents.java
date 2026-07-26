package com.pixulse.infx.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
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

        if (event.getEntity() instanceof Animal animal
                && R196Livestock.hasSickSkin(animal)
                && !R196Livestock.isWell(animal)) {
            // MITE's unwell state removes only the meat yield. Leather, wool and feathers remain.
            event.getDrops().removeIf(drop -> isLivestockMeat(animal, drop.getItem()));
        }

        if (event.getEntity() instanceof IronGolem golem) {
            event.getDrops().removeIf(drop -> drop.getItem().is(Items.IRON_INGOT));
            event.getDrops()
                    .add(drop(level, golem, new ItemStack(Items.IRON_NUGGET, 2 + golem.getRandom().nextInt(4))));
        }
    }

    private static boolean isLivestockMeat(Animal animal, ItemStack stack) {
        if (animal instanceof R196Cow) {
            return stack.is(Items.BEEF) || stack.is(Items.COOKED_BEEF);
        }
        if (animal instanceof R196Chicken) {
            return stack.is(Items.CHICKEN) || stack.is(Items.COOKED_CHICKEN);
        }
        if (animal instanceof R196Pig) {
            return stack.is(Items.PORKCHOP) || stack.is(Items.COOKED_PORKCHOP);
        }
        if (animal instanceof R196Sheep) {
            return stack.is(Items.MUTTON) || stack.is(Items.COOKED_MUTTON);
        }
        return false;
    }

    private static ItemEntity drop(ServerLevel level, Entity source, ItemStack stack) {
        return new ItemEntity(level, source.getX(), source.getY(), source.getZ(), stack);
    }
}
