package com.pixulse.infx.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pixulse.infx.registry.ModEntityTypes;
import net.minecraft.world.entity.EntityTypes;
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

    @Test
    void worldSpawnReplacementMapsEveryVanillaFishToItsR196Entity() {
        assertEquals(ModEntityTypes.R196_COD.get(), R196MonsterEvents.replacementFor(EntityTypes.COD));
        assertEquals(ModEntityTypes.R196_SALMON.get(), R196MonsterEvents.replacementFor(EntityTypes.SALMON));
        assertEquals(ModEntityTypes.R196_PUFFERFISH.get(), R196MonsterEvents.replacementFor(EntityTypes.PUFFERFISH));
        assertEquals(
                ModEntityTypes.R196_TROPICAL_FISH.get(),
                R196MonsterEvents.replacementFor(EntityTypes.TROPICAL_FISH));
    }
}
