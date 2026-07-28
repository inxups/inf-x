package com.pixulse.infx.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import org.joml.Vector3fc;

/** Inventory/item display for metal safes using the non-overlapping safe model. */
public final class SafeSpecialRenderer implements NoDataSpecialModelRenderer {
    public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(Identifier.CODEC.fieldOf("texture").forGetter(Unbaked::texture))
                    .apply(i, Unbaked::new));

    private final SpriteGetter sprites;
    private final SafeModel model;
    private final SpriteId sprite;

    private SafeSpecialRenderer(SpriteGetter sprites, SafeModel model, SpriteId sprite) {
        this.sprites = sprites;
        this.model = model;
        this.sprite = sprite;
    }

    @Override
    public void submit(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            int overlayCoords,
            boolean hasFoil,
            int outlineColor) {
        submitNodeCollector.submitModel(
                model, 0.0F, poseStack, lightCoords, overlayCoords, -1, sprite, sprites, outlineColor, null);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        model.setupAnim(0.0F);
        model.root().getExtentsForGui(poseStack, output);
    }

    public record Unbaked(Identifier texture) implements NoDataSpecialModelRenderer.Unbaked {
        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SafeSpecialRenderer bake(SpecialModelRenderer.BakingContext context) {
            SafeModel model = new SafeModel(context.entityModelSet().bakeLayer(SafeModel.LAYER));
            SpriteId fullTexture = Sheets.CHEST_MAPPER.apply(texture);
            return new SafeSpecialRenderer(context.sprites(), model, fullTexture);
        }
    }
}
