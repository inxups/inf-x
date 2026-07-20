package com.pixulse.infx.tag;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.harvest.HarvestTier;
import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.item.R196MiningFamily;
import com.pixulse.infx.material.R196Material;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class ModTags {
    private ModTags() {}

    public static final class Blocks {
        public static final TagKey<Block> PORTABLE_HAND_HARVEST = create("portable_hand_harvest");
        public static final TagKey<Block> NO_EFFECTIVE_TOOL = create("no_effective_tool");
        public static final TagKey<Block> METAL_SHOVEL_EFFECTIVE = create("effective_tool/metal_shovel");
        public static final TagKey<Block> WAR_HAMMER_EFFECTIVE = create("effective_tool/war_hammer");
        public static final TagKey<Block> AXE_HALF_SPEED = create("effective_tool/axe_half_speed");

        private Blocks() {}

        public static TagKey<Block> requiredLevel(int level) {
            if (level < 0 || level > 6) {
                throw new IllegalArgumentException("Harvest level must be between 0 and 6: " + level);
            }
            return create("requires_harvest_level/" + level);
        }

        public static TagKey<Block> effectiveWith(R196MiningFamily family) {
            if (family == R196MiningFamily.NONE) {
                throw new IllegalArgumentException("NONE has no effective block tag");
            }
            return create("effective_tool/" + family.path());
        }

        private static TagKey<Block> create(String path) {
            return TagKey.create(Registries.BLOCK, InfiniteX.id(path));
        }
    }

    public static final class Items {
        public static final TagKey<Item> BINDINGS = create("bindings");
        public static final TagKey<Item> FURNACE_FUELS_HEAT_2 = create("furnace_fuels/heat_2");
        public static final TagKey<Item> SMELTING_INPUTS_HEAT_2 = create("smelting_inputs/heat_2");
        public static final TagKey<Item> SMELTING_INPUTS_HEAT_3 = create("smelting_inputs/heat_3");
        public static final TagKey<Item> SMELTING_INPUTS_HEAT_4 = create("smelting_inputs/heat_4");

        private Items() {}

        public static TagKey<Item> toolTier(HarvestTier tier) {
            return create("tool_tier/" + tier.path());
        }

        public static TagKey<Item> repairMaterial(R196Material material) {
            return create("repair_materials/" + material.path());
        }

        public static TagKey<Item> material(R196Material material) {
            return create("materials/" + material.path());
        }

        public static TagKey<Item> equipmentType(R196EquipmentType type) {
            return create("equipment/" + type.path());
        }

        private static TagKey<Item> create(String path) {
            return TagKey.create(Registries.ITEM, InfiniteX.id(path));
        }
    }
}
