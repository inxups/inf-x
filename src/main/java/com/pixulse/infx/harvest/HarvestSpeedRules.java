package com.pixulse.infx.harvest;

import com.pixulse.infx.enchantment.R196EnchantmentRules;
import com.pixulse.infx.enchantment.R196Enchantments;
import com.pixulse.infx.progression.R196Experience;
import com.pixulse.infx.registry.ModAttachments;
import com.pixulse.infx.registry.ModEnchantments;
import com.pixulse.infx.registry.ModMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;

/** R196 block strength, environmental multipliers and 512-unit mining progress. */
public final class HarvestSpeedRules {
    static final float MODERN_CORRECT_TOOL_DIVISOR = 30.0F;
    static final float MITE_PROGRESS_DIVISOR = 512.0F;
    static final float PORTABLE_STRENGTH_PER_HARDNESS = 4.0F;

    private HarvestSpeedRules() {}

    /**
     * Converts the already-computed 26.2 dig speed into a value that produces MITE progress.
     * Water and airborne penalties are already present in {@code modernSpeed}; only MITE's
     * additional conditions are applied to ordinary blocks.
     */
    public static float adjustedBreakSpeed(
            Player player,
            float modernSpeed,
            float hardness,
            boolean portable) {
        if (modernSpeed <= 0.0F) {
            return 0.0F;
        }

        float strength;
        if (portable && hardness > 0.0F) {
            float minimum = hardness * PORTABLE_STRENGTH_PER_HARDNESS;
            strength = Math.max(minimum, minimum * portableMultiplier(player));
        } else {
            boolean paralyzed = isParalyzed(player);
            boolean inCobweb = isInCobweb(player);
            strength = replaceModernMiningFatigue(player, modernSpeed)
                    * multiplier(
                            player.experienceLevel,
                            false,
                            false,
                            !player.getData(ModAttachments.SURVIVAL).hasFoodEnergy(),
                            paralyzed,
                            inCobweb);
            strength = applyFreeMovementResistance(player, strength, paralyzed, inCobweb);
        }
        return toModernBreakSpeed(strength);
    }

    static float toModernBreakSpeed(float miteStrength) {
        return miteStrength * MODERN_CORRECT_TOOL_DIVISOR / MITE_PROGRESS_DIVISOR;
    }

    static float portableStrength(float hardness, float contextMultiplier) {
        float minimum = hardness * PORTABLE_STRENGTH_PER_HARDNESS;
        return Math.max(minimum, minimum * contextMultiplier);
    }

    public static float multiplier(
            int level,
            boolean submerged,
            boolean airborne,
            boolean hungry,
            boolean paralyzed,
            boolean inCobweb) {
        float result = R196Experience.harvestOrCraftMultiplier(level);
        if (submerged) result *= 0.2F;
        if (airborne) result *= 0.2F;
        if (hungry) result *= 0.2F;
        if (paralyzed) result *= 0.1F;
        if (inCobweb) result *= 0.1F;
        return result;
    }

    static float miteMiningFatigueMultiplier(int amplifier) {
        return amplifier < 0 ? 1.0F : Math.max(0.0F, 1.0F - (amplifier + 1) * 0.2F);
    }

    static float modernMiningFatigueMultiplier(int amplifier) {
        return switch (amplifier) {
            case 0 -> 0.3F;
            case 1 -> 0.09F;
            case 2 -> 0.0027F;
            default -> amplifier < 0 ? 1.0F : 0.00081F;
        };
    }

    private static float replaceModernMiningFatigue(Player player, float modernSpeed) {
        var fatigue = player.getEffect(MobEffects.MINING_FATIGUE);
        if (fatigue == null) {
            return modernSpeed;
        }
        int amplifier = fatigue.getAmplifier();
        return modernSpeed
                / modernMiningFatigueMultiplier(amplifier)
                * miteMiningFatigueMultiplier(amplifier);
    }

    private static float portableMultiplier(Player player) {
        float result = 1.0F;
        if (MobEffectUtil.hasDigSpeed(player)) {
            result *= 1.0F + (MobEffectUtil.getDigSpeedAmplification(player) + 1) * 0.2F;
        }
        var fatigue = player.getEffect(MobEffects.MINING_FATIGUE);
        result *= miteMiningFatigueMultiplier(fatigue == null ? -1 : fatigue.getAmplifier());
        if (player.isEyeInFluid(FluidTags.WATER)) {
            result *= (float) player.getAttributeValue(Attributes.SUBMERGED_MINING_SPEED);
        }
        if (!player.onGround()) result *= 0.2F;
        if (!player.getData(ModAttachments.SURVIVAL).hasFoodEnergy()) result *= 0.2F;
        boolean paralyzed = isParalyzed(player);
        boolean inCobweb = isInCobweb(player);
        if (paralyzed) result *= 0.1F;
        if (inCobweb) result *= 0.1F;
        result = applyFreeMovementResistance(player, result, paralyzed, inCobweb);
        result *= R196Experience.harvestOrCraftMultiplier(player.experienceLevel);
        return result;
    }

    public static boolean isParalyzed(Player player) {
        if (player.hasEffect(ModMobEffects.PARALYSIS)) return true;
        var slowness = player.getEffect(MobEffects.SLOWNESS);
        return slowness != null && slowness.getAmplifier() >= 4;
    }

    public static boolean isInCobweb(Player player) {
        BlockPos feet = player.blockPosition();
        BlockPos upper = BlockPos.containing(player.getX(), player.getEyeY() - 0.25, player.getZ());
        return player.level().getBlockState(feet).is(Blocks.COBWEB)
                || player.level().getBlockState(upper).is(Blocks.COBWEB);
    }

    private static float applyFreeMovementResistance(
            Player player, float strength, boolean paralyzed, boolean inCobweb) {
        int freeMovement = R196Enchantments.maxArmorLevel(player, ModEnchantments.FREE_MOVEMENT);
        if (freeMovement <= 0) return strength;
        float correction = R196EnchantmentRules.reducedImpairmentMultiplier(0.1F, freeMovement) / 0.1F;
        if (paralyzed) strength *= correction;
        if (inCobweb) strength *= correction;
        return strength;
    }
}
