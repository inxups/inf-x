package com.pixulse.infx.curse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.serialization.JsonOps;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class R196CurseDataTest {
    @Test
    void pendingCurseRealizesLearnsAndRemainsUniquePerPlayer() {
        R196CurseData data = new R196CurseData();
        UUID player = UUID.randomUUID();
        UUID witch = UUID.randomUUID();

        assertTrue(data.add(player, witch, R196CurseType.CANNOT_RUN, 6000));
        assertFalse(data.add(player, UUID.randomUUID(), R196CurseType.CLUMSINESS, 7000));
        assertFalse(data.realizeIfDue(player, 5999).orElseThrow().realized());

        var realized = data.realizeIfDue(player, 6000).orElseThrow();
        assertTrue(realized.realized());
        assertFalse(realized.known());
        assertTrue(data.realizeIfDue(player, Long.MAX_VALUE).orElseThrow().realized());

        var learned = data.learn(player).orElseThrow();
        assertTrue(learned.known());
        assertEquals(R196CurseType.CANNOT_RUN, learned.type());
        assertEquals(witch, learned.witch());
    }

    @Test
    void witchDeathRemovesItsPendingAndRealizedCursesOnly() {
        R196CurseData data = new R196CurseData();
        UUID witch = UUID.randomUUID();
        UUID otherWitch = UUID.randomUUID();
        UUID pendingPlayer = UUID.randomUUID();
        UUID realizedPlayer = UUID.randomUUID();
        UUID survivor = UUID.randomUUID();
        data.add(pendingPlayer, witch, R196CurseType.CANNOT_SLEEP, 100);
        data.add(realizedPlayer, witch, R196CurseType.CLUMSINESS, 0);
        data.realizeIfDue(realizedPlayer, 0);
        data.add(survivor, otherWitch, R196CurseType.ENDERMEN_AGGRO, 0);

        var removed = data.removeForWitch(witch);
        assertEquals(2, removed.size());
        assertTrue(removed.containsKey(pendingPlayer));
        assertTrue(removed.containsKey(realizedPlayer));
        assertTrue(data.entry(pendingPlayer).isEmpty());
        assertTrue(data.entry(realizedPlayer).isEmpty());
        assertEquals(otherWitch, data.entry(survivor).orElseThrow().witch());
    }

    @Test
    void codecPersistsPendingRealizedAndKnownState() {
        R196CurseData data = new R196CurseData();
        UUID pendingPlayer = UUID.randomUUID();
        UUID knownPlayer = UUID.randomUUID();
        UUID witch = UUID.randomUUID();
        data.add(pendingPlayer, witch, R196CurseType.CANNOT_DRINK, 9876);
        data.add(knownPlayer, witch, R196CurseType.FEAR_OF_UNDEAD, 100);
        data.realizeIfDue(knownPlayer, 100);
        data.learn(knownPlayer);

        var json = R196CurseData.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow();
        R196CurseData decoded = R196CurseData.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();

        var pending = decoded.entry(pendingPlayer).orElseThrow();
        assertFalse(pending.realized());
        assertFalse(pending.known());
        assertEquals(9876, pending.realizationTick());
        var known = decoded.entry(knownPlayer).orElseThrow();
        assertTrue(known.realized());
        assertTrue(known.known());
        assertEquals(R196CurseType.FEAR_OF_UNDEAD, known.type());
    }
}
