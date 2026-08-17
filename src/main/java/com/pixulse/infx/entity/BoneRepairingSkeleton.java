package com.pixulse.infx.entity;

import net.minecraft.world.item.ItemStack;

/** Shared MITE bone-repair contract for skeleton-family replacements. */
public interface BoneRepairingSkeleton {
    boolean canRepairFromBone();

    boolean tryRepairFromBone(ItemStack stack);
}
