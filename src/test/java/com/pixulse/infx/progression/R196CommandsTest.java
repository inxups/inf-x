package com.pixulse.infx.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class R196CommandsTest {
    @Test
    void commandRosterUsesInfxRootWithDayVillagesAndLivestock() {
        assertEquals("infx", R196Commands.ROOT);
        assertEquals(List.of("infx day", "infx villages", "infx livestock"), R196Commands.NAMES);
        assertEquals(3, new HashSet<>(R196Commands.NAMES).size());
    }
}
