package com.pixulse.infx.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.world.MoonPhase;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import org.junit.jupiter.api.Test;

class LivestockRulesTest {
    @Test
    void wellnessUsesTheMinimumThreshold() {
        assertTrue(Livestock.isWell(0.25F, 0.25F, 0.25F));
        assertFalse(Livestock.isWell(0.249F, 1.0F, 1.0F));
        assertFalse(Livestock.isWell(1.0F, 0.249F, 1.0F));
        assertFalse(Livestock.isWell(1.0F, 1.0F, 0.249F));
    }

    @Test
    void wellnessGainsAndLossesMatchRates() {
        assertEquals(0.9F, Livestock.adjustNeed(0.8F, true), 1.0E-6F);
        assertEquals(0.795F, Livestock.adjustNeed(0.8F, false), 1.0E-6F);
        assertEquals(1.0F, Livestock.adjustNeed(0.95F, true), 1.0E-6F);
        assertEquals(0.0F, Livestock.adjustNeed(0.0F, false), 1.0E-6F);
    }

    @Test
    void onlyExtremeHungerStopsManure() {
        assertFalse(Livestock.isDesperateForFood(0.05F));
        assertTrue(Livestock.isDesperateForFood(0.049F));
    }

    @Test
    void lunarCalendarKeepsBloodAndBlueMoonReplacementRules() {
        assertEquals(MoonPhase.FULL, MoonPhase.atDay(8));
        assertEquals(MoonPhase.NEW, MoonPhase.atDay(4));
        assertEquals(MoonPhase.NORMAL, MoonPhase.atDay(1));
        assertEquals(MoonPhase.BLOOD, MoonPhase.atDay(32));
        assertEquals(MoonPhase.BLUE, MoonPhase.atDay(128));
        assertEquals(MoonPhase.YELLOW, MoonPhase.atDay(24));
        assertEquals(MoonPhase.PHANTOM, MoonPhase.atDay(120));
    }

    @Test
    void horseFailureCooldownIsExactlyTwoHundredSeconds() {
        assertEquals(4_000L, Livestock.horseRetryTicks());
    }

    @Test
    void unhealthyAdultChickenCannotLayAnEggOnTheNextTick() {
        assertTrue(InfxChicken.shouldDelayEgg(true, false, false, 1));
        assertFalse(InfxChicken.shouldDelayEgg(true, false, true, 1));
        assertFalse(InfxChicken.shouldDelayEgg(false, false, false, 1));
        assertFalse(InfxChicken.shouldDelayEgg(true, true, false, 1));
        assertFalse(InfxChicken.shouldDelayEgg(true, false, false, 2));
    }

    @Test
    void foodIsRecordedOnlyForSuccessfulServerInteractions() {
        assertTrue(Livestock.foodInteractionSucceeded(true, InteractionResult.SUCCESS_SERVER));
        assertTrue(Livestock.foodInteractionSucceeded(true, InteractionResult.SUCCESS));
        assertFalse(Livestock.foodInteractionSucceeded(true, InteractionResult.CONSUME));
        assertFalse(Livestock.foodInteractionSucceeded(true, InteractionResult.PASS));
        assertFalse(Livestock.foodInteractionSucceeded(false, InteractionResult.SUCCESS_SERVER));
    }

    @Test
    void needSearchRangeExpandsWithTheWellnessThresholds() {
        assertEquals(16, Livestock.searchRange(0.5F));
        assertEquals(32, Livestock.searchRange(0.249F));
        assertEquals(48, Livestock.searchRange(0.049F));
    }

    @Test
    void panicIsActiveOnlyBeforeItsDeadline() {
        assertTrue(Livestock.isPanicActive(101L, 100L));
        assertFalse(Livestock.isPanicActive(100L, 100L));
        assertFalse(Livestock.isPanicActive(99L, 100L));
    }

    @Test
    void repeatedPanicCannotShortenTheCurrentDeadline() {
        assertEquals(800L, Livestock.extendPanicUntil(800L, 600L));
        assertEquals(800L, Livestock.extendPanicUntil(600L, 800L));
    }

    @Test
    void panicIncreasesMovementSpeedByFiftyPercent() {
        assertEquals(0.30D, Livestock.panicMovementSpeed(0.20D), 1.0E-9D);
    }

    @Test
    void waterGoalPrefersReachablePathsAndRejectsStationaryPartials() {
        BlockPos target = new BlockPos(4, 0, 0);
        Path empty = new Path(List.of(), target, false);
        Path stationary = new Path(List.of(new Node(0, 0, 0)), target, false);
        Path partial = new Path(List.of(new Node(0, 0, 0), new Node(2, 0, 0)), target, false);
        Path reached = new Path(List.of(new Node(0, 0, 0), new Node(4, 0, 0)), target, true);

        assertFalse(Livestock.NeedsGoal.hasNavigableNodes(empty));
        assertTrue(Livestock.NeedsGoal.hasNavigableNodes(partial));
        assertFalse(Livestock.NeedsGoal.isUsefulPath(stationary, BlockPos.ZERO));
        assertTrue(Livestock.NeedsGoal.isUsefulPath(partial, BlockPos.ZERO));
        assertSame(
                reached,
                Livestock.NeedsGoal.preferredPath(List.of(partial, reached), BlockPos.ZERO));
    }
}
