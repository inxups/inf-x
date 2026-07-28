package com.pixulse.infx.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.block.entity.SafeBlockEntity;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Placed safes use a non-overlapping chest model so metal lid/body seams do not z-fight.
 * Chunk meshes stay particle-only; this BER is the only world geometry.
 */
public final class SafeRenderer implements BlockEntityRenderer<SafeBlockEntity, ChestRenderState> {
    private final SpriteGetter sprites;
    private final SafeModel model;

    public SafeRenderer(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
        this.model = new SafeModel(context.bakeLayer(SafeModel.LAYER));
    }

    @Override
    public ChestRenderState createRenderState() {
        return new ChestRenderState();
    }

    @Override
    public void extractRenderState(
            SafeBlockEntity blockEntity,
            ChestRenderState state,
            float partialTicks,
            @NonNull Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(blockEntity, state, breakProgress);
        state.type = ChestType.SINGLE;
        Direction facing = blockEntity.getBlockState().getValue(BarrelBlock.FACING);
        state.facing = facing.getAxis().isHorizontal() ? facing : Direction.NORTH;
        state.open = blockEntity.getOpenNess(partialTicks);
        state.material = ChestRenderState.ChestMaterialType.REGULAR;
        state.customSprite = Sheets.CHEST_MAPPER.apply(
                InfiniteX.id(blockEntity.materialBlock().material().path()));
    }

    @Override
    public void submit(
            ChestRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            @NonNull CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.mulPose(ChestRenderer.modelTransformation(state.facing));
        float open = state.open;
        open = 1.0F - open;
        open = 1.0F - open * open * open;
        SpriteId spriteId = state.customSprite != null
                ? state.customSprite
                : Sheets.chooseSprite(state.material, state.type);
        submitNodeCollector.submitModel(
                model,
                open,
                poseStack,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                -1,
                spriteId,
                sprites,
                0,
                state.breakProgress);
        poseStack.popPose();
    }

    @Override
    public net.minecraft.world.phys.@NonNull AABB getRenderBoundingBox(SafeBlockEntity blockEntity) {
        net.minecraft.core.BlockPos pos = blockEntity.getBlockPos();
        return net.minecraft.world.phys.AABB.encapsulatingFullBlocks(pos.offset(-1, 0, -1), pos.offset(1, 1, 1));
    }
}
