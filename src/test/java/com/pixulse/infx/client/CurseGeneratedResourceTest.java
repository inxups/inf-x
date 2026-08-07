package com.pixulse.infx.client;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pixulse.infx.data.curse.CurseType;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("generated-resources")
class CurseGeneratedResourceTest {
    private static final Path ROOT = findProjectRoot();
    private static final Path GENERATED = ROOT.resolve("src/generated/resources");

    @Test
    void everyCurseHasEnglishAndChineseNamesAndDescriptions() throws Exception {
        JsonObject english = json(GENERATED.resolve("assets/infx/lang/en_us.json"));
        JsonObject chinese = json(GENERATED.resolve("assets/infx/lang/zh_cn.json"));
        for (CurseType type : CurseType.values()) {
            String base = "curse.infx." + type.getSerializedName();
            assertTrue(english.has(base + ".name"), "missing English name for " + type);
            assertTrue(english.has(base + ".desc"), "missing English description for " + type);
            assertTrue(chinese.has(base + ".name"), "missing Chinese name for " + type);
            assertTrue(chinese.has(base + ".desc"), "missing Chinese description for " + type);
        }
        for (String key : new String[] {
            "curse.infx.unknown",
            "hud.infx.curse",
            "message.infx.curse.realized",
            "message.infx.curse.learned",
            "message.infx.curse.lifted",
            "message.infx.curse.cannot_sleep"
        }) {
            assertTrue(english.has(key), "missing English lifecycle text " + key);
            assertTrue(chinese.has(key), "missing Chinese lifecycle text " + key);
        }
    }

    @Test
    void curseBehaviorTagsCoverCategories() throws Exception {
        String vines = text("data/infx/tags/block/curse/vines.json");
        String plants = text("data/infx/tags/block/curse/plants.json");
        String animals = text("data/infx/tags/item/curse/animal_products.json");
        String plantProducts = text("data/infx/tags/item/curse/plant_products.json");
        String drinks = text("data/infx/tags/item/curse/drinks.json");

        assertTrue(vines.contains("minecraft:vine"));
        assertTrue(vines.contains("#minecraft:cave_vines"));
        assertTrue(plants.contains("#minecraft:crops"));
        assertTrue(plants.contains("minecraft:short_grass"));
        assertTrue(animals.contains("#minecraft:meat"));
        assertTrue(animals.contains("minecraft:cod"));
        assertTrue(animals.contains("minecraft:salmon"));
        assertTrue(animals.contains("infx:milk_bowl"));
        assertTrue(plantProducts.contains("minecraft:apple"));
        assertTrue(plantProducts.contains("infx:vegetable_soup"));
        assertTrue(drinks.contains("minecraft:potion"));
        assertTrue(drinks.contains("infx:chicken_soup"));
        assertFalse(drinks.contains("minecraft:mushroom_stew"));
        assertFalse(drinks.contains("infx:beef_stew"));
        assertFalse(drinks.contains("infx:bottle_of_disenchanting"));
    }

    private static JsonObject json(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static String text(String relative) throws IOException {
        return Files.readString(GENERATED.resolve(relative), UTF_8);
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
