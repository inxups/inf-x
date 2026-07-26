package com.pixulse.infx.entity;

import com.pixulse.infx.registry.ModEntityTypes;
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
import org.jspecify.annotations.Nullable;

/** R196 chicken: egg delay, feather shedding, and livestock needs. */
public final class R196Chicken extends Chicken {
    /**
     * Per-class isWell id. It must be registered while this class initializes, before Entity builds
     * its fixed-size synced-data array, and not on {@code Animal.class} where it collides with
     * Chicken variant data.
     */
    private static final EntityDataAccessor<Boolean> DATA_WELL =
            SynchedEntityData.defineId(R196Chicken.class, EntityDataSerializers.BOOLEAN);
    private static final String NEXT_FEATHER = "infx_chicken_next_feather";
    private static final long FEATHER_INTERVAL = 96_000L;

    public R196Chicken(EntityType<? extends Chicken> type, Level level) {
        super(type, level);
    }

    static EntityDataAccessor<Boolean> dataWell() {
        return DATA_WELL;
    }

    public static AttributeSupplier.Builder attributes() {
        return Chicken.createAttributes().add(Attributes.MAX_HEALTH, 4.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        R196Livestock.defineWellData(entityData, dataWell());
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        R196Livestock.ensureGoals(this);
    }

    @Override
    public void aiStep() {
        if (!level().isClientSide()
                && shouldDelayEgg(!isBaby(), isChickenJockey(), R196Livestock.isProductive(this), eggTime)) {
            eggTime = 1_200;
        }
        super.aiStep();
        if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
            R196Livestock.serverTick(this);
            updateProduction(serverLevel);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt) {
            R196Livestock.onHurt(this, amount);
            long accelerated = level.getGameTime() + 1_200L;
            long current = getPersistentData().getLong(NEXT_FEATHER).orElse(Long.MAX_VALUE);
            getPersistentData().putLong(NEXT_FEATHER, Math.min(current, accelerated));
        }
        return hurt;
    }

    @Override
    public boolean canMate(Animal partner) {
        if (!(level() instanceof ServerLevel serverLevel)) return super.canMate(partner);
        return super.canMate(partner) && R196Livestock.canMateWith(serverLevel, this, partner);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        boolean offeredFood = isFood(player.getItemInHand(hand));
        InteractionResult result = super.mobInteract(player, hand);
        R196Livestock.markFedAfterInteraction(this, offeredFood, result);
        return result;
    }

    public static boolean shouldDelayEgg(boolean adult, boolean jockey, boolean productive, int eggTime) {
        return adult && !jockey && !productive && eggTime <= 1;
    }

    public void updateProduction(ServerLevel level) {
        if (isBaby() || isChickenJockey()) return;
        if (!R196Livestock.isProductive(this)) {
            eggTime = Math.max(eggTime, 1_200);
        }

        long now = level.getGameTime();
        long next = getPersistentData().getLong(NEXT_FEATHER).orElse(0L);
        if (next == 0L) {
            getPersistentData().putLong(NEXT_FEATHER, now + FEATHER_INTERVAL);
        } else if (now >= next && R196Livestock.isProductive(this)) {
            spawnAtLocation(level, Items.FEATHER);
            getPersistentData().putLong(NEXT_FEATHER, now + FEATHER_INTERVAL);
        }
    }

    @Override
    public void finalizeSpawnChildFromBreeding(
            ServerLevel level, Animal partner, @Nullable AgeableMob offspring) {
        super.finalizeSpawnChildFromBreeding(level, partner, offspring);
        if (offspring instanceof Animal child) {
            R196Livestock.adoptWellnessFromParents(child, this, partner);
        }
    }

    @Override
    public @Nullable Chicken getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return ModEntityTypes.R196_CHICKEN.get().create(level, EntitySpawnReason.BREEDING);
    }
}
