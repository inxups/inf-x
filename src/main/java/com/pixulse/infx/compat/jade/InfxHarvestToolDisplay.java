package com.pixulse.infx.compat.jade;

import com.pixulse.infx.data.harvest.HarvestRequirements;
import com.pixulse.infx.data.harvest.HarvestTier;
import com.pixulse.infx.data.harvest.InfxMiningRules;
import com.pixulse.infx.item.EquipmentKey;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.MiningFamily;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.registry.InfXItems;
import com.pixulse.infx.registry.tag.InfXBlockTags;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Resolves the InfX tools that can harvest a block for the Jade tooltip.
 *
 * <p>Unlike Jade's vanilla harvest-tool logic, which keys off the vanilla {@code mineable/*}
 * tags and always reports the cheapest vanilla tool, this resolver reads the InfX harvest
 * system: the effective {@link MiningFamily} tags and the numeric harvest level from
 * {@link HarvestRequirements}. For every effective family it reports the cheapest InfX tool
 * of a material whose harvest tier satisfies the required level (e.g. an iron pickaxe for
 * mithril ore instead of a wooden pickaxe).
 */
public final class InfxHarvestToolDisplay {
    /** Display order of the InfX mining families. */
    private static final List<MiningFamily> FAMILIES = List.of(
            MiningFamily.PICKAXE,
            MiningFamily.SHOVEL,
            MiningFamily.AXE,
            MiningFamily.HOE,
            MiningFamily.SCYTHE,
            MiningFamily.CUDGEL,
            MiningFamily.SWORD,
            MiningFamily.SHEARS);

    private InfxHarvestToolDisplay() {}

    /**
     * Returns one representative tool per effective family: the cheapest tool of the family
     * whose material tier is at least {@code requiredLevel(state)}. Returns an empty list
     * when no InfX tool can harvest the block.
     */
    public static List<ItemStack> toolsFor(BlockState state) {
        int requiredLevel = HarvestRequirements.requiredLevel(state);
        List<ItemStack> tools = new ArrayList<>();
        for (MiningFamily family : FAMILIES) {
            EquipmentKey key = representativeKey(family, state, requiredLevel);
            if (key != null) {
                tools.add(InfXItems.catalog().equipment(key.material(), key.type()).holder().toStack());
            }
        }
        return tools;
    }

    private static EquipmentKey representativeKey(MiningFamily family, BlockState state, int requiredLevel) {
        if (family == MiningFamily.NONE || state.is(InfXBlockTags.NO_EFFECTIVE_TOOL)) {
            return null;
        }
        if (family == MiningFamily.HOE && state.is(Blocks.CLAY)) {
            return null;
        }
        if (family == MiningFamily.SCYTHE && isRootCrop(state)) {
            return null;
        }
        boolean tagEffective = state.is(InfXBlockTags.effectiveWith(family));
        if (!tagEffective) {
            if (family != MiningFamily.PICKAXE || !state.is(InfXBlockTags.WAR_HAMMER_EFFECTIVE)) {
                if (family != MiningFamily.SHOVEL || !state.is(InfXBlockTags.METAL_SHOVEL_EFFECTIVE)) {
                    return null;
                }
            }
        }
        EquipmentType type = family == MiningFamily.PICKAXE && !tagEffective
                ? EquipmentType.WAR_HAMMER
                : representativeType(family);
        for (InfxMaterial material : materialPreference(requiredLevel)) {
            if (!type.allows(material)) {
                continue;
            }
            EquipmentKey key = new EquipmentKey(material, type);
            if (InfxMiningRules.isEffective(key, state)) {
                return key;
            }
        }
        return null;
    }

    /**
     * Materials in ascending harvest level; within one level the canonical progression
     * material comes first (copper before gold for level 2, iron before ancient metal for
     * level 3), so the displayed "cheapest tool" is the expected baseline item.
     */
    private static List<InfxMaterial> materialPreference(int requiredLevel) {
        List<InfxMaterial> order = new ArrayList<>();
        if (requiredLevel <= 0) {
            // Tier-less materials (wood, leather) only qualify for hand-tier blocks.
            for (InfxMaterial material : InfxMaterial.values()) {
                if (material.harvestTier().isEmpty()) {
                    order.add(material);
                }
            }
        }
        for (HarvestTier tier : HarvestTier.values()) {
            if (tier.level() < requiredLevel) {
                continue;
            }
            for (InfxMaterial material : InfxMaterial.values()) {
                if (material.harvestTier().orElse(null) == tier && material.path().equals(tier.path())) {
                    order.add(material);
                }
            }
            for (InfxMaterial material : InfxMaterial.values()) {
                if (material.harvestTier().orElse(null) == tier && !material.path().equals(tier.path())) {
                    order.add(material);
                }
            }
        }
        return order;
    }

    private static EquipmentType representativeType(MiningFamily family) {
        return switch (family) {
            case PICKAXE -> EquipmentType.PICKAXE;
            case SHOVEL -> EquipmentType.SHOVEL;
            case AXE -> EquipmentType.AXE;
            case HOE -> EquipmentType.HOE;
            case SCYTHE -> EquipmentType.SCYTHE;
            case CUDGEL -> EquipmentType.CUDGEL;
            case SWORD -> EquipmentType.SWORD;
            case SHEARS -> EquipmentType.SHEARS;
            case NONE -> throw new IllegalArgumentException("NONE has no representative tool");
        };
    }

    private static boolean isRootCrop(BlockState state) {
        return state.is(Blocks.CARROTS) || state.is(Blocks.POTATOES) || state.is(Blocks.BEETROOTS);
    }
}
