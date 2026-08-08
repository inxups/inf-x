package com.pixulse.infx.block;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.registry.InfXItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;

/** InfX-style three-stage bush that yields one blueberry and regrows after harvesting. */
public final class BlueberryBushBlock extends SweetBerryBushBlock {
    public static final MapCodec<SweetBerryBushBlock> CODEC = simpleCodec(BlueberryBushBlock::new);
    private static final float GROWTH_CHANCE = 0.025F;
    private static final VoxelShape SHAPE = Block.box(3.2D, 0.0D, 3.2D, 12.8D, 9.6D, 12.8D);

    public BlueberryBushBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull MapCodec<SweetBerryBushBlock> codec() {
        return CODEC;
    }

    @Override
    protected @NonNull ItemStack getCloneItemStack(
            @NonNull LevelReader level, @NonNull BlockPos pos, @NonNull BlockState state, boolean includeData) {
        return InfXItems.BLUEBERRY_BUSH.toStack();
    }

    @Override
    protected @NonNull VoxelShape getShape(
            @NonNull BlockState state,
            @NonNull BlockGetter level,
            @NonNull BlockPos pos,
            @NonNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void randomTick(
            @NonNull BlockState state, @NonNull ServerLevel level, @NonNull BlockPos pos, @NonNull RandomSource random) {
        int age = state.getValue(AGE);
        if (age >= MAX_AGE || level.getRawBrightness(pos.above(), 0) != 15 || random.nextFloat() >= GROWTH_CHANCE) {
            return;
        }

        BlockState grown = state.setValue(AGE, age + 1);
        level.setBlock(pos, grown, 2);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(grown));
    }

    @Override
    protected void entityInside(
            @NonNull BlockState state,
            @NonNull Level level,
            @NonNull BlockPos pos,
            @NonNull Entity entity,
            @NonNull InsideBlockEffectApplier effectApplier,
            boolean isPrecise) {
        // InfX blueberry bushes are harmless; do not inherit Sweet Berry Bush slowing or damage.
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(
            @NonNull BlockState state,
            @NonNull Level level,
            @NonNull BlockPos pos,
            @NonNull Player player,
            @NonNull BlockHitResult hitResult) {
        if (state.getValue(AGE) != MAX_AGE) {
            return super.useWithoutItem(state, level, pos, player, hitResult);
        }

        if (!level.isClientSide()) {
            popResource(level, pos, InfXItems.BLUEBERRIES.toStack());
            level.playSound(
                    null,
                    pos,
                    SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,
                    SoundSource.BLOCKS,
                    1.0F,
                    0.8F + level.getRandom().nextFloat() * 0.4F);
            BlockState picked = state.setValue(AGE, 0);
            level.setBlock(pos, picked, 2);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, picked));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isValidBonemealTarget(
            @NonNull LevelReader level, @NonNull BlockPos pos, @NonNull BlockState state) {
        // InfX berry bushes cannot be forced with bone meal; they grow only over time.
        return false;
    }

    @Override
    public void performBonemeal(
            @NonNull ServerLevel level, @NonNull RandomSource random, @NonNull BlockPos pos, @NonNull BlockState state) {
        int grownAge = Math.min(MAX_AGE, state.getValue(AGE) + 1 + random.nextInt(2));
        level.setBlock(pos, state.setValue(AGE, grownAge), 2);
    }
}
