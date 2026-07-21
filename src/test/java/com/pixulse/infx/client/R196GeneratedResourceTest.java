package com.pixulse.infx.client;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pixulse.infx.crafting.BenchTier;
import com.pixulse.infx.item.R196Catalog;
import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.registry.ModItems;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("generated-resources")
class R196GeneratedResourceTest {
    private static final Path ROOT = findProjectRoot();
    private static final Path GENERATED = ROOT.resolve("src/generated/resources");
    private static final Path STATIC = ROOT.resolve("src/main/resources");

    @Test
    void r196SpawnTablesUseCorrectPoolsAndSources() throws Exception {
        JsonObject modifier = json(GENERATED.resolve(
                "data/infx/neoforge/biome_modifier/r196_spawns.json"));
        JsonObject infestedStone = json(GENERATED.resolve(
                "data/infx/worldgen/configured_feature/r196_infested_stone.json"));
        JsonObject infestedNetherrack = json(GENERATED.resolve(
                "data/infx/worldgen/configured_feature/r196_infested_netherrack.json"));
        JsonObject underworld = json(GENERATED.resolve("data/infx/worldgen/biome/underworld.json"));
        String underworldMonsters = underworld
                .getAsJsonObject("spawners")
                .get("monster")
                .toString();
        String underworldAmbient = underworld
                .getAsJsonObject("spawners")
                .get("ambient")
                .toString();
        String underworldWater = underworld
                .getAsJsonObject("spawners")
                .get("water_creature")
                .toString();

        assertAll(
                "R196 spawn tables",
                () -> assertEquals("infx:r196_spawns", modifier.get("type").getAsString()),
                () -> assertTrue(infestedStone.toString().contains("minecraft:infested_stone")),
                () -> assertTrue(infestedNetherrack.toString().contains("infx:infested_netherrack")),
                () -> assertTrue(Files.isRegularFile(
                        GENERATED.resolve("assets/infx/blockstates/infested_netherrack.json"))),
                () -> assertFalse(underworldMonsters.contains("minecraft:zombie")),
                () -> assertFalse(underworldMonsters.contains("minecraft:skeleton")),
                () -> assertFalse(underworldMonsters.contains("infx:fire_elemental")),
                () -> assertFalse(underworldMonsters.contains("infx:hoary_silverfish")),
                () -> assertTrue(underworldAmbient.contains("infx:vampire_bat")),
                () -> assertTrue(underworldAmbient.contains("infx:nightwing")),
                () -> assertFalse(underworldAmbient.contains("infx:giant_vampire_bat")),
                () -> assertTrue(underworldWater.contains("minecraft:squid")));
    }

    @Test
    void everyCatalogItemHasDefinitionModelAndTwoTranslations() throws Exception {
        JsonObject english = json(GENERATED.resolve("assets/infx/lang/en_us.json"));
        JsonObject chinese = json(GENERATED.resolve("assets/infx/lang/zh_cn.json"));
        for (R196Catalog.Entry entry : ModItems.catalog().entries()) {
            Path definition = GENERATED.resolve("assets/infx/items/" + entry.path() + ".json");
            Path model = GENERATED.resolve("assets/infx/models/item/" + entry.path() + ".json");
            assertAll(
                    entry.path(),
                    () -> assertTrue(Files.isRegularFile(definition), "missing item definition"),
                    () -> assertTrue(Files.isRegularFile(model), "missing base model"),
                    () -> assertTrue(english.has("item.infx." + entry.path()), "missing en_us"),
                    () -> assertTrue(chinese.has("item.infx." + entry.path()), "missing zh_cn"));
        }
    }

    @Test
    void creativeTabsHaveEnglishAndChineseTranslations() throws Exception {
        JsonObject english = json(GENERATED.resolve("assets/infx/lang/en_us.json"));
        JsonObject chinese = json(GENERATED.resolve("assets/infx/lang/zh_cn.json"));
        Map<String, String> englishNames = Map.of(
                "itemGroup.infx", "InfiniteX: Blocks",
                "itemGroup.infx.ingredients", "InfiniteX: Ingredients",
                "itemGroup.infx.food_and_consumables", "InfiniteX: Food & Consumables",
                "itemGroup.infx.tools_and_utilities", "InfiniteX: Tools & Utilities",
                "itemGroup.infx.combat_and_equipment", "InfiniteX: Combat & Equipment");
        Map<String, String> chineseNames = Map.of(
                "itemGroup.infx", "InfiniteX：方块",
                "itemGroup.infx.ingredients", "InfiniteX：原料",
                "itemGroup.infx.food_and_consumables", "InfiniteX：食物与消耗品",
                "itemGroup.infx.tools_and_utilities", "InfiniteX：工具与实用品",
                "itemGroup.infx.combat_and_equipment", "InfiniteX：战斗与装备");

        englishNames.forEach((key, name) -> assertAll(
                key,
                () -> assertEquals(name, english.get(key).getAsString()),
                () -> assertEquals(chineseNames.get(key), chinese.get(key).getAsString())));
    }

    @Test
    void everyWorkbenchHasClientDataLootRecipeAndTranslations() throws Exception {
        JsonObject english = json(GENERATED.resolve("assets/infx/lang/en_us.json"));
        JsonObject chinese = json(GENERATED.resolve("assets/infx/lang/zh_cn.json"));
        for (BenchTier tier : BenchTier.values()) {
            if (!tier.isWorkbench()) {
                continue;
            }
            String path = tier.serializedName() + "_workbench";
            assertAll(
                    path,
                    () -> assertTrue(Files.isRegularFile(STATIC.resolve("assets/infx/blockstates/" + path + ".json"))),
                    () -> assertTrue(Files.isRegularFile(STATIC.resolve("assets/infx/items/" + path + ".json"))),
                    () -> assertTrue(Files.isRegularFile(STATIC.resolve("assets/infx/models/block/" + path + ".json"))),
                    () -> assertTrue(Files.isRegularFile(GENERATED.resolve("data/infx/loot_table/blocks/" + path + ".json"))),
                    () -> assertTrue(Files.isRegularFile(GENERATED.resolve("data/infx/recipe/" + path + ".json"))),
                    () -> assertTrue(english.has("block.infx." + path)),
                    () -> assertTrue(english.has("container.infx." + path)),
                    () -> assertTrue(chinese.has("block.infx." + path)),
                    () -> assertTrue(chinese.has("container.infx." + path)));
        }
    }

    @Test
    void copperToIronProgressionDataIsComplete() throws Exception {
        for (String recipe : List.of("flint_shovel", "cobblestone_furnace", "iron_pickaxe")) {
            assertTrue(
                    Files.isRegularFile(GENERATED.resolve("data/infx/recipe/" + recipe + ".json")),
                    recipe);
        }
        for (String advancement : List.of(
                "build_shovel", "build_furnace", "acquire_iron", "build_better_pickaxe")) {
            assertTrue(
                    Files.isRegularFile(
                            GENERATED.resolve("data/infx/advancement/progression/" + advancement + ".json")),
                    advancement);
        }
        for (String disabled : List.of(
                "iron_ingot_from_blasting_deepslate_iron_ore",
                "iron_ingot_from_blasting_iron_ore",
                "iron_ingot_from_blasting_raw_iron",
                "iron_pickaxe")) {
            assertTrue(
                    Files.isRegularFile(GENERATED.resolve("data/minecraft/recipe/" + disabled + ".json")),
                    disabled);
        }
    }

    @Test
    void earlyCoreToolRecipesKeepR196DifficultiesAndBenchTiers() throws Exception {
        Map<String, Float> difficulties = Map.ofEntries(
                Map.entry("flint_axe", 375.0F),
                Map.entry("copper_pickaxe", 1250.0F),
                Map.entry("copper_shovel", 450.0F),
                Map.entry("copper_axe", 1250.0F),
                Map.entry("copper_hoe", 850.0F),
                Map.entry("copper_sword", 825.0F),
                Map.entry("iron_pickaxe", 2450.0F),
                Map.entry("iron_shovel", 850.0F),
                Map.entry("iron_axe", 2450.0F),
                Map.entry("iron_hoe", 1650.0F),
                Map.entry("iron_sword", 1625.0F));
        for (var entry : difficulties.entrySet()) {
            String recipeName = entry.getKey();
            JsonObject recipe = json(GENERATED.resolve("data/infx/recipe/" + recipeName + ".json"));
            String requiredBench = recipeName.substring(0, recipeName.indexOf('_'));
            assertAll(
                    recipeName,
                    () -> assertEquals(entry.getValue(), recipe.get("difficulty").getAsFloat()),
                    () -> assertEquals(requiredBench, recipe.get("required_bench").getAsString()),
                    () -> assertEquals(
                            "infx:" + recipeName,
                            recipe.getAsJsonObject("result").get("id").getAsString()));
        }
        for (String advancement : List.of("build_axe", "build_hoe")) {
            assertTrue(
                    Files.isRegularFile(
                            GENERATED.resolve("data/infx/advancement/progression/" + advancement + ".json")),
                    advancement);
        }
        for (String disabled : List.of("iron_axe", "iron_hoe", "iron_shovel", "iron_sword")) {
            assertTrue(
                    Files.isRegularFile(GENERATED.resolve("data/minecraft/recipe/" + disabled + ".json")),
                    disabled);
        }
    }

    @Test
    void advancedCoreToolsAndMetalConversionsKeepR196Rules() throws Exception {
        Map<String, Float> ingotDifficulties = Map.of(
                "silver", 400.0F,
                "gold", 400.0F,
                "ancient_metal", 1600.0F,
                "mithril", 6400.0F,
                "adamantium", 25600.0F);
        Map<String, String> toolBenches = Map.of(
                "silver", "copper",
                "gold", "copper",
                "ancient_metal", "ancient_metal",
                "mithril", "mithril",
                "adamantium", "adamantium");
        Map<String, Integer> ingotCounts = Map.of(
                "pickaxe", 3,
                "shovel", 1,
                "axe", 3,
                "hoe", 2,
                "sword", 2);
        Map<String, Integer> stickCounts = Map.of(
                "pickaxe", 2,
                "shovel", 2,
                "axe", 2,
                "hoe", 2,
                "sword", 1);
        for (var material : ingotDifficulties.entrySet()) {
            for (String tool : ingotCounts.keySet()) {
                String recipeName = material.getKey() + "_" + tool;
                JsonObject recipe = json(GENERATED.resolve("data/infx/recipe/" + recipeName + ".json"));
                float expectedDifficulty = material.getValue() * ingotCounts.get(tool) + 25.0F * stickCounts.get(tool);
                assertAll(
                        recipeName,
                        () -> assertEquals(expectedDifficulty, recipe.get("difficulty").getAsFloat()),
                        () -> assertEquals(toolBenches.get(material.getKey()), recipe.get("required_bench").getAsString()),
                        () -> assertEquals(
                                "infx:" + recipeName,
                                recipe.getAsJsonObject("result").get("id").getAsString()));
            }
        }

        Map<String, Float> allMetalDifficulties = Map.ofEntries(
                Map.entry("copper", 400.0F),
                Map.entry("silver", 400.0F),
                Map.entry("gold", 400.0F),
                Map.entry("iron", 800.0F),
                Map.entry("ancient_metal", 1600.0F),
                Map.entry("mithril", 6400.0F),
                Map.entry("adamantium", 25600.0F));
        Set<String> vanillaMetals = Set.of("copper", "gold", "iron");
        for (var material : allMetalDifficulties.entrySet()) {
            String namespace = vanillaMetals.contains(material.getKey()) ? "minecraft" : "infx";
            JsonObject ingotRecipe = json(GENERATED.resolve(
                    "data/infx/recipe/" + material.getKey() + "_ingot_from_nuggets.json"));
            JsonObject nuggetRecipe = json(GENERATED.resolve(
                    "data/infx/recipe/" + material.getKey() + "_nuggets_from_ingot.json"));
            assertAll(
                    material.getKey() + " conversions",
                    () -> assertEquals(material.getValue(), ingotRecipe.get("difficulty").getAsFloat()),
                    () -> assertEquals("flint", ingotRecipe.get("required_bench").getAsString()),
                    () -> assertEquals(
                            namespace + ":" + material.getKey() + "_ingot",
                            ingotRecipe.getAsJsonObject("result").get("id").getAsString()),
                    () -> assertEquals(material.getValue(), nuggetRecipe.get("difficulty").getAsFloat()),
                    () -> assertEquals("hand", nuggetRecipe.get("required_bench").getAsString()),
                    () -> assertEquals(9, nuggetRecipe.getAsJsonObject("result").get("count").getAsInt()),
                    () -> assertEquals(
                            namespace + ":" + material.getKey() + "_nugget",
                            nuggetRecipe.getAsJsonObject("result").get("id").getAsString()));
        }

        for (String disabled : List.of(
                "gold_ingot_from_nuggets",
                "gold_nugget",
                "iron_ingot_from_nuggets",
                "iron_nugget",
                "golden_axe",
                "golden_hoe",
                "golden_pickaxe",
                "golden_shovel",
                "golden_sword")) {
            assertTrue(
                    Files.isRegularFile(GENERATED.resolve("data/minecraft/recipe/" + disabled + ".json")),
                    disabled);
        }

        Map<String, List<String>> advancementRecipes = Map.of(
                "build_axe",
                List.of("silver_axe", "gold_axe", "ancient_metal_axe", "mithril_axe", "adamantium_axe"),
                "build_hoe",
                List.of("silver_hoe", "gold_hoe", "ancient_metal_hoe", "mithril_hoe", "adamantium_hoe"),
                "build_pickaxe",
                List.of("silver_pickaxe", "gold_pickaxe"),
                "build_better_pickaxe",
                List.of("ancient_metal_pickaxe", "mithril_pickaxe", "adamantium_pickaxe"));
        for (var advancement : advancementRecipes.entrySet()) {
            String contents = Files.readString(
                    GENERATED.resolve("data/infx/advancement/progression/" + advancement.getKey() + ".json"),
                    UTF_8);
            for (String recipe : advancement.getValue()) {
                assertTrue(contents.contains("infx:" + recipe), advancement.getKey() + " must accept " + recipe);
            }
        }
    }

    @Test
    void specialToolRecipesKeepR196DifficultiesAndBenchTiers() throws Exception {
        Map<String, Float> ingotDifficulties = Map.ofEntries(
                Map.entry("copper", 400.0F),
                Map.entry("silver", 400.0F),
                Map.entry("gold", 400.0F),
                Map.entry("iron", 800.0F),
                Map.entry("ancient_metal", 1600.0F),
                Map.entry("mithril", 6400.0F),
                Map.entry("adamantium", 25600.0F));
        Map<String, String> benches = Map.ofEntries(
                Map.entry("copper", "copper"),
                Map.entry("silver", "copper"),
                Map.entry("gold", "copper"),
                Map.entry("iron", "iron"),
                Map.entry("ancient_metal", "ancient_metal"),
                Map.entry("mithril", "mithril"),
                Map.entry("adamantium", "adamantium"));
        Map<String, Integer> ingotCounts = Map.of(
                "mattock", 4,
                "battle_axe", 4,
                "war_hammer", 5,
                "scythe", 2,
                "hatchet", 1,
                "shears", 2);
        Map<String, Integer> stickCounts = Map.of(
                "mattock", 2,
                "battle_axe", 2,
                "war_hammer", 2,
                "scythe", 3,
                "hatchet", 2,
                "shears", 0);
        for (var material : ingotDifficulties.entrySet()) {
            for (String type : ingotCounts.keySet()) {
                String recipeName = material.getKey() + "_" + type;
                JsonObject recipe = json(GENERATED.resolve("data/infx/recipe/" + recipeName + ".json"));
                float expectedDifficulty = material.getValue() * ingotCounts.get(type)
                        + 25.0F * stickCounts.get(type);
                assertAll(
                        recipeName,
                        () -> assertEquals(expectedDifficulty, recipe.get("difficulty").getAsFloat()),
                        () -> assertEquals(benches.get(material.getKey()), recipe.get("required_bench").getAsString()),
                        () -> assertEquals(
                                "infx:" + recipeName,
                                recipe.getAsJsonObject("result").get("id").getAsString()));
            }
        }

        Map<String, Float> obsidianDifficulties = Map.of(
                "obsidian_hatchet", 315.0F,
                "obsidian_shovel", 315.0F,
                "obsidian_axe", 795.0F);
        for (var entry : obsidianDifficulties.entrySet()) {
            JsonObject recipe = json(GENERATED.resolve("data/infx/recipe/" + entry.getKey() + ".json"));
            String requiredBench = entry.getKey().equals("obsidian_hatchet") ? "hand" : "flint";
            assertAll(
                    entry.getKey(),
                    () -> assertEquals(entry.getValue(), recipe.get("difficulty").getAsFloat()),
                    () -> assertEquals(requiredBench, recipe.get("required_bench").getAsString()),
                    () -> assertEquals(
                            "infx:" + entry.getKey(),
                            recipe.getAsJsonObject("result").get("id").getAsString()));
        }
        assertTrue(Files.isRegularFile(GENERATED.resolve("data/minecraft/recipe/shears.json")));

        Map<String, List<String>> advancementRecipes = Map.of(
                "build_axe",
                List.of(
                        "copper_battle_axe",
                        "silver_battle_axe",
                        "gold_battle_axe",
                        "iron_battle_axe",
                        "ancient_metal_battle_axe",
                        "mithril_battle_axe",
                        "adamantium_battle_axe"),
                "build_shovel",
                List.of(
                        "obsidian_shovel",
                        "copper_shovel",
                        "silver_shovel",
                        "gold_shovel",
                        "iron_shovel",
                        "ancient_metal_shovel",
                        "mithril_shovel",
                        "adamantium_shovel"),
                "build_hoe",
                List.of(
                        "copper_mattock",
                        "silver_mattock",
                        "gold_mattock",
                        "iron_mattock",
                        "ancient_metal_mattock",
                        "mithril_mattock",
                        "adamantium_mattock"),
                "build_scythe",
                List.of(
                        "copper_scythe",
                        "silver_scythe",
                        "gold_scythe",
                        "iron_scythe",
                        "ancient_metal_scythe",
                        "mithril_scythe",
                        "adamantium_scythe"),
                "build_better_pickaxe",
                List.of(
                        "iron_war_hammer",
                        "ancient_metal_war_hammer",
                        "mithril_war_hammer",
                        "adamantium_war_hammer"));
        for (var advancement : advancementRecipes.entrySet()) {
            String contents = Files.readString(
                    GENERATED.resolve("data/infx/advancement/progression/" + advancement.getKey() + ".json"),
                    UTF_8);
            for (String recipe : advancement.getValue()) {
                assertTrue(contents.contains("infx:" + recipe), advancement.getKey() + " must accept " + recipe);
            }
        }
        JsonObject english = json(GENERATED.resolve("assets/infx/lang/en_us.json"));
        JsonObject chinese = json(GENERATED.resolve("assets/infx/lang/zh_cn.json"));
        assertAll(
                "build scythe translations",
                () -> assertTrue(english.has("advancements.infx.build_scythe.title")),
                () -> assertTrue(english.has("advancements.infx.build_scythe.description")),
                () -> assertTrue(chinese.has("advancements.infx.build_scythe.title")),
                () -> assertTrue(chinese.has("advancements.infx.build_scythe.description")));
    }

    @Test
    void weaponRecipesKeepR196DifficultiesBenchTiersAndProgression() throws Exception {
        Map<String, Float> fixedDifficulties = Map.of(
                "wood_cudgel", 105.0F,
                "wood_club", 185.0F,
                "flint_knife", 150.0F,
                "obsidian_knife", 290.0F,
                "wood_bow", 150.0F,
                "ancient_metal_bow", 1750.0F,
                "mithril_bow", 6550.0F);
        Map<String, String> fixedBenches = Map.of(
                "wood_cudgel", "hand",
                "wood_club", "flint",
                "flint_knife", "hand",
                "obsidian_knife", "hand",
                "wood_bow", "flint",
                "ancient_metal_bow", "ancient_metal",
                "mithril_bow", "mithril");
        for (var entry : fixedDifficulties.entrySet()) {
            String recipeName = entry.getKey();
            JsonObject recipe = json(GENERATED.resolve("data/infx/recipe/" + recipeName + ".json"));
            assertAll(
                    recipeName,
                    () -> assertEquals(entry.getValue(), recipe.get("difficulty").getAsFloat()),
                    () -> assertEquals(fixedBenches.get(recipeName), recipe.get("required_bench").getAsString()),
                    () -> assertEquals(
                            "infx:" + recipeName,
                            recipe.getAsJsonObject("result").get("id").getAsString()));
        }

        Map<String, Float> ingotDifficulties = Map.ofEntries(
                Map.entry("copper", 400.0F),
                Map.entry("silver", 400.0F),
                Map.entry("gold", 400.0F),
                Map.entry("iron", 800.0F),
                Map.entry("ancient_metal", 1600.0F),
                Map.entry("mithril", 6400.0F),
                Map.entry("adamantium", 25600.0F));
        Map<String, String> benches = Map.ofEntries(
                Map.entry("copper", "copper"),
                Map.entry("silver", "copper"),
                Map.entry("gold", "copper"),
                Map.entry("iron", "iron"),
                Map.entry("ancient_metal", "ancient_metal"),
                Map.entry("mithril", "mithril"),
                Map.entry("adamantium", "adamantium"));
        for (var material : ingotDifficulties.entrySet()) {
            String recipeName = material.getKey() + "_dagger";
            JsonObject recipe = json(GENERATED.resolve("data/infx/recipe/" + recipeName + ".json"));
            assertAll(
                    recipeName,
                    () -> assertEquals(material.getValue() + 25.0F, recipe.get("difficulty").getAsFloat()),
                    () -> assertEquals(benches.get(material.getKey()), recipe.get("required_bench").getAsString()),
                    () -> assertEquals(
                            "infx:" + recipeName,
                            recipe.getAsJsonObject("result").get("id").getAsString()));
        }

        Map<String, Float> arrowheadDifficulties = Map.ofEntries(
                Map.entry("flint", 25.0F),
                Map.entry("obsidian", 200.0F / 9.0F),
                Map.entry("copper", 400.0F / 9.0F),
                Map.entry("silver", 400.0F / 9.0F),
                Map.entry("gold", 400.0F / 9.0F),
                Map.entry("iron", 800.0F / 9.0F),
                Map.entry("ancient_metal", 1600.0F / 9.0F),
                Map.entry("mithril", 6400.0F / 9.0F),
                Map.entry("adamantium", 25600.0F / 9.0F));
        Map<String, String> arrowBenches = Map.ofEntries(
                Map.entry("flint", "flint"),
                Map.entry("obsidian", "flint"),
                Map.entry("copper", "copper"),
                Map.entry("silver", "copper"),
                Map.entry("gold", "copper"),
                Map.entry("iron", "iron"),
                Map.entry("ancient_metal", "ancient_metal"),
                Map.entry("mithril", "mithril"),
                Map.entry("adamantium", "adamantium"));
        for (var material : arrowheadDifficulties.entrySet()) {
            String recipeName = material.getKey() + "_arrow";
            JsonObject recipe = json(GENERATED.resolve("data/infx/recipe/" + recipeName + ".json"));
            JsonObject result = recipe.getAsJsonObject("result");
            int count = result.has("count") ? result.get("count").getAsInt() : 1;
            assertAll(
                    recipeName,
                    () -> assertEquals(material.getValue() + 50.0F, recipe.get("difficulty").getAsFloat()),
                    () -> assertEquals(arrowBenches.get(material.getKey()), recipe.get("required_bench").getAsString()),
                    () -> assertEquals("infx:" + recipeName, result.get("id").getAsString()),
                    () -> assertEquals(1, count));
        }

        for (String disabled : List.of("arrow", "bow")) {
            assertTrue(Files.isRegularFile(GENERATED.resolve("data/minecraft/recipe/" + disabled + ".json")));
        }
        String cuttingEdge = Files.readString(
                GENERATED.resolve("data/infx/advancement/progression/cutting_edge.json"), UTF_8);
        assertTrue(cuttingEdge.contains("infx:flint_knife"));
        assertTrue(Files.isRegularFile(
                GENERATED.resolve("data/infx/advancement/progression/build_club.json")));

        JsonObject english = json(GENERATED.resolve("assets/infx/lang/en_us.json"));
        JsonObject chinese = json(GENERATED.resolve("assets/infx/lang/zh_cn.json"));
        assertAll(
                "weapon progression translations",
                () -> assertEquals(
                        "Time to Strike!",
                        english.get("advancements.infx.build_club.title").getAsString()),
                () -> assertTrue(english.has("advancements.infx.build_club.description")),
                () -> assertEquals(
                        "出击时间到",
                        chinese.get("advancements.infx.build_club.title").getAsString()),
                () -> assertTrue(chinese.has("advancements.infx.build_club.description")));
    }

    @Test
    void armorAndChainRecipesKeepR196RulesAndProgression() throws Exception {
        Map<String, Float> ingotDifficulties = Map.ofEntries(
                Map.entry("copper", 400.0F),
                Map.entry("silver", 400.0F),
                Map.entry("gold", 400.0F),
                Map.entry("iron", 800.0F),
                Map.entry("ancient_metal", 1600.0F),
                Map.entry("mithril", 6400.0F),
                Map.entry("adamantium", 25600.0F));
        Map<String, String> benches = Map.ofEntries(
                Map.entry("copper", "copper"),
                Map.entry("silver", "copper"),
                Map.entry("gold", "copper"),
                Map.entry("rusted_iron", "copper"),
                Map.entry("iron", "iron"),
                Map.entry("ancient_metal", "ancient_metal"),
                Map.entry("mithril", "mithril"),
                Map.entry("adamantium", "adamantium"));

        for (var material : ingotDifficulties.entrySet()) {
            float expectedDifficulty = material.getValue() * 4.0F / 9.0F;
            JsonObject chain = json(GENERATED.resolve(
                    "data/infx/recipe/" + material.getKey() + "_chain_from_nuggets.json"));
            JsonObject nuggets = json(GENERATED.resolve(
                    "data/infx/recipe/" + material.getKey() + "_nuggets_from_chain.json"));
            assertAll(
                    material.getKey() + " chain conversions",
                    () -> assertEquals(expectedDifficulty, chain.get("difficulty").getAsFloat()),
                    () -> assertEquals(benches.get(material.getKey()), chain.get("required_bench").getAsString()),
                    () -> assertEquals(
                            "infx:" + material.getKey() + "_chain",
                            chain.getAsJsonObject("result").get("id").getAsString()),
                    () -> assertEquals(expectedDifficulty, nuggets.get("difficulty").getAsFloat()),
                    () -> assertEquals("hand", nuggets.get("required_bench").getAsString()),
                    () -> assertEquals(4, nuggets.getAsJsonObject("result").get("count").getAsInt()));
        }

        Map<String, Float> plateComponents = Map.ofEntries(
                Map.entry("leather", 100.0F),
                Map.entry("copper", 400.0F),
                Map.entry("silver", 400.0F),
                Map.entry("gold", 400.0F),
                Map.entry("iron", 800.0F),
                Map.entry("ancient_metal", 1600.0F),
                Map.entry("mithril", 6400.0F),
                Map.entry("adamantium", 25600.0F));
        Map<String, Integer> platePieces = Map.of(
                "helmet", 5,
                "chestplate", 8,
                "leggings", 7,
                "boots", 4);
        for (var material : plateComponents.entrySet()) {
            for (var piece : platePieces.entrySet()) {
                String recipeName = material.getKey() + "_" + piece.getKey();
                JsonObject recipe = json(GENERATED.resolve("data/infx/recipe/" + recipeName + ".json"));
                String expectedBench = material.getKey().equals("leather")
                        ? "flint"
                        : benches.get(material.getKey());
                assertAll(
                        recipeName,
                        () -> assertEquals(
                                material.getValue() * piece.getValue(),
                                recipe.get("difficulty").getAsFloat()),
                        () -> assertEquals(expectedBench, recipe.get("required_bench").getAsString()),
                        () -> assertEquals(
                                "infx:" + recipeName,
                                recipe.getAsJsonObject("result").get("id").getAsString()));
            }
        }

        Map<String, Float> chainComponents = Map.ofEntries(
                Map.entry("copper", 400.0F * 4.0F / 9.0F),
                Map.entry("silver", 400.0F * 4.0F / 9.0F),
                Map.entry("gold", 400.0F * 4.0F / 9.0F),
                Map.entry("rusted_iron", 400.0F * 4.0F / 9.0F),
                Map.entry("iron", 800.0F * 4.0F / 9.0F),
                Map.entry("ancient_metal", 1600.0F * 4.0F / 9.0F),
                Map.entry("mithril", 6400.0F * 4.0F / 9.0F),
                Map.entry("adamantium", 25600.0F * 4.0F / 9.0F));
        Map<String, Integer> chainPieces = Map.of(
                "chainmail_helmet", 5,
                "chainmail_chestplate", 8,
                "chainmail_leggings", 7,
                "chainmail_boots", 4);
        for (var material : chainComponents.entrySet()) {
            for (var piece : chainPieces.entrySet()) {
                String recipeName = material.getKey() + "_" + piece.getKey();
                JsonObject recipe = json(GENERATED.resolve("data/infx/recipe/" + recipeName + ".json"));
                assertAll(
                        recipeName,
                        () -> assertEquals(
                                material.getValue() * piece.getValue(),
                                recipe.get("difficulty").getAsFloat()),
                        () -> assertEquals(benches.get(material.getKey()), recipe.get("required_bench").getAsString()),
                        () -> assertEquals(
                                "infx:" + recipeName,
                                recipe.getAsJsonObject("result").get("id").getAsString()));
            }
        }

        for (String piece : platePieces.keySet()) {
            assertFalse(Files.exists(GENERATED.resolve("data/infx/recipe/rusted_iron_" + piece + ".json")));
        }
        for (String material : ingotDifficulties.keySet()) {
            assertFalse(Files.exists(GENERATED.resolve("data/infx/recipe/" + material + "_horse_armor.json")));
        }
        for (String disabled : List.of(
                "leather_helmet",
                "leather_chestplate",
                "leather_leggings",
                "leather_boots",
                "golden_helmet",
                "golden_chestplate",
                "golden_leggings",
                "golden_boots",
                "iron_helmet",
                "iron_chestplate",
                "iron_leggings",
                "iron_boots")) {
            assertTrue(Files.isRegularFile(GENERATED.resolve("data/minecraft/recipe/" + disabled + ".json")));
        }

        String buildChainMail = Files.readString(
                GENERATED.resolve("data/infx/advancement/progression/build_chain_mail.json"), UTF_8);
        for (String material : chainComponents.keySet()) {
            for (String piece : chainPieces.keySet()) {
                assertTrue(buildChainMail.contains("infx:" + material + "_" + piece));
            }
        }
        for (String advancement : List.of(
                "wear_leather", "wear_all_plate_armor", "wear_all_adamantium_plate_armor")) {
            assertTrue(Files.isRegularFile(
                    GENERATED.resolve("data/infx/advancement/progression/" + advancement + ".json")));
        }

        JsonObject english = json(GENERATED.resolve("assets/infx/lang/en_us.json"));
        JsonObject chinese = json(GENERATED.resolve("assets/infx/lang/zh_cn.json"));
        assertAll(
                "armor progression translations",
                () -> assertEquals(
                        "Better Armor",
                        english.get("advancements.infx.build_chain_mail.title").getAsString()),
                () -> assertEquals(
                        "Juggernaut",
                        english.get("advancements.infx.wear_all_adamantium_plate_armor.title").getAsString()),
                () -> assertEquals(
                        "更好的护甲",
                        chinese.get("advancements.infx.build_chain_mail.title").getAsString()),
                () -> assertEquals(
                        "世界主宰",
                        chinese.get("advancements.infx.wear_all_adamantium_plate_armor.title").getAsString()));
    }

    @Test
    void horseArmorLootSupplementsKeepR196StructuresWeightsAndRolls() throws Exception {
        Map<String, Map<String, Integer>> expectedEntries = Map.of(
                "simple_dungeon",
                Map.of(
                        "minecraft:empty", 147,
                        "infx:gold_horse_armor", 2,
                        "infx:copper_horse_armor", 5,
                        "infx:iron_horse_armor", 1),
                "nether_bridge",
                Map.of(
                        "minecraft:empty", 50,
                        "infx:gold_horse_armor", 8,
                        "infx:copper_horse_armor", 5,
                        "infx:iron_horse_armor", 3),
                "desert_pyramid",
                Map.of(
                        "minecraft:empty", 65,
                        "infx:iron_horse_armor", 1,
                        "infx:silver_horse_armor", 1,
                        "infx:gold_horse_armor", 1),
                "jungle_temple",
                Map.of(
                        "minecraft:empty", 60,
                        "infx:iron_horse_armor", 1,
                        "infx:silver_horse_armor", 1,
                        "infx:gold_horse_armor", 1),
                "stronghold_corridor",
                Map.of(
                        "minecraft:empty", 188,
                        "infx:copper_horse_armor", 1,
                        "infx:iron_horse_armor", 1));
        Map<String, List<Float>> expectedRolls = Map.of(
                "simple_dungeon", List.of(8.0F, 8.0F),
                "nether_bridge", List.of(2.0F, 5.0F),
                "desert_pyramid", List.of(2.0F, 6.0F),
                "jungle_temple", List.of(2.0F, 6.0F),
                "stronghold_corridor", List.of(2.0F, 3.0F));

        for (var structure : expectedEntries.entrySet()) {
            String path = structure.getKey();
            JsonObject table = json(GENERATED.resolve(
                    "data/infx/loot_table/chests/horse_armor/" + path + ".json"));
            JsonObject pool = table.getAsJsonArray("pools").get(0).getAsJsonObject();
            Map<String, Integer> entries = pool.getAsJsonArray("entries").asList().stream()
                    .map(JsonElement::getAsJsonObject)
                    .collect(Collectors.toMap(
                            entry -> entry.has("name")
                                    ? entry.get("name").getAsString()
                                    : "minecraft:empty",
                            entry -> entry.has("weight")
                                    ? entry.get("weight").getAsInt()
                                    : 1));
            JsonElement rolls = pool.get("rolls");
            float minRolls = rolls.isJsonObject()
                    ? rolls.getAsJsonObject().get("min").getAsFloat()
                    : rolls.getAsFloat();
            float maxRolls = rolls.isJsonObject()
                    ? rolls.getAsJsonObject().get("max").getAsFloat()
                    : rolls.getAsFloat();
            assertAll(
                    path,
                    () -> assertEquals(structure.getValue(), entries),
                    () -> assertEquals(expectedRolls.get(path).get(0), minRolls),
                    () -> assertEquals(expectedRolls.get(path).get(1), maxRolls),
                    () -> assertFalse(table.toString().contains("ancient_metal_horse_armor")),
                    () -> assertFalse(table.toString().contains("mithril_horse_armor")),
                    () -> assertFalse(table.toString().contains("adamantium_horse_armor")));

            JsonObject modifier = json(GENERATED.resolve(
                    "data/infx/loot_modifiers/horse_armor_" + path + ".json"));
            JsonObject condition = modifier.getAsJsonArray("conditions").get(0).getAsJsonObject();
            assertAll(
                    path + " modifier",
                    () -> assertEquals("neoforge:add_table", modifier.get("type").getAsString()),
                    () -> assertEquals(
                            "infx:chests/horse_armor/" + path,
                            modifier.get("table").getAsString()),
                    () -> assertEquals(
                            "minecraft:chests/" + path,
                            condition.get("loot_table_id").getAsString()));
        }
        assertEquals(
                5,
                jsonCount(GENERATED.resolve("data/infx/loot_table/chests/horse_armor")));
    }

    @Test
    void furnaceHeatTagsSeparateCoalFromLowHeatFuel() throws Exception {
        String heatTwoFuels = Files.readString(
                GENERATED.resolve("data/infx/tags/item/furnace_fuels/heat_2.json"), UTF_8);
        assertAll(
                "heat-2 fuels",
                () -> assertTrue(heatTwoFuels.contains("minecraft:coal")),
                () -> assertTrue(heatTwoFuels.contains("minecraft:coal_block")),
                () -> assertFalse(heatTwoFuels.contains("minecraft:charcoal")));

        String heatTwoInputs = Files.readString(
                GENERATED.resolve("data/infx/tags/item/smelting_inputs/heat_2.json"), UTF_8);
        assertAll(
                "heat-2 inputs",
                () -> assertTrue(heatTwoInputs.contains("minecraft:raw_iron")),
                () -> assertTrue(heatTwoInputs.contains("minecraft:iron_ore")),
                () -> assertTrue(heatTwoInputs.contains("minecraft:nether_quartz_ore")),
                () -> assertTrue(heatTwoInputs.contains("minecraft:sandstone")),
                () -> assertTrue(heatTwoInputs.contains("infx:silver_ore")),
                () -> assertTrue(Files.isRegularFile(GENERATED.resolve(
                        "data/infx/recipe/silver_ingot_from_smelting_silver_ore.json"))));
    }

    @Test
    void miteOresHaveCompleteResourcesAndProgressionData() throws Exception {
        JsonObject english = json(GENERATED.resolve("assets/infx/lang/en_us.json"));
        JsonObject chinese = json(GENERATED.resolve("assets/infx/lang/zh_cn.json"));
        for (String ore : List.of("silver_ore", "mithril_ore", "adamantium_ore")) {
            assertAll(
                    ore,
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("assets/infx/blockstates/" + ore + ".json"))),
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("assets/infx/items/" + ore + ".json"))),
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("assets/infx/models/block/" + ore + ".json"))),
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("data/infx/loot_table/blocks/" + ore + ".json"))),
                    () -> assertTrue(english.has("block.infx." + ore)),
                    () -> assertTrue(chinese.has("block.infx." + ore)));
        }

        String heatThreeInputs = Files.readString(
                GENERATED.resolve("data/infx/tags/item/smelting_inputs/heat_3.json"), UTF_8);
        String heatFourInputs = Files.readString(
                GENERATED.resolve("data/infx/tags/item/smelting_inputs/heat_4.json"), UTF_8);
        assertAll(
                "high-heat smelting",
                () -> assertTrue(heatThreeInputs.contains("infx:mithril_ore")),
                () -> assertTrue(heatFourInputs.contains("infx:adamantium_ore")),
                () -> assertTrue(Files.isRegularFile(GENERATED.resolve(
                        "data/infx/recipe/mithril_ingot_from_smelting_mithril_ore.json"))),
                () -> assertTrue(Files.isRegularFile(GENERATED.resolve(
                        "data/infx/recipe/adamantium_ingot_from_smelting_adamantium_ore.json"))));

        String placedFeature = Files.readString(
                GENERATED.resolve("data/infx/worldgen/placed_feature/mithril_ore.json"), UTF_8);
        String silverConfigured = Files.readString(
                GENERATED.resolve("data/infx/worldgen/configured_feature/silver_ore.json"), UTF_8);
        String silverPlaced = Files.readString(
                GENERATED.resolve("data/infx/worldgen/placed_feature/silver_ore.json"), UTF_8);
        assertAll(
                "overworld ore generation",
                () -> assertTrue(silverConfigured.contains("\"size\": 6")),
                () -> assertTrue(silverConfigured.contains("infx:silver_ore")),
                () -> assertTrue(silverPlaced.contains("minecraft:biased_to_bottom")),
                () -> assertTrue(silverPlaced.contains("\"absolute\": 96")),
                () -> assertTrue(Files.isRegularFile(GENERATED.resolve(
                        "data/infx/neoforge/biome_modifier/add_silver_ore.json"))),
                () -> assertTrue(Files.isRegularFile(GENERATED.resolve(
                        "data/infx/worldgen/configured_feature/mithril_ore.json"))),
                () -> assertTrue(placedFeature.contains("minecraft:biased_to_bottom")),
                () -> assertTrue(placedFeature.contains("\"absolute\": 0")),
                () -> assertTrue(placedFeature.contains("\"absolute\": 32")),
                () -> assertTrue(Files.isRegularFile(GENERATED.resolve(
                        "data/infx/neoforge/biome_modifier/add_mithril_ore.json"))));
    }

    @Test
    void underworldDataKeepsDimensionTerrainOreAndDungeonProgression() throws Exception {
        JsonObject dimension = json(GENERATED.resolve("data/infx/dimension/underworld.json"));
        JsonObject generator = dimension.getAsJsonObject("generator");
        JsonObject dimensionType = json(GENERATED.resolve("data/infx/dimension_type/underworld.json"));
        JsonObject bedRule = dimensionType
                .getAsJsonObject("attributes")
                .getAsJsonObject("minecraft:gameplay/bed_rule");
        JsonObject biome = json(GENERATED.resolve("data/infx/worldgen/biome/underworld.json"));
        JsonObject noise = json(GENERATED.resolve("data/infx/worldgen/noise_settings/underworld.json"));
        Set<String> underworldFeatures = biome.getAsJsonArray("features").asList().stream()
                .flatMap(step -> step.getAsJsonArray().asList().stream())
                .map(JsonElement::getAsString)
                .collect(Collectors.toSet());
        int underworldMinY = dimensionType.get("min_y").getAsInt();
        JsonObject miteDensity = json(GENERATED.resolve(
                "data/infx/worldgen/density_function/mite_r196_first_cave.json"));
        JsonObject noiseShape = noise.getAsJsonObject("noise");
        JsonObject finalDensity = noise.getAsJsonObject("noise_router").getAsJsonObject("final_density");
        JsonObject miteShape = miteDensity.getAsJsonObject("input").getAsJsonObject("argument");
        JsonObject scaledMiteTerrain = miteShape.getAsJsonObject("argument1");
        JsonObject legacyBlendedNoise = scaledMiteTerrain.getAsJsonObject("argument2");
        JsonObject surfaceRule = noise.getAsJsonObject("surface_rule");
        var surfaceSequence = surfaceRule.getAsJsonArray("sequence");
        JsonObject roofBedrockRule = surfaceSequence.get(0).getAsJsonObject();
        JsonObject roofGradient = roofBedrockRule
                .getAsJsonObject("if_true")
                .getAsJsonObject("invert");
        JsonObject floorCoreRule = surfaceSequence.get(1).getAsJsonObject();
        JsonObject mantleRule = surfaceSequence.get(2).getAsJsonObject();
        JsonObject firstInternalBedrockRule = surfaceSequence.get(3).getAsJsonObject();
        JsonObject secondInternalBedrockRule = surfaceSequence.get(4).getAsJsonObject();
        JsonObject thirdInternalBedrockRule = surfaceSequence.get(5).getAsJsonObject();
        JsonObject deepslateRule = surfaceSequence.get(6).getAsJsonObject();
        JsonObject deepslateCutoff = deepslateRule
                .getAsJsonObject("if_true")
                .getAsJsonObject("invert");
        Set<String> terrainNoises = new HashSet<>();
        visit(finalDensity, (key, value) -> {
            if (key.equals("noise")) {
                terrainNoises.add(value);
            }
        });
        assertAll(
                "Underworld dimension",
                () -> assertEquals("infx:underworld", dimension.get("type").getAsString()),
                () -> assertEquals("minecraft:noise", generator.get("type").getAsString()),
                () -> assertEquals(
                        "infx:underworld",
                        generator.getAsJsonObject("biome_source").get("biome").getAsString()),
                () -> assertEquals("infx:underworld", generator.get("settings").getAsString()),
                () -> assertEquals(-192, dimensionType.get("min_y").getAsInt()),
                () -> assertEquals(512, dimensionType.get("height").getAsInt()),
                () -> assertEquals(512, dimensionType.get("logical_height").getAsInt()),
                () -> assertEquals(
                        320,
                        dimensionType.get("min_y").getAsInt()
                                + dimensionType.get("height").getAsInt()),
                () -> assertTrue(dimensionType.get("has_ceiling").getAsBoolean()),
                () -> assertFalse(dimensionType.get("has_skylight").getAsBoolean()),
                () -> assertEquals("minecraft:stone", noise.getAsJsonObject("default_block").get("Name").getAsString()),
                () -> assertEquals("minecraft:water", noise.getAsJsonObject("default_fluid").get("Name").getAsString()),
                () -> assertEquals(140, noise.get("sea_level").getAsInt()),
                () -> assertFalse(noise.get("aquifers_enabled").getAsBoolean()),
                () -> assertEquals(-192, noiseShape.get("min_y").getAsInt()),
                () -> assertEquals(512, noiseShape.get("height").getAsInt()),
                () -> assertEquals("minecraft:clamp", finalDensity.get("type").getAsString()),
                () -> assertEquals(-1.0, finalDensity.get("min").getAsDouble()),
                () -> assertEquals(1.0, finalDensity.get("max").getAsDouble()),
                () -> assertTrue(hasGradient(finalDensity, 127, 136, 0.0, 1.0)),
                () -> assertTrue(hasGradient(finalDensity, 196, 208, 0.0, 1.0)),
                () -> assertTrue(hasGradient(finalDensity, 204, 222, 0.0, 1.0)),
                () -> assertTrue(hasGradient(finalDensity, 204, 242, 0.0, 1.0)),
                () -> assertTrue(hasGradient(finalDensity, 222, 242, 0.0, 1.0)),
                () -> assertTrue(hasGradient(finalDensity, 238, 250, 0.0, 1.0)),
                () -> assertTrue(hasGradient(finalDensity, 296, 319, -1.0, 1.0)),
                () -> assertTrue(finalDensity.toString().contains("infx:mite_r196_first_cave")),
                () -> assertEquals("minecraft:clamp", miteDensity.get("type").getAsString()),
                () -> assertEquals("minecraft:interpolated", miteDensity
                        .getAsJsonObject("input")
                        .get("type")
                        .getAsString()),
                () -> assertEquals("minecraft:mul", scaledMiteTerrain.get("type").getAsString()),
                () -> assertEquals(128.0, scaledMiteTerrain.get("argument1").getAsDouble()),
                () -> assertEquals("minecraft:old_blended_noise", legacyBlendedNoise.get("type").getAsString()),
                () -> assertEquals(0.25, legacyBlendedNoise.get("xz_scale").getAsDouble()),
                () -> assertEquals(0.375, legacyBlendedNoise.get("y_scale").getAsDouble()),
                () -> assertEquals(80.0, legacyBlendedNoise.get("xz_factor").getAsDouble()),
                () -> assertEquals(60.0, legacyBlendedNoise.get("y_factor").getAsDouble()),
                () -> assertEquals(8.0, legacyBlendedNoise.get("smear_scale_multiplier").getAsDouble()),
                () -> assertTrue(hasGradient(
                        miteDensity,
                        128,
                        139,
                        Math.cos(4.0 * Math.PI * 6.0 / 17.0) * 2.0,
                        Math.cos(5.0 * Math.PI * 6.0 / 17.0) * 2.0)),
                () -> assertTrue(hasGradient(
                        miteDensity,
                        172,
                        183,
                        Math.cos(8.0 * Math.PI * 6.0 / 17.0) * 2.0,
                        Math.cos(9.0 * Math.PI * 6.0 / 17.0) * 2.0)),
                () -> assertTrue(hasGradient(
                        miteDensity,
                        205,
                        216,
                        Math.cos(11.0 * Math.PI * 6.0 / 17.0) * 2.0,
                        Math.cos(12.0 * Math.PI * 6.0 / 17.0) * 2.0)),
                () -> assertEquals(Set.of(
                        "minecraft:cave_cheese",
                        "minecraft:cave_entrance",
                        "minecraft:cave_layer",
                        "minecraft:pillar",
                        "minecraft:spaghetti_2d",
                        "minecraft:spaghetti_3d_1",
                        "minecraft:spaghetti_3d_2"), terrainNoises),
                () -> assertTrue(finalDensity.toString().contains("minecraft:interpolated")),
                () -> assertEquals(7, surfaceSequence.size()),
                () -> assertEquals("minecraft:vertical_gradient", roofGradient.get("type").getAsString()),
                () -> assertEquals(5, roofGradient
                        .getAsJsonObject("true_at_and_below")
                        .get("below_top")
                        .getAsInt()),
                () -> assertEquals(0, roofGradient
                        .getAsJsonObject("false_at_and_above")
                        .get("below_top")
                        .getAsInt()),
                () -> assertEquals("minecraft:bedrock", roofBedrockRule
                        .getAsJsonObject("then_run")
                        .getAsJsonObject("result_state")
                        .get("Name")
                        .getAsString()),
                () -> assertEquals("infx:core", floorCoreRule
                        .getAsJsonObject("then_run")
                        .getAsJsonObject("result_state")
                        .get("Name")
                        .getAsString()),
                () -> assertEquals(1, floorCoreRule
                        .getAsJsonObject("if_true")
                        .getAsJsonObject("invert")
                        .getAsJsonObject("anchor")
                        .get("above_bottom")
                        .getAsInt()),
                () -> assertEquals("infx:mantle", mantleRule
                        .getAsJsonObject("then_run")
                        .getAsJsonObject("result_state")
                        .get("Name")
                        .getAsString()),
                () -> assertEquals(5, mantleRule
                        .getAsJsonObject("if_true")
                        .getAsJsonObject("invert")
                        .getAsJsonObject("anchor")
                        .get("above_bottom")
                        .getAsInt()),
                () -> assertInternalBedrockBand(
                        firstInternalBedrockRule, underworldMinY, 152, 161, -40, -31, "minecraft:pillar"),
                () -> assertInternalBedrockBand(
                        secondInternalBedrockRule, underworldMinY, 216, 225, 24, 33, "minecraft:spaghetti_2d"),
                () -> assertInternalBedrockBand(
                        thirdInternalBedrockRule, underworldMinY, 272, 281, 80, 89, "minecraft:cave_layer"),
                () -> assertEquals("minecraft:y_above", deepslateCutoff.get("type").getAsString()),
                () -> assertEquals(0, deepslateCutoff
                        .getAsJsonObject("anchor")
                        .get("absolute")
                        .getAsInt()),
                () -> assertEquals("minecraft:deepslate", deepslateRule
                        .getAsJsonObject("then_run")
                        .getAsJsonObject("result_state")
                        .get("Name")
                        .getAsString()),
                () -> assertEquals("never", bedRule.get("can_sleep").getAsString()),
                () -> assertEquals("never", bedRule.get("can_set_spawn").getAsString()),
                () -> assertFalse(bedRule.has("explodes")),
                () -> assertTrue(underworldFeatures.contains("minecraft:monster_room")),
                () -> assertTrue(underworldFeatures.contains("minecraft:monster_room_deep")),
                () -> assertFalse(underworldFeatures.contains("minecraft:lake_lava_underground")),
                () -> assertFalse(underworldFeatures.contains("minecraft:lake_lava_surface")),
                () -> assertFalse(underworldFeatures.contains("minecraft:ore_dirt")),
                () -> assertFalse(underworldFeatures.stream().anyMatch(feature -> feature.contains("ore_coal"))),
                () -> assertFalse(underworldFeatures.stream().anyMatch(feature -> feature.contains("mushroom"))),
                () -> assertTrue(underworldFeatures.contains("minecraft:ore_gravel")),
                () -> assertTrue(underworldFeatures.contains("minecraft:ore_iron_upper")),
                () -> assertTrue(underworldFeatures.contains("minecraft:glow_lichen")),
                () -> assertTrue(biome.toString().contains("minecraft:cave_spider")),
                () -> assertTrue(underworldFeatures.contains("infx:silver_ore")),
                () -> assertTrue(underworldFeatures.contains("infx:mithril_ore")),
                () -> assertTrue(underworldFeatures.contains("infx:underworld_adamantium_ore")),
                () -> assertTrue(underworldFeatures.contains("infx:underworld_mantle_basin")));

        JsonObject configured = json(GENERATED.resolve(
                "data/infx/worldgen/configured_feature/underworld_adamantium_ore.json"));
        JsonObject configuredConfig = configured.getAsJsonObject("config");
        JsonObject placed = json(GENERATED.resolve(
                "data/infx/worldgen/placed_feature/underworld_adamantium_ore.json"));
        String placedContents = placed.toString();
        assertAll(
                "Underworld adamantium",
                () -> assertEquals(3, configuredConfig.get("size").getAsInt()),
                () -> assertTrue(configured.toString().contains("infx:adamantium_ore")),
                () -> assertEquals("infx:underworld_adamantium_ore", placed.get("feature").getAsString()),
                () -> assertTrue(placedContents.contains("\"count\":8")),
                () -> assertTrue(placedContents.contains("minecraft:biased_to_bottom")),
                () -> assertTrue(placedContents.contains("\"absolute\":0")),
                () -> assertTrue(placedContents.contains("\"absolute\":136")),
                () -> assertFalse(Files.exists(GENERATED.resolve(
                        "data/infx/neoforge/biome_modifier/add_adamantium_ore.json"))));

        JsonObject mantle = json(GENERATED.resolve(
                "data/infx/worldgen/configured_feature/underworld_mantle_basin.json"));
        JsonObject mantleConfig = mantle.getAsJsonObject("config");
        JsonObject mantleRadius = mantleConfig.getAsJsonObject("radius");
        JsonObject placedMantle = json(GENERATED.resolve(
                "data/infx/worldgen/placed_feature/underworld_mantle_basin.json"));
        JsonObject mantleHeight = placedMantle
                .getAsJsonArray("placement")
                .get(2)
                .getAsJsonObject()
                .getAsJsonObject("height");
        int mantleHalfHeight = mantleConfig.get("half_height").getAsInt();
        int mantleMinimumOffset = mantleHeight
                .getAsJsonObject("min_inclusive")
                .get("above_bottom")
                .getAsInt();
        int mantleMaximumOffset = mantleHeight
                .getAsJsonObject("max_inclusive")
                .get("above_bottom")
                .getAsInt();
        assertAll(
                "Underworld mantle basin",
                () -> assertEquals("minecraft:disk", mantle.get("type").getAsString()),
                () -> assertEquals(3, mantleRadius.get("min_inclusive").getAsInt()),
                () -> assertEquals(8, mantleRadius.get("max_inclusive").getAsInt()),
                () -> assertTrue(mantleConfig.toString().contains("infx:mantle")),
                () -> assertEquals("infx:underworld_mantle_basin", placedMantle.get("feature").getAsString()),
                () -> assertTrue(placedMantle.toString().contains("\"count\":2")),
                () -> assertEquals(120, mantleMinimumOffset),
                () -> assertEquals(136, mantleMaximumOffset),
                () -> assertEquals(-72, underworldMinY + mantleMinimumOffset),
                () -> assertEquals(-56, underworldMinY + mantleMaximumOffset),
                () -> assertEquals(-73, underworldMinY + mantleMinimumOffset - mantleHalfHeight),
                () -> assertEquals(-55, underworldMinY + mantleMaximumOffset + mantleHalfHeight));

        JsonObject dungeon = json(GENERATED.resolve("data/infx/loot_table/chests/underworld_dungeon.json"));
        JsonObject dungeonPool = dungeon.getAsJsonArray("pools").get(0).getAsJsonObject();
        String dungeonContents = dungeon.toString();
        JsonObject modifier = json(GENERATED.resolve("data/infx/loot_modifiers/underworld_dungeon.json"));
        assertAll(
                "Underworld dungeon progression",
                () -> assertEquals(8.0F, dungeonPool.get("rolls").getAsFloat()),
                () -> assertTrue(dungeonContents.contains("infx:ancient_metal_ingot")),
                () -> assertTrue(dungeonContents.contains("infx:ancient_metal_horse_armor")),
                () -> assertTrue(dungeonContents.contains("infx:ancient_metal_pickaxe")),
                () -> assertEquals("infx:underworld_dungeon", modifier.get("type").getAsString()),
                () -> assertEquals(1500, modifier.get("priority").getAsInt()));
    }

    @Test
    void overworldStopsAtMinusSixteenAndMovesUndergroundStructuresToUnderworld() throws Exception {
        JsonObject dimensionType = json(GENERATED.resolve("data/minecraft/dimension_type/overworld.json"));
        assertAll(
                "Overworld build height",
                () -> assertEquals(-16, dimensionType.get("min_y").getAsInt()),
                () -> assertEquals(336, dimensionType.get("height").getAsInt()),
                () -> assertEquals(336, dimensionType.get("logical_height").getAsInt()),
                () -> assertEquals(
                        320,
                        dimensionType.get("min_y").getAsInt()
                                + dimensionType.get("height").getAsInt()));

        for (String settings : List.of("overworld", "large_biomes", "amplified")) {
            JsonObject noise = json(GENERATED.resolve(
                    "data/minecraft/worldgen/noise_settings/" + settings + ".json"));
            JsonObject shape = noise.getAsJsonObject("noise");
            assertAll(
                    settings,
                    () -> assertEquals(-16, shape.get("min_y").getAsInt()),
                    () -> assertEquals(336, shape.get("height").getAsInt()),
                    () -> assertTrue(noise.getAsJsonObject("noise_router")
                            .get("final_density")
                            .toString()
                            .contains("\"from_y\":-16")),
                    () -> assertTrue(noise.getAsJsonObject("noise_router")
                            .get("final_density")
                            .toString()
                            .contains("\"to_y\":8")));
        }

        for (String structure : List.of(
                "ancient_city",
                "buried_treasure",
                "mineshaft",
                "mineshaft_mesa",
                "trail_ruins",
                "trial_chambers")) {
            JsonObject tag = json(GENERATED.resolve(
                    "data/minecraft/tags/worldgen/biome/has_structure/" + structure + ".json"));
            assertAll(
                    structure,
                    () -> assertTrue(tag.get("replace").getAsBoolean()),
                    () -> assertEquals(1, tag.getAsJsonArray("values").size()),
                    () -> assertEquals(
                            "infx:underworld",
                            tag.getAsJsonArray("values").get(0).getAsString()));
        }

        JsonObject stronghold = json(GENERATED.resolve(
                "data/minecraft/tags/worldgen/biome/has_structure/stronghold.json"));
        assertAll(
                "restored stronghold progression",
                () -> assertFalse(stronghold.has("replace")),
                () -> assertEquals(
                        "#minecraft:is_overworld",
                        stronghold.getAsJsonArray("values").get(0).getAsString()));
    }

    @Test
    void metalAnvilsAndTheirComponentsKeepR196ResourcesAndDifficulties() throws Exception {
        JsonObject english = json(GENERATED.resolve("assets/infx/lang/en_us.json"));
        JsonObject chinese = json(GENERATED.resolve("assets/infx/lang/zh_cn.json"));
        Map<String, Float> difficulties = Map.ofEntries(
                Map.entry("copper", 12_400.0F),
                Map.entry("silver", 12_400.0F),
                Map.entry("gold", 12_400.0F),
                Map.entry("iron", 24_800.0F),
                Map.entry("ancient_metal", 49_600.0F),
                Map.entry("mithril", 198_400.0F),
                Map.entry("adamantium", 793_600.0F));
        for (var entry : difficulties.entrySet()) {
            String path = entry.getKey() + "_anvil";
            JsonObject recipe = json(GENERATED.resolve("data/infx/recipe/" + path + ".json"));
            assertAll(
                    path,
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("assets/infx/blockstates/" + path + ".json"))),
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("assets/infx/items/" + path + ".json"))),
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("data/infx/loot_table/blocks/" + path + ".json"))),
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("assets/infx/models/block/" + path + "_stage_0.json"))),
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("assets/infx/models/block/" + path + "_stage_1.json"))),
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("assets/infx/models/block/" + path + "_stage_2.json"))),
                    () -> assertTrue(english.has("block.infx." + path)),
                    () -> assertTrue(chinese.has("block.infx." + path)),
                    () -> assertEquals(entry.getValue(), recipe.get("difficulty").getAsFloat()),
                    () -> assertEquals(entry.getKey(), recipe.get("required_bench").getAsString()),
                    () -> assertEquals(
                            "[\"BBB\",\"I I\",\"I I\"]",
                            recipe.getAsJsonArray("pattern").toString()),
                    () -> assertEquals(
                            "infx:" + path,
                            recipe.getAsJsonObject("result").get("id").getAsString()));
        }

        Map<String, Float> storageDifficulties = Map.of(
                "silver", 3_600.0F,
                "ancient_metal", 14_400.0F,
                "mithril", 57_600.0F,
                "adamantium", 230_400.0F);
        for (var entry : storageDifficulties.entrySet()) {
            JsonObject blockRecipe = json(GENERATED.resolve(
                    "data/infx/recipe/" + entry.getKey() + "_block.json"));
            JsonObject ingotRecipe = json(GENERATED.resolve(
                    "data/infx/recipe/" + entry.getKey() + "_block_to_ingots.json"));
            assertAll(
                    entry.getKey() + " storage",
                    () -> assertEquals(entry.getValue(), blockRecipe.get("difficulty").getAsFloat()),
                    () -> assertEquals("flint", blockRecipe.get("required_bench").getAsString()),
                    () -> assertEquals(entry.getValue(), ingotRecipe.get("difficulty").getAsFloat()),
                    () -> assertEquals(9, ingotRecipe.getAsJsonObject("result").get("count").getAsInt()));
        }

        Map<String, Float> shardDifficulties = Map.of(
                "diamond", 1_600.0F,
                "nether_quartz", 900.0F,
                "glass", 200.0F);
        for (var entry : shardDifficulties.entrySet()) {
            JsonObject combine = json(GENERATED.resolve(
                    "data/infx/recipe/" + entry.getKey() + "_from_shards.json"));
            JsonObject split = json(GENERATED.resolve(
                    "data/infx/recipe/" + entry.getKey() + "_to_shards.json"));
            assertAll(
                    entry.getKey() + " shards",
                    () -> assertEquals(entry.getValue(), combine.get("difficulty").getAsFloat()),
                    () -> assertEquals("flint", combine.get("required_bench").getAsString()),
                    () -> assertEquals(entry.getValue(), split.get("difficulty").getAsFloat()),
                    () -> assertEquals(9, split.getAsJsonObject("result").get("count").getAsInt()));
        }
    }

    @Test
    void r196FurnacesHaveGeneratedAssetsRecipesLootAndTranslations() throws Exception {
        JsonObject english = json(GENERATED.resolve("assets/infx/lang/en_us.json"));
        JsonObject chinese = json(GENERATED.resolve("assets/infx/lang/zh_cn.json"));
        for (String path : List.of(
                "clay_furnace",
                "sandstone_furnace",
                "hardened_clay_furnace",
                "obsidian_furnace",
                "netherrack_furnace")) {
            assertAll(
                    path,
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("assets/infx/blockstates/" + path + ".json"))),
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("assets/infx/items/" + path + ".json"))),
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("assets/infx/models/block/" + path + ".json"))),
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("assets/infx/models/block/" + path + "_on.json"))),
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("data/infx/loot_table/blocks/" + path + ".json"))),
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("data/infx/recipe/" + path + ".json"))),
                    () -> assertTrue(english.has("block.infx." + path)),
                    () -> assertTrue(english.has("container.infx." + path)),
                    () -> assertTrue(chinese.has("block.infx." + path)),
                    () -> assertTrue(chinese.has("container.infx." + path)));
        }
        for (String recipe : List.of("sand_batch", "sandstone_to_glass")) {
            assertTrue(Files.isRegularFile(GENERATED.resolve("data/infx/recipe/" + recipe + ".json")));
        }
        for (String disabled : List.of("glass", "sandstone", "smooth_sandstone")) {
            assertTrue(Files.isRegularFile(GENERATED.resolve("data/minecraft/recipe/" + disabled + ".json")));
        }

        JsonObject clay = json(GENERATED.resolve("data/infx/recipe/clay_furnace.json"));
        JsonObject sandstone = json(GENERATED.resolve("data/infx/recipe/sandstone_furnace.json"));
        JsonObject hardenedClay =
                json(GENERATED.resolve("data/infx/recipe/hardened_clay_furnace.json"));
        JsonObject obsidian = json(GENERATED.resolve("data/infx/recipe/obsidian_furnace.json"));
        JsonObject netherrack = json(GENERATED.resolve("data/infx/recipe/netherrack_furnace.json"));
        assertAll(
                "R196 furnace crafting",
                () -> assertEquals("hand", clay.get("required_bench").getAsString()),
                () -> assertEquals(320.0F, clay.get("difficulty").getAsFloat()),
                () -> assertEquals("flint", sandstone.get("required_bench").getAsString()),
                () -> assertEquals(640.0F, sandstone.get("difficulty").getAsFloat()),
                () -> assertEquals("flint", hardenedClay.get("required_bench").getAsString()),
                () -> assertEquals(1440.0F, hardenedClay.get("difficulty").getAsFloat()),
                () -> assertEquals("flint", obsidian.get("required_bench").getAsString()),
                () -> assertEquals(1920.0F, obsidian.get("difficulty").getAsFloat()),
                () -> assertEquals("flint", netherrack.get("required_bench").getAsString()),
                () -> assertEquals(1280.0F, netherrack.get("difficulty").getAsFloat()));
    }

    @Test
    void generatedCountsAreExact() throws Exception {
        assertEquals(339, jsonCount(GENERATED.resolve("assets/infx/items")));
        assertEquals(408, jsonCount(GENERATED.resolve("assets/infx/models/item")));
        assertEquals(17, jsonCount(GENERATED.resolve("assets/infx/equipment")));
    }

    @Test
    void runeStonesHaveR196NuggetRecipesAndModernBypassesStayDisabled() throws Exception {
        Map<String, Map<String, Object>> runes = Map.of(
                "mithril",
                Map.of("bench", "mithril", "difficulty", 3_200.0F, "nugget", "infx:mithril_nugget"),
                "adamantium",
                Map.of("bench", "adamantium", "difficulty", 12_800.0F, "nugget", "infx:adamantium_nugget"));
        for (var entry : runes.entrySet()) {
            JsonObject recipe = json(GENERATED.resolve(
                    "data/infx/recipe/" + entry.getKey() + "_rune_stone.json"));
            assertAll(
                    entry.getKey() + " rune stone",
                    () -> assertEquals("infx:crafting_shaped", recipe.get("type").getAsString()),
                    () -> assertEquals(entry.getValue().get("bench"), recipe.get("required_bench").getAsString()),
                    () -> assertEquals(
                            (Float) entry.getValue().get("difficulty"), recipe.get("difficulty").getAsFloat()),
                    () -> assertEquals(
                            entry.getValue().get("nugget"),
                            recipe.getAsJsonObject("key").get("N").getAsString()),
                    () -> assertEquals(
                            "minecraft:obsidian",
                            recipe.getAsJsonObject("key").get("O").getAsString()),
                    () -> assertEquals(
                            "infx:" + entry.getKey() + "_rune_stone",
                            recipe.getAsJsonObject("result").get("id").getAsString()));
        }

        for (String disabled : List.of(
                "bundle",
                "blue_bundle",
                "copper_block",
                "copper_chest",
                "copper_ingot",
                "copper_ingot_from_waxed_copper_block",
                "crafter",
                "netherite_block",
                "netherite_horse_armor_smithing",
                "netherite_ingot",
                "netherite_ingot_from_netherite_block",
                "netherite_nautilus_armor_smithing",
                "netherite_scrap",
                "netherite_scrap_from_blasting",
                "netherite_upgrade_smithing_template",
                "raw_copper",
                "raw_copper_block")) {
            JsonObject recipe = json(GENERATED.resolve("data/minecraft/recipe/" + disabled + ".json"));
            assertEquals(
                    "neoforge:never",
                    recipe.getAsJsonArray("neoforge:conditions")
                            .get(0)
                            .getAsJsonObject()
                            .get("type")
                            .getAsString(),
                    disabled);
        }
    }

    @Test
    void advancementGraphMatchesAllSixtyTwoR196Nodes() throws Exception {
        Map<String, String> parents = Map.ofEntries(
                Map.entry("stick_picker", "open_inventory"),
                Map.entry("cutting_edge", "stick_picker"),
                Map.entry("mine_wood", "cutting_edge"),
                Map.entry("build_work_bench", "mine_wood"),
                Map.entry("build_shovel", "build_work_bench"),
                Map.entry("nuggets", "build_shovel"),
                Map.entry("better_tools", "nuggets"),
                Map.entry("build_pickaxe", "better_tools"),
                Map.entry("build_furnace", "build_pickaxe"),
                Map.entry("acquire_iron", "build_furnace"),
                Map.entry("build_better_pickaxe", "acquire_iron"),
                Map.entry("obsidian_furnace", "build_better_pickaxe"),
                Map.entry("mithril_ingot", "obsidian_furnace"),
                Map.entry("diamonds", "mithril_ingot"),
                Map.entry("emeralds", "build_better_pickaxe"),
                Map.entry("enchantments", "diamonds"),
                Map.entry("overkill", "enchantments"),
                Map.entry("bookcase", "enchantments"),
                Map.entry("enlightenment", "bookcase"),
                Map.entry("portal", "build_better_pickaxe"),
                Map.entry("portal_to_nether", "portal"),
                Map.entry("ghast", "portal_to_nether"),
                Map.entry("blaze_rod", "portal_to_nether"),
                Map.entry("potion", "blaze_rod"),
                Map.entry("the_end", "blaze_rod"),
                Map.entry("the_end2", "the_end"),
                Map.entry("netherrack_furnace", "blaze_rod"),
                Map.entry("adamantium_ingot", "netherrack_furnace"),
                Map.entry("crystal_breaker", "adamantium_ingot"),
                Map.entry("runegate", "portal"),
                Map.entry("on_a_rail", "acquire_iron"),
                Map.entry("build_hoe", "better_tools"),
                Map.entry("flour", "build_hoe"),
                Map.entry("make_bread", "flour"),
                Map.entry("bake_cake", "flour"),
                Map.entry("build_scythe", "build_hoe"),
                Map.entry("soil_enrichment", "build_hoe"),
                Map.entry("make_mycelium", "soil_enrichment"),
                Map.entry("supersize_me", "make_mycelium"),
                Map.entry("plant_doctor", "build_hoe"),
                Map.entry("build_chain_mail", "better_tools"),
                Map.entry("wear_all_plate_armor", "build_chain_mail"),
                Map.entry("wear_all_adamantium_plate_armor", "wear_all_plate_armor"),
                Map.entry("fishing_rod", "better_tools"),
                Map.entry("cook_fish", "fishing_rod"),
                Map.entry("build_club", "build_work_bench"),
                Map.entry("kill_enemy", "build_club"),
                Map.entry("snipe_skeleton", "kill_enemy"),
                Map.entry("kill_cow", "build_club"),
                Map.entry("fly_pig", "kill_cow"),
                Map.entry("wear_leather", "kill_cow"),
                Map.entry("build_axe", "build_work_bench"),
                Map.entry("build_torches", "build_work_bench"),
                Map.entry("well_rested", "build_work_bench"),
                Map.entry("seaworthy", "build_work_bench"),
                Map.entry("fine_dining", "build_work_bench"),
                Map.entry("seeds", "open_inventory"),
                Map.entry("eggs", "seeds"),
                Map.entry("build_oven", "open_inventory"),
                Map.entry("flint_finder", "open_inventory"),
                Map.entry("explorer", "open_inventory"));
        Set<String> challenges = Set.of(
                "on_a_rail",
                "fly_pig",
                "snipe_skeleton",
                "ghast",
                "the_end",
                "the_end2",
                "overkill",
                "wear_all_adamantium_plate_armor",
                "explorer",
                "enlightenment",
                "runegate",
                "crystal_breaker");
        Path root = GENERATED.resolve("data/infx/advancement/progression");
        List<Path> files;
        try (Stream<Path> stream = Files.list(root)) {
            files = stream.filter(path -> path.toString().endsWith(".json")).toList();
        }
        assertEquals(62, files.size());
        assertEquals(61, parents.size());

        JsonObject english = json(GENERATED.resolve("assets/infx/lang/en_us.json"));
        JsonObject chinese = json(GENERATED.resolve("assets/infx/lang/zh_cn.json"));
        Set<String> actualNames = new HashSet<>();
        Set<String> actualChallenges = new HashSet<>();
        for (Path file : files) {
            String name = file.getFileName().toString().replaceFirst("\\.json$", "");
            actualNames.add(name);
            JsonObject advancement = json(file);
            if (name.equals("open_inventory")) {
                assertFalse(advancement.has("parent"));
            } else {
                assertEquals("infx:progression/" + parents.get(name), advancement.get("parent").getAsString(), name);
            }
            JsonObject display = advancement.getAsJsonObject("display");
            if (display.has("frame") && display.get("frame").getAsString().equals("challenge")) {
                actualChallenges.add(name);
            }
            assertTrue(english.has("advancements.infx." + name + ".title"), name);
            assertTrue(english.has("advancements.infx." + name + ".description"), name);
            assertTrue(chinese.has("advancements.infx." + name + ".title"), name);
            assertTrue(chinese.has("advancements.infx." + name + ".description"), name);
        }
        assertEquals(parents.keySet(), actualNames.stream()
                .filter(name -> !name.equals("open_inventory"))
                .collect(Collectors.toSet()));
        assertEquals(challenges, actualChallenges);

        JsonObject enchantments = json(root.resolve("enchantments.json"));
        var alternatives = enchantments.getAsJsonArray("requirements").get(0).getAsJsonArray();
        assertEquals(2, alternatives.size());
        assertTrue(alternatives.toString().contains("diamond_path"));
        assertTrue(alternatives.toString().contains("emerald_path"));

        JsonObject workbench = json(root.resolve("build_work_bench.json"));
        assertTrue(workbench.getAsJsonObject("criteria").has("crafted_flint_bench"));
        assertTrue(workbench.getAsJsonObject("criteria").has("crafted_obsidian_bench"));
        JsonObject betterTools = json(root.resolve("better_tools.json"));
        assertEquals(7, betterTools.getAsJsonObject("criteria").size());
        JsonObject nuggets = json(root.resolve("nuggets.json"));
        assertEquals(
                "minecraft:impossible",
                nuggets.getAsJsonObject("criteria")
                        .getAsJsonObject("picked_up_metal_nugget")
                        .get("trigger")
                        .getAsString());
        String mixedArmor = Files.readString(root.resolve("wear_all_plate_armor.json"), UTF_8);
        assertTrue(mixedArmor.contains("infx:copper_chainmail_helmet"));
        assertTrue(mixedArmor.contains("infx:adamantium_chainmail_boots"));
    }

    @Test
    void everyGeneratedModelAndTextureReferenceResolves() throws Exception {
        Path models = GENERATED.resolve("assets/infx/models");
        Path textures = STATIC.resolve("assets/infx/textures");
        try (Stream<Path> files = Files.walk(GENERATED.resolve("assets/infx/items"))) {
            for (Path definition : files.filter(path -> path.toString().endsWith(".json")).toList()) {
                visit(json(definition), (key, value) -> {
                    if (key.equals("model") && value.startsWith("infx:")) {
                        Path target = models.resolve(value.substring("infx:".length()) + ".json");
                        assertTrue(Files.isRegularFile(target), () -> definition + " -> " + value);
                    }
                });
            }
        }
        try (Stream<Path> files = Files.walk(models)) {
            for (Path model : files.filter(path -> path.toString().endsWith(".json")).toList()) {
                JsonObject root = json(model);
                if (!root.has("textures")) {
                    continue;
                }
                for (var texture : root.getAsJsonObject("textures").entrySet()) {
                    String value = texture.getValue().getAsString();
                    if (value.startsWith("infx:")) {
                        Path target = textures.resolve(value.substring("infx:".length()) + ".png");
                        assertTrue(Files.isRegularFile(target), () -> model + " -> " + value);
                    }
                }
            }
        }
    }

    @Test
    void bowAndFishingDispatchesAreComplete() throws Exception {
        for (R196Catalog.EquipmentEntry entry : ModItems.catalog().equipmentEntries()) {
            if (entry.key().type() == R196EquipmentType.BOW) {
                String definition = Files.readString(
                        GENERATED.resolve("assets/infx/items/" + entry.path() + ".json"), UTF_8);
                for (var material : R196EquipmentType.ARROW.allowedMaterials()) {
                    assertTrue(definition.contains(material.path()), entry.path() + " missing " + material.path());
                    for (int frame = 0; frame < 3; frame++) {
                        assertTrue(Files.isRegularFile(GENERATED.resolve("assets/infx/models/item/"
                                + entry.path()
                                + "/"
                                + material.path()
                                + "_"
                                + frame
                                + ".json")));
                    }
                }
            } else if (entry.key().type() == R196EquipmentType.FISHING_ROD) {
                String definition = Files.readString(
                        GENERATED.resolve("assets/infx/items/" + entry.path() + ".json"), UTF_8);
                assertTrue(definition.contains(entry.path() + "_cast"), entry.path());
                assertTrue(Files.isRegularFile(
                        GENERATED.resolve("assets/infx/models/item/" + entry.path() + "_cast.json")));
            }
        }
    }

    @Test
    void equipmentAssetsExposeEveryRequiredLayer() throws Exception {
        for (R196Catalog.EquipmentEntry entry : ModItems.catalog().equipmentEntries()) {
            var form = entry.key().type().armorForm();
            if (form == R196EquipmentType.ArmorForm.NONE) {
                continue;
            }
            String assetPath = entry.key().equipmentAsset().identifier().getPath();
            JsonObject layers = json(GENERATED.resolve("assets/infx/equipment/" + assetPath + ".json"))
                    .getAsJsonObject("layers");
            if (form == R196EquipmentType.ArmorForm.HORSE) {
                assertTrue(layers.has("horse_body"), entry.path());
            } else {
                assertAll(
                        entry.path(),
                        () -> assertTrue(layers.has("humanoid")),
                        () -> assertTrue(layers.has("humanoid_baby")),
                        () -> assertTrue(layers.has("humanoid_leggings")));
            }
        }
    }

    @Test
    void manifestHasOnlyCatalogOrApprovedDerivedTextures() throws Exception {
        Set<String> destinations = new HashSet<>();
        for (String line : Files.readAllLines(STATIC.resolve("assets/infx/mite_texture_manifest.tsv"), UTF_8)
                .stream()
                .skip(1)
                .toList()) {
            destinations.add(line.split("\t", -1)[2]);
        }
        for (R196Catalog.Entry entry : ModItems.catalog().entries()) {
            assertTrue(destinations.remove("textures/item/" + entry.path() + ".png"), entry.path());
        }
        assertTrue(destinations.remove("textures/item/fishing_rod_cast.png"));
        assertTrue(destinations.removeIf(path -> path.matches(
                "textures/item/(wood|ancient_metal|mithril)_bow/(flint|obsidian|copper|silver|gold|rusted_iron|iron|ancient_metal|mithril|adamantium)_[0-2]\\.png")));
        assertTrue(destinations.removeIf(
                path -> path.matches("textures/item/leather_(helmet|chestplate|leggings|boots)_overlay\\.png")));
        assertTrue(destinations.removeIf(path -> path.startsWith("textures/entity/equipment/")));
        assertTrue(destinations.removeIf(path -> path.matches(
                "textures/block/(flint|obsidian)_workbench_top\\.png"
                        + "|textures/block/(copper|silver|gold|iron|ancient_metal|mithril|adamantium)_workbench_(front|side)\\.png")));
        assertTrue(destinations.removeIf(path -> path.matches(
                "textures/block/(clay|hardened_clay|sandstone|obsidian|netherrack)_furnace_(front|front_on|side|top)\\.png")));
        assertTrue(destinations.remove("textures/block/silver_ore.png"));
        assertTrue(destinations.remove("textures/block/mithril_ore.png"));
        assertTrue(destinations.remove("textures/block/adamantium_ore.png"));
        assertTrue(destinations.removeIf(path -> path.matches(
                "textures/block/(silver|ancient_metal|mithril|adamantium)_block\\.png")));
        assertTrue(destinations.removeIf(path -> path.matches(
                "textures/block/anvil/(copper|silver|gold|iron|ancient_metal|mithril|adamantium)/(base|top_damaged_[0-2])\\.png")));
        assertTrue(destinations.removeIf(path -> path.matches(
                "textures/block/safe/(copper|silver|gold|iron|ancient_metal|mithril|adamantium)\\.png")));
        assertTrue(destinations.removeIf(path -> path.matches(
                "textures/item/(flour|water_bowl|dough|salad|blueberries|blueberry_porridge|milk_bowl|cereal_porridge"
                        + "|chocolate|pumpkin_soup|cream_of_mushroom_soup|onion|vegetable_soup"
                        + "|cream_of_vegetable_soup|chicken_soup|beef_stew|orange|fruit_ice|cheese"
                        + "|mashed_potato|ice_cream|banana|worm|cooked_worm)\\.png")));
        assertTrue(destinations.isEmpty(), () -> "unexpected selected textures " + destinations);
        assertFalse(Files.exists(STATIC.resolve("assets/minecraft")));
        assertFalse(Files.exists(GENERATED.resolve("assets/minecraft")));
    }

    @Test
    void safeAndFoodModelsReferenceTheirImportedMiteTextures() throws Exception {
        for (String material : List.of("copper", "silver", "gold", "iron", "ancient_metal", "mithril", "adamantium")) {
            JsonObject model = json(GENERATED.resolve("assets/infx/models/block/" + material + "_safe.json"));
            assertAll(
                    material + " safe",
                    () -> assertEquals("infx:block/template_metal_safe", model.get("parent").getAsString()),
                    () -> assertEquals(
                            "infx:block/safe/" + material,
                            model.getAsJsonObject("textures").get("texture").getAsString()));
        }
        for (String food : List.of(
                "flour",
                "water_bowl",
                "dough",
                "salad",
                "blueberries",
                "blueberry_porridge",
                "milk_bowl",
                "cereal_porridge",
                "chocolate",
                "pumpkin_soup",
                "cream_of_mushroom_soup",
                "onion",
                "vegetable_soup",
                "cream_of_vegetable_soup",
                "chicken_soup",
                "beef_stew",
                "orange",
                "fruit_ice",
                "cheese",
                "mashed_potato",
                "ice_cream",
                "banana",
                "worm",
                "cooked_worm")) {
            JsonObject model = json(GENERATED.resolve("assets/infx/models/item/" + food + ".json"));
            assertEquals(
                    "infx:item/" + food,
                    model.getAsJsonObject("textures").get("layer0").getAsString(),
                    food);
        }
    }

    @Test
    void miteHarvestLevelsAndEffectiveToolsCoverR196AndModern262Families() throws Exception {
        Map<Integer, Set<String>> levels = new java.util.LinkedHashMap<>();
        Set<String> directAssignments = new HashSet<>();
        for (int level = 0; level <= 6; level++) {
            Set<String> values = tagValues("requires_harvest_level/" + level);
            levels.put(level, values);
            for (String value : values) {
                if (!value.startsWith("#")) {
                    assertTrue(directAssignments.add(value), value + " is directly assigned to multiple levels");
                }
            }
        }

        assertAll(
                "MITE and 26.2 level representatives",
                () -> assertTrue(levels.get(0).contains("minecraft:coal_block")),
                () -> assertTrue(levels.get(0).contains("#minecraft:rails")),
                () -> assertTrue(levels.get(0).contains("minecraft:infested_stone")),
                () -> assertTrue(levels.get(0).contains("infx:sandstone_furnace")),
                () -> assertTrue(levels.get(1).contains("#c:glass_blocks")),
                () -> assertTrue(levels.get(1).contains("#minecraft:terracotta")),
                () -> assertTrue(levels.get(1).contains("#c:sandstone/slabs")),
                () -> assertFalse(levels.get(1).contains("#c:sandstone/stairs")),
                () -> assertTrue(levels.get(2).contains("infx:silver_ore")),
                () -> assertTrue(levels.get(3).contains("minecraft:copper_bulb")),
                () -> assertTrue(levels.get(3).contains("minecraft:waxed_oxidized_cut_copper_stairs")),
                () -> assertTrue(levels.get(3).contains("minecraft:redstone_block")),
                () -> assertTrue(levels.get(3).contains("infx:mithril_ore")),
                () -> assertTrue(levels.get(3).contains("infx:mithril_rune_stone")),
                () -> assertTrue(levels.get(3).contains("infx:adamantium_rune_stone")),
                () -> assertTrue(levels.get(4).contains("#c:ores/diamond")),
                () -> assertTrue(levels.get(4).contains("infx:ancient_metal_block")),
                () -> assertTrue(levels.get(5).contains("infx:mithril_block")),
                () -> assertTrue(levels.get(6).contains("infx:adamantium_block")));

        Set<String> pickaxe = tagValues("effective_tool/pickaxe");
        Set<String> axe = tagValues("effective_tool/axe");
        Set<String> shovel = tagValues("effective_tool/shovel");
        Set<String> hoe = tagValues("effective_tool/hoe");
        Set<String> cudgel = tagValues("effective_tool/cudgel");
        Set<String> sword = tagValues("effective_tool/sword");
        Set<String> shears = tagValues("effective_tool/shears");
        Set<String> axeHalfSpeed = tagValues("effective_tool/axe_half_speed");
        Set<String> portable = tagValues("portable_hand_harvest");
        assertAll(
                "MITE effective-tool and portability tags",
                () -> assertTrue(pickaxe.contains("#minecraft:mineable/pickaxe")),
                () -> assertTrue(pickaxe.contains("#c:glass_blocks")),
                () -> assertTrue(pickaxe.contains("#minecraft:flower_pots")),
                () -> assertTrue(pickaxe.contains("minecraft:glowstone")),
                () -> assertTrue(axe.contains("#c:sandstone/blocks")),
                () -> assertTrue(axe.contains("#c:sandstone/slabs")),
                () -> assertFalse(axe.contains("#c:sandstone/stairs")),
                () -> assertTrue(axe.contains("#minecraft:terracotta")),
                () -> assertTrue(shovel.contains("#c:glass_panes")),
                () -> assertTrue(shovel.contains("infx:infested_netherrack")),
                () -> assertTrue(hoe.contains("#minecraft:mineable/shovel")),
                () -> assertTrue(hoe.contains("infx:sandstone_furnace")),
                () -> assertTrue(cudgel.contains("minecraft:glowstone")),
                () -> assertTrue(cudgel.contains("#minecraft:coral_blocks")),
                () -> assertTrue(sword.contains("minecraft:hay_block")),
                () -> assertTrue(shears.contains("minecraft:nether_wart")),
                () -> assertTrue(axeHalfSpeed.contains("#c:sandstone/blocks")),
                () -> assertFalse(axeHalfSpeed.contains("#c:sandstone/slabs")),
                () -> assertTrue(portable.contains("minecraft:furnace")),
                () -> assertTrue(portable.contains("#minecraft:anvil")),
                () -> assertTrue(portable.contains("infx:adamantium_safe")));
    }

    private static Set<String> tagValues(String path) throws IOException {
        JsonObject tag = json(GENERATED.resolve("data/infx/tags/block/" + path + ".json"));
        return tag.getAsJsonArray("values").asList().stream()
                .map(value -> value.isJsonObject()
                        ? value.getAsJsonObject().get("id").getAsString()
                        : value.getAsString())
                .collect(Collectors.toSet());
    }

    private static JsonObject json(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static long jsonCount(Path root) throws IOException {
        try (Stream<Path> files = Files.walk(root)) {
            return files.filter(path -> path.toString().endsWith(".json")).count();
        }
    }

    private static void visit(JsonElement element, BiConsumer<String, String> strings) {
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(child -> visit(child, strings));
        } else if (element.isJsonObject()) {
            element.getAsJsonObject().entrySet().forEach(entry -> {
                if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isString()) {
                    strings.accept(entry.getKey(), entry.getValue().getAsString());
                }
                visit(entry.getValue(), strings);
            });
        }
    }

    private static boolean hasGradient(
            JsonElement element,
            int fromY,
            int toY,
            double fromValue,
            double toValue) {
        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                if (hasGradient(child, fromY, toY, fromValue, toValue)) {
                    return true;
                }
            }
            return false;
        }
        if (!element.isJsonObject()) {
            return false;
        }
        JsonObject object = element.getAsJsonObject();
        if (object.has("type")
                && object.get("type").getAsString().equals("minecraft:y_clamped_gradient")
                && object.get("from_y").getAsInt() == fromY
                && object.get("to_y").getAsInt() == toY
                && Math.abs(object.get("from_value").getAsDouble() - fromValue) < 1.0E-12
                && Math.abs(object.get("to_value").getAsDouble() - toValue) < 1.0E-12) {
            return true;
        }
        for (var entry : object.entrySet()) {
            if (hasGradient(entry.getValue(), fromY, toY, fromValue, toValue)) {
                return true;
            }
        }
        return false;
    }

    private static void assertInternalBedrockBand(
            JsonObject bandRule,
            int minimumBuildY,
            int lowerOffset,
            int upperOffset,
            int expectedLowerY,
            int expectedUpperY,
            String gapNoise) {
        JsonObject lower = bandRule.getAsJsonObject("if_true");
        JsonObject upper = bandRule
                .getAsJsonObject("then_run")
                .getAsJsonObject("if_true")
                .getAsJsonObject("invert");
        JsonObject noise = bandRule
                .getAsJsonObject("then_run")
                .getAsJsonObject("then_run")
                .getAsJsonObject("if_true");
        JsonObject block = bandRule
                .getAsJsonObject("then_run")
                .getAsJsonObject("then_run")
                .getAsJsonObject("then_run")
                .getAsJsonObject("result_state");
        assertAll(
                expectedLowerY + "-" + expectedUpperY + " internal bedrock band",
                () -> assertEquals("minecraft:y_above", lower.get("type").getAsString()),
                () -> assertEquals(
                        lowerOffset,
                        lower.getAsJsonObject("anchor").get("above_bottom").getAsInt()),
                () -> assertEquals("minecraft:y_above", upper.get("type").getAsString()),
                () -> assertEquals(
                        upperOffset + 1,
                        upper.getAsJsonObject("anchor").get("above_bottom").getAsInt()),
                () -> assertEquals(expectedLowerY, minimumBuildY + lowerOffset),
                () -> assertEquals(expectedUpperY, minimumBuildY + upperOffset),
                () -> assertEquals("minecraft:noise_threshold", noise.get("type").getAsString()),
                () -> assertEquals(gapNoise, noise.get("noise").getAsString()),
                () -> assertEquals(0.62, noise.get("max_threshold").getAsDouble()),
                () -> assertEquals("minecraft:bedrock", block.get("Name").getAsString()));
    }

    private static Path findProjectRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("settings.gradle"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not locate project root from " + Path.of("").toAbsolutePath());
    }
}
