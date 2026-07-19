package com.pixulse.infx.data;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.R196Catalog;
import com.pixulse.infx.registry.ModEntityTypes;
import com.pixulse.infx.registry.ModItems;
import java.util.Map;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

final class ModLanguageProvider extends LanguageProvider {
    private static final Map<String, String[]> FOOD_NAMES = Map.ofEntries(
            Map.entry("flour", names("Flour", "面粉")),
            Map.entry("water_bowl", names("Bowl of Water", "水碗")),
            Map.entry("dough", names("Dough", "面团")),
            Map.entry("salad", names("Salad", "沙拉")),
            Map.entry("blueberries", names("Blueberries", "蓝莓")),
            Map.entry("blueberry_porridge", names("Blueberry Porridge", "蓝莓粥")),
            Map.entry("milk_bowl", names("Bowl of Milk", "牛奶碗")),
            Map.entry("cereal_porridge", names("Cereal Porridge", "麦片粥")),
            Map.entry("chocolate", names("Chocolate", "巧克力")),
            Map.entry("pumpkin_soup", names("Pumpkin Soup", "南瓜汤")),
            Map.entry("cream_of_mushroom_soup", names("Cream of Mushroom Soup", "奶油蘑菇汤")),
            Map.entry("onion", names("Onion", "洋葱")),
            Map.entry("vegetable_soup", names("Vegetable Soup", "蔬菜汤")),
            Map.entry("cream_of_vegetable_soup", names("Cream of Vegetable Soup", "奶油蔬菜汤")),
            Map.entry("chicken_soup", names("Chicken Soup", "鸡汤")),
            Map.entry("beef_stew", names("Beef Stew", "牛肉羹")),
            Map.entry("orange", names("Orange", "橘子")),
            Map.entry("fruit_ice", names("Fruit Ice", "果汁雪糕")),
            Map.entry("cheese", names("Cheese", "奶酪")),
            Map.entry("mashed_potato", names("Mashed Potato", "土豆泥")),
            Map.entry("ice_cream", names("Ice Cream", "冰淇淋")),
            Map.entry("banana", names("Banana", "香蕉")),
            Map.entry("worm", names("Worm", "虫子")),
            Map.entry("cooked_worm", names("Cooked Worm", "熟虫子")));
    private static final Map<String, String[]> ENCHANTMENT_NAMES = Map.ofEntries(
            Map.entry("durability", names("Durability", "耐久")),
            Map.entry("disarming", names("Disarming", "缴械")),
            Map.entry("quickness", names("Quickness", "迅捷")),
            Map.entry("precision", names("Precision", "精准")),
            Map.entry("poisoning", names("Poisoning", "中毒")),
            Map.entry("butchering", names("Butchering", "屠宰")),
            Map.entry("stunning", names("Stunning", "击晕")),
            Map.entry("vampirism", names("Vampirism", "吸血")),
            Map.entry("recovery", names("Recovery", "回收")),
            Map.entry("slaughter", names("Slaughter", "杀害")),
            Map.entry("cleaving", names("Cleaving", "劈裂")),
            Map.entry("harvesting", names("Harvesting", "收获")),
            Map.entry("penetration", names("Penetration", "穿透")),
            Map.entry("baiting", names("Baiting", "饵钓")),
            Map.entry("fertility", names("Fertility", "肥沃")),
            Map.entry("tree_felling", names("Tree Felling", "砍伐")),
            Map.entry("fortune", names("Fortune", "时运")),
            Map.entry("free_movement", names("Free Movement", "灵活移动")),
            Map.entry("regeneration", names("Regeneration", "再生")),
            Map.entry("speed", names("Speed", "速度")),
            Map.entry("endurance", names("Endurance", "耐力")),
            Map.entry("protection", names("Protection", "保护")));
    private static final Map<String, String[]> REMAINING_ADVANCEMENTS = Map.ofEntries(
            Map.entry("kill_cow", names("Cow Tipper", "斗牛士")),
            Map.entry("kill_enemy", names("Monster Hunter", "怪物猎人")),
            Map.entry("snipe_skeleton", names("Sniper Duel", "狙击手的对决")),
            Map.entry("fly_pig", names("When Pigs Fly", "当猪飞的时候")),
            Map.entry("flour", names("The Basic Ingredient", "基础成分")),
            Map.entry("make_bread", names("Bake Bread", "烤面包")),
            Map.entry("bake_cake", names("The Lie", "蛋糕是个谎言")),
            Map.entry("on_a_rail", names("On a Rail", "在铁路上")),
            Map.entry("obsidian_furnace", names("Lava Time", "岩浆烧制时刻")),
            Map.entry("mithril_ingot", names("Mythical Age", "神话时代")),
            Map.entry("diamonds", names("Diamonds!", "钻石")),
            Map.entry("emeralds", names("Emeralds!", "绿宝石")),
            Map.entry("enchantments", names("Enchanter", "附魔师")),
            Map.entry("overkill", names("Overkill", "赶尽杀绝")),
            Map.entry("bookcase", names("Knowledge is Power", "知识就是力量")),
            Map.entry("enlightenment", names("Enlightenment", "启蒙之书")),
            Map.entry("portal", names("The Underworld", "地下世界")),
            Map.entry("portal_to_nether", names("A Long Way Down", "长路漫漫")),
            Map.entry("ghast", names("Return to Sender", "见鬼去吧")),
            Map.entry("blaze_rod", names("Into Fire", "与火共舞")),
            Map.entry("potion", names("Local Brewery", "本地的酿造厂")),
            Map.entry("the_end", names("The End?", "末地？")),
            Map.entry("the_end2", names("The End.", "末地。")),
            Map.entry("netherrack_furnace", names("Ultimate Furnace", "最强的熔炉")),
            Map.entry("adamantium_ingot", names("Ultimate Metal", "最强的金属")),
            Map.entry("crystal_breaker", names("Crystal Breaker", "末影水晶破坏者")),
            Map.entry("runegate", names("The Power of Runes", "符文的力量")),
            Map.entry("seeds", names("Scavenger", "拾荒者")),
            Map.entry("eggs", names("Eat an Egg", "吃鸡蛋")),
            Map.entry("build_oven", names("Clay Craft", "粘土工艺")),
            Map.entry("flint_finder", names("Flint Finder", "燧石寻找者")),
            Map.entry("build_torches", names("Light It Up", "让一切亮起来吧")),
            Map.entry("soil_enrichment", names("Soil Enrichment", "土壤增肥")),
            Map.entry("make_mycelium", names("Keep It Dark", "保持黑暗")),
            Map.entry("supersize_me", names("Supersize Me", "喂蘑菇")),
            Map.entry("plant_doctor", names("Plant Doctor", "植物医生")),
            Map.entry("well_rested", names("Well Rested", "休息充沛")),
            Map.entry("seaworthy", names("Seaworthy", "航海家")),
            Map.entry("explorer", names("Explorer", "探险家")),
            Map.entry("fishing_rod", names("Go Fishing", "去钓鱼")),
            Map.entry("cook_fish", names("Delicious Fish", "美味的鱼儿")),
            Map.entry("fine_dining", names("Fine Dining", "美食")));

    private static String[] names(String english, String chinese) {
        return new String[]{english, chinese};
    }
    enum Locale {
        EN_US("en_us", Map.ofEntries(
                Map.entry("itemGroup.infx", "InfiniteX: Blocks"),
                Map.entry("itemGroup.infx.ingredients", "InfiniteX: Ingredients"),
                Map.entry("itemGroup.infx.food_and_consumables", "InfiniteX: Food & Consumables"),
                Map.entry("itemGroup.infx.tools_and_utilities", "InfiniteX: Tools & Utilities"),
                Map.entry("itemGroup.infx.combat_and_equipment", "InfiniteX: Combat & Equipment"),
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
                Map.entry("block.infx.large_clay_oven", "Large Clay Oven"),
                Map.entry("block.infx.sandstone_furnace", "Sandstone Oven"),
                Map.entry("block.infx.hardened_clay_furnace", "Hardened Clay Furnace"),
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
                Map.entry("container.infx.large_clay_oven", "Large Clay Oven"),
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
                Map.entry("itemGroup.infx", "InfiniteX：方块"),
                Map.entry("itemGroup.infx.ingredients", "InfiniteX：原料"),
                Map.entry("itemGroup.infx.food_and_consumables", "InfiniteX：食物与消耗品"),
                Map.entry("itemGroup.infx.tools_and_utilities", "InfiniteX：工具与实用品"),
                Map.entry("itemGroup.infx.combat_and_equipment", "InfiniteX：战斗与装备"),
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
                Map.entry("block.infx.large_clay_oven", "大型粘土烤炉"),
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
                Map.entry("container.infx.large_clay_oven", "大型粘土烤炉"),
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
        FOOD_NAMES.forEach((path, names) -> add("item.infx." + path, names[locale == Locale.EN_US ? 0 : 1]));
        for (var material : ModItems.BUCKET_MATERIALS) {
            for (var contents : com.pixulse.infx.item.R196BucketItem.Contents.values()) {
                String englishContents = switch (contents) {
                    case EMPTY -> "";
                    case WATER -> " Water";
                    case LAVA -> " Lava";
                    case MILK -> " Milk";
                    case STONE -> " Stone";
                };
                String chineseContents = switch (contents) {
                    case EMPTY -> "";
                    case WATER -> "水";
                    case LAVA -> "岩浆";
                    case MILK -> "牛奶";
                    case STONE -> "石头";
                };
                add(
                        "item.infx." + contents.path(material),
                        locale == Locale.EN_US
                                ? material.englishNoun() + englishContents + " Bucket"
                                : material.chinesePrefix() + chineseContents + "桶");
            }
        }
        add("item.infx.bottle_of_disenchanting", locale == Locale.EN_US ? "Bottle of Disenchanting" : "祛魔之瓶");
        addRecord("underworld", "Underworld");
        addRecord("descent", "Descent");
        addRecord("wanderer", "Wanderer");
        addRecord("legends", "Legends");
        add("block.infx.rose", locale == Locale.EN_US ? "Rose" : "玫瑰");
        add("block.infx.orchid", locale == Locale.EN_US ? "Orchid" : "兰花");
        add("block.infx.allium", locale == Locale.EN_US ? "Allium" : "绒球葱");
        add("block.infx.tulip", locale == Locale.EN_US ? "Tulip" : "郁金香");
        add("block.infx.dahlia", locale == Locale.EN_US ? "Dahlia" : "大丽花");
        add("block.infx.daisy", locale == Locale.EN_US ? "Daisy" : "雏菊");
        add("block.infx.witherwood", locale == Locale.EN_US ? "Witherwood" : "凋零灌木");
        add("block.infx.nether_gravel", locale == Locale.EN_US ? "Nether Gravel" : "下界沙砾");
        add("block.infx.core", locale == Locale.EN_US ? "Core" : "地核");
        add("disconnect.infx.chat_spam", locale == Locale.EN_US ? "Kicked for chat spam" : "因聊天刷屏被踢出");
        add("disconnect.infx.reconnect_limited", locale == Locale.EN_US
                ? "Reconnect is limited until the next day around adjusted hour %s (%s seconds minimum)"
                : "重连受限：请等待次日调整时刻 %s 左右（至少 %s 秒）");
        ENCHANTMENT_NAMES.forEach((path, names) -> add("enchantment.infx." + path, names[locale == Locale.EN_US ? 0 : 1]));
        ModEntityTypes.names().forEach(entity -> add(
                "entity.infx." + entity.path(),
                locale == Locale.EN_US ? entity.english() : entity.chinese()));
        locale.baseTranslations.forEach(this::add);
        REMAINING_ADVANCEMENTS.forEach((path, names) -> {
            String name = names[locale == Locale.EN_US ? 0 : 1];
            add("advancements.infx." + path + ".title", name);
            add(
                    "advancements.infx." + path + ".description",
                    locale == Locale.EN_US
                            ? "Complete the R196 requirement: " + name
                            : "完成 R196 条件：" + name);
        });
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
        add("block.infx.mantle", locale == Locale.EN_US ? "Mantle" : "地幔");
        add("block.infx.mithril_rune_stone", locale == Locale.EN_US ? "Mithril Rune Stone" : "秘银符文石");
        add("block.infx.adamantium_rune_stone", locale == Locale.EN_US ? "Adamantium Rune Stone" : "艾德曼符文石");
        add("block.infx.emerald_enchanting_table", locale == Locale.EN_US ? "Emerald Enchanting Table" : "绿宝石附魔台");
        add("block.infx.diamond_enchanting_table", locale == Locale.EN_US ? "Diamond Enchanting Table" : "钻石附魔台");
        for (var safe : com.pixulse.infx.registry.ModBlocks.METAL_SAFES) {
            String material = safe.get().material().path();
            String name = locale == Locale.EN_US
                    ? safe.get().material().englishNoun() + " Safe"
                    : safe.get().material().chinesePrefix() + "保险箱";
            add("block.infx." + material + "_safe", name);
            add("container.infx." + material + "_safe", name);
        }
        add("message.infx.rune_selected", locale == Locale.EN_US ? "Rune pattern: %s" : "符文图案：%s");
        add("message.infx.underworld_bed_unsafe", locale == Locale.EN_US
                ? "It is not safe to sleep in the Underworld"
                : "地下世界不适合睡眠");
        add("message.infx.creative_disabled", locale == Locale.EN_US
                ? "Creative mode is disabled in R196 survival worlds"
                : "R196 生存世界禁止创造模式");
        add("message.infx.world_first", locale == Locale.EN_US
                ? "%s was first to earn %s on day %s"
                : "%s 首个完成 %s（第 %s 天）");
        add("message.infx.safe_obstructed", locale == Locale.EN_US ? "The safe is obstructed above" : "保险箱上方被遮挡");
        add("message.infx.enchanting_table_obstructed", locale == Locale.EN_US
                ? "The enchanting table needs clear space above it"
                : "附魔台上方需要留空");
        add("message.infx.safe_owned", locale == Locale.EN_US ? "This safe belongs to %s" : "该保险箱属于 %s");
        add("message.infx.safe_tool", locale == Locale.EN_US ? "A matching metal tool is required" : "需要对应金属等级的工具");
        add("message.infx.safe_foreign_tool", locale == Locale.EN_US ? "Another player's safe requires a tool one metal tier higher" : "破坏其他玩家的保险箱需要高一级金属工具");
        add("message.infx.disconnect_penalty", locale == Locale.EN_US ? "Combat disconnect penalty applied" : "已应用战斗断线惩罚");
        add("key.categories.infx.controls", locale == Locale.EN_US ? "InfiniteX" : "InfiniteX");
        add("key.infx.lock_sprint", locale == Locale.EN_US ? "Lock Sprint" : "锁定疾跑");
        add("key.infx.zoom", locale == Locale.EN_US ? "Zoom" : "拉近镜头");
        add("key.infx.reload_chunks", locale == Locale.EN_US ? "Reload Chunks" : "重载区块");
        add("key.infx.smart_pickup", locale == Locale.EN_US ? "Smart Harvest" : "智能采集模式");
        add("key.infx.smart_use", locale == Locale.EN_US ? "Smart Use" : "智能使用模式");
        add("key.infx.place_fluid_source", locale == Locale.EN_US ? "Place Fluid Source" : "放置流体源");
        add("message.infx.sprint_lock", locale == Locale.EN_US ? "Sprint lock: %s" : "疾跑锁定：%s");
        add("message.infx.smart_pickup", locale == Locale.EN_US ? "Smart harvest: %s" : "智能采集：%s");
        add("message.infx.smart_use", locale == Locale.EN_US ? "Smart use: %s" : "智能使用：%s");
        add("message.infx.chunks_reloaded", locale == Locale.EN_US ? "Chunks reloaded" : "区块已重载");
        add("effect.infx.malnutrition", locale == Locale.EN_US ? "Malnutrition" : "营养不良");
        add("effect.infx.witch_curse", locale == Locale.EN_US ? "Witch's Curse" : "女巫的诅咒");
        add("effect.infx.insulin_resistance", locale == Locale.EN_US ? "Insulin Resistance" : "胰岛素抵抗");
        add("effect.infx.paralysis", locale == Locale.EN_US ? "Paralysis" : "麻痹");
        String[] creationTitles = {"Boat", "Crypt", "Crystal", "Dragon", "Globe", "Serpent", "Sphinx", "Star", "Temple"};
        for (String title : creationTitles) {
            add("book.infx.creation." + title.toLowerCase(java.util.Locale.ROOT), locale == Locale.EN_US
                    ? "A fragment of Father Phoonzang's account: " + title
                    : "Father Phoonzang 的创世记载：《" + title + "》");
        }
        for (com.pixulse.infx.material.R196Quality quality : com.pixulse.infx.material.R196Quality.values()) {
            String english = switch (quality) {
                case WRETCHED -> "Wretched Quality";
                case POOR -> "Poor Quality";
                case FINE -> "Fine Quality";
                case EXCELLENT -> "Excellent Quality";
                case SUPERB -> "Superb Quality";
                case MASTERWORK -> "Masterwork";
                case LEGENDARY -> "Legendary";
            };
            String chinese = switch (quality) {
                case WRETCHED -> "破烂品质";
                case POOR -> "粗劣品质";
                case FINE -> "精良品质";
                case EXCELLENT -> "优秀品质";
                case SUPERB -> "卓越品质";
                case MASTERWORK -> "大师之作";
                case LEGENDARY -> "传奇品质";
            };
            add("quality.infx." + quality.getSerializedName(), locale == Locale.EN_US ? english : chinese);
        }
        for (com.pixulse.infx.material.R196Material material : com.pixulse.infx.material.R196Material.values()) {
            add(
                    "material.infx." + material.path(),
                    locale == Locale.EN_US ? material.englishNoun() : material.chinesePrefix());
        }
        add("tooltip.infx.material", locale == Locale.EN_US ? "Material: %s" : "材料：%s");
        add("tooltip.infx.damage", locale == Locale.EN_US ? "R196 damage: %s" : "R196 伤害：%s");
        add("tooltip.infx.reach", locale == Locale.EN_US ? "Melee reach: %s blocks" : "近战距离：%s 格");
        add("tooltip.infx.protection", locale == Locale.EN_US ? "Fixed protection: %s" : "固定防护：%s");
        add("tooltip.infx.repair", locale == Locale.EN_US ? "Repair with %s nuggets" : "使用%s粒修理");
        add("tooltip.infx.fuel_heat", locale == Locale.EN_US ? "Fuel heat: %s" : "燃料热量：%s");
        add("tooltip.infx.recipe_heat", locale == Locale.EN_US ? "Required heat: %s" : "所需热量：%s");
        add("tooltip.infx.furnace_heat", locale == Locale.EN_US ? "Maximum heat: %s" : "最高热量：%s");
        add("enchantment.infx.clumsiness", locale == Locale.EN_US ? "Curse of Clumsiness" : "笨拙诅咒");
    }

    private void addRecord(String path, String title) {
        add("item.infx.record_" + path, locale == Locale.EN_US ? "Music Disc - " + title : "音乐唱片 - " + title);
        add("jukebox_song.infx." + path, title + " — The Fat Man");
    }
}
