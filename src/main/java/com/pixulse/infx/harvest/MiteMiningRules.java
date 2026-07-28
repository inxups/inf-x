package com.pixulse.infx.harvest;

import com.pixulse.infx.item.EquipmentKey;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.MiningFamily;
import com.pixulse.infx.item.material.MiteMaterial;
import com.pixulse.infx.registry.tag.ModTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** MITE R196 tool-family and harvest-level checks for InfiniteX equipment. */
public final class MiteMiningRules {
    private MiteMiningRules() {}

    public static float destroySpeed(EquipmentKey key, BlockState state) {
        if (!canHarvest(key, state)) {
            return 1.0F;
        }
        float speed = key.miningSpeed();
        if (isAxeFamily(key.type()) && state.is(ModTags.Blocks.AXE_HALF_SPEED)) {
            speed *= 0.5F;
        }
        return speed;
    }

    public static boolean canHarvest(EquipmentKey key, BlockState state) {
        return isEffective(key, state) && harvestLevel(key.material()) >= HarvestRequirements.requiredLevel(state);
    }

    public static boolean isEffective(EquipmentKey key, BlockState state) {
        MiningFamily family = key.type().miningFamily();
        if (family == MiningFamily.NONE || state.is(ModTags.Blocks.NO_EFFECTIVE_TOOL)) {
            return false;
        }
        if (family == MiningFamily.HOE && state.is(Blocks.CLAY)) {
            return false;
        }
        if (family == MiningFamily.SCYTHE && isRootCrop(state)) {
            return false;
        }
        if (state.is(ModTags.Blocks.effectiveWith(family))) {
            return true;
        }
        if (key.type() == EquipmentType.WAR_HAMMER && state.is(ModTags.Blocks.WAR_HAMMER_EFFECTIVE)) {
            return true;
        }
        return family == MiningFamily.SHOVEL
                && key.material().has(MiteMaterial.Flag.METAL)
                && state.is(ModTags.Blocks.METAL_SHOVEL_EFFECTIVE);
    }

    public static int harvestLevel(MiteMaterial material) {
        return material.harvestTier().map(HarvestTier::level).orElse(0);
    }

    private static boolean isRootCrop(BlockState state) {
        return state.is(Blocks.CARROTS) || state.is(Blocks.POTATOES) || state.is(Blocks.BEETROOTS);
    }

    private static boolean isAxeFamily(EquipmentType type) {
        return type == EquipmentType.HATCHET
                || type == EquipmentType.AXE
                || type == EquipmentType.BATTLE_AXE;
    }
}
