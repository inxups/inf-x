package com.pixulse.infx.mixin;

import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

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

}
