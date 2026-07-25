package com.pixulse.infx.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class R196CommandsTest {
    @Test
    void commandRosterContainsOnlyDayVillagesAndLivestock() {
        assertEquals(List.of("day", "villages", "infxlivestock"), R196Commands.NAMES);
        assertEquals(3, new HashSet<>(R196Commands.NAMES).size());
    }
}
