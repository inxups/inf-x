package com.pixulse.infx.client;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import java.util.Set;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class R196FulltextAssetProvenanceTest {
    private static final Path ROOT = findProjectRoot();
    private static final Path ASSETS = ROOT.resolve("src/main/resources/assets/infx");
    private static final Path MANIFEST = ASSETS.resolve("mite_fulltext_manifest.tsv");

    @Test
    void allFiftyFourAuthorizedAssetsAreUniqueReadableAndHashPinned() throws Exception {
        List<String> lines = Files.readAllLines(MANIFEST, UTF_8);
        assertEquals("source_root\tsource\tdestination\tsha256", lines.getFirst());
        assertEquals(55, lines.size());
        Set<String> destinations = new HashSet<>();
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        for (String line : lines.subList(1, lines.size())) {
            String[] fields = line.split("\t", -1);
            assertEquals(4, fields.length, line);
            assertTrue(destinations.add(fields[2]), fields[2]);
            byte[] bytes = Files.readAllBytes(ASSETS.resolve(fields[2]));
            assertEquals(fields[3], HexFormat.of().formatHex(sha256.digest(bytes)), fields[2]);
            if (fields[2].endsWith(".png")) {
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
                assertNotNull(image, fields[2]);
                assertTrue(image.getWidth() > 0 && image.getHeight() > 0, fields[2]);
            } else if (fields[2].endsWith(".ogg")) {
                assertTrue(bytes.length > 4);
                assertEquals("OggS", new String(bytes, 0, 4, java.nio.charset.StandardCharsets.US_ASCII));
            }
        }
    }

    @Test
    void optionalAuthorizedSourceStillMatchesEveryCommittedAsset() throws Exception {
        Path source = Path.of(
                "/Users/inxups/IdeaProjects/mc/inf-x/codex/reference/mite- resource-pack/assets/minecraft");
        if (!Files.isDirectory(source)) return;
        for (String line : Files.readAllLines(MANIFEST, UTF_8).subList(1, 55)) {
            String[] fields = line.split("\t", -1);
            assertEquals(-1L, Files.mismatch(source.resolve(fields[1]), ASSETS.resolve(fields[2])), fields[2]);
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
