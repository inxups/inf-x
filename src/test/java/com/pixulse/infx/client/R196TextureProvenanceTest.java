package com.pixulse.infx.client;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class R196TextureProvenanceTest {
    private static final Path ROOT = findProjectRoot();
    private static final Path ASSETS = ROOT.resolve("src/main/resources/assets/infx");
    private static final Path MANIFEST = ASSETS.resolve("mite_texture_manifest.tsv");

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

    @Test
    void everySelectedDestinationIsUniqueReadableAndHashPinned() throws Exception {
        List<String> lines = Files.readAllLines(MANIFEST, UTF_8);
        assertEquals("source_root\tsource\tdestination\tsha256", lines.getFirst());
        assertEquals(493, lines.size(), "header plus 492 selected destinations");
        Set<String> destinations = new HashSet<>();
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        for (String line : lines.subList(1, lines.size())) {
            String[] fields = line.split("\t", -1);
            assertEquals(4, fields.length, line);
            assertTrue(destinations.add(fields[2]), "duplicate destination " + fields[2]);
            Path destination = ASSETS.resolve(fields[2]);
            byte[] bytes = Files.readAllBytes(destination);
            assertEquals(fields[3], HexFormat.of().formatHex(sha256.digest(bytes)), fields[2]);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            assertNotNull(image, fields[2]);
            assertTrue(image.getWidth() > 0 && image.getHeight() > 0, fields[2]);
        }
    }

    @Test
    void safeAndFoodDestinationsUseTheirExplicitMiteTextures() throws Exception {
        Map<String, String> sourcesByDestination = Files.readAllLines(MANIFEST, UTF_8).stream()
                .skip(1)
                .map(line -> line.split("\\t", -1))
                .collect(Collectors.toMap(fields -> fields[2], fields -> fields[1]));
        Map<String, String> expected = Map.ofEntries(
                Map.entry("textures/entity/chest/copper.png", "entity/chest/copper_single.png"),
                Map.entry("textures/entity/chest/silver.png", "entity/chest/silver_single.png"),
                Map.entry("textures/entity/chest/gold.png", "entity/chest/gold_single.png"),
                Map.entry("textures/entity/chest/iron.png", "entity/chest/iron_single.png"),
                Map.entry("textures/entity/chest/ancient_metal.png", "entity/chest/ancient_metal_single.png"),
                Map.entry("textures/entity/chest/mithril.png", "entity/chest/mithril_single.png"),
                Map.entry("textures/entity/chest/adamantium.png", "entity/chest/adamantium_single.png"),
                Map.entry("textures/item/flour.png", "items/food/flour.png"),
                Map.entry("textures/item/water_bowl.png", "items/bowls/bowl_water.png"),
                Map.entry("textures/item/dough.png", "items/food/dough.png"),
                Map.entry("textures/item/salad.png", "items/bowls/bowl_salad.png"),
                Map.entry("textures/item/blueberries.png", "items/food/blueberries.png"),
                Map.entry("textures/item/blueberry_porridge.png", "items/bowls/porridge.png"),
                Map.entry("textures/item/milk_bowl.png", "items/bowls/bowl_milk.png"),
                Map.entry("textures/item/cereal_porridge.png", "items/bowls/cereal.png"),
                Map.entry("textures/item/chocolate.png", "items/food/chocolate.png"),
                Map.entry("textures/item/pumpkin_soup.png", "items/bowls/pumpkin_soup.png"),
                Map.entry("textures/item/cream_of_mushroom_soup.png", "items/bowls/cream_of_mushroom_soup.png"),
                Map.entry("textures/item/onion.png", "items/food/onion.png"),
                Map.entry("textures/item/vegetable_soup.png", "items/bowls/vegetable_soup.png"),
                Map.entry("textures/item/cream_of_vegetable_soup.png", "items/bowls/cream_of_vegetable_soup.png"),
                Map.entry("textures/item/chicken_soup.png", "items/bowls/chicken_soup.png"),
                Map.entry("textures/item/beef_stew.png", "items/bowls/beef_stew.png"),
                Map.entry("textures/item/orange.png", "items/food/orange.png"),
                Map.entry("textures/item/fruit_ice.png", "items/bowls/sorbet.png"),
                Map.entry("textures/item/cheese.png", "items/food/cheese.png"),
                Map.entry("textures/item/mashed_potato.png", "items/bowls/mashed_potato.png"),
                Map.entry("textures/item/ice_cream.png", "items/bowls/ice_cream.png"),
                Map.entry("textures/item/banana.png", "items/food/banana.png"),
                Map.entry("textures/item/worm.png", "items/food/worm_raw.png"),
                Map.entry("textures/item/cooked_worm.png", "items/food/worm_cooked.png"));
        assertEquals(expected, expected.keySet().stream()
                .collect(Collectors.toMap(destination -> destination, sourcesByDestination::get)));
    }

    @Test
    void optionalLocalSourcesMatchByteForByte() throws Exception {
        Path reference = ROOT.resolve("codex/reference");
        if (!Files.isDirectory(reference)) {
            reference = Path.of("/Users/inxups/IdeaProjects/mc/inf-x/codex/reference");
        }
        if (!Files.isDirectory(reference)) {
            return;
        }
        List<String> lines = Files.readAllLines(MANIFEST, UTF_8);
        for (String line : lines.subList(1, lines.size())) {
            String[] fields = line.split("\t", -1);
            Path sourceRoot = switch (fields[0]) {
                case "resource-pack" -> reference.resolve("mite- resource-pack/assets/minecraft/textures");
                case "mite-src" -> reference.resolve("mite-src/assets/minecraft/textures");
                default -> throw new AssertionError("unknown source root " + fields[0]);
            };
            assertEquals(
                    -1L,
                    Files.mismatch(sourceRoot.resolve(fields[1]), ASSETS.resolve(fields[2])),
                    fields[2]);
        }
    }

    @Test
    void noVanillaNamespaceOrUnapprovedArtifactIsCommitted() throws Exception {
        assertFalse(Files.exists(ROOT.resolve("src/main/resources/assets/minecraft")));
        Set<String> destinations = Files.readAllLines(MANIFEST, UTF_8).stream()
                .skip(1)
                .map(line -> line.split("\t", -1)[2])
                .collect(java.util.stream.Collectors.toSet());
        assertFalse(destinations.stream().anyMatch(path -> path.contains("diamond_helmet")));
        assertFalse(destinations.stream().anyMatch(path -> path.contains("iron_knife")));
        assertFalse(destinations.stream().anyMatch(path -> path.contains("stone_dagger")));
        assertFalse(destinations.stream().anyMatch(path -> path.contains("chip_flint_knife")));
        assertFalse(destinations.stream().anyMatch(path -> path.contains("iron_coin")));
        assertFalse(destinations.stream().anyMatch(path -> path.endsWith("_frags.png")));
    }
}
