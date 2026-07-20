package com.pixulse.infx.harvest;

import com.pixulse.infx.item.R196EquipmentKey;
import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.item.R196MiningFamily;
import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.tag.ModTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** MITE R196 tool-family and harvest-level checks for InfiniteX equipment. */
public final class MiteMiningRules {
    private MiteMiningRules() {}

    public static float destroySpeed(R196EquipmentKey key, BlockState state) {
        if (!canHarvest(key, state)) {
            return 1.0F;
        }
        float speed = key.miningSpeed();
        if (isAxeFamily(key.type()) && state.is(ModTags.Blocks.AXE_HALF_SPEED)) {
            speed *= 0.5F;
        }
        return speed;
    }

    public static boolean canHarvest(R196EquipmentKey key, BlockState state) {
        return isEffective(key, state) && harvestLevel(key.material()) >= HarvestRequirements.requiredLevel(state);
    }

    public static boolean isEffective(R196EquipmentKey key, BlockState state) {
        R196MiningFamily family = key.type().miningFamily();
        if (family == R196MiningFamily.NONE || state.is(ModTags.Blocks.NO_EFFECTIVE_TOOL)) {
            return false;
        }
        if (family == R196MiningFamily.HOE && state.is(Blocks.CLAY)) {
            return false;
        }
        if (family == R196MiningFamily.SCYTHE && isRootCrop(state)) {
            return false;
        }
        if (state.is(ModTags.Blocks.effectiveWith(family))) {
            return true;
        }
        if (key.type() == R196EquipmentType.WAR_HAMMER && state.is(ModTags.Blocks.WAR_HAMMER_EFFECTIVE)) {
            return true;
        }
        return family == R196MiningFamily.SHOVEL
                && key.material().has(R196Material.Flag.METAL)
                && state.is(ModTags.Blocks.METAL_SHOVEL_EFFECTIVE);
    }

    public static int harvestLevel(R196Material material) {
        return material.harvestTier().map(HarvestTier::level).orElse(0);
    }

    private static boolean isRootCrop(BlockState state) {
        return state.is(Blocks.CARROTS) || state.is(Blocks.POTATOES) || state.is(Blocks.BEETROOTS);
    }

    private static boolean isAxeFamily(R196EquipmentType type) {
        return type == R196EquipmentType.HATCHET
                || type == R196EquipmentType.AXE
                || type == R196EquipmentType.BATTLE_AXE;
    }
}
