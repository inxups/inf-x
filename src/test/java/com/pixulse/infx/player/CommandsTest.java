package com.pixulse.infx.player;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class CommandsTest {
    @Test
    void commandRosterUsesInfxRootWithDayVillagesAndXp() {
        assertEquals("infx", InfxCommands.ROOT);
        assertEquals(List.of("infx day", "infx villages", "infx xp"), InfxCommands.NAMES);
        assertEquals(3, new HashSet<>(InfxCommands.NAMES).size());
    }

    @Test
    void xpCommandReportsTotalLevelAndProgress() {
        assertEquals("Experience: total 123; level 3; progress 45%",
                InfxCommands.experienceMessage(123, 3, 0.45F));
    }
}
