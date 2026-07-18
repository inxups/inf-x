package com.pixulse.infx.data;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.R196Catalog;
import com.pixulse.infx.registry.ModEntityTypes;
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
                Map.entry("block.infx.clay_furnace", "Clay Oven"),
                Map.entry("block.infx.sandstone_furnace", "Sandstone Oven"),
                Map.entry("block.infx.hardened_clay_furnace", "Large Clay Oven"),
                Map.entry("block.infx.obsidian_furnace", "Obsidian Furnace"),
                Map.entry("block.infx.netherrack_furnace", "Netherrack Furnace"),
                Map.entry("block.infx.silver_ore", "Silver Ore"),
                Map.entry("block.infx.mithril_ore", "Mithril Ore"),
                Map.entry("block.infx.adamantium_ore", "Adamantium Ore"),
                Map.entry("container.infx.flint_workbench", "Flint Workbench"),
                Map.entry("container.infx.copper_workbench", "Copper Workbench"),
                Map.entry("container.infx.silver_workbench", "Silver Workbench"),
                Map.entry("container.infx.gold_workbench", "Gold Workbench"),
                Map.entry("container.infx.iron_workbench", "Iron Workbench"),
                Map.entry("container.infx.ancient_metal_workbench", "Ancient Metal Workbench"),
                Map.entry("container.infx.mithril_workbench", "Mithril Workbench"),
                Map.entry("container.infx.adamantium_workbench", "Adamantium Workbench"),
                Map.entry("container.infx.obsidian_workbench", "Obsidian Workbench"),
                Map.entry("container.infx.clay_furnace", "Clay Oven"),
                Map.entry("container.infx.sandstone_furnace", "Sandstone Oven"),
                Map.entry("container.infx.hardened_clay_furnace", "Large Clay Oven"),
                Map.entry("container.infx.obsidian_furnace", "Obsidian Furnace"),
                Map.entry("container.infx.netherrack_furnace", "Netherrack Furnace"),
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
                Map.entry("advancements.infx.cutting_edge.description", "Craft a flint hatchet or knife"),
                Map.entry("advancements.infx.mine_wood.title", "Mine Wood"),
                Map.entry("advancements.infx.mine_wood.description", "Use the right tool to harvest a log"),
                Map.entry("advancements.infx.build_work_bench.title", "Build Work Bench"),
                Map.entry("advancements.infx.build_work_bench.description", "Craft a flint workbench"),
                Map.entry("advancements.infx.build_club.title", "Time to Strike!"),
                Map.entry("advancements.infx.build_club.description", "Use planks and sticks to make a club"),
                Map.entry("advancements.infx.build_axe.title", "Lumberjack"),
                Map.entry("advancements.infx.build_axe.description", "Craft a full axe or battle axe"),
                Map.entry("advancements.infx.build_shovel.title", "Explore the Surface"),
                Map.entry("advancements.infx.build_shovel.description", "Craft a shovel"),
                Map.entry("advancements.infx.nuggets.title", "Nuggets"),
                Map.entry("advancements.infx.nuggets.description", "Recover a copper nugget from gravel"),
                Map.entry("advancements.infx.better_tools.title", "Better Tools"),
                Map.entry("advancements.infx.better_tools.description", "Build a copper workbench"),
                Map.entry("advancements.infx.wear_leather.title", "Suiting Up"),
                Map.entry("advancements.infx.wear_leather.description", "Wear leather armor to protect yourself"),
                Map.entry("advancements.infx.build_chain_mail.title", "Better Armor"),
                Map.entry(
                        "advancements.infx.build_chain_mail.description",
                        "Use nuggets to craft chains and chain mail armor"),
                Map.entry("advancements.infx.wear_all_plate_armor.title", "Tin Can"),
                Map.entry(
                        "advancements.infx.wear_all_plate_armor.description",
                        "Cover yourself head to toe with plate armor"),
                Map.entry("advancements.infx.wear_all_adamantium_plate_armor.title", "Juggernaut"),
                Map.entry(
                        "advancements.infx.wear_all_adamantium_plate_armor.description",
                        "Don a full suit of adamantium plate armor"),
                Map.entry("advancements.infx.build_hoe.title", "Time to Farm!"),
                Map.entry("advancements.infx.build_hoe.description", "Craft a metal hoe or mattock"),
                Map.entry("advancements.infx.build_scythe.title", "Reaper"),
                Map.entry("advancements.infx.build_scythe.description", "Make a scythe to harvest wheat"),
                Map.entry("advancements.infx.build_pickaxe.title", "Build Pickaxe"),
                Map.entry("advancements.infx.build_pickaxe.description", "Craft an InfiniteX copper pickaxe"),
                Map.entry("advancements.infx.build_furnace.title", "Hot Topic"),
                Map.entry("advancements.infx.build_furnace.description", "Build a cobblestone furnace"),
                Map.entry("advancements.infx.acquire_iron.title", "Acquire Hardware"),
                Map.entry("advancements.infx.acquire_iron.description", "Smelt an iron ingot"),
                Map.entry("advancements.infx.build_better_pickaxe.title", "Getting an Upgrade"),
                Map.entry(
                        "advancements.infx.build_better_pickaxe.description",
                        "Craft an iron-or-better pickaxe or war hammer"))) {
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
                Map.entry("block.infx.clay_furnace", "粘土炉"),
                Map.entry("block.infx.sandstone_furnace", "沙石炉"),
                Map.entry("block.infx.hardened_clay_furnace", "陶瓦炉"),
                Map.entry("block.infx.obsidian_furnace", "黑曜石熔炉"),
                Map.entry("block.infx.netherrack_furnace", "下界岩熔炉"),
                Map.entry("block.infx.silver_ore", "银矿石"),
                Map.entry("block.infx.mithril_ore", "秘银矿石"),
                Map.entry("block.infx.adamantium_ore", "艾德曼矿石"),
                Map.entry("container.infx.flint_workbench", "燧石工具台"),
                Map.entry("container.infx.copper_workbench", "铜工具台"),
                Map.entry("container.infx.silver_workbench", "银工具台"),
                Map.entry("container.infx.gold_workbench", "金工具台"),
                Map.entry("container.infx.iron_workbench", "铁工具台"),
                Map.entry("container.infx.ancient_metal_workbench", "远古金属工具台"),
                Map.entry("container.infx.mithril_workbench", "秘银工具台"),
                Map.entry("container.infx.adamantium_workbench", "艾德曼工具台"),
                Map.entry("container.infx.obsidian_workbench", "黑曜石工具台"),
                Map.entry("container.infx.clay_furnace", "粘土炉"),
                Map.entry("container.infx.sandstone_furnace", "沙石炉"),
                Map.entry("container.infx.hardened_clay_furnace", "陶瓦炉"),
                Map.entry("container.infx.obsidian_furnace", "黑曜石熔炉"),
                Map.entry("container.infx.netherrack_furnace", "下界岩熔炉"),
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
                Map.entry("advancements.infx.cutting_edge.description", "制作一把燧石短斧或小刀"),
                Map.entry("advancements.infx.mine_wood.title", "伐木"),
                Map.entry("advancements.infx.mine_wood.description", "用正确的工具采集原木"),
                Map.entry("advancements.infx.build_work_bench.title", "搭建工具台"),
                Map.entry("advancements.infx.build_work_bench.description", "制作燧石工具台"),
                Map.entry("advancements.infx.build_club.title", "出击时间到"),
                Map.entry("advancements.infx.build_club.description", "用木板和木棍制作一根木棒"),
                Map.entry("advancements.infx.build_axe.title", "伐木工"),
                Map.entry("advancements.infx.build_axe.description", "制作一把斧或战斧"),
                Map.entry("advancements.infx.build_shovel.title", "探索地表"),
                Map.entry("advancements.infx.build_shovel.description", "制作一把锹"),
                Map.entry("advancements.infx.nuggets.title", "铜粒"),
                Map.entry("advancements.infx.nuggets.description", "从沙砾中取得一粒铜"),
                Map.entry("advancements.infx.better_tools.title", "更好的工具"),
                Map.entry("advancements.infx.better_tools.description", "搭建铜工具台"),
                Map.entry("advancements.infx.wear_leather.title", "文明着装"),
                Map.entry("advancements.infx.wear_leather.description", "穿上一件皮革护甲"),
                Map.entry("advancements.infx.build_chain_mail.title", "更好的护甲"),
                Map.entry("advancements.infx.build_chain_mail.description", "用金属粒制造锁链以及锁链护甲"),
                Map.entry("advancements.infx.wear_all_plate_armor.title", "铁罐头"),
                Map.entry("advancements.infx.wear_all_plate_armor.description", "用盔甲从头武装到脚趾"),
                Map.entry("advancements.infx.wear_all_adamantium_plate_armor.title", "世界主宰"),
                Map.entry("advancements.infx.wear_all_adamantium_plate_armor.description", "穿上整套艾德曼护甲"),
                Map.entry("advancements.infx.build_hoe.title", "农耕时间到"),
                Map.entry("advancements.infx.build_hoe.description", "制作一把金属锄或鹤嘴锄"),
                Map.entry("advancements.infx.build_scythe.title", "收割者"),
                Map.entry("advancements.infx.build_scythe.description", "合成一把镰刀收割小麦"),
                Map.entry("advancements.infx.build_pickaxe.title", "制作铜镐"),
                Map.entry("advancements.infx.build_pickaxe.description", "制作 InfiniteX 铜镐"),
                Map.entry("advancements.infx.build_furnace.title", "温暖的炉火"),
                Map.entry("advancements.infx.build_furnace.description", "制作一座圆石熔炉"),
                Map.entry("advancements.infx.acquire_iron.title", "铁器时代"),
                Map.entry("advancements.infx.acquire_iron.description", "冶炼一块铁锭"),
                Map.entry("advancements.infx.build_better_pickaxe.title", "获得升级"),
                Map.entry("advancements.infx.build_better_pickaxe.description", "制作铁级或更高级的镐或战锤"))) {
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
        ModEntityTypes.names().forEach(entity -> add(
                "entity.infx." + entity.path(),
                locale == Locale.EN_US ? entity.english() : entity.chinese()));
        locale.baseTranslations.forEach(this::add);
        for (var anvil : com.pixulse.infx.registry.ModBlocks.METAL_ANVILS) {
            String material = anvil.get().material().path();
            String name = locale == Locale.EN_US
                    ? anvil.get().material().englishNoun() + " Anvil"
                    : anvil.get().material().chinesePrefix() + "砧";
            add("block.infx." + material + "_anvil", name);
        }
        add("container.infx.metal_anvil", locale == Locale.EN_US ? "Metal Anvil" : "金属砧");
        add("block.infx.silver_block", locale == Locale.EN_US ? "Block of Silver" : "银块");
        add("block.infx.ancient_metal_block", locale == Locale.EN_US ? "Block of Ancient Metal" : "远古金属块");
        add("block.infx.mithril_block", locale == Locale.EN_US ? "Block of Mithril" : "秘银块");
        add("block.infx.adamantium_block", locale == Locale.EN_US ? "Block of Adamantium" : "艾德曼块");
        for (com.pixulse.infx.material.R196Quality quality : com.pixulse.infx.material.R196Quality.values()) {
            String english = switch (quality) {
                case POOR -> "Poor Quality";
                case FINE -> "Fine Quality";
                case EXCELLENT -> "Excellent Quality";
                case SUPERB -> "Superb Quality";
                case MASTERWORK -> "Masterwork";
                case LEGENDARY -> "Legendary";
            };
            String chinese = switch (quality) {
                case POOR -> "粗劣品质";
                case FINE -> "精良品质";
                case EXCELLENT -> "优秀品质";
                case SUPERB -> "卓越品质";
                case MASTERWORK -> "大师之作";
                case LEGENDARY -> "传奇品质";
            };
            add("quality.infx." + quality.getSerializedName(), locale == Locale.EN_US ? english : chinese);
        }
    }
}
