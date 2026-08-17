package com.pixulse.infx.entity;

import com.pixulse.infx.registry.InfXEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

/** MITE's throwable web: it traps the impact cell instead of dealing projectile damage. */
public final class InfxWebProjectile extends ThrowableItemProjectile {
    private static final String FIERY_KEY = "infx.fiery_web";
    private static final int ENTITY_WEB_LEAD_TICKS = 4;

    private boolean fiery;

    public InfxWebProjectile(EntityType<? extends InfxWebProjectile> type, Level level) {
        super(type, level);
    }

    public InfxWebProjectile(Level level, LivingEntity owner, boolean fiery) {
        super(InfXEntityTypes.WEB_PROJECTILE.get(), owner, level, Items.COBWEB.getDefaultInstance());
        this.fiery = fiery;
        if (fiery) {
            igniteForSeconds(10.0F);
        }
    }

    @Override
    protected @NonNull Item getDefaultItem() {
        return Items.COBWEB;
    }

    @Override
    protected void onHitEntity(@NonNull EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (!(level() instanceof ServerLevel level) || !(hitResult.getEntity() instanceof LivingEntity target)) {
            return;
        }
        if (isFiery()) {
            target.igniteForSeconds(5.0F);
        }
        placeWebOnEntity(level, target);
    }

    @Override
    protected void onHitBlock(@NonNull BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        if (!(level() instanceof ServerLevel level) || !level.getGameRules().get(GameRules.MOB_GRIEFING)) {
            return;
        }
        BlockPos impact = hitResult.getBlockPos();
        BlockPos neighbor = impact.relative(hitResult.getDirection());
        if (!canPlaceWebAfterBlockImpact(level.getBlockState(impact), level.getBlockState(neighbor))) {
            return;
        }
        BlockPos placement = impact;
        if (!isWebReplaceable(level.getBlockState(placement))) {
            placement = neighbor;
        }
        if (!placeWeb(level, placement)) {
            placeWeb(level, placement.above());
        }
    }

    @Override
    protected void onHit(@NonNull HitResult hitResult) {
        super.onHit(hitResult);
        if (!level().isClientSide()) {
            discard();
        }
    }

    @Override
    protected void addAdditionalSaveData(@NonNull ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean(FIERY_KEY, fiery);
    }

    @Override
    protected void readAdditionalSaveData(@NonNull ValueInput input) {
        super.readAdditionalSaveData(input);
        fiery = input.getBooleanOr(FIERY_KEY, false);
    }

    /** A fire web remains fire-capable after its visual fire ticks have expired. */
    public boolean isFiery() {
        return fiery || isOnFire();
    }

    static boolean isWebReplaceable(BlockState state) {
        return state.isAir()
                || state.is(Blocks.SNOW) && state.getValue(SnowLayerBlock.LAYERS) == 1;
    }

    /** Mirrors MITE's water, lava, and adjacent-fire impact exclusions. */
    static boolean canPlaceWebAfterBlockImpact(BlockState impact, BlockState neighbor) {
        return !neighbor.is(Blocks.FIRE)
                && !impact.is(Blocks.LAVA)
                && !impact.getFluidState().is(FluidTags.LAVA)
                && !neighbor.is(Blocks.LAVA)
                && !neighbor.getFluidState().is(FluidTags.LAVA)
                && impact.getFluidState().isEmpty()
                && !neighbor.is(Blocks.WATER)
                && !neighbor.getFluidState().is(FluidTags.WATER);
    }

    private void placeWebOnEntity(ServerLevel level, LivingEntity target) {
        if (!level.getGameRules().get(GameRules.MOB_GRIEFING)) {
            return;
        }
        BlockPos current = target.blockPosition();
        Vec3 movement = target.getKnownMovement();
        BlockPos predicted = BlockPos.containing(
                target.getX() + movement.x * ENTITY_WEB_LEAD_TICKS,
                target.getY(),
                target.getZ() + movement.z * ENTITY_WEB_LEAD_TICKS);
        if (!predicted.equals(current) && placeWeb(level, predicted)) {
            return;
        }
        if (placeWeb(level, current)) {
            return;
        }

        AABB bounds = target.getBoundingBox();
        BlockPos min = BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ);
        BlockPos max = BlockPos.containing(bounds.maxX, bounds.maxY, bounds.maxZ);
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (placeWeb(level, pos)) {
                return;
            }
        }
    }

    private boolean placeWeb(ServerLevel level, BlockPos pos) {
        if (!isWebReplaceable(level.getBlockState(pos))
                || !level.setBlock(pos, Blocks.COBWEB.defaultBlockState(), 3)) {
            return false;
        }
        if (isFiery()) {
            igniteWeb(level, pos);
        }
        return true;
    }

    private void igniteWeb(ServerLevel level, BlockPos pos) {
        Direction[] choices = new Direction[Direction.values().length];
        int choiceCount = 0;
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = pos.relative(direction);
            if (level.isEmptyBlock(neighbor) && BaseFireBlock.canBePlacedAt(level, neighbor, Direction.UP)) {
                choices[choiceCount++] = direction;
            }
        }
        if (choiceCount > 0) {
            BlockPos firePos = pos.relative(choices[getRandom().nextInt(choiceCount)]);
            level.setBlock(firePos, BaseFireBlock.getState(level, firePos), 3);
        }
        for (LivingEntity entity : level.getEntitiesOfClass(
                LivingEntity.class,
                AABB.unitCubeFromLowerCorner(Vec3.atLowerCornerOf(pos)),
                Entity::isAlive)) {
            entity.igniteForSeconds(5.0F);
        }
    }
}
