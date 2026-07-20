package com.pixulse.infx.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.InfiniteXTestMode;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.minecraft.client.renderer.RenderPipelines;
import com.pixulse.infx.registry.ModAttachments;
import com.pixulse.infx.survival.R196SurvivalRules;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.lwjgl.glfw.GLFW;

/** Six R196 controls plus the debug-profile, sleep and scaled-food interfaces. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID, value = Dist.CLIENT)
public final class R196ClientControls {
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(InfiniteX.id("controls"));
    public static final KeyMapping LOCK_SPRINT = key("lock_sprint", GLFW.GLFW_KEY_CAPS_LOCK, 0);
    public static final KeyMapping ZOOM = key("zoom", GLFW.GLFW_KEY_C, 1);
    public static final KeyMapping RELOAD_CHUNKS = key("reload_chunks", GLFW.GLFW_KEY_R, 2);
    public static final KeyMapping SMART_PICKUP = key("smart_pickup", GLFW.GLFW_KEY_P, 3);
    public static final KeyMapping SMART_USE = key("smart_use", GLFW.GLFW_KEY_U, 4);
    public static final KeyMapping PLACE_FLUID_SOURCE = key("place_fluid_source", GLFW.GLFW_KEY_G, 5);

    private static boolean sprintLocked;
    private static boolean smartPickup;
    private static boolean smartUse;
    private static boolean releaseUseNextTick;
    private static boolean debugConfigured;

    private R196ClientControls() {}

    private static KeyMapping key(String path, int code, int order) {
        return new KeyMapping("key.infx." + path, InputConstants.Type.KEYSYM, code, CATEGORY, order);
    }

    @SubscribeEvent
    private static void registerKeys(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(LOCK_SPRINT);
        event.register(ZOOM);
        event.register(RELOAD_CHUNKS);
        event.register(SMART_PICKUP);
        event.register(SMART_USE);
        event.register(PLACE_FLUID_SOURCE);
    }

    @SubscribeEvent
    private static void clientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) return;
        configureDebugOverlay(minecraft);
        if (releaseUseNextTick) {
            minecraft.options.keyUse.setDown(false);
            releaseUseNextTick = false;
        }
        while (LOCK_SPRINT.consumeClick()) {
            sprintLocked = !sprintLocked;
            overlay(minecraft, "message.infx.sprint_lock", sprintLocked);
        }
        while (SMART_PICKUP.consumeClick()) {
            smartPickup = !smartPickup;
            overlay(minecraft, "message.infx.smart_pickup", smartPickup);
        }
        while (SMART_USE.consumeClick()) {
            smartUse = !smartUse;
            overlay(minecraft, "message.infx.smart_use", smartUse);
        }
        while (RELOAD_CHUNKS.consumeClick()) {
            minecraft.levelRenderer.invalidateCompiledGeometry(
                    minecraft.level,
                    minecraft.options,
                    minecraft.gameRenderer.mainCamera(),
                    minecraft.getBlockColors());
            minecraft.player.sendOverlayMessage(Component.translatable("message.infx.chunks_reloaded"));
        }
        while (PLACE_FLUID_SOURCE.consumeClick()) {
            minecraft.options.keyUse.setDown(true);
            releaseUseNextTick = true;
        }
        if (sprintLocked && !minecraft.player.isShiftKeyDown() && minecraft.player.input.hasForwardImpulse()) {
            minecraft.player.setSprinting(true);
        }
        if (smartPickup && minecraft.options.keyAttack.isDown() && minecraft.hitResult instanceof BlockHitResult hit) {
            chooseBestTool(minecraft, hit);
        }
        if (smartUse && minecraft.options.keyUse.isDown() && minecraft.hitResult instanceof BlockHitResult hit) {
            chooseMatchingBlock(minecraft, hit);
        }
    }

    private static void chooseBestTool(Minecraft minecraft, BlockHitResult hit) {
        BlockState state = minecraft.level.getBlockState(hit.getBlockPos());
        int bestSlot = minecraft.player.getInventory().getSelectedSlot();
        float bestSpeed = minecraft.player.getInventory().getItem(bestSlot).getDestroySpeed(state);
        for (int slot = 0; slot < 9; slot++) {
            float speed = minecraft.player.getInventory().getItem(slot).getDestroySpeed(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = slot;
            }
        }
        minecraft.player.getInventory().setSelectedSlot(bestSlot);
    }

    private static void chooseMatchingBlock(Minecraft minecraft, BlockHitResult hit) {
        BlockState state = minecraft.level.getBlockState(hit.getBlockPos());
        for (int slot = 0; slot < 9; slot++) {
            var stack = minecraft.player.getInventory().getItem(slot);
            if (stack.getItem() instanceof net.minecraft.world.item.BlockItem blockItem
                    && blockItem.getBlock() == state.getBlock()) {
                minecraft.player.getInventory().setSelectedSlot(slot);
                return;
            }
        }
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

    private static void overlay(Minecraft minecraft, String key, boolean enabled) {
        minecraft.player.sendOverlayMessage(Component.translatable(
                key, Component.translatable(enabled ? "options.on" : "options.off")));
    }

    @SubscribeEvent
    private static void zoom(ComputeFovModifierEvent event) {
        if (ZOOM.isDown()) event.setNewFovModifier(event.getNewFovModifier() * .25F);
    }

    @SubscribeEvent
    private static void removeLeaveBedButton(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof InBedChatScreen)) return;
        for (var listener : java.util.List.copyOf(event.getListenersList())) {
            if (listener instanceof Button) event.removeListener(listener);
        }
    }

    @SubscribeEvent
    private static void renderScaledFoodBar(RenderGuiLayerEvent.Pre event) {
        if (!event.getName().equals(VanillaGuiLayers.FOOD_LEVEL)) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.player.isCreative()) return;
        event.setCanceled(true);
        var graphics = event.getGuiGraphics();
        var data = minecraft.player.getData(ModAttachments.SURVIVAL);
        int food = (int) Math.ceil(data.nutrition());
        int slots = (int) Math.ceil(R196SurvivalRules.foodCap(minecraft.player.experienceLevel) / 2.0D);
        int rows = Math.max(1, (slots + 9) / 10);
        int xRight = graphics.guiWidth() / 2 + 91;
        int yBase = graphics.guiHeight() - minecraft.gui.hud.rightHeight;
        var empty = net.minecraft.resources.Identifier.withDefaultNamespace("hud/food_empty");
        var half = net.minecraft.resources.Identifier.withDefaultNamespace("hud/food_half");
        var full = net.minecraft.resources.Identifier.withDefaultNamespace("hud/food_full");
        for (int index = 0; index < slots; index++) {
            int row = index / 10;
            int column = index % 10;
            int x = xRight - column * 8 - 9;
            int y = yBase - row * 10;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, empty, x, y, 9, 9);
            if (index * 2 + 1 < food) graphics.blitSprite(RenderPipelines.GUI_TEXTURED, full, x, y, 9, 9);
            else if (index * 2 + 1 == food) graphics.blitSprite(RenderPipelines.GUI_TEXTURED, half, x, y, 9, 9);
        }
        minecraft.gui.hud.rightHeight += rows * 10;
    }

    static int registeredKeyCount() {
        return 6;
    }
}
