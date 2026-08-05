package com.pixulse.infx.data.harvest;

import net.minecraft.resources.Identifier;

/** Hardness mappings for vanilla blocks whose MITE values differ in modern Minecraft. */
public final class MiteBlockHardness {
    /** MITE's {@code BlockHardness.obsidian} value. */
    public static final float OBSIDIAN_HARDNESS = 8.0F;

    private MiteBlockHardness() {}

    /** Returns whether a vanilla block uses MITE's obsidian hardness. */
    public static boolean appliesTo(Identifier blockId) {
        return Identifier.DEFAULT_NAMESPACE.equals(blockId.getNamespace())
                && switch (blockId.getPath()) {
                    case "cobweb", "obsidian", "crying_obsidian" -> true;
                    default -> false;
                };
    }

    /** Returns MITE's obsidian hardness for a mapped block. */
    public static float destroyTime(Identifier blockId) {
        if (!appliesTo(blockId)) {
            throw new IllegalArgumentException("No MITE block hardness mapping for " + blockId);
        }
        return OBSIDIAN_HARDNESS;
    }
}
