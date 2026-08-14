package com.pixulse.infx.item.equipment;

import com.pixulse.infx.item.Catalog;
import com.pixulse.infx.item.EquipmentKey;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.item.material.Quality;
import com.pixulse.infx.registry.InfXDataComponents;
import com.pixulse.infx.registry.InfXItems;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class QualitySystem {
    public static final int AVERAGE_CODE = 0;
    private static final int AVERAGE_ORDINAL = 2;
    private static final List<Quality> QUALITY_CYCLE = List.of(
            Quality.FINE,
            Quality.EXCELLENT,
            Quality.SUPERB,
            Quality.MASTERWORK,
            Quality.LEGENDARY,
            Quality.WRETCHED,
            Quality.POOR);

    private QualitySystem() {}

    public static boolean supportsQuality(ItemStack stack) {
        Catalog.EquipmentEntry entry = InfXItems.catalog().equipment(stack);
        return entry != null && entry.key().durability() > 0;
    }

    public static EquipmentKey key(ItemStack stack) {
        Catalog.EquipmentEntry entry = InfXItems.catalog().equipment(stack);
        return entry == null ? null : entry.key();
    }

    public static Quality fromCode(int code) {
        int ordinal = code - 1;
        return ordinal >= 0 && ordinal < Quality.values().length
                ? Quality.values()[ordinal]
                : null;
    }

    public static int toCode(Quality quality) {
        return quality == null ? AVERAGE_CODE : quality.ordinal() + 1;
    }

    public static int cycleCode(ItemStack output, Player player, float difficulty, int currentCode) {
        return cycleCode(output, player, difficulty, currentCode, false);
    }

    public static int cycleCode(
            ItemStack output,
            Player player,
            float difficulty,
            int currentCode,
            boolean clumsy) {
        return cycleCode(output, player, difficulty, currentCode, clumsy, false);
    }

    public static int cycleCode(
            ItemStack output,
            Player player,
            float difficulty,
            int currentCode,
            boolean clumsy,
            boolean witchClumsiness) {
        EquipmentKey key = key(output);
        if (key == null) {
            return AVERAGE_CODE;
        }
        Quality current = fromCode(currentCode);
        Quality candidate = nextSelectableQuality(
                current,
                key.material().maximumQuality(),
                player.totalExperience,
                difficulty,
                player.experienceLevel,
                clumsy,
                witchClumsiness);
        return toCode(candidate);
    }

    static Quality nextSelectableQuality(
            Quality current,
            Quality maximum,
            int totalExperience,
            float difficulty) {
        return nextSelectableQuality(current, maximum, totalExperience, difficulty, 0, false, false);
    }

    static Quality nextSelectableQuality(
            Quality current,
            Quality maximum,
            int totalExperience,
            float difficulty,
            boolean clumsy) {
        return nextSelectableQuality(current, maximum, totalExperience, difficulty, 0, clumsy, false);
    }

    static Quality nextSelectableQuality(
            Quality current,
            Quality maximum,
            int totalExperience,
            float difficulty,
            int experienceLevel,
            boolean clumsy,
            boolean witchClumsiness) {
        int start = current == null ? 0 : QUALITY_CYCLE.indexOf(current) + 1;
        Quality minimum = minimumQuality(experienceLevel, witchClumsiness);
        int minimumOrdinal = minimum == null ? AVERAGE_ORDINAL : minimum.ordinal();
        for (int index = Math.max(0, start); index < QUALITY_CYCLE.size(); index++) {
            Quality candidate = QUALITY_CYCLE.get(index);
            if (candidate.isAtMost(maximum)
                    && candidate.ordinal() >= minimumOrdinal
                    && (experienceLevel >= 0 || candidate == minimum)
                    && (totalExperience > 0 || candidate == minimum)
                    && (experienceCost(difficulty, candidate, clumsy) == 0
                            || totalExperience >= experienceCost(difficulty, candidate, clumsy))) {
                return candidate;
            }
        }
        return null;
    }

    public static int clampCode(ItemStack output, Player player, float difficulty, int requestedCode) {
        return clampCode(output, player, difficulty, requestedCode, false);
    }

    public static int clampCode(
            ItemStack output,
            Player player,
            float difficulty,
            int requestedCode,
            boolean clumsy) {
        return clampCode(output, player, difficulty, requestedCode, clumsy, false);
    }

    public static int clampCode(
            ItemStack output,
            Player player,
            float difficulty,
            int requestedCode,
            boolean clumsy,
            boolean witchClumsiness) {
        EquipmentKey key = key(output);
        if (key == null) {
            return AVERAGE_CODE;
        }
        Quality minimum = minimumQuality(player.experienceLevel, witchClumsiness);
        if (minimum != null && !minimum.isAtMost(key.material().maximumQuality())) {
            minimum = key.material().maximumQuality();
        }
        int minimumOrdinal = minimum == null ? AVERAGE_ORDINAL : minimum.ordinal();
        int fallback = toCode(minimum);
        Quality requested = fromCode(requestedCode);
        if (requested == null) return fallback;
        int cost = experienceCost(difficulty, requested, clumsy);
        if (!requested.isAtMost(key.material().maximumQuality())
                || requested.ordinal() < minimumOrdinal
                || (player.experienceLevel < 0 && requested != minimum)
                || (cost > 0 && player.totalExperience < cost)) {
            return fallback;
        }
        return requestedCode;
    }

    static int clumsyFallbackCode(int experienceLevel, boolean clumsy) {
        return toCode(minimumQuality(experienceLevel, clumsy));
    }

    public static Quality minimumQuality(int experienceLevel, boolean witchClumsiness) {
        if (!witchClumsiness && experienceLevel < 0) {
            return experienceLevel <= -11
                    ? Quality.WRETCHED
                    : Quality.POOR;
        }

        int effectiveLevel = experienceLevel - (witchClumsiness ? 20 : 0);
        int qualityOrdinal = Math.clamp(AVERAGE_ORDINAL + effectiveLevel / 10, 0, AVERAGE_ORDINAL);
        return switch (qualityOrdinal) {
            case 0 -> Quality.WRETCHED;
            case 1 -> Quality.POOR;
            default -> null;
        };
    }

    public static float adjustedDifficulty(float difficulty, int qualityCode) {
        Quality quality = fromCode(qualityCode);
        if (quality == null || quality == Quality.WRETCHED || quality == Quality.POOR) {
            return difficulty;
        }
        return difficulty * quality.craftingDifficultyMultiplier();
    }

    public static int experienceCost(float difficulty, Quality quality) {
        return experienceCost(difficulty, quality, false);
    }

    public static int experienceCost(float difficulty, Quality quality, boolean clumsy) {
        if (quality == null || quality == Quality.WRETCHED || quality == Quality.POOR) {
            return 0;
        }
        int cost = Math.round(adjustedDifficulty(difficulty, toCode(quality)) / 5.0F);
        return clumsy ? Math.multiplyExact(cost, 2) : cost;
    }

    public static int applySelectedQuality(ItemStack stack, int qualityCode) {
        EquipmentKey key = key(stack);
        Quality quality = fromCode(qualityCode);
        if (key == null || quality == null) {
            if (key != null && key.material() != InfxMaterial.RUSTED_IRON) {
                stack.remove(InfXDataComponents.QUALITY.get());
                stack.set(DataComponents.MAX_DAMAGE, key.durability());
            }
            return 0;
        }
        stack.set(InfXDataComponents.QUALITY.get(), quality);
        stack.set(
                DataComponents.MAX_DAMAGE,
                Math.max(1, Math.round(key.durability() * quality.durabilityMultiplier())));
        return qualityCode;
    }
}
