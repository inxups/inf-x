package com.pixulse.infx.world;

import com.pixulse.infx.item.R196BucketItem;
import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/** Registers dispenser behavior for every material-preserving R196 bucket. */
public final class R196BucketEvents {
    private R196BucketEvents() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(R196BucketEvents::commonSetup);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            for (R196Material material : ModItems.BUCKET_MATERIALS) {
                registerEmpty(material);
                registerFilled(material, R196BucketItem.Contents.WATER);
                registerFilled(material, R196BucketItem.Contents.LAVA);
            }
        });
    }

    private static void registerFilled(R196Material material, R196BucketItem.Contents contents) {
        Item filled = ModItems.bucket(material, contents).value();
        Item empty = ModItems.bucket(material, R196BucketItem.Contents.EMPTY).value();
        DispenserBlock.registerBehavior(filled, new DefaultDispenseItemBehavior() {
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
        });
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
                R196BucketItem.Contents contents = state.getFluidState().is(net.minecraft.world.level.material.Fluids.WATER)
                        ? R196BucketItem.Contents.WATER
                        : state.getFluidState().is(net.minecraft.world.level.material.Fluids.LAVA)
                                ? R196BucketItem.Contents.LAVA
                                : null;
                if (contents == null) return super.execute(source, dispensed);
                ItemStack vanilla = pickup.pickupBlock(null, level, target, state);
                if (vanilla.isEmpty()) return super.execute(source, dispensed);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, target);
                if (contents == R196BucketItem.Contents.LAVA
                        && source.level().getRandom().nextFloat() < R196BucketItem.lavaMeltChance(material)) {
                    dispensed.shrink(1);
                    source.level().playSound(
                            null, target, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 0.7F);
                    return dispensed;
                }
                return consumeWithRemainder(
                        source,
                        dispensed,
                        ModItems.bucket(material, contents).toStack());
            }
        });
    }
}
