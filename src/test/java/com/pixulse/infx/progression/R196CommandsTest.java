package com.pixulse.infx.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import org.junit.jupiter.api.Test;

class R196CommandsTest {
    @Test
    void commandRosterContainsFourteenUniqueMiteQueries() {
        assertEquals(14, R196Commands.NAMES.size());
        assertEquals(14, new HashSet<>(R196Commands.NAMES).size());
    }
}
