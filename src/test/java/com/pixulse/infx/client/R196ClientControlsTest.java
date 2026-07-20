package com.pixulse.infx.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import org.junit.jupiter.api.Test;

class R196ClientControlsTest {
    @Test
    void sixR196ControlsAreRegistered() {
        assertEquals(6, R196ClientControls.registeredKeyCount());
    }

    @Test
    void testModeDisplaysEveryDebugEntryInTheF3Overlay() {
        assertEquals(
                DebugScreenEntryStatus.IN_OVERLAY,
                R196ClientControls.debugStatus(true, DebugScreenEntries.MEMORY));
        assertEquals(
                DebugScreenEntryStatus.IN_OVERLAY,
                R196ClientControls.debugStatus(true, DebugScreenEntries.FPS));
    }

    @Test
    void survivalModeKeepsTheReducedDebugProfile() {
        assertEquals(
                DebugScreenEntryStatus.IN_OVERLAY,
                R196ClientControls.debugStatus(false, DebugScreenEntries.FPS));
        assertEquals(
                DebugScreenEntryStatus.NEVER,
                R196ClientControls.debugStatus(false, DebugScreenEntries.MEMORY));
    }
}
