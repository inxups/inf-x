package com.pixulse.infx.config;

import com.iafenvoy.jupiter.config.container.AutoInitConfigContainer;
import com.iafenvoy.jupiter.config.entry.BooleanEntry;
import com.iafenvoy.jupiter.config.entry.DoubleEntry;
import com.iafenvoy.jupiter.config.entry.IntegerEntry;
import com.pixulse.infx.InfiniteX;
import net.minecraft.network.chat.Component;

/** Server-authoritative InfiniteX configuration stored in {@code config/infx/infx-common.json}. */
public final class InfXConfig extends AutoInitConfigContainer {
    public static final InfXConfig INSTANCE = new InfXConfig();

    public final SurvivalConfig survival = new SurvivalConfig();
    public final ProgressionConfig progression = new ProgressionConfig();
    public final ProductionConfig production = new ProductionConfig();
    public final WorldConfig world = new WorldConfig();
    public final MobConfig mobs = new MobConfig();
    public final DevelopmentConfig development = new DevelopmentConfig();

    private InfXConfig() {
        super(InfiniteX.id("common"), Component.literal("InfiniteX Server"), "./config/infx/infx-common.json");
    }

    private static BooleanEntry flag(String key, String name, boolean defaultValue) {
        return BooleanEntry.builder(Component.literal(name), defaultValue).key(key).build();
    }

    private static IntegerEntry integer(String key, String name, int defaultValue, int min, int max) {
        return IntegerEntry.builder(Component.literal(name), defaultValue).range(min, max).key(key).build();
    }

    private static DoubleEntry decimal(String key, String name, double defaultValue, double min, double max) {
        return DoubleEntry.builder(Component.literal(name), defaultValue).range(min, max).key(key).build();
    }

    public static final class SurvivalConfig extends AutoInitConfigCategoryBase {
        public final BooleanEntry enabled = flag("enabled", "Enable survival rules", true);
        public final DoubleEntry initialCapacity = decimal("initialCapacity", "Initial health and food capacity", 6.0D, 1.0D, 40.0D);
        public final DoubleEntry maximumCapacity = decimal("maximumCapacity", "Maximum health and food capacity", 20.0D, 1.0D, 100.0D);
        public final IntegerEntry levelsPerCapacityIncrease = integer("levelsPerCapacityIncrease", "Levels per capacity increase", 5, 1, 200);
        public final DoubleEntry capacityIncreasePerMilestone = decimal("capacityIncreasePerMilestone", "Capacity increase per milestone", 2.0D, 0.0D, 40.0D);
        public final DoubleEntry metabolismMultiplier = decimal("metabolismMultiplier", "Metabolism multiplier", 1.0D, 0.0D, 100.0D);
        public final DoubleEntry nutritionMetabolismRatio = decimal("nutritionMetabolismRatio", "Nutrition metabolism ratio", 0.25D, 0.0D, 1.0D);
        public final DoubleEntry naturalHealingMultiplier = decimal("naturalHealingMultiplier", "Natural healing multiplier", 1.0D, 0.0D, 100.0D);
        public final BooleanEntry craftingRequiresFoodEnergy = flag("craftingRequiresFoodEnergy", "Crafting requires food energy", true);

        private SurvivalConfig() {
            super("survival", Component.literal("Survival"));
        }
    }

    public static final class ProgressionConfig extends AutoInitConfigCategoryBase {
        public final BooleanEntry enabled = flag("enabled", "Enable progression rules", true);
        public final BooleanEntry harvestRequirements = flag("harvestRequirements", "Enforce harvest requirements", true);
        public final DoubleEntry craftingSpeedMultiplier = decimal("craftingSpeedMultiplier", "Crafting speed multiplier", 1.0D, 0.01D, 100.0D);

        private ProgressionConfig() {
            super("progression", Component.literal("Progression"));
        }
    }

    public static final class ProductionConfig extends AutoInitConfigCategoryBase {
        public final BooleanEntry enabled = flag("enabled", "Enable production rules", true);
        public final BooleanEntry furnaceHeat = flag("furnaceHeat", "Enforce furnace heat", true);
        public final BooleanEntry furnaceMouthBlocking = flag("furnaceMouthBlocking", "Block furnace mouths", true);

        private ProductionConfig() {
            super("production", Component.literal("Production"));
        }
    }

    public static final class WorldConfig extends AutoInitConfigCategoryBase {
        public final BooleanEntry enabled = flag("enabled", "Enable world rules", true);
        public final BooleanEntry moonEvents = flag("moonEvents", "Enable moon events", true);
        public final BooleanEntry lunarFishing = flag("lunarFishing", "Enable lunar fishing modifiers", true);
        public final BooleanEntry lunarTaming = flag("lunarTaming", "Enable lunar taming modifiers", true);
        public final BooleanEntry underworldPortals = flag("underworldPortals", "Enable underworld portals", true);

        private WorldConfig() {
            super("world", Component.literal("World"));
        }
    }

    public static final class MobConfig extends AutoInitConfigCategoryBase {
        public final BooleanEntry enabled = flag("enabled", "Enable mob rules", true);
        public final BooleanEntry datapackSpawnRules = flag("datapackSpawnRules", "Enable datapack spawn rules", true);
        public final BooleanEntry gelatinousBlockDissolving = flag("gelatinousBlockDissolving", "Enable gelatinous block dissolving", true);
        public final BooleanEntry gelatinousItemCorrosion = flag("gelatinousItemCorrosion", "Enable gelatinous item corrosion", true);
        public final BooleanEntry bloodMoonFrenzy = flag("bloodMoonFrenzy", "Enable blood moon frenzy", true);

        private MobConfig() {
            super("mobs", Component.literal("Mobs"));
        }
    }

    public static final class DevelopmentConfig extends AutoInitConfigCategoryBase {
        public final BooleanEntry testMode = flag("testMode", "Enable development test mode", false);

        private DevelopmentConfig() {
            super("development", Component.literal("Development"));
        }
    }
}
