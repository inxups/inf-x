package com.pixulse.infx.client;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("generated-resources")
class R196FulltextGeneratedResourceTest {
    private static final Path ROOT = findProjectRoot();
    private static final Path GENERATED = ROOT.resolve("src/generated/resources");

    @Test
    void allThirtyFiveBucketsHaveModelsAndBothMaterialRecipes() throws Exception {
        Map<String, String> benches = Map.of(
                "copper", "copper",
                "silver", "copper",
                "gold", "copper",
                "iron", "iron",
                "ancient_metal", "ancient_metal",
                "mithril", "mithril",
                "adamantium", "adamantium");
        Map<String, Float> difficulties = Map.of(
                "copper", 1_200.0F,
                "silver", 1_200.0F,
                "gold", 1_200.0F,
                "iron", 2_400.0F,
                "ancient_metal", 4_800.0F,
                "mithril", 19_200.0F,
                "adamantium", 76_800.0F);
        for (String material : benches.keySet()) {
            for (String contents : List.of("", "water_", "lava_", "milk_", "stone_")) {
                String path = material + "_" + contents + "bucket";
                assertAll(
                        path,
                        () -> assertTrue(Files.isRegularFile(
                                GENERATED.resolve("assets/infx/items/" + path + ".json"))),
                        () -> assertTrue(Files.isRegularFile(
                                GENERATED.resolve("assets/infx/models/item/" + path + ".json"))));
            }

            JsonObject shaped = json(GENERATED.resolve("data/infx/recipe/" + material + "_bucket.json"));
            JsonObject reclaimed = json(GENERATED.resolve(
                    "data/infx/recipe/" + material + "_bucket_from_stone_bucket.json"));
            assertAll(
                    material + " bucket recipes",
                    () -> assertEquals(benches.get(material), shaped.get("required_bench").getAsString()),
                    () -> assertEquals(difficulties.get(material), shaped.get("difficulty").getAsFloat()),
                    () -> assertEquals(List.of("I I", " I "), strings(shaped.getAsJsonArray("pattern"))),
                    () -> assertEquals(
                            "infx:" + material + "_bucket",
                            shaped.getAsJsonObject("result").get("id").getAsString()),
                    () -> assertEquals("hand", reclaimed.get("required_bench").getAsString()),
                    () -> assertEquals(100.0F, reclaimed.get("difficulty").getAsFloat()),
                    () -> assertEquals(
                            "infx:" + material + "_stone_bucket",
                            reclaimed.getAsJsonArray("ingredients").get(0).getAsString()));
        }
    }

    @Test
    void disenchantingBottleRequiresExactlyAWaterPotion() throws Exception {
        JsonObject recipe = json(GENERATED.resolve("data/infx/recipe/bottle_of_disenchanting.json"));
        JsonObject water = recipe.getAsJsonArray("ingredients").get(0).getAsJsonObject();
        assertAll(
                () -> assertEquals("neoforge:components", water.get("neoforge:ingredient_type").getAsString()),
                () -> assertEquals("minecraft:potion", water.get("items").getAsString()),
                () -> assertEquals(
                        "minecraft:water",
                        water.getAsJsonObject("components")
                                .getAsJsonObject("minecraft:potion_contents")
                                .get("potion")
                                .getAsString()),
                () -> assertEquals("minecraft:nether_wart", recipe.getAsJsonArray("ingredients").get(1).getAsString()),
                () -> assertEquals("minecraft:charcoal", recipe.getAsJsonArray("ingredients").get(2).getAsString()),
                () -> assertEquals(
                        "infx:bottle_of_disenchanting",
                        recipe.getAsJsonObject("result").get("id").getAsString()));
    }

    @Test
    void flowersNetherBlocksCoreAndRecordsHaveCompleteGeneratedData() throws Exception {
        for (String block : List.of(
                "rose", "orchid", "allium", "tulip", "dahlia", "daisy",
                "witherwood", "nether_gravel", "core")) {
            assertAll(
                    block,
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("assets/infx/blockstates/" + block + ".json"))),
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("assets/infx/items/" + block + ".json"))),
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("assets/infx/models/block/" + block + ".json"))),
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("data/infx/loot_table/blocks/" + block + ".json"))));
        }
        JsonObject coreLoot = json(GENERATED.resolve("data/infx/loot_table/blocks/core.json"));
        assertFalse(coreLoot.has("pools"), "the indestructible Core must not drop itself");

        Map<String, Integer> comparatorOutputs = Map.of(
                "underworld", 11,
                "descent", 12,
                "wanderer", 13,
                "legends", 14);
        for (var entry : comparatorOutputs.entrySet()) {
            String record = "record_" + entry.getKey();
            JsonObject song = json(GENERATED.resolve(
                    "data/infx/jukebox_song/" + entry.getKey() + ".json"));
            assertAll(
                    entry.getKey(),
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("assets/infx/items/" + record + ".json"))),
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("assets/infx/models/item/" + record + ".json"))),
                    () -> assertEquals(entry.getValue(), song.get("comparator_output").getAsInt()),
                    () -> assertEquals(
                            "infx:record." + entry.getKey(),
                            song.get("sound_event").getAsString()));
        }
    }

    @Test
    void fulltextWorldgenHasDistinctRiversNaturalPlantsAndDistantCaves() throws Exception {
        Map<String, float[]> climate = Map.of(
                "desert_river", new float[] {1.4F, 0.0F, 0.0F},
                "jungle_river", new float[] {1.0F, 0.9F, 1.0F},
                "swamp_river", new float[] {0.8F, 0.9F, 1.0F});
        for (var entry : climate.entrySet()) {
            JsonObject biome = json(GENERATED.resolve(
                    "data/infx/worldgen/biome/" + entry.getKey() + ".json"));
            float[] expected = entry.getValue();
            assertAll(
                    entry.getKey(),
                    () -> assertEquals(expected[0], biome.get("temperature").getAsFloat()),
                    () -> assertEquals(expected[1], biome.get("downfall").getAsFloat()),
                    () -> assertEquals(expected[2] == 1.0F, biome.get("has_precipitation").getAsBoolean()));
        }
        JsonObject swamp = json(GENERATED.resolve("data/infx/worldgen/biome/swamp_river.json"));
        assertEquals("swamp", swamp.getAsJsonObject("effects").get("grass_color_modifier").getAsString());

        JsonObject overworld = json(GENERATED.resolve("data/minecraft/dimension/overworld.json"));
        String overworldText = overworld.toString();
        for (String river : climate.keySet()) {
            assertTrue(overworldText.contains("infx:" + river), "Overworld climate source misses " + river);
        }

        JsonObject carver = json(GENERATED.resolve("data/infx/worldgen/configured_carver/large_cave.json"));
        JsonObject y = carver.getAsJsonObject("config").getAsJsonObject("y");
        assertAll(
                "distant large cave carver",
                () -> assertEquals("infx:large_cave", carver.get("type").getAsString()),
                () -> assertEquals(8, y.getAsJsonObject("min_inclusive").get("absolute").getAsInt()),
                () -> assertEquals(55, y.getAsJsonObject("max_inclusive").get("absolute").getAsInt()),
                () -> assertTrue(Files.isRegularFile(GENERATED.resolve(
                        "data/infx/neoforge/biome_modifier/add_large_caves.json"))));

        for (String feature : List.of("r196_flowers", "r196_allium", "witherwood_patch")) {
            assertAll(
                    feature,
                    () -> assertTrue(Files.isRegularFile(GENERATED.resolve(
                            "data/infx/worldgen/configured_feature/" + feature + ".json"))),
                    () -> assertTrue(Files.isRegularFile(GENERATED.resolve(
                            "data/infx/worldgen/placed_feature/" + feature + ".json"))));
        }
        assertTrue(Files.isRegularFile(GENERATED.resolve(
                "data/infx/neoforge/biome_modifier/add_r196_flowers.json")));
        assertTrue(Files.isRegularFile(GENERATED.resolve(
                "data/infx/neoforge/biome_modifier/add_r196_allium.json")));
        JsonObject witherwoodModifier = json(GENERATED.resolve(
                "data/infx/neoforge/biome_modifier/add_witherwood.json"));
        assertAll(
                "natural Nether witherwood",
                () -> assertEquals("#minecraft:is_nether", witherwoodModifier.get("biomes").getAsString()),
                () -> assertEquals("infx:witherwood_patch", witherwoodModifier.get("features").getAsString()));

        JsonObject netherGravel = json(GENERATED.resolve(
                "data/minecraft/worldgen/configured_feature/ore_gravel_nether.json"));
        assertTrue(netherGravel.toString().contains("infx:nether_gravel"));
        JsonObject underworld = json(GENERATED.resolve(
                "data/infx/worldgen/noise_settings/underworld.json"));
        assertFalse(underworld.toString().contains("infx:core"), "Underworld terrain must not use Core");
        assertFalse(underworld.toString().contains("infx:mantle"), "Mantle is written by the MITE runtime strata pass");
        assertFalse(underworld.toString().contains("minecraft:bedrock"), "Bedrock is written by the MITE runtime strata pass");
    }

    private static List<String> strings(JsonArray array) {
        return array.asList().stream().map(element -> element.getAsString()).toList();
    }

    private static JsonObject json(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static Path findProjectRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("settings.gradle"))) return current;
            current = current.getParent();
        }
        throw new IllegalStateException("Could not locate project root");
    }
}
