package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.config.InfXConfig;
import com.pixulse.infx.registry.InfXEntityTypes;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import org.junit.jupiter.api.Test;

class SpawnerMechanicsTest {
    @Test
    void lifetimeCapIsFifteen() {
        assertEquals(15, SpawnGate.MAX_SPAWNER_KILLS);
        assertFalse(SpawnGate.spawnerExhausted(14));
        assertTrue(SpawnGate.spawnerExhausted(15));
        assertTrue(SpawnGate.spawnerExhausted(16));
    }

    @Test
    void spawnerConfigsDefaultToEnabled() {
        assertTrue(InfXConfig.INSTANCE.mobs.spawnerLifetime.getValue());
        assertTrue(InfXConfig.INSTANCE.mobs.spawnerDepthLayering.getValue());
    }

    @Test
    void surfaceRoomsNeverProduceDeepMobs() {
        // y=63: the depth term is (int)(max(1-63/64,0)*4)=0, so the depth branch caps at 2. Even
        // with jitter the deep tiers (Wight/Demon Spider/Hellhound) are unreachable.
        Set<EntityType<?>> surfaceTypes = new HashSet<>();
        RandomSource random = RandomSource.create(12345);
        for (int i = 0; i < 2000; i++) {
            surfaceTypes.add(SpawnGate.spawnerDepthType(random, 63));
        }
        assertTrue(surfaceTypes.contains(InfXEntityTypes.INFX_ZOMBIE.get()));
        assertTrue(surfaceTypes.contains(InfXEntityTypes.GHOUL.get()));
        assertTrue(surfaceTypes.contains(InfXEntityTypes.INFX_SKELETON.get()));
        assertTrue(surfaceTypes.contains(InfXEntityTypes.INFX_SPIDER.get()));
        assertFalse(surfaceTypes.contains(InfXEntityTypes.WIGHT.get()));
        assertFalse(surfaceTypes.contains(InfXEntityTypes.DEMON_SPIDER.get()));
        assertFalse(surfaceTypes.contains(InfXEntityTypes.HELLHOUND.get()));
    }

    @Test
    void deepRoomsReachWightAtY32AndDemonSpiderAtY16() {
        // y=32: depth term (int)(max(1-32/64,0)*4)=(int)2=2, jitter ±2 -> 0..4 (Wight reachable).
        Set<EntityType<?>> y32 = new HashSet<>();
        RandomSource random32 = RandomSource.create(57005);
        for (int i = 0; i < 4000; i++) {
            y32.add(SpawnGate.spawnerDepthType(random32, 32));
        }
        assertTrue(y32.contains(InfXEntityTypes.WIGHT.get()));

        // y=16: depth term (int)(max(1-16/64,0)*4)=(int)3=3, jitter ±2 -> 1..5 (Demon Spider).
        Set<EntityType<?>> y16 = new HashSet<>();
        RandomSource random16 = RandomSource.create(48879);
        for (int i = 0; i < 4000; i++) {
            y16.add(SpawnGate.spawnerDepthType(random16, 16));
        }
        assertTrue(y16.contains(InfXEntityTypes.DEMON_SPIDER.get()));
    }

    @Test
    void bedrockYReachesHellhound() {
        // y=0: depth term (int)(max(1-0/64,0)*4)=4, jitter ±2 -> 2..6 (Hellhound reachable).
        Set<EntityType<?>> y0 = new HashSet<>();
        RandomSource random = RandomSource.create(51966);
        for (int i = 0; i < 8000; i++) {
            y0.add(SpawnGate.spawnerDepthType(random, 0));
        }
        assertTrue(y0.contains(InfXEntityTypes.HELLHOUND.get()));
    }
}
