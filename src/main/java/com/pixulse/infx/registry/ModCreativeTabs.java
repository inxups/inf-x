package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.block.RuneStoneBlock;
import com.pixulse.infx.item.R196Catalog;
import com.pixulse.infx.item.R196EquipmentCategory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, InfiniteX.MOD_ID);
    private static final Identifier MAIN_ID = InfiniteX.id("main");
    private static final Identifier INGREDIENTS_ID = InfiniteX.id("ingredients");
    private static final Identifier FOOD_AND_CONSUMABLES_ID = InfiniteX.id("food_and_consumables");
    private static final Identifier TOOLS_AND_UTILITIES_ID = InfiniteX.id("tools_and_utilities");
    private static final Map<Category, List<DeferredItem<? extends Item>>> ITEMS_BY_CATEGORY =
            createCategorizedItems();

    // NeoForge fixes all mod tabs after the vanilla chain, so anchor the group at its final category.
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main", () ->
            tab("itemGroup.infx", ModItems.SILVER_ORE::toStack, Category.BLOCKS)
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> INGREDIENTS =
            TABS.register("ingredients", () ->
                    tab("itemGroup.infx.ingredients", ModItems.MITHRIL_INGOT::toStack, Category.INGREDIENTS)
                            .withTabsBefore(MAIN_ID)
                            .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FOOD_AND_CONSUMABLES =
            TABS.register("food_and_consumables", () ->
                    tab(
                                    "itemGroup.infx.food_and_consumables",
                                    ModItems.BEEF_STEW::toStack,
                                    Category.FOOD_AND_CONSUMABLES)
                            .withTabsBefore(INGREDIENTS_ID)
                            .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TOOLS_AND_UTILITIES =
            TABS.register("tools_and_utilities", () ->
                    tab(
                                    "itemGroup.infx.tools_and_utilities",
                                    ModItems.IRON_PICKAXE::toStack,
                                    Category.TOOLS_AND_UTILITIES)
                            .withTabsBefore(FOOD_AND_CONSUMABLES_ID)
                            .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> COMBAT_AND_EQUIPMENT =
            TABS.register("combat_and_equipment", () ->
                    tab(
                                    "itemGroup.infx.combat_and_equipment",
                                    ModItems.IRON_SWORD::toStack,
                                    Category.COMBAT_AND_EQUIPMENT)
                            .withTabsBefore(TOOLS_AND_UTILITIES_ID)
                            .build());

    enum Category {
        BLOCKS,
        INGREDIENTS,
        FOOD_AND_CONSUMABLES,
        TOOLS_AND_UTILITIES,
        COMBAT_AND_EQUIPMENT
    }

    private ModCreativeTabs() {}

    static List<DeferredItem<? extends Item>> items(Category category) {
        return ITEMS_BY_CATEGORY.get(category);
    }

    private static CreativeModeTab.Builder tab(
            String translationKey, Supplier<ItemStack> icon, Category category) {
        return CreativeModeTab.builder()
                .title(Component.translatable(translationKey))
                .icon(icon)
                .displayItems((parameters, output) -> items(category).stream()
                        .map(DeferredItem::value)
                        .filter(item -> item.isEnabled(parameters.enabledFeatures()))
                        .forEach(item -> {
                            if (RuneStoneBlock.isRuneStone(item.getDefaultInstance())) {
                                for (int rune = 0; rune < RuneStoneBlock.RUNE_COUNT; rune++) {
                                    output.accept(RuneStoneBlock.applyRune(new ItemStack(item), rune));
                                }
                            } else {
                                output.accept(item);
                            }
                        }));
    }

    private static Map<Category, List<DeferredItem<? extends Item>>> createCategorizedItems() {
        EnumMap<Category, List<DeferredItem<? extends Item>>> categories = new EnumMap<>(Category.class);
        categories.put(
                Category.BLOCKS,
                concatenate(
                        ModItems.ORES,
                        ModItems.METAL_STORAGE_BLOCKS,
                        ModItems.WORLD_BLOCKS,
                        ModItems.FULLTEXT_BLOCKS,
                        ModItems.R196_FLOWERS,
                        ModItems.WORKBENCHES,
                        ModItems.FURNACES,
                        ModItems.METAL_ANVILS,
                        ModItems.ENCHANTING_TABLES,
                        ModItems.METAL_SAFES));
        categories.put(
                Category.INGREDIENTS,
                concatenate(
                        ModItems.catalog().rawEntries().stream().map(ModCreativeTabs::holder).toList(),
                        List.of(ModItems.FLOUR)));
        categories.put(
                Category.FOOD_AND_CONSUMABLES,
                concatenate(
                        List.of(ModItems.WATER_BOWL),
                        ModItems.R196_FOODS,
                        List.of(ModItems.BOTTLE_OF_DISENCHANTING)));
        categories.put(
                Category.TOOLS_AND_UTILITIES,
                concatenate(
                        equipmentItems(R196EquipmentCategory.TOOL),
                        ModItems.R196_BUCKETS,
                        ModItems.R196_RECORDS));
        categories.put(
                Category.COMBAT_AND_EQUIPMENT,
                ModItems.catalog().equipmentEntries().stream()
                        .filter(entry -> entry.key().type().category() != R196EquipmentCategory.TOOL)
                        .map(ModCreativeTabs::holder)
                        .toList());
        return Collections.unmodifiableMap(categories);
    }

    private static List<DeferredItem<? extends Item>> equipmentItems(R196EquipmentCategory category) {
        return ModItems.catalog().equipmentEntries().stream()
                .filter(entry -> entry.key().type().category() == category)
                .map(ModCreativeTabs::holder)
                .toList();
    }

    private static DeferredItem<? extends Item> holder(R196Catalog.Entry entry) {
        return entry.holder();
    }

    @SafeVarargs
    private static List<DeferredItem<? extends Item>> concatenate(
            List<? extends DeferredItem<? extends Item>>... groups) {
        List<DeferredItem<? extends Item>> items = new ArrayList<>();
        for (var group : groups) {
            items.addAll(group);
        }
        return List.copyOf(items);
    }

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
