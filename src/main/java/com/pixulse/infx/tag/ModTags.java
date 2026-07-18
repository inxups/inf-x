package com.pixulse.infx.tag;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.harvest.HarvestTier;
import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.material.R196Material;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class ModTags {
    private ModTags() {}

    public static final class Blocks {
        public static final TagKey<Block> RESTRICTED_HARVEST = create("restricted_harvest");

        private Blocks() {}

        public static TagKey<Block> requiredTier(HarvestTier tier) {
            return create("requires_tier/" + tier.path());
        }

        private static TagKey<Block> create(String path) {
            return TagKey.create(Registries.BLOCK, InfiniteX.id(path));
        }
    }

    public static final class Items {
        public static final TagKey<Item> BINDINGS = create("bindings");
        public static final TagKey<Item> FURNACE_FUELS_HEAT_2 = create("furnace_fuels/heat_2");
        public static final TagKey<Item> SMELTING_INPUTS_HEAT_2 = create("smelting_inputs/heat_2");

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
