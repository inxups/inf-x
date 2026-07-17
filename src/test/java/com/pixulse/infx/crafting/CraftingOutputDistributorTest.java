package com.pixulse.infx.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class CraftingOutputDistributorTest {
    @Test
    void successfulInventoryInsertionDoesNotDropTheResult() {
        List<String> dropped = new ArrayList<>();

        CraftingOutputDistributor.giveOrDrop("result", result -> true, dropped::add);

        assertEquals(List.of(), dropped);
    }

    @Test
    void fullInventoryDropsTheResultAtThePlayer() {
        List<String> dropped = new ArrayList<>();

        CraftingOutputDistributor.giveOrDrop("result", result -> false, dropped::add);

        assertEquals(List.of("result"), dropped);
    }
}
