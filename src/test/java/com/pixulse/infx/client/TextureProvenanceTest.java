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

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.registry.InfXMobEffects;
import net.minecraft.client.gui.Gui;
import org.junit.jupiter.api.Test;

class TextureProvenanceTest {
    private static final Path ROOT = findProjectRoot();
    private static final Path ASSETS = ROOT.resolve("src/main/resources/assets/infx");
    private static final Path MANIFEST = ASSETS.resolve("infx_texture_manifest.tsv");

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
        Set<String> destinations = new HashSet<>();
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        for (String line : lines.subList(1, lines.size())) {
            String[] fields = line.split("\t", -1);
            assertEquals(4, fields.length, line);
            assertTrue(
                    fields[0].equals("resource-pack")
                            || fields[0].equals("mite-src")
                            || fields[0].equals("itf-reborn")
                            || fields[0].equals("workbench-top")
                            || fields[0].equals("derived"),
                    "unknown source root " + fields[0]);
            assertTrue(destinations.add(fields[2]), "duplicate destination " + fields[2]);
            Path destination = ASSETS.resolve(fields[2]);
            byte[] bytes = Files.readAllBytes(destination);
            assertEquals(fields[3], HexFormat.of().formatHex(sha256.digest(bytes)), fields[2]);
            if (!fields[2].endsWith(".png")) {
                continue;
            }
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            assertNotNull(image, fields[2]);
            assertTrue(image.getWidth() > 0 && image.getHeight() > 0, fields[2]);
        }
    }

    @Test
    void malnutritionEffectIconIsCompleteAndCentered() throws Exception {
        assertEquals(
                InfiniteX.id("mob_effect/malnutrition"),
                Gui.getMobEffectSprite(InfXMobEffects.MALNUTRITION));
        BufferedImage image = ImageIO.read(ASSETS.resolve("textures/mob_effect/malnutrition.png").toFile());
        assertNotNull(image);
        assertEquals(18, image.getWidth());
        assertEquals(18, image.getHeight());

        int minX = image.getWidth();
        int minY = image.getHeight();
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) == 0) {
                    continue;
                }
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
        }
        assertEquals(1, minX);
        assertEquals(1, minY);
        assertEquals(16, maxX);
        assertEquals(16, maxY);
    }

    @Test
    void insulinResistanceEffectIconUsesTheCenteredSugarSprite() throws Exception {
        assertEquals(
                InfiniteX.id("mob_effect/insulin_resistance"),
                Gui.getMobEffectSprite(InfXMobEffects.INSULIN_RESISTANCE));
        BufferedImage image = ImageIO.read(ASSETS.resolve("textures/mob_effect/insulin_resistance.png").toFile());
        assertNotNull(image);
        assertEquals(18, image.getWidth());
        assertEquals(18, image.getHeight());

        int minX = image.getWidth();
        int minY = image.getHeight();
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) == 0) {
                    continue;
                }
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
        }
        assertEquals(3, minX);
        assertEquals(4, minY);
        assertEquals(14, maxX);
        assertEquals(14, maxY);
    }

    @Test
    void witchCurseEffectIconUsesTheGoldRingSprite() throws Exception {
        assertEquals(
                InfiniteX.id("mob_effect/witch_curse"),
                Gui.getMobEffectSprite(InfXMobEffects.WITCH_CURSE));
        BufferedImage image = ImageIO.read(ASSETS.resolve("textures/mob_effect/witch_curse.png").toFile());
        assertNotNull(image);
        assertEquals(18, image.getWidth());
        assertEquals(18, image.getHeight());

        int minX = image.getWidth();
        int minY = image.getHeight();
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) == 0) {
                    continue;
                }
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
        }
        assertEquals(2, minX);
        assertEquals(0, minY);
        assertEquals(15, maxX);
        assertEquals(17, maxY);
    }

    @Test
    void safeFoodAndGelatinousDestinationsUseTheirExplicitTextures() throws Exception {
        Map<String, String> sourcesByDestination = Files.readAllLines(MANIFEST, UTF_8).stream()
                .skip(1)
                .map(line -> line.split("\\t", -1))
                .collect(Collectors.toMap(fields -> fields[2], fields -> fields[1]));
        Map<String, String> expected = Map.ofEntries(
                Map.entry(
                        "textures/entity/chest/copper.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/entity/chest/copper.png"),
                Map.entry(
                        "textures/entity/chest/silver.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/entity/chest/silver.png"),
                Map.entry(
                        "textures/entity/chest/gold.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/entity/chest/gold.png"),
                Map.entry(
                        "textures/entity/chest/iron.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/entity/chest/iron.png"),
                Map.entry(
                        "textures/entity/chest/ancient_metal.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/entity/chest/ancient_metal.png"),
                Map.entry(
                        "textures/entity/chest/mithril.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/entity/chest/mithril.png"),
                Map.entry(
                        "textures/entity/chest/adamantium.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/entity/chest/adamantium.png"),
                Map.entry("textures/block/blueberry_bush.png", "blocks/bushes/blueberry.png"),
                Map.entry("textures/block/blueberry_bush_picked.png", "blocks/bushes/blueberry_picked.png"),
                Map.entry(
                        "textures/block/emerald_enchanting_table_side.png",
                        "blocks/emerald_enchanting_table_side.png"),
                Map.entry(
                        "textures/block/emerald_enchanting_table_top.png",
                        "blocks/emerald_enchanting_table_top.png"),
                Map.entry("textures/block/mantle.png", "blocks/mantle.png"),
                Map.entry("textures/block/mantle.png.mcmeta", "blocks/mantle.png.mcmeta"),
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
                Map.entry(
                        "textures/item/chicken_soup.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/item/chicken_soup.png"),
                Map.entry("textures/item/beef_stew.png", "items/bowls/beef_stew.png"),
                Map.entry("textures/item/orange.png", "items/food/orange.png"),
                Map.entry(
                        "textures/item/fruit_ice.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/item/fruit_ice.png"),
                Map.entry("textures/item/cheese.png", "items/food/cheese.png"),
                Map.entry(
                        "textures/item/mashed_potato.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/item/mashed_potato.png"),
                Map.entry("textures/item/ice_cream.png", "items/bowls/ice_cream.png"),
                Map.entry("textures/item/banana.png", "items/food/banana.png"),
                Map.entry("textures/item/worm.png", "items/food/worm_raw.png"),
                Map.entry("textures/item/cooked_worm.png", "items/food/worm_cooked.png"),
                Map.entry(
                        "textures/item/gelatinous_sphere/green.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/item/gelatinous_sphere/green.png"),
                Map.entry(
                        "textures/item/gelatinous_sphere/ochre.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/item/gelatinous_sphere/ochre.png"),
                Map.entry(
                        "textures/item/gelatinous_sphere/crimson.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/item/gelatinous_sphere/crimson.png"),
                Map.entry(
                        "textures/item/gelatinous_sphere/gray.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/item/gelatinous_sphere/gray.png"),
                Map.entry(
                        "textures/item/gelatinous_sphere/black.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/item/gelatinous_sphere/black.png"),
                Map.entry(
                        "textures/entity/slime/slime.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/entity/slime/slime.png"),
                Map.entry(
                        "textures/entity/slime/jelly.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/entity/slime/jelly.png"),
                Map.entry(
                        "textures/entity/slime/blob.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/entity/slime/blob.png"),
                Map.entry(
                        "textures/entity/slime/ooze.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/entity/slime/ooze.png"),
                Map.entry(
                        "textures/entity/slime/pudding.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/entity/slime/pudding.png"),
                Map.entry(
                        "textures/entity/slime/magmacube.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/entity/slime/magmacube.png"),
                Map.entry("textures/entity/ghoul.png", "mite/entity/ghoul.png+expand64x64"),
                Map.entry("textures/entity/shadow.png", "mite/entity/shadow.png+expand64x64"),
                Map.entry("textures/entity/wight.png", "mite/entity/wight.png+expand64x64"),
                Map.entry("textures/entity/ghoul_baby.png", "mite/entity/ghoul.png+baby_uv"),
                Map.entry("textures/entity/shadow_baby.png", "mite/entity/shadow.png+baby_uv"),
                Map.entry("textures/entity/wight_baby.png", "mite/entity/wight.png+baby_uv"),
                Map.entry(
                        "textures/entity/zombie/revenant.png",
                        "zombie/revenant.png"),
                Map.entry(
                        "textures/entity/zombie/revenant_baby.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/entity/zombie/revenant_baby.png"),
                Map.entry(
                        "textures/entity/spider/spider.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/entity/spider/spider.png"),
                Map.entry("textures/entity/blaze.png", "entity/blaze.png"),
                Map.entry("textures/entity/ghast/ghast.png", "entity/ghast/ghast.png"),
                Map.entry("textures/entity/ghast/ghast_shooting.png", "entity/ghast/ghast_shooting.png"),
                Map.entry("textures/entity/zombie_pigman.png", "entity/zombie_pigman.png"),
                Map.entry("textures/entity/zombie_pigman_baby.png", "mite/entity/zombie_pigman.png+baby_uv"),
                Map.entry("textures/entity/skeleton/longdead.png", "entity/skeleton/longdead.png"),
                Map.entry(
                        "textures/entity/skeleton/bone_lord.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/entity/skeleton/bone_lord.png"),
                Map.entry(
                        "textures/entity/skeleton/longdead_guardian.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/entity/skeleton/longdead_guardian.png"),
                Map.entry(
                        "textures/entity/spider/black_widow.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/entity/spider/black_widow.png"),
                Map.entry(
                        "textures/entity/spider/demon_spider.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/entity/spider/demon_spider.png"),
                Map.entry(
                        "textures/entity/spider/wood_spider.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/entity/spider/wood_spider.png"),
                Map.entry(
                        "textures/entity/spider/phase_spider.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/entity/spider/phase_spider.png"),
                Map.entry(
                        "textures/entity/spider/cave_spider.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/entity/spider/cave_spider.png"),
                Map.entry(
                        "textures/entity/creeper/infernal_creeper.png",
                        "entity/creeper/infernal_creeper.png"),
                Map.entry("textures/entity/fire_elemental.png", "entity/fire_elemental.png"),
                Map.entry(
                        "textures/entity/earth_elemental/clay/earth_elemental_clay.png",
                        "entity/earth_elemental/clay/earth_elemental_clay.png"),
                Map.entry(
                        "textures/entity/earth_elemental/clay/earth_elemental_clay_hardened.png",
                        "entity/earth_elemental/clay/earth_elemental_clay_hardened.png"),
                Map.entry(
                        "textures/entity/earth_elemental/earth_elemental_glow.png",
                        "entity/earth_elemental/earth_elemental_glow.png"),
                Map.entry(
                        "textures/entity/earth_elemental/earth_elemental_magma_glow.png",
                        "entity/earth_elemental/earth_elemental_magma_glow.png"),
                Map.entry(
                        "textures/entity/earth_elemental/end_stone/earth_elemental_end_stone.png",
                        "entity/earth_elemental/end_stone/earth_elemental_end_stone.png"),
                Map.entry(
                        "textures/entity/earth_elemental/end_stone/earth_elemental_end_stone_magma.png",
                        "entity/earth_elemental/end_stone/earth_elemental_end_stone_magma.png"),
                Map.entry(
                        "textures/entity/earth_elemental/netherrack/earth_elemental_netherrack.png",
                        "entity/earth_elemental/netherrack/earth_elemental_netherrack.png"),
                Map.entry(
                        "textures/entity/earth_elemental/netherrack/earth_elemental_netherrack_magma.png",
                        "entity/earth_elemental/netherrack/earth_elemental_netherrack_magma.png"),
                Map.entry(
                        "textures/entity/earth_elemental/obsidian/earth_elemental_obsidian.png",
                        "entity/earth_elemental/obsidian/earth_elemental_obsidian.png"),
                Map.entry(
                        "textures/entity/earth_elemental/obsidian/earth_elemental_obsidian_magma.png",
                        "entity/earth_elemental/obsidian/earth_elemental_obsidian_magma.png"),
                Map.entry(
                        "textures/entity/earth_elemental/stone/earth_elemental_stone.png",
                        "entity/earth_elemental/stone/earth_elemental_stone.png"),
                Map.entry(
                        "textures/entity/earth_elemental/stone/earth_elemental_stone_magma.png",
                        "entity/earth_elemental/stone/earth_elemental_stone_magma.png"),
                Map.entry(
                        "textures/item/earth_elemental_spawn_egg.png",
                        "item/spawn_egg/spawn_egg_earth_element_clay.png"),
                Map.entry(
                        "textures/item/clay_golem_spawn_egg.png",
                        "item/spawn_egg/spawn_egg_earth_element_clay.png"),
                Map.entry("textures/entity/silverfish/netherspawn.png", "entity/silverfish/netherspawn.png"),
                Map.entry("textures/entity/silverfish/copperspine.png", "entity/silverfish/copperspine.png"),
                Map.entry("textures/entity/silverfish/hoary.png", "entity/silverfish/hoary.png"),
                Map.entry(
                        "textures/entity/bat.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/entity/bat.png"),
                Map.entry(
                        "textures/entity/bat/vampire.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/entity/bat/vampire.png"),
                Map.entry(
                        "textures/entity/bat/nightwing.png",
                        "owner-provided:/Users/inxups/Downloads/nightwing.png"),
                Map.entry(
                        "textures/entity/hellhound/hellhound.png",
                        "owner-provided:/Users/inxups/Library/Containers/com.tencent.qq/Data/Downloads/textures.rar#textures/entity/hellhound/hellhound.png"),
                Map.entry("textures/entity/dire_wolf/neutral.png", "entity/dire_wolf/neutral.png"),
                Map.entry("textures/entity/dire_wolf/tame.png", "entity/dire_wolf/tame.png"),
                Map.entry("textures/entity/dire_wolf/angry.png", "entity/dire_wolf/angry.png"),
                Map.entry(
                        "textures/entity/cow/cow_temperate_sick.png",
                        "26.2/cow/cow_temperate.png+mite/cow/sick.png"),
                Map.entry(
                        "textures/entity/pig/pig_temperate_sick.png",
                        "26.2/pig/pig_temperate.png+mite/pig/sick.png"),
                Map.entry(
                        "textures/entity/chicken/chicken_temperate_sick.png",
                        "26.2/chicken/chicken_temperate.png+mite/chicken/sick.png"),
                Map.entry(
                        "textures/entity/sheep/sheep_sick.png",
                        "26.2/sheep/sheep.png+mite/sheep/sick.png"));
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
            if (fields[0].equals("derived")) {
                // Derived assets are tint composites of authorized bases, not byte copies.
                continue;
            }
            Path sourceRoot = switch (fields[0]) {
                case "resource-pack" -> reference.resolve("mite-resource-pack/assets/minecraft/textures");
                case "mite-src" -> reference.resolve("mite-src/assets/minecraft/textures");
                case "itf-reborn" -> Path.of(
                        "/Users/inxups/Downloads/ITF-Reborn-INFX/src/main/resources/assets/miteitfrb/textures");
                case "workbench-top" -> Path.of("/Users/inxups/Downloads/工作台顶");
                default -> throw new AssertionError("unknown source root " + fields[0]);
            };
            Path source = sourceRoot.resolve(fields[1]);
            if (!Files.isRegularFile(source)) {
                continue;
            }
            assertEquals(
                    -1L,
                    Files.mismatch(source, ASSETS.resolve(fields[2])),
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
