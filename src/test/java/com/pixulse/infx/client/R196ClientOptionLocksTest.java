package com.pixulse.infx.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

class R196ClientOptionLocksTest {
    @Test
    void brightnessIsLockedToTheMoodyMinimum() {
        assertEquals(0.0D, R196ClientOptionLocks.DIM_BRIGHTNESS);
    }

    @Test
    void disablesOnlyTheAllowCommandsSwitchAndForcesItOff() {
        CycleButton<Boolean> allowCommands = booleanSwitch("selectWorld.allowCommands", true);
        CycleButton<Boolean> unrelated = booleanSwitch("options.someOtherToggle", true);

        R196ClientOptionLocks.lockAllowCommandsWidgets(List.of(allowCommands, unrelated));

        assertFalse(allowCommands.getValue());
        assertFalse(allowCommands.active);
        assertTrue(unrelated.getValue());
        assertTrue(unrelated.active);
    }

    private static CycleButton<Boolean> booleanSwitch(String translationKey, boolean value) {
        return CycleButton.onOffBuilder(value)
                .create(Component.translatable(translationKey), (button, selected) -> {});
    }
}
