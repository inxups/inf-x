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
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Renders each safe as a single vanilla 26.2 chest; no double-chest state is exposed. */
final class R196SafeRenderer extends ChestRenderer<R196SafeBlockEntity> {
    R196SafeRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    /**
     * Vanilla's chest renderer reads {@code ChestBlock.FACING}, which is a
     * horizontal-only property. Safes intentionally retain the barrel's
     * six-way facing property, so extract the common chest state here.
     */
    @Override
    public void extractRenderState(
            R196SafeBlockEntity blockEntity,
            ChestRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(blockEntity, state, breakProgress);
        state.type = ChestType.SINGLE;
        state.facing = blockEntity.getBlockState().getValue(BarrelBlock.FACING);
        state.open = blockEntity.getOpenNess(partialTicks);
        state.customSprite = getCustomSprite(blockEntity, state);
    }

    @Override
    protected SpriteId getCustomSprite(R196SafeBlockEntity blockEntity, ChestRenderState renderState) {
        return Sheets.CHEST_MAPPER.apply(InfiniteX.id(blockEntity.materialBlock().material().path()));
    }
}
