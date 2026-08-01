package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.Difficulty;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

class InfXFireSpreadRulesTest {
    @Test
    void miteRatesCoverLegacyFireMaterials() {
        assertEquals(5, InfXFireSpreadRules.chanceToEncourageFire(Blocks.OAK_PLANKS));
        assertEquals(20, InfXFireSpreadRules.abilityToCatchFire(Blocks.OAK_PLANKS));
        assertEquals(30, InfXFireSpreadRules.chanceToEncourageFire(Blocks.OAK_LEAVES));
        assertEquals(60, InfXFireSpreadRules.abilityToCatchFire(Blocks.OAK_LEAVES));
        assertEquals(60, InfXFireSpreadRules.chanceToEncourageFire(Blocks.HAY_BLOCK));
        assertEquals(100, InfXFireSpreadRules.abilityToCatchFire(Blocks.HAY_BLOCK));
        assertEquals(60, InfXFireSpreadRules.chanceToEncourageFire(Blocks.COBWEB));
        assertEquals(100, InfXFireSpreadRules.abilityToCatchFire(Blocks.COBWEB));
        assertEquals(30, InfXFireSpreadRules.chanceToEncourageFire(Blocks.SUGAR_CANE));
        assertEquals(50, InfXFireSpreadRules.abilityToCatchFire(Blocks.SUGAR_CANE));
        assertEquals(30, InfXFireSpreadRules.chanceToEncourageFire(Blocks.BUSH));
        assertEquals(50, InfXFireSpreadRules.abilityToCatchFire(Blocks.BUSH));
    }

    @Test
    void miteSpreadArithmeticUsesIntegerBoundaries() {
        assertEquals(100, InfXFireSpreadRules.airSpreadDenominator(-1));
        assertEquals(100, InfXFireSpreadRules.airSpreadDenominator(1));
        assertEquals(200, InfXFireSpreadRules.airSpreadDenominator(2));
        assertEquals(400, InfXFireSpreadRules.airSpreadDenominator(4));
        assertEquals(3, InfXFireSpreadRules.airSpreadOdds(60, 0, Difficulty.NORMAL, false));
        assertEquals(1, InfXFireSpreadRules.airSpreadOdds(60, 0, Difficulty.NORMAL, true));
        assertEquals(300, InfXFireSpreadRules.directCatchDenominator(net.minecraft.core.Direction.EAST, false));
        assertEquals(250, InfXFireSpreadRules.directCatchDenominator(net.minecraft.core.Direction.UP, false));
        assertEquals(250, InfXFireSpreadRules.directCatchDenominator(net.minecraft.core.Direction.EAST, true));
        assertEquals(200, InfXFireSpreadRules.directCatchDenominator(net.minecraft.core.Direction.UP, true));
    }

    @Test
    void miteAgeRollsAreBoundedAndInherited() {
        assertEquals(4, InfXFireSpreadRules.nextAge(4, 0));
        assertEquals(4, InfXFireSpreadRules.nextAge(4, 1));
        assertEquals(5, InfXFireSpreadRules.nextAge(4, 2));
        assertEquals(15, InfXFireSpreadRules.nextAge(15, 2));
        assertEquals(4, InfXFireSpreadRules.inheritedAge(4, 3));
        assertEquals(5, InfXFireSpreadRules.inheritedAge(4, 4));
        assertEquals(15, InfXFireSpreadRules.inheritedAge(15, 4));
        assertTrue(InfXFireSpreadRules.rates().containsKey(Blocks.TNT));
    }
}
