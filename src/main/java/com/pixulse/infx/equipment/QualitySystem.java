package com.pixulse.infx.equipment;

import com.pixulse.infx.item.Catalog;
import com.pixulse.infx.item.EquipmentKey;
import com.pixulse.infx.material.MiteMaterial;
import com.pixulse.infx.material.Quality;
import com.pixulse.infx.registry.ModDataComponents;
import com.pixulse.infx.registry.ModItems;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class QualitySystem {
    public static final int AVERAGE_CODE = 0;
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
        Catalog.EquipmentEntry entry = ModItems.catalog().equipment(stack);
        return entry != null && entry.key().durability() > 0;
    }

    public static EquipmentKey key(ItemStack stack) {
        Catalog.EquipmentEntry entry = ModItems.catalog().equipment(stack);
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
        EquipmentKey key = key(output);
        if (key == null) {
            return AVERAGE_CODE;
        }
        Quality current = fromCode(currentCode);
        Quality candidate = nextSelectableQuality(
                current, key.material().maximumQuality(), player.totalExperience, difficulty, clumsy);
        return toCode(candidate);
    }

    static Quality nextSelectableQuality(
            Quality current,
            Quality maximum,
            int totalExperience,
            float difficulty) {
        return nextSelectableQuality(current, maximum, totalExperience, difficulty, false);
    }

    static Quality nextSelectableQuality(
            Quality current,
            Quality maximum,
            int totalExperience,
            float difficulty,
            boolean clumsy) {
        int start = current == null ? 0 : QUALITY_CYCLE.indexOf(current) + 1;
        for (int index = Math.max(0, start); index < QUALITY_CYCLE.size(); index++) {
            Quality candidate = QUALITY_CYCLE.get(index);
            if (candidate.isAtMost(maximum)
                    && totalExperience >= experienceCost(difficulty, candidate, clumsy)) {
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
        EquipmentKey key = key(output);
        if (key == null) {
            return AVERAGE_CODE;
        }
        int fallback = clumsyFallbackCode(player.experienceLevel, clumsy);
        Quality requested = fromCode(requestedCode);
        if (requested == null) return fallback;
        if (!requested.isAtMost(key.material().maximumQuality())
                || player.totalExperience < experienceCost(difficulty, requested, clumsy)) {
            return fallback;
        }
        return requestedCode;
    }

    static int clumsyFallbackCode(int experienceLevel, boolean clumsy) {
        if (!clumsy) return AVERAGE_CODE;
        // MITE uses average.ordinal() + (level - 20) / 10; Java truncates negative division toward zero.
        int originalOrdinal = Math.max(0, Math.min(2, 2 + (experienceLevel - 20) / 10));
        return switch (originalOrdinal) {
            case 0 -> toCode(Quality.WRETCHED);
            case 1 -> toCode(Quality.POOR);
            default -> AVERAGE_CODE;
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
            if (key != null && key.material() != MiteMaterial.RUSTED_IRON) {
                stack.remove(ModDataComponents.QUALITY.get());
                stack.set(DataComponents.MAX_DAMAGE, key.durability());
            }
            return 0;
        }
        stack.set(ModDataComponents.QUALITY.get(), quality);
        stack.set(
                DataComponents.MAX_DAMAGE,
                Math.max(1, Math.round(key.durability() * quality.durabilityMultiplier())));
        return qualityCode;
    }
}
