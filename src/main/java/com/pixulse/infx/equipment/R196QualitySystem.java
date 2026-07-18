package com.pixulse.infx.equipment;

import com.pixulse.infx.item.R196Catalog;
import com.pixulse.infx.item.R196EquipmentKey;
import com.pixulse.infx.material.R196Quality;
import com.pixulse.infx.registry.ModDataComponents;
import com.pixulse.infx.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class R196QualitySystem {
    public static final int AVERAGE_CODE = 0;

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
        R196EquipmentKey key = key(output);
        if (key == null) {
            return AVERAGE_CODE;
        }
        R196Quality current = fromCode(currentCode);
        R196Quality candidate = current == null || current == R196Quality.POOR
                ? R196Quality.FINE
                : current.ordinal() + 1 < R196Quality.values().length
                        ? R196Quality.values()[current.ordinal() + 1]
                        : null;
        if (candidate == null
                || !candidate.isAtMost(key.material().maximumQuality())
                || player.totalExperience < experienceCost(difficulty, candidate)) {
            return AVERAGE_CODE;
        }
        return toCode(candidate);
    }

    public static int clampCode(ItemStack output, Player player, float difficulty, int requestedCode) {
        R196EquipmentKey key = key(output);
        R196Quality requested = fromCode(requestedCode);
        if (key == null || requested == null) {
            return AVERAGE_CODE;
        }
        if (requested == R196Quality.POOR
                || !requested.isAtMost(key.material().maximumQuality())
                || player.totalExperience < experienceCost(difficulty, requested)) {
            return AVERAGE_CODE;
        }
        return requestedCode;
    }

    public static float adjustedDifficulty(float difficulty, int qualityCode) {
        R196Quality quality = fromCode(qualityCode);
        if (quality == null || quality == R196Quality.POOR) {
            return difficulty;
        }
        return difficulty * (1 << quality.ordinal());
    }

    public static int experienceCost(float difficulty, R196Quality quality) {
        if (quality == null || quality == R196Quality.POOR) {
            return 0;
        }
        return Math.round(adjustedDifficulty(difficulty, toCode(quality)) / 5.0F);
    }

    public static int applySelectedQuality(ItemStack stack, int qualityCode) {
        R196EquipmentKey key = key(stack);
        R196Quality quality = fromCode(qualityCode);
        if (key == null || quality == null || quality == R196Quality.POOR) {
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
