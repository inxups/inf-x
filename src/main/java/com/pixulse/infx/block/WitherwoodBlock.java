package com.pixulse.infx.block;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.registry.ModBlocks;
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

/** Nether-gravel-only shrub that applies Wither while an entity touches it. */
public final class WitherwoodBlock extends VegetationBlock {
    public static final MapCodec<WitherwoodBlock> CODEC = simpleCodec(WitherwoodBlock::new);
    private static final VoxelShape SHAPE = Block.column(12.0, 0.0, 13.0);

    public WitherwoodBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<WitherwoodBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(ModBlocks.NETHER_GRAVEL.get());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void entityInside(
            BlockState state,
            Level level,
            BlockPos pos,
            Entity entity,
            InsideBlockEffectApplier effectApplier,
            boolean isPrecise) {
        if (!level.isClientSide() && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 0, false, true));
        }
    }
}
