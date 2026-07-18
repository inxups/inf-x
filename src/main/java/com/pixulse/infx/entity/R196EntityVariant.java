package com.pixulse.infx.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

final class R196EntityVariant {
    private R196EntityVariant() {}

    static String path(Entity entity) {
        return EntityType.getKey(entity.getType()).getPath();
    }
}
