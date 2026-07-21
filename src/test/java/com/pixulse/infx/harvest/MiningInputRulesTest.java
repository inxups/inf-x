package com.pixulse.infx.harvest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.world.phys.HitResult;
import org.junit.jupiter.api.Test;

class MiningInputRulesTest {
    @Test
    void invalidBlockTargetsUseTheEmptyAttackBranch() {
        assertEquals(
                HitResult.Type.MISS,
                MiningInputRules.attackTargetType(HitResult.Type.BLOCK, false));
        assertEquals(
                HitResult.Type.BLOCK,
                MiningInputRules.attackTargetType(HitResult.Type.BLOCK, true));
        assertEquals(
                HitResult.Type.ENTITY,
                MiningInputRules.attackTargetType(HitResult.Type.ENTITY, false));
        assertEquals(
                HitResult.Type.MISS,
                MiningInputRules.attackTargetType(HitResult.Type.MISS, false));
    }
}
