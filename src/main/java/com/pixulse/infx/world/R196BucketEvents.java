package com.pixulse.infx.world;

import com.pixulse.infx.item.R196BucketItem;
import com.pixulse.infx.item.R196MobBucketItem;
import com.pixulse.infx.item.R196MobBucketKind;
import com.pixulse.infx.item.R196SolidBucketItem;
import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Dispenser behavior, MITE source decay and melt pickup suppression for R196 buckets. */
public final class R196BucketEvents {
    private R196BucketEvents() {}

    public static void register(IEventBus modBus, IEventBus gameBus) {
        modBus.addListener(R196BucketEvents::commonSetup);
        gameBus.addListener(R196BucketEvents::tickFluidDecay);
        gameBus.addListener(R196BucketEvents::suppressMeltPickup);
    }

    /** Runs the MITE scheduleBlockChange queue that degrades unpaid source cells to flowing. */
    private static void tickFluidDecay(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            R196FluidDecayData.get(level).tick(level);
        }
    }

    /**
     * MITE prevent_item_pickup_due_to_held_item_breaking_until: after a bucket melts, the now-empty
     * hand must not immediately scoop up a nearby drop.
     */
    private static void suppressMeltPickup(ItemEntityPickupEvent.Pre event) {
        Player player = event.getPlayer();
        long until = player.getPersistentData().getLong(R196BucketItem.MELT_PICKUP_BLOCK).orElse(0L);
        if (until <= 0L) {
            return;
        }
        if (player.level().getGameTime() < until) {
            event.setCanPickup(TriState.FALSE);
        } else {
            player.getPersistentData().remove(R196BucketItem.MELT_PICKUP_BLOCK);
        }
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            for (R196Material material : ModItems.BUCKET_MATERIALS) {
                registerEmpty(material);
                registerFilled(material, R196BucketItem.Contents.WATER);
                registerFilled(material, R196BucketItem.Contents.LAVA);
                registerPowderSnow(material);
                for (R196MobBucketKind kind : R196MobBucketKind.values()) {
                    registerMob(material, kind);
                }
            }
        });
    }

    private static void registerFilled(R196Material material, R196BucketItem.Contents contents) {
        Item filled = ModItems.bucket(material, contents).value();
        Item empty = ModItems.bucket(material, R196BucketItem.Contents.EMPTY).value();
        DispenserBlock.registerBehavior(filled, filledBehavior(empty));
    }

    private static void registerPowderSnow(R196Material material) {
        R196SolidBucketItem filled = ModItems.powderSnowBucket(material).value();
        DispenserBlock.registerBehavior(filled, filledBehavior(filled.emptyBucket()));
    }

    private static void registerMob(R196Material material, R196MobBucketKind kind) {
        R196MobBucketItem filled = ModItems.mobBucket(material, kind).value();
        DispenserBlock.registerBehavior(filled, filledBehavior(filled.emptyBucket()));
    }

    private static DefaultDispenseItemBehavior filledBehavior(Item empty) {
        return new DefaultDispenseItemBehavior() {
            private final DefaultDispenseItemBehavior fallback = new DefaultDispenseItemBehavior();

            @Override
            public ItemStack execute(net.minecraft.core.dispenser.BlockSource source, ItemStack dispensed) {
                DispensibleContainerItem container = (DispensibleContainerItem) dispensed.getItem();
                BlockPos target = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
                Level level = source.level();
                if (!container.emptyContents(null, level, target, null, dispensed)) {
                    return fallback.dispense(source, dispensed);
                }
                container.checkExtraContent(null, level, dispensed, target);
                return consumeWithRemainder(source, dispensed, new ItemStack(empty));
            }
        };
    }

    private static void registerEmpty(R196Material material) {
        Item empty = ModItems.bucket(material, R196BucketItem.Contents.EMPTY).value();
        DispenserBlock.registerBehavior(empty, new DefaultDispenseItemBehavior() {
            @Override
            public ItemStack execute(net.minecraft.core.dispenser.BlockSource source, ItemStack dispensed) {
                LevelAccessor level = source.level();
                BlockPos target = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
                BlockState state = level.getBlockState(target);
                if (!(state.getBlock() instanceof BucketPickup pickup)) return super.execute(source, dispensed);
                ItemStack filledStack;
                if (state.getFluidState().is(net.minecraft.world.level.material.Fluids.WATER)) {
                    filledStack = ModItems.bucket(material, R196BucketItem.Contents.WATER).toStack();
                } else if (state.getFluidState().is(net.minecraft.world.level.material.Fluids.LAVA)) {
                    filledStack = ModItems.bucket(material, R196BucketItem.Contents.LAVA).toStack();
                } else if (state.is(Blocks.POWDER_SNOW)) {
                    filledStack = ModItems.powderSnowBucket(material).toStack();
                } else {
                    return super.execute(source, dispensed);
                }
                ItemStack vanilla = pickup.pickupBlock(null, level, target, state);
                if (vanilla.isEmpty()) return super.execute(source, dispensed);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, target);
                if (state.getFluidState().is(net.minecraft.world.level.material.Fluids.LAVA)
                        && source.level().getRandom().nextFloat() < R196BucketItem.lavaMeltChance(material)) {
                    dispensed.shrink(1);
                    source.level().playSound(
                            null, target, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 0.7F);
                    return dispensed;
                }
                return consumeWithRemainder(source, dispensed, filledStack);
            }
        });
    }
}
