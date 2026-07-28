package com.pixulse.infx.event.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ServerRulesTest {
    @Test
    void chatScoreAddsTwentyDecaysOneAndKicksOnlyAboveTwoHundred() {
        int score = 0;
        for (int message = 0; message < 10; message++) score = ServerRules.chatScoreAfterMessage(score);
        assertEquals(200, score);
        assertFalse(score > ServerRules.CHAT_THRESHOLD);
        score = ServerRules.chatScoreAfterMessage(score);
        assertTrue(score > ServerRules.CHAT_THRESHOLD);
        assertEquals(219, ServerRules.decayChatScore(score));
    }

    @Test
    void reconnectWindowUsesAdjustedClockGraceAndNextDayLogoutHour() {
        assertEquals(0, ReconnectData.adjustedTime(18_000L));
        assertEquals(6_000, ReconnectData.adjustedTime(0L));
        var restriction = new ReconnectData.Restriction(10_000L, 8, 36_000L);
        assertTrue(restriction.mayReconnect(10_600L, 2_000L));
        assertFalse(restriction.mayReconnect(10_601L, 2_000L));
        assertFalse(restriction.mayReconnect(36_000L, 1_000L));
        assertTrue(restriction.mayReconnect(36_000L, 2_000L));
        assertTrue(restriction.mayReconnect(20_000L, 14_000L));
    }

    @Test
    void reconnectLogoutBeforeSunriseAndAfterLatestClampsToTwenty() {
        ReconnectData data = new ReconnectData();
        UUID player = UUID.randomUUID();

        data.restrict(player, 1_000L, 22_000L);
        var beforeSunrise = data.restriction(player).orElseThrow();
        assertEquals(20, beforeSunrise.adjustedLogoutHour());
        assertEquals(41_000L, beforeSunrise.soonestReconnectTick());

        data.restrict(player, 1_000L, 23_000L);
        var atSunrise = data.restriction(player).orElseThrow();
        assertEquals(5, atSunrise.adjustedLogoutHour());
        assertEquals(25_000L, atSunrise.soonestReconnectTick());

        data.restrict(player, 1_000L, 14_000L);
        assertEquals(20, data.restriction(player).orElseThrow().adjustedLogoutHour());
        data.restrict(player, 1_000L, 15_000L);
        assertEquals(20, data.restriction(player).orElseThrow().adjustedLogoutHour());
    }
}
