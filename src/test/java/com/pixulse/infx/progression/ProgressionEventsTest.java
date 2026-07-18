package com.pixulse.infx.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ProgressionEventsTest {
    @Test
    void formalEndCompletionRequiresDragonKillCreditsAndReturn() {
        assertTrue(ProgressionEvents.shouldAwardEndReturn(true, true, true));
        assertFalse(ProgressionEvents.shouldAwardEndReturn(false, true, true));
        assertFalse(ProgressionEvents.shouldAwardEndReturn(true, false, true));
        assertFalse(ProgressionEvents.shouldAwardEndReturn(true, true, false));
    }

    @Test
    void nineAuthenticCreationBookTitlesCompleteEnlightenment() {
        int mask = 0;
        for (String title : java.util.List.of(
                "Boat", "Crypt", "Crystal", "Dragon", "Globe", "Serpent", "Sphinx", "Star", "Temple")) {
            int index = ProgressionEvents.creationBookIndex("Father Phoonzang", title);
            assertTrue(index >= 0);
            mask |= 1 << index;
        }
        assertTrue(ProgressionEvents.allCreationBooksRead(mask));
        assertFalse(ProgressionEvents.allCreationBooksRead(mask & ~(1 << 4)));
        assertEquals(-1, ProgressionEvents.creationBookIndex("Player", "Boat"));
    }
}
