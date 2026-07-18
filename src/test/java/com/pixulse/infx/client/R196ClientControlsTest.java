package com.pixulse.infx.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class R196ClientControlsTest {
    @Test
    void sixR196ControlsAreRegistered() {
        assertEquals(6, R196ClientControls.registeredKeyCount());
    }
}
