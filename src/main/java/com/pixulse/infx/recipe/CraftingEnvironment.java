package com.pixulse.infx.recipe;

import com.pixulse.infx.effect.curse.CurseManager;
import com.pixulse.infx.effect.curse.CurseType;
import com.pixulse.infx.harvest.HarvestSpeedRules;
import com.pixulse.infx.registry.ModEnchantments;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;

/** Environmental gates and curse modifiers shared by hand and workbench crafting. */
public final class CraftingEnvironment {
    private CraftingEnvironment() {}

    public static boolean canCraft(Player player) {
        return !HarvestSpeedRules.isInCobweb(player) && !HarvestSpeedRules.isParalyzed(player);
    }

    public static boolean hasClumsiness(Player player) {
        if (CurseManager.hasCurse(player, CurseType.CLUMSINESS)) {
            CurseManager.reveal(player, CurseType.CLUMSINESS);
            return true;
        }
        var enchantments = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        return enchantments.get(ModEnchantments.CLUMSINESS)
                .map(enchantment -> {
                    for (EquipmentSlot slot : EquipmentSlot.VALUES) {
                        var stack = player.getItemBySlot(slot);
                        if (stack.getEnchantmentLevel(enchantment) > 0) {
                            return true;
                        }
                    }
                    return false;
                })
                .orElse(false);
    }

    public static int applyClumsiness(int ticks, boolean clumsy) {
        return clumsy ? Math.multiplyExact(ticks, 2) : ticks;
    }
}
