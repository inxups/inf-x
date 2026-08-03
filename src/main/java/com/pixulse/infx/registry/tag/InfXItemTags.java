package com.pixulse.infx.registry.tag;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.data.harvest.HarvestTier;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.material.InfxMaterial;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class InfXItemTags {
    public static final TagKey<Item> BINDINGS = create("bindings");
    public static final TagKey<Item> FURNACE_FUELS_HEAT_2 = create("furnace_fuels/heat_2");
    public static final TagKey<Item> WATER_BUCKETS = create("water_buckets");
    public static final TagKey<Item> MILK_BUCKETS = create("milk_buckets");
    public static final TagKey<Item> SMELTING_INPUTS_HEAT_2 = create("smelting_inputs/heat_2");
    public static final TagKey<Item> SMELTING_INPUTS_HEAT_3 = create("smelting_inputs/heat_3");
    public static final TagKey<Item> SMELTING_INPUTS_HEAT_4 = create("smelting_inputs/heat_4");
    public static final TagKey<Item> INFX_DURABILITY_ENCHANTABLE = enchantable("infx_durability");
    public static final TagKey<Item> INFX_DISARMING_ENCHANTABLE = enchantable("infx_disarming");
    public static final TagKey<Item> INFX_BUTCHERING_ENCHANTABLE = enchantable("infx_butchering");
    public static final TagKey<Item> INFX_STUNNING_ENCHANTABLE = enchantable("infx_stunning");
    public static final TagKey<Item> INFX_VAMPIRISM_ENCHANTABLE = enchantable("infx_vampirism");
    public static final TagKey<Item> INFX_SLAUGHTER_ENCHANTABLE = enchantable("infx_slaughter");
    public static final TagKey<Item> INFX_CLEAVING_ENCHANTABLE = enchantable("infx_cleaving");
    public static final TagKey<Item> INFX_HARVESTING_ENCHANTABLE = enchantable("infx_harvesting");
    public static final TagKey<Item> INFX_PENETRATION_ENCHANTABLE = enchantable("infx_penetration");
    public static final TagKey<Item> INFX_FERTILITY_ENCHANTABLE = enchantable("infx_fertility");
    public static final TagKey<Item> INFX_TREE_FELLING_ENCHANTABLE = enchantable("infx_tree_felling");
    public static final TagKey<Item> INFX_FORTUNE_ENCHANTABLE = enchantable("infx_fortune");
    public static final TagKey<Item> INFX_FREE_MOVEMENT_ENCHANTABLE = enchantable("infx_free_movement");
    public static final TagKey<Item> INFX_CHEST_ARMOR_ENCHANTABLE = enchantable("infx_chest_armor");
    public static final TagKey<Item> INFX_SWORD_FAMILY_ENCHANTABLE = enchantable("infx_sword_family");
    public static final TagKey<Item> INFX_SHARPNESS_ENCHANTABLE = enchantable("infx_sharpness");
    public static final TagKey<Item> INFX_SWEEPING_ENCHANTABLE = enchantable("infx_sweeping");
    public static final TagKey<Item> INFX_SMITE_ENCHANTABLE = enchantable("infx_smite");
    public static final TagKey<Item> INFX_KNOCKBACK_ENCHANTABLE = enchantable("infx_knockback");
    public static final TagKey<Item> INFX_LOOTING_ENCHANTABLE = enchantable("infx_looting");
    public static final TagKey<Item> INFX_EFFICIENCY_ENCHANTABLE = enchantable("infx_efficiency");
    public static final TagKey<Item> INFX_SILK_TOUCH_ENCHANTABLE = enchantable("infx_silk_touch");
    public static final TagKey<Item> INFX_THORNS_ENCHANTABLE = enchantable("infx_thorns");
    public static final TagKey<Item> INFX_SOLID_METAL_TORSO_ENCHANTABLE = enchantable("infx_solid_metal_torso");
    public static final TagKey<Item> GELATINOUS_SPHERES = create("gelatinous_spheres");
    public static final TagKey<Item> CURSE_ANIMAL_PRODUCTS = create("curse/animal_products");
    public static final TagKey<Item> CURSE_PLANT_PRODUCTS = create("curse/plant_products");
    public static final TagKey<Item> CURSE_DRINKS = create("curse/drinks");

    private InfXItemTags() {
    }

    public static TagKey<Item> toolTier(HarvestTier tier) {
        return create("tool_tier/" + tier.path());
    }

    public static TagKey<Item> repairMaterial(InfxMaterial material) {
        return create("repair_materials/" + material.path());
    }

    public static TagKey<Item> material(InfxMaterial material) {
        return create("materials/" + material.path());
    }

    public static TagKey<Item> equipmentType(EquipmentType type) {
        return create("equipment/" + type.path());
    }

    private static TagKey<Item> enchantable(String path) {
        return create("enchantable/" + path);
    }

    private static TagKey<Item> create(String path) {
        return TagKey.create(Registries.ITEM, InfiniteX.id(path));
    }
}
