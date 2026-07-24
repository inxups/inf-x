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

    @Test
    void meleeUsesR196PositiveBonusAndNegativeDebtPenalty() {
        assertEquals(1.0F, PlayerProgressionEvents.meleeMultiplier(0), 0.0001F);
        assertEquals(1.05F, PlayerProgressionEvents.meleeMultiplier(10), 0.0001F);
        assertEquals(2.0F, PlayerProgressionEvents.meleeMultiplier(200), 0.0001F);
        assertEquals(0.98F, PlayerProgressionEvents.meleeMultiplier(-1), 0.0001F);
        assertEquals(0.2F, PlayerProgressionEvents.meleeMultiplier(-40), 0.0001F);
    }
}
