package com.pixulse.infx.block;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.registry.InfXBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;

/** Nether-gravel-only shrub that applies Wither while an entity touches it. */
public final class WitherwoodBlock extends VegetationBlock {
    public static final MapCodec<WitherwoodBlock> CODEC = simpleCodec(WitherwoodBlock::new);
    private static final VoxelShape SHAPE = Block.column(12.0, 0.0, 13.0);

    public WitherwoodBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull MapCodec<WitherwoodBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, @NonNull BlockGetter level, @NonNull BlockPos pos) {
        return state.is(InfXBlocks.NETHER_GRAVEL.get());
    }

    @Override
    protected @NonNull VoxelShape getShape(@NonNull BlockState state, @NonNull BlockGetter level, @NonNull BlockPos pos, @NonNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void entityInside(
            @NonNull BlockState state,
            Level level,
            @NonNull BlockPos pos,
            @NonNull Entity entity,
            @NonNull InsideBlockEffectApplier effectApplier,
            boolean isPrecise) {
        if (!level.isClientSide() && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 0, false, true));
        }
    }
}
