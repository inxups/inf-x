package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.ItemReach;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Player reach attributes owned entirely by INFX. */
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
        modBus.addListener(InfXAttributes::addPlayerAttributes);
    }

    private static void addPlayerAttributes(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, ITEM_INTERACTION_RANGE);
        event.add(EntityType.PLAYER, ITEM_MELEE_RANGE);
    }
}
