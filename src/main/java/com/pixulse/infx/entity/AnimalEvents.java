package com.pixulse.infx.entity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.InfxBucketItem;
import com.pixulse.infx.registry.InfXItems;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.Donkey;
import net.minecraft.world.entity.animal.equine.Mule;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Residual global animal hooks that are not owned by INFX replacement entity classes
 * (e.g. iron golem drop rewrite and livestock panic after a completed hit).
 */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class AnimalEvents {
    private static final String GOAT_MILK_DAY = "infx_goat_milk_day";
    private static final String GOAT_MILK_UNITS = "infx_goat_milk_units";

    private AnimalEvents() {}

    /**
     * MITE goats share the cow daily milk quota: a metal empty bucket (or the vanilla bucket)
     * fills with milk once per day. Vanilla goats have no INFX wellness skin, so they are not
     * gated by livestock productivity.
     */
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        InteractionResult result = milkGoat(
                event.getEntity(), event.getHand(), event.getTarget(), event.getItemStack(), event.getLevel());
        if (result != null) {
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }

    /** The "interact at" path used by clients and GameTest helpers fires the specific event. */
    @SubscribeEvent
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        InteractionResult result = milkGoat(
                event.getEntity(), event.getHand(), event.getTarget(), event.getItemStack(), event.getLevel());
        if (result != null) {
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }

    private static @org.jspecify.annotations.Nullable InteractionResult milkGoat(
            net.minecraft.world.entity.player.Player player,
            net.minecraft.world.InteractionHand hand,
            net.minecraft.world.entity.Entity target,
            ItemStack stack,
            net.minecraft.world.level.Level level) {
        if (!(target instanceof Goat goat) || goat.isBaby()) return null;
        if (!(level instanceof ServerLevel serverLevel)) return null;

        ItemStack filled = ItemStack.EMPTY;
        int units = 0;
        if (stack.is(Items.BUCKET)) {
            units = InfxCow.MILK_UNITS_PER_DAY;
            filled = new ItemStack(Items.MILK_BUCKET);
        } else if (stack.getItem() instanceof InfxBucketItem bucket
                && bucket.contents() == InfxBucketItem.Contents.EMPTY) {
            units = InfxCow.MILK_UNITS_PER_DAY;
            filled = InfXItems.bucket(bucket.material(), InfxBucketItem.Contents.MILK).toStack();
        } else {
            return null;
        }

        if (!Livestock.takeMilk(
                goat, serverLevel, units, GOAT_MILK_DAY, GOAT_MILK_UNITS, InfxCow.MILK_UNITS_PER_DAY)) {
            return InteractionResult.CONSUME;
        }
        ItemStack remainder = ItemUtils.createFilledResult(stack, player, filled);
        player.setItemInHand(hand, remainder);
        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        goat.playSound(SoundEvents.GOAT_MILK, 1.0F, 1.0F);
        return InteractionResult.SUCCESS;
    }

    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        // MITE wolf-family combat mobs keep their experience; other animals grant none.
        if (event.getEntity() instanceof Animal animal && !(animal instanceof Wolf)) {
            event.setDroppedExperience(0);
        }
    }

    @SubscribeEvent
    public static void onDamaged(LivingDamageEvent.Post event) {
        if (event.getEntity() instanceof Animal animal) {
            Livestock.onHurt(animal, event.getSource(), event.getOriginalDamage());
        }
    }

    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;

        if (event.getEntity() instanceof Animal animal
                && Livestock.hasSickSkin(animal)
                && !Livestock.isWell(animal)) {
            // MITE's unwell state removes only the meat yield. Leather, wool and feathers remain.
            event.getDrops().removeIf(drop -> isLivestockMeat(animal, drop.getItem()));
        }

        if (event.getEntity() instanceof IronGolem golem) {
            event.getDrops().removeIf(drop -> drop.getItem().is(Items.IRON_INGOT));
            event.getDrops()
                    .add(drop(level, golem, new ItemStack(Items.IRON_NUGGET, 2 + golem.getRandom().nextInt(4))));
        }

        if (event.getEntity() instanceof Donkey || event.getEntity() instanceof Mule) {
            // MITE donkeys and mules drop beef like the INFX horse replacement.
            event.getDrops().add(drop(level, event.getEntity(), new ItemStack(
                    Items.BEEF, 1 + event.getEntity().getRandom().nextInt(3))));
        }
    }

    private static boolean isLivestockMeat(Animal animal, ItemStack stack) {
        if (animal instanceof InfxCow) {
            return stack.is(Items.BEEF) || stack.is(Items.COOKED_BEEF);
        }
        if (animal instanceof InfxChicken) {
            return stack.is(Items.CHICKEN) || stack.is(Items.COOKED_CHICKEN);
        }
        if (animal instanceof InfxPig) {
            return stack.is(Items.PORKCHOP) || stack.is(Items.COOKED_PORKCHOP);
        }
        if (animal instanceof InfxSheep) {
            return stack.is(Items.MUTTON) || stack.is(Items.COOKED_MUTTON);
        }
        return false;
    }

    private static ItemEntity drop(ServerLevel level, Entity source, ItemStack stack) {
        return new ItemEntity(level, source.getX(), source.getY(), source.getZ(), stack);
    }
}
