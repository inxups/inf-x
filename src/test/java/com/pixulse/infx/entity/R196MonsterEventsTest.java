package com.pixulse.infx.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class R196MonsterEventsTest {
    @Test
    void lightSearchPhasesSpreadFreshMobsAcrossTheInterval() {
        for (int tick = 0; tick < 80; tick++) {
            int searches = 0;
            for (int entityId = 0; entityId < 80; entityId++) {
                if (R196MonsterEvents.shouldSearchForLight(tick, entityId)) searches++;
            }
            assertEquals(1, searches, "exactly one sequential fresh mob must scan on each tick");
        }
    }

    @Test
    void lightSearchKeepsOneScanPerMobEveryInterval() {
        for (int entityId : new int[] {0, 1, 79, 80, 12_345}) {
            int searches = 0;
            for (int tick = 0; tick < 80; tick++) {
                if (R196MonsterEvents.shouldSearchForLight(tick, entityId)) searches++;
            }
            assertEquals(1, searches, "each mob must retain its original 80-tick scan cadence");
        }
    }
}
