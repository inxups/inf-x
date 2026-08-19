package com.pixulse.infx.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.equipment.ShieldModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

/**
 * Shield special renderer that draws its base plate from a per-material texture instead of the
 * shared vanilla {@link Sheets#SHIELD_BASE}. Mirrors {@link net.minecraft.client.renderer.special.ShieldSpecialRenderer}
 * exactly; the only difference is that the two base {@link SpriteId}s ({@code base} for the
 * banner-capable plate, {@code noPattern} for the plain plate) are resolved per shield via
 * {@link Sheets#SHIELD_MAPPER#apply} rather than the hardcoded {@link Sheets#SHIELD_BASE}.
 *
 * <p>The shield atlas uses a {@code minecraft:directory} sprite source on {@code entity/shield/}
 * (see {@code assets/minecraft/atlases/shield_patterns.json}); that source aggregates textures
 * across namespaces, so the InfX textures under {@code assets/infx/textures/entity/shield/} are
 * registered automatically — the same mechanism that already loads InfX zombie/creeper textures.
 */
public final class InfXShieldSpecialRenderer implements net.minecraft.client.renderer.special.SpecialModelRenderer<DataComponentMap> {
    public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                    Identifier.CODEC.fieldOf("base").forGetter(Unbaked::base),
                    Identifier.CODEC.fieldOf("no_pattern").forGetter(Unbaked::noPattern))
                    .apply(i, Unbaked::new));

    private final SpriteGetter sprites;
    private final ShieldModel model;
    private final SpriteId base;
    private final SpriteId noPattern;

    private InfXShieldSpecialRenderer(
            SpriteGetter sprites, ShieldModel model, SpriteId base, SpriteId noPattern) {
        this.sprites = sprites;
        this.model = model;
        this.base = base;
        this.noPattern = noPattern;
    }

    @Override
    public @Nullable DataComponentMap extractArgument(ItemStack stack) {
        return stack.immutableComponents();
    }

    @Override
    public void submit(
            @Nullable DataComponentMap components,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            int overlayCoords,
            boolean hasFoil,
            int outlineColor) {
        BannerPatternLayers patterns = components != null
                ? components.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
                : BannerPatternLayers.EMPTY;
        DyeColor baseColor = components != null ? components.get(DataComponents.BASE_COLOR) : null;
        boolean hasPatterns = !patterns.layers().isEmpty() || baseColor != null;
        SpriteId base = hasPatterns ? this.base : this.noPattern;
        submitNodeCollector.submitModel(
                this.model, Unit.INSTANCE, poseStack, lightCoords, overlayCoords, -1, base, this.sprites, outlineColor, null);
        if (hasPatterns) {
            BannerRenderer.submitPatterns(
                    this.sprites,
                    poseStack,
                    submitNodeCollector,
                    lightCoords,
                    overlayCoords,
                    this.model,
                    Unit.INSTANCE,
                    false,
                    Objects.requireNonNullElse(baseColor, DyeColor.WHITE),
                    patterns,
                    null);
        }
        if (hasFoil) {
            submitNodeCollector.submitModel(
                    this.model,
                    Unit.INSTANCE,
                    poseStack,
                    RenderTypes.entityGlint(),
                    lightCoords,
                    overlayCoords,
                    -1,
                    this.sprites.get(base),
                    0,
                    null);
        }
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        this.model.root().getExtentsForGui(poseStack, output);
    }

    public record Unbaked(Identifier base, Identifier noPattern)
            implements net.minecraft.client.renderer.special.SpecialModelRenderer.Unbaked<DataComponentMap> {
        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public InfXShieldSpecialRenderer bake(net.minecraft.client.renderer.special.SpecialModelRenderer.BakingContext context) {
            ShieldModel model = new ShieldModel(context.entityModelSet().bakeLayer(ModelLayers.SHIELD));
            return new InfXShieldSpecialRenderer(
                    context.sprites(),
                    model,
                    Sheets.SHIELD_MAPPER.apply(base),
                    Sheets.SHIELD_MAPPER.apply(noPattern));
        }
    }
}
