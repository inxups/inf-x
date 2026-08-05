package com.pixulse.infx.harvest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.data.harvest.MiteBlockHardness;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class MiteBlockHardnessTest {
    @Test
    void locksMitesObsidianHardness() {
        assertEquals(8.0F, MiteBlockHardness.OBSIDIAN_HARDNESS);
    }

    @Test
    void mapsCobwebAndBothObsidianBlocks() {
        for (String path : new String[] {"cobweb", "obsidian", "crying_obsidian"}) {
            Identifier id = Identifier.withDefaultNamespace(path);
            assertTrue(MiteBlockHardness.appliesTo(id), path + " must use MITE obsidian hardness");
            assertEquals(MiteBlockHardness.OBSIDIAN_HARDNESS, MiteBlockHardness.destroyTime(id), path);
        }
    }

    @Test
    void leavesOtherBlocksAndNamespacesUnmapped() {
        assertFalse(MiteBlockHardness.appliesTo(Identifier.withDefaultNamespace("stone")));
        assertFalse(MiteBlockHardness.appliesTo(Identifier.fromNamespaceAndPath("infx", "obsidian")));
    }
}
