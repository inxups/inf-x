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
                Map.entry("container.infx.flint_workbench", "Flint Workbench"),
                Map.entry("container.infx.copper_workbench", "Copper Workbench"),
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
                Map.entry("advancements.infx.nuggets.title", "Nuggets"),
                Map.entry("advancements.infx.nuggets.description", "Recover a copper nugget from gravel"),
                Map.entry("advancements.infx.better_tools.title", "Better Tools"),
                Map.entry("advancements.infx.better_tools.description", "Build a copper workbench"),
                Map.entry("advancements.infx.build_pickaxe.title", "Build Pickaxe"),
                Map.entry("advancements.infx.build_pickaxe.description", "Craft an InfiniteX copper pickaxe"))) {
            @Override
            String name(R196Catalog.Entry entry) {
                return entry.englishName();
            }
        },
        ZH_CN("zh_cn", Map.ofEntries(
                Map.entry("itemGroup.infx", "InfiniteX"),
                Map.entry("block.infx.flint_workbench", "燧石工具台"),
                Map.entry("block.infx.copper_workbench", "铜工具台"),
                Map.entry("container.infx.flint_workbench", "燧石工具台"),
                Map.entry("container.infx.copper_workbench", "铜工具台"),
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
                Map.entry("advancements.infx.nuggets.title", "铜粒"),
                Map.entry("advancements.infx.nuggets.description", "从沙砾中取得一粒铜"),
                Map.entry("advancements.infx.better_tools.title", "更好的工具"),
                Map.entry("advancements.infx.better_tools.description", "搭建铜工具台"),
                Map.entry("advancements.infx.build_pickaxe.title", "制作铜镐"),
                Map.entry("advancements.infx.build_pickaxe.description", "制作 InfiniteX 铜镐"))) {
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
