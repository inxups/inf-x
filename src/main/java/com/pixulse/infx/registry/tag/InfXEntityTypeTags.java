package com.pixulse.infx.registry.tag;

import com.pixulse.infx.InfiniteX;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class InfXEntityTypeTags {
    /**
     * Entity types a third-party mod can add here to stop InfX from replacing the vanilla entity
     * with an {@code INFX_*} class at spawn time. Empty by default; fill from
     * {@code data/infx/tags/entity_type/keep_vanilla_entity.json} with {@code "replace": false}.
     */
    public static final TagKey<EntityType<?>> KEEP_VANILLA_ENTITY = create("keep_vanilla_entity");

    private InfXEntityTypeTags() {
    }

    private static TagKey<EntityType<?>> create(String path) {
        return TagKey.create(Registries.ENTITY_TYPE, InfiniteX.id(path));
    }
}
