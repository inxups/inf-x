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
    void generatedCountsAreExact() throws Exception {
        assertEquals(234, jsonCount(GENERATED.resolve("assets/infx/items")));
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
