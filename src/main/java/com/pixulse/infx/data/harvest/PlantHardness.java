package com.pixulse.infx.data.harvest;

import net.minecraft.resources.Identifier;

/** Hardness mappings for vanilla plants that must retain non-instant InfX harvesting progress. */
public final class PlantHardness {
    public static final float TALL_GRASS_HARDNESS = 0.02F;
    public static final float SUGAR_CANE_HARDNESS = 0.02F;

    private PlantHardness() {}

    /** Returns whether a 26.2 block is covered by one of the explicit plant hardness mappings. */
    public static boolean appliesTo(Identifier blockId) {
        return Identifier.DEFAULT_NAMESPACE.equals(blockId.getNamespace())
                && switch (blockId.getPath()) {
                    case "short_grass", "tall_grass", "fern", "large_fern", "sugar_cane",
                            "short_dry_grass", "tall_dry_grass", "dead_bush", "bush",
                            "firefly_bush", "weeping_vines", "weeping_vines_plant",
                            "twisting_vines", "twisting_vines_plant", "azalea", "flowering_azalea" ->
                            true;
                    default -> false;
                };
    }

    /** Returns the mapped destroy time for a block accepted by {@link #appliesTo(Identifier)}. */
    public static float destroyTime(Identifier blockId) {
        return switch (blockId.getPath()) {
            case "short_grass", "tall_grass", "fern", "large_fern", "short_dry_grass",
                    "tall_dry_grass", "dead_bush", "bush", "firefly_bush", "weeping_vines",
                    "weeping_vines_plant", "twisting_vines", "twisting_vines_plant",
                    "azalea", "flowering_azalea" ->
                    TALL_GRASS_HARDNESS;
            case "sugar_cane" -> SUGAR_CANE_HARDNESS;
            default -> throw new IllegalArgumentException("No InfX plant hardness mapping for " + blockId);
        };
    }
}
