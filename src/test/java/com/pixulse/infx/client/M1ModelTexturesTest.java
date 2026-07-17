package com.pixulse.infx.client;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonParser;

class M1ModelTexturesTest {
    private static final Map<String, String> ITEM_MODELS = Map.ofEntries(
            Map.entry("/assets/infx/models/item/flint_chip.json", "infx:item/flint_chip"),
            Map.entry("/assets/infx/models/item/obsidian_shard.json", "infx:item/obsidian_shard"),
            Map.entry("/assets/infx/models/item/emerald_shard.json", "infx:item/emerald_shard"),
            Map.entry("/assets/infx/models/item/sinew.json", "infx:item/sinew"),
            Map.entry("/assets/infx/models/item/silver_nugget.json", "infx:item/silver_nugget"),
            Map.entry("/assets/infx/models/item/mithril_nugget.json", "infx:item/mithril_nugget"),
            Map.entry("/assets/infx/models/item/adamantium_nugget.json", "infx:item/adamantium_nugget"),
            Map.entry("/assets/infx/models/item/flint_hatchet.json", "infx:item/flint_hatchet"),
            Map.entry("/assets/infx/models/item/copper_pickaxe.json", "infx:item/copper_pickaxe"));

    private static final Map<String, Map<String, String>> BLOCK_MODELS = Map.of(
            "/assets/infx/models/block/flint_workbench.json",
            Map.of(
                    "down", "minecraft:block/oak_planks",
                    "east", "minecraft:block/crafting_table_side",
                    "north", "minecraft:block/crafting_table_front",
                    "particle", "minecraft:block/crafting_table_front",
                    "south", "minecraft:block/crafting_table_side",
                    "up", "infx:block/flint_workbench_top",
                    "west", "minecraft:block/crafting_table_front"),
            "/assets/infx/models/block/copper_workbench.json",
            Map.of(
                    "down", "minecraft:block/copper_block",
                    "east", "infx:block/copper_workbench_side",
                    "north", "infx:block/copper_workbench_front",
                    "particle", "minecraft:block/copper_block",
                    "south", "infx:block/copper_workbench_front",
                    "up", "minecraft:block/copper_block",
                    "west", "infx:block/copper_workbench_side"));

    @Test
    void itemModelsUseTheirInfiniteXTextures() throws IOException {
        for (var entry : ITEM_MODELS.entrySet()) {
            assertEquals(entry.getValue(), readTextures(entry.getKey()).get("layer0"), entry.getKey());
        }
    }

    @Test
    void workbenchModelsUseOnlyTheirSelectedMiteFaces() throws IOException {
        for (var entry : BLOCK_MODELS.entrySet()) {
            assertEquals(entry.getValue(), readTextures(entry.getKey()), entry.getKey());
        }
    }

    private Map<String, String> readTextures(String resourcePath) throws IOException {
        try (InputStream input = getClass().getResourceAsStream(resourcePath)) {
            assertNotNull(input, () -> "Missing model " + resourcePath);
            try (var reader = new InputStreamReader(input, UTF_8)) {
                return JsonParser.parseReader(reader)
                        .getAsJsonObject()
                        .getAsJsonObject("textures")
                        .entrySet()
                        .stream()
                        .collect(Collectors.toUnmodifiableMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().getAsString()));
            }
        }
    }
}
