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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("generated-resources")
class UnderworldCarverGeneratedResourceTest {
    private static final Path ROOT = findProjectRoot();
    private static final Path GENERATED = ROOT.resolve("src/generated/resources");

    @Test
    void underworldCarversCanReplaceBedrockWithoutMakingMantleOrOtherBiomesReplaceable() throws Exception {
        JsonObject replaceables = json(GENERATED.resolve("data/infx/tags/block/underworld_carver_replaceables.json"));
        List<String> values = strings(replaceables.getAsJsonArray("values"));
        JsonObject underworld = json(GENERATED.resolve("data/infx/worldgen/biome/underworld.json"));
        List<String> underworldCarvers = strings(underworld.getAsJsonArray("carvers"));
        List<String> riverCarvers = strings(json(GENERATED.resolve("data/infx/worldgen/biome/desert_river.json"))
                .getAsJsonArray("carvers"));

        assertAll(
                () -> assertEquals(
                        List.of("#minecraft:overworld_carver_replaceables", "minecraft:bedrock"),
                        values),
                () -> assertFalse(values.contains("infx:mantle")),
                () -> assertEquals(
                        List.of(
                                "infx:underworld_cave",
                                "infx:underworld_cave_extra_underground",
                                "infx:underworld_canyon"),
                        underworldCarvers),
                () -> assertFalse(riverCarvers.contains("infx:underworld_")),
                () -> assertTrue(riverCarvers.contains("minecraft:cave")));

        assertCarverUsesUnderworldReplaceables("underworld_cave", "minecraft:cave");
        assertCarverUsesUnderworldReplaceables("underworld_cave_extra_underground", "minecraft:cave");
        assertCarverUsesUnderworldReplaceables("underworld_canyon", "minecraft:canyon");
    }

    private static void assertCarverUsesUnderworldReplaceables(String name, String type) throws IOException {
        JsonObject carver = json(GENERATED.resolve("data/infx/worldgen/configured_carver/" + name + ".json"));
        assertAll(
                name,
                () -> assertEquals(type, carver.get("type").getAsString()),
                () -> assertEquals(
                        "#infx:underworld_carver_replaceables",
                        carver.getAsJsonObject("config").get("replaceable").getAsString()));
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
