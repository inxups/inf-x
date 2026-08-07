package com.pixulse.infx.block;

import com.pixulse.infx.block.entity.SafeBlockEntity;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.registry.InfXBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class SafeBlock extends BarrelBlock {
    /** InfX strongbox bounds: 1–15 on X/Z, 0–14 on Y. */
    private static final VoxelShape SHAPE = Shapes.box(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.875D, 0.9375D);

    private final InfxMaterial material;

    public SafeBlock(InfxMaterial material, BlockBehaviour.Properties properties) {
        super(properties);
        this.material = material;
    }

    public InfxMaterial material() {
        return material;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new SafeBlockEntity(pos, state);
    }

    @Override
    public @NonNull BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        return defaultBlockState().setValue(FACING, facing).setValue(OPEN, false);
    }

    @Override
    protected @NonNull VoxelShape getShape(@NonNull BlockState state, @NonNull BlockGetter level, @NonNull BlockPos pos, @NonNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void setPlacedBy(
            @NonNull Level level,
            @NonNull BlockPos pos,
            @NonNull BlockState state,
            @Nullable LivingEntity placer,
            @NonNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer instanceof Player player && level.getBlockEntity(pos) instanceof SafeBlockEntity safe) {
            safe.setOwner(player);
        }
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(
            @NonNull BlockState state, @NonNull Level level, @NonNull BlockPos pos, @NonNull Player player, @NonNull BlockHitResult hit) {
        if (!(level instanceof ServerLevel) || !(level.getBlockEntity(pos) instanceof SafeBlockEntity safe)) {
            return InteractionResult.SUCCESS;
        }
        if (!level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.infx.safe_obstructed"));
            return InteractionResult.FAIL;
        }
        if (!safe.canOpen(player)) {
            player.sendSystemMessage(Component.translatable("message.infx.safe_owned", safe.ownerName()));
            return InteractionResult.FAIL;
        }
        player.openMenu(safe);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void tick(@NonNull BlockState state, ServerLevel level, @NonNull BlockPos pos, @NonNull RandomSource random) {
        if (level.getBlockEntity(pos) instanceof SafeBlockEntity safe) {
            safe.recheckOpen();
        }
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, @NonNull BlockState state, @NonNull BlockEntityType<T> type) {
        return level.isClientSide()
                ? createTickerHelper(
                        type, InfXBlockEntityTypes.SAFE.get(), SafeBlockEntity::lidAnimateTick)
                : null;
    }
}
