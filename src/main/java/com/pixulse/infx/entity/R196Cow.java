package com.pixulse.infx.entity;

import com.pixulse.infx.item.R196BucketItem;
import com.pixulse.infx.registry.ModEntityTypes;
import com.pixulse.infx.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/** R196 cow: livestock needs, milk quota, and panic live on the entity. */
public final class R196Cow extends Cow {
    private static final String MILK_DAY = "infx_cow_milk_day";
    private static final String MILK_UNITS = "infx_cow_milk_units";
    /** Daily milk budget shared by buckets (4) and bowls (1 each, max 4). */
    public static final int MILK_UNITS_PER_DAY = 4;

    public R196Cow(EntityType<? extends Cow> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return AbstractCow.createAttributes().add(Attributes.MAX_HEALTH, 20.0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        R196Livestock.ensureGoals(this);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide()) {
            R196Livestock.serverTick(this);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt) R196Livestock.onHurt(this, amount);
        return hurt;
    }

    @Override
    public boolean canMate(Animal partner) {
        if (!(level() instanceof ServerLevel serverLevel)) return super.canMate(partner);
        return super.canMate(partner) && R196Livestock.canMateWith(serverLevel, this, partner);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        R196Livestock.markFedIfFood(this, stack);
        if (!isBaby() && level() instanceof ServerLevel serverLevel) {
            if (stack.is(Items.BUCKET)) {
                if (!takeMilk(serverLevel, MILK_UNITS_PER_DAY)) {
                    return InteractionResult.CONSUME;
                }
                // Let vanilla fill the milk bucket.
                return super.mobInteract(player, hand);
            }
            if (stack.is(Items.BOWL)) {
                if (!takeMilk(serverLevel, 1)) return InteractionResult.CONSUME;
                giveFilled(player, hand, stack, ModItems.MILK_BOWL.toStack(), Items.BOWL);
                return InteractionResult.SUCCESS;
            }
            if (stack.getItem() instanceof R196BucketItem bucket
                    && bucket.contents() == R196BucketItem.Contents.EMPTY) {
                if (!takeMilk(serverLevel, MILK_UNITS_PER_DAY)) return InteractionResult.CONSUME;
                giveFilled(
                        player,
                        hand,
                        stack,
                        ModItems.bucket(bucket.material(), R196BucketItem.Contents.MILK).toStack(),
                        bucket);
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    boolean takeMilk(ServerLevel level, int units) {
        if (!R196Livestock.isProductive(this)) return false;
        long day = level.getOverworldClockTime() / 24_000L;
        var data = getPersistentData();
        if (data.getLong(MILK_DAY).orElse(Long.MIN_VALUE) != day) {
            data.putLong(MILK_DAY, day);
            data.putInt(MILK_UNITS, 0);
        }
        int used = data.getInt(MILK_UNITS).orElse(0);
        if (used + units > MILK_UNITS_PER_DAY) return false;
        data.putInt(MILK_UNITS, used + units);
        return true;
    }

    private void giveFilled(
            Player player, InteractionHand hand, ItemStack empty, ItemStack filled, Item usedItem) {
        ItemStack remainder = ItemUtils.createFilledResult(empty, player, filled);
        player.setItemInHand(hand, remainder);
        player.awardStat(Stats.ITEM_USED.get(usedItem));
        playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
    }

    @Override
    public @Nullable Cow getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return ModEntityTypes.R196_COW.get().create(level, EntitySpawnReason.BREEDING);
    }
}
