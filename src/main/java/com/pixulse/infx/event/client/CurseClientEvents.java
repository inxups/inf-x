package com.pixulse.infx.event.client;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.data.curse.CurseManager;
import com.pixulse.infx.data.curse.CurseStatus;
import com.pixulse.infx.data.curse.CurseType;
import com.pixulse.infx.registry.InfXMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/** Client projection of realized curse state; pending curses are never visible here. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID, value = Dist.CLIENT)
public final class CurseClientEvents {
    private static final int MAX_EDGE = 28;
    private static final int STATUS_X = 4;
    private static final int STATUS_Y = 4;
    private static final int COMPACT_STATUS_SIZE = 24;
    private static final int COMPACT_ICON_OFFSET = 3;
    private static final int DETAIL_ICON_OFFSET = 7;
    private static final int DETAIL_TEXT_X_OFFSET = 32;
    private static final int DETAIL_TEXT_Y_OFFSET = 7;
    private static final int DETAIL_PADDING = 7;
    private static final int DETAIL_MAX_WIDTH = 240;
    private static final Identifier EFFECT_BACKGROUND_SPRITE =
            Identifier.withDefaultNamespace("hud/effect_background");
    private static final Identifier EFFECT_DETAIL_BACKGROUND_SPRITE =
            Identifier.withDefaultNamespace("container/inventory/effect_background");

    private CurseClientEvents() {}

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        var player = Minecraft.getInstance().player;
        if (player != null
                && player.isSprinting()
                && CurseManager.hasCurse(player, CurseType.CANNOT_RUN)) {
            player.setSprinting(false);
        }
    }

    @SubscribeEvent
    public static void renderVignette(RenderGuiEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !CurseManager.status(minecraft.player).active()) return;

        var graphics = event.getGuiGraphics();
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        int edge = Math.min(MAX_EDGE, Math.max(8, Math.min(width, height) / 10));
        graphics.fill(0, 0, width, height, 0x18000000);
        graphics.fillGradient(0, 0, width, edge, 0xB0000000, 0x00000000);
        graphics.fillGradient(0, height - edge, width, height, 0x00000000, 0xB0000000);
        graphics.fill(0, edge, edge / 2, height - edge, 0x60000000);
        graphics.fill(width - edge / 2, edge, width, height - edge, 0x60000000);
    }

    @SubscribeEvent
    public static void renderStatus(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        CurseStatus status = CurseManager.status(minecraft.player);
        if (!status.active()) return;

        var graphics = event.getGuiGraphics();
        if (minecraft.player.isShiftKeyDown()) {
            renderDetail(graphics, minecraft, status);
        } else {
            renderCompact(graphics);
        }
    }

    private static void renderCompact(GuiGraphicsExtractor graphics) {
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                EFFECT_BACKGROUND_SPRITE,
                STATUS_X,
                STATUS_Y,
                COMPACT_STATUS_SIZE,
                COMPACT_STATUS_SIZE);
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                Gui.getMobEffectSprite(InfXMobEffects.WITCH_CURSE),
                STATUS_X + COMPACT_ICON_OFFSET,
                STATUS_Y + COMPACT_ICON_OFFSET,
                18,
                18);
    }

    private static void renderDetail(GuiGraphicsExtractor graphics, Minecraft minecraft, CurseStatus status) {
        Component title = title(status);
        Component description = description(status);
        int maxWidth = Math.max(
                32,
                Math.min(DETAIL_MAX_WIDTH, graphics.guiWidth() - STATUS_X - DETAIL_PADDING));
        int maxTextWidth = maxWidth - DETAIL_TEXT_X_OFFSET - DETAIL_PADDING;
        var titleLines = minecraft.font.split(title, maxTextWidth);
        var descriptionLines = minecraft.font.split(description, maxTextWidth);
        int textWidth = 0;
        for (var line : titleLines) {
            textWidth = Math.max(textWidth, minecraft.font.width(line));
        }
        for (var line : descriptionLines) {
            textWidth = Math.max(textWidth, minecraft.font.width(line));
        }

        int width = Math.max(32, DETAIL_TEXT_X_OFFSET + textWidth + DETAIL_PADDING);
        int height = Math.max(
                32,
                DETAIL_TEXT_Y_OFFSET + (titleLines.size() + descriptionLines.size()) * minecraft.font.lineHeight + DETAIL_PADDING);
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                EFFECT_DETAIL_BACKGROUND_SPRITE,
                STATUS_X,
                STATUS_Y,
                width,
                height);
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                Gui.getMobEffectSprite(InfXMobEffects.WITCH_CURSE),
                STATUS_X + DETAIL_ICON_OFFSET,
                STATUS_Y + DETAIL_ICON_OFFSET,
                18,
                18);

        int textY = STATUS_Y + DETAIL_TEXT_Y_OFFSET;
        for (var line : titleLines) {
            graphics.text(minecraft.font, line, STATUS_X + DETAIL_TEXT_X_OFFSET, textY, 0xFFFFFFFF);
            textY += minecraft.font.lineHeight;
        }
        for (var line : descriptionLines) {
            graphics.text(minecraft.font, line, STATUS_X + DETAIL_TEXT_X_OFFSET, textY, 0xFFAAAAAA);
            textY += minecraft.font.lineHeight;
        }
    }

    private static Component title(CurseStatus status) {
        Component detail = status.known() && status.type() != null
                ? status.type().title()
                : Component.translatable("curse.infx.unknown");
        return Component.translatable("hud.infx.curse", detail);
    }

    private static Component description(CurseStatus status) {
        CurseType type = status.known() ? status.type() : null;
        return type != null ? type.description() : Component.translatable("curse.infx.unknown");
    }
}
