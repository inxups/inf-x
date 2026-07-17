package com.pixulse.infx.client;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

class M1TextureAssetsTest {
    private static final Map<String, String> APPROVED_TEXTURES = Map.ofEntries(
            Map.entry(
                    "/assets/infx/textures/item/flint_chip.png",
                    "dce24ee5ded8e6bcf15d1b9adda143789cbca955e1c2055addd3c25f4c3a53cd"),
            Map.entry(
                    "/assets/infx/textures/item/obsidian_shard.png",
                    "573ef14e08c19d30d776d9061553b44e38a8af8511d1806c46abe196a20c9442"),
            Map.entry(
                    "/assets/infx/textures/item/emerald_shard.png",
                    "9519f79da479b8fcb95d71b4cd5592cf9f6298800f60b4b47f190e6184f26333"),
            Map.entry(
                    "/assets/infx/textures/item/sinew.png",
                    "5cc87c07a946c29da3c3cdca634d380b110fbc7a53cbbc1018dcb2b948d6ab4c"),
            Map.entry(
                    "/assets/infx/textures/item/silver_nugget.png",
                    "7cdb89c30134cadbe77dfe2e49ac3c012d06f8c6feebfa72a4584e9defbddf0c"),
            Map.entry(
                    "/assets/infx/textures/item/mithril_nugget.png",
                    "5c2c8a9d131cf285b48cbae808ba0b568214f5258d3c3553e37b19e741acb3f2"),
            Map.entry(
                    "/assets/infx/textures/item/adamantium_nugget.png",
                    "a5ee929c3f9a48326b205ebf731036b820c6b2ee19a46d3083af4209bb724341"),
            Map.entry(
                    "/assets/infx/textures/item/flint_hatchet.png",
                    "f43c8e6f84dd4736e53292ecd19f7238c2d5a9fc07d8fec37638d685f1aff655"),
            Map.entry(
                    "/assets/infx/textures/item/copper_pickaxe.png",
                    "6b485d9358b478dddd2bd96ac7d61f81a97424b418a51acc73064e10a9e4aebe"),
            Map.entry(
                    "/assets/infx/textures/block/flint_workbench_top.png",
                    "efe8aa262dccb6ea5d8c694638bf82837682130a51c9a541b324cb8c850c1e8b"),
            Map.entry(
                    "/assets/infx/textures/block/copper_workbench_front.png",
                    "084d1e5ea3ec094cabc2fb1a6018e62f8d23de7a78b95a2cbe5db682f42f4758"),
            Map.entry(
                    "/assets/infx/textures/block/copper_workbench_side.png",
                    "f6c072571ebf94f8fb5e070c9cf238416da82def8cb2738c2c44bee18f8ea3d9"));

    @Test
    void texturesMatchApprovedMiteSources() throws Exception {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

        for (var entry : APPROVED_TEXTURES.entrySet()) {
            String resourcePath = entry.getKey();
            byte[] bytes;
            try (InputStream input = getClass().getResourceAsStream(resourcePath)) {
                assertNotNull(input, () -> "Missing resource " + resourcePath);
                bytes = input.readAllBytes();
            }

            String actualHash = HexFormat.of().formatHex(sha256.digest(bytes));
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            assertNotNull(image, () -> "Not a readable PNG " + resourcePath);
            assertAll(
                    resourcePath,
                    () -> assertEquals(entry.getValue(), actualHash, "Unexpected source bytes"),
                    () -> assertEquals(16, image.getWidth(), "Unexpected width"),
                    () -> assertEquals(16, image.getHeight(), "Unexpected height"));
        }
    }
}
