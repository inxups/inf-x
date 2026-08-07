package com.pixulse.infx.harvest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.data.harvest.BlockHardness;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class BlockHardnessTest {
    @Test
    void locksObsidianHardness() {
        assertEquals(8.0F, BlockHardness.OBSIDIAN_HARDNESS);
    }

    @Test
    void mapsCobwebAndBothObsidianBlocks() {
        for (String path : new String[] {"cobweb", "obsidian", "crying_obsidian"}) {
            Identifier id = Identifier.withDefaultNamespace(path);
            assertTrue(BlockHardness.appliesTo(id), path + " must use InfX obsidian hardness");
            assertEquals(BlockHardness.OBSIDIAN_HARDNESS, BlockHardness.destroyTime(id), path);
        }
    }

    @Test
    void leavesOtherBlocksAndNamespacesUnmapped() {
        assertFalse(BlockHardness.appliesTo(Identifier.withDefaultNamespace("stone")));
        assertFalse(BlockHardness.appliesTo(Identifier.fromNamespaceAndPath("infx", "obsidian")));
    }
}
