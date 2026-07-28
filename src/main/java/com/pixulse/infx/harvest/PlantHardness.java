package com.pixulse.infx.harvest;

import net.minecraft.resources.Identifier;

/** Hardness mappings for vanilla plants that must retain non-instant MITE harvesting progress. */
public final class PlantHardness {
    public static final float TALL_GRASS_HARDNESS = 0.02F;
    public static final float SUGAR_CANE_HARDNESS = 0.02F;

    private PlantHardness() {}

    /** Returns whether a 26.2 block is covered by one of the explicit plant hardness mappings. */
    public static boolean appliesTo(Identifier blockId) {
        return Identifier.DEFAULT_NAMESPACE.equals(blockId.getNamespace())
                && switch (blockId.getPath()) {
                    case "short_grass", "tall_grass", "fern", "large_fern", "sugar_cane" -> true;
                    default -> false;
                };
    }

    /** Returns the mapped destroy time for a block accepted by {@link #appliesTo(Identifier)}. */
    public static float destroyTime(Identifier blockId) {
        return switch (blockId.getPath()) {
            case "short_grass", "tall_grass", "fern", "large_fern" -> TALL_GRASS_HARDNESS;
            case "sugar_cane" -> SUGAR_CANE_HARDNESS;
            default -> throw new IllegalArgumentException("No MITE plant hardness mapping for " + blockId);
        };
    }
}
