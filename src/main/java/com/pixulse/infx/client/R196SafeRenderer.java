package com.pixulse.infx.client;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.block.entity.R196SafeBlockEntity;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * World safes use the same chest BER as vanilla. Facing comes from the barrel property
 * (horizontal placement only) rather than {@code ChestBlock.FACING}.
 */
final class R196SafeRenderer extends ChestRenderer<R196SafeBlockEntity> {
    R196SafeRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void extractRenderState(
            R196SafeBlockEntity blockEntity,
            ChestRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(blockEntity, state, breakProgress);
        state.type = ChestType.SINGLE;
        Direction facing = blockEntity.getBlockState().getValue(BarrelBlock.FACING);
        state.facing = facing.getAxis().isHorizontal() ? facing : Direction.NORTH;
        state.open = blockEntity.getOpenNess(partialTicks);
        state.customSprite = getCustomSprite(blockEntity, state);
    }

    @Override
    protected SpriteId getCustomSprite(R196SafeBlockEntity blockEntity, ChestRenderState renderState) {
        return Sheets.CHEST_MAPPER.apply(InfiniteX.id(blockEntity.materialBlock().material().path()));
    }
}
