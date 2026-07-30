package com.pixulse.infx.entity;

import com.pixulse.infx.registry.InfXEntityTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** INFX pig: livestock needs and breeding gates. */
public final class InfxPig extends Pig {
    /**
     * Per-class isWell id. It must be registered while this class initializes, before Entity builds
     * its fixed-size synced-data array, and not on {@code Animal.class} where it collides with Pig
     * variant data.
     */
    private static final EntityDataAccessor<Boolean> DATA_WELL =
            SynchedEntityData.defineId(InfxPig.class, EntityDataSerializers.BOOLEAN);

    public InfxPig(EntityType<? extends Pig> type, Level level) {
        super(type, level);
    }

    static EntityDataAccessor<Boolean> dataWell() {
        return DATA_WELL;
    }

    public static AttributeSupplier.Builder attributes() {
        return Pig.createAttributes().add(Attributes.MAX_HEALTH, 10.0);
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
        super.aiStep();
        if (!level().isClientSide()) {
            Livestock.serverTick(this);
        }
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

    @Override
    public void finalizeSpawnChildFromBreeding(
            @NonNull ServerLevel level, @NonNull Animal partner, @Nullable AgeableMob offspring) {
        super.finalizeSpawnChildFromBreeding(level, partner, offspring);
        if (offspring instanceof Animal child) {
            Livestock.adoptWellnessFromParents(child, this, partner);
        }
    }

    @Override
    public @Nullable Pig getBreedOffspring(@NonNull ServerLevel level, @NonNull AgeableMob partner) {
        return InfXEntityTypes.INFX_PIG.get().create(level, EntitySpawnReason.BREEDING);
    }
}
