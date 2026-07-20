package com.pixulse.infx.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.world.entity.player.Player;
import org.junit.jupiter.api.Test;

class PlayerProgressionEventsTest {
    @Test
    void testModeRestoresVanillaInteractionRanges() {
        assertEquals(Player.DEFAULT_BLOCK_INTERACTION_RANGE, PlayerProgressionEvents.blockInteractionRange(true));
        assertEquals(Player.DEFAULT_ENTITY_INTERACTION_RANGE, PlayerProgressionEvents.entityInteractionRange(true));
    }

    @Test
    void survivalModeUsesR196InteractionRanges() {
        assertEquals(2.75, PlayerProgressionEvents.blockInteractionRange(false));
        assertEquals(2.5, PlayerProgressionEvents.entityInteractionRange(false));
    }
}
