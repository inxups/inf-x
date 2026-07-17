package com.pixulse.infx.loot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.function.IntUnaryOperator;

import org.junit.jupiter.api.Test;

class GravelDropSelectorTest {
    @Test
    void fortuneZeroThroughThreeChangesOnlyTheOpeningRollBound() {
        assertDrop(GravelDrop.GRAVEL, 0, roll(12, 3));
        assertDrop(GravelDrop.GRAVEL, 1, roll(10, 3));
        assertDrop(GravelDrop.GRAVEL, 2, roll(8, 3));
        assertDrop(GravelDrop.GRAVEL, 3, roll(6, 3));
    }

    @Test
    void fortuneIsClampedToTheSupportedRange() {
        assertDrop(GravelDrop.GRAVEL, -4, roll(12, 3));
        assertDrop(GravelDrop.GRAVEL, 20, roll(6, 3));
    }

    @Test
    void selectsFlintChipBranch() {
        assertDrop(GravelDrop.FLINT_CHIP, 0, roll(12, 0), roll(3, 1), roll(16, 1));
    }

    @Test
    void selectsWholeFlintBranch() {
        assertDrop(GravelDrop.FLINT, 0, roll(12, 0), roll(3, 1), roll(16, 0));
    }

    @Test
    void selectsCopperNuggetBranch() {
        assertDrop(GravelDrop.COPPER_NUGGET, 0, roll(12, 0), roll(3, 0), roll(3, 1));
    }

    @Test
    void selectsSilverNuggetBranch() {
        assertDrop(GravelDrop.SILVER_NUGGET, 0, roll(12, 0), roll(3, 0), roll(3, 0), roll(3, 1));
    }

    @Test
    void selectsGoldNuggetBranch() {
        assertDrop(
                GravelDrop.GOLD_NUGGET,
                0,
                roll(12, 0), roll(3, 0), roll(3, 0), roll(3, 0), roll(3, 1));
    }

    @Test
    void selectsObsidianShardBranch() {
        assertDrop(
                GravelDrop.OBSIDIAN_SHARD,
                0,
                roll(12, 0), roll(3, 0), roll(3, 0), roll(3, 0), roll(3, 0), roll(3, 1));
    }

    @Test
    void selectsEmeraldShardBranch() {
        assertDrop(
                GravelDrop.EMERALD_SHARD,
                0,
                roll(12, 0), roll(3, 0), roll(3, 0), roll(3, 0), roll(3, 0), roll(3, 0), roll(3, 1));
    }

    @Test
    void removedDiamondShardBranchFallsBackToGravel() {
        assertDrop(
                GravelDrop.GRAVEL,
                0,
                roll(12, 0),
                roll(3, 0), roll(3, 0), roll(3, 0), roll(3, 0), roll(3, 0), roll(3, 0), roll(3, 1));
    }

    @Test
    void selectsMithrilNuggetBranch() {
        assertDrop(
                GravelDrop.MITHRIL_NUGGET,
                0,
                roll(12, 0),
                roll(3, 0), roll(3, 0), roll(3, 0), roll(3, 0), roll(3, 0), roll(3, 0), roll(3, 0), roll(3, 1));
    }

    @Test
    void selectsAdamantiumNuggetBranch() {
        assertDrop(
                GravelDrop.ADAMANTIUM_NUGGET,
                0,
                roll(12, 0),
                roll(3, 0), roll(3, 0), roll(3, 0), roll(3, 0), roll(3, 0), roll(3, 0), roll(3, 0), roll(3, 0));
    }

    private static void assertDrop(GravelDrop expected, int fortune, Roll... rolls) {
        ScriptedRandom random = new ScriptedRandom(rolls);
        assertEquals(expected, GravelDropSelector.select(fortune, random));
        assertEquals(0, random.remainingRolls());
    }

    private static Roll roll(int bound, int value) {
        return new Roll(bound, value);
    }

    private record Roll(int bound, int value) {}

    private static final class ScriptedRandom implements IntUnaryOperator {
        private final Queue<Roll> rolls;

        private ScriptedRandom(Roll... rolls) {
            this.rolls = new ArrayDeque<>(Arrays.asList(rolls));
        }

        @Override
        public int applyAsInt(int bound) {
            Roll roll = rolls.remove();
            assertEquals(roll.bound(), bound);
            return roll.value();
        }

        private int remainingRolls() {
            return rolls.size();
        }
    }
}
