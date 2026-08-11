package com.pixulse.infx.event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

class AgricultureEventsTest {
    @Test
    void brownMushroomsGrowAtOneThirdChance() {
        assertEquals(1.0F / 3.0F, AgricultureEvents.mushroomGrowChance(Blocks.BROWN_MUSHROOM.defaultBlockState()));
    }

    @Test
    void redMushroomsGrowAtOneFifthChance() {
        assertEquals(1.0F / 5.0F, AgricultureEvents.mushroomGrowChance(Blocks.RED_MUSHROOM.defaultBlockState()));
    }
}
