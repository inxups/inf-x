package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SpawnDensityTest {
    @Test
    void strongholdProximityIsZeroInsideTheTwoThousandBlockSpawnRing() {
        assertEquals(0.0F, SpawnDensity.strongholdProximityFromDistances(1_000.0, 500.0), "stronghold too close to world spawn");
        assertEquals(0.0F, SpawnDensity.strongholdProximityFromDistances(1_999.0, 0.0), "ring boundary still yields zero");
    }

    @Test
    void strongholdProximityPeaksAtTheStrongholdAndFadesAtWorldSpawn() {
        assertEquals(1.0F, SpawnDensity.strongholdProximityFromDistances(10_000.0, 0.0), "player at the stronghold");
        assertEquals(0.5F, SpawnDensity.strongholdProximityFromDistances(10_000.0, 5_000.0), "player halfway to the stronghold");
        assertEquals(0.0F, SpawnDensity.strongholdProximityFromDistances(10_000.0, 10_000.0), "player at the world-spawn ring");
    }

    @Test
    void strongholdProximityClampsPastTheStrongholdToZero() {
        assertEquals(0.0F, SpawnDensity.strongholdProximityFromDistances(10_000.0, 15_000.0), "player beyond the stronghold");
    }

    @Test
    void nightWindowMatchesMiteThirteenToTwentyThree() {
        assertTrue(MoonPhase.isNightTime(13_000), "night begins inclusively at 13000");
        assertTrue(MoonPhase.isNightTime(22_999), "night holds until 23000");
        assertFalse(MoonPhase.isNightTime(23_000), "day resumes at 23000");
        assertFalse(MoonPhase.isNightTime(12_999), "still day before 13000");
        assertFalse(MoonPhase.isNightTime(0), "noon is day");
        assertTrue(MoonPhase.isNightTime(13_000 + 24_000), "night wraps across day boundaries");
        assertFalse(MoonPhase.isNightTime(23_000 + 24_000), "day resumes after a full cycle");
    }
}
