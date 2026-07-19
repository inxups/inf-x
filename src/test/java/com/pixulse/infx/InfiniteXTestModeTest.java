package com.pixulse.infx;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class InfiniteXTestModeTest {
    @Test
    void onlyAnExplicitTrueValueEnablesTestMode() {
        assertTrue(InfiniteXTestMode.parse("true"));
        assertTrue(InfiniteXTestMode.parse("TRUE"));
        assertFalse(InfiniteXTestMode.parse(null));
        assertFalse(InfiniteXTestMode.parse("false"));
        assertFalse(InfiniteXTestMode.parse("1"));
    }
}
