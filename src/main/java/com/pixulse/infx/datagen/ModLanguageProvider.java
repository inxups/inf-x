package com.pixulse.infx.datagen;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.InfxBucketItem;
import com.pixulse.infx.item.Catalog;
import com.pixulse.infx.item.MobBucketKind;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.item.material.Quality;
import com.pixulse.infx.registry.InfXBlocks;
import com.pixulse.infx.registry.InfXEntityTypes;
import com.pixulse.infx.registry.InfXItems;
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
    private static final Map<String, String[]> STRIPPED_LOG_NAMES = Map.ofEntries(
            Map.entry("oak", names("Oak", "橡木")),
            Map.entry("spruce", names("Spruce", "云杉木")),
            Map.entry("birch", names("Birch", "白桦木")),
            Map.entry("jungle", names("Jungle", "丛林木")),
            Map.entry("acacia", names("Acacia", "金合欢木")),
            Map.entry("cherry", names("Cherry", "樱花木")),
            Map.entry("pale_oak", names("Pale Oak", "苍白橡木")),
            Map.entry("dark_oak", names("Dark Oak", "深色橡木")),
            Map.entry("mangrove", names("Mangrove", "红树木")),
            Map.entry("crimson", names("Crimson Stem", "绯红菌柄")),
            Map.entry("warped", names("Warped Stem", "诡异菌柄")));
    private static final Map<String, String[]> GELATINOUS_SPHERE_NAMES = Map.ofEntries(
            Map.entry("green_gelatinous_sphere", names("Green Gelatinous Sphere", "绿色粘液球")),
            Map.entry("ochre_gelatinous_sphere", names("Ochre Gelatinous Sphere", "赭色粘液球")),
            Map.entry("crimson_gelatinous_sphere", names("Crimson Gelatinous Sphere", "深红粘液球")),
            Map.entry("gray_gelatinous_sphere", names("Gray Gelatinous Sphere", "灰色粘液球")),
            Map.entry("black_gelatinous_sphere", names("Black Gelatinous Sphere", "黑色粘液球")));
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
    private static final Map<String, String[]> CURSE_NAMES = Map.ofEntries(
            Map.entry("equipment_decay", names("Corrosive Skin", "腐蚀性皮肤")),
            Map.entry("cannot_hold_breath", names("Cannot Hold Breath", "不能屏住呼吸")),
            Map.entry("cannot_run", names("Cannot Run", "不能疾跑")),
            Map.entry("cannot_eat_animals", names("Cannot Eat Animal Products", "不能食肉")),
            Map.entry("cannot_eat_plants", names("Cannot Eat Plant Products", "不能食素")),
            Map.entry("cannot_drink", names("Cannot Drink", "不能饮用")),
            Map.entry("endermen_aggro", names("Endermen's Enemy", "末影仇恨")),
            Map.entry("clumsiness", names("Clumsiness", "智力下降")),
            Map.entry("entanglement", names("Entanglement", "植物恐惧")),
            Map.entry("cannot_wear_armor", names("Armor Rejection", "盔甲排斥")),
            Map.entry("cannot_open_chests", names("Fear of Chests", "箱子恐惧")),
            Map.entry("cannot_sleep", names("Insomnia", "失眠症")),
            Map.entry("fear_of_spiders", names("Fear of Spiders", "蜘蛛恐惧")),
            Map.entry("fear_of_wolves", names("Fear of Wolves", "恶狼恐惧")),
            Map.entry("fear_of_creepers", names("Fear of Creepers", "苦力怕恐惧")),
            Map.entry("fear_of_undead", names("Fear of Undead", "亡灵生物恐惧")));
    private static final Map<String, String[]> CURSE_DESCRIPTIONS = Map.ofEntries(
            Map.entry("equipment_decay", names(
                    "Your equipment, weapons, and tools lose durability twice as fast",
                    "你的装备、武器与工具的耐久度下降得更快")),
            Map.entry("cannot_hold_breath", names(
                    "You cannot hold your breath for very long",
                    "你不能长时间屏住呼吸")),
            Map.entry("cannot_run", names("You cannot sprint", "你不能疾跑")),
            Map.entry("cannot_eat_animals", names(
                    "You cannot eat any animal products",
                    "你不能食用任何动物制品")),
            Map.entry("cannot_eat_plants", names(
                    "You cannot eat any plant products",
                    "你不能食用任何植物制品")),
            Map.entry("cannot_drink", names(
                    "You cannot drink potions, milk, water, or soups",
                    "你不能饮用药水、牛奶、水或汤类食物")),
            Map.entry("endermen_aggro", names(
                    "Endermen may attack you without provocation",
                    "末影人会无缘无故地对你发起攻击")),
            Map.entry("clumsiness", names(
                    "Crafting takes longer, costs more experience, and may produce worse equipment",
                    "制作物品耗时更久、经验消耗更多，并可能产出更差的装备")),
            Map.entry("entanglement", names(
                    "Vines and plants greatly impede your movement",
                    "藤蔓和植物会严重阻碍你的移动")),
            Map.entry("cannot_wear_armor", names(
                    "You cannot keep armor equipped",
                    "你不能穿着盔甲")),
            Map.entry("cannot_open_chests", names(
                    "You cannot open chests or safes",
                    "你不能使用箱子或保险箱")),
            Map.entry("cannot_sleep", names("You cannot sleep", "你无法入睡")),
            Map.entry("fear_of_spiders", names(
                    "Three out of four attacks against spiders fail",
                    "你对蜘蛛发起的四次攻击中通常只有一次有效")),
            Map.entry("fear_of_wolves", names(
                    "Three out of four attacks against wolves fail",
                    "你对狼发起的四次攻击中通常只有一次有效")),
            Map.entry("fear_of_creepers", names(
                    "Three out of four attacks against creepers fail",
                    "你对苦力怕发起的四次攻击中通常只有一次有效")),
            Map.entry("fear_of_undead", names(
                    "Three out of four attacks against undead creatures fail",
                    "你对亡灵生物发起的四次攻击中通常只有一次有效")));
    private static final Map<String, String[]> PROGRESSION_NAMES = Map.ofEntries(
            Map.entry("first_steps", names("First Steps", "第一步")),
            Map.entry("flint_kit", names("Flint Kit", "燧石工具")),
            Map.entry("flint_workbench", names("Flint Workbench", "燧石工具台")),
            Map.entry("first_furnace", names("First Furnace", "第一座熔炉")),
            Map.entry("copper_workbench", names("Copper Age", "铜器时代")),
            Map.entry("iron_age", names("Iron Age", "铁器时代")),
            Map.entry("obsidian_furnace", names("Lava Heat", "熔岩之热")),
            Map.entry("ancient_metal_age", names("Ancient Metal", "远古金属")),
            Map.entry("mithril_age", names("Mithril Age", "秘银时代")),
            Map.entry("adamantium_age", names("Adamantium Age", "艾德曼时代")),
            Map.entry("masterwork", names("Masterwork", "大师之作")),
            Map.entry("leather_armor", names("Suiting Up", "穿上护甲")),
            Map.entry("metal_armor", names("Metal Shell", "金属战甲")),
            Map.entry("adamantium_armor", names("Juggernaut", "世界主宰")),
            Map.entry("farming", names("Time to Farm", "农耕时间")),
            Map.entry("food", names("Cooked Cuisine", "熟食之道")),
            Map.entry("enchanting", names("Enchanter", "附魔师")),
            Map.entry("bookcase", names("Knowledge is Power", "知识就是力量")),
            Map.entry("enlightenment", names("Enlightenment", "启蒙")),
            Map.entry("underworld", names("The Underworld", "地下世界")),
            Map.entry("nether", names("The Nether", "下界")),
            Map.entry("nether_forge", names("Nether Forge", "下界熔炉")),
            Map.entry("rune_gate", names("Rune Gate", "符文之门")),
            Map.entry("the_end", names("The End?", "末地？")),
            Map.entry("the_end2", names("The End.", "末地。")));
    private static final Map<String, String[]> PROGRESSION_DESCRIPTIONS = Map.ofEntries(
            Map.entry("first_steps", names("Pick up your first item and begin surviving", "拾起第一件物品，开始求生")),
            Map.entry("flint_kit", names("Craft a flint hatchet, knife, shovel, or axe", "制作一把燧石短斧、小刀、锹或斧")),
            Map.entry("flint_workbench", names("Craft a stripped-log flint or obsidian workbench", "制作去皮原木燧石或黑曜石工具台")),
            Map.entry("first_furnace", names("Craft a clay, sandstone, hardened clay, or cobblestone furnace", "制作黏土、砂岩、陶瓦或圆石熔炉")),
            Map.entry("copper_workbench", names("Craft a copper, silver, or gold workbench", "制作铜、银或金工具台")),
            Map.entry("iron_age", names("Smelt iron or craft an iron workbench and pickaxe", "冶炼铁锭，或制作铁工具台与铁镐")),
            Map.entry("obsidian_furnace", names("Craft an obsidian furnace", "制作黑曜石熔炉")),
            Map.entry("ancient_metal_age", names("Craft an ancient metal workbench", "制作远古金属工具台")),
            Map.entry("mithril_age", names("Smelt mithril or craft a mithril workbench", "冶炼秘银，或制作秘银工具台")),
            Map.entry("adamantium_age", names("Smelt adamantium or craft an adamantium workbench", "冶炼艾德曼，或制作艾德曼工具台")),
            Map.entry("masterwork", names("Craft an adamantium pickaxe or war hammer", "制作艾德曼镐或战锤")),
            Map.entry("leather_armor", names("Wear a full set of leather armor", "穿上整套皮革护甲")),
            Map.entry("metal_armor", names("Wear a full set of non-adamantium metal armor", "穿上整套非艾德曼金属护甲")),
            Map.entry("adamantium_armor", names("Wear a full set of adamantium armor", "穿上整套艾德曼护甲")),
            Map.entry("farming", names("Craft a copper, silver, gold, or iron hoe", "制作铜、银、金或铁锄")),
            Map.entry("food", names("Craft flour, dough, bread, or an INFX soup", "制作面粉、面团、面包或 InfX 汤类食物")),
            Map.entry("enchanting", names("Craft an emerald or diamond enchanting table", "制作绿宝石或钻石附魔台")),
            Map.entry("bookcase", names("Craft a bookshelf", "制作书架")),
            Map.entry("enlightenment", names("Read all nine creation books", "读完九本创世之书")),
            Map.entry("underworld", names("Enter the Underworld", "进入地下世界")),
            Map.entry("nether", names("Enter the Nether", "进入下界")),
            Map.entry("nether_forge", names("Craft a netherrack furnace", "制作下界岩熔炉")),
            Map.entry("rune_gate", names("Use a rune gate", "使用符文门")),
            Map.entry("the_end", names("Enter the End", "进入末地")),
            Map.entry("the_end2", names("Defeat the ender dragon and return", "击败末影龙并返回")));

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
                Map.entry("itemGroup.infx.spawn_eggs", "InfiniteX: Spawn Eggs"),
                Map.entry("block.infx.copper_workbench", "Copper Workbench"),
                Map.entry("block.infx.silver_workbench", "Silver Workbench"),
                Map.entry("block.infx.gold_workbench", "Gold Workbench"),
                Map.entry("block.infx.iron_workbench", "Iron Workbench"),
                Map.entry("block.infx.ancient_metal_workbench", "Ancient Metal Workbench"),
                Map.entry("block.infx.mithril_workbench", "Mithril Workbench"),
                Map.entry("block.infx.adamantium_workbench", "Adamantium Workbench"),
                Map.entry("block.infx.snow_slab", "Snow Slab"),
                Map.entry("block.infx.infx_onions", "Onion Plant"),
                Map.entry("block.infx.clay_furnace", "Clay Oven"),
                Map.entry("block.infx.sandstone_furnace", "Sandstone Oven"),
                Map.entry("block.infx.hardened_clay_furnace", "Hardened Clay Furnace"),
                Map.entry("block.infx.obsidian_furnace", "Obsidian Furnace"),
                Map.entry("block.infx.netherrack_furnace", "Netherrack Furnace"),
                Map.entry("block.infx.silver_ore", "Silver Ore"),
                Map.entry("block.infx.deepslate_silver_ore", "Deepslate Silver Ore"),
                Map.entry("block.infx.mithril_ore", "Mithril Ore"),
                Map.entry("block.infx.deepslate_mithril_ore", "Deepslate Mithril Ore"),
                Map.entry("block.infx.adamantium_ore", "Adamantium Ore"),
                Map.entry("block.infx.deepslate_adamantium_ore", "Deepslate Adamantium Ore"),
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
                Map.entry("container.infx.hardened_clay_furnace", "Hardened Clay Furnace"),
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
                Map.entry("jei.infx.required_bench", "Required: %s"),
                Map.entry("message.infx.workbench_obstructed", "The workbench needs clear space above it")
                )) {
            @Override
            String name(Catalog.Entry entry) {
                return entry.englishName();
            }
        },
        ZH_CN("zh_cn", Map.ofEntries(
                Map.entry("itemGroup.infx", "InfiniteX：方块"),
                Map.entry("itemGroup.infx.ingredients", "InfiniteX：原料"),
                Map.entry("itemGroup.infx.food_and_consumables", "InfiniteX：食物与消耗品"),
                Map.entry("itemGroup.infx.tools_and_utilities", "InfiniteX：工具与实用品"),
                Map.entry("itemGroup.infx.combat_and_equipment", "InfiniteX：战斗与装备"),
                Map.entry("itemGroup.infx.spawn_eggs", "InfiniteX：刷怪蛋"),
                Map.entry("block.infx.copper_workbench", "铜工具台"),
                Map.entry("block.infx.silver_workbench", "银工具台"),
                Map.entry("block.infx.gold_workbench", "金工具台"),
                Map.entry("block.infx.iron_workbench", "铁工具台"),
                Map.entry("block.infx.ancient_metal_workbench", "远古金属工具台"),
                Map.entry("block.infx.mithril_workbench", "秘银工具台"),
                Map.entry("block.infx.adamantium_workbench", "艾德曼工具台"),
                Map.entry("block.infx.snow_slab", "雪台阶"),
                Map.entry("block.infx.infx_onions", "洋葱植株"),
                Map.entry("block.infx.clay_furnace", "粘土炉"),
                Map.entry("block.infx.sandstone_furnace", "沙石炉"),
                Map.entry("block.infx.hardened_clay_furnace", "陶瓦炉"),
                Map.entry("block.infx.obsidian_furnace", "黑曜石熔炉"),
                Map.entry("block.infx.netherrack_furnace", "下界岩熔炉"),
                Map.entry("block.infx.silver_ore", "银矿石"),
                Map.entry("block.infx.deepslate_silver_ore", "深板岩银矿石"),
                Map.entry("block.infx.mithril_ore", "秘银矿石"),
                Map.entry("block.infx.deepslate_mithril_ore", "深板岩秘银矿石"),
                Map.entry("block.infx.adamantium_ore", "艾德曼矿石"),
                Map.entry("block.infx.deepslate_adamantium_ore", "深板岩艾德曼矿石"),
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
                Map.entry("jei.infx.required_bench", "需要：%s"),
                Map.entry("message.infx.workbench_obstructed", "工具台上方需要留出空间")
                )) {
            @Override
            String name(Catalog.Entry entry) {
                return entry.chineseName();
            }
        };

        final String code;
        final Map<String, String> baseTranslations;

        Locale(String code, Map<String, String> baseTranslations) {
            this.code = code;
            this.baseTranslations = baseTranslations;
        }

        abstract String name(Catalog.Entry entry);
    }

    private final Locale locale;

    ModLanguageProvider(PackOutput output, Locale locale) {
        super(output, InfiniteX.MOD_ID, locale.code);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        InfXItems.catalog().entries().forEach(entry -> add("item.infx." + entry.path(), locale.name(entry)));
        FOOD_NAMES.forEach((path, names) -> add("item.infx." + path, names[locale == Locale.EN_US ? 0 : 1]));
        GELATINOUS_SPHERE_NAMES.forEach(
                (path, names) -> add("item.infx." + path, names[locale == Locale.EN_US ? 0 : 1]));
        for (var hookMaterial : InfXItems.FISHING_HOOK_MATERIALS) {
            add(
                    "item.infx." + hookMaterial.path() + "_carrot_on_a_stick",
                    locale == Locale.EN_US
                            ? hookMaterial.englishEquipmentPrefix() + " Carrot on a Stick"
                            : "胡萝卜" + hookMaterial.chinesePrefix() + "钓竿");
        }
        CURSE_NAMES.forEach((path, names) ->
                add("curse.infx." + path + ".name", names[locale == Locale.EN_US ? 0 : 1]));
        CURSE_DESCRIPTIONS.forEach((path, descriptions) ->
                add("curse.infx." + path + ".desc", descriptions[locale == Locale.EN_US ? 0 : 1]));
        for (var material : InfXItems.BUCKET_MATERIALS) {
            for (var contents : InfxBucketItem.Contents.values()) {
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
                    case MILK -> "奶";
                    case STONE -> "石头";
                };
                add(
                        "item.infx." + contents.path(material),
                        locale == Locale.EN_US
                                ? material.englishNoun() + englishContents + " Bucket"
                                : material.chinesePrefix() + chineseContents + "桶");
            }
            for (var kind : MobBucketKind.values()) {
                add(
                        "item.infx." + kind.path(material),
                        locale == Locale.EN_US
                                ? material.englishNoun() + " " + kind.englishName() + " Bucket"
                                : material.chinesePrefix() + kind.chineseName() + "桶");
            }
            add(
                    "item.infx.powder_snow_" + material.path() + "_bucket",
                    locale == Locale.EN_US
                            ? material.englishNoun() + " Powder Snow Bucket"
                            : material.chinesePrefix() + "细雪桶");
        }
        add("item.infx.bottle_of_disenchanting", locale == Locale.EN_US ? "Bottle of Disenchanting" : "祛魔之瓶");
        addRecord("underworld", "Underworld");
        addRecord("descent", "Descent");
        addRecord("wanderer", "Wanderer");
        addRecord("legends", "Legends");
        add("block.infx.witherwood", locale == Locale.EN_US ? "Witherwood" : "凋零灌木");
        add("death.infx.poison", locale == Locale.EN_US ? "%s was poisoned to death" : "%s 毒发身亡");
        add("block.infx.blueberry_bush", locale == Locale.EN_US ? "Blueberry Bush" : "蓝莓丛");
        add("block.infx.infx_wheat", locale == Locale.EN_US ? "Wheat Crop" : "小麦作物");
        add("block.infx.infx_carrots", locale == Locale.EN_US ? "Carrot Crop" : "胡萝卜作物");
        add("block.infx.infx_potatoes", locale == Locale.EN_US ? "Potato Crop" : "马铃薯作物");
        add("block.infx.infx_beetroots", locale == Locale.EN_US ? "Beetroot Crop" : "甜菜根作物");
        add("block.infx.sgravel", locale == Locale.EN_US ? "Gravel" : "砾石");
        add("block.infx.nether_gravel", locale == Locale.EN_US ? "Nether Gravel" : "下界沙砾");
        add("block.infx.core", locale == Locale.EN_US ? "Core" : "地核");
        add("block.infx.infested_netherrack", locale == Locale.EN_US ? "Infested Netherrack" : "虫蚀下界岩");
        add("block.infx.underworld_portal", locale == Locale.EN_US ? "Underworld Portal" : "地下世界传送门");
        add("block.infx.nether_portal", locale == Locale.EN_US ? "Nether Portal" : "下界传送门");
        add("block.infx.return_spawn_portal", locale == Locale.EN_US ? "Return Spawn Portal" : "返回出生点传送门");
        ENCHANTMENT_NAMES.forEach((path, names) -> add("enchantment.infx." + path, names[locale == Locale.EN_US ? 0 : 1]));
        InfXEntityTypes.names().forEach(entity -> {
            add(
                    "entity.infx." + entity.path(),
                    locale == Locale.EN_US ? entity.english() : entity.chinese());
            add(
                    "item.infx." + entity.path() + "_spawn_egg",
                    locale == Locale.EN_US
                            ? entity.english() + " Spawn Egg"
                            : entity.chinese() + "刷怪蛋");
        });
        locale.baseTranslations.forEach(this::add);
        for (var workbench : InfXBlocks.STRIPPED_LOG_WORKBENCHES) {
            String[] woodNames = STRIPPED_LOG_NAMES.get(workbench.wood());
            String prefix = "stripped_" + workbench.wood();
            String flintName = locale == Locale.EN_US
                    ? "Stripped " + woodNames[0] + " Flint Workbench"
                    : "去皮" + woodNames[1] + "燧石工具台";
            String obsidianName = locale == Locale.EN_US
                    ? "Stripped " + woodNames[0] + " Obsidian Workbench"
                    : "去皮" + woodNames[1] + "黑曜石工具台";
            add("block.infx." + prefix + "_flint_workbench", flintName);
            add("container.infx." + prefix + "_flint_workbench", flintName);
            add("block.infx." + prefix + "_obsidian_workbench", obsidianName);
            add("container.infx." + prefix + "_obsidian_workbench", obsidianName);
        }
        PROGRESSION_NAMES.forEach((path, names) -> {
            add("advancements.infx." + path + ".title", names[locale == Locale.EN_US ? 0 : 1]);
        });
        PROGRESSION_DESCRIPTIONS.forEach((path, descriptions) -> {
            add("advancements.infx." + path + ".description", descriptions[locale == Locale.EN_US ? 0 : 1]);
        });
        for (var anvil : InfXBlocks.METAL_ANVILS) {
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
        for (var safe : InfXBlocks.METAL_SAFES) {
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
        add("message.infx.bed.not_sheltered", locale == Locale.EN_US
                ? "A bed needs shelter from the sky"
                : "床需要遮蔽天空");
        add("message.infx.bed.too_hungry", locale == Locale.EN_US
                ? "You are too hungry to sleep"
                : "你太饥饿，无法入睡");
        add("message.infx.bed.poisoned", locale == Locale.EN_US
                ? "You cannot sleep while poisoned"
                : "中毒时无法入睡");
        add("message.infx.bed.mobs_digging", locale == Locale.EN_US
                ? "Nearby zombies are digging"
                : "附近的僵尸正在挖掘");
        add("message.infx.bed.wake_hungry", locale == Locale.EN_US
                ? "You wake up too hungry to continue sleeping"
                : "你因过于饥饿而醒来");
        add("message.infx.bed.wake_mobs", locale == Locale.EN_US
                ? "You wake up to nearby monsters"
                : "附近的怪物惊醒了你");
        add("message.infx.bed.unsafe_dimension", locale == Locale.EN_US
                ? "Beds can only be used in the Overworld"
                : "床只能在主世界使用");
        add("message.infx.creative_disabled", locale == Locale.EN_US
                ? "Creative mode is disabled in INFX survival worlds"
                : "INFX 生存世界禁止创造模式");
        add("menu.infx.test_mode", locale == Locale.EN_US ? "INFX TEST MODE" : "INFX测试模式");
        add("disconnect.infx.test_mode_mismatch", locale == Locale.EN_US
                ? "InfiniteX test mode must match the server"
                : "InfiniteX 测试模式必须与服务端一致");
        add("message.infx.server_management_disabled", locale == Locale.EN_US
                ? "This server command is disabled outside InfiniteX test mode"
                : "该服务端命令在非 InfiniteX 测试模式下被禁用");
        add("message.infx.safe_obstructed", locale == Locale.EN_US ? "The safe is obstructed above" : "保险箱上方被遮挡");
        add("message.infx.enchanting_table_obstructed", locale == Locale.EN_US
                ? "The enchanting table needs clear space above it"
                : "附魔台上方需要留空");
        add("message.infx.safe_owned", locale == Locale.EN_US ? "This safe belongs to %s" : "该保险箱属于 %s");
        add("message.infx.safe_tool", locale == Locale.EN_US ? "A matching metal tool is required" : "需要对应金属等级的工具");
        add("message.infx.safe_foreign_tool", locale == Locale.EN_US ? "Another player's safe requires a tool one metal tier higher" : "破坏其他玩家的保险箱需要高一级金属工具");
        add("effect.infx.malnutrition", locale == Locale.EN_US ? "Malnutrition" : "营养不良");
        add("effect.infx.witch_curse", locale == Locale.EN_US ? "Witch's Curse" : "女巫的诅咒");
        add("curse.infx.unknown", locale == Locale.EN_US ? "Unknown Effect" : "未知效果");
        add("hud.infx.curse", locale == Locale.EN_US ? "Witch's Curse" : "女巫的诅咒");
        add("message.infx.curse.realized", locale == Locale.EN_US ? "You have been cursed!" : "你已经被诅咒了！");
        add("message.infx.curse.learned", locale == Locale.EN_US ? "Curse revealed: %s" : "诅咒效果已揭示：%s");
        add("message.infx.curse.lifted", locale == Locale.EN_US ? "Your curse has been lifted!" : "你的诅咒解除了！");
        add("message.infx.curse.cannot_sleep", locale == Locale.EN_US
                ? "Your curse prevents you from sleeping"
                : "诅咒使你无法入睡");
        add("effect.infx.insulin_resistance", locale == Locale.EN_US ? "Insulin Resistance" : "胰岛素抵抗");
        add("effect.infx.paralysis", locale == Locale.EN_US ? "Paralysis" : "麻痹");
        String[] creationTitles = {"Boat", "Crypt", "Crystal", "Dragon", "Globe", "Serpent", "Sphinx", "Star", "Temple"};
        for (String title : creationTitles) {
            add("book.infx.creation." + title.toLowerCase(java.util.Locale.ROOT), locale == Locale.EN_US
                    ? "A fragment of Father Phoonzang's account: " + title
                    : "Father Phoonzang 的创世记载：《" + title + "》");
        }
        for (Quality quality : Quality.values()) {
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
        for (InfxMaterial material : InfxMaterial.values()) {
            add(
                    "material.infx." + material.path(),
                    locale == Locale.EN_US ? material.englishNoun() : material.chinesePrefix());
        }
        add("tooltip.infx.material", locale == Locale.EN_US ? "Material: %s" : "材料：%s");
        add("tooltip.infx.crafting_experience", locale == Locale.EN_US
                ? "Experience cost: %s XP"
                : "经验消耗：%s 点");
        add("tooltip.infx.damage", locale == Locale.EN_US ? "INFX damage: %s" : "INFX 伤害：%s");
        add("tooltip.infx.reach", locale == Locale.EN_US ? "Melee reach: %s blocks" : "近战距离：%s 格");
        add("tooltip.infx.protection", locale == Locale.EN_US ? "Fixed protection: %s" : "固定防护：%s");
        add("tooltip.infx.repair", locale == Locale.EN_US ? "Repair with %s nuggets" : "使用%s粒修理");
        add("tooltip.infx.silver_undead_bonus", locale == Locale.EN_US
                ? "+25% damage to undead"
                : "对亡灵生物伤害+25%");
        add("tooltip.infx.silver_armor_resistance", locale == Locale.EN_US
                ? "Each silver piece shortens negative effects by 15%"
                : "每件银甲使负面效果时长缩短15%");
        add("tooltip.infx.skeleton_bane", locale == Locale.EN_US
                ? "+2 damage to skeletons"
                : "对骷髅生物伤害+2");
        add("tooltip.infx.fuel_heat", locale == Locale.EN_US ? "Fuel heat: %s" : "燃料热量：%s");
        add("tooltip.infx.recipe_heat", locale == Locale.EN_US ? "Required heat: %s" : "所需热量：%s");
        add("tooltip.infx.furnace_heat", locale == Locale.EN_US ? "Maximum heat: %s" : "最高热量：%s");
        add("tooltip.infx.food.satiation", locale == Locale.EN_US ? "Satiation: +%s" : "饱食度：+%s");
        add("tooltip.infx.food.nutrition", locale == Locale.EN_US ? "Nutrition: +%s" : "营养：+%s");
        add("tooltip.infx.food.protein", locale == Locale.EN_US ? "Protein: +%s" : "蛋白质：+%s");
        add("tooltip.infx.food.essential_fats", locale == Locale.EN_US ? "Essential fats: +%s" : "必需脂肪：+%s");
        add("tooltip.infx.food.phytonutrients", locale == Locale.EN_US ? "Phytonutrients: +%s" : "植物营养素：+%s");
        add("tooltip.infx.food.sugar", locale == Locale.EN_US ? "Sugar: +%s" : "糖分：+%s");
        add("tooltip.infx.food.insulin_response", locale == Locale.EN_US ? "Insulin response: +%s" : "胰岛素反应：+%s");
        add(
                "tooltip.infx.place_bucket_as_source",
                locale == Locale.EN_US ? "Hold Ctrl to place a source block" : "按住Ctrl键来放置源头");
        add(
                "tooltip.infx.when_bucket_filled",
                locale == Locale.EN_US ? "When filled with lava:" : "捞岩浆时:");
        add(
                "tooltip.infx.chance_of_bucket_melting",
                locale == Locale.EN_US ? "%s%% chance of melting" : "有%s%%的几率融化");
        add("enchantment.infx.clumsiness", locale == Locale.EN_US ? "Curse of Clumsiness" : "笨拙诅咒");
    }

    private void addRecord(String path, String title) {
        add("item.infx.record_" + path, locale == Locale.EN_US ? "Music Disc - " + title : "音乐唱片 - " + title);
        add("jukebox_song.infx." + path, title + " — The Fat Man");
    }
}
