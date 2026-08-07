package com.pixulse.infx.event.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NightwingClientEventsTest {
    private static final float EPSILON = 1.0E-6F;

    @Test
    void receivedNightwingDimmingUsesPacketMaximumSemantics() {
        assertEquals(1.25F, NightwingClientEvents.applyIncomingDimming(0.4F, 1.25F), EPSILON);
        assertEquals(1.25F, NightwingClientEvents.applyIncomingDimming(1.25F, 0.625F), EPSILON);
    }

    @Test
    void clientDimmingFadesAtOneHundredthPerTickAndCapsAtTwo() {
        assertEquals(0.99F, NightwingClientEvents.decayDimming(1.0F), EPSILON);
        assertEquals(0.0F, NightwingClientEvents.decayDimming(0.009F), EPSILON);
        assertEquals(2.0F, NightwingClientEvents.decayDimming(2.5F), EPSILON);
    }
}
