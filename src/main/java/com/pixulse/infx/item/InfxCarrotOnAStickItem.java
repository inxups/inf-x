package com.pixulse.infx.item;

import com.pixulse.infx.entity.InfxPig;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FoodOnAStickItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * MITE carrot on a stick: each hook material has its own stick that boosts the ridden pig.
 * The INFX pig replacement uses its own entity type, so the vanilla type check is widened.
 */
public final class InfxCarrotOnAStickItem extends FoodOnAStickItem<Pig> {
    private static final int PIG_BOOST_DAMAGE = 7;

    public InfxCarrotOnAStickItem(Properties properties) {
        super(EntityType.PIG, PIG_BOOST_DAMAGE, properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }
        Entity vehicle = player.getControlledVehicle();
        if (player.isPassenger()
                && vehicle instanceof ItemSteerable steerable
                && (vehicle.is(EntityType.PIG) || vehicle instanceof InfxPig)
                && steerable.boost()) {
            ItemStack stack = player.getItemInHand(hand);
            return InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(
                    stack.hurtAndConvertOnBreak(PIG_BOOST_DAMAGE, this, player, hand.asEquipmentSlot()));
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.PASS;
    }
}
