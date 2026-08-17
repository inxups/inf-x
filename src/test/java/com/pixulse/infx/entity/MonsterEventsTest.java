package com.pixulse.infx.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.registry.InfXEntityTypes;
import com.pixulse.infx.world.SpawnGate;
import java.time.LocalDate;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import org.junit.jupiter.api.Test;

class MonsterEventsTest {
    @Test
    void dedicatedTorchGoalsUseMiteCadencesAndLightTypes() {
        assertFalse(InfxSeekLitTorchGoal.isSearchDue(39, 40));
        assertTrue(InfxSeekLitTorchGoal.isSearchDue(40, 40));
        assertTrue(InfxSeekLitTorchGoal.isSearchDue(41, 40));
        assertFalse(InfxSeekLitTorchGoal.isSearchDue(199, 200));
        assertTrue(InfxSeekLitTorchGoal.isSearchDue(200, 200));
        assertTrue(InfxSeekLitTorchGoal.isSearchDue(201, 200));
        assertFalse(InfxSeekLitTorchGoal.canCreateGroundPath(false, false));
        assertTrue(InfxSeekLitTorchGoal.canCreateGroundPath(true, false));
        assertTrue(InfxSeekLitTorchGoal.canCreateGroundPath(false, true));
        assertEquals(8, InfxSeekLitTorchGoal.MAX_CANDIDATES);
        assertTrue(InfxSeekLitTorchGoal.isLitTorch(net.minecraft.world.level.block.Blocks.TORCH.defaultBlockState()));
        assertTrue(InfxSeekLitTorchGoal.isLitTorch(net.minecraft.world.level.block.Blocks.REDSTONE_TORCH.defaultBlockState()));
        assertTrue(InfxSeekLitTorchGoal.isLitTorch(net.minecraft.world.level.block.Blocks.JACK_O_LANTERN.defaultBlockState()));
        assertFalse(InfxSeekLitTorchGoal.isLitTorch(net.minecraft.world.level.block.Blocks.LANTERN.defaultBlockState()));
    }

    @Test
    void onlyStructureAndExplicitCreationReplaceWitherSkeletons() {
        assertTrue(SpawnGate.shouldReplaceWitherSkeleton(EntitySpawnReason.STRUCTURE));
        assertTrue(SpawnGate.shouldReplaceWitherSkeleton(EntitySpawnReason.SPAWN_ITEM_USE));
        assertTrue(SpawnGate.shouldReplaceWitherSkeleton(EntitySpawnReason.DISPENSER));
        assertFalse(SpawnGate.shouldReplaceWitherSkeleton(EntitySpawnReason.NATURAL));
        assertFalse(SpawnGate.shouldReplaceWitherSkeleton(EntitySpawnReason.COMMAND));
    }

    @Test
    void worldSpawnReplacementMapsEveryVanillaFishToItsR196Entity() {
        assertEquals(InfXEntityTypes.INFX_BAT.get(), SpawnGate.replacementFor(EntityType.BAT));
        assertEquals(InfXEntityTypes.INFX_COD.get(), SpawnGate.replacementFor(EntityType.COD));
        assertEquals(InfXEntityTypes.INFX_SALMON.get(), SpawnGate.replacementFor(EntityType.SALMON));
        assertEquals(InfXEntityTypes.INFX_PUFFERFISH.get(), SpawnGate.replacementFor(EntityType.PUFFERFISH));
        assertEquals(
                InfXEntityTypes.INFX_TROPICAL_FISH.get(),
                SpawnGate.replacementFor(EntityType.TROPICAL_FISH));
    }

    @Test
    void batHalloweenWindowMatchesR196CalendarDates() {
        assertTrue(SpawnGate.isBatHalloweenWindow(LocalDate.of(2026, 10, 20)));
        assertTrue(SpawnGate.isBatHalloweenWindow(LocalDate.of(2026, 11, 3)));
        assertFalse(SpawnGate.isBatHalloweenWindow(LocalDate.of(2026, 10, 19)));
        assertFalse(SpawnGate.isBatHalloweenWindow(LocalDate.of(2026, 11, 4)));
    }

    @Test
    void followRangeUsesAnInclusiveSphericalBoundary() {
        assertTrue(MonsterEvents.withinFollowRange(16.0 * 16.0, 16.0));
        assertFalse(MonsterEvents.withinFollowRange(16.0 * 16.0 + 0.001, 16.0));
        assertFalse(MonsterEvents.withinFollowRange(17.0 * 17.0 + 17.0 * 17.0, 24.0));
        assertFalse(MonsterEvents.withinFollowRange(0.0, 0.0));
    }
}
