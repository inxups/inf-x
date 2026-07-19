package com.pixulse.infx.client;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.world.WorldCreationLockProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.tabs.MenuTabBar;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

/** Applies and visually locks the InfiniteX single-player world creation profile. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID, value = Dist.CLIENT)
public final class WorldCreationLockClientEvents {
    private static final String GAME_TAB = "createWorld.tab.game.title";
    private static final String WORLD_TAB = "createWorld.tab.world.title";
    private static final String MORE_TAB = "createWorld.tab.more.title";
    private static final String CUSTOMIZE_WORLD_TYPE = "selectWorld.customizeType";

    private WorldCreationLockClientEvents() {}

    @SubscribeEvent
    private static void lockWorldCreation(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof CreateWorldScreen screen)) return;
        MenuTabBar tabBar = event.getListenersList().stream()
                .filter(MenuTabBar.class::isInstance)
                .map(MenuTabBar.class::cast)
                .findFirst()
                .orElse(null);
        if (tabBar == null) return;

        WorldCreationUiState state = screen.getUiState();
        enforceProfile(state);
        lockWidgets(tabBar);
        state.addListener(ignored -> {
            enforceProfile(state);
            lockWidgets(tabBar);
        });
    }

    static void enforceProfile(WorldCreationUiState state) {
        if (state.getGameMode() != WorldCreationUiState.SelectedGameMode.SURVIVAL
                || state.getGameMode().gameType != WorldCreationLockProfile.GAME_TYPE) {
            state.setGameMode(WorldCreationUiState.SelectedGameMode.SURVIVAL);
        }
        var lockedDifficulty = WorldCreationLockProfile.difficulty();
        if (state.getDifficulty() != lockedDifficulty) {
            state.setDifficulty(lockedDifficulty);
        }
        if (state.isAllowCommands() != WorldCreationLockProfile.ALLOW_COMMANDS) {
            state.setAllowCommands(WorldCreationLockProfile.ALLOW_COMMANDS);
        }
        if (state.isBonusChest() != WorldCreationLockProfile.BONUS_CHEST) {
            state.setBonusChest(WorldCreationLockProfile.BONUS_CHEST);
        }
        if (!state.getWorldType().isAmplified()) {
            Holder<WorldPreset> amplified = state.getSettings()
                    .worldgenLoadContext()
                    .lookupOrThrow(Registries.WORLD_PRESET)
                    .get(WorldCreationLockProfile.WORLD_PRESET)
                    .orElseThrow(() -> new IllegalStateException("Amplified world preset is unavailable"));
            state.setWorldType(new WorldCreationUiState.WorldTypeEntry(amplified));
        }
    }

    static void lockWidgets(MenuTabBar tabBar) {
        findTab(tabBar, GAME_TAB).ifPresent(tab -> tab.visitChildren(widget -> {
            if (!(widget instanceof EditBox)) widget.active = false;
        }));
        findTab(tabBar, WORLD_TAB).ifPresent(WorldCreationLockClientEvents::lockWorldWidgets);
        if (!WorldCreationLockProfile.ALLOW_ADVANCED_CONFIGURATION) {
            findTab(tabBar, MORE_TAB).ifPresent(tab -> tab.visitChildren(widget -> widget.active = false));
        }
    }

    private static void lockWorldWidgets(Tab tab) {
        List<CycleButton<?>> switches = new ArrayList<>();
        tab.visitChildren(widget -> {
            if (widget instanceof CycleButton<?> cycleButton) {
                if (cycleButton.getValue() instanceof WorldCreationUiState.WorldTypeEntry) {
                    widget.active = false;
                } else if (cycleButton.getValue() instanceof Boolean) {
                    switches.add(cycleButton);
                }
            } else if (widget instanceof Button && hasTranslationKey(widget.getMessage(), CUSTOMIZE_WORLD_TYPE)) {
                widget.active = false;
            }
        });
        if (switches.size() >= 2) {
            // Vanilla adds "Generate Structures" first and "Bonus Chest" second.
            switches.get(1).active = false;
        }
    }

    private static Optional<Tab> findTab(MenuTabBar tabBar, String titleKey) {
        return tabBar.getTabs().stream()
                .filter(tab -> hasTranslationKey(tab.getTabTitle(), titleKey))
                .findFirst();
    }

    private static boolean hasTranslationKey(Component component, String key) {
        return component.getContents() instanceof TranslatableContents contents && contents.getKey().equals(key);
    }
}
