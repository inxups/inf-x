package com.pixulse.infx.entity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;

/**
 * Residual global animal hooks that are not owned by INFX replacement entity classes
 * (e.g. iron golem drop rewrite and livestock panic after a completed hit).
 */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class AnimalEvents {
    private AnimalEvents() {}

    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        if (event.getEntity() instanceof Animal) {
            event.setDroppedExperience(0);
        }
    }

    @SubscribeEvent
    public static void onDamaged(LivingDamageEvent.Post event) {
        if (event.getEntity() instanceof Animal animal) {
            Livestock.onHurt(animal, event.getSource(), event.getOriginalDamage());
        }
    }

    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;

        if (event.getEntity() instanceof Animal animal
                && Livestock.hasSickSkin(animal)
                && !Livestock.isWell(animal)) {
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
        if (animal instanceof InfxCow) {
            return stack.is(Items.BEEF) || stack.is(Items.COOKED_BEEF);
        }
        if (animal instanceof InfxChicken) {
            return stack.is(Items.CHICKEN) || stack.is(Items.COOKED_CHICKEN);
        }
        if (animal instanceof InfxPig) {
            return stack.is(Items.PORKCHOP) || stack.is(Items.COOKED_PORKCHOP);
        }
        if (animal instanceof InfxSheep) {
            return stack.is(Items.MUTTON) || stack.is(Items.COOKED_MUTTON);
        }
        return false;
    }

    private static ItemEntity drop(ServerLevel level, Entity source, ItemStack stack) {
        return new ItemEntity(level, source.getX(), source.getY(), source.getZ(), stack);
    }
}
