package com.pixulse.infx.item;

import com.pixulse.infx.registry.ModItems;
import com.pixulse.infx.entity.R196Livestock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.Animal;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Server-authoritative livestock manure cycles from R196. */
public final class R196ManureEvents {
    private static final String NEXT_DROP_TAG = "infx_manure_next_drop";

    private R196ManureEvents() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(R196ManureEvents::onEntityTick);
    }

    private static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Animal animal) || !(entity.level() instanceof ServerLevel level)) {
            return;
        }
        int interval = interval(animal.getType());
        if (interval <= 0) {
            return;
        }

        long now = level.getGameTime();
        long next = animal.getPersistentData().getLong(NEXT_DROP_TAG).orElse(0L);
        if (next == 0L) {
            animal.getPersistentData().putLong(NEXT_DROP_TAG, now + interval);
        } else if (now >= next && R196Livestock.isProductive(animal)) {
            animal.spawnAtLocation(level, ModItems.catalog().raw("manure").holder());
            animal.getPersistentData().putLong(NEXT_DROP_TAG, now + interval);
        }
    }

    static int interval(EntityType<?> type) {
        if (type == EntityTypes.COW || type == EntityTypes.MOOSHROOM) {
            return 24_000;
        }
        if (type == EntityTypes.PIG || type == EntityTypes.SHEEP) {
            return 48_000;
        }
        if (type == EntityTypes.CHICKEN) {
            return 384_000;
        }
        return 0;
    }
}
