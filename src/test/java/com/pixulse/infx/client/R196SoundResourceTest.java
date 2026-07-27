package com.pixulse.infx.client;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class R196SoundResourceTest {
    @Test
    void gelatinousCubeCorrosionUsesTheImportedMiteSizzle() throws Exception {
        Path sounds = findProjectRoot().resolve("src/main/resources/assets/infx/sounds.json");
        try (Reader reader = Files.newBufferedReader(sounds, UTF_8)) {
            JsonObject event = JsonParser.parseReader(reader)
                    .getAsJsonObject()
                    .getAsJsonObject("entity.gelatinous_cube.corrosion");
            assertEquals("infx:random/sizzle", event.getAsJsonArray("sounds").get(0).getAsString());
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
