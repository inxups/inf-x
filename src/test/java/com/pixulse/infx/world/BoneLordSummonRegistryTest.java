package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class BoneLordSummonRegistryTest {
    @Test
    void rosterProtectsSixTroopsAndReleasesSlotsByTroopOrLord() {
        BoneLordSummonRegistry registry = new BoneLordSummonRegistry();
        UUID lord = UUID.randomUUID();
        UUID firstTroop = UUID.randomUUID();
        long protectedUntil = 9_600L;
        registry.register(lord, firstTroop, protectedUntil);
        for (int index = 1; index < BoneLordSummonRegistry.MAX_TROOPS_PER_LORD; index++) {
            registry.register(lord, UUID.randomUUID(), protectedUntil);
        }
        registry.register(lord, UUID.randomUUID(), protectedUntil);

        assertEquals(BoneLordSummonRegistry.MAX_TROOPS_PER_LORD, registry.count(lord));
        assertFalse(registry.hasCapacity(lord));
        assertTrue(registry.isTracked(firstTroop));
        assertTrue(registry.isProtected(firstTroop, protectedUntil - 1));
        assertFalse(registry.isProtected(firstTroop, protectedUntil));

        registry.releaseTroop(firstTroop);
        assertEquals(BoneLordSummonRegistry.MAX_TROOPS_PER_LORD - 1, registry.count(lord));
        assertTrue(registry.hasCapacity(lord));
        assertFalse(registry.isTracked(firstTroop));

        registry.releaseLord(lord);
        assertEquals(0, registry.count(lord));
    }
}
