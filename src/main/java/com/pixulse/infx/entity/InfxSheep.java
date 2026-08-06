package com.pixulse.infx.entity;

import com.pixulse.infx.registry.InfXEntityTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** INFX sheep: productive shearing, fire/acid wool strip, leather drop chance. */
public final class InfxSheep extends Sheep {
    /**
     * Per-class isWell id. It must be registered while this class initializes, before Entity builds
     * its fixed-size synced-data array, and not on {@code Animal.class} where it collides with Sheep
     * wool data.
     */
    private static final EntityDataAccessor<Boolean> DATA_WELL =
            SynchedEntityData.defineId(InfxSheep.class, EntityDataSerializers.BOOLEAN);

    public InfxSheep(EntityType<? extends Sheep> type, Level level) {
        super(type, level);
    }

    static EntityDataAccessor<Boolean> dataWell() {
        return DATA_WELL;
    }

    public static AttributeSupplier.Builder attributes() {
        return Sheep.createAttributes().add(Attributes.MAX_HEALTH, 8.0);
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
    public boolean hurtServer(@NonNull ServerLevel level, @NonNull DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt) {
            if (source.is(DamageTypeTags.IS_FIRE)
                    || source.typeHolder().unwrapKey()
                            .map(key -> key.identifier().getPath().contains("acid"))
                            .orElse(false)) {
                setSheared(true);
            }
            // MITE EntitySheep#onEntityDamaged: any gelatinous sphere hit (including the gray and
            // black acid spheres) or gelatinous-cube melee instantly corrodes the wool.
            if (source.getDirectEntity() instanceof GelatinousSphere || source.getDirectEntity() instanceof InfxSlime) {
                setSheared(true);
            }
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

    @Override
    public void die(@NonNull DamageSource source) {
        if (!level().isClientSide()
                && getRandom().nextBoolean()
                && level() instanceof ServerLevel serverLevel) {
            serverLevel.addFreshEntity(new ItemEntity(
                    serverLevel, getX(), getY(), getZ(), new ItemStack(Items.LEATHER)));
        }
        super.die(source);
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
    public @Nullable Sheep getBreedOffspring(@NonNull ServerLevel level, @NonNull AgeableMob partner) {
        return InfXEntityTypes.INFX_SHEEP.get().create(level, EntitySpawnReason.BREEDING);
    }
}
