package com.pixulse.infx.client;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.InfiniteXTestMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.minecraft.client.renderer.RenderPipelines;
import com.pixulse.infx.registry.InfXAttachments;
import com.pixulse.infx.data.food.SurvivalRules;
import net.minecraft.world.effect.MobEffects;

/** INFX debug-profile, sleep and scaled-food interfaces (all custom hotkeys removed). */
@EventBusSubscriber(modid = InfiniteX.MOD_ID, value = Dist.CLIENT)
public final class ClientControls {

    private static boolean debugConfigured;

    private static final FoodBarSprites NORMAL_FOOD_SPRITES = new FoodBarSprites(
            Identifier.withDefaultNamespace("hud/food_empty"),
            Identifier.withDefaultNamespace("hud/food_half"),
            Identifier.withDefaultNamespace("hud/food_full"));
    private static final FoodBarSprites HUNGER_FOOD_SPRITES = new FoodBarSprites(
            Identifier.withDefaultNamespace("hud/food_empty_hunger"),
            Identifier.withDefaultNamespace("hud/food_half_hunger"),
            Identifier.withDefaultNamespace("hud/food_full_hunger"));

    private ClientControls() {}

    @SubscribeEvent
    public static void clientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) return;
        configureDebugOverlay(minecraft);
    }

    private static void configureDebugOverlay(Minecraft minecraft) {
        if (debugConfigured) return;
        boolean testMode = InfiniteXTestMode.isEnabled();
        if (testMode) {
            if (!minecraft.debugEntries.isUsingProfile(DebugScreenProfile.DEFAULT)) {
                minecraft.debugEntries.loadProfile(DebugScreenProfile.DEFAULT);
            }
            debugConfigured = true;
            return;
        }
        for (var id : DebugScreenEntries.allEntries().keySet()) {
            DebugScreenEntryStatus status = debugStatus(false, id);
            if (minecraft.debugEntries.getStatus(id) != status) minecraft.debugEntries.setStatus(id, status);
        }
        debugConfigured = true;
    }

    static DebugScreenEntryStatus debugStatus(boolean testMode, Identifier id) {
        if (testMode) {
            return DebugScreenEntries.PROFILES.get(DebugScreenProfile.DEFAULT)
                    .getOrDefault(id, DebugScreenEntryStatus.NEVER);
        }
        return id.equals(DebugScreenEntries.FPS)
                ? DebugScreenEntryStatus.IN_OVERLAY
                : DebugScreenEntryStatus.NEVER;
    }

    @SubscribeEvent
    public static void removeLeaveBedButton(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof InBedChatScreen)) return;
        for (var listener : java.util.List.copyOf(event.getListenersList())) {
            if (listener instanceof Button) event.removeListener(listener);
        }
    }

    @SubscribeEvent
    public static void renderScaledFoodBar(RenderGuiLayerEvent.Pre event) {
        if (!event.getName().equals(VanillaGuiLayers.FOOD_LEVEL)) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null
                || !shouldRenderFoodBar(minecraft.player.isCreative(), minecraft.player.isSpectator())) return;
        event.setCanceled(true);
        var graphics = event.getGuiGraphics();
        var data = minecraft.player.getData(InfXAttachments.SURVIVAL);
        int food = (int) Math.ceil(data.nutrition());
        int slots = (int) Math.ceil(SurvivalRules.foodCap(minecraft.player.experienceLevel) / 2.0D);
        int rows = Math.max(1, (slots + 9) / 10);
        int xRight = graphics.guiWidth() / 2 + 91;
        int yBase = graphics.guiHeight() - minecraft.gui.rightHeight;
        FoodBarSprites sprites = foodBarSprites(minecraft.player.hasEffect(MobEffects.HUNGER));
        for (int index = 0; index < slots; index++) {
            int row = index / 10;
            int column = index % 10;
            int x = xRight - column * 8 - 9;
            int y = yBase - row * 10;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprites.empty(), x, y, 9, 9);
            if (index * 2 + 1 < food) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprites.full(), x, y, 9, 9);
            } else if (index * 2 + 1 == food) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprites.half(), x, y, 9, 9);
            }
        }
        minecraft.gui.rightHeight += rows * 10;
    }

    static FoodBarSprites foodBarSprites(boolean hunger) {
        return hunger ? HUNGER_FOOD_SPRITES : NORMAL_FOOD_SPRITES;
    }

    record FoodBarSprites(Identifier empty, Identifier half, Identifier full) {}

    static int registeredKeyCount() {
        return 0;
    }

    static boolean shouldRenderFoodBar(boolean creative, boolean spectator) {
        return !creative && !spectator;
    }
}
