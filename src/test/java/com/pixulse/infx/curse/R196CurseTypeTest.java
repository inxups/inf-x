package com.pixulse.infx.curse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayDeque;
import java.util.Random;
import org.junit.jupiter.api.Test;

class R196CurseTypeTest {
    @Test
    void idsRoundTripAllSixteenCurses() {
        assertEquals(16, R196CurseType.values().length);
        for (R196CurseType type : R196CurseType.values()) {
            assertEquals(type, R196CurseType.byId(type.id()));
            assertNotNull(type.title());
            assertNotNull(type.description());
        }
    }

    @Test
    void originalSelectionRejectsEmptySlotsInTheSixtyFourEntryTable() {
        var random = new SequenceRandom(63, 0, 17, 16);
        assertEquals(R196CurseType.FEAR_OF_UNDEAD, R196CurseType.random(random));
        assertEquals(4, random.calls);
    }

    @Test
    void witchSeedAndOriginalUsernameProduceStableTypes() {
        assertEquals(1076, R196CurseType.originalUsernameHash("Steve"));
        assertEquals(R196CurseType.CANNOT_WEAR_ARMOR, R196CurseType.forWitch(123456789, "Steve"));
        assertEquals(R196CurseType.CANNOT_EAT_ANIMALS, R196CurseType.forWitch(123456789, "Alex"));
        assertEquals(R196CurseType.CANNOT_HOLD_BREATH, R196CurseType.forWitch(-42, "Steve"));
    }

    private static final class SequenceRandom extends Random {
        private final ArrayDeque<Integer> values = new ArrayDeque<>();
        private int calls;

        private SequenceRandom(int... values) {
            for (int value : values) this.values.add(value);
        }

        @Override
        public int nextInt(int bound) {
            assertEquals(64, bound);
            calls++;
            return values.removeFirst();
        }
    }
}
