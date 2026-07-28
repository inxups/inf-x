package com.pixulse.infx.item;

import com.pixulse.infx.item.material.RawItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public final class CoinItem extends Item {
    private final RawItem definition;

    public CoinItem(RawItem definition, Properties properties) {
        super(properties);
        this.definition = definition;
    }

    public int experienceValue() {
        return definition.coinXp();
    }

    @Override
    public @NonNull InteractionResult use(Level level, Player player, @NonNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            stack.shrink(1);
            player.giveExperiencePoints(experienceValue());
        }
        return InteractionResult.SUCCESS;
    }
}
