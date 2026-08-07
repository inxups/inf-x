package com.pixulse.infx.entity;

import com.pixulse.infx.registry.InfXEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.NonNull;

/** InfX EntityBrick: the throwable counterpart of the brick, nether brick and resin brick items. */
public final class InfxBrickProjectile extends ThrowableItemProjectile {
    public InfxBrickProjectile(EntityType<? extends InfxBrickProjectile> type, Level level) {
        super(type, level);
    }

    public InfxBrickProjectile(Level level, LivingEntity owner, ItemStack stack) {
        super(InfXEntityTypes.BRICK_PROJECTILE.get(), owner, level, stack);
    }

    public InfxBrickProjectile(Level level, double x, double y, double z, ItemStack stack) {
        super(InfXEntityTypes.BRICK_PROJECTILE.get(), x, y, z, level, stack);
    }

    @Override
    protected @NonNull Item getDefaultItem() {
        return Items.BRICK;
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
        // InfX EntityBrick#onImpact: 2.0 thrown damage.
        target.hurtServer(level, damageSources().thrown(this, getOwner()), 2.0F);
    }

    @Override
    protected void onHitBlock(@NonNull BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        if (level() instanceof ServerLevel level) {
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = level.getBlockState(pos);
            // InfX EntityBrick#onImpact: a brick breaks thin glass (panes) on impact.
            if (BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().endsWith("glass_pane")) {
                level.destroyBlock(pos, false);
            }
        }
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
}
