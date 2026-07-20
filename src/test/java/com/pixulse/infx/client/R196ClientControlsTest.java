package com.pixulse.infx.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import org.junit.jupiter.api.Test;

class R196ClientControlsTest {
    @Test
    void sixR196ControlsAreRegistered() {
        assertEquals(6, R196ClientControls.registeredKeyCount());
    }

    @Test
    void testModeUsesTheVanillaDefaultDebugProfile() {
        assertEquals(
                DebugScreenEntryStatus.IN_OVERLAY,
                R196ClientControls.debugStatus(true, DebugScreenEntries.MEMORY));
        assertEquals(
                DebugScreenEntryStatus.IN_OVERLAY,
                R196ClientControls.debugStatus(true, DebugScreenEntries.PLAYER_POSITION));
        assertEquals(
                DebugScreenEntryStatus.IN_OVERLAY,
                R196ClientControls.debugStatus(true, DebugScreenEntries.THREE_DIMENSIONAL_CROSSHAIR));
        assertEquals(
                DebugScreenEntryStatus.NEVER,
                R196ClientControls.debugStatus(true, DebugScreenEntries.BIOME));
        assertEquals(
                DebugScreenEntryStatus.NEVER,
                R196ClientControls.debugStatus(true, DebugScreenEntries.LOOKING_AT_BLOCK_TAGS));
        assertEquals(
                DebugScreenEntryStatus.NEVER,
                R196ClientControls.debugStatus(true, DebugScreenEntries.CHUNK_BORDERS));
        assertEquals(
                DebugScreenEntryStatus.NEVER,
                R196ClientControls.debugStatus(true, DebugScreenEntries.VISUALIZE_CHUNKS_ON_SERVER));
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

    @Test
    void foodBarOnlyRendersForSurvivalPlayers() {
        assertTrue(R196ClientControls.shouldRenderFoodBar(false, false));
        assertFalse(R196ClientControls.shouldRenderFoodBar(true, false));
        assertFalse(R196ClientControls.shouldRenderFoodBar(false, true));
    }
}
