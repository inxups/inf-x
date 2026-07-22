package com.pixulse.infx.world;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/** Extends vanilla fire portal ignition to the Underworld without narrowing other extensions. */
public final class UnderworldPortalRules {
    private UnderworldPortalRules() {}

    public static boolean allowsFirePortalIgnition(
            boolean vanillaAllows, ResourceKey<Level> dimension) {
        return vanillaAllows || Underworld.LEVEL.equals(dimension);
    }
}
