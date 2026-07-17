package com.pixulse.infx.loot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class FirstLootUnitReplacerTest {
    @Test
    void replacesOnlyOneBaseUnitAndPreservesEveryExtraDrop() {
        MutableStack extraBefore = new MutableStack("extra_before", 2);
        MutableStack base = new MutableStack("gravel", 2);
        MutableStack extraAfter = new MutableStack("extra_after", 4);
        MutableStack replacement = new MutableStack("copper_nugget", 1);
        List<MutableStack> loot = new ArrayList<>(List.of(extraBefore, base, extraAfter));

        boolean replaced = FirstLootUnitReplacer.replace(
                loot,
                stack -> stack.item().equals("gravel") || stack.item().equals("flint"),
                MutableStack::count,
                MutableStack::setCount,
                replacement);

        assertTrue(replaced);
        assertEquals(List.of(extraBefore, replacement, base, extraAfter), loot);
        assertEquals(1, base.count());
        assertEquals(2, extraBefore.count());
        assertEquals(4, extraAfter.count());
    }

    @Test
    void replacesTheWholeEntryWhenTheBaseStackHasOneUnit() {
        MutableStack base = new MutableStack("flint", 1);
        MutableStack replacement = new MutableStack("flint_chip", 1);
        List<MutableStack> loot = new ArrayList<>(List.of(base));

        assertTrue(FirstLootUnitReplacer.replace(
                loot,
                stack -> stack.item().equals("flint"),
                MutableStack::count,
                MutableStack::setCount,
                replacement));
        assertEquals(List.of(replacement), loot);
    }

    @Test
    void leavesLootUntouchedWhenNoVanillaBaseUnitExists() {
        MutableStack extra = new MutableStack("other_mod_drop", 1);
        List<MutableStack> loot = new ArrayList<>(List.of(extra));

        assertFalse(FirstLootUnitReplacer.replace(
                loot,
                stack -> stack.item().equals("gravel"),
                MutableStack::count,
                MutableStack::setCount,
                new MutableStack("copper_nugget", 1)));
        assertEquals(List.of(extra), loot);
    }

    private static final class MutableStack {
        private final String item;
        private int count;

        private MutableStack(String item, int count) {
            this.item = item;
            this.count = count;
        }

        private String item() {
            return item;
        }

        private int count() {
            return count;
        }

        private void setCount(int count) {
            this.count = count;
        }
    }
}
