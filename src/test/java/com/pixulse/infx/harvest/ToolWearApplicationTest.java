package com.pixulse.infx.harvest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class ToolWearApplicationTest {
    @Test
    void appliesCalculatedWearThroughThePostSnapshotBoundary() {
        AtomicInteger appliedDamage = new AtomicInteger();

        ToolWearApplication.afterHarvestSnapshot(1.5F, 1.0F, appliedDamage::set);

        assertEquals(150, appliedDamage.get());
    }

    @Test
    void doesNotInvokeDamageForZeroHardness() {
        AtomicInteger calls = new AtomicInteger();

        ToolWearApplication.afterHarvestSnapshot(0.0F, 1.0F, damage -> calls.incrementAndGet());

        assertEquals(0, calls.get());
    }
}
