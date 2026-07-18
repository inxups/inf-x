package com.pixulse.infx.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pixulse.infx.registry.ModLootModifiers;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/** Removes vanilla paths that bypass the R196 material and crafting progression. */
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
            "_helmet", "_chestplate", "_leggings", "_boots", "_horse_armor");
    private static final Set<String> VANILLA_MATERIAL_PREFIXES = Set.of(
            "wooden", "stone", "copper", "iron", "golden", "diamond", "netherite");

    public static final MapCodec<ModernProgressionLootFilter> CODEC = RecordCodecBuilder.mapCodec(instance ->
            codecStart(instance).apply(instance, ModernProgressionLootFilter::new));

    public ModernProgressionLootFilter(LootItemCondition[] conditions, int priority) {
        super(conditions, priority);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        Identifier table = context.getQueriedLootTableId();
        if (table == null || !table.getNamespace().equals("minecraft")) return loot;
        String path = table.getPath();
        if (!path.startsWith("chests/") && !path.equals("gameplay/piglin_bartering")) return loot;
        loot.removeIf(ModernProgressionLootFilter::isForbidden);
        return loot;
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
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.MODERN_PROGRESSION_FILTER.get();
    }
}
