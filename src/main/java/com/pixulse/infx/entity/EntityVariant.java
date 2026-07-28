package com.pixulse.infx.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

final class EntityVariant {
    private EntityVariant() {}

    static String path(Entity entity) {
        return EntityType.getKey(entity.getType()).getPath();
    }
}
