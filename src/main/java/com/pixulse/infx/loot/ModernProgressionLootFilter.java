package com.pixulse.infx.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.registry.InfXItems;
import com.pixulse.infx.registry.InfXLootModifiers;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jspecify.annotations.NonNull;

/** Removes vanilla paths that bypass the INFX material and crafting progression. */
public final class ModernProgressionLootFilter extends LootModifier {
    private static final Set<String> FORBIDDEN_EXACT = Set.of(
            "raw_copper",
            "raw_copper_block",
            "copper_block",
            "copper_nugget",
            "copper_ingot",
            "raw_iron",
            "iron_nugget",
            "iron_ingot",
            "raw_gold",
            "gold_nugget",
            "gold_ingot",
            "diamond",
            "emerald",
            "netherite_scrap",
            "netherite_ingot",
            "netherite_block",
            "ancient_debris",
            "netherite_upgrade_smithing_template",
            "shield",
            "enchanted_book",
            "elytra",
            "trident",
            "mace",
            "heavy_core",
            "breeze_rod",
            "wind_charge",
            "trial_key",
            "ominous_trial_key",
            "bundle",
            "crafter",
            "totem_of_undying",
            "heart_of_the_sea",
            "echo_shard",
            "recovery_compass",
            "sniffer_egg",
            "bucket",
            "water_bucket",
            "lava_bucket",
            "milk_bucket");
    private static final Set<String> EQUIPMENT_SUFFIXES = Set.of(
            "_axe", "_hoe", "_pickaxe", "_shovel", "_spear", "_sword",
            "_helmet", "_chestplate", "_leggings", "_boots", "_horse_armor", "_nautilus_armor");
    private static final Set<String> VANILLA_MATERIAL_PREFIXES = Set.of(
            "wooden", "stone", "copper", "iron", "golden", "diamond", "netherite");

    public static final MapCodec<ModernProgressionLootFilter> CODEC = RecordCodecBuilder.mapCodec(instance ->
            codecStart(instance).apply(instance, ModernProgressionLootFilter::new));

    public ModernProgressionLootFilter(LootItemCondition[] conditions, int priority) {
        super(conditions, priority);
    }

    @Override
    protected @NonNull ObjectArrayList<ItemStack> doApply(@NonNull ObjectArrayList<ItemStack> loot, LootContext context) {
        Identifier table = context.getQueriedLootTableId();
        if (table == null || !table.getNamespace().equals("minecraft")) return loot;
        String path = table.getPath();
        if (!path.startsWith("chests/") && !path.equals("gameplay/piglin_bartering")) return loot;
        for (int index = 0; index < loot.size(); index++) {
            ItemStack converted = convertEquipment(loot.get(index));
            if (converted != loot.get(index)) loot.set(index, converted);
        }
        loot.removeIf(ModernProgressionLootFilter::isForbidden);
        return loot;
    }

    /** Converts vanilla equipment before the final forbidden-item pass removes it. */
    public static ItemStack convertEquipment(ItemStack stack) {
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        EquipmentType type = conversionType(id);
        if (type == null) return stack;
        return stack.transmuteCopy(InfXItems.catalog().equipment(InfxMaterial.ANCIENT_METAL, type).holder());
    }

    static EquipmentType conversionType(Identifier id) {
        if (!id.getNamespace().equals("minecraft")) return null;
        String path = id.getPath();
        if (path.startsWith("chainmail_")) {
            return switch (path) {
                case "chainmail_helmet" -> EquipmentType.CHAINMAIL_HELMET;
                case "chainmail_chestplate" -> EquipmentType.CHAINMAIL_CHESTPLATE;
                case "chainmail_leggings" -> EquipmentType.CHAINMAIL_LEGGINGS;
                case "chainmail_boots" -> EquipmentType.CHAINMAIL_BOOTS;
                default -> null;
            };
        }
        if (!path.startsWith("iron_") && !path.startsWith("diamond_")) return null;
        String suffix = path.substring(path.indexOf('_') + 1);
        return switch (suffix) {
            case "pickaxe" -> EquipmentType.PICKAXE;
            case "shovel" -> EquipmentType.SHOVEL;
            case "axe" -> EquipmentType.AXE;
            case "hoe" -> EquipmentType.HOE;
            case "sword", "spear" -> EquipmentType.SWORD;
            case "helmet" -> EquipmentType.HELMET;
            case "chestplate" -> EquipmentType.CHESTPLATE;
            case "leggings" -> EquipmentType.LEGGINGS;
            case "boots" -> EquipmentType.BOOTS;
            case "horse_armor", "nautilus_armor" -> EquipmentType.HORSE_ARMOR;
            default -> null;
        };
    }

    public static boolean isForbidden(ItemStack stack) {
        return isForbidden(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    static boolean isForbidden(Identifier id) {
        if (!id.getNamespace().equals("minecraft")) return false;
        String path = id.getPath();
        if (FORBIDDEN_EXACT.contains(path)) return true;
        if (path.endsWith("_bundle")) return true;
        for (String prefix : VANILLA_MATERIAL_PREFIXES) {
            if (!path.startsWith(prefix + "_")) continue;
            for (String suffix : EQUIPMENT_SUFFIXES) {
                if (path.endsWith(suffix)) return true;
            }
        }
        return false;
    }

    @Override
    public @NonNull MapCodec<? extends IGlobalLootModifier> codec() {
        return InfXLootModifiers.MODERN_PROGRESSION_FILTER.get();
    }
}
