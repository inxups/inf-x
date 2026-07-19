package com.pixulse.infx.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.components.tabs.MenuTabBar;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

class WorldCreationLockClientEventsTest {
    @Test
    void disablesLockedControlsButLeavesWorldStructureToggleAvailable() {
        Button gameMode = button("game mode");
        TestTab game = new TestTab("createWorld.tab.game.title", gameMode);

        WorldCreationUiState.WorldTypeEntry customType = new WorldCreationUiState.WorldTypeEntry(null);
        CycleButton<WorldCreationUiState.WorldTypeEntry> worldType = CycleButton
                .builder(value -> Component.literal("type"), customType)
                .withValues(customType)
                .create(Component.literal("world type"), (button, value) -> {});
        Button customize = Button.builder(Component.translatable("selectWorld.customizeType"), button -> {}).build();
        CycleButton<Boolean> structures = booleanSwitch(true);
        CycleButton<Boolean> bonusChest = booleanSwitch(false);
        TestTab world = new TestTab("createWorld.tab.world.title", worldType, customize, structures, bonusChest);

        Button gameRules = button("game rules");
        Button experiments = button("experiments");
        Button dataPacks = button("data packs");
        TestTab more = new TestTab("createWorld.tab.more.title", gameRules, experiments, dataPacks);
        MenuTabBar tabBar = MenuTabBar.builder(new TabManager(widget -> {}, widget -> {}), 320)
                .addTabs(game, world, more)
                .build();

        WorldCreationLockClientEvents.lockWidgets(tabBar);

        assertFalse(gameMode.active);
        assertFalse(worldType.active);
        assertFalse(customize.active);
        assertTrue(structures.active);
        assertFalse(bonusChest.active);
        assertFalse(gameRules.active);
        assertFalse(experiments.active);
        assertFalse(dataPacks.active);
    }

    private static Button button(String name) {
        return Button.builder(Component.literal(name), button -> {}).build();
    }

    private static CycleButton<Boolean> booleanSwitch(boolean value) {
        return CycleButton.onOffBuilder(value)
                .displayOnlyValue()
                .create(Component.empty(), (button, selected) -> {});
    }

    private static final class TestTab extends GridLayoutTab {
        private TestTab(String titleKey, AbstractWidget... widgets) {
            super(Component.translatable(titleKey));
            var rows = this.layout.createRowHelper(1);
            for (AbstractWidget widget : widgets) rows.addChild(widget);
        }
    }
}
