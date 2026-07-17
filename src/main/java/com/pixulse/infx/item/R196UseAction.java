package com.pixulse.infx.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.common.ItemAbility;

public enum R196UseAction {
    NONE,
    AXE,
    SHOVEL,
    HOE,
    MATTOCK;

    public InteractionResult useOn(UseOnContext context) {
        return switch (this) {
            case NONE -> InteractionResult.PASS;
            case AXE -> Items.IRON_AXE.useOn(context);
            case SHOVEL -> Items.IRON_SHOVEL.useOn(context);
            case HOE -> Items.IRON_HOE.useOn(context);
            case MATTOCK -> {
                InteractionResult shovel = Items.IRON_SHOVEL.useOn(context);
                yield shovel == InteractionResult.PASS ? Items.IRON_HOE.useOn(context) : shovel;
            }
        };
    }

    public boolean canPerformAction(ItemInstance stack, ItemAbility ability) {
        return switch (this) {
            case NONE -> false;
            case AXE -> Items.IRON_AXE.canPerformAction(stack, ability);
            case SHOVEL -> Items.IRON_SHOVEL.canPerformAction(stack, ability);
            case HOE -> Items.IRON_HOE.canPerformAction(stack, ability);
            case MATTOCK -> Items.IRON_SHOVEL.canPerformAction(stack, ability)
                    || Items.IRON_HOE.canPerformAction(stack, ability);
        };
    }
}
