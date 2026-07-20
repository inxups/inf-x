package com.pixulse.infx.equipment;

import com.pixulse.infx.item.R196Catalog;
import com.pixulse.infx.item.R196EquipmentKey;
import com.pixulse.infx.material.R196Quality;
import com.pixulse.infx.registry.ModDataComponents;
import com.pixulse.infx.registry.ModItems;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class R196QualitySystem {
    public static final int AVERAGE_CODE = 0;
    private static final List<R196Quality> QUALITY_CYCLE = List.of(
            R196Quality.FINE,
            R196Quality.EXCELLENT,
            R196Quality.SUPERB,
            R196Quality.MASTERWORK,
            R196Quality.LEGENDARY,
            R196Quality.WRETCHED,
            R196Quality.POOR);

    private R196QualitySystem() {}

    public static boolean supportsQuality(ItemStack stack) {
        R196Catalog.EquipmentEntry entry = ModItems.catalog().equipment(stack);
        return entry != null && entry.key().durability() > 0;
    }

    public static R196EquipmentKey key(ItemStack stack) {
        R196Catalog.EquipmentEntry entry = ModItems.catalog().equipment(stack);
        return entry == null ? null : entry.key();
    }

    public static R196Quality fromCode(int code) {
        int ordinal = code - 1;
        return ordinal >= 0 && ordinal < R196Quality.values().length
                ? R196Quality.values()[ordinal]
                : null;
    }

    public static int toCode(R196Quality quality) {
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
        R196EquipmentKey key = key(output);
        if (key == null) {
            return AVERAGE_CODE;
        }
        R196Quality current = fromCode(currentCode);
        R196Quality candidate = nextSelectableQuality(
                current, key.material().maximumQuality(), player.totalExperience, difficulty, clumsy);
        return toCode(candidate);
    }

    static R196Quality nextSelectableQuality(
            R196Quality current,
            R196Quality maximum,
            int totalExperience,
            float difficulty) {
        return nextSelectableQuality(current, maximum, totalExperience, difficulty, false);
    }

    static R196Quality nextSelectableQuality(
            R196Quality current,
            R196Quality maximum,
            int totalExperience,
            float difficulty,
            boolean clumsy) {
        int start = current == null ? 0 : QUALITY_CYCLE.indexOf(current) + 1;
        for (int index = Math.max(0, start); index < QUALITY_CYCLE.size(); index++) {
            R196Quality candidate = QUALITY_CYCLE.get(index);
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
        R196EquipmentKey key = key(output);
        R196Quality requested = fromCode(requestedCode);
        if (key == null || requested == null) {
            return AVERAGE_CODE;
        }
        if (!requested.isAtMost(key.material().maximumQuality())
                || player.totalExperience < experienceCost(difficulty, requested, clumsy)) {
            return AVERAGE_CODE;
        }
        return requestedCode;
    }

    public static float adjustedDifficulty(float difficulty, int qualityCode) {
        R196Quality quality = fromCode(qualityCode);
        if (quality == null || quality == R196Quality.WRETCHED || quality == R196Quality.POOR) {
            return difficulty;
        }
        return difficulty * quality.craftingDifficultyMultiplier();
    }

    public static int experienceCost(float difficulty, R196Quality quality) {
        return experienceCost(difficulty, quality, false);
    }

    public static int experienceCost(float difficulty, R196Quality quality, boolean clumsy) {
        if (quality == null || quality == R196Quality.WRETCHED || quality == R196Quality.POOR) {
            return 0;
        }
        int cost = Math.round(adjustedDifficulty(difficulty, toCode(quality)) / 5.0F);
        return clumsy ? Math.multiplyExact(cost, 2) : cost;
    }

    public static int applySelectedQuality(ItemStack stack, int qualityCode) {
        R196EquipmentKey key = key(stack);
        R196Quality quality = fromCode(qualityCode);
        if (key == null || quality == null) {
            if (key != null && key.material() != com.pixulse.infx.material.R196Material.RUSTED_IRON) {
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
