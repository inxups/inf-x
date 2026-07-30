package com.pixulse.infx.item;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;

import com.pixulse.infx.registry.InfXItems;
import com.pixulse.infx.entity.Livestock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Server-authoritative livestock manure cycles from INFX. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class ManureEvents {
    private static final String COUNTDOWN_TAG = "infx_manure_countdown";

    private ManureEvents() {}

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Animal animal)
                || !(entity.level() instanceof ServerLevel level)
                || !Livestock.hasSickSkin(animal)) {
            return;
        }
        int interval = interval(animal);
        if (interval <= 0) {
            return;
        }

        var data = animal.getPersistentData();
        int countdown = data.getInt(COUNTDOWN_TAG).orElse(animal.getRandom().nextInt(interval));
        if (!animal.isBaby() && !Livestock.isDesperateForFood(animal) && --countdown <= 0) {
            animal.spawnAtLocation(level, InfXItems.catalog().raw("manure").holder());
            countdown = interval / 2 + animal.getRandom().nextInt(interval);
        }
        data.putInt(COUNTDOWN_TAG, countdown);
    }

    /** Prefer class checks so INFX replacements (subclasses) share MITE manure periods. */
    static int interval(Entity entity) {
        if (entity instanceof AbstractCow) {
            return 24_000;
        }
        if (entity instanceof Pig || entity instanceof Sheep) {
            return 48_000;
        }
        if (entity instanceof Chicken) {
            return 384_000;
        }
        return 0;
    }

    /** Test helper matching vanilla entity types used by unit tests. */
    static int interval(EntityType<?> type) {
        if (type == EntityType.COW || type == EntityType.MOOSHROOM) {
            return 24_000;
        }
        if (type == EntityType.PIG || type == EntityType.SHEEP) {
            return 48_000;
        }
        if (type == EntityType.CHICKEN) {
            return 384_000;
        }
        return 0;
    }
}
