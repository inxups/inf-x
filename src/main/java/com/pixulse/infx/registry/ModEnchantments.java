package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

/** R196 enchantments needed by systems completed in this milestone. */
public final class ModEnchantments {
    public static final ResourceKey<Enchantment> CLUMSINESS = ResourceKey.create(
            Registries.ENCHANTMENT, InfiniteX.id("clumsiness"));

    private ModEnchantments() {}

    public static void bootstrap(BootstrapContext<Enchantment> context) {
        HolderGetter<Item> items = context.lookup(Registries.ITEM);
        Enchantment.Builder clumsiness = Enchantment.enchantment(
                Enchantment.definition(
                        items.getOrThrow(ItemTags.DURABILITY_ENCHANTABLE),
                        1,
                        1,
                        Enchantment.constantCost(25),
                        Enchantment.constantCost(50),
                        8,
                        EquipmentSlotGroup.ANY));
        context.register(CLUMSINESS, clumsiness.build(CLUMSINESS.identifier()));
    }
}
