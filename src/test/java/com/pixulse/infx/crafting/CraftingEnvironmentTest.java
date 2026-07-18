package com.pixulse.infx.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CraftingEnvironmentTest {
    @Test
    void clumsinessExactlyDoublesCraftingTime() {
        assertEquals(137, CraftingEnvironment.applyClumsiness(137, false));
        assertEquals(274, CraftingEnvironment.applyClumsiness(137, true));
    }
}
