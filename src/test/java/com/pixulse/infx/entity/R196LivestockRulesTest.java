package com.pixulse.infx.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.world.R196MoonPhase;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import org.junit.jupiter.api.Test;

class R196LivestockRulesTest {
    @Test
    void everyNeedIsRequiredForHealthyProduction() {
        assertTrue(R196Livestock.healthy(true, true, true, true, true, true, false, false));
        assertFalse(R196Livestock.healthy(false, true, true, true, true, true, false, false));
        assertFalse(R196Livestock.healthy(true, false, true, true, true, true, false, false));
        assertFalse(R196Livestock.healthy(true, true, false, true, true, true, false, false));
        assertFalse(R196Livestock.healthy(true, true, true, true, true, true, true, false));
        assertFalse(R196Livestock.healthy(true, true, true, true, true, true, false, true));
    }

    @Test
    void wellFlagMatchesProductiveHealthForSickSkins() {
        // MITE isWell gates sick textures; infx maps that to healthy && !diseased.
        assertTrue(R196Livestock.healthy(true, true, true, true, true, true, false, false));
        assertFalse(R196Livestock.healthy(true, true, true, true, true, true, false, true));
    }

    @Test
    void lunarCalendarKeepsBloodAndBlueMoonReplacementRules() {
        assertEquals(R196MoonPhase.FULL, R196MoonPhase.atDay(1));
        assertEquals(R196MoonPhase.NEW, R196MoonPhase.atDay(5));
        assertEquals(R196MoonPhase.BLOOD, R196MoonPhase.atDay(32));
        assertEquals(R196MoonPhase.BLUE, R196MoonPhase.atDay(128));
        assertEquals(R196MoonPhase.YELLOW, R196MoonPhase.atDay(24));
        assertEquals(R196MoonPhase.PHANTOM, R196MoonPhase.atDay(120));
    }

    @Test
    void horseFailureCooldownIsExactlyTwoHundredSeconds() {
        assertEquals(4_000L, R196Livestock.horseRetryTicks());
    }

    @Test
    void unhealthyAdultChickenCannotLayAnEggOnTheNextTick() {
        assertTrue(R196Chicken.shouldDelayEgg(true, false, false, 1));
        assertFalse(R196Chicken.shouldDelayEgg(true, false, true, 1));
        assertFalse(R196Chicken.shouldDelayEgg(false, false, false, 1));
        assertFalse(R196Chicken.shouldDelayEgg(true, true, false, 1));
        assertFalse(R196Chicken.shouldDelayEgg(true, false, false, 2));
    }

    @Test
    void foodIsRecordedOnlyForSuccessfulServerInteractions() {
        assertTrue(R196Livestock.foodInteractionSucceeded(true, InteractionResult.SUCCESS_SERVER));
        assertTrue(R196Livestock.foodInteractionSucceeded(true, InteractionResult.SUCCESS));
        assertFalse(R196Livestock.foodInteractionSucceeded(true, InteractionResult.CONSUME));
        assertFalse(R196Livestock.foodInteractionSucceeded(true, InteractionResult.PASS));
        assertFalse(R196Livestock.foodInteractionSucceeded(false, InteractionResult.SUCCESS_SERVER));
    }

    @Test
    void needSearchRangeExpandsAsHungerOrThirstDeepens() {
        assertEquals(16, R196Livestock.searchRange(24_001L, 24_000L));
        assertEquals(32, R196Livestock.searchRange(48_000L, 24_000L));
        assertEquals(48, R196Livestock.searchRange(72_000L, 24_000L));
    }

    @Test
    void waterGoalPrefersReachablePathsAndRejectsStationaryPartials() {
        BlockPos target = new BlockPos(4, 0, 0);
        Path empty = new Path(List.of(), target, false);
        Path stationary = new Path(List.of(new Node(0, 0, 0)), target, false);
        Path partial = new Path(List.of(new Node(0, 0, 0), new Node(2, 0, 0)), target, false);
        Path reached = new Path(List.of(new Node(0, 0, 0), new Node(4, 0, 0)), target, true);

        assertFalse(R196Livestock.NeedsGoal.hasNavigableNodes(empty));
        assertTrue(R196Livestock.NeedsGoal.hasNavigableNodes(partial));
        assertFalse(R196Livestock.NeedsGoal.isUsefulPath(stationary, BlockPos.ZERO));
        assertTrue(R196Livestock.NeedsGoal.isUsefulPath(partial, BlockPos.ZERO));
        assertSame(
                reached,
                R196Livestock.NeedsGoal.preferredPath(List.of(partial, reached), BlockPos.ZERO));
    }
}
