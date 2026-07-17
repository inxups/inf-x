# M1 MITE Resource Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the placeholder vanilla textures of the current M1 InfiniteX items and workbenches with twelve approved, byte-identical MITE resource-pack textures without overriding the `minecraft` namespace.

**Architecture:** Approved PNGs are copied from the ignored local reference into modern `assets/infx/textures/item` and `assets/infx/textures/block` paths. Existing model JSON files are redirected to those local resources, while permanent JUnit resource-contract tests pin the approved SHA-256 values, image dimensions, and model texture bindings. A repository notice records provenance without assigning a license that the source bundle does not name.

**Tech Stack:** Minecraft/NeoForge resource JSON, 16x16 PNG assets, Java 25, JUnit Jupiter 5.13.4, Gson, Gradle ModDev.

> **Execution override (2026-07-17):** The project owner requested direct replacement without tests. The implementation therefore removes the two resource-contract test classes introduced during execution and skips all remaining Gradle, data-generation, build, and client verification steps. The approved PNG and model mappings remain unchanged.

---

## File map

**Create**

- `src/test/java/com/pixulse/infx/client/M1TextureAssetsTest.java`: pins the twelve approved PNG hashes and dimensions.
- `src/test/java/com/pixulse/infx/client/M1ModelTexturesTest.java`: verifies every current M1 model points at the intended local texture.
- `src/main/resources/assets/infx/textures/item/flint_chip.png`
- `src/main/resources/assets/infx/textures/item/obsidian_shard.png`
- `src/main/resources/assets/infx/textures/item/emerald_shard.png`
- `src/main/resources/assets/infx/textures/item/sinew.png`
- `src/main/resources/assets/infx/textures/item/silver_nugget.png`
- `src/main/resources/assets/infx/textures/item/mithril_nugget.png`
- `src/main/resources/assets/infx/textures/item/adamantium_nugget.png`
- `src/main/resources/assets/infx/textures/item/flint_hatchet.png`
- `src/main/resources/assets/infx/textures/item/copper_pickaxe.png`
- `src/main/resources/assets/infx/textures/block/flint_workbench_top.png`
- `src/main/resources/assets/infx/textures/block/copper_workbench_front.png`
- `src/main/resources/assets/infx/textures/block/copper_workbench_side.png`
- `THIRD_PARTY_NOTICES.md`: documents the selected source assets and confirmed redistribution permission.

**Modify**

- `src/main/resources/assets/infx/models/item/flint_chip.json`
- `src/main/resources/assets/infx/models/item/obsidian_shard.json`
- `src/main/resources/assets/infx/models/item/emerald_shard.json`
- `src/main/resources/assets/infx/models/item/sinew.json`
- `src/main/resources/assets/infx/models/item/silver_nugget.json`
- `src/main/resources/assets/infx/models/item/mithril_nugget.json`
- `src/main/resources/assets/infx/models/item/adamantium_nugget.json`
- `src/main/resources/assets/infx/models/item/flint_hatchet.json`
- `src/main/resources/assets/infx/models/item/copper_pickaxe.json`
- `src/main/resources/assets/infx/models/block/flint_workbench.json`
- `src/main/resources/assets/infx/models/block/copper_workbench.json`

### Task 1: Pin and migrate the approved bitmap assets

**Files:**

- Create: `src/test/java/com/pixulse/infx/client/M1TextureAssetsTest.java`
- Create: the twelve PNG files listed in the file map

- [ ] **Step 1: Write the failing bitmap contract test**

Create `src/test/java/com/pixulse/infx/client/M1TextureAssetsTest.java` with:

```java
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
            Map.entry("/assets/infx/textures/item/flint_chip.png", "dce24ee5ded8e6bcf15d1b9adda143789cbca955e1c2055addd3c25f4c3a53cd"),
            Map.entry("/assets/infx/textures/item/obsidian_shard.png", "573ef14e08c19d30d776d9061553b44e38a8af8511d1806c46abe196a20c9442"),
            Map.entry("/assets/infx/textures/item/emerald_shard.png", "9519f79da479b8fcb95d71b4cd5592cf9f6298800f60b4b47f190e6184f26333"),
            Map.entry("/assets/infx/textures/item/sinew.png", "5cc87c07a946c29da3c3cdca634d380b110fbc7a53cbbc1018dcb2b948d6ab4c"),
            Map.entry("/assets/infx/textures/item/silver_nugget.png", "7cdb89c30134cadbe77dfe2e49ac3c012d06f8c6feebfa72a4584e9defbddf0c"),
            Map.entry("/assets/infx/textures/item/mithril_nugget.png", "5c2c8a9d131cf285b48cbae808ba0b568214f5258d3c3553e37b19e741acb3f2"),
            Map.entry("/assets/infx/textures/item/adamantium_nugget.png", "a5ee929c3f9a48326b205ebf731036b820c6b2ee19a46d3083af4209bb724341"),
            Map.entry("/assets/infx/textures/item/flint_hatchet.png", "f43c8e6f84dd4736e53292ecd19f7238c2d5a9fc07d8fec37638d685f1aff655"),
            Map.entry("/assets/infx/textures/item/copper_pickaxe.png", "6b485d9358b478dddd2bd96ac7d61f81a97424b418a51acc73064e10a9e4aebe"),
            Map.entry("/assets/infx/textures/block/flint_workbench_top.png", "efe8aa262dccb6ea5d8c694638bf82837682130a51c9a541b324cb8c850c1e8b"),
            Map.entry("/assets/infx/textures/block/copper_workbench_front.png", "084d1e5ea3ec094cabc2fb1a6018e62f8d23de7a78b95a2cbe5db682f42f4758"),
            Map.entry("/assets/infx/textures/block/copper_workbench_side.png", "f6c072571ebf94f8fb5e070c9cf238416da82def8cb2738c2c44bee18f8ea3d9"));

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
```

- [ ] **Step 2: Run the focused test and verify the red state**

Run:

```bash
bash gradlew test --tests com.pixulse.infx.client.M1TextureAssetsTest
```

Expected: FAIL with `Missing resource /assets/infx/textures/` because none of the twelve destinations exists yet.

- [ ] **Step 3: Copy the approved binaries under modern InfiniteX paths**

Run from `/private/tmp/inf-x-m1-mite-resources`:

```bash
SOURCE_ROOT='/Users/inxups/IdeaProjects/mc/inf-x/codex/reference/mite- resource-pack/assets/minecraft/textures'
mkdir -p src/main/resources/assets/infx/textures/item
mkdir -p src/main/resources/assets/infx/textures/block
cp "$SOURCE_ROOT/items/shards/flint.png" src/main/resources/assets/infx/textures/item/flint_chip.png
cp "$SOURCE_ROOT/items/shards/obsidian.png" src/main/resources/assets/infx/textures/item/obsidian_shard.png
cp "$SOURCE_ROOT/items/shards/emerald.png" src/main/resources/assets/infx/textures/item/emerald_shard.png
cp "$SOURCE_ROOT/items/sinew.png" src/main/resources/assets/infx/textures/item/sinew.png
cp "$SOURCE_ROOT/items/nuggets/silver.png" src/main/resources/assets/infx/textures/item/silver_nugget.png
cp "$SOURCE_ROOT/items/nuggets/mithril.png" src/main/resources/assets/infx/textures/item/mithril_nugget.png
cp "$SOURCE_ROOT/items/nuggets/adamantium.png" src/main/resources/assets/infx/textures/item/adamantium_nugget.png
cp "$SOURCE_ROOT/items/tools/flint_hatchet.png" src/main/resources/assets/infx/textures/item/flint_hatchet.png
cp "$SOURCE_ROOT/items/tools/copper_pickaxe.png" src/main/resources/assets/infx/textures/item/copper_pickaxe.png
cp "$SOURCE_ROOT/blocks/crafting_table/flint/top.png" src/main/resources/assets/infx/textures/block/flint_workbench_top.png
cp "$SOURCE_ROOT/blocks/crafting_table/copper/front.png" src/main/resources/assets/infx/textures/block/copper_workbench_front.png
cp "$SOURCE_ROOT/blocks/crafting_table/copper/side.png" src/main/resources/assets/infx/textures/block/copper_workbench_side.png
```

- [ ] **Step 4: Run the bitmap contract and verify the green state**

Run:

```bash
bash gradlew test --tests com.pixulse.infx.client.M1TextureAssetsTest
```

Expected: PASS and `BUILD SUCCESSFUL`.

- [ ] **Step 5: Independently compare each copied file with its approved source**

Run:

```bash
SOURCE_ROOT='/Users/inxups/IdeaProjects/mc/inf-x/codex/reference/mite- resource-pack/assets/minecraft/textures'
cmp "$SOURCE_ROOT/items/shards/flint.png" src/main/resources/assets/infx/textures/item/flint_chip.png
cmp "$SOURCE_ROOT/items/shards/obsidian.png" src/main/resources/assets/infx/textures/item/obsidian_shard.png
cmp "$SOURCE_ROOT/items/shards/emerald.png" src/main/resources/assets/infx/textures/item/emerald_shard.png
cmp "$SOURCE_ROOT/items/sinew.png" src/main/resources/assets/infx/textures/item/sinew.png
cmp "$SOURCE_ROOT/items/nuggets/silver.png" src/main/resources/assets/infx/textures/item/silver_nugget.png
cmp "$SOURCE_ROOT/items/nuggets/mithril.png" src/main/resources/assets/infx/textures/item/mithril_nugget.png
cmp "$SOURCE_ROOT/items/nuggets/adamantium.png" src/main/resources/assets/infx/textures/item/adamantium_nugget.png
cmp "$SOURCE_ROOT/items/tools/flint_hatchet.png" src/main/resources/assets/infx/textures/item/flint_hatchet.png
cmp "$SOURCE_ROOT/items/tools/copper_pickaxe.png" src/main/resources/assets/infx/textures/item/copper_pickaxe.png
cmp "$SOURCE_ROOT/blocks/crafting_table/flint/top.png" src/main/resources/assets/infx/textures/block/flint_workbench_top.png
cmp "$SOURCE_ROOT/blocks/crafting_table/copper/front.png" src/main/resources/assets/infx/textures/block/copper_workbench_front.png
cmp "$SOURCE_ROOT/blocks/crafting_table/copper/side.png" src/main/resources/assets/infx/textures/block/copper_workbench_side.png
```

Expected: every command exits 0 with no output.

- [ ] **Step 6: Commit the approved texture set and contract**

```bash
git add src/test/java/com/pixulse/infx/client/M1TextureAssetsTest.java src/main/resources/assets/infx/textures
git commit -m "feat: add approved M1 MITE textures"
```

### Task 2: Redirect M1 item and workbench models

**Files:**

- Create: `src/test/java/com/pixulse/infx/client/M1ModelTexturesTest.java`
- Modify: the nine item models and two block models listed in the file map

- [ ] **Step 1: Write the failing model-binding test**

Create `src/test/java/com/pixulse/infx/client/M1ModelTexturesTest.java` with:

```java
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
```

- [ ] **Step 2: Run the model test and verify the red state**

Run:

```bash
bash gradlew test --tests com.pixulse.infx.client.M1ModelTexturesTest
```

Expected: both test methods FAIL because the models still point at placeholder vanilla textures and the workbench face maps do not match the approved design.

- [ ] **Step 3: Redirect all nine item models**

Replace each file with the exact JSON shown:

`src/main/resources/assets/infx/models/item/flint_chip.json`

```json
{"parent": "minecraft:item/generated", "textures": {"layer0": "infx:item/flint_chip"}}
```

`src/main/resources/assets/infx/models/item/obsidian_shard.json`

```json
{"parent": "minecraft:item/generated", "textures": {"layer0": "infx:item/obsidian_shard"}}
```

`src/main/resources/assets/infx/models/item/emerald_shard.json`

```json
{"parent": "minecraft:item/generated", "textures": {"layer0": "infx:item/emerald_shard"}}
```

`src/main/resources/assets/infx/models/item/sinew.json`

```json
{"parent": "minecraft:item/generated", "textures": {"layer0": "infx:item/sinew"}}
```

`src/main/resources/assets/infx/models/item/silver_nugget.json`

```json
{"parent": "minecraft:item/generated", "textures": {"layer0": "infx:item/silver_nugget"}}
```

`src/main/resources/assets/infx/models/item/mithril_nugget.json`

```json
{"parent": "minecraft:item/generated", "textures": {"layer0": "infx:item/mithril_nugget"}}
```

`src/main/resources/assets/infx/models/item/adamantium_nugget.json`

```json
{"parent": "minecraft:item/generated", "textures": {"layer0": "infx:item/adamantium_nugget"}}
```

`src/main/resources/assets/infx/models/item/flint_hatchet.json`

```json
{"parent": "minecraft:item/handheld", "textures": {"layer0": "infx:item/flint_hatchet"}}
```

`src/main/resources/assets/infx/models/item/copper_pickaxe.json`

```json
{"parent": "minecraft:item/handheld", "textures": {"layer0": "infx:item/copper_pickaxe"}}
```

- [ ] **Step 4: Update the two workbench cube models**

Replace `src/main/resources/assets/infx/models/block/flint_workbench.json` with:

```json
{
  "parent": "minecraft:block/cube",
  "textures": {
    "down": "minecraft:block/oak_planks",
    "east": "minecraft:block/crafting_table_side",
    "north": "minecraft:block/crafting_table_front",
    "particle": "minecraft:block/crafting_table_front",
    "south": "minecraft:block/crafting_table_side",
    "up": "infx:block/flint_workbench_top",
    "west": "minecraft:block/crafting_table_front"
  }
}
```

Replace `src/main/resources/assets/infx/models/block/copper_workbench.json` with:

```json
{
  "parent": "minecraft:block/cube",
  "textures": {
    "down": "minecraft:block/copper_block",
    "east": "infx:block/copper_workbench_side",
    "north": "infx:block/copper_workbench_front",
    "particle": "minecraft:block/copper_block",
    "south": "infx:block/copper_workbench_front",
    "up": "minecraft:block/copper_block",
    "west": "infx:block/copper_workbench_side"
  }
}
```

- [ ] **Step 5: Run the focused model test and verify the green state**

Run:

```bash
bash gradlew test --tests com.pixulse.infx.client.M1ModelTexturesTest
```

Expected: both tests PASS and Gradle reports `BUILD SUCCESSFUL`.

- [ ] **Step 6: Run both resource-contract classes together**

Run:

```bash
bash gradlew test --tests 'com.pixulse.infx.client.M1*Test'
```

Expected: all three resource-contract tests PASS and Gradle reports `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit the model redirection and tests**

```bash
git add src/test/java/com/pixulse/infx/client/M1ModelTexturesTest.java src/main/resources/assets/infx/models/item src/main/resources/assets/infx/models/block
git commit -m "feat: apply MITE textures to M1 models"
```

### Task 3: Record texture provenance

**Files:**

- Create: `THIRD_PARTY_NOTICES.md`

- [ ] **Step 1: Add the factual provenance notice**

Create `THIRD_PARTY_NOTICES.md` with:

```markdown
# Third-Party Notices

## MITE 1.6.4 resource-pack textures

InfiniteX includes twelve texture files copied byte-for-byte from the local resource pack described by its metadata as “A Minecraft New Pack For 1.6.4-MITE Version:1.0.3 By Modded MITE Community.” Only their resource paths and filenames were adapted for modern Minecraft.

The included InfiniteX files are:

- `assets/infx/textures/item/flint_chip.png`
- `assets/infx/textures/item/obsidian_shard.png`
- `assets/infx/textures/item/emerald_shard.png`
- `assets/infx/textures/item/sinew.png`
- `assets/infx/textures/item/silver_nugget.png`
- `assets/infx/textures/item/mithril_nugget.png`
- `assets/infx/textures/item/adamantium_nugget.png`
- `assets/infx/textures/item/flint_hatchet.png`
- `assets/infx/textures/item/copper_pickaxe.png`
- `assets/infx/textures/block/flint_workbench_top.png`
- `assets/infx/textures/block/copper_workbench_front.png`
- `assets/infx/textures/block/copper_workbench_side.png`

The locally supplied source bundle did not contain a named license. The InfiniteX project owner confirmed on 2026-07-17 that these selected textures may be copied and distributed with this project. This notice applies only to the twelve files listed above and makes no statement about the remainder of the reference resource pack.
```

- [ ] **Step 2: Verify that the notice names all twelve committed destinations**

Run:

```bash
rg -c '^\- `assets/infx/textures/' THIRD_PARTY_NOTICES.md
```

Expected: `12`.

- [ ] **Step 3: Commit the provenance notice**

```bash
git add THIRD_PARTY_NOTICES.md
git commit -m "docs: record MITE texture provenance"
```

### Task 4: Run full resource and packaging verification

**Files:**

- Verify only; no planned file changes

- [ ] **Step 1: Run the complete unit-test suite**

Run:

```bash
bash gradlew test
```

Expected: all existing tests plus the three resource-contract tests PASS and Gradle reports `BUILD SUCCESSFUL`.

- [ ] **Step 2: Run client data generation**

Run:

```bash
bash gradlew runData
```

Expected: data generation exits 0 and Gradle reports `BUILD SUCCESSFUL` without changing generated data unexpectedly.

- [ ] **Step 3: Build the distributable mod**

Run:

```bash
bash gradlew build
```

Expected: compilation, tests, resource processing, and packaging exit 0 with `BUILD SUCCESSFUL`.

- [ ] **Step 4: Prove that no vanilla resource override entered the branch**

Run:

```bash
git diff --name-only master...HEAD | rg '^src/main/resources/assets/minecraft/'
```

Expected: no output and `rg` exit code 1, meaning the branch adds no `assets/minecraft` resource.

- [ ] **Step 5: Check patch integrity**

Run:

```bash
git diff --check master...HEAD
```

Expected: exit 0 with no output.

- [ ] **Step 6: Start the development client through resource loading**

Run in a PTY:

```bash
bash gradlew runClient
```

Expected: the client reaches the title screen after loading `mod_resources`. Stop it with Ctrl-C after the title screen appears.

- [ ] **Step 7: Check the client log for InfiniteX resource failures**

Run:

```bash
rg -n -i '(missing|unable to load|failed to load).*(infx|infinitex)|(infx|infinitex).*(missing|unable to load|failed to load)' run/logs/latest.log
```

Expected: no output and `rg` exit code 1.

- [ ] **Step 8: Confirm the implementation worktree is clean**

Run:

```bash
git status --short --branch
```

Expected: only `## codex/m1-mite-resources` with no modified or untracked files.
