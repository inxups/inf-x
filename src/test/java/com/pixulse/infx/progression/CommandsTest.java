package com.pixulse.infx.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class CommandsTest {
    @Test
    void commandRosterUsesInfxRootWithDayAndVillages() {
        assertEquals("infx", MiteCommands.ROOT);
        assertEquals(List.of("infx day", "infx villages"), MiteCommands.NAMES);
        assertEquals(2, new HashSet<>(MiteCommands.NAMES).size());
    }
}
