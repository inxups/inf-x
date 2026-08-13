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
import net.neoforged.neoforge.client.event.ScreenEvent;

/** INFX debug-profile and sleep interfaces (all custom hotkeys and HUD layers removed). */
@EventBusSubscriber(modid = InfiniteX.MOD_ID, value = Dist.CLIENT)
public final class ClientControls {

    private static boolean debugConfigured;

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

    static int registeredKeyCount() {
        return 0;
    }
}
