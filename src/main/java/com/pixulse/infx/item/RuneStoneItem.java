package com.pixulse.infx.item;

import com.pixulse.infx.block.RuneStoneBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/** A rune-stone item whose selected R196 rune is stored as block-state item data. */
public final class RuneStoneItem extends BlockItem {
    public RuneStoneItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack)
                .copy()
                .append(" \"")
                .append(RuneStoneBlock.magicName(RuneStoneBlock.rune(stack)))
                .append("\"");
    }
}
