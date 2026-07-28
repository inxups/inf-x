package com.pixulse.infx.effect.curse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayDeque;
import java.util.Random;
import org.junit.jupiter.api.Test;

class CurseTypeTest {
    @Test
    void idsRoundTripAllSixteenCurses() {
        assertEquals(16, CurseType.values().length);
        for (CurseType type : CurseType.values()) {
            assertEquals(type, CurseType.byId(type.id()));
            assertNotNull(type.title());
            assertNotNull(type.description());
        }
    }

    @Test
    void originalSelectionRejectsEmptySlotsInTheSixtyFourEntryTable() {
        var random = new SequenceRandom(63, 0, 17, 16);
        assertEquals(CurseType.FEAR_OF_UNDEAD, CurseType.random(random));
        assertEquals(4, random.calls);
    }

    @Test
    void witchSeedAndOriginalUsernameProduceStableTypes() {
        assertEquals(1076, CurseType.originalUsernameHash("Steve"));
        assertEquals(CurseType.CANNOT_WEAR_ARMOR, CurseType.forWitch(123456789, "Steve"));
        assertEquals(CurseType.CANNOT_EAT_ANIMALS, CurseType.forWitch(123456789, "Alex"));
        assertEquals(CurseType.CANNOT_HOLD_BREATH, CurseType.forWitch(-42, "Steve"));
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
