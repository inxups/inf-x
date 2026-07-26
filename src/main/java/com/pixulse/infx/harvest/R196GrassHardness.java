package com.pixulse.infx.harvest;

import net.minecraft.resources.Identifier;

/**
 * Hardness mapping for MITE R196 {@code Block.tallGrass}. Its metadata values 1 and 2 represent
 * grass and fern respectively, and both use a hardness of {@code 0.02F}.
 */
public final class R196GrassHardness {
    public static final float TALL_GRASS_HARDNESS = 0.02F;

    private R196GrassHardness() {}

    /** Returns whether a 26.2 block is a direct grass or fern counterpart of MITE tall grass. */
    public static boolean appliesTo(Identifier blockId) {
        if (!Identifier.DEFAULT_NAMESPACE.equals(blockId.getNamespace())) {
            return false;
        }
        return switch (blockId.getPath()) {
            case "short_grass", "tall_grass", "fern", "large_fern" -> true;
            default -> false;
        };
    }
}
