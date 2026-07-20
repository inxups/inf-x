package com.pixulse.infx.harvest;

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
        return correctForDrops
                && toolLevel >= requiredLevel;
    }
}
