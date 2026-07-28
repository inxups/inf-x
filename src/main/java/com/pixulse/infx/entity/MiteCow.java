package com.pixulse.infx.entity;

import com.pixulse.infx.item.MiteBucketItem;
import com.pixulse.infx.registry.InfXEntityTypes;
import com.pixulse.infx.registry.InfXItems;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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

/** R196 cow: livestock needs and milk quota; panic is handled by the common livestock event. */
public final class MiteCow extends Cow {
    /**
     * Per-class isWell id. It must be registered while this class initializes, before Entity builds
     * its fixed-size synced-data array, and not on {@code Animal.class} where it collides with Cow
     * variant data.
     */
    private static final EntityDataAccessor<Boolean> DATA_WELL =
            SynchedEntityData.defineId(MiteCow.class, EntityDataSerializers.BOOLEAN);
    private static final String MILK_DAY = "infx_cow_milk_day";
    private static final String MILK_UNITS = "infx_cow_milk_units";
    /** Daily milk budget shared by buckets (4) and bowls (1 each, max 4). */
    public static final int MILK_UNITS_PER_DAY = 4;

    public MiteCow(EntityType<? extends Cow> type, Level level) {
        super(type, level);
    }

    static EntityDataAccessor<Boolean> dataWell() {
        return DATA_WELL;
    }

    public static AttributeSupplier.Builder attributes() {
        return AbstractCow.createAttributes().add(Attributes.MAX_HEALTH, 20.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        Livestock.defineWellData(entityData, dataWell());
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        Livestock.ensureGoals(this);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide()) {
            Livestock.serverTick(this);
        }
    }

    @Override
    public boolean canMate(Animal partner) {
        if (!(level() instanceof ServerLevel serverLevel)) return super.canMate(partner);
        return super.canMate(partner) && Livestock.canMateWith(serverLevel, this, partner);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean offeredFood = isFood(stack);
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
                giveFilled(player, hand, stack, InfXItems.MILK_BOWL.toStack(), Items.BOWL);
                return InteractionResult.SUCCESS;
            }
            if (stack.getItem() instanceof MiteBucketItem bucket
                    && bucket.contents() == MiteBucketItem.Contents.EMPTY) {
                if (!takeMilk(serverLevel, MILK_UNITS_PER_DAY)) return InteractionResult.CONSUME;
                giveFilled(
                        player,
                        hand,
                        stack,
                        InfXItems.bucket(bucket.material(), MiteBucketItem.Contents.MILK).toStack(),
                        bucket);
                return InteractionResult.SUCCESS;
            }
        }
        InteractionResult result = super.mobInteract(player, hand);
        Livestock.markFedAfterInteraction(this, offeredFood, result);
        return result;
    }

    boolean takeMilk(ServerLevel level, int units) {
        if (!Livestock.isProductive(this)) return false;
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
    public void finalizeSpawnChildFromBreeding(
            ServerLevel level, Animal partner, @Nullable AgeableMob offspring) {
        super.finalizeSpawnChildFromBreeding(level, partner, offspring);
        if (offspring instanceof Animal child) {
            Livestock.adoptWellnessFromParents(child, this, partner);
        }
    }

    @Override
    public @Nullable Cow getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return InfXEntityTypes.R196_COW.get().create(level, EntitySpawnReason.BREEDING);
    }
}
