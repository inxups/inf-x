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
    void wellnessUsesTheMiteMinimumThreshold() {
        assertTrue(R196Livestock.isWell(0.25F, 0.25F, 0.25F));
        assertFalse(R196Livestock.isWell(0.249F, 1.0F, 1.0F));
        assertFalse(R196Livestock.isWell(1.0F, 0.249F, 1.0F));
        assertFalse(R196Livestock.isWell(1.0F, 1.0F, 0.249F));
    }

    @Test
    void wellnessGainsAndLossesMatchMiteRates() {
        assertEquals(0.9F, R196Livestock.adjustNeed(0.8F, true), 1.0E-6F);
        assertEquals(0.795F, R196Livestock.adjustNeed(0.8F, false), 1.0E-6F);
        assertEquals(1.0F, R196Livestock.adjustNeed(0.95F, true), 1.0E-6F);
        assertEquals(0.0F, R196Livestock.adjustNeed(0.0F, false), 1.0E-6F);
    }

    @Test
    void onlyExtremeHungerStopsMiteManure() {
        assertFalse(R196Livestock.isDesperateForFood(0.05F));
        assertTrue(R196Livestock.isDesperateForFood(0.049F));
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
    void needSearchRangeExpandsWithTheMiteWellnessThresholds() {
        assertEquals(16, R196Livestock.searchRange(0.5F));
        assertEquals(32, R196Livestock.searchRange(0.249F));
        assertEquals(48, R196Livestock.searchRange(0.049F));
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
