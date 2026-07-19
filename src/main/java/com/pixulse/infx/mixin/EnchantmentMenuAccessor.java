package com.pixulse.infx.mixin;

import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EnchantmentMenu.class)
public interface EnchantmentMenuAccessor {
    @Accessor("enchantSlots")
    Container infx$enchantSlots();

    @Accessor("access")
    ContainerLevelAccess infx$access();

    @Accessor("random")
    RandomSource infx$random();

    @Accessor("enchantmentSeed")
    DataSlot infx$enchantmentSeed();

    @Invoker("getEnchantmentList")
    List<EnchantmentInstance> infx$getEnchantmentList(
            RegistryAccess access, ItemStack stack, int slot, int cost);
}
