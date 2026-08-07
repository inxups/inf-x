package com.pixulse.infx.data.harvest;

import net.minecraft.resources.Identifier;

/** Hardness mappings for vanilla blocks whose InfX values differ in modern Minecraft. */
public final class BlockHardness {
    /** InfX's {@code BlockHardness.obsidian} value. */
    public static final float OBSIDIAN_HARDNESS = 8.0F;

    private BlockHardness() {}

    /** Returns whether a vanilla block uses InfX's obsidian hardness. */
    public static boolean appliesTo(Identifier blockId) {
        return Identifier.DEFAULT_NAMESPACE.equals(blockId.getNamespace())
                && switch (blockId.getPath()) {
                    case "cobweb", "obsidian", "crying_obsidian" -> true;
                    default -> false;
                };
    }

    /** Returns InfX's obsidian hardness for a mapped block. */
    public static float destroyTime(Identifier blockId) {
        if (!appliesTo(blockId)) {
            throw new IllegalArgumentException("No InfX block hardness mapping for " + blockId);
        }
        return OBSIDIAN_HARDNESS;
    }
}
