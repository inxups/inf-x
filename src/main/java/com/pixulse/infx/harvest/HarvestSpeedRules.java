package com.pixulse.infx.harvest;

import com.pixulse.infx.progression.R196Experience;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import com.pixulse.infx.enchantment.R196Enchantments;
import com.pixulse.infx.registry.ModEnchantments;
import com.pixulse.infx.registry.ModMobEffects;

/** R196 environmental mining multipliers, kept pure enough for focused tests. */
public final class HarvestSpeedRules {
    private HarvestSpeedRules() {}

    public static float multiplier(
            int level,
            boolean submerged,
            boolean airborne,
            boolean hungry,
            boolean paralyzed,
            boolean inCobweb) {
        float result = 1.0F + Math.max(0, Math.min(level, R196Experience.MAX_DISPLAY_LEVEL)) * 0.02F;
        if (submerged) result *= 0.2F;
        if (airborne) result *= 0.2F;
        if (hungry) result *= 0.2F;
        if (paralyzed) result *= 0.1F;
        if (inCobweb) result *= 0.1F;
        return result;
    }

    public static boolean isParalyzed(Player player) {
        if (R196Enchantments.armorLevel(player, ModEnchantments.FREE_MOVEMENT) > 0) return false;
        if (player.hasEffect(ModMobEffects.PARALYSIS)) return true;
        var slowness = player.getEffect(MobEffects.SLOWNESS);
        return slowness != null && slowness.getAmplifier() >= 4;
    }

    public static boolean isInCobweb(Player player) {
        if (R196Enchantments.armorLevel(player, ModEnchantments.FREE_MOVEMENT) > 0) return false;
        BlockPos feet = player.blockPosition();
        BlockPos upper = BlockPos.containing(player.getX(), player.getEyeY() - 0.25, player.getZ());
        return player.level().getBlockState(feet).is(Blocks.COBWEB)
                || player.level().getBlockState(upper).is(Blocks.COBWEB);
    }
}
