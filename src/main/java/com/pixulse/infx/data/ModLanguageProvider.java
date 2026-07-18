package com.pixulse.infx.data;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.R196Catalog;
import com.pixulse.infx.registry.ModItems;
import java.util.Map;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

final class ModLanguageProvider extends LanguageProvider {
    enum Locale {
        EN_US("en_us", Map.ofEntries(
                Map.entry("itemGroup.infx", "InfiniteX"),
                Map.entry("block.infx.flint_workbench", "Flint Workbench"),
                Map.entry("block.infx.copper_workbench", "Copper Workbench"),
                Map.entry("block.infx.silver_workbench", "Silver Workbench"),
                Map.entry("block.infx.gold_workbench", "Gold Workbench"),
                Map.entry("block.infx.iron_workbench", "Iron Workbench"),
                Map.entry("block.infx.ancient_metal_workbench", "Ancient Metal Workbench"),
                Map.entry("block.infx.mithril_workbench", "Mithril Workbench"),
                Map.entry("block.infx.adamantium_workbench", "Adamantium Workbench"),
                Map.entry("block.infx.obsidian_workbench", "Obsidian Workbench"),
                Map.entry("container.infx.flint_workbench", "Flint Workbench"),
                Map.entry("container.infx.copper_workbench", "Copper Workbench"),
                Map.entry("container.infx.silver_workbench", "Silver Workbench"),
                Map.entry("container.infx.gold_workbench", "Gold Workbench"),
                Map.entry("container.infx.iron_workbench", "Iron Workbench"),
                Map.entry("container.infx.ancient_metal_workbench", "Ancient Metal Workbench"),
                Map.entry("container.infx.mithril_workbench", "Mithril Workbench"),
                Map.entry("container.infx.adamantium_workbench", "Adamantium Workbench"),
                Map.entry("container.infx.obsidian_workbench", "Obsidian Workbench"),
                Map.entry("jei.infx.category.hand", "Hand Crafting"),
                Map.entry("jei.infx.category.flint", "Flint Workbench"),
                Map.entry("jei.infx.category.copper", "Copper Workbench"),
                Map.entry("jei.infx.category.silver", "Silver Workbench"),
                Map.entry("jei.infx.category.gold", "Gold Workbench"),
                Map.entry("jei.infx.category.iron", "Iron Workbench"),
                Map.entry("jei.infx.category.ancient_metal", "Ancient Metal Workbench"),
                Map.entry("jei.infx.category.mithril", "Mithril Workbench"),
                Map.entry("jei.infx.category.adamantium", "Adamantium Workbench"),
                Map.entry("jei.infx.category.obsidian", "Obsidian Workbench"),
                Map.entry("jei.infx.difficulty", "Difficulty: %s"),
                Map.entry("message.infx.workbench_obstructed", "The workbench needs clear space above it"),
                Map.entry("advancements.infx.open_inventory.title", "Taking Stock"),
                Map.entry("advancements.infx.open_inventory.description", "Open your inventory and assess your situation"),
                Map.entry("advancements.infx.stick_picker.title", "Stick Picker"),
                Map.entry("advancements.infx.stick_picker.description", "Find your first stick"),
                Map.entry("advancements.infx.cutting_edge.title", "Cutting Edge"),
                Map.entry("advancements.infx.cutting_edge.description", "Craft a flint hatchet"),
                Map.entry("advancements.infx.mine_wood.title", "Mine Wood"),
                Map.entry("advancements.infx.mine_wood.description", "Use the right tool to harvest a log"),
                Map.entry("advancements.infx.build_work_bench.title", "Build Work Bench"),
                Map.entry("advancements.infx.build_work_bench.description", "Craft a flint workbench"),
                Map.entry("advancements.infx.build_axe.title", "Lumberjack"),
                Map.entry("advancements.infx.build_axe.description", "Craft a flint or metal axe"),
                Map.entry("advancements.infx.build_shovel.title", "Explore the Surface"),
                Map.entry("advancements.infx.build_shovel.description", "Craft a flint shovel"),
                Map.entry("advancements.infx.nuggets.title", "Nuggets"),
                Map.entry("advancements.infx.nuggets.description", "Recover a copper nugget from gravel"),
                Map.entry("advancements.infx.better_tools.title", "Better Tools"),
                Map.entry("advancements.infx.better_tools.description", "Build a copper workbench"),
                Map.entry("advancements.infx.build_hoe.title", "Time to Farm!"),
                Map.entry("advancements.infx.build_hoe.description", "Craft a metal hoe"),
                Map.entry("advancements.infx.build_pickaxe.title", "Build Pickaxe"),
                Map.entry("advancements.infx.build_pickaxe.description", "Craft an InfiniteX copper pickaxe"),
                Map.entry("advancements.infx.build_furnace.title", "Hot Topic"),
                Map.entry("advancements.infx.build_furnace.description", "Build a cobblestone furnace"),
                Map.entry("advancements.infx.acquire_iron.title", "Acquire Hardware"),
                Map.entry("advancements.infx.acquire_iron.description", "Smelt an iron ingot"),
                Map.entry("advancements.infx.build_better_pickaxe.title", "Getting an Upgrade"),
                Map.entry("advancements.infx.build_better_pickaxe.description", "Craft an InfiniteX iron pickaxe"))) {
            @Override
            String name(R196Catalog.Entry entry) {
                return entry.englishName();
            }
        },
        ZH_CN("zh_cn", Map.ofEntries(
                Map.entry("itemGroup.infx", "InfiniteX"),
                Map.entry("block.infx.flint_workbench", "燧石工具台"),
                Map.entry("block.infx.copper_workbench", "铜工具台"),
                Map.entry("block.infx.silver_workbench", "银工具台"),
                Map.entry("block.infx.gold_workbench", "金工具台"),
                Map.entry("block.infx.iron_workbench", "铁工具台"),
                Map.entry("block.infx.ancient_metal_workbench", "远古金属工具台"),
                Map.entry("block.infx.mithril_workbench", "秘银工具台"),
                Map.entry("block.infx.adamantium_workbench", "艾德曼工具台"),
                Map.entry("block.infx.obsidian_workbench", "黑曜石工具台"),
                Map.entry("container.infx.flint_workbench", "燧石工具台"),
                Map.entry("container.infx.copper_workbench", "铜工具台"),
                Map.entry("container.infx.silver_workbench", "银工具台"),
                Map.entry("container.infx.gold_workbench", "金工具台"),
                Map.entry("container.infx.iron_workbench", "铁工具台"),
                Map.entry("container.infx.ancient_metal_workbench", "远古金属工具台"),
                Map.entry("container.infx.mithril_workbench", "秘银工具台"),
                Map.entry("container.infx.adamantium_workbench", "艾德曼工具台"),
                Map.entry("container.infx.obsidian_workbench", "黑曜石工具台"),
                Map.entry("jei.infx.category.hand", "手工制作"),
                Map.entry("jei.infx.category.flint", "燧石工具台"),
                Map.entry("jei.infx.category.copper", "铜工具台"),
                Map.entry("jei.infx.category.silver", "银工具台"),
                Map.entry("jei.infx.category.gold", "金工具台"),
                Map.entry("jei.infx.category.iron", "铁工具台"),
                Map.entry("jei.infx.category.ancient_metal", "远古金属工具台"),
                Map.entry("jei.infx.category.mithril", "秘银工具台"),
                Map.entry("jei.infx.category.adamantium", "艾德曼工具台"),
                Map.entry("jei.infx.category.obsidian", "黑曜石工具台"),
                Map.entry("jei.infx.difficulty", "难度：%s"),
                Map.entry("message.infx.workbench_obstructed", "工具台上方需要留出空间"),
                Map.entry("advancements.infx.open_inventory.title", "查看物品栏"),
                Map.entry("advancements.infx.open_inventory.description", "打开物品栏，确认眼下的处境"),
                Map.entry("advancements.infx.stick_picker.title", "拾枝者"),
                Map.entry("advancements.infx.stick_picker.description", "找到第一根木棍"),
                Map.entry("advancements.infx.cutting_edge.title", "锋芒初现"),
                Map.entry("advancements.infx.cutting_edge.description", "制作一把燧石短斧"),
                Map.entry("advancements.infx.mine_wood.title", "伐木"),
                Map.entry("advancements.infx.mine_wood.description", "用正确的工具采集原木"),
                Map.entry("advancements.infx.build_work_bench.title", "搭建工具台"),
                Map.entry("advancements.infx.build_work_bench.description", "制作燧石工具台"),
                Map.entry("advancements.infx.build_axe.title", "伐木工"),
                Map.entry("advancements.infx.build_axe.description", "制作一把燧石斧或金属斧"),
                Map.entry("advancements.infx.build_shovel.title", "探索地表"),
                Map.entry("advancements.infx.build_shovel.description", "制作一把燧石锹"),
                Map.entry("advancements.infx.nuggets.title", "铜粒"),
                Map.entry("advancements.infx.nuggets.description", "从沙砾中取得一粒铜"),
                Map.entry("advancements.infx.better_tools.title", "更好的工具"),
                Map.entry("advancements.infx.better_tools.description", "搭建铜工具台"),
                Map.entry("advancements.infx.build_hoe.title", "农耕时间到"),
                Map.entry("advancements.infx.build_hoe.description", "用金属锭和木棍制作一把锄"),
                Map.entry("advancements.infx.build_pickaxe.title", "制作铜镐"),
                Map.entry("advancements.infx.build_pickaxe.description", "制作 InfiniteX 铜镐"),
                Map.entry("advancements.infx.build_furnace.title", "温暖的炉火"),
                Map.entry("advancements.infx.build_furnace.description", "制作一座圆石熔炉"),
                Map.entry("advancements.infx.acquire_iron.title", "铁器时代"),
                Map.entry("advancements.infx.acquire_iron.description", "冶炼一块铁锭"),
                Map.entry("advancements.infx.build_better_pickaxe.title", "获得升级"),
                Map.entry("advancements.infx.build_better_pickaxe.description", "制作 InfiniteX 铁镐"))) {
            @Override
            String name(R196Catalog.Entry entry) {
                return entry.chineseName();
            }
        };

        final String code;
        final Map<String, String> baseTranslations;

        Locale(String code, Map<String, String> baseTranslations) {
            this.code = code;
            this.baseTranslations = baseTranslations;
        }

        abstract String name(R196Catalog.Entry entry);
    }

    private final Locale locale;

    ModLanguageProvider(PackOutput output, Locale locale) {
        super(output, InfiniteX.MOD_ID, locale.code);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        ModItems.catalog().entries().forEach(entry -> add("item.infx." + entry.path(), locale.name(entry)));
        locale.baseTranslations.forEach(this::add);
    }
}
