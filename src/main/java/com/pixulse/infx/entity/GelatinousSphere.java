package com.pixulse.infx.entity;

import com.pixulse.infx.item.equipment.CorrosionType;
import com.pixulse.infx.item.GelatinousSphereItem;
import com.pixulse.infx.registry.InfXEntityTypes;
import com.pixulse.infx.registry.InfXItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.NonNull;

/** Throwable counterpart to an R196 gelatinous sphere item. */
public final class GelatinousSphere extends ThrowableItemProjectile {
    public GelatinousSphere(EntityType<? extends GelatinousSphere> type, Level level) {
        super(type, level);
    }

    public GelatinousSphere(Level level, LivingEntity owner, ItemStack stack) {
        super(InfXEntityTypes.GELATINOUS_SPHERE.get(), owner, level, stack);
    }

    public GelatinousSphere(Level level, double x, double y, double z, ItemStack stack) {
        super(InfXEntityTypes.GELATINOUS_SPHERE.get(), x, y, z, level, stack);
    }

    @Override
    protected @NonNull Item getDefaultItem() {
        return InfXItems.GREEN_GELATINOUS_SPHERE.get();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.07;
    }

    @Override
    protected void onHitEntity(@NonNull EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (!(level() instanceof ServerLevel level)) {
            return;
        }
        Entity target = hitResult.getEntity();
        target.hurtServer(level, damageSources().thrown(this, getOwner()), 1.0F + sphere().attackDamage());
    }

    @Override
    protected void onHitBlock(@NonNull BlockHitResult hitResult) {
        if (level() instanceof ServerLevel level) {
            BlockPos target = hitResult.getBlockPos();
            Direction face = hitResult.getDirection();
            CorrosionType type = sphere().corrosionType();
            boolean reacted = GelatinousCubeRules.dissolveOnContact(
                    level, target.relative(face), type, face.getOpposite());
            reacted |= GelatinousCubeRules.dissolveOnContact(level, target, type, face);
            if (type == CorrosionType.ACID && reacted) {
                GelatinousCubeEvents.playAcidCorrosionFizz(level, target, getRandom());
            }
        }
        super.onHitBlock(hitResult);
    }

    @Override
    protected void onHit(@NonNull HitResult hitResult) {
        super.onHit(hitResult);
        if (!level().isClientSide()) {
            level().broadcastEntityEvent(this, (byte) 3);
            discard();
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id != 3) {
            super.handleEntityEvent(id);
            return;
        }
        ParticleOptions particle = new ItemParticleOption(
                ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(getItem()));
        for (int index = 0; index < 8; index++) {
            level().addParticle(particle, getX(), getY(), getZ(), 0.0, 0.0, 0.0);
        }
    }

    private GelatinousSphereItem sphere() {
        return getItem().getItem() instanceof GelatinousSphereItem item
                ? item
                : InfXItems.GREEN_GELATINOUS_SPHERE.get();
    }
}
