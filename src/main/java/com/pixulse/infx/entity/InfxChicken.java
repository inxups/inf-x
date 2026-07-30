package com.pixulse.infx.entity;

import com.pixulse.infx.registry.InfXEntityTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** INFX chicken: egg delay, feather shedding, and livestock needs. */
public final class InfxChicken extends Chicken {
    /**
     * Per-class isWell id. It must be registered while this class initializes, before Entity builds
     * its fixed-size synced-data array, and not on {@code Animal.class} where it collides with
     * Chicken variant data.
     */
    private static final EntityDataAccessor<Boolean> DATA_WELL =
            SynchedEntityData.defineId(InfxChicken.class, EntityDataSerializers.BOOLEAN);
    private static final String NEXT_FEATHER = "infx_chicken_next_feather";
    private static final long FEATHER_INTERVAL = 96_000L;

    public InfxChicken(EntityType<? extends Chicken> type, Level level) {
        super(type, level);
    }

    static EntityDataAccessor<Boolean> dataWell() {
        return DATA_WELL;
    }

    public static AttributeSupplier.Builder attributes() {
        return Chicken.createAttributes().add(Attributes.MAX_HEALTH, 4.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NonNull Builder entityData) {
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
        if (!level().isClientSide()
                && shouldDelayEgg(!isBaby(), isChickenJockey(), Livestock.isProductive(this), eggTime)) {
            eggTime = 1_200;
        }
        super.aiStep();
        if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
            Livestock.serverTick(this);
            updateProduction(serverLevel);
        }
    }

    @Override
    public boolean hurtServer(@NonNull ServerLevel level, @NonNull DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt) {
            long accelerated = level.getGameTime() + 1_200L;
            long current = getPersistentData().getLong(NEXT_FEATHER).orElse(Long.MAX_VALUE);
            getPersistentData().putLong(NEXT_FEATHER, Math.min(current, accelerated));
        }
        return hurt;
    }

    @Override
    public boolean canMate(@NonNull Animal partner) {
        if (!(level() instanceof ServerLevel serverLevel)) return super.canMate(partner);
        return super.canMate(partner) && Livestock.canMateWith(serverLevel, this, partner);
    }

    @Override
    public @NonNull InteractionResult mobInteract(Player player, @NonNull InteractionHand hand) {
        boolean offeredFood = isFood(player.getItemInHand(hand));
        InteractionResult result = super.mobInteract(player, hand);
        Livestock.markFedAfterInteraction(this, offeredFood, result);
        return result;
    }

    public static boolean shouldDelayEgg(boolean adult, boolean jockey, boolean productive, int eggTime) {
        return adult && !jockey && !productive && eggTime <= 1;
    }

    public void updateProduction(ServerLevel level) {
        if (isBaby() || isChickenJockey()) return;
        if (!Livestock.isProductive(this)) {
            eggTime = Math.max(eggTime, 1_200);
        }

        long now = level.getGameTime();
        long next = getPersistentData().getLong(NEXT_FEATHER).orElse(0L);
        if (next == 0L) {
            getPersistentData().putLong(NEXT_FEATHER, now + FEATHER_INTERVAL);
        } else if (now >= next && Livestock.isProductive(this)) {
            spawnAtLocation(level, Items.FEATHER);
            getPersistentData().putLong(NEXT_FEATHER, now + FEATHER_INTERVAL);
        }
    }

    @Override
    public void finalizeSpawnChildFromBreeding(
            @NonNull ServerLevel level, @NonNull Animal partner, @Nullable AgeableMob offspring) {
        super.finalizeSpawnChildFromBreeding(level, partner, offspring);
        if (offspring instanceof Animal child) {
            Livestock.adoptWellnessFromParents(child, this, partner);
        }
    }

    @Override
    public @Nullable Chicken getBreedOffspring(@NonNull ServerLevel level, @NonNull AgeableMob partner) {
        return InfXEntityTypes.INFX_CHICKEN.get().create(level, EntitySpawnReason.BREEDING);
    }
}
