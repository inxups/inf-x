package com.pixulse.infx.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.data.food.SurvivalData;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.resources.Identifier;
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

    @Test
    void foodBarOnlyRendersForSurvivalPlayers() {
        assertTrue(ClientControls.shouldRenderFoodBar(false, false));
        assertFalse(ClientControls.shouldRenderFoodBar(true, false));
        assertFalse(ClientControls.shouldRenderFoodBar(false, true));
    }

    @Test
    void hungerUsesTheDedicatedFoodSprites() {
        ClientControls.FoodBarSprites normal = ClientControls.foodBarSprites(false);
        assertEquals(Identifier.withDefaultNamespace("hud/food_empty"), normal.empty());
        assertEquals(Identifier.withDefaultNamespace("hud/food_half"), normal.half());
        assertEquals(Identifier.withDefaultNamespace("hud/food_full"), normal.full());

        ClientControls.FoodBarSprites hunger = ClientControls.foodBarSprites(true);
        assertEquals(Identifier.withDefaultNamespace("hud/food_empty_hunger"), hunger.empty());
        assertEquals(Identifier.withDefaultNamespace("hud/food_half_hunger"), hunger.half());
        assertEquals(Identifier.withDefaultNamespace("hud/food_full_hunger"), hunger.full());
    }

    @Test
    void starvingPlayersShowAFullyEmptyBar() {
        assertEquals(10, ClientControls.foodBarFood(new SurvivalData(8, 9.4, 1, 1, 1, 0, 0)));
        assertEquals(1, ClientControls.foodBarFood(new SurvivalData(0, 0.5, 1, 1, 1, 0, 0)));
        assertEquals(0, ClientControls.foodBarFood(new SurvivalData(5, 0.00005, 1, 1, 1, 0, 0)));
        assertEquals(0, ClientControls.foodBarFood(new SurvivalData(0, 0, 1, 1, 1, 0, 0)));
    }
}
