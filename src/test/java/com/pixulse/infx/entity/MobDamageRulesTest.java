package com.pixulse.infx.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;

import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

class MobDamageRulesTest {
    @Test
    void emptyStackIsNeverAStoneMiningTool() {
        assertFalse(MobDamageRules.isStoneMiningTool(ItemStack.EMPTY));
    }
}
