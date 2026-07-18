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
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("generated-resources")
class R196GeneratedResourceTest {
    private static final Path ROOT = findProjectRoot();
    private static final Path GENERATED = ROOT.resolve("src/generated/resources");
    private static final Path STATIC = ROOT.resolve("src/main/resources");

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
                () -> assertTrue(heatTwoInputs.contains("minecraft:sandstone")));
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
        assertEquals(239, jsonCount(GENERATED.resolve("assets/infx/items")));
        assertEquals(337, jsonCount(GENERATED.resolve("assets/infx/models/item")));
        assertEquals(17, jsonCount(GENERATED.resolve("assets/infx/equipment")));
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
        assertTrue(destinations.isEmpty(), () -> "unexpected selected textures " + destinations);
        assertFalse(Files.exists(STATIC.resolve("assets/minecraft")));
        assertFalse(Files.exists(GENERATED.resolve("assets/minecraft")));
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
