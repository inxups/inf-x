package com.pixulse.infx.data.harvest;

/** Resolves whether a player may start destroying a block under InfX harvest rules. */
public final class HarvestPolicy {
    private HarvestPolicy() {}

    public static boolean allows(
            boolean creative,
            boolean portable,
            boolean correctForDrops,
            int toolLevel,
            int requiredLevel) {
        if (creative || portable || requiredLevel <= 0) {
            return true;
        }
        return correctForDrops && toolLevel >= requiredLevel;
    }
}
