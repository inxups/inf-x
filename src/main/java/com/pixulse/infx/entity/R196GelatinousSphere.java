package com.pixulse.infx.entity;

import com.pixulse.infx.equipment.R196CorrosionType;
import com.pixulse.infx.item.R196GelatinousSphereItem;
import com.pixulse.infx.registry.ModEntityTypes;
import com.pixulse.infx.registry.ModItems;
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

/** Throwable counterpart to an R196 gelatinous sphere item. */
public final class R196GelatinousSphere extends ThrowableItemProjectile {
    public R196GelatinousSphere(EntityType<? extends R196GelatinousSphere> type, Level level) {
        super(type, level);
    }

    public R196GelatinousSphere(Level level, LivingEntity owner, ItemStack stack) {
        super(ModEntityTypes.GELATINOUS_SPHERE.get(), owner, level, stack);
    }

    public R196GelatinousSphere(Level level, double x, double y, double z, ItemStack stack) {
        super(ModEntityTypes.GELATINOUS_SPHERE.get(), x, y, z, level, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.GREEN_GELATINOUS_SPHERE.get();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.07;
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (!(level() instanceof ServerLevel level)) {
            return;
        }
        Entity target = hitResult.getEntity();
        target.hurtServer(level, damageSources().thrown(this, getOwner()), 1.0F + sphere().attackDamage());
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        if (level() instanceof ServerLevel level) {
            BlockPos target = hitResult.getBlockPos();
            Direction face = hitResult.getDirection();
            R196CorrosionType type = sphere().corrosionType();
            R196GelatinousCubeRules.dissolveOnContact(level, target.relative(face), type, face.getOpposite());
            R196GelatinousCubeRules.dissolveOnContact(level, target, type, face);
        }
        super.onHitBlock(hitResult);
    }

    @Override
    protected void onHit(HitResult hitResult) {
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

    private R196GelatinousSphereItem sphere() {
        return getItem().getItem() instanceof R196GelatinousSphereItem item
                ? item
                : ModItems.GREEN_GELATINOUS_SPHERE.get();
    }
}
