package com.pixulse.infx.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class R196CommandsTest {
    @Test
    void commandRosterUsesInfxRootWithDayAndVillages() {
        assertEquals("infx", R196Commands.ROOT);
        assertEquals(List.of("infx day", "infx villages"), R196Commands.NAMES);
        assertEquals(2, new HashSet<>(R196Commands.NAMES).size());
    }
}
