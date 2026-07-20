package com.pixulse.infx.client;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.InfiniteXTestMode;
import com.pixulse.infx.world.WorldCreationLockProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.client.gui.screens.options.WorldOptionsScreen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

/** Locks survival-only client and integrated-world options outside test mode. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID, value = Dist.CLIENT)
public final class R196ClientOptionLocks {
    static final double DIM_BRIGHTNESS = 0.0D;
    private static final String ALLOW_COMMANDS = "selectWorld.allowCommands";

    private R196ClientOptionLocks() {}

    @SubscribeEvent
    private static void enforceLockedValues(ClientTickEvent.Post event) {
        if (InfiniteXTestMode.isEnabled()) return;
        Minecraft minecraft = Minecraft.getInstance();
        enforceBrightness(minecraft);
        enforceAllowCommands(minecraft);
    }

    @SubscribeEvent
    private static void lockOptionScreens(ScreenEvent.Init.Post event) {
        if (InfiniteXTestMode.isEnabled()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (event.getScreen() instanceof VideoSettingsScreen) {
            enforceBrightness(minecraft);
            lockBrightnessWidget(minecraft.options.gamma(), event.getListenersList());
        } else if (event.getScreen() instanceof WorldOptionsScreen) {
            enforceAllowCommands(minecraft);
            lockAllowCommandsWidgets(event.getListenersList());
        }
    }

    private static void enforceBrightness(Minecraft minecraft) {
        OptionInstance<Double> brightness = minecraft.options.gamma();
        if (Double.compare(brightness.get(), DIM_BRIGHTNESS) == 0) return;
        brightness.set(DIM_BRIGHTNESS);
        minecraft.options.save();
    }

    private static void enforceAllowCommands(Minecraft minecraft) {
        IntegratedServer server = minecraft.getSingleplayerServer();
        if (server == null) return;
        if (server.getWorldData().isAllowCommands() != WorldCreationLockProfile.ALLOW_COMMANDS) {
            server.setWorldAllowCommands(WorldCreationLockProfile.ALLOW_COMMANDS);
        }
        if (server.commandsAllowedForOtherPlayers() != WorldCreationLockProfile.ALLOW_COMMANDS) {
            server.setCommandsAllowedForOtherPlayers(WorldCreationLockProfile.ALLOW_COMMANDS);
        }
    }

    private static void lockBrightnessWidget(
            OptionInstance<Double> brightness, Iterable<? extends GuiEventListener> listeners) {
        for (GuiEventListener listener : listeners) {
            if (!(listener instanceof OptionsList optionsList)) continue;
            AbstractWidget widget = optionsList.findOption(brightness);
            if (widget == null) continue;
            optionsList.resetOption(brightness);
            widget.active = false;
        }
    }

    static void lockAllowCommandsWidgets(Iterable<? extends GuiEventListener> listeners) {
        for (GuiEventListener listener : listeners) {
            if (!(listener instanceof CycleButton<?> button)
                    || !(button.getValue() instanceof Boolean)
                    || !hasTranslationKey(button.getMessage(), ALLOW_COMMANDS)) {
                continue;
            }
            lockBooleanButton(button);
        }
    }

    @SuppressWarnings("unchecked")
    private static void lockBooleanButton(CycleButton<?> button) {
        CycleButton<Boolean> booleanButton = (CycleButton<Boolean>) button;
        booleanButton.setValue(WorldCreationLockProfile.ALLOW_COMMANDS);
        booleanButton.active = false;
    }

    private static boolean hasTranslationKey(Component component, String key) {
        if (component.getContents() instanceof TranslatableContents contents) {
            if (contents.getKey().equals(key)) return true;
            for (Object argument : contents.getArgs()) {
                if (argument instanceof Component nested && hasTranslationKey(nested, key)) return true;
            }
        }
        return component.getSiblings().stream().anyMatch(sibling -> hasTranslationKey(sibling, key));
    }
}
