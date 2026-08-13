package com.pixulse.infx.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import org.junit.jupiter.api.Test;

class ClientControlsTest {
    @Test
    void noCustomHotkeysAreRegistered() {
        assertEquals(0, ClientControls.registeredKeyCount());
    }

    @Test
    void testModeUsesTheVanillaDefaultDebugProfile() {
        assertEquals(
                DebugScreenEntryStatus.IN_OVERLAY,
                ClientControls.debugStatus(true, DebugScreenEntries.MEMORY));
        assertEquals(
                DebugScreenEntryStatus.IN_OVERLAY,
                ClientControls.debugStatus(true, DebugScreenEntries.PLAYER_POSITION));
        assertEquals(
                DebugScreenEntryStatus.IN_OVERLAY,
                ClientControls.debugStatus(true, DebugScreenEntries.THREE_DIMENSIONAL_CROSSHAIR));
        assertEquals(
                DebugScreenEntryStatus.NEVER,
                ClientControls.debugStatus(true, DebugScreenEntries.BIOME));
        assertEquals(
                DebugScreenEntryStatus.NEVER,
                ClientControls.debugStatus(true, DebugScreenEntries.LOOKING_AT_BLOCK_TAGS));
        assertEquals(
                DebugScreenEntryStatus.NEVER,
                ClientControls.debugStatus(true, DebugScreenEntries.CHUNK_BORDERS));
        assertEquals(
                DebugScreenEntryStatus.NEVER,
                ClientControls.debugStatus(true, DebugScreenEntries.VISUALIZE_CHUNKS_ON_SERVER));
    }

    @Test
    void survivalModeKeepsTheReducedDebugProfile() {
        assertEquals(
                DebugScreenEntryStatus.IN_OVERLAY,
                ClientControls.debugStatus(false, DebugScreenEntries.FPS));
        assertEquals(
                DebugScreenEntryStatus.NEVER,
                ClientControls.debugStatus(false, DebugScreenEntries.MEMORY));
    }
}
