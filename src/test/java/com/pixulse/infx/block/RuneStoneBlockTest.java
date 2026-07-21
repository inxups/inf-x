package com.pixulse.infx.block;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RuneStoneBlockTest {
    @Test
    void allSixteenR196RunesHaveStableNamesAndCycle() {
        String[] names = {
            "Nul", "Quas", "Por", "An", "Nox", "Flam", "Vas", "Des",
            "Ort", "Tym", "Corp", "Lor", "Mani", "Jux", "Ylem", "Sanct"
        };
        for (int rune = 0; rune < RuneStoneBlock.RUNE_COUNT; rune++) {
            assertEquals(names[rune], RuneStoneBlock.magicName(rune));
            assertEquals((rune + 1) % RuneStoneBlock.RUNE_COUNT, RuneStoneBlock.nextRune(rune));
        }
    }

    @Test
    void blockStateComponentValueCarriesAndNormalizesTheSelectedRune() {
        assertEquals(15, RuneStoneBlock.itemState(15).get(RuneStoneBlock.RUNE));
        assertEquals(15, RuneStoneBlock.itemState(-1).get(RuneStoneBlock.RUNE));
    }
}
