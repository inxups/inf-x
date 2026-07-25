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
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/** R196 pig: livestock needs and breeding gates. */
public final class R196Pig extends Pig {
    /**
     * Per-class isWell id. It must be registered while this class initializes, before Entity builds
     * its fixed-size synced-data array, and not on {@code Animal.class} where it collides with Pig
     * variant data.
     */
    private static final EntityDataAccessor<Boolean> DATA_WELL =
            SynchedEntityData.defineId(R196Pig.class, EntityDataSerializers.BOOLEAN);

    public R196Pig(EntityType<? extends Pig> type, Level level) {
        super(type, level);
    }

    static EntityDataAccessor<Boolean> dataWell() {
        return DATA_WELL;
    }

    public static AttributeSupplier.Builder attributes() {
        return Pig.createAttributes().add(Attributes.MAX_HEALTH, 10.0);
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
        boolean offeredFood = isFood(player.getItemInHand(hand));
        InteractionResult result = super.mobInteract(player, hand);
        R196Livestock.markFedAfterInteraction(this, offeredFood, result);
        return result;
    }

    @Override
    public @Nullable Pig getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return ModEntityTypes.R196_PIG.get().create(level, EntitySpawnReason.BREEDING);
    }
}
