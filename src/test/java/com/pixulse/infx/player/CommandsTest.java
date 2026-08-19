package com.pixulse.infx.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.world.StructureGenerationGates;
import com.pixulse.infx.world.StructureGenerationGates.StructureGate;
import com.pixulse.infx.world.StructureGenerationGates.WorldProgressSnapshot;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CommandsTest {
    @Test
    void commandRosterUsesInfxRootWithDayStructureAndXp() {
        assertEquals("infx", InfxCommands.ROOT);
        assertEquals(List.of("infx day", "infx structure", "infx xp"), InfxCommands.NAMES);
        assertEquals(3, new HashSet<>(InfxCommands.NAMES).size());
    }

    @Test
    void dayFromTicksAndTicksForDayAreInverseAndClampToOne() {
        // day 1 is the floor: 0 ticks is still day 1, matching day() = max(1, ...).
        assertEquals(1L, InfxCommands.InfxMonsterDay.dayFromTicks(0L));
        assertEquals(1L, InfxCommands.InfxMonsterDay.dayFromTicks(23_999L));
        assertEquals(5L, InfxCommands.InfxMonsterDay.dayFromTicks(96_000L));
        // ticksForDay is the inverse: day N starts at (N-1)*24000 ticks (dawn of day N).
        assertEquals(0L, InfxCommands.InfxMonsterDay.ticksForDay(1L));
        assertEquals(96_000L, InfxCommands.InfxMonsterDay.ticksForDay(5L));
        // round-trip: setting day N then reading back yields day N.
        assertEquals(5L, InfxCommands.InfxMonsterDay.dayFromTicks(
                InfxCommands.InfxMonsterDay.ticksForDay(5L)));
    }

    @Test
    void xpCommandReportsTotalLevelAndProgress() {
        assertEquals("Experience: total 123; level 3; progress 45%",
                InfxCommands.experienceMessage(123, 3, 0.45F));
    }

    @Test
    void structureListMessageShowsEveryGatedStructureAndItsState() {
        WorldProgressSnapshot progress = new WorldProgressSnapshot(34L, Set.of(), Set.of());
        String message = InfxCommands.structureListMessage(progress);

        for (StructureGate gate : StructureGenerationGates.rules()) {
            assertTrue(message.contains(gate.id().getPath() + ": locked"), gate.id().toString());
        }
        assertEquals(StructureGenerationGates.rules().size(), message.split("\n").length);
        assertFalse(message.contains("unlocked"));
    }

    @Test
    void structureGateMessageShowsUnlockStateAndPerConditionStatus() {
        WorldProgressSnapshot progress = new WorldProgressSnapshot(34L, Set.of(), Set.of());
        StructureGate village = StructureGenerationGates.rule(InfiniteX.id("village")).orElseThrow();

        String message = InfxCommands.structureGateMessage(village, progress);

        assertTrue(message.contains("village (infx:village): locked"));
        assertTrue(message.contains("[✗] Survival day 60 or later (current: 34)"));
        assertTrue(message.contains("[✗] World iron-tier tool crafted"));
    }

    @Test
    void structureGateMessageShowsUnlockedOnceEveryConditionIsMet() {
        WorldProgressSnapshot progress = new WorldProgressSnapshot(
                60L, Set.of(StructureGenerationGates.WorldMilestone.IRON_TOOL_CRAFTED), Set.of());
        StructureGate village = StructureGenerationGates.rule(InfiniteX.id("village")).orElseThrow();

        String message = InfxCommands.structureGateMessage(village, progress);

        assertTrue(message.contains("village (infx:village): unlocked"));
        assertTrue(message.contains("[✓] Survival day 60 or later (current: 60)"));
        assertTrue(message.contains("[✓] World iron-tier tool crafted"));
    }
}
