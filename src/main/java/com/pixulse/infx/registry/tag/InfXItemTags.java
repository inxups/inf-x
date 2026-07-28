package com.pixulse.infx.registry.tag;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.harvest.HarvestTier;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.material.MiteMaterial;
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
    public static final TagKey<Item> R196_DURABILITY_ENCHANTABLE = enchantable("r196_durability");
    public static final TagKey<Item> R196_DISARMING_ENCHANTABLE = enchantable("r196_disarming");
    public static final TagKey<Item> R196_BUTCHERING_ENCHANTABLE = enchantable("r196_butchering");
    public static final TagKey<Item> R196_STUNNING_ENCHANTABLE = enchantable("r196_stunning");
    public static final TagKey<Item> R196_VAMPIRISM_ENCHANTABLE = enchantable("r196_vampirism");
    public static final TagKey<Item> R196_SLAUGHTER_ENCHANTABLE = enchantable("r196_slaughter");
    public static final TagKey<Item> R196_CLEAVING_ENCHANTABLE = enchantable("r196_cleaving");
    public static final TagKey<Item> R196_HARVESTING_ENCHANTABLE = enchantable("r196_harvesting");
    public static final TagKey<Item> R196_PENETRATION_ENCHANTABLE = enchantable("r196_penetration");
    public static final TagKey<Item> R196_FERTILITY_ENCHANTABLE = enchantable("r196_fertility");
    public static final TagKey<Item> R196_TREE_FELLING_ENCHANTABLE = enchantable("r196_tree_felling");
    public static final TagKey<Item> R196_FORTUNE_ENCHANTABLE = enchantable("r196_fortune");
    public static final TagKey<Item> R196_FREE_MOVEMENT_ENCHANTABLE = enchantable("r196_free_movement");
    public static final TagKey<Item> R196_CHEST_ARMOR_ENCHANTABLE = enchantable("r196_chest_armor");
    public static final TagKey<Item> R196_SWORD_FAMILY_ENCHANTABLE = enchantable("r196_sword_family");
    public static final TagKey<Item> R196_SMITE_ENCHANTABLE = enchantable("r196_smite");
    public static final TagKey<Item> R196_KNOCKBACK_ENCHANTABLE = enchantable("r196_knockback");
    public static final TagKey<Item> R196_LOOTING_ENCHANTABLE = enchantable("r196_looting");
    public static final TagKey<Item> R196_EFFICIENCY_ENCHANTABLE = enchantable("r196_efficiency");
    public static final TagKey<Item> R196_SILK_TOUCH_ENCHANTABLE = enchantable("r196_silk_touch");
    public static final TagKey<Item> R196_THORNS_ENCHANTABLE = enchantable("r196_thorns");
    public static final TagKey<Item> R196_SOLID_METAL_TORSO_ENCHANTABLE = enchantable("r196_solid_metal_torso");
    public static final TagKey<Item> GELATINOUS_SPHERES = create("gelatinous_spheres");
    public static final TagKey<Item> CURSE_ANIMAL_PRODUCTS = create("curse/animal_products");
    public static final TagKey<Item> CURSE_PLANT_PRODUCTS = create("curse/plant_products");
    public static final TagKey<Item> CURSE_DRINKS = create("curse/drinks");

    private InfXItemTags() {
    }

    public static TagKey<Item> toolTier(HarvestTier tier) {
        return create("tool_tier/" + tier.path());
    }

    public static TagKey<Item> repairMaterial(MiteMaterial material) {
        return create("repair_materials/" + material.path());
    }

    public static TagKey<Item> material(MiteMaterial material) {
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
