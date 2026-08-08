package com.pixulse.infx.world;

import net.minecraft.server.level.ServerLevel;

/**
 * InfX pillager patrol spawning rules.
 *
 * <p>Patrols need at least {@value #MINIMUM_PATROL_DAY} days, then follow the same condition
 * that unlocks village generation: survival day 60 and a world-crafted iron tool. The village
 * requirement dominates once reached, while the day floor keeps patrols away even if the
 * village milestone data is ever relaxed.
 */
public final class PatrolRules {
    /** InfX patrols never spawn before this day, replacing the vanilla five-day timeline gate. */
    public static final long MINIMUM_PATROL_DAY = 32L;

    private PatrolRules() {}

    public static boolean maySpawn(ServerLevel level) {
        return maySpawn(
                StructureGenerationGates.day(level),
                StructureGenerationGates.isUnlocked(StructureGenerationGates.VILLAGE_RULE, level));
    }

    static boolean maySpawn(long day, boolean villageUnlocked) {
        return day >= MINIMUM_PATROL_DAY && villageUnlocked;
    }
}
