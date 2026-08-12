package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.entity.InfxSkeleton;
import com.pixulse.infx.item.ItemReach;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** INFX player reach attributes plus selected vanilla mob-profile overrides. */
public final class InfXAttributes {
    private static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, InfiniteX.MOD_ID);

    public static final DeferredHolder<Attribute, Attribute> ITEM_INTERACTION_RANGE = ATTRIBUTES.register(
            "item_interaction_range",
            () -> new RangedAttribute(
                            "attribute.name.infx.item_interaction_range",
                            ItemReach.BASE_RANGE,
                            0.0,
                            ItemReach.MAX_RANGE)
                    .setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> ITEM_MELEE_RANGE = ATTRIBUTES.register(
            "item_melee_range",
            () -> new RangedAttribute(
                            "attribute.name.infx.item_melee_range",
                            ItemReach.BASE_RANGE,
                            0.0,
                            ItemReach.MAX_RANGE)
                    .setSyncable(true));

    private InfXAttributes() {}

    public static void register(IEventBus modBus) {
        ATTRIBUTES.register(modBus);
        modBus.addListener(InfXAttributes::addEntityAttributes);
    }

    private static void addEntityAttributes(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, ITEM_INTERACTION_RANGE);
        event.add(EntityType.PLAYER, ITEM_MELEE_RANGE);
        event.add(EntityType.STRAY, Attributes.MAX_HEALTH, InfxSkeleton.ORDINARY_MAX_HEALTH);
        event.add(EntityType.BOGGED, Attributes.MAX_HEALTH, InfxSkeleton.ORDINARY_MAX_HEALTH);
        event.add(EntityType.PARCHED, Attributes.MAX_HEALTH, InfxSkeleton.ORDINARY_MAX_HEALTH);
    }
}
