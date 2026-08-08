package com.pixulse.infx.item;

import com.pixulse.infx.InfiniteX;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.Repairable;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

/** INFX default-component overrides for vanilla equipment. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class InfXVanillaEquipmentComponents {
    /** InfX crossbow durability. */
    private static final int CROSSBOW_DURABILITY = 64;
    /**
     * InfX crossbow enchantability: on a full diamond enchanting table (100 power) a 30
     * enchantability reaches 53 effective power, i.e. a 5300-experience enchantment cost.
     */
    private static final int CROSSBOW_ENCHANTABILITY = 30;

    private InfXVanillaEquipmentComponents() {}

    @SubscribeEvent
    public static void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        // InfX crossbow: 64 durability instead of the modern 465, anvil-repairable with iron
        // nuggets, and the 30 enchantability that allows the 5300-xp top enchantment.
        event.modify(Items.CROSSBOW, (components, context, modifiedItem) -> {
            components.set(DataComponents.MAX_DAMAGE, CROSSBOW_DURABILITY);
            components.set(
                    DataComponents.REPAIRABLE,
                    new Repairable(HolderSet.direct(Items.IRON_NUGGET.builtInRegistryHolder())));
            components.set(DataComponents.ENCHANTABLE, new Enchantable(CROSSBOW_ENCHANTABILITY));
        });
    }
}
