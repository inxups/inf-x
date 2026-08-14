package com.pixulse.infx.mixin.world.entity.item;

import com.pixulse.infx.world.FallingBlockTargetAccess;
import com.pixulse.infx.world.SoilCollapse;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityLandslideMixin extends Entity implements FallingBlockTargetAccess {
    @Unique
    private static final double ROLL_SPEED = 0.24D;
    @Unique
    private boolean rolling;
    @Unique
    @Nullable
    private BlockPos allocatedTarget;

    @Shadow
    public int time;

    @Shadow
    public abstract BlockState getBlockState();

    protected FallingBlockEntityLandslideMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void tickCollapsingBlock(CallbackInfo callbackInfo) {
        if (!SoilCollapse.usesLandslideBehavior(getBlockState())) return;
        callbackInfo.cancel();

        FallingBlockEntity self = (FallingBlockEntity) (Object) this;
        if (getBlockState().isAir()) {
            SoilCollapse.releaseSlideTarget(self);
            discard();
            return;
        }

        time++;
        if (rolling) {
            roll();
        } else {
            applyGravity();
            move(MoverType.SELF, getDeltaMovement());
            handlePortal();
            if (onGround()) {
                BlockPos landingPos = blockPosition();
                BlockPos target = SoilCollapse.reserveSlideTarget(level(), landingPos, self);
                if (target == null) {
                    setDeltaMovement(Vec3.ZERO);
                    if (!level().isClientSide()) SoilCollapse.settle(self);
                } else {
                    setPos(landingPos.getX() + 0.5D, landingPos.getY(), landingPos.getZ() + 0.5D);
                    rolling = true;
                    setOnGround(false);
                    setDeltaMovement(Vec3.ZERO);
                }
            } else {
                setDeltaMovement(getDeltaMovement().scale(0.98D));
            }
        }

        if (!level().isClientSide() && !isRemoved()
                && (time > 600 || time > 100
                        && (blockPosition().getY() <= level().getMinY()
                                || blockPosition().getY() > level().getMaxY()))) {
            SoilCollapse.drop(self);
        }
    }

    @Override
    public @Nullable BlockPos getAllocatedTarget() {
        return allocatedTarget;
    }

    @Override
    public void setAllocatedTarget(@Nullable BlockPos target) {
        allocatedTarget = target;
    }

    @Unique
    private void roll() {
        BlockPos target = allocatedTarget;
        if (target == null) {
            rolling = false;
            return;
        }
        double targetX = target.getX() + 0.5D;
        double targetZ = target.getZ() + 0.5D;
        double dx = targetX - getX();
        double dz = targetZ - getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance <= ROLL_SPEED) {
            setPos(targetX, getY(), targetZ);
            setDeltaMovement(Vec3.ZERO);
            setOnGround(false);
            rolling = false;
            return;
        }
        setOnGround(false);
        setDeltaMovement(dx / distance * ROLL_SPEED, 0.0D, dz / distance * ROLL_SPEED);
        move(MoverType.SELF, getDeltaMovement());
        if (horizontalCollision) {
            setDeltaMovement(Vec3.ZERO);
            rolling = false;
            SoilCollapse.releaseSlideTarget((FallingBlockEntity) (Object) this);
        }
    }
}
