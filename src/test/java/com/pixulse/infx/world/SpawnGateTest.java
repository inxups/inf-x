package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.registry.InfXEntityTypes;
import java.time.LocalDate;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.junit.jupiter.api.Test;

class SpawnGateTest {
    @Test
    void hostileCapCeilingCapsMonsterCategoryToFifty() {
        assertEquals(50, SpawnGate.hostileCapCeiling(MobCategory.MONSTER, 70), "MITE caps hostiles at 50");
        assertEquals(70, SpawnGate.hostileCapCeiling(MobCategory.CREATURE, 70), "creatures keep the vanilla cap");
    }

    @Test
    void vanillaWitchCancellationKeepsTheExplicitAllowSet() {
        assertFalse(SpawnGate.shouldCancelVanillaWitch(EntitySpawnReason.STRUCTURE));
        assertFalse(SpawnGate.shouldCancelVanillaWitch(EntitySpawnReason.COMMAND));
        assertFalse(SpawnGate.shouldCancelVanillaWitch(EntitySpawnReason.SPAWN_ITEM_USE));
        assertFalse(SpawnGate.shouldCancelVanillaWitch(EntitySpawnReason.DISPENSER));
        assertFalse(SpawnGate.shouldCancelVanillaWitch(EntitySpawnReason.LOAD));
        assertTrue(SpawnGate.shouldCancelVanillaWitch(EntitySpawnReason.NATURAL));
        assertTrue(SpawnGate.shouldCancelVanillaWitch(EntitySpawnReason.SPAWNER));
        assertTrue(SpawnGate.shouldCancelVanillaWitch(EntitySpawnReason.PATROL));
    }

    @Test
    void worldSpawnReasonsAreOnlyWorldDrivenOnes() {
        assertTrue(SpawnGate.isWorldSpawn(EntitySpawnReason.NATURAL));
        assertTrue(SpawnGate.isWorldSpawn(EntitySpawnReason.CHUNK_GENERATION));
        assertTrue(SpawnGate.isWorldSpawn(EntitySpawnReason.SPAWNER));
        assertTrue(SpawnGate.isWorldSpawn(EntitySpawnReason.STRUCTURE));
        assertTrue(SpawnGate.isWorldSpawn(EntitySpawnReason.REINFORCEMENT));
        assertTrue(SpawnGate.isWorldSpawn(EntitySpawnReason.PATROL));
        assertTrue(SpawnGate.isWorldSpawn(EntitySpawnReason.TRIAL_SPAWNER));
        assertFalse(SpawnGate.isWorldSpawn(EntitySpawnReason.COMMAND));
        assertFalse(SpawnGate.isWorldSpawn(EntitySpawnReason.SPAWN_ITEM_USE));
        assertFalse(SpawnGate.isWorldSpawn(EntitySpawnReason.LOAD));
    }

    @Test
    void onlyStructureAndExplicitCreationReplaceNetherWitherSkeletons() {
        assertTrue(SpawnGate.shouldReplaceWitherSkeleton(EntitySpawnReason.STRUCTURE));
        assertTrue(SpawnGate.shouldReplaceWitherSkeleton(EntitySpawnReason.SPAWN_ITEM_USE));
        assertTrue(SpawnGate.shouldReplaceWitherSkeleton(EntitySpawnReason.DISPENSER));
        assertFalse(SpawnGate.shouldReplaceWitherSkeleton(EntitySpawnReason.NATURAL));
        assertFalse(SpawnGate.shouldReplaceWitherSkeleton(EntitySpawnReason.COMMAND));
    }

    @Test
    void longdeadGuardianReplacementWaitsForNaturalUnderworldRollZero() {
        assertTrue(SpawnGate.shouldReplaceLongdeadWithGuardian(
                InfXEntityTypes.LONGDEAD.get(), com.pixulse.infx.world.Underworld.LEVEL,
                EntitySpawnReason.NATURAL, 0));
        assertFalse(SpawnGate.shouldReplaceLongdeadWithGuardian(
                InfXEntityTypes.LONGDEAD.get(), com.pixulse.infx.world.Underworld.LEVEL,
                EntitySpawnReason.NATURAL, 1));
        assertFalse(SpawnGate.shouldReplaceLongdeadWithGuardian(
                InfXEntityTypes.LONGDEAD.get(), com.pixulse.infx.world.Underworld.LEVEL,
                EntitySpawnReason.SPAWNER, 0));
        assertThrows(IllegalArgumentException.class, () -> SpawnGate.shouldReplaceLongdeadWithGuardian(
                InfXEntityTypes.LONGDEAD.get(), com.pixulse.infx.world.Underworld.LEVEL,
                EntitySpawnReason.NATURAL, -1));
        assertThrows(IllegalArgumentException.class, () -> SpawnGate.shouldReplaceLongdeadWithGuardian(
                InfXEntityTypes.LONGDEAD.get(), com.pixulse.infx.world.Underworld.LEVEL,
                EntitySpawnReason.NATURAL, 6));
    }

    @Test
    void replacementForCoversEveryMappedVanillaSpawn() {
        assertEquals(InfXEntityTypes.INFX_BAT.get(), SpawnGate.replacementFor(EntityType.BAT));
        assertEquals(InfXEntityTypes.INFX_SKELETON.get(), SpawnGate.replacementFor(EntityType.SKELETON));
        assertEquals(InfXEntityTypes.INFX_SPIDER.get(), SpawnGate.replacementFor(EntityType.SPIDER));
        assertEquals(InfXEntityTypes.INFX_CREEPER.get(), SpawnGate.replacementFor(EntityType.CREEPER));
        assertEquals(InfXEntityTypes.INFX_SLIME.get(), SpawnGate.replacementFor(EntityType.SLIME));
        assertEquals(InfXEntityTypes.INFX_ENDERMAN.get(), SpawnGate.replacementFor(EntityType.ENDERMAN));
        assertEquals(InfXEntityTypes.INFX_WITCH.get(), SpawnGate.replacementFor(EntityType.WITCH));
        assertEquals(InfXEntityTypes.INFX_WOLF.get(), SpawnGate.replacementFor(EntityType.WOLF));
        assertNull(SpawnGate.replacementFor(EntityType.ZOMBIE), "zombies keep the vanilla entity and MITE events");
    }

    @Test
    void replacementFamilyGroupsSharedCanonicalTypes() {
        assertTrue(SpawnGate.sameSpawnFamily(EntityType.SPIDER, InfXEntityTypes.INFX_SPIDER.get()), "vanilla and infx spider share the family");
        assertFalse(SpawnGate.sameSpawnFamily(EntityType.SPIDER, EntityType.CAVE_SPIDER), "spider and cave spider are distinct variants");
        assertFalse(SpawnGate.sameSpawnFamily(EntityType.SPIDER, EntityType.ZOMBIE), "spiders and zombies are different families");
    }

    @Test
    void batHalloweenWindowMatchesR196CalendarDates() {
        assertTrue(SpawnGate.isBatHalloweenWindow(LocalDate.of(2026, 10, 20)));
        assertTrue(SpawnGate.isBatHalloweenWindow(LocalDate.of(2026, 11, 3)));
        assertFalse(SpawnGate.isBatHalloweenWindow(LocalDate.of(2026, 10, 19)));
        assertFalse(SpawnGate.isBatHalloweenWindow(LocalDate.of(2026, 11, 4)));
    }
}