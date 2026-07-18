package com.pixulse.infx.item;

import com.pixulse.infx.material.R196RawItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class R196CoinItem extends Item {
    private final R196RawItem definition;

    public R196CoinItem(R196RawItem definition, Properties properties) {
        super(properties);
        this.definition = definition;
    }

    public int experienceValue() {
        return definition.coinXp();
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            stack.shrink(1);
            player.giveExperiencePoints(experienceValue());
        }
        return InteractionResult.SUCCESS;
    }
}
