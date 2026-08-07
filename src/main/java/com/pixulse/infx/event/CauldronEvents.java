package com.pixulse.infx.event;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.InfxBucketItem;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.registry.InfXItems;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.event.RegisterCauldronInteractionEvent;

/**
 * InfX BlockCauldron#onBlockActivated ItemVessel branch for INFX metal buckets and bowls. An empty
 * bucket takes a full water cauldron (vessel volume 4 capped to 3) and a water bucket pours back
 * three levels; bowls transfer a single level.
 */
public final class CauldronEvents {
    private static final Identifier WATER = Identifier.withDefaultNamespace("water");
    private static final Identifier EMPTY = Identifier.withDefaultNamespace("empty");

    private CauldronEvents() {}

    public static void registerCauldronInteractions(RegisterCauldronInteractionEvent.Interaction event) {
        for (InfxMaterial material : InfXItems.BUCKET_MATERIALS) {
            Item emptyBucket = InfXItems.bucket(material, InfxBucketItem.Contents.EMPTY).value();
            Item waterBucket = InfXItems.bucket(material, InfxBucketItem.Contents.WATER).value();
            event.register(WATER, emptyBucket, CauldronEvents::fillBucket);
            event.register(WATER, waterBucket, CauldronEvents::emptyWaterBucket);
            event.register(EMPTY, waterBucket, CauldronEvents::emptyWaterBucket);
        }
        event.register(WATER, Items.BOWL, CauldronEvents::fillBowl);
        event.register(WATER, InfXItems.WATER_BOWL.value(), CauldronEvents::emptyBowl);
        event.register(EMPTY, InfXItems.WATER_BOWL.value(), CauldronEvents::emptyBowl);
    }

    /** InfX: empty vessel with volume >= 1 takes water from a cauldron of the same volume. */
    private static InteractionResult fillBucket(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            ItemStack itemInHand) {
        if (waterLevel(state) != 3) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!level.isClientSide()) {
            Item usedItem = itemInHand.getItem();
            Item waterBucket = InfXItems.bucket(
                            ((InfxBucketItem) usedItem).material(), InfxBucketItem.Contents.WATER)
                    .value();
            player.setItemInHand(hand, ItemUtils.createFilledResult(itemInHand, player, new ItemStack(waterBucket)));
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(usedItem));
            level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
            level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
        }
        return InteractionResult.SUCCESS;
    }

    /** InfX: a full vessel empties into a cauldron below capacity, clamping at three levels. */
    private static InteractionResult emptyWaterBucket(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            ItemStack itemInHand) {
        if (waterLevel(state) == 3) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        return emptyVessel(
                level,
                pos,
                player,
                hand,
                itemInHand,
                Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3),
                SoundEvents.BUCKET_EMPTY);
    }

    private static InteractionResult fillBowl(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            ItemStack itemInHand) {
        if (waterLevel(state) < 1) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!level.isClientSide()) {
            Item usedItem = itemInHand.getItem();
            player.setItemInHand(
                    hand, ItemUtils.createFilledResult(itemInHand, player, new ItemStack(InfXItems.WATER_BOWL.value())));
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(usedItem));
            LayeredCauldronBlock.lowerFillLevel(state, level, pos);
            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
        }
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult emptyBowl(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            ItemStack itemInHand) {
        if (waterLevel(state) == 3) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        return emptyVessel(
                level,
                pos,
                player,
                hand,
                itemInHand,
                state.is(Blocks.CAULDRON)
                        ? Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 1)
                        : state.cycle(LayeredCauldronBlock.LEVEL),
                SoundEvents.BOTTLE_EMPTY);
    }

    /** Water level of a cauldron state; an empty cauldron has none and reads as zero. */
    private static int waterLevel(BlockState state) {
        return state.hasProperty(LayeredCauldronBlock.LEVEL)
                ? state.getValue(LayeredCauldronBlock.LEVEL)
                : 0;
    }

    private static InteractionResult emptyVessel(
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            ItemStack itemInHand,
            BlockState newState,
            SoundEvent soundEvent) {
        if (!level.isClientSide()) {
            Item usedItem = itemInHand.getItem();
            Item empty = usedItem instanceof InfxBucketItem bucket
                    ? bucket.emptyBucket()
                    : Items.BOWL;
            player.setItemInHand(hand, ItemUtils.createFilledResult(itemInHand, player, new ItemStack(empty)));
            player.awardStat(Stats.FILL_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(usedItem));
            level.setBlockAndUpdate(pos, newState);
            level.playSound(null, pos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
        }
        return InteractionResult.SUCCESS;
    }
}
