package com.pixulse.infx.client;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pixulse.infx.recipe.BenchTier;
import com.pixulse.infx.item.Catalog;
import com.pixulse.infx.item.EquipmentKey;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.item.material.Quality;
import com.pixulse.infx.registry.InfXBlocks;
import com.pixulse.infx.registry.InfXEnchantments;
import com.pixulse.infx.registry.InfXEntityTypes;
import com.pixulse.infx.registry.InfXItems;
import com.pixulse.infx.registry.InfXJukeboxSongs;
import com.pixulse.infx.world.Underworld;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.regex.Pattern;
import net.minecraft.world.level.levelgen.GenerationStep;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("generated-resources")
class GeneratedResourceTest {
    private static final Path ROOT = findProjectRoot();
    private static final Path GENERATED = ROOT.resolve("src/generated/resources");
    private static final Path STATIC = ROOT.resolve("src/main/resources");
    private static final Pattern FORMAT_SPECIFIER = Pattern.compile("%(?:\\d+\\$)?[A-Za-z]");
    private static final double[] MITE_UNDERWORLD_PROFILE = {
        -638.0,
        -269.1085232884469,
        -81.20526927275851,
        -11.965946199367803,
        -0.5473259801441662,
        1.4780178344413184,
        1.8649444588087118,
        0.18453671892660584,
        -1.7004342714592278,
        -1.7004342714592284,
        0.18453671892660437,
        1.8649444588087107,
        1.4780178344413193,
        -10.547325980144166,
        -81.9659461993678,
        -271.2052692727585,
        -639.1085232884469
    };

    @Test
    void enchantmentSourcesStrictlyReplaceVanillaWithR196() throws Exception {
        Set<String> expected = InfXEnchantments.ALL.stream()
                .map(key -> key.identifier().toString())
                .collect(Collectors.toSet());
        for (String tag : List.of(
                "in_enchanting_table",
                "on_mob_spawn_equipment",
                "on_traded_equipment",
                "on_random_loot",
                "tradeable",
                "trades/desert_common",
                "trades/jungle_common",
                "trades/plains_common",
                "trades/savanna_common",
                "trades/snow_common",
                "trades/swamp_common",
                "trades/taiga_common")) {
            JsonObject source = json(GENERATED.resolve("data/minecraft/tags/enchantment/" + tag + ".json"));
            Set<String> values = source.getAsJsonArray("values").asList().stream()
                    .map(JsonElement::getAsString)
                    .collect(Collectors.toSet());
            assertAll(
                    tag,
                    () -> assertTrue(source.has("replace") && source.get("replace").getAsBoolean()),
                    () -> assertEquals(expected, values),
                    () -> assertEquals(42, values.size()));
        }
    }

    @Test
    void miteChineseNamesForMilkAndPoisonDeath() throws Exception {
        JsonObject zh = json(GENERATED.resolve("assets/infx/lang/zh_cn.json"));
        assertEquals("铁奶桶", zh.get("item.infx.iron_milk_bucket").getAsString());
        assertEquals("%s 毒发身亡", zh.get("death.infx.poison").getAsString());
    }

    @Test
    void portalParticlesReuseVanillaPortalSprites() throws Exception {
        List<String> expected = List.of(
                "minecraft:generic_0",
                "minecraft:generic_1",
                "minecraft:generic_2",
                "minecraft:generic_3",
                "minecraft:generic_4",
                "minecraft:generic_5",
                "minecraft:generic_6",
                "minecraft:generic_7");
        for (String particle : List.of("underworld_portal", "nether_portal", "runegate")) {
            JsonArray textures = json(GENERATED.resolve("assets/infx/particles/" + particle + ".json"))
                    .getAsJsonArray("textures");
            assertEquals(expected, textures.asList().stream().map(JsonElement::getAsString).toList());
        }
    }

    /** The vanilla-id overrides must carry MITE weights, level caps and self-exclusivity. */
    @Test
    void vanillaEnchantmentOverridesUseMiteDefinitions() throws Exception {
        Map<String, int[]> expected = Map.ofEntries(
                Map.entry("fire_protection", new int[]{25, 4}),
                Map.entry("feather_falling", new int[]{25, 4}),
                Map.entry("blast_protection", new int[]{25, 4}),
                Map.entry("projectile_protection", new int[]{25, 4}),
                Map.entry("respiration", new int[]{5, 3}),
                Map.entry("aqua_affinity", new int[]{5, 1}),
                Map.entry("thorns", new int[]{5, 3}),
                Map.entry("smite", new int[]{25, 5}),
                Map.entry("bane_of_arthropods", new int[]{25, 5}),
                Map.entry("knockback", new int[]{25, 2}),
                Map.entry("fire_aspect", new int[]{5, 2}),
                Map.entry("looting", new int[]{25, 3}),
                Map.entry("efficiency", new int[]{100, 5}),
                Map.entry("silk_touch", new int[]{5, 1}),
                Map.entry("power", new int[]{100, 5}),
                Map.entry("punch", new int[]{25, 2}),
                Map.entry("flame", new int[]{5, 1}));
        for (Map.Entry<String, int[]> entry : expected.entrySet()) {
            JsonObject definition = json(GENERATED.resolve(
                    "data/minecraft/enchantment/" + entry.getKey() + ".json"));
            assertAll(
                    entry.getKey(),
                    () -> assertEquals(entry.getValue()[0], definition.get("weight").getAsInt()),
                    () -> assertEquals(entry.getValue()[1], definition.get("max_level").getAsInt()),
                    () -> assertEquals(8, definition.get("anvil_cost").getAsInt()));
        }
        JsonObject silkTouch = json(GENERATED.resolve("data/minecraft/enchantment/silk_touch.json"));
        assertTrue(silkTouch.getAsJsonArray("exclusive_set").asList().stream()
                        .map(JsonElement::getAsString)
                        .anyMatch("infx:fortune"::equals),
                "silk touch stays exclusive with MITE fortune");
        JsonObject fortune = json(GENERATED.resolve("data/infx/enchantment/fortune.json"));
        assertTrue(fortune.getAsJsonArray("exclusive_set").asList().stream()
                        .map(JsonElement::getAsString)
                        .anyMatch("minecraft:silk_touch"::equals),
                "fortune stays exclusive with silk touch");

        Set<String> protectionSet = json(GENERATED.resolve("data/infx/enchantment/protection.json"))
                .getAsJsonArray("exclusive_set").asList().stream()
                .map(JsonElement::getAsString)
                .collect(Collectors.toSet());
        Set<String> typedProtectionSet = Set.of(
                "infx:protection",
                "minecraft:fire_protection",
                "minecraft:blast_protection",
                "minecraft:projectile_protection");
        assertEquals(typedProtectionSet, protectionSet, "MITE protection enchantments must be mutually exclusive");
        for (String path : List.of("fire_protection", "blast_protection", "projectile_protection")) {
            Set<String> exclusiveSet = json(GENERATED.resolve(
                            "data/minecraft/enchantment/" + path + ".json"))
                    .getAsJsonArray("exclusive_set").asList().stream()
                    .map(JsonElement::getAsString)
                    .collect(Collectors.toSet());
            assertEquals(typedProtectionSet, exclusiveSet, path + " must use the shared MITE protection set");
        }
        String featherFallingSet = json(GENERATED.resolve(
                        "data/minecraft/enchantment/feather_falling.json"))
                .get("exclusive_set")
                .getAsString();
        assertEquals("minecraft:feather_falling", featherFallingSet,
                "feather falling must remain compatible with the other protection enchantments");
    }

    @Test
    void sharpnessTargetsVanillaAndInfiniteXSwords() throws Exception {
        JsonObject tag = json(GENERATED.resolve("data/infx/tags/item/enchantable/infx_sharpness.json"));
        Set<String> values = tag.getAsJsonArray("values").asList().stream()
                .map(JsonElement::getAsString)
                .collect(Collectors.toSet());
        Set<String> swords = EquipmentKey.all().stream()
                .filter(key -> key.type() == EquipmentType.SWORD)
                .map(key -> "infx:" + key.path())
                .collect(Collectors.toSet());
        assertTrue(values.contains("#minecraft:swords"), "sharpness must target vanilla swords");
        assertTrue(values.containsAll(swords), "sharpness must target InfiniteX swords");
    }

    @Test
    void slaughterUsesTheDamageExclusiveSetAndExcludesSwords() throws Exception {
        JsonObject slaughter = json(GENERATED.resolve("data/infx/enchantment/slaughter.json"));
        Set<String> exclusiveSet = slaughter.getAsJsonArray("exclusive_set").asList().stream()
                .map(JsonElement::getAsString)
                .collect(Collectors.toSet());
        assertTrue(exclusiveSet.contains("infx:slaughter"), "slaughter stays self-exclusive");
        assertTrue(exclusiveSet.contains("minecraft:sharpness"), "slaughter excludes sharpness");
        assertTrue(exclusiveSet.contains("minecraft:smite"), "slaughter excludes smite");
        assertTrue(
                exclusiveSet.contains("minecraft:bane_of_arthropods"),
                "slaughter excludes bane of arthropods");

        JsonObject tag = json(GENERATED.resolve("data/infx/tags/item/enchantable/infx_slaughter.json"));
        Set<String> supported = tag.getAsJsonArray("values").asList().stream()
                .map(JsonElement::getAsString)
                .collect(Collectors.toSet());
        Set<String> swords = EquipmentKey.all().stream()
                .filter(key -> key.type() == EquipmentType.SWORD)
                .map(key -> "infx:" + key.path())
                .collect(Collectors.toSet());
        assertTrue(swords.stream().noneMatch(supported::contains), "slaughter must exclude swords");
        assertTrue(
                EquipmentKey.all().stream()
                        .filter(key -> key.type() == EquipmentType.BATTLE_AXE
                                || key.type() == EquipmentType.SCYTHE)
                        .map(key -> "infx:" + key.path())
                        .allMatch(supported::contains),
                "slaughter must retain battle axe and scythe targets");
    }

    @Test
    void materialShearsAreSilkTouchEnchantable() throws Exception {
        JsonObject tag = json(GENERATED.resolve("data/infx/tags/item/enchantable/infx_silk_touch.json"));
        Set<String> values = tag.getAsJsonArray("values").asList().stream()
                .map(JsonElement::getAsString)
                .collect(Collectors.toSet());
        Set<String> materialShears = EquipmentKey.all().stream()
                .filter(key -> key.type() == EquipmentType.SHEARS)
                .map(key -> "infx:" + key.path())
                .collect(Collectors.toSet());
        assertTrue(values.containsAll(materialShears), "all material shears must support silk touch");
    }

    @Test
    void r196SpawnTablesUseCorrectPoolsAndSources() throws Exception {
        JsonObject modifier = json(GENERATED.resolve(
                "data/infx/neoforge/biome_modifier/infx_spawns.json"));
        JsonObject infestedStone = json(GENERATED.resolve(
                "data/infx/worldgen/configured_feature/infx_infested_stone.json"));
        JsonObject infestedNetherrack = json(GENERATED.resolve(
                "data/infx/worldgen/configured_feature/infx_infested_netherrack.json"));
        JsonObject underworld = json(GENERATED.resolve("data/infx/worldgen/biome/underworld.json"));
        JsonObject underworldSpawners = underworld.getAsJsonObject("spawners");
        Map<String, int[]> expectedUnderworldMonsters = Map.ofEntries(
                Map.entry("infx:infx_spider", new int[] {80, 1, 2}),
                Map.entry("infx:infx_creeper", new int[] {100, 1, 2}),
                Map.entry("infx:infx_enderman", new int[] {10, 1, 1}),
                Map.entry("infx:wight", new int[] {10, 1, 1}),
                Map.entry("infx:invisible_stalker", new int[] {10, 1, 1}),
                Map.entry("infx:demon_spider", new int[] {10, 1, 1}),
                Map.entry("infx:hellhound", new int[] {10, 1, 2}),
                Map.entry("infx:shadow", new int[] {10, 1, 1}),
                Map.entry("infx:earth_elemental", new int[] {10, 1, 1}),
                Map.entry("infx:jelly", new int[] {30, 1, 4}),
                Map.entry("infx:blob", new int[] {30, 1, 4}),
                Map.entry("infx:ooze", new int[] {20, 1, 4}),
                Map.entry("infx:pudding", new int[] {30, 1, 4}),
                Map.entry("infx:clay_golem", new int[] {50, 1, 1}),
                Map.entry("infx:phase_spider", new int[] {5, 1, 1}),
                Map.entry("infx:infx_cave_spider", new int[] {40, 1, 2}),
                Map.entry("infx:longdead", new int[] {40, 1, 2}),
                Map.entry("infx:ancient_bone_lord", new int[] {5, 1, 1}));
        Map<String, int[]> actualUnderworldMonsters = new java.util.HashMap<>();
        for (JsonElement element : underworldSpawners.getAsJsonArray("monster")) {
            JsonObject entry = element.getAsJsonObject();
            actualUnderworldMonsters.put(entry.get("type").getAsString(), new int[] {
                entry.get("weight").getAsInt(),
                spawnCount(entry, "minCount", "min_count"),
                spawnCount(entry, "maxCount", "max_count")
            });
        }

        assertAll(
                "INFX spawn tables",
                () -> assertEquals("infx:infx_spawns", modifier.get("type").getAsString()),
                () -> assertTrue(infestedStone.toString().contains("minecraft:infested_stone")),
                () -> assertTrue(infestedNetherrack.toString().contains("infx:infested_netherrack")),
                () -> assertTrue(Files.isRegularFile(
                        GENERATED.resolve("assets/infx/blockstates/infested_netherrack.json"))),
                () -> assertEquals(expectedUnderworldMonsters.keySet(), actualUnderworldMonsters.keySet()),
                () -> expectedUnderworldMonsters.forEach((type, expected) -> {
                    int[] actual = actualUnderworldMonsters.get(type);
                    assertEquals(expected[0], actual[0], type + " weight");
                    assertEquals(expected[1], actual[1], type + " minimum");
                    assertEquals(expected[2], actual[2], type + " maximum");
                }),
                () -> assertTrue(
                        underworldSpawners.entrySet().stream()
                                .filter(entry -> !entry.getKey().equals("monster"))
                                .allMatch(entry -> entry.getValue().getAsJsonArray().isEmpty()),
                        "Underworld non-monster spawn tables must remain empty"));

        JsonObject lush = json(GENERATED.resolve("data/infx/worldgen/biome/underworld_lush.json"));
        JsonObject lushSpawners = lush.getAsJsonObject("spawners");
        Map<String, int[]> expectedLushMonsters = new java.util.HashMap<>(expectedUnderworldMonsters);
        expectedLushMonsters.put("infx:wood_spider", new int[] {20, 1, 1});
        Map<String, int[]> actualLushMonsters = new java.util.HashMap<>();
        for (JsonElement element : lushSpawners.getAsJsonArray("monster")) {
            JsonObject entry = element.getAsJsonObject();
            actualLushMonsters.put(entry.get("type").getAsString(), new int[] {
                entry.get("weight").getAsInt(),
                spawnCount(entry, "minCount", "min_count"),
                spawnCount(entry, "maxCount", "max_count")
            });
        }
        assertAll(
                "INFX lush spawn tables",
                () -> assertEquals(expectedLushMonsters.keySet(), actualLushMonsters.keySet()),
                () -> expectedLushMonsters.forEach((type, expected) -> {
                    int[] actual = actualLushMonsters.get(type);
                    assertEquals(expected[0], actual[0], type + " weight");
                    assertEquals(expected[1], actual[1], type + " minimum");
                    assertEquals(expected[2], actual[2], type + " maximum");
                }),
                () -> assertTrue(
                        lushSpawners.entrySet().stream()
                                .filter(entry -> !entry.getKey().equals("monster"))
                                .allMatch(entry -> entry.getValue().getAsJsonArray().isEmpty()),
                        "Lush underworld non-monster spawn tables must remain empty"));
    }

    @Test
    void r196EndermanPearlLootUsesLooting() throws Exception {
        JsonObject table = json(GENERATED.resolve("data/infx/loot_table/entities/infx_enderman.json"));
        JsonObject entry = table.getAsJsonArray("pools")
                .get(0)
                .getAsJsonObject()
                .getAsJsonArray("entries")
                .get(0)
                .getAsJsonObject();
        assertTrue(
                entry.getAsJsonArray("functions").asList().stream()
                        .map(JsonElement::getAsJsonObject)
                        .anyMatch(function -> function.get("function").getAsString().equals("minecraft:enchanted_count_increase")
                                && function.get("enchantment").getAsString().equals("minecraft:looting")),
                "INFX ender pearl drops must retain MITE's Looting count increase");
    }

    @Test
    void infernalCreeperKeepsItsInheritedSkeletonDiscLoot() throws Exception {
        JsonObject table = json(GENERATED.resolve("data/infx/loot_table/entities/infernal_creeper.json"));
        JsonObject pool = table.getAsJsonArray("pools").get(0).getAsJsonObject();
        JsonObject entry = pool.getAsJsonArray("entries").get(0).getAsJsonObject();
        assertAll(
                () -> assertEquals("minecraft:tag", entry.get("type").getAsString()),
                () -> assertEquals("minecraft:creeper_drop_music_discs", entry.get("name").getAsString()),
                () -> assertTrue(pool.toString().contains("minecraft:skeletons")));
    }

    @Test
    void netherPortalModelsUseDestinationTintTemplates() throws Exception {
        for (String orientation : List.of("ns", "ew")) {
            JsonObject model = json(GENERATED.resolve("assets/infx/models/block/nether_portal_" + orientation + ".json"));
            JsonObject template = json(STATIC.resolve(
                    "assets/infx/models/block/template_red_nether_portal_" + orientation + ".json"));
            JsonObject textures = model.getAsJsonObject("textures");

            assertAll(
                    () -> assertEquals(
                            "infx:block/template_red_nether_portal_" + orientation,
                            model.get("parent").getAsString()),
                    () -> assertEquals("infx:block/nether_portal", textures.get("portal").getAsString()),
                    () -> assertEquals("infx:block/nether_portal", textures.get("particle").getAsString()));
            template.getAsJsonArray("elements")
                    .get(0)
                    .getAsJsonObject()
                    .getAsJsonObject("faces")
                    .entrySet()
                    .forEach(face -> assertEquals(
                            0,
                            face.getValue().getAsJsonObject().get("tintindex").getAsInt(),
                            orientation + " portal face " + face.getKey()
                                    + " must use the destination tint source"));
        }
    }

    @Test
    void emeraldEnchantingTableModelUsesImportedMiteTextures() throws Exception {
        JsonObject model = json(GENERATED.resolve("assets/infx/models/block/emerald_enchanting_table.json"));
        JsonObject textures = model.getAsJsonObject("textures");
        JsonObject blockState = json(GENERATED.resolve("assets/infx/blockstates/emerald_enchanting_table.json"));
        assertAll(
                () -> assertEquals("minecraft:block/enchanting_table", model.get("parent").getAsString()),
                () -> assertEquals(
                        "infx:block/emerald_enchanting_table_side",
                        textures.get("particle").getAsString()),
                () -> assertEquals(
                        "infx:block/emerald_enchanting_table_side",
                        textures.get("side").getAsString()),
                () -> assertEquals(
                        "infx:block/emerald_enchanting_table_top",
                        textures.get("top").getAsString()),
                () -> assertEquals(
                        "infx:block/emerald_enchanting_table",
                        blockState.getAsJsonObject("variants")
                                .getAsJsonObject("")
                                .get("model")
                                .getAsString()));
    }

    @Test
    void everyCatalogItemHasDefinitionModelOrVanillaReferenceAndTwoTranslations() throws Exception {
        JsonObject english = json(GENERATED.resolve("assets/infx/lang/en_us.json"));
        JsonObject chinese = json(GENERATED.resolve("assets/infx/lang/zh_cn.json"));
        for (Catalog.Entry entry : InfXItems.catalog().entries()) {
            Path definition = GENERATED.resolve("assets/infx/items/" + entry.path() + ".json");
            Path model = GENERATED.resolve("assets/infx/models/item/" + entry.path() + ".json");
            boolean reusesVanillaLeatherModel = entry instanceof Catalog.EquipmentEntry equipment
                    && equipment.key().material() == InfxMaterial.LEATHER
                    && equipment.key().type().armorForm() == EquipmentType.ArmorForm.PLATE;
            assertAll(
                    entry.path(),
                    () -> assertTrue(Files.isRegularFile(definition), "missing item definition"),
                    () -> assertTrue(
                            reusesVanillaLeatherModel || Files.isRegularFile(model),
                            "missing base model or vanilla model reference"),
                    () -> assertTrue(english.has("item.infx." + entry.path()), "missing en_us"),
                    () -> assertTrue(chinese.has("item.infx." + entry.path()), "missing zh_cn"));
        }
    }

    @Test
    void generatedLanguagesCoverRegisteredContentAndKeepFormatsInSync() throws Exception {
        JsonObject english = json(GENERATED.resolve("assets/infx/lang/en_us.json"));
        JsonObject chinese = json(GENERATED.resolve("assets/infx/lang/zh_cn.json"));

        assertEquals(english.keySet(), chinese.keySet(), "language files must contain the same keys");
        english.entrySet().forEach(entry -> {
            String key = entry.getKey();
            String englishValue = entry.getValue().getAsString();
            String chineseValue = chinese.get(key).getAsString();
            assertAll(
                    key,
                    () -> assertFalse(englishValue.isBlank(), "en_us translation must not be blank"),
                    () -> assertFalse(chineseValue.isBlank(), "zh_cn translation must not be blank"),
                    () -> assertEquals(
                            formatSpecifiers(englishValue),
                            formatSpecifiers(chineseValue),
                            "format specifiers must match"));
        });

        Set<String> blockPaths = InfXBlocks.BLOCKS.getEntries().stream()
                .map(block -> block.getId().getPath())
                .collect(Collectors.toSet());
        InfXBlocks.BLOCKS.getEntries().forEach(block ->
                assertLanguageKey(english, chinese, "block.infx." + block.getId().getPath()));
        InfXItems.ITEMS.getEntries().forEach(item -> {
            String path = item.getId().getPath();
            String prefix = blockPaths.contains(path) ? "block.infx." : "item.infx.";
            assertLanguageKey(english, chinese, prefix + path);
        });
        InfXEntityTypes.names().forEach(entity ->
                assertLanguageKey(english, chinese, "entity.infx." + entity.path()));
        Stream.concat(InfXEnchantments.INFX.stream(), Stream.of(InfXEnchantments.CLUMSINESS))
                .forEach(enchantment -> assertLanguageKey(
                        english, chinese, "enchantment.infx." + enchantment.identifier().getPath()));
        for (String effect : List.of("malnutrition", "witch_curse", "insulin_resistance", "paralysis")) {
            assertLanguageKey(english, chinese, "effect.infx." + effect);
        }
        for (InfxMaterial material : InfxMaterial.values()) {
            assertLanguageKey(english, chinese, "material.infx." + material.path());
        }
        for (Quality quality : Quality.values()) {
            assertLanguageKey(english, chinese, "quality.infx." + quality.getSerializedName());
        }
        Stream.of(
                        InfXJukeboxSongs.UNDERWORLD,
                        InfXJukeboxSongs.DESCENT,
                        InfXJukeboxSongs.WANDERER,
                        InfXJukeboxSongs.LEGENDS)
                .forEach(song -> assertLanguageKey(
                        english, chinese, "jukebox_song.infx." + song.identifier().getPath()));
        InfXBlocks.FURNACES.forEach(furnace ->
                assertLanguageKey(english, chinese, "container.infx." + furnace.getId().getPath()));
        InfXBlocks.WORKBENCHES.forEach(workbench ->
                assertLanguageKey(english, chinese, "container.infx." + workbench.getId().getPath()));
        InfXBlocks.METAL_SAFES.forEach(safe ->
                assertLanguageKey(english, chinese, "container.infx." + safe.getId().getPath()));
        assertLanguageKey(english, chinese, "container.infx.metal_anvil");
    }

    @Test
    void specialBlockItemsUseTheirBlockTranslations() {
        assertEquals("block.infx.mithril_rune_stone", InfXItems.MITHRIL_RUNE_STONE.get().getDescriptionId());
        assertEquals(
                "block.infx.adamantium_rune_stone", InfXItems.ADAMANTIUM_RUNE_STONE.get().getDescriptionId());
        InfXItems.METAL_ANVILS.forEach(anvil -> assertEquals(
                "block.infx." + anvil.getId().getPath(), anvil.get().getDescriptionId()));
    }

    @Test
    void creativeTabsHaveEnglishAndChineseTranslations() throws Exception {
        JsonObject english = json(GENERATED.resolve("assets/infx/lang/en_us.json"));
        JsonObject chinese = json(GENERATED.resolve("assets/infx/lang/zh_cn.json"));
        Map<String, String> englishNames = Map.of(
                "itemGroup.infx", "InfiniteX: Blocks",
                "itemGroup.infx.ingredients", "InfiniteX: Ingredients",
                "itemGroup.infx.food_and_consumables", "InfiniteX: Food & Consumables",
                "itemGroup.infx.tools_and_utilities", "InfiniteX: Tools & Utilities",
                "itemGroup.infx.combat_and_equipment", "InfiniteX: Combat & Equipment");
        Map<String, String> chineseNames = Map.of(
                "itemGroup.infx", "InfiniteX：方块",
                "itemGroup.infx.ingredients", "InfiniteX：原料",
                "itemGroup.infx.food_and_consumables", "InfiniteX：食物与消耗品",
                "itemGroup.infx.tools_and_utilities", "InfiniteX：工具与实用品",
                "itemGroup.infx.combat_and_equipment", "InfiniteX：战斗与装备");

        englishNames.forEach((key, name) -> assertAll(
                key,
                () -> assertEquals(name, english.get(key).getAsString()),
                () -> assertEquals(chineseNames.get(key), chinese.get(key).getAsString())));
    }

    @Test
    void everyWorkbenchHasClientDataLootRecipeAndTranslations() throws Exception {
        JsonObject english = json(GENERATED.resolve("assets/infx/lang/en_us.json"));
        JsonObject chinese = json(GENERATED.resolve("assets/infx/lang/zh_cn.json"));
        for (var workbench : InfXBlocks.WORKBENCHES) {
            String path = workbench.getId().getPath();
            assertAll(
                    path,
                    () -> assertTrue(resourceExists("assets/infx/blockstates/" + path + ".json")),
                    () -> assertTrue(resourceExists("assets/infx/items/" + path + ".json")),
                    () -> assertTrue(resourceExists("assets/infx/models/block/" + path + ".json")),
                    () -> assertTrue(Files.isRegularFile(GENERATED.resolve("data/infx/loot_table/blocks/" + path + ".json"))),
                    () -> assertTrue(Files.isRegularFile(GENERATED.resolve("data/infx/recipe/" + path + ".json"))),
                    () -> assertTrue(english.has("block.infx." + path)),
                    () -> assertTrue(english.has("container.infx." + path)),
                    () -> assertTrue(chinese.has("block.infx." + path)),
                    () -> assertTrue(chinese.has("container.infx." + path)),
                    () -> assertEquals(
                            english.get("block.infx." + path).getAsString(),
                            english.get("container.infx." + path).getAsString(),
                            "English block and menu names must match"),
                    () -> assertEquals(
                            chinese.get("block.infx." + path).getAsString(),
                            chinese.get("container.infx." + path).getAsString(),
                            "Chinese block and menu names must match"));
        }
    }

    @Test
    void copperToIronProgressionDataIsComplete() {
        for (String recipe : List.of("flint_shovel", "cobblestone_furnace", "iron_pickaxe")) {
            assertTrue(
                    Files.isRegularFile(GENERATED.resolve("data/infx/recipe/" + recipe + ".json")),
                    recipe);
        }
        for (String advancement : List.of(
                "flint_kit", "first_furnace", "copper_workbench", "iron_age")) {
            assertTrue(
                    Files.isRegularFile(
                            GENERATED.resolve("data/infx/advancement/progression/" + advancement + ".json")),
                    advancement);
        }
        for (String disabled : List.of(
                "iron_ingot_from_blasting_deepslate_iron_ore",
                "iron_ingot_from_blasting_iron_ore",
                "iron_ingot_from_blasting_raw_iron",
                "iron_pickaxe")) {
            assertTrue(
                    Files.isRegularFile(GENERATED.resolve("data/minecraft/recipe/" + disabled + ".json")),
                    disabled);
        }
    }

    @Test
    void earlyCoreToolRecipesKeepR196DifficultiesAndBenchTiers() throws Exception {
        Map<String, Float> difficulties = Map.ofEntries(
                Map.entry("flint_axe", 375.0F),
                Map.entry("copper_pickaxe", 1250.0F),
                Map.entry("copper_shovel", 450.0F),
                Map.entry("copper_axe", 1250.0F),
                Map.entry("copper_hoe", 850.0F),
                Map.entry("copper_sword", 825.0F),
                Map.entry("iron_pickaxe", 2450.0F),
                Map.entry("iron_shovel", 850.0F),
                Map.entry("iron_axe", 2450.0F),
                Map.entry("iron_hoe", 1650.0F),
                Map.entry("iron_sword", 1625.0F));
        for (var entry : difficulties.entrySet()) {
            String recipeName = entry.getKey();
            JsonObject recipe = json(GENERATED.resolve("data/infx/recipe/" + recipeName + ".json"));
            String requiredBench = recipeName.substring(0, recipeName.indexOf('_'));
            assertAll(
                    recipeName,
                    () -> assertEquals(entry.getValue(), recipe.get("difficulty").getAsFloat()),
                    () -> assertEquals(requiredBench, recipe.get("required_bench").getAsString()),
                    () -> assertEquals(
                            "infx:" + recipeName,
                            recipe.getAsJsonObject("result").get("id").getAsString()));
        }
        for (String advancement : List.of("flint_kit", "farming")) {
            assertTrue(
                    Files.isRegularFile(
                            GENERATED.resolve("data/infx/advancement/progression/" + advancement + ".json")),
                    advancement);
        }
        for (String disabled : List.of("iron_axe", "iron_hoe", "iron_shovel", "iron_sword")) {
            assertTrue(
                    Files.isRegularFile(GENERATED.resolve("data/minecraft/recipe/" + disabled + ".json")),
                    disabled);
        }
    }

    @Test
    void advancedCoreToolsAndMetalConversionsKeepR196Rules() throws Exception {
        Map<String, Float> ingotDifficulties = Map.of(
                "silver", 400.0F,
                "gold", 400.0F,
                "ancient_metal", 1600.0F,
                "mithril", 6400.0F,
                "adamantium", 25600.0F);
        Map<String, String> toolBenches = Map.of(
                "silver", "copper",
                "gold", "copper",
                "ancient_metal", "ancient_metal",
                "mithril", "mithril",
                "adamantium", "adamantium");
        Map<String, Integer> ingotCounts = Map.of(
                "pickaxe", 3,
                "shovel", 1,
                "axe", 3,
                "hoe", 2,
                "sword", 2);
        Map<String, Integer> stickCounts = Map.of(
                "pickaxe", 2,
                "shovel", 2,
                "axe", 2,
                "hoe", 2,
                "sword", 1);
        for (var material : ingotDifficulties.entrySet()) {
            for (String tool : ingotCounts.keySet()) {
                String recipeName = material.getKey() + "_" + tool;
                JsonObject recipe = json(GENERATED.resolve("data/infx/recipe/" + recipeName + ".json"));
                float expectedDifficulty = material.getValue() * ingotCounts.get(tool) + 25.0F * stickCounts.get(tool);
                assertAll(
                        recipeName,
                        () -> assertEquals(expectedDifficulty, recipe.get("difficulty").getAsFloat()),
                        () -> assertEquals(toolBenches.get(material.getKey()), recipe.get("required_bench").getAsString()),
                        () -> assertEquals(
                                "infx:" + recipeName,
                                recipe.getAsJsonObject("result").get("id").getAsString()));
            }
        }

        Map<String, Float> allMetalDifficulties = Map.ofEntries(
                Map.entry("copper", 400.0F),
                Map.entry("silver", 400.0F),
                Map.entry("gold", 400.0F),
                Map.entry("iron", 800.0F),
                Map.entry("ancient_metal", 1600.0F),
                Map.entry("mithril", 6400.0F),
                Map.entry("adamantium", 25600.0F));
        Set<String> vanillaMetals = Set.of("copper", "gold", "iron");
        for (var material : allMetalDifficulties.entrySet()) {
            String namespace = vanillaMetals.contains(material.getKey()) ? "minecraft" : "infx";
            JsonObject ingotRecipe = json(GENERATED.resolve(
                    "data/infx/recipe/" + material.getKey() + "_ingot_from_nuggets.json"));
            JsonObject nuggetRecipe = json(GENERATED.resolve(
                    "data/infx/recipe/" + material.getKey() + "_nuggets_from_ingot.json"));
            assertAll(
                    material.getKey() + " conversions",
                    () -> assertEquals(material.getValue(), ingotRecipe.get("difficulty").getAsFloat()),
                    () -> assertEquals("flint", ingotRecipe.get("required_bench").getAsString()),
                    () -> assertEquals(
                            namespace + ":" + material.getKey() + "_ingot",
                            ingotRecipe.getAsJsonObject("result").get("id").getAsString()),
                    () -> assertEquals(material.getValue(), nuggetRecipe.get("difficulty").getAsFloat()),
                    () -> assertEquals("hand", nuggetRecipe.get("required_bench").getAsString()),
                    () -> assertEquals(9, nuggetRecipe.getAsJsonObject("result").get("count").getAsInt()),
                    () -> assertEquals(
                            namespace + ":" + material.getKey() + "_nugget",
                            nuggetRecipe.getAsJsonObject("result").get("id").getAsString()));
        }

        for (String material : List.of("copper", "silver", "gold", "ancient_metal", "mithril", "adamantium")) {
            String nuggetNamespace = vanillaMetals.contains(material) ? "minecraft" : "infx";
            JsonObject coinRecipe = json(GENERATED.resolve(
                    "data/infx/recipe/" + material + "_coin_from_nugget.json"));
            JsonObject nuggetRecipe = json(GENERATED.resolve(
                    "data/infx/recipe/" + material + "_nugget_from_coin.json"));
            assertAll(
                    material + " coin conversions",
                    () -> assertEquals(100.0F, coinRecipe.get("difficulty").getAsFloat()),
                    () -> assertEquals("hand", coinRecipe.get("required_bench").getAsString()),
                    () -> assertEquals(
                            "infx:" + material + "_coin",
                            coinRecipe.getAsJsonObject("result").get("id").getAsString()),
                    () -> assertEquals(25.0F, nuggetRecipe.get("difficulty").getAsFloat()),
                    () -> assertEquals("hand", nuggetRecipe.get("required_bench").getAsString()),
                    () -> assertEquals(
                            nuggetNamespace + ":" + material + "_nugget",
                            nuggetRecipe.getAsJsonObject("result").get("id").getAsString()));
        }

        for (String disabled : List.of(
                "gold_ingot_from_nuggets",
                "gold_nugget",
                "iron_ingot_from_nuggets",
                "iron_nugget",
                "golden_axe",
                "golden_hoe",
                "golden_pickaxe",
                "golden_shovel",
                "golden_sword")) {
            assertTrue(
                    Files.isRegularFile(GENERATED.resolve("data/minecraft/recipe/" + disabled + ".json")),
                    disabled);
        }

        Map<String, List<String>> advancementRecipes = Map.of(
                "flint_kit",
                List.of("flint_hatchet", "flint_knife", "flint_shovel", "flint_axe"),
                "farming",
                List.of("copper_hoe", "silver_hoe", "gold_hoe", "iron_hoe"),
                "iron_age",
                List.of("iron_workbench", "iron_pickaxe"),
                "masterwork",
                List.of("adamantium_pickaxe", "adamantium_war_hammer"));
        for (var advancement : advancementRecipes.entrySet()) {
            String contents = Files.readString(
                    GENERATED.resolve("data/infx/advancement/progression/" + advancement.getKey() + ".json"),
                    UTF_8);
            for (String recipe : advancement.getValue()) {
                assertTrue(contents.contains("infx:" + recipe), advancement.getKey() + " must accept " + recipe);
            }
        }
    }

    @Test
    void specialToolRecipesKeepR196DifficultiesAndBenchTiers() throws Exception {
        Map<String, Float> ingotDifficulties = Map.ofEntries(
                Map.entry("copper", 400.0F),
                Map.entry("silver", 400.0F),
                Map.entry("gold", 400.0F),
                Map.entry("iron", 800.0F),
                Map.entry("ancient_metal", 1600.0F),
                Map.entry("mithril", 6400.0F),
                Map.entry("adamantium", 25600.0F));
        Map<String, String> benches = Map.ofEntries(
                Map.entry("copper", "copper"),
                Map.entry("silver", "copper"),
                Map.entry("gold", "copper"),
                Map.entry("iron", "iron"),
                Map.entry("ancient_metal", "ancient_metal"),
                Map.entry("mithril", "mithril"),
                Map.entry("adamantium", "adamantium"));
        Map<String, Integer> ingotCounts = Map.of(
                "mattock", 4,
                "battle_axe", 4,
                "war_hammer", 5,
                "scythe", 2,
                "hatchet", 1,
                "shears", 2);
        Map<String, Integer> stickCounts = Map.of(
                "mattock", 2,
                "battle_axe", 2,
                "war_hammer", 2,
                "scythe", 3,
                "hatchet", 2,
                "shears", 0);
        for (var material : ingotDifficulties.entrySet()) {
            for (String type : ingotCounts.keySet()) {
                String recipeName = material.getKey() + "_" + type;
                JsonObject recipe = json(GENERATED.resolve("data/infx/recipe/" + recipeName + ".json"));
                float expectedDifficulty = material.getValue() * ingotCounts.get(type)
                        + 25.0F * stickCounts.get(type);
                assertAll(
                        recipeName,
                        () -> assertEquals(expectedDifficulty, recipe.get("difficulty").getAsFloat()),
                        () -> assertEquals(benches.get(material.getKey()), recipe.get("required_bench").getAsString()),
                        () -> assertEquals(
                                "infx:" + recipeName,
                                recipe.getAsJsonObject("result").get("id").getAsString()));
            }
        }

        Map<String, Float> obsidianDifficulties = Map.of(
                "obsidian_hatchet", 315.0F,
                "obsidian_shovel", 315.0F,
                "obsidian_axe", 795.0F);
        for (var entry : obsidianDifficulties.entrySet()) {
            JsonObject recipe = json(GENERATED.resolve("data/infx/recipe/" + entry.getKey() + ".json"));
            String requiredBench = entry.getKey().equals("obsidian_hatchet") ? "hand" : "flint";
            assertAll(
                    entry.getKey(),
                    () -> assertEquals(entry.getValue(), recipe.get("difficulty").getAsFloat()),
                    () -> assertEquals(requiredBench, recipe.get("required_bench").getAsString()),
                    () -> assertEquals(
                            "infx:" + entry.getKey(),
                            recipe.getAsJsonObject("result").get("id").getAsString()));
        }
        assertTrue(Files.isRegularFile(GENERATED.resolve("data/minecraft/recipe/shears.json")));

        Map<String, List<String>> advancementRecipes = Map.of(
                "flint_kit",
                List.of("flint_hatchet", "flint_knife", "flint_shovel", "flint_axe"),
                "masterwork",
                List.of("adamantium_pickaxe", "adamantium_war_hammer"));
        for (var advancement : advancementRecipes.entrySet()) {
            String contents = Files.readString(
                    GENERATED.resolve("data/infx/advancement/progression/" + advancement.getKey() + ".json"),
                    UTF_8);
            for (String recipe : advancement.getValue()) {
                assertTrue(contents.contains("infx:" + recipe), advancement.getKey() + " must accept " + recipe);
            }
        }
        JsonObject english = json(GENERATED.resolve("assets/infx/lang/en_us.json"));
        JsonObject chinese = json(GENERATED.resolve("assets/infx/lang/zh_cn.json"));
        assertAll(
                "masterwork translations",
                () -> assertTrue(english.has("advancements.infx.masterwork.title")),
                () -> assertTrue(english.has("advancements.infx.masterwork.description")),
                () -> assertTrue(chinese.has("advancements.infx.masterwork.title")),
                () -> assertTrue(chinese.has("advancements.infx.masterwork.description")));
    }

    @Test
    void weaponRecipesKeepR196DifficultiesBenchTiersAndProgression() throws Exception {
        Map<String, Float> fixedDifficulties = Map.of(
                "wood_cudgel", 105.0F,
                "wood_club", 185.0F,
                "flint_knife", 150.0F,
                "obsidian_knife", 290.0F,
                "wood_bow", 150.0F,
                "ancient_metal_bow", 1750.0F,
                "mithril_bow", 6550.0F);
        Map<String, String> fixedBenches = Map.of(
                "wood_cudgel", "hand",
                "wood_club", "flint",
                "flint_knife", "hand",
                "obsidian_knife", "hand",
                "wood_bow", "flint",
                "ancient_metal_bow", "ancient_metal",
                "mithril_bow", "mithril");
        for (var entry : fixedDifficulties.entrySet()) {
            String recipeName = entry.getKey();
            JsonObject recipe = json(GENERATED.resolve("data/infx/recipe/" + recipeName + ".json"));
            assertAll(
                    recipeName,
                    () -> assertEquals(entry.getValue(), recipe.get("difficulty").getAsFloat()),
                    () -> assertEquals(fixedBenches.get(recipeName), recipe.get("required_bench").getAsString()),
                    () -> assertEquals(
                            "infx:" + recipeName,
                            recipe.getAsJsonObject("result").get("id").getAsString()));
        }

        Map<String, Float> ingotDifficulties = Map.ofEntries(
                Map.entry("copper", 400.0F),
                Map.entry("silver", 400.0F),
                Map.entry("gold", 400.0F),
                Map.entry("iron", 800.0F),
                Map.entry("ancient_metal", 1600.0F),
                Map.entry("mithril", 6400.0F),
                Map.entry("adamantium", 25600.0F));
        Map<String, String> benches = Map.ofEntries(
                Map.entry("copper", "copper"),
                Map.entry("silver", "copper"),
                Map.entry("gold", "copper"),
                Map.entry("iron", "iron"),
                Map.entry("ancient_metal", "ancient_metal"),
                Map.entry("mithril", "mithril"),
                Map.entry("adamantium", "adamantium"));
        for (var material : ingotDifficulties.entrySet()) {
            String recipeName = material.getKey() + "_dagger";
            JsonObject recipe = json(GENERATED.resolve("data/infx/recipe/" + recipeName + ".json"));
            assertAll(
                    recipeName,
                    () -> assertEquals(material.getValue() + 25.0F, recipe.get("difficulty").getAsFloat()),
                    () -> assertEquals(benches.get(material.getKey()), recipe.get("required_bench").getAsString()),
                    () -> assertEquals(
                            "infx:" + recipeName,
                            recipe.getAsJsonObject("result").get("id").getAsString()));
        }

        Map<String, Float> arrowheadDifficulties = Map.ofEntries(
                Map.entry("flint", 25.0F),
                Map.entry("obsidian", 200.0F / 9.0F),
                Map.entry("copper", 400.0F / 9.0F),
                Map.entry("silver", 400.0F / 9.0F),
                Map.entry("gold", 400.0F / 9.0F),
                Map.entry("iron", 800.0F / 9.0F),
                Map.entry("ancient_metal", 1600.0F / 9.0F),
                Map.entry("mithril", 6400.0F / 9.0F),
                Map.entry("adamantium", 25600.0F / 9.0F));
        Map<String, String> arrowBenches = Map.ofEntries(
                Map.entry("flint", "flint"),
                Map.entry("obsidian", "flint"),
                Map.entry("copper", "copper"),
                Map.entry("silver", "copper"),
                Map.entry("gold", "copper"),
                Map.entry("iron", "iron"),
                Map.entry("ancient_metal", "ancient_metal"),
                Map.entry("mithril", "mithril"),
                Map.entry("adamantium", "adamantium"));
        for (var material : arrowheadDifficulties.entrySet()) {
            String recipeName = material.getKey() + "_arrow";
            JsonObject recipe = json(GENERATED.resolve("data/infx/recipe/" + recipeName + ".json"));
            JsonObject result = recipe.getAsJsonObject("result");
            int count = result.has("count") ? result.get("count").getAsInt() : 1;
            assertAll(
                    recipeName,
                    () -> assertEquals(material.getValue() + 50.0F, recipe.get("difficulty").getAsFloat()),
                    () -> assertEquals(arrowBenches.get(material.getKey()), recipe.get("required_bench").getAsString()),
                    () -> assertEquals("infx:" + recipeName, result.get("id").getAsString()),
                    () -> assertEquals(1, count));
        }

        for (String disabled : List.of("arrow", "bow")) {
            assertTrue(Files.isRegularFile(GENERATED.resolve("data/minecraft/recipe/" + disabled + ".json")));
        }
        String flintKit = Files.readString(
                GENERATED.resolve("data/infx/advancement/progression/flint_kit.json"), UTF_8);
        assertTrue(flintKit.contains("infx:flint_knife"));
        assertTrue(Files.isRegularFile(
                GENERATED.resolve("data/infx/advancement/progression/flint_kit.json")));

        JsonObject english = json(GENERATED.resolve("assets/infx/lang/en_us.json"));
        JsonObject chinese = json(GENERATED.resolve("assets/infx/lang/zh_cn.json"));
        assertAll(
                "weapon progression translations",
                () -> assertEquals(
                        "Flint Kit",
                        english.get("advancements.infx.flint_kit.title").getAsString()),
                () -> assertTrue(english.has("advancements.infx.flint_kit.description")),
                () -> assertEquals(
                        "燧石工具",
                        chinese.get("advancements.infx.flint_kit.title").getAsString()),
                () -> assertTrue(chinese.has("advancements.infx.flint_kit.description")));
    }

    @Test
    void armorAndChainRecipesKeepR196RulesAndProgression() throws Exception {
        Map<String, Float> ingotDifficulties = Map.ofEntries(
                Map.entry("copper", 400.0F),
                Map.entry("silver", 400.0F),
                Map.entry("gold", 400.0F),
                Map.entry("iron", 800.0F),
                Map.entry("ancient_metal", 1600.0F),
                Map.entry("mithril", 6400.0F),
                Map.entry("adamantium", 25600.0F));
        Map<String, String> benches = Map.ofEntries(
                Map.entry("copper", "copper"),
                Map.entry("silver", "copper"),
                Map.entry("gold", "copper"),
                Map.entry("rusted_iron", "copper"),
                Map.entry("iron", "iron"),
                Map.entry("ancient_metal", "ancient_metal"),
                Map.entry("mithril", "mithril"),
                Map.entry("adamantium", "adamantium"));

        for (var material : ingotDifficulties.entrySet()) {
            float expectedDifficulty = material.getValue() * 4.0F / 9.0F;
            JsonObject chain = json(GENERATED.resolve(
                    "data/infx/recipe/" + material.getKey() + "_chain_from_nuggets.json"));
            JsonObject nuggets = json(GENERATED.resolve(
                    "data/infx/recipe/" + material.getKey() + "_nuggets_from_chain.json"));
            assertAll(
                    material.getKey() + " chain conversions",
                    () -> assertEquals(expectedDifficulty, chain.get("difficulty").getAsFloat()),
                    () -> assertEquals(benches.get(material.getKey()), chain.get("required_bench").getAsString()),
                    () -> assertEquals(
                            "infx:" + material.getKey() + "_chain",
                            chain.getAsJsonObject("result").get("id").getAsString()),
                    () -> assertEquals(expectedDifficulty, nuggets.get("difficulty").getAsFloat()),
                    () -> assertEquals("hand", nuggets.get("required_bench").getAsString()),
                    () -> assertEquals(4, nuggets.getAsJsonObject("result").get("count").getAsInt()));
        }

        Map<String, Float> plateComponents = Map.ofEntries(
                Map.entry("leather", 100.0F),
                Map.entry("copper", 400.0F),
                Map.entry("silver", 400.0F),
                Map.entry("gold", 400.0F),
                Map.entry("iron", 800.0F),
                Map.entry("ancient_metal", 1600.0F),
                Map.entry("mithril", 6400.0F),
                Map.entry("adamantium", 25600.0F));
        Map<String, Integer> platePieces = Map.of(
                "helmet", 5,
                "chestplate", 8,
                "leggings", 7,
                "boots", 4);
        for (var material : plateComponents.entrySet()) {
            for (var piece : platePieces.entrySet()) {
                String recipeName = material.getKey() + "_" + piece.getKey();
                JsonObject recipe = json(GENERATED.resolve("data/infx/recipe/" + recipeName + ".json"));
                String expectedBench = material.getKey().equals("leather")
                        ? "flint"
                        : benches.get(material.getKey());
                assertAll(
                        recipeName,
                        () -> assertEquals(
                                material.getValue() * piece.getValue(),
                                recipe.get("difficulty").getAsFloat()),
                        () -> assertEquals(expectedBench, recipe.get("required_bench").getAsString()),
                        () -> assertEquals(
                                "infx:" + recipeName,
                                recipe.getAsJsonObject("result").get("id").getAsString()));
            }
        }

        Map<String, Float> chainComponents = Map.ofEntries(
                Map.entry("copper", 400.0F * 4.0F / 9.0F),
                Map.entry("silver", 400.0F * 4.0F / 9.0F),
                Map.entry("gold", 400.0F * 4.0F / 9.0F),
                Map.entry("rusted_iron", 400.0F * 4.0F / 9.0F),
                Map.entry("iron", 800.0F * 4.0F / 9.0F),
                Map.entry("ancient_metal", 1600.0F * 4.0F / 9.0F),
                Map.entry("mithril", 6400.0F * 4.0F / 9.0F),
                Map.entry("adamantium", 25600.0F * 4.0F / 9.0F));
        Map<String, Integer> chainPieces = Map.of(
                "chainmail_helmet", 5,
                "chainmail_chestplate", 8,
                "chainmail_leggings", 7,
                "chainmail_boots", 4);
        for (var material : chainComponents.entrySet()) {
            for (var piece : chainPieces.entrySet()) {
                String recipeName = material.getKey() + "_" + piece.getKey();
                JsonObject recipe = json(GENERATED.resolve("data/infx/recipe/" + recipeName + ".json"));
                assertAll(
                        recipeName,
                        () -> assertEquals(
                                material.getValue() * piece.getValue(),
                                recipe.get("difficulty").getAsFloat()),
                        () -> assertEquals(benches.get(material.getKey()), recipe.get("required_bench").getAsString()),
                        () -> assertEquals(
                                "infx:" + recipeName,
                                recipe.getAsJsonObject("result").get("id").getAsString()));
            }
        }

        for (String piece : platePieces.keySet()) {
            assertFalse(Files.exists(GENERATED.resolve("data/infx/recipe/rusted_iron_" + piece + ".json")));
        }
        for (String material : ingotDifficulties.keySet()) {
            assertFalse(Files.exists(GENERATED.resolve("data/infx/recipe/" + material + "_horse_armor.json")));
        }
        for (String disabled : List.of(
                "leather_helmet",
                "leather_chestplate",
                "leather_leggings",
                "leather_boots",
                "golden_helmet",
                "golden_chestplate",
                "golden_leggings",
                "golden_boots",
                "iron_helmet",
                "iron_chestplate",
                "iron_leggings",
                "iron_boots")) {
            assertTrue(Files.isRegularFile(GENERATED.resolve("data/minecraft/recipe/" + disabled + ".json")));
        }

        String metalArmor = Files.readString(
                GENERATED.resolve("data/infx/advancement/progression/metal_armor.json"), UTF_8);
        assertTrue(metalArmor.contains("infx:copper_chainmail_helmet"));
        assertTrue(metalArmor.contains("infx:mithril_chainmail_boots"));
        for (String advancement : List.of(
                "leather_armor", "metal_armor", "adamantium_armor")) {
            assertTrue(Files.isRegularFile(
                    GENERATED.resolve("data/infx/advancement/progression/" + advancement + ".json")));
        }

        JsonObject english = json(GENERATED.resolve("assets/infx/lang/en_us.json"));
        JsonObject chinese = json(GENERATED.resolve("assets/infx/lang/zh_cn.json"));
        assertAll(
                "armor progression translations",
                () -> assertEquals(
                        "Metal Shell",
                        english.get("advancements.infx.metal_armor.title").getAsString()),
                () -> assertEquals(
                        "Juggernaut",
                        english.get("advancements.infx.adamantium_armor.title").getAsString()),
                () -> assertEquals(
                        "金属战甲",
                        chinese.get("advancements.infx.metal_armor.title").getAsString()),
                () -> assertEquals(
                        "世界主宰",
                        chinese.get("advancements.infx.adamantium_armor.title").getAsString()));
    }

    @Test
    void horseArmorLootSupplementsKeepR196StructuresWeightsAndRolls() throws Exception {
        Map<String, Map<String, Integer>> expectedEntries = Map.of(
                "simple_dungeon",
                Map.of(
                        "minecraft:empty", 147,
                        "infx:gold_horse_armor", 2,
                        "infx:copper_horse_armor", 5,
                        "infx:iron_horse_armor", 1),
                "nether_bridge",
                Map.of(
                        "minecraft:empty", 50,
                        "infx:gold_horse_armor", 8,
                        "infx:copper_horse_armor", 5,
                        "infx:iron_horse_armor", 3),
                "desert_pyramid",
                Map.of(
                        "minecraft:empty", 65,
                        "infx:iron_horse_armor", 1,
                        "infx:silver_horse_armor", 1,
                        "infx:gold_horse_armor", 1),
                "jungle_temple",
                Map.of(
                        "minecraft:empty", 60,
                        "infx:iron_horse_armor", 1,
                        "infx:silver_horse_armor", 1,
                        "infx:gold_horse_armor", 1),
                "stronghold_corridor",
                Map.of(
                        "minecraft:empty", 188,
                        "infx:copper_horse_armor", 1,
                        "infx:iron_horse_armor", 1));
        Map<String, List<Float>> expectedRolls = Map.of(
                "simple_dungeon", List.of(8.0F, 8.0F),
                "nether_bridge", List.of(2.0F, 5.0F),
                "desert_pyramid", List.of(2.0F, 6.0F),
                "jungle_temple", List.of(2.0F, 6.0F),
                "stronghold_corridor", List.of(2.0F, 3.0F));

        for (var structure : expectedEntries.entrySet()) {
            String path = structure.getKey();
            JsonObject table = json(GENERATED.resolve(
                    "data/infx/loot_table/chests/horse_armor/" + path + ".json"));
            JsonObject pool = table.getAsJsonArray("pools").get(0).getAsJsonObject();
            Map<String, Integer> entries = pool.getAsJsonArray("entries").asList().stream()
                    .map(JsonElement::getAsJsonObject)
                    .collect(Collectors.toMap(
                            entry -> entry.has("name")
                                    ? entry.get("name").getAsString()
                                    : "minecraft:empty",
                            entry -> entry.has("weight")
                                    ? entry.get("weight").getAsInt()
                                    : 1));
            JsonElement rolls = pool.get("rolls");
            float minRolls = rolls.isJsonObject()
                    ? rolls.getAsJsonObject().get("min").getAsFloat()
                    : rolls.getAsFloat();
            float maxRolls = rolls.isJsonObject()
                    ? rolls.getAsJsonObject().get("max").getAsFloat()
                    : rolls.getAsFloat();
            assertAll(
                    path,
                    () -> assertEquals(structure.getValue(), entries),
                    () -> assertEquals(expectedRolls.get(path).getFirst(), minRolls),
                    () -> assertEquals(expectedRolls.get(path).get(1), maxRolls),
                    () -> assertFalse(table.toString().contains("ancient_metal_horse_armor")),
                    () -> assertFalse(table.toString().contains("mithril_horse_armor")),
                    () -> assertFalse(table.toString().contains("adamantium_horse_armor")));

            JsonObject modifier = json(GENERATED.resolve(
                    "data/infx/loot_modifiers/horse_armor_" + path + ".json"));
            JsonObject condition = modifier.getAsJsonArray("conditions").get(0).getAsJsonObject();
            assertAll(
                    path + " modifier",
                    () -> assertEquals("neoforge:add_table", modifier.get("type").getAsString()),
                    () -> assertEquals(
                            "infx:chests/horse_armor/" + path,
                            modifier.get("table").getAsString()),
                    () -> assertEquals(
                            "minecraft:chests/" + path,
                            condition.get("loot_table_id").getAsString()));
        }
        assertEquals(
                5,
                jsonCount(GENERATED.resolve("data/infx/loot_table/chests/horse_armor")));
    }

    @Test
    void miteStructureLootTargetsAndMappingsAreGenerated() throws Exception {
        List<String> structures = List.of(
                "simple_dungeon",
                "abandoned_mineshaft",
                "nether_bridge",
                "desert_pyramid",
                "jungle_temple",
                "stronghold_corridor",
                "stronghold_crossing",
                "stronghold_library",
                "ancient_city",
                "ancient_city_ice_box",
                "bastion_bridge",
                "bastion_hoglin_stable",
                "bastion_other",
                "bastion_treasure",
                "buried_treasure",
                "end_city_treasure",
                "igloo_chest",
                "pillager_outpost",
                "ruined_portal",
                "shipwreck_map",
                "shipwreck_supply",
                "shipwreck_treasure",
                "underwater_ruin_big",
                "underwater_ruin_small",
                "woodland_mansion",
                "village/village_armorer",
                "village/village_butcher",
                "village/village_cartographer",
                "village/village_desert_house",
                "village/village_fisher",
                "village/village_fletcher",
                "village/village_mason",
                "village/village_plains_house",
                "village/village_savanna_house",
                "village/village_shepherd",
                "village/village_snowy_house",
                "village/village_taiga_house",
                "village/village_tannery",
                "village/village_temple",
                "village/village_toolsmith",
                "village/village_weaponsmith",
                "trial_chambers/corridor",
                "trial_chambers/entrance",
                "trial_chambers/intersection",
                "trial_chambers/intersection_barrel",
                "trial_chambers/reward",
                "trial_chambers/reward_ominous",
                "trial_chambers/supply");
        Path tables = GENERATED.resolve("data/infx/loot_table/chests/mite");
        assertEquals(48, jsonCount(tables));
        assertFalse(Files.exists(GENERATED.resolve("data/infx/loot_table/chests/mite/trail_ruins_common.json")));
        assertFalse(Files.exists(GENERATED.resolve("data/infx/loot_table/chests/mite/trail_ruins_rare.json")));

        for (String structure : structures) {
            String modifierName = "mite_structure_" + structure.replace('/', '_') + ".json";
            JsonObject modifier = json(GENERATED.resolve("data/infx/loot_modifiers/" + modifierName));
            JsonObject condition = modifier.getAsJsonArray("conditions").get(0).getAsJsonObject();
            assertAll(
                    structure,
                    () -> assertEquals("neoforge:add_table", modifier.get("type").getAsString()),
                    () -> assertEquals("infx:chests/mite/" + structure, modifier.get("table").getAsString()),
                    () -> assertEquals(
                            "minecraft:chests/" + structure,
                            condition.get("loot_table_id").getAsString()));
        }

        String dungeon = Files.readString(tables.resolve("simple_dungeon.json"), UTF_8);
        String mineshaft = Files.readString(tables.resolve("abandoned_mineshaft.json"), UTF_8);
        String armorer = Files.readString(tables.resolve("village/village_armorer.json"), UTF_8);
        String trial = Files.readString(tables.resolve("trial_chambers/reward.json"), UTF_8);
        String ominous = Files.readString(tables.resolve("trial_chambers/reward_ominous.json"), UTF_8);
        assertAll(
                "MITE mappings",
                () -> assertTrue(dungeon.contains("infx:onion")),
                () -> assertTrue(dungeon.contains("infx:copper_coin")),
                () -> assertFalse(dungeon.contains("infx:ancient_metal_fishing_rod")),
                () -> assertFalse(dungeon.contains("minecraft:diamond")),
                () -> assertTrue(mineshaft.contains("infx:cheese")),
                () -> assertTrue(mineshaft.contains("infx:silver_ingot")),
                () -> assertFalse(mineshaft.contains("infx:ancient_metal_pickaxe")),
                () -> assertFalse(armorer.contains("infx:ancient_metal_chainmail_boots")),
                () -> assertFalse(armorer.contains("infx:ancient_metal_chestplate")),
                () -> assertTrue(trial.contains("minecraft:book")),
                () -> assertTrue(trial.contains("infx:diamond_shard")),
                () -> assertFalse(ominous.contains("infx:ancient_metal_pickaxe")));

        try (Stream<Path> modifiers = Files.walk(GENERATED.resolve("data/infx/loot_modifiers"))) {
            assertEquals(
                    48,
                    modifiers.filter(path -> path.getFileName().toString().startsWith("mite_structure_"))
                            .count());
        }
    }

    @Test
    void furnaceHeatTagsSeparateCoalFromLowHeatFuel() throws Exception {
        String heatTwoFuels = Files.readString(
                GENERATED.resolve("data/infx/tags/item/furnace_fuels/heat_2.json"), UTF_8);
        assertAll(
                "heat-2 fuels",
                () -> assertTrue(heatTwoFuels.contains("minecraft:coal")),
                () -> assertTrue(heatTwoFuels.contains("minecraft:coal_block")),
                () -> assertFalse(heatTwoFuels.contains("minecraft:charcoal")));

        String heatTwoInputs = Files.readString(
                GENERATED.resolve("data/infx/tags/item/smelting_inputs/heat_2.json"), UTF_8);
        assertAll(
                "heat-2 inputs",
                () -> assertTrue(heatTwoInputs.contains("minecraft:raw_iron")),
                () -> assertTrue(heatTwoInputs.contains("minecraft:iron_ore")),
                () -> assertTrue(heatTwoInputs.contains("minecraft:nether_quartz_ore")),
                () -> assertTrue(heatTwoInputs.contains("minecraft:sandstone")),
                () -> assertTrue(heatTwoInputs.contains("infx:silver_ore")),
                () -> assertTrue(heatTwoInputs.contains("infx:deepslate_silver_ore")),
                () -> assertTrue(Files.isRegularFile(GENERATED.resolve(
                        "data/infx/recipe/silver_ingot_from_smelting_silver_ore.json"))));
    }

    @Test
    void miteOresHaveCompleteResourcesAndProgressionData() throws Exception {
        JsonObject english = json(GENERATED.resolve("assets/infx/lang/en_us.json"));
        JsonObject chinese = json(GENERATED.resolve("assets/infx/lang/zh_cn.json"));
        for (String ore : List.of(
                "silver_ore",
                "deepslate_silver_ore",
                "mithril_ore",
                "deepslate_mithril_ore",
                "adamantium_ore",
                "deepslate_adamantium_ore")) {
            assertAll(
                    ore,
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("assets/infx/blockstates/" + ore + ".json"))),
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("assets/infx/items/" + ore + ".json"))),
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("assets/infx/models/block/" + ore + ".json"))),
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("data/infx/loot_table/blocks/" + ore + ".json"))),
                    () -> assertTrue(english.has("block.infx." + ore)),
                    () -> assertTrue(chinese.has("block.infx." + ore)));
        }

        String heatThreeInputs = Files.readString(
                GENERATED.resolve("data/infx/tags/item/smelting_inputs/heat_3.json"), UTF_8);
        String heatFourInputs = Files.readString(
                GENERATED.resolve("data/infx/tags/item/smelting_inputs/heat_4.json"), UTF_8);
        assertAll(
                "high-heat smelting",
                () -> assertTrue(heatThreeInputs.contains("infx:mithril_ore")),
                () -> assertTrue(heatThreeInputs.contains("infx:deepslate_mithril_ore")),
                () -> assertTrue(heatFourInputs.contains("infx:adamantium_ore")),
                () -> assertTrue(heatFourInputs.contains("infx:deepslate_adamantium_ore")),
                () -> assertTrue(Files.isRegularFile(GENERATED.resolve(
                        "data/infx/recipe/mithril_ingot_from_smelting_mithril_ore.json"))),
                () -> assertTrue(Files.isRegularFile(GENERATED.resolve(
                        "data/infx/recipe/adamantium_ingot_from_smelting_adamantium_ore.json"))));

    }

    @Test
    void overworldResourceOresUseHardcoreProfileAndReplaceVanillaFeatures() throws Exception {
        Map<String, OverworldOreExpectation> expectedOres = Map.ofEntries(
                Map.entry("coal", new OverworldOreExpectation(
                        12,
                        "minecraft:coal_ore",
                        "minecraft:deepslate_coal_ore",
                        "minecraft:count",
                        4,
                        "minecraft:uniform",
                        32,
                        160)),
                Map.entry("copper", new OverworldOreExpectation(
                        6,
                        "minecraft:copper_ore",
                        "minecraft:deepslate_copper_ore",
                        "minecraft:count",
                        3,
                        "minecraft:uniform",
                        -8,
                        96)),
                Map.entry("iron", new OverworldOreExpectation(
                        6,
                        "minecraft:iron_ore",
                        "minecraft:deepslate_iron_ore",
                        "minecraft:count",
                        4,
                        "minecraft:biased_to_bottom",
                        -8,
                        80)),
                Map.entry("silver", new OverworldOreExpectation(
                        6,
                        "infx:silver_ore",
                        "infx:deepslate_silver_ore",
                        "minecraft:rarity_filter",
                        2,
                        "minecraft:biased_to_bottom",
                        -16,
                        64)),
                Map.entry("gold", new OverworldOreExpectation(
                        4,
                        "minecraft:gold_ore",
                        "minecraft:deepslate_gold_ore",
                        "minecraft:count",
                        1,
                        "minecraft:biased_to_bottom",
                        -16,
                        40)),
                Map.entry("mithril", new OverworldOreExpectation(
                        3,
                        "infx:mithril_ore",
                        "infx:deepslate_mithril_ore",
                        "minecraft:rarity_filter",
                        4,
                        "minecraft:biased_to_bottom",
                        -16,
                        8)),
                Map.entry("redstone", new OverworldOreExpectation(
                        5,
                        "minecraft:redstone_ore",
                        "minecraft:deepslate_redstone_ore",
                        "minecraft:rarity_filter",
                        2,
                        "minecraft:biased_to_bottom",
                        -16,
                        16)),
                Map.entry("diamond", new OverworldOreExpectation(
                        3,
                        "minecraft:diamond_ore",
                        "minecraft:deepslate_diamond_ore",
                        "minecraft:rarity_filter",
                        4,
                        "minecraft:biased_to_bottom",
                        -16,
                        4)),
                Map.entry("lapis", new OverworldOreExpectation(
                        3,
                        "minecraft:lapis_ore",
                        "minecraft:deepslate_lapis_ore",
                        "minecraft:rarity_filter",
                        4,
                        "minecraft:uniform",
                        8,
                        32)),
                Map.entry("emerald", new OverworldOreExpectation(
                        1,
                        "minecraft:emerald_ore",
                        "minecraft:deepslate_emerald_ore",
                        "minecraft:rarity_filter",
                        2,
                        "minecraft:uniform",
                        48,
                        128)));
        for (Map.Entry<String, OverworldOreExpectation> entry : expectedOres.entrySet()) {
            assertOverworldOre(entry.getKey(), entry.getValue());
        }

        JsonObject removed = json(GENERATED.resolve(
                "data/infx/neoforge/biome_modifier/remove_overworld_resource_ores.json"));
        JsonObject added = json(GENERATED.resolve(
                "data/infx/neoforge/biome_modifier/add_overworld_resource_ores.json"));
        JsonObject emerald = json(GENERATED.resolve(
                "data/infx/neoforge/biome_modifier/add_overworld_emerald_ore.json"));
        JsonObject silver = json(GENERATED.resolve(
                "data/infx/neoforge/biome_modifier/add_silver_ore.json"));
        JsonObject mithril = json(GENERATED.resolve(
                "data/infx/neoforge/biome_modifier/add_mithril_ore.json"));
        Set<String> removedFeatures = removed.getAsJsonArray("features").asList().stream()
                .map(JsonElement::getAsString)
                .collect(Collectors.toSet());
        Set<String> addedFeatures = added.getAsJsonArray("features").asList().stream()
                .map(JsonElement::getAsString)
                .collect(Collectors.toSet());
        assertAll(
                "overworld resource ore modifiers",
                () -> assertEquals("#minecraft:is_overworld", removed.get("biomes").getAsString()),
                () -> assertEquals("underground_ores", removed.get("steps").getAsString()),
                () -> assertEquals(
                        Set.of(
                                "minecraft:ore_coal_upper",
                                "minecraft:ore_coal_lower",
                                "minecraft:ore_iron_upper",
                                "minecraft:ore_iron_middle",
                                "minecraft:ore_iron_small",
                                "minecraft:ore_gold_extra",
                                "minecraft:ore_gold",
                                "minecraft:ore_gold_lower",
                                "minecraft:ore_redstone",
                                "minecraft:ore_redstone_lower",
                                "minecraft:ore_diamond",
                                "minecraft:ore_diamond_medium",
                                "minecraft:ore_diamond_large",
                                "minecraft:ore_diamond_buried",
                                "minecraft:ore_lapis",
                                "minecraft:ore_lapis_buried",
                                "minecraft:ore_copper",
                                "minecraft:ore_copper_large",
                                "minecraft:ore_emerald"),
                        removedFeatures),
                () -> assertFalse(removedFeatures.contains("minecraft:ore_infested")),
                () -> assertFalse(removedFeatures.contains("minecraft:ore_dirt")),
                () -> assertEquals("#minecraft:is_overworld", added.get("biomes").getAsString()),
                () -> assertEquals(
                        Set.of(
                                "infx:overworld_coal_ore",
                                "infx:overworld_copper_ore",
                                "infx:overworld_iron_ore",
                                "infx:overworld_gold_ore",
                                "infx:overworld_redstone_ore",
                                "infx:overworld_diamond_ore",
                                "infx:overworld_lapis_ore"),
                        addedFeatures),
                () -> assertEquals("#minecraft:is_mountain", emerald.get("biomes").getAsString()),
                () -> assertEquals("infx:overworld_emerald_ore", emerald.get("features").getAsString()),
                () -> assertEquals("infx:overworld_silver_ore", silver.get("features").getAsString()),
                () -> assertEquals("infx:overworld_mithril_ore", mithril.get("features").getAsString()));

    }

    @Test
    void mountainBiomesUseSgravelForSurfaceDisksTerrainAndPatches() throws Exception {
        JsonObject sandConfigured = json(GENERATED.resolve(
                "data/infx/worldgen/configured_feature/sgravel_disk.json"));
        JsonObject gravelConfigured = json(GENERATED.resolve(
                "data/infx/worldgen/configured_feature/sgravel_gravel_disk.json"));
        JsonObject oreConfigured = json(GENERATED.resolve(
                "data/infx/worldgen/configured_feature/mountain_sgravel_ore.json"));
        JsonObject sandPlaced = json(GENERATED.resolve(
                "data/infx/worldgen/placed_feature/sgravel_disk.json"));
        JsonObject gravelPlaced = json(GENERATED.resolve(
                "data/infx/worldgen/placed_feature/sgravel_gravel_disk.json"));
        JsonObject orePlaced = json(GENERATED.resolve(
                "data/infx/worldgen/placed_feature/mountain_sgravel_ore.json"));
        JsonObject removed = json(GENERATED.resolve(
                "data/infx/neoforge/biome_modifier/replace_mountain_soft_disks.json"));
        JsonObject added = json(GENERATED.resolve(
                "data/infx/neoforge/biome_modifier/add_mountain_sgravel_disks.json"));
        JsonObject sandDiskConfig = sandConfigured.getAsJsonObject("config");
        JsonObject gravelDiskConfig = gravelConfigured.getAsJsonObject("config");
        JsonObject oreConfig = oreConfigured.getAsJsonObject("config");
        JsonArray sandPlacement = sandPlaced.getAsJsonArray("placement");
        JsonArray gravelPlacement = gravelPlaced.getAsJsonArray("placement");
        JsonArray orePlacement = orePlaced.getAsJsonArray("placement");
        Set<String> expectedBiomes = Set.of(
                "minecraft:stony_peaks",
                "minecraft:windswept_gravelly_hills");
        Set<String> removedBiomes = removed.getAsJsonArray("biomes").asList().stream()
                .map(JsonElement::getAsString)
                .collect(Collectors.toSet());
        Set<String> addedBiomes = added.getAsJsonArray("biomes").asList().stream()
                .map(JsonElement::getAsString)
                .collect(Collectors.toSet());
        Set<String> targets = sandDiskConfig.getAsJsonObject("target")
                .getAsJsonArray("blocks")
                .asList()
                .stream()
                .map(JsonElement::getAsString)
                .collect(Collectors.toSet());
        Set<String> removedFeatures = removed.getAsJsonArray("features").asList().stream()
                .map(JsonElement::getAsString)
                .collect(Collectors.toSet());
        Set<String> addedFeatures = added.getAsJsonArray("features").asList().stream()
                .map(JsonElement::getAsString)
                .collect(Collectors.toSet());

        assertAll(
                "mountain sgravel disks",
                () -> assertEquals("minecraft:disk", sandConfigured.get("type").getAsString()),
                () -> assertEquals("minecraft:disk", gravelConfigured.get("type").getAsString()),
                () -> assertEquals("infx:sgravel", sandDiskConfig
                        .getAsJsonObject("state_provider")
                        .getAsJsonObject("state")
                        .get("Name")
                        .getAsString()),
                () -> assertEquals("infx:sgravel", gravelDiskConfig
                        .getAsJsonObject("state_provider")
                        .getAsJsonObject("state")
                        .get("Name")
                        .getAsString()),
                () -> assertEquals(2, sandDiskConfig.get("half_height").getAsInt()),
                () -> assertEquals(2, gravelDiskConfig.get("half_height").getAsInt()),
                () -> assertEquals(2, sandDiskConfig.getAsJsonObject("radius").get("min_inclusive").getAsInt()),
                () -> assertEquals(6, sandDiskConfig.getAsJsonObject("radius").get("max_inclusive").getAsInt()),
                () -> assertEquals(2, gravelDiskConfig.getAsJsonObject("radius").get("min_inclusive").getAsInt()),
                () -> assertEquals(5, gravelDiskConfig.getAsJsonObject("radius").get("max_inclusive").getAsInt()),
                () -> assertEquals(Set.of("minecraft:dirt", "minecraft:grass_block"), targets),
                () -> assertEquals("infx:sgravel_disk", sandPlaced.get("feature").getAsString()),
                () -> assertEquals("infx:sgravel_gravel_disk", gravelPlaced.get("feature").getAsString()),
                () -> assertEquals("minecraft:ore", oreConfigured.get("type").getAsString()),
                () -> assertEquals(33, oreConfig.get("size").getAsInt()),
                () -> assertEquals("infx:sgravel", oreConfig.getAsJsonArray("targets")
                        .get(0)
                        .getAsJsonObject()
                        .getAsJsonObject("state")
                        .get("Name")
                        .getAsString()),
                () -> assertEquals("minecraft:base_stone_overworld", oreConfig.getAsJsonArray("targets")
                        .get(0)
                        .getAsJsonObject()
                        .getAsJsonObject("target")
                        .get("tag")
                        .getAsString()),
                () -> assertEquals("infx:mountain_sgravel_ore", orePlaced.get("feature").getAsString()),
                () -> assertEquals("minecraft:count", orePlacement.get(0)
                        .getAsJsonObject()
                        .get("type")
                        .getAsString()),
                () -> assertEquals(14, orePlacement.get(0).getAsJsonObject().get("count").getAsInt()),
                () -> assertEquals("minecraft:in_square", orePlacement.get(1)
                        .getAsJsonObject()
                        .get("type")
                        .getAsString()),
                () -> assertEquals("minecraft:height_range", orePlacement.get(2)
                        .getAsJsonObject()
                        .get("type")
                        .getAsString()),
                () -> assertEquals("minecraft:uniform", orePlacement.get(2)
                        .getAsJsonObject()
                        .getAsJsonObject("height")
                        .get("type")
                        .getAsString()),
                () -> assertEquals(0, orePlacement.get(2)
                        .getAsJsonObject()
                        .getAsJsonObject("height")
                        .getAsJsonObject("min_inclusive")
                        .get("above_bottom")
                        .getAsInt()),
                () -> assertEquals(0, orePlacement.get(2)
                        .getAsJsonObject()
                        .getAsJsonObject("height")
                        .getAsJsonObject("max_inclusive")
                        .get("below_top")
                        .getAsInt()),
                () -> assertEquals("minecraft:biome", orePlacement.get(3)
                        .getAsJsonObject()
                        .get("type")
                        .getAsString()),
                () -> assertEquals("minecraft:count", sandPlacement.get(0)
                        .getAsJsonObject()
                        .get("type")
                        .getAsString()),
                () -> assertEquals(3, sandPlacement.get(0)
                        .getAsJsonObject()
                        .get("count")
                        .getAsInt()),
                () -> assertEquals("minecraft:heightmap", sandPlacement.get(2)
                        .getAsJsonObject()
                        .get("type")
                        .getAsString()),
                () -> assertEquals("OCEAN_FLOOR_WG", sandPlacement.get(2)
                        .getAsJsonObject()
                        .get("heightmap")
                        .getAsString()),
                () -> assertEquals("minecraft:matching_fluids", sandPlacement.get(3)
                        .getAsJsonObject()
                        .getAsJsonObject("predicate")
                        .get("type")
                        .getAsString()),
                () -> assertEquals("minecraft:heightmap", gravelPlacement.get(1)
                        .getAsJsonObject()
                        .get("type")
                        .getAsString()),
                () -> assertEquals("OCEAN_FLOOR_WG", gravelPlacement.get(1)
                        .getAsJsonObject()
                        .get("heightmap")
                        .getAsString()),
                () -> assertEquals(
                        Set.of("minecraft:disk_sand", "minecraft:disk_gravel", "minecraft:ore_gravel"),
                        removedFeatures),
                () -> assertEquals("underground_ores", removed.get("steps").getAsString()),
                () -> assertEquals(
                        Set.of("infx:sgravel_disk", "infx:sgravel_gravel_disk", "infx:mountain_sgravel_ore"),
                        addedFeatures),
                () -> assertEquals("underground_ores", added.get("step").getAsString()),
                () -> assertEquals(expectedBiomes, removedBiomes),
                () -> assertEquals(expectedBiomes, addedBiomes));

        for (String noiseSettings : List.of("overworld", "large_biomes", "amplified")) {
            JsonObject surfaceRule = json(GENERATED.resolve(
                    "data/minecraft/worldgen/noise_settings/" + noiseSettings + ".json"))
                    .getAsJsonObject("surface_rule");
            JsonObject replacement = surfaceRule.getAsJsonArray("sequence").get(0).getAsJsonObject();
            JsonObject biomeRule = replacement.getAsJsonObject("then_run");
            Set<String> replacementStates = new HashSet<>();
            visit(replacement, (key, value) -> {
                if ("Name".equals(key)) replacementStates.add(value);
            });

            assertAll(
                    noiseSettings + " mountain terrain sgravel",
                    () -> assertEquals("minecraft:sequence", surfaceRule.get("type").getAsString()),
                    () -> assertEquals("minecraft:above_preliminary_surface", replacement
                            .getAsJsonObject("if_true")
                            .get("type")
                            .getAsString()),
                    () -> assertEquals("minecraft:biome", biomeRule
                            .getAsJsonObject("if_true")
                            .get("type")
                            .getAsString()),
                    () -> assertEquals(
                            Set.of("minecraft:windswept_gravelly_hills"),
                            biomeRule.getAsJsonObject("if_true")
                                    .getAsJsonArray("biome_is")
                                    .asList()
                                    .stream()
                                    .map(JsonElement::getAsString)
                                    .collect(Collectors.toSet())),
                    () -> assertEquals(Set.of("infx:sgravel"), replacementStates));
        }
    }

    @Test
    void shoreAndRiverBiomesUseSgravelForSoftDisksTerrainAndPatches() throws Exception {
        JsonObject sandConfigured = json(GENERATED.resolve(
                "data/infx/worldgen/configured_feature/shore_river_sgravel_disk.json"));
        JsonObject gravelConfigured = json(GENERATED.resolve(
                "data/infx/worldgen/configured_feature/shore_river_sgravel_gravel_disk.json"));
        JsonObject oreConfigured = json(GENERATED.resolve(
                "data/infx/worldgen/configured_feature/shore_river_sgravel_ore.json"));
        JsonObject sandPlaced = json(GENERATED.resolve(
                "data/infx/worldgen/placed_feature/shore_river_sgravel_disk.json"));
        JsonObject gravelPlaced = json(GENERATED.resolve(
                "data/infx/worldgen/placed_feature/shore_river_sgravel_gravel_disk.json"));
        JsonObject orePlaced = json(GENERATED.resolve(
                "data/infx/worldgen/placed_feature/shore_river_sgravel_ore.json"));
        JsonObject removed = json(GENERATED.resolve(
                "data/infx/neoforge/biome_modifier/replace_shore_river_soft_disks.json"));
        JsonObject added = json(GENERATED.resolve(
                "data/infx/neoforge/biome_modifier/add_shore_river_sgravel_disks.json"));
        JsonObject sandDiskConfig = sandConfigured.getAsJsonObject("config");
        JsonObject gravelDiskConfig = gravelConfigured.getAsJsonObject("config");
        JsonObject oreConfig = oreConfigured.getAsJsonObject("config");
        JsonArray orePlacement = orePlaced.getAsJsonArray("placement");
        Set<String> expectedBiomes = Set.of(
                "minecraft:stony_shore",
                "minecraft:river",
                "minecraft:frozen_river",
                "infx:desert_river",
                "infx:jungle_river",
                "infx:swamp_river");
        Set<String> removedBiomes = removed.getAsJsonArray("biomes").asList().stream()
                .map(JsonElement::getAsString)
                .collect(Collectors.toSet());
        Set<String> addedBiomes = added.getAsJsonArray("biomes").asList().stream()
                .map(JsonElement::getAsString)
                .collect(Collectors.toSet());
        Set<String> removedFeatures = removed.getAsJsonArray("features").asList().stream()
                .map(JsonElement::getAsString)
                .collect(Collectors.toSet());
        Set<String> addedFeatures = added.getAsJsonArray("features").asList().stream()
                .map(JsonElement::getAsString)
                .collect(Collectors.toSet());

        assertAll(
                "shore and river sgravel disks",
                () -> assertEquals("minecraft:disk", sandConfigured.get("type").getAsString()),
                () -> assertEquals("minecraft:disk", gravelConfigured.get("type").getAsString()),
                () -> assertEquals("infx:sgravel", sandDiskConfig
                        .getAsJsonObject("state_provider")
                        .getAsJsonObject("state")
                        .get("Name")
                        .getAsString()),
                () -> assertEquals("infx:sgravel", gravelDiskConfig
                        .getAsJsonObject("state_provider")
                        .getAsJsonObject("state")
                        .get("Name")
                        .getAsString()),
                () -> assertEquals(6, sandDiskConfig
                        .getAsJsonObject("radius")
                        .get("max_inclusive")
                        .getAsInt()),
                () -> assertEquals(5, gravelDiskConfig
                        .getAsJsonObject("radius")
                        .get("max_inclusive")
                        .getAsInt()),
                () -> assertEquals("infx:shore_river_sgravel_disk", sandPlaced
                        .get("feature")
                        .getAsString()),
                () -> assertEquals("infx:shore_river_sgravel_gravel_disk", gravelPlaced
                        .get("feature")
                        .getAsString()),
                () -> assertEquals("minecraft:ore", oreConfigured.get("type").getAsString()),
                () -> assertEquals(33, oreConfig.get("size").getAsInt()),
                () -> assertEquals("infx:sgravel", oreConfig.getAsJsonArray("targets")
                        .get(0)
                        .getAsJsonObject()
                        .getAsJsonObject("state")
                        .get("Name")
                        .getAsString()),
                () -> assertEquals("minecraft:base_stone_overworld", oreConfig.getAsJsonArray("targets")
                        .get(0)
                        .getAsJsonObject()
                        .getAsJsonObject("target")
                        .get("tag")
                        .getAsString()),
                () -> assertEquals("infx:shore_river_sgravel_ore", orePlaced.get("feature").getAsString()),
                () -> assertEquals("minecraft:count", orePlacement.get(0)
                        .getAsJsonObject()
                        .get("type")
                        .getAsString()),
                () -> assertEquals(14, orePlacement.get(0).getAsJsonObject().get("count").getAsInt()),
                () -> assertEquals("minecraft:in_square", orePlacement.get(1)
                        .getAsJsonObject()
                        .get("type")
                        .getAsString()),
                () -> assertEquals("minecraft:height_range", orePlacement.get(2)
                        .getAsJsonObject()
                        .get("type")
                        .getAsString()),
                () -> assertEquals("minecraft:biome", orePlacement.get(3)
                        .getAsJsonObject()
                        .get("type")
                        .getAsString()),
                () -> assertEquals("neoforge:remove_features", removed.get("type").getAsString()),
                () -> assertEquals("neoforge:add_features", added.get("type").getAsString()),
                () -> assertEquals(
                        Set.of("minecraft:disk_sand", "minecraft:disk_gravel", "minecraft:ore_gravel"),
                        removedFeatures),
                () -> assertEquals("underground_ores", removed.get("steps").getAsString()),
                () -> assertEquals(
                        Set.of(
                                "infx:shore_river_sgravel_disk",
                                "infx:shore_river_sgravel_gravel_disk",
                                "infx:shore_river_sgravel_ore"),
                        addedFeatures),
                () -> assertEquals("underground_ores", added.get("step").getAsString()),
                () -> assertEquals(expectedBiomes, removedBiomes),
                () -> assertEquals(expectedBiomes, addedBiomes));

        for (String noiseSettings : List.of("overworld", "large_biomes", "amplified")) {
            JsonObject surfaceRule = json(GENERATED.resolve(
                    "data/minecraft/worldgen/noise_settings/" + noiseSettings + ".json"))
                    .getAsJsonObject("surface_rule");
            JsonArray sequence = surfaceRule.getAsJsonArray("sequence");
            JsonObject stonyShoreReplacement = sequence.get(1).getAsJsonObject();
            JsonObject stonyShoreBiomeRule = stonyShoreReplacement.getAsJsonObject("then_run");
            JsonObject waterfrontReplacement = sequence.get(2).getAsJsonObject();
            JsonObject waterfrontBiomeRule = waterfrontReplacement.getAsJsonObject("then_run");
            Set<String> stonyShoreStates = new HashSet<>();
            Set<String> waterfrontStates = new HashSet<>();
            visit(stonyShoreReplacement, (key, value) -> {
                if ("Name".equals(key)) stonyShoreStates.add(value);
            });
            visit(waterfrontReplacement, (key, value) -> {
                if ("Name".equals(key)) waterfrontStates.add(value);
            });

            assertAll(
                    noiseSettings + " shore and river terrain sgravel",
                    () -> assertEquals("minecraft:condition", stonyShoreReplacement
                            .get("type")
                            .getAsString()),
                    () -> assertEquals("minecraft:above_preliminary_surface", stonyShoreReplacement
                            .getAsJsonObject("if_true")
                            .get("type")
                            .getAsString()),
                    () -> assertEquals(
                            Set.of("minecraft:stony_shore"),
                            stonyShoreBiomeRule.getAsJsonObject("if_true")
                                    .getAsJsonArray("biome_is")
                                    .asList()
                                    .stream()
                                    .map(JsonElement::getAsString)
                                    .collect(Collectors.toSet())),
                    () -> assertEquals(Set.of("infx:sgravel"), stonyShoreStates),
                    () -> assertEquals("minecraft:condition", waterfrontReplacement
                            .get("type")
                            .getAsString()),
                    () -> assertEquals("minecraft:above_preliminary_surface", waterfrontReplacement
                            .getAsJsonObject("if_true")
                            .get("type")
                            .getAsString()),
                    () -> assertEquals(
                            expectedBiomes,
                            waterfrontBiomeRule.getAsJsonObject("if_true")
                                    .getAsJsonArray("biome_is")
                                    .asList()
                                    .stream()
                                    .map(JsonElement::getAsString)
                                    .collect(Collectors.toSet())),
                    () -> assertEquals(Set.of("infx:sgravel"), waterfrontStates));
        }
    }

    @Test
    void underworldDataUsesShiftedMiteTerrainWithDeepSlateAndRandomBoundaries() throws Exception {
        JsonObject dimension = json(GENERATED.resolve("data/infx/dimension/underworld.json"));
        JsonObject generator = dimension.getAsJsonObject("generator");
        JsonObject dimensionType = json(GENERATED.resolve("data/infx/dimension_type/underworld.json"));
        JsonObject bedRule = dimensionType
                .getAsJsonObject("attributes")
                .getAsJsonObject("minecraft:gameplay/bed_rule");
        JsonObject biome = json(GENERATED.resolve("data/infx/worldgen/biome/underworld.json"));
        JsonObject lushBiome = json(GENERATED.resolve("data/infx/worldgen/biome/underworld_lush.json"));
        JsonObject deepDarkBiome = json(GENERATED.resolve("data/infx/worldgen/biome/underworld_deep_dark.json"));
        JsonObject biomeAttributes = biome.getAsJsonObject("attributes");
        JsonObject lushBiomeAttributes = lushBiome.getAsJsonObject("attributes");
        JsonObject deepDarkBiomeAttributes = deepDarkBiome.getAsJsonObject("attributes");
        JsonObject dimensionAttributes = dimensionType.getAsJsonObject("attributes");
        JsonObject noise = json(GENERATED.resolve("data/infx/worldgen/noise_settings/underworld.json"));
        JsonObject biomeSource = generator.getAsJsonObject("biome_source");
        Set<String> underworldFeatures = biome.getAsJsonArray("features").asList().stream()
                .flatMap(step -> step.getAsJsonArray().asList().stream())
                .map(JsonElement::getAsString)
                .collect(Collectors.toSet());
        JsonArray underworldDungeonFeatures = biome.getAsJsonArray("features")
                .get(GenerationStep.Decoration.UNDERGROUND_STRUCTURES.ordinal())
                .getAsJsonArray();
        JsonArray underworldDecorationFeatures = biome.getAsJsonArray("features")
                .get(GenerationStep.Decoration.UNDERGROUND_DECORATION.ordinal())
                .getAsJsonArray();
        JsonArray underworldVegetalFeatures = featureStep(biome, GenerationStep.Decoration.VEGETAL_DECORATION);
        JsonArray lushVegetalFeatures = featureStep(lushBiome, GenerationStep.Decoration.VEGETAL_DECORATION);
        JsonArray deepDarkDecorationFeatures = featureStep(
                deepDarkBiome, GenerationStep.Decoration.UNDERGROUND_DECORATION);
        JsonArray deepDarkVegetalFeatures = featureStep(deepDarkBiome, GenerationStep.Decoration.VEGETAL_DECORATION);
        JsonObject configuredDungeon = json(GENERATED.resolve(
                "data/infx/worldgen/configured_feature/underworld_dungeon.json"));
        JsonObject ancientCity = json(GENERATED.resolve(
                "data/infx/worldgen/structure/underworld_ancient_city.json"));
        JsonObject ancientCities = json(GENERATED.resolve(
                "data/infx/worldgen/structure_set/underworld_ancient_cities.json"));
        JsonObject placedDungeon = json(GENERATED.resolve(
                "data/infx/worldgen/placed_feature/underworld_dungeon.json"));
        JsonArray dungeonPlacement = placedDungeon.getAsJsonArray("placement");
        JsonObject noiseShape = noise.getAsJsonObject("noise");
        JsonElement finalDensity = noise.getAsJsonObject("noise_router").get("final_density");
        JsonObject surfaceRule = noise.getAsJsonObject("surface_rule");
        JsonArray surfaceRules = surfaceRule.getAsJsonArray("sequence");
        JsonObject mantleRule = surfaceRules.get(0).getAsJsonObject();
        JsonObject mantleGradient = mantleRule.getAsJsonObject("if_true");
        JsonObject mantleTrueAnchor = mantleGradient.getAsJsonObject("true_at_and_below");
        JsonObject mantleFalseAnchor = mantleGradient.getAsJsonObject("false_at_and_above");
        JsonObject mantleState = mantleRule.getAsJsonObject("then_run").getAsJsonObject("result_state");
        JsonObject bedrockRule = surfaceRules.get(1).getAsJsonObject();
        JsonObject bedrockCondition = bedrockRule.getAsJsonObject("if_true");
        JsonObject bedrockGradient = bedrockCondition.getAsJsonObject("invert");
        JsonObject bedrockTrueAnchor = bedrockGradient.getAsJsonObject("true_at_and_below");
        JsonObject bedrockFalseAnchor = bedrockGradient.getAsJsonObject("false_at_and_above");
        JsonObject bedrockState = bedrockRule.getAsJsonObject("then_run").getAsJsonObject("result_state");
        JsonObject deepslateRule = surfaceRules.get(2).getAsJsonObject();
        JsonObject deepslateCondition = deepslateRule.getAsJsonObject("if_true");
        JsonObject deepslateAbove = deepslateCondition.getAsJsonObject("invert");
        JsonObject deepslateAnchor = deepslateAbove.getAsJsonObject("anchor");
        JsonObject deepslateState = deepslateRule.getAsJsonObject("then_run").getAsJsonObject("result_state");
        JsonObject stoneState = surfaceRules.get(3).getAsJsonObject().getAsJsonObject("result_state");
        String mixinConfig = Files.readString(STATIC.resolve("infx.mixins.json"));
        assertAll(
                "Underworld dimension",
                () -> assertEquals("infx:underworld", dimension.get("type").getAsString()),
                () -> assertEquals("infx:underworld", generator.get("type").getAsString()),
                () -> assertEquals("infx:underworld_biome_source", biomeSource.get("type").getAsString()),
                () -> assertEquals("infx:underworld", biomeSource.get("ordinary").getAsString()),
                () -> assertEquals("infx:underworld_lush", biomeSource.get("lush").getAsString()),
                () -> assertEquals("infx:underworld_deep_dark", biomeSource.get("deep_dark").getAsString()),
                () -> assertEquals("infx:underworld", generator.get("settings").getAsString()),
                () -> assertEquals(-128, dimensionType.get("min_y").getAsInt()),
                () -> assertEquals(384, dimensionType.get("height").getAsInt()),
                () -> assertEquals(384, dimensionType.get("logical_height").getAsInt()),
                () -> assertEquals(
                        256,
                        dimensionType.get("min_y").getAsInt()
                                + dimensionType.get("height").getAsInt()),
                () -> assertTrue(dimensionType.get("has_ceiling").getAsBoolean()),
                () -> assertFalse(dimensionType.get("has_skylight").getAsBoolean()),
                () -> assertEquals(0.05F, dimensionType.get("ambient_light").getAsFloat()),
                () -> assertEquals(
                        8.0F,
                        dimensionAttributes
                                .get("minecraft:visual/fog_start_distance")
                                .getAsFloat()),
                () -> assertEquals(
                        96.0F,
                        dimensionAttributes
                                .get("minecraft:visual/fog_end_distance")
                                .getAsFloat()),
                () -> assertEquals("#3f76e4", biome.getAsJsonObject("effects")
                        .get("water_color")
                        .getAsString()),
                () -> assertEquals("#3f76e4", lushBiome.getAsJsonObject("effects")
                        .get("water_color")
                        .getAsString()),
                () -> assertEquals("#3f76e4", deepDarkBiome.getAsJsonObject("effects")
                        .get("water_color")
                        .getAsString()),
                () -> assertEquals(
                        "#303030",
                        biomeAttributes.get("minecraft:visual/fog_color").getAsString()),
                () -> assertEquals(
                        "#303030",
                        biomeAttributes
                                .get("minecraft:visual/ambient_light_color")
                                .getAsString()),
                () -> assertEquals(
                        "#6b4630",
                        lushBiomeAttributes.get("minecraft:visual/fog_color").getAsString()),
                () -> assertEquals(
                        "#6b4630",
                        lushBiomeAttributes
                                .get("minecraft:visual/ambient_light_color")
                                .getAsString()),
                () -> assertFalse(deepDarkBiomeAttributes.has("minecraft:visual/fog_color")),
                () -> assertFalse(deepDarkBiomeAttributes.has("minecraft:visual/ambient_light_color")),
                () -> assertEquals("minecraft:stone", noise.getAsJsonObject("default_block").get("Name").getAsString()),
                () -> assertEquals("minecraft:water", noise.getAsJsonObject("default_fluid").get("Name").getAsString()),
                () -> assertEquals(144, noise.get("sea_level").getAsInt()),
                () -> assertFalse(noise.get("aquifers_enabled").getAsBoolean()),
                () -> assertFalse(noise.get("ore_veins_enabled").getAsBoolean()),
                () -> assertTrue(noise.get("legacy_random_source").getAsBoolean()),
                () -> assertEquals("minecraft:noise", noise.getAsJsonObject("noise_router")
                        .getAsJsonObject("temperature").get("type").getAsString()),
                () -> assertEquals("infx:underworld_biome", noise.getAsJsonObject("noise_router")
                        .getAsJsonObject("temperature").get("noise").getAsString()),
                () -> assertEquals(1.0 / 64.0, noise.getAsJsonObject("noise_router")
                        .getAsJsonObject("temperature").get("xz_scale").getAsDouble()),
                () -> assertEquals(0.0, noise.getAsJsonObject("noise_router")
                        .getAsJsonObject("temperature").get("y_scale").getAsDouble()),
                () -> assertEquals(-128, noiseShape.get("min_y").getAsInt()),
                () -> assertEquals(384, noiseShape.get("height").getAsInt()),
                () -> assertTrue(finalDensity.isJsonPrimitive()),
                () -> assertEquals("infx:underworld_terrain", finalDensity.getAsString()),
                () -> assertTrue(Files.exists(GENERATED.resolve(
                        "data/infx/worldgen/density_function/underworld_terrain.json"))),
                () -> assertEquals("minecraft:sequence", surfaceRule.get("type").getAsString()),
                () -> assertEquals(4, surfaceRules.size()),
                () -> assertEquals("minecraft:condition", mantleRule.get("type").getAsString()),
                () -> assertEquals("minecraft:vertical_gradient", mantleGradient.get("type").getAsString()),
                () -> assertEquals("infx:underworld_mantle", mantleGradient.get("random_name").getAsString()),
                () -> assertEquals(0, mantleTrueAnchor.get("above_bottom").getAsInt()),
                () -> assertEquals(5, mantleFalseAnchor.get("above_bottom").getAsInt()),
                () -> assertEquals("infx:mantle", mantleState.get("Name").getAsString()),
                () -> assertEquals("minecraft:condition", bedrockRule.get("type").getAsString()),
                () -> assertEquals("minecraft:not", bedrockCondition.get("type").getAsString()),
                () -> assertEquals("minecraft:vertical_gradient", bedrockGradient.get("type").getAsString()),
                () -> assertEquals("infx:underworld_bedrock_roof", bedrockGradient.get("random_name").getAsString()),
                () -> assertEquals(5, bedrockTrueAnchor.get("below_top").getAsInt()),
                () -> assertEquals(0, bedrockFalseAnchor.get("below_top").getAsInt()),
                () -> assertEquals("minecraft:bedrock", bedrockState.get("Name").getAsString()),
                () -> assertEquals("minecraft:condition", deepslateRule.get("type").getAsString()),
                () -> assertEquals("minecraft:not", deepslateCondition.get("type").getAsString()),
                () -> assertEquals("minecraft:y_above", deepslateAbove.get("type").getAsString()),
                () -> assertFalse(deepslateAbove.get("add_stone_depth").getAsBoolean()),
                () -> assertEquals(0, deepslateAbove.get("surface_depth_multiplier").getAsInt()),
                () -> assertEquals(120, deepslateAnchor.get("absolute").getAsInt()),
                () -> assertEquals("minecraft:deepslate", deepslateState.get("Name").getAsString()),
                () -> assertEquals("minecraft:stone", stoneState.get("Name").getAsString()),
                () -> assertEquals("never", bedRule.get("can_sleep").getAsString()),
                () -> assertEquals("never", bedRule.get("can_set_spawn").getAsString()),
                () -> assertFalse(bedRule.has("explodes")),
                () -> assertEquals(
                        18,
                        biome.getAsJsonObject("spawners").getAsJsonArray("monster").size(),
                        "Underworld monster spawn table"),
                () -> assertTrue(
                        biome.getAsJsonObject("spawners").entrySet().stream()
                                .filter(entry -> !entry.getKey().equals("monster"))
                                .allMatch(entry -> entry.getValue().getAsJsonArray().isEmpty()),
                        "Underworld non-monster spawn tables must remain empty"),
                () -> assertEquals(
                        Set.of(
                                "infx:underworld_dungeon",
                                "infx:underworld_copper_ore_low",
                                "infx:underworld_copper_ore_full",
                                "infx:underworld_silver_ore_low",
                                "infx:underworld_silver_ore_full",
                                "infx:underworld_gold_ore_low",
                                "infx:underworld_gold_ore_full",
                                "infx:underworld_iron_ore_low",
                                "infx:underworld_iron_ore_full",
                                "infx:underworld_mithril_ore_low",
                                "infx:underworld_mithril_ore_full",
                                "infx:underworld_adamantium_ore_low",
                                "infx:underworld_adamantium_ore_full",
                                "infx:underworld_redstone_ore_low",
                                "infx:underworld_redstone_ore_full",
                                "infx:underworld_diamond_ore_low",
                                "infx:underworld_diamond_ore_full",
                                "infx:underworld_lapis_ore_low",
                                "infx:underworld_lapis_ore_full",
                                "infx:underworld_silverfish_low",
                                "infx:underworld_silverfish_full",
                                "infx:underworld_gravel_low",
                                "infx:underworld_gravel_full",
                                "infx:underworld_mycelium",
                                "infx:underworld_brown_mushroom",
                                "infx:underworld_liquid_source"),
                        underworldFeatures),
                () -> assertTrue(biome.getAsJsonArray("carvers").isEmpty()),
                () -> assertTrue(lushBiome.getAsJsonArray("carvers").isEmpty()),
                () -> assertTrue(deepDarkBiome.getAsJsonArray("carvers").isEmpty()),
                () -> assertEquals("minecraft:jigsaw", ancientCity.get("type").getAsString()),
                () -> assertEquals(
                        "infx:underworld_deep_dark",
                        ancientCity.get("biomes").getAsString()),
                () -> assertEquals("minecraft:ancient_city/city_center", ancientCity.get("start_pool").getAsString()),
                () -> assertEquals("minecraft:city_anchor", ancientCity.get("start_jigsaw_name").getAsString()),
                () -> assertEquals(24, ancientCity.getAsJsonObject("start_height").get("absolute").getAsInt()),
                () -> assertEquals(7, ancientCity.get("size").getAsInt()),
                () -> assertEquals(116, ancientCity.get("max_distance_from_center").getAsInt()),
                () -> assertEquals("underground_decoration", ancientCity.get("step").getAsString()),
                () -> assertEquals("beard_box", ancientCity.get("terrain_adaptation").getAsString()),
                () -> assertEquals(
                        "infx:underworld_ancient_city",
                        ancientCities.getAsJsonArray("structures")
                                .get(0)
                                .getAsJsonObject()
                                .get("structure")
                                .getAsString()),
                () -> assertEquals(1, ancientCities.getAsJsonArray("structures")
                        .get(0)
                        .getAsJsonObject()
                        .get("weight")
                        .getAsInt()),
                () -> assertEquals("minecraft:random_spread", ancientCities.getAsJsonObject("placement")
                        .get("type")
                        .getAsString()),
                () -> assertEquals(24, ancientCities.getAsJsonObject("placement").get("spacing").getAsInt()),
                () -> assertEquals(8, ancientCities.getAsJsonObject("placement").get("separation").getAsInt()),
                () -> assertEquals(20083232, ancientCities.getAsJsonObject("placement").get("salt").getAsInt()),
                () -> assertFalse(mixinConfig.contains("\"NoiseBasedChunkGeneratorMixin\"")),
                () -> assertFalse(Files.exists(GENERATED.resolve(
                        "data/infx/worldgen/density_function/infx_first_cave.json"))),
                () -> assertFalse(Files.exists(GENERATED.resolve(
                        "data/infx/worldgen/configured_carver/underworld_cave.json"))),
                () -> assertFalse(Files.exists(GENERATED.resolve(
                        "data/infx/worldgen/configured_carver/underworld_cave_extra_underground.json"))),
                () -> assertFalse(Files.exists(GENERATED.resolve(
                        "data/infx/worldgen/configured_carver/underworld_canyon.json"))),
                () -> assertFalse(Files.exists(GENERATED.resolve(
                        "data/infx/worldgen/configured_carver/underworld_large_cave.json"))),
                () -> assertFalse(Files.exists(GENERATED.resolve(
                        "data/infx/worldgen/configured_feature/silver_ore.json"))),
                () -> assertFalse(Files.exists(GENERATED.resolve(
                        "data/infx/worldgen/configured_feature/mithril_ore.json"))),
                () -> assertFalse(Files.exists(GENERATED.resolve(
                        "data/infx/worldgen/placed_feature/silver_ore.json"))),
                () -> assertFalse(Files.exists(GENERATED.resolve(
                        "data/infx/worldgen/placed_feature/mithril_ore.json"))),
                () -> assertFalse(Files.exists(GENERATED.resolve(
                        "data/infx/neoforge/biome_modifier/add_underworld_ores.json"))),
                () -> assertFalse(Files.exists(GENERATED.resolve(
                        "data/infx/loot_modifiers/underworld_dungeon.json"))));

        Set<String> lushFeatures = lushBiome.getAsJsonArray("features").asList().stream()
                .flatMap(step -> step.getAsJsonArray().asList().stream())
                .map(JsonElement::getAsString)
                .collect(Collectors.toSet());
        Set<String> deepDarkFeatures = deepDarkBiome.getAsJsonArray("features").asList().stream()
                .flatMap(step -> step.getAsJsonArray().asList().stream())
                .map(JsonElement::getAsString)
                .collect(Collectors.toSet());
        assertAll(
                "Underworld biome feature partition",
                () -> assertTrue(lushFeatures.contains("infx:underworld_lush_caves_vegetation")),
                () -> assertTrue(lushFeatures.contains("infx:underworld_rooted_azalea_tree")),
                () -> assertFalse(lushFeatures.contains("infx:underworld_mycelium")),
                () -> assertFalse(lushFeatures.contains("infx:underworld_brown_mushroom")),
                () -> assertTrue(deepDarkFeatures.contains("infx:underworld_mycelium")),
                () -> assertTrue(deepDarkFeatures.contains("minecraft:sculk_patch_deep_dark")),
                () -> assertTrue(deepDarkFeatures.contains("minecraft:sculk_vein")),
                () -> assertTrue(deepDarkFeatures.contains("minecraft:glow_lichen")),
                () -> assertFalse(deepDarkFeatures.contains("infx:underworld_lush_caves_vegetation")),
                () -> assertTrue(deepDarkDecorationFeatures.asList().stream()
                        .map(JsonElement::getAsString)
                        .anyMatch("minecraft:sculk_patch_deep_dark"::equals)),
                () -> assertEquals(List.of("minecraft:glow_lichen"), deepDarkVegetalFeatures.asList().stream()
                        .map(JsonElement::getAsString)
                        .toList()));

        JsonObject dungeonOffset = dungeonPlacement.get(2).getAsJsonObject();
        JsonObject dungeonHeight = dungeonPlacement.get(3).getAsJsonObject().getAsJsonObject("height");
        assertAll(
                "Underworld dungeon decoration",
                () -> assertEquals("infx:underworld_dungeon", configuredDungeon.get("type").getAsString()),
                () -> assertTrue(configuredDungeon.getAsJsonObject("config").isEmpty()),
                () -> assertEquals(List.of("infx:underworld_dungeon"), underworldDungeonFeatures.asList().stream()
                        .map(JsonElement::getAsString)
                        .toList()),
                () -> assertEquals(5, dungeonPlacement.size()),
                () -> assertEquals("minecraft:count", dungeonPlacement.get(0).getAsJsonObject()
                        .get("type")
                        .getAsString()),
                () -> assertEquals(16, dungeonPlacement.get(0).getAsJsonObject().get("count").getAsInt()),
                () -> assertEquals("minecraft:in_square", dungeonPlacement.get(1).getAsJsonObject()
                        .get("type")
                        .getAsString()),
                () -> assertEquals("minecraft:random_offset", dungeonOffset.get("type").getAsString()),
                () -> assertEquals(8, intProviderValue(dungeonOffset.get("xz_spread"))),
                () -> assertEquals(0, intProviderValue(dungeonOffset.get("y_spread"))),
                () -> assertEquals("minecraft:height_range", dungeonPlacement.get(3).getAsJsonObject()
                        .get("type")
                        .getAsString()),
                () -> assertEquals("minecraft:uniform", dungeonHeight.get("type").getAsString()),
                () -> assertEquals(
                        140,
                        dungeonHeight.getAsJsonObject("min_inclusive")
                                .get("absolute")
                                .getAsInt()),
                () -> assertEquals(
                        171,
                        dungeonHeight.getAsJsonObject("max_inclusive")
                                .get("absolute")
                                .getAsInt()),
                () -> assertEquals("minecraft:biome", dungeonPlacement.get(4).getAsJsonObject()
                        .get("type")
                        .getAsString()));

        List<String> oreFeatureIds = List.of(
                "underworld_copper_ore_low",
                "underworld_copper_ore_full",
                "underworld_silver_ore_low",
                "underworld_silver_ore_full",
                "underworld_gold_ore_low",
                "underworld_gold_ore_full",
                "underworld_iron_ore_low",
                "underworld_iron_ore_full",
                "underworld_mithril_ore_low",
                "underworld_mithril_ore_full",
                "underworld_adamantium_ore_low",
                "underworld_adamantium_ore_full",
                "underworld_redstone_ore_low",
                "underworld_redstone_ore_full",
                "underworld_diamond_ore_low",
                "underworld_diamond_ore_full",
                "underworld_lapis_ore_low",
                "underworld_lapis_ore_full",
                "underworld_silverfish_low",
                "underworld_silverfish_full",
                "underworld_gravel_low",
                "underworld_gravel_full");
        List<String> naturalFeatureIds = List.of(
                "underworld_mycelium",
                "underworld_brown_mushroom",
                "underworld_liquid_source");
        assertEquals(
                Stream.concat(oreFeatureIds.stream(), naturalFeatureIds.stream())
                        .map(id -> "infx:" + id)
                        .toList(),
                underworldDecorationFeatures.asList().stream().map(JsonElement::getAsString).toList());
        for (String featureId : naturalFeatureIds) {
            JsonObject configured = json(GENERATED.resolve(
                    "data/infx/worldgen/configured_feature/" + featureId + ".json"));
            JsonObject placed = json(GENERATED.resolve(
                    "data/infx/worldgen/placed_feature/" + featureId + ".json"));
            JsonArray placement = placed.getAsJsonArray("placement");
            assertAll(
                    "Underworld natural decoration " + featureId,
                    () -> assertEquals("infx:" + featureId, configured.get("type").getAsString()),
                    () -> assertTrue(configured.getAsJsonObject("config").isEmpty()),
                    () -> assertEquals("infx:" + featureId, placed.get("feature").getAsString()),
                    () -> assertEquals(2, placement.size()),
                    () -> assertEquals("minecraft:count", placement.get(0).getAsJsonObject()
                            .get("type")
                            .getAsString()),
                    () -> assertEquals(1, placement.get(0).getAsJsonObject().get("count").getAsInt()),
                    () -> assertEquals("minecraft:biome", placement.get(1).getAsJsonObject()
                            .get("type")
                            .getAsString()));
        }

        List<String> lushFeatureIds = List.of(
                "underworld_lush_caves_ceiling_vegetation",
                "underworld_cave_vines",
                "underworld_lush_caves_clay",
                "underworld_lush_caves_vegetation",
                "underworld_rooted_azalea_tree",
                "underworld_spore_blossom",
                "underworld_classic_vines");
        assertEquals(
                lushFeatureIds.stream().map(id -> "infx:" + id).toList(),
                lushVegetalFeatures.asList().stream().map(JsonElement::getAsString).toList());
        Map<String, String> lushConfiguredFeatures = Map.ofEntries(
                Map.entry("underworld_lush_caves_ceiling_vegetation", "minecraft:moss_patch_ceiling"),
                Map.entry("underworld_cave_vines", "minecraft:cave_vine"),
                Map.entry("underworld_lush_caves_clay", "minecraft:lush_caves_clay"),
                Map.entry("underworld_lush_caves_vegetation", "minecraft:moss_patch"),
                Map.entry("underworld_rooted_azalea_tree", "minecraft:rooted_azalea_tree"),
                Map.entry("underworld_spore_blossom", "minecraft:spore_blossom"),
                Map.entry("underworld_classic_vines", "minecraft:vines"));
        Map<String, Integer> lushCounts = Map.ofEntries(
                Map.entry("underworld_lush_caves_ceiling_vegetation", 125),
                Map.entry("underworld_cave_vines", 188),
                Map.entry("underworld_lush_caves_clay", 62),
                Map.entry("underworld_lush_caves_vegetation", 125),
                Map.entry("underworld_rooted_azalea_tree", -1),
                Map.entry("underworld_spore_blossom", 25),
                Map.entry("underworld_classic_vines", 256));
        Set<String> floorScannedLushFeatures = Set.of(
                "underworld_lush_caves_clay", "underworld_lush_caves_vegetation");
        for (String featureId : lushFeatureIds) {
            JsonObject placed = json(GENERATED.resolve(
                    "data/infx/worldgen/placed_feature/" + featureId + ".json"));
            JsonArray placement = placed.getAsJsonArray("placement");
            JsonObject height = placement.get(2).getAsJsonObject().getAsJsonObject("height");
            int expectedMinimumY = floorScannedLushFeatures.contains(featureId)
                    ? Underworld.LUSH_CAVES_FLOOR_SCAN_MIN_Y
                    : Underworld.LUSH_CAVES_MIN_Y;
            assertAll(
                    "Underworld lush decoration " + featureId,
                    () -> assertEquals(lushConfiguredFeatures.get(featureId), placed.get("feature").getAsString()),
                    () -> assertEquals("minecraft:count", placement.get(0).getAsJsonObject()
                            .get("type")
                            .getAsString()),
                    () -> {
                        if (lushCounts.get(featureId) >= 0) {
                            assertEquals(lushCounts.get(featureId), placement.get(0).getAsJsonObject()
                                    .get("count")
                                    .getAsInt());
                        } else {
                            JsonObject count = placement.get(0).getAsJsonObject().getAsJsonObject("count");
                            assertEquals(1, count.get("min_inclusive").getAsInt());
                            assertEquals(2, count.get("max_inclusive").getAsInt());
                        }
                    },
                    () -> assertEquals("minecraft:in_square", placement.get(1).getAsJsonObject()
                            .get("type")
                            .getAsString()),
                    () -> assertEquals("minecraft:height_range", placement.get(2).getAsJsonObject()
                            .get("type")
                            .getAsString()),
                    () -> assertEquals("minecraft:uniform", height.get("type").getAsString()),
                    () -> assertEquals(expectedMinimumY, height.getAsJsonObject("min_inclusive")
                            .get("absolute")
                            .getAsInt()),
                    () -> assertEquals(Underworld.LUSH_CAVES_MAX_Y_INCLUSIVE,
                            height.getAsJsonObject("max_inclusive").get("absolute").getAsInt()),
                    () -> assertEquals("minecraft:biome", placement.get(placement.size() - 1)
                            .getAsJsonObject()
                            .get("type")
                            .getAsString()));
        }

        Map<String, UnderworldOreExpectation> underworldOres = Map.ofEntries(
                Map.entry("copper_ore", new UnderworldOreExpectation(
                        "minecraft:ore", 6, 24, 8, "minecraft:copper_ore", "minecraft:deepslate_copper_ore")),
                Map.entry("silver_ore", new UnderworldOreExpectation(
                        "minecraft:ore", 6, 6, 2, "infx:silver_ore", "infx:deepslate_silver_ore")),
                Map.entry("gold_ore", new UnderworldOreExpectation(
                        "minecraft:ore", 4, 12, 4, "minecraft:gold_ore", "minecraft:deepslate_gold_ore")),
                Map.entry("iron_ore", new UnderworldOreExpectation(
                        "minecraft:ore", 6, 36, 12, "minecraft:iron_ore", "minecraft:deepslate_iron_ore")),
                Map.entry("mithril_ore", new UnderworldOreExpectation(
                        "minecraft:ore", 3, 6, 2, "infx:mithril_ore", "infx:deepslate_mithril_ore")),
                Map.entry("adamantium_ore", new UnderworldOreExpectation(
                        "minecraft:ore", 3, 8, 0, "infx:adamantium_ore", "infx:deepslate_adamantium_ore")),
                Map.entry("redstone_ore", new UnderworldOreExpectation(
                        "minecraft:ore", 5, 6, 2, "minecraft:redstone_ore", "minecraft:deepslate_redstone_ore")),
                Map.entry("diamond_ore", new UnderworldOreExpectation(
                        "minecraft:ore", 3, 3, 1, "minecraft:diamond_ore", "minecraft:deepslate_diamond_ore")),
                Map.entry("lapis_ore", new UnderworldOreExpectation(
                        "minecraft:ore", 3, 3, 1, "minecraft:lapis_ore", "minecraft:deepslate_lapis_ore")),
                Map.entry("silverfish", new UnderworldOreExpectation(
                        "minecraft:ore", 3, 0, 40, "minecraft:infested_stone", "minecraft:infested_deepslate")),
                Map.entry("gravel", new UnderworldOreExpectation(
                        "infx:underworld_supported_gravel", 32, 0, 30, "minecraft:gravel", "minecraft:gravel")));
        for (Map.Entry<String, UnderworldOreExpectation> entry : underworldOres.entrySet()) {
            assertUnderworldOre(entry.getKey(), entry.getValue());
        }

        for (String strataNoise : List.of(
                "underworld_bedrock_strata_1a",
                "underworld_bedrock_strata_1b",
                "underworld_bedrock_strata_2",
                "underworld_bedrock_strata_3",
                "underworld_bedrock_strata_4",
                "underworld_bedrock_strata_1a_bump",
                "underworld_bedrock_strata_1b_bump",
                "underworld_bedrock_strata_1c_bump",
                "underworld_bedrock_strata_2_bump",
                "underworld_bedrock_strata_3_bump",
                "underworld_bedrock_strata_4_bump")) {
            JsonObject strata = json(GENERATED.resolve("data/infx/worldgen/noise/" + strataNoise + ".json"));
            assertAll(
                    strataNoise,
                    () -> assertEquals(-3, strata.get("firstOctave").getAsInt()),
                    () -> assertEquals(List.of(1.0, 1.0, 1.0, 1.0), strata.getAsJsonArray("amplitudes")
                            .asList()
                            .stream()
                            .map(JsonElement::getAsDouble)
                            .toList()));
        }

        JsonObject dungeon = json(GENERATED.resolve("data/infx/loot_table/chests/underworld_dungeon.json"));
        JsonObject dungeonPool = dungeon.getAsJsonArray("pools").get(0).getAsJsonObject();
        String dungeonContents = dungeon.toString();
        JsonObject emptyDungeonEntry = dungeonPool.getAsJsonArray("entries").get(0).getAsJsonObject();
        assertAll(
                "Underworld dungeon progression",
                () -> assertEquals(8.0F, dungeonPool.get("rolls").getAsFloat()),
                () -> assertEquals("minecraft:empty", emptyDungeonEntry.get("type").getAsString()),
                () -> assertEquals(54, emptyDungeonEntry.get("weight").getAsInt()),
                () -> assertEquals(
                        100,
                        dungeonPool.getAsJsonArray("entries").asList().stream()
                                .mapToInt(entry -> {
                                    JsonObject lootEntry = entry.getAsJsonObject();
                                    return lootEntry.has("weight") ? lootEntry.get("weight").getAsInt() : 1;
                                })
                                .sum()),
                () -> assertTrue(dungeonContents.contains("infx:ancient_metal_ingot")),
                () -> assertTrue(dungeonContents.contains("infx:ancient_metal_horse_armor")),
                () -> assertTrue(dungeonContents.contains("infx:ancient_metal_pickaxe")));
    }

    @Test
    void netherDataUsesMantleAtTopAndSingleCoreAtBottom() throws Exception {
        JsonObject noise = json(GENERATED.resolve("data/minecraft/worldgen/noise_settings/nether.json"));
        JsonArray surfaceRules = noise.getAsJsonObject("surface_rule").getAsJsonArray("sequence");
        JsonObject coreRule = surfaceRules.get(0).getAsJsonObject();
        JsonObject coreCondition = coreRule.getAsJsonObject("if_true");
        JsonObject coreYCheck = coreCondition.getAsJsonObject("invert");
        JsonObject coreAnchor = coreYCheck.getAsJsonObject("anchor");
        JsonObject coreState = coreRule.getAsJsonObject("then_run").getAsJsonObject("result_state");
        JsonObject mantleRule = surfaceRules.get(1).getAsJsonObject();
        JsonObject mantleYCheck = mantleRule.getAsJsonObject("if_true");
        JsonObject mantleAnchor = mantleYCheck.getAsJsonObject("anchor");
        JsonObject mantleState = mantleRule.getAsJsonObject("then_run").getAsJsonObject("result_state");

        assertAll(
                "Nether boundary layers",
                () -> assertEquals(0, noise.getAsJsonObject("noise").get("min_y").getAsInt()),
                () -> assertEquals(128, noise.getAsJsonObject("noise").get("height").getAsInt()),
                () -> assertEquals("minecraft:sequence", noise.getAsJsonObject("surface_rule")
                        .get("type")
                        .getAsString()),
                () -> assertEquals("minecraft:condition", coreRule.get("type").getAsString()),
                () -> assertEquals("minecraft:not", coreCondition.get("type").getAsString()),
                () -> assertEquals("minecraft:y_above", coreYCheck.get("type").getAsString()),
                () -> assertEquals(1, coreAnchor.get("above_bottom").getAsInt()),
                () -> assertFalse(coreYCheck.get("add_stone_depth").getAsBoolean()),
                () -> assertEquals(0, coreYCheck.get("surface_depth_multiplier").getAsInt()),
                () -> assertEquals("infx:core", coreState.get("Name").getAsString()),
                () -> assertEquals("minecraft:condition", mantleRule.get("type").getAsString()),
                () -> assertEquals("minecraft:y_above", mantleYCheck.get("type").getAsString()),
                () -> assertEquals(0, mantleAnchor.get("below_top").getAsInt()),
                () -> assertFalse(mantleYCheck.get("add_stone_depth").getAsBoolean()),
                () -> assertEquals(0, mantleYCheck.get("surface_depth_multiplier").getAsInt()),
                () -> assertEquals("infx:mantle", mantleState.get("Name").getAsString()),
                () -> assertTrue(surfaceRules.size() > 2));
    }

    @Test
    void underworldDensityDataUsesMiteProfileAndShiftedCoordinates() throws Exception {
        JsonObject terrain = json(GENERATED.resolve(
                "data/infx/worldgen/density_function/underworld_terrain.json"));
        JsonObject shiftedY = objectWithType(terrain, "infx:shifted_y");
        JsonObject blendedNoise = shiftedY.getAsJsonObject("input");
        JsonObject terrainInRange = terrain.getAsJsonObject("when_in_range");
        JsonObject interpolatedTerrain = terrainInRange.getAsJsonObject("input");

        assertMiteUnderworldProfile(terrain);
        assertAll(
                "MITE Underworld density",
                () -> assertEquals("minecraft:range_choice", terrain.get("type").getAsString()),
                () -> assertEquals(120.0, terrain.get("min_inclusive").getAsDouble()),
                () -> assertEquals(248.0, terrain.get("max_exclusive").getAsDouble()),
                () -> assertEquals(1.0, terrain.get("when_out_of_range").getAsDouble()),
                () -> assertEquals("minecraft:clamp", terrainInRange.get("type").getAsString()),
                () -> assertEquals("minecraft:interpolated", interpolatedTerrain.get("type").getAsString()),
                () -> assertEquals(120, shiftedY.get("offset").getAsInt()),
                () -> assertEquals("minecraft:old_blended_noise", blendedNoise.get("type").getAsString()),
                () -> assertEquals(0.25, blendedNoise.get("xz_scale").getAsDouble()),
                () -> assertEquals(0.375, blendedNoise.get("y_scale").getAsDouble()),
                () -> assertEquals(80.0, blendedNoise.get("xz_factor").getAsDouble()),
                () -> assertEquals(60.0, blendedNoise.get("y_factor").getAsDouble()),
                () -> assertEquals(8.0, blendedNoise.get("smear_scale_multiplier").getAsDouble()),
                () -> assertTrue(hasGradient(terrain, 224, 248, 0.0, 1.0)));
    }

    @Test
    void overworldStopsAtMinusSixteenAndLeavesUndergroundStructuresUnassigned() throws Exception {
        JsonObject dimensionType = json(GENERATED.resolve("data/minecraft/dimension_type/overworld.json"));
        assertAll(
                "Overworld build height",
                () -> assertEquals(-16, dimensionType.get("min_y").getAsInt()),
                () -> assertEquals(336, dimensionType.get("height").getAsInt()),
                () -> assertEquals(336, dimensionType.get("logical_height").getAsInt()),
                () -> assertEquals(
                        320,
                        dimensionType.get("min_y").getAsInt()
                                + dimensionType.get("height").getAsInt()));

        for (String settings : List.of("overworld", "large_biomes", "amplified")) {
            JsonObject noise = json(GENERATED.resolve(
                    "data/minecraft/worldgen/noise_settings/" + settings + ".json"));
            JsonObject shape = noise.getAsJsonObject("noise");
            assertAll(
                    settings,
                    () -> assertEquals(-16, shape.get("min_y").getAsInt()),
                    () -> assertEquals(336, shape.get("height").getAsInt()),
                    () -> assertFalse(noise.get("ore_veins_enabled").getAsBoolean()),
                    () -> assertTrue(noise.getAsJsonObject("noise_router")
                            .get("final_density")
                            .toString()
                            .contains("\"from_y\":-16")),
                    () -> assertTrue(noise.getAsJsonObject("noise_router")
                            .get("final_density")
                            .toString()
                            .contains("\"to_y\":8")));
        }

        JsonObject ancientCityTag = json(GENERATED.resolve(
                "data/minecraft/tags/worldgen/biome/has_structure/ancient_city.json"));
        assertEquals(
                Set.of("minecraft:deep_dark"),
                ancientCityTag.getAsJsonArray("values").asList().stream()
                        .map(JsonElement::getAsString)
                        .collect(Collectors.toSet()));
        for (String structure : List.of("buried_treasure", "trail_ruins", "trial_chambers")) {
            assertFalse(
                    Files.exists(GENERATED.resolve(
                            "data/minecraft/tags/worldgen/biome/has_structure/" + structure + ".json")),
                    structure + " must not target the empty Underworld biome");
        }
        for (String structure : List.of("mineshaft", "mineshaft_mesa")) {
            assertFalse(
                    Files.exists(GENERATED.resolve(
                            "data/minecraft/tags/worldgen/biome/has_structure/" + structure + ".json")),
                    structure + " must retain its vanilla Overworld distribution");
        }

        JsonObject stronghold = json(GENERATED.resolve(
                "data/minecraft/tags/worldgen/biome/has_structure/stronghold.json"));
        assertAll(
                "restored stronghold progression",
                () -> assertFalse(stronghold.has("replace")),
                () -> assertEquals(
                        "#minecraft:is_overworld",
                        stronghold.getAsJsonArray("values").get(0).getAsString()));
    }

    @Test
    void moonTimelineUsesMiteDayOnePhaseOrder() throws Exception {
        JsonObject timeline = json(GENERATED.resolve("data/minecraft/timeline/moon.json"));
        JsonArray keyframes = timeline
                .getAsJsonObject("tracks")
                .getAsJsonObject("minecraft:visual/moon_phase")
                .getAsJsonArray("keyframes");
        List<String> phases = keyframes.asList().stream()
                .map(JsonElement::getAsJsonObject)
                .map(frame -> frame.get("value").getAsString())
                .toList();
        List<Integer> ticks = keyframes.asList().stream()
                .map(JsonElement::getAsJsonObject)
                .map(frame -> frame.get("ticks").getAsInt())
                .toList();

        assertAll(
                "MITE moon timeline",
                () -> assertEquals("minecraft:overworld", timeline.get("clock").getAsString()),
                () -> assertEquals(192_000, timeline.get("period_ticks").getAsInt()),
                () -> assertEquals(List.of(0, 24_000, 48_000, 72_000, 96_000, 120_000, 144_000, 168_000), ticks),
                () -> assertEquals(
                        List.of(
                                "waning_gibbous",
                                "third_quarter",
                                "waning_crescent",
                                "new_moon",
                                "waxing_crescent",
                                "first_quarter",
                                "waxing_gibbous",
                                "full_moon"),
                        phases));
    }

    @Test
    void metalAnvilsAndTheirComponentsKeepR196ResourcesAndDifficulties() throws Exception {
        JsonObject english = json(GENERATED.resolve("assets/infx/lang/en_us.json"));
        JsonObject chinese = json(GENERATED.resolve("assets/infx/lang/zh_cn.json"));
        Map<String, Float> difficulties = Map.ofEntries(
                Map.entry("copper", 12_400.0F),
                Map.entry("silver", 12_400.0F),
                Map.entry("gold", 12_400.0F),
                Map.entry("iron", 24_800.0F),
                Map.entry("ancient_metal", 49_600.0F),
                Map.entry("mithril", 198_400.0F),
                Map.entry("adamantium", 793_600.0F));
        for (var entry : difficulties.entrySet()) {
            String path = entry.getKey() + "_anvil";
            JsonObject recipe = json(GENERATED.resolve("data/infx/recipe/" + path + ".json"));
            assertAll(
                    path,
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("assets/infx/blockstates/" + path + ".json"))),
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("assets/infx/items/" + path + ".json"))),
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("data/infx/loot_table/blocks/" + path + ".json"))),
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("assets/infx/models/block/" + path + "_stage_0.json"))),
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("assets/infx/models/block/" + path + "_stage_1.json"))),
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("assets/infx/models/block/" + path + "_stage_2.json"))),
                    () -> assertTrue(english.has("block.infx." + path)),
                    () -> assertTrue(chinese.has("block.infx." + path)),
                    () -> assertEquals(entry.getValue(), recipe.get("difficulty").getAsFloat()),
                    () -> assertEquals(entry.getKey(), recipe.get("required_bench").getAsString()),
                    () -> assertEquals(
                            "[\"BBB\",\" I \",\"III\"]",
                            recipe.getAsJsonArray("pattern").toString()),
                    () -> assertEquals(
                            "infx:" + path,
                            recipe.getAsJsonObject("result").get("id").getAsString()));
        }

        Map<String, Float> storageDifficulties = Map.of(
                "silver", 3_600.0F,
                "ancient_metal", 14_400.0F,
                "mithril", 57_600.0F,
                "adamantium", 230_400.0F);
        for (var entry : storageDifficulties.entrySet()) {
            JsonObject blockRecipe = json(GENERATED.resolve(
                    "data/infx/recipe/" + entry.getKey() + "_block.json"));
            JsonObject ingotRecipe = json(GENERATED.resolve(
                    "data/infx/recipe/" + entry.getKey() + "_block_to_ingots.json"));
            assertAll(
                    entry.getKey() + " storage",
                    () -> assertEquals(entry.getValue(), blockRecipe.get("difficulty").getAsFloat()),
                    () -> assertEquals("flint", blockRecipe.get("required_bench").getAsString()),
                    () -> assertEquals(entry.getValue(), ingotRecipe.get("difficulty").getAsFloat()),
                    () -> assertEquals(9, ingotRecipe.getAsJsonObject("result").get("count").getAsInt()));
        }

        Map<String, Float> shardDifficulties = Map.of(
                "obsidian", 200.0F,
                "diamond", 1_600.0F,
                "nether_quartz", 900.0F,
                "glass", 200.0F);
        Map<String, String> shardResults = Map.of(
                "obsidian", "minecraft:obsidian",
                "diamond", "minecraft:diamond",
                "nether_quartz", "minecraft:quartz",
                "glass", "minecraft:glass_pane");
        for (var entry : shardDifficulties.entrySet()) {
            JsonObject combine = json(GENERATED.resolve(
                    "data/infx/recipe/" + entry.getKey() + "_from_shards.json"));
            JsonObject split = json(GENERATED.resolve(
                    "data/infx/recipe/" + entry.getKey() + "_to_shards.json"));
            assertAll(
                    entry.getKey() + " shards",
                    () -> assertEquals(entry.getValue(), combine.get("difficulty").getAsFloat()),
                    () -> assertEquals("flint", combine.get("required_bench").getAsString()),
                    () -> assertEquals(
                            shardResults.get(entry.getKey()),
                            combine.getAsJsonObject("result").get("id").getAsString()),
                    () -> assertEquals(entry.getValue(), split.get("difficulty").getAsFloat()),
                    () -> assertEquals(9, split.getAsJsonObject("result").get("count").getAsInt()));
        }
    }

    @Test
    void miteRecipeTableOverridesMatchTheReferenceRecipes() throws Exception {
        Map<String, String> shapedPatterns = Map.ofEntries(
                Map.entry("stick", "[\"P\",\"P\"]"),
                Map.entry("sugar_from_sugar_cane", "[\"C\"]"),
                Map.entry("flour", "[\"WWW\"]"),
                Map.entry("dough_from_water_bucket", "[\"F F\",\" W \",\"F F\"]"),
                Map.entry("flint_from_flint_chips", "[\"FF\",\"FF\"]"),
                Map.entry("pumpkin_pie", "[\"PF\",\"SE\"]"),
                Map.entry("cake", "[\"FS\",\"EM\"]"),
                Map.entry("cake_from_milk_bowl", "[\"FS\",\"EM\"]"),
                Map.entry("mushroom_stew", "[\"RB\",\"W \"]"),
                Map.entry("stone_from_cobblestone", "[\"CC\",\"CC\"]"),
                Map.entry("stone_bricks", "[\"SS\",\"SS\"]"),
                Map.entry("compass", "[\"NNN\",\"NRN\",\"NNN\"]"),
                Map.entry("clock", "[\"NNN\",\"NRN\",\"NNN\"]"),
                Map.entry("flint_and_steel", "[\"N \",\" F\"]"),
                Map.entry("glass_pane", "[\"G\"]"),
                Map.entry("bricks", "[\"BBB\",\"BSB\",\"BBB\"]"),
                Map.entry("snow", "[\"S\"]"),
                Map.entry("snow_slab", "[\"SS\",\"SS\"]"),
                Map.entry("snow_block", "[\"S\",\"S\"]"),
                Map.entry("oak_sign", "[\"W\",\"S\"]"),
                Map.entry("oak_fence", "[\"SSS\",\"SSS\"]"),
                Map.entry("ladder", "[\"S S\",\"S S\",\"S S\"]"),
                Map.entry("nether_bricks", "[\"BBB\",\"BSB\",\"BBB\"]"),
                Map.entry("saddle", "[\"LLL\",\"L L\",\"N N\"]"));
        for (var entry : shapedPatterns.entrySet()) {
            JsonObject recipe = json(GENERATED.resolve("data/infx/recipe/" + entry.getKey() + ".json"));
            assertEquals(entry.getValue(), recipe.getAsJsonArray("pattern").toString(), entry.getKey());
        }

        JsonObject dough = json(GENERATED.resolve("data/infx/recipe/dough.json"));
        JsonObject boneMeal = json(GENERATED.resolve("data/infx/recipe/bone_meal.json"));
        JsonObject stick = json(GENERATED.resolve("data/infx/recipe/stick.json"));
        JsonObject sugar = json(GENERATED.resolve("data/infx/recipe/sugar_from_sugar_cane.json"));
        JsonObject flintFromChips = json(GENERATED.resolve("data/infx/recipe/flint_from_flint_chips.json"));
        JsonObject cake = json(GENERATED.resolve("data/infx/recipe/cake.json"));
        JsonObject cakeFromMilkBowl = json(GENERATED.resolve("data/infx/recipe/cake_from_milk_bowl.json"));
        JsonObject mushroomStew = json(GENERATED.resolve("data/infx/recipe/mushroom_stew.json"));
        JsonObject snowSlab = json(GENERATED.resolve("data/infx/recipe/snow_slab.json"));
        JsonObject snowSlabModel = json(GENERATED.resolve("assets/infx/models/block/snow_slab.json"));
        JsonObject saddle = json(GENERATED.resolve("data/infx/recipe/saddle.json"));
        JsonObject english = json(GENERATED.resolve("assets/infx/lang/en_us.json"));
        JsonObject chinese = json(GENERATED.resolve("assets/infx/lang/zh_cn.json"));
        assertAll(
                "MITE recipe table",
                () -> assertEquals("infx:crafting_shapeless", dough.get("type").getAsString()),
                () -> assertEquals("infx:crafting_shaped", stick.get("type").getAsString()),
                () -> assertEquals("hand", stick.get("required_bench").getAsString()),
                () -> assertEquals(160.0F, stick.get("difficulty").getAsFloat()),
                () -> assertEquals(
                        "#minecraft:planks", stick.getAsJsonObject("key").get("P").getAsString()),
                () -> assertEquals(
                        "minecraft:stick", stick.getAsJsonObject("result").get("id").getAsString()),
                () -> assertEquals(4, stick.getAsJsonObject("result").get("count").getAsInt()),
                () -> assertEquals("infx:crafting_shapeless", boneMeal.get("type").getAsString()),
                () -> assertEquals("hand", boneMeal.get("required_bench").getAsString()),
                () -> assertEquals(100.0F, boneMeal.get("difficulty").getAsFloat()),
                () -> assertEquals(
                        "minecraft:bone",
                        boneMeal.getAsJsonArray("ingredients").get(0).getAsString()),
                () -> assertEquals(
                        "minecraft:bone_meal",
                        boneMeal.getAsJsonObject("result").get("id").getAsString()),
                () -> assertFalse(boneMeal.getAsJsonObject("result").has("count")),
                () -> assertEquals(800.0F, sugar.get("difficulty").getAsFloat()),
                () -> assertEquals("hand", sugar.get("required_bench").getAsString()),
                () -> assertEquals(
                        "minecraft:sugar_cane", sugar.getAsJsonObject("key").get("C").getAsString()),
                () -> assertEquals(
                        "minecraft:sugar", sugar.getAsJsonObject("result").get("id").getAsString()),
                () -> assertEquals(100.0F, flintFromChips.get("difficulty").getAsFloat()),
                () -> assertEquals("hand", flintFromChips.get("required_bench").getAsString()),
                () -> assertEquals(
                        "infx:flint_chip", flintFromChips.getAsJsonObject("key").get("F").getAsString()),
                () -> assertEquals(
                        "minecraft:flint", flintFromChips.getAsJsonObject("result").get("id").getAsString()),
                () -> assertEquals(
                        "#infx:milk_buckets",
                        cake.getAsJsonObject("key").get("M").getAsString()),
                () -> assertEquals(
                        "infx:milk_bowl",
                        cakeFromMilkBowl.getAsJsonObject("key").get("M").getAsString()),
                () -> assertEquals(
                        "infx:water_bowl",
                        mushroomStew.getAsJsonObject("key").get("W").getAsString()),
                () -> assertEquals(
                        "infx:snow_slab",
                        snowSlab.getAsJsonObject("result").get("id").getAsString()),
                () -> assertEquals(4, saddle.getAsJsonObject("result").get("count").getAsInt()),
                () -> assertTrue(Files.isRegularFile(
                        GENERATED.resolve("assets/infx/blockstates/snow_slab.json"))),
                () -> assertTrue(Files.isRegularFile(
                        GENERATED.resolve("assets/infx/items/snow_slab.json"))),
                () -> assertTrue(Files.isRegularFile(
                        GENERATED.resolve("assets/infx/models/block/snow_slab_top.json"))),
                () -> assertEquals(
                        "infx:block/snow_slab",
                        snowSlabModel.getAsJsonObject("textures").get("all").getAsString()),
                () -> assertTrue(Files.isRegularFile(
                        GENERATED.resolve("data/infx/loot_table/blocks/snow_slab.json"))),
                () -> assertEquals("Snow Slab", english.get("block.infx.snow_slab").getAsString()),
                () -> assertEquals("雪台阶", chinese.get("block.infx.snow_slab").getAsString()));

        for (String disabled : List.of(
                "bricks",
                "bone_meal",
                "chiseled_stone_bricks",
                "clock",
                "compass",
                "flint_and_steel",
                "glass_pane",
                "ladder",
                "melon",
                "nether_bricks",
                "oak_fence",
                "oak_sign",
                "saddle",
                "snow",
                "snow_block",
                "stick",
                "stone",
                "stone_bricks")) {
            JsonObject recipe = json(GENERATED.resolve("data/minecraft/recipe/" + disabled + ".json"));
            assertEquals(
                    "neoforge:never",
                    recipe.getAsJsonArray("neoforge:conditions")
                            .get(0)
                            .getAsJsonObject()
                            .get("type")
                            .getAsString(),
                    disabled);
        }
    }

    @Test
    void r196FurnacesHaveGeneratedAssetsRecipesLootAndTranslations() throws Exception {
        JsonObject english = json(GENERATED.resolve("assets/infx/lang/en_us.json"));
        JsonObject chinese = json(GENERATED.resolve("assets/infx/lang/zh_cn.json"));
        for (String path : List.of(
                "clay_furnace",
                "sandstone_furnace",
                "hardened_clay_furnace",
                "obsidian_furnace",
                "netherrack_furnace")) {
            assertAll(
                    path,
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("assets/infx/blockstates/" + path + ".json"))),
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("assets/infx/items/" + path + ".json"))),
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("assets/infx/models/block/" + path + ".json"))),
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("assets/infx/models/block/" + path + "_on.json"))),
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("data/infx/loot_table/blocks/" + path + ".json"))),
                    () -> assertTrue(Files.isRegularFile(
                            GENERATED.resolve("data/infx/recipe/" + path + ".json"))),
                    () -> assertTrue(english.has("block.infx." + path)),
                    () -> assertTrue(english.has("container.infx." + path)),
                    () -> assertTrue(chinese.has("block.infx." + path)),
                    () -> assertTrue(chinese.has("container.infx." + path)),
                    () -> assertEquals(
                            english.get("block.infx." + path).getAsString(),
                            english.get("container.infx." + path).getAsString(),
                            "English block and menu names must match"),
                    () -> assertEquals(
                            chinese.get("block.infx." + path).getAsString(),
                            chinese.get("container.infx." + path).getAsString(),
                            "Chinese block and menu names must match"));
        }
        for (String recipe : List.of("sand_batch", "sandstone_to_glass")) {
            assertTrue(Files.isRegularFile(GENERATED.resolve("data/infx/recipe/" + recipe + ".json")));
        }
        for (String disabled : List.of("glass", "sandstone", "smooth_sandstone")) {
            assertTrue(Files.isRegularFile(GENERATED.resolve("data/minecraft/recipe/" + disabled + ".json")));
        }

        JsonObject clay = json(GENERATED.resolve("data/infx/recipe/clay_furnace.json"));
        JsonObject sandstone = json(GENERATED.resolve("data/infx/recipe/sandstone_furnace.json"));
        JsonObject hardenedClay =
                json(GENERATED.resolve("data/infx/recipe/hardened_clay_furnace.json"));
        JsonObject obsidian = json(GENERATED.resolve("data/infx/recipe/obsidian_furnace.json"));
        JsonObject netherrack = json(GENERATED.resolve("data/infx/recipe/netherrack_furnace.json"));
        assertAll(
                "INFX furnace crafting",
                () -> assertEquals("hand", clay.get("required_bench").getAsString()),
                () -> assertEquals(320.0F, clay.get("difficulty").getAsFloat()),
                () -> assertEquals("flint", sandstone.get("required_bench").getAsString()),
                () -> assertEquals(640.0F, sandstone.get("difficulty").getAsFloat()),
                () -> assertEquals("flint", hardenedClay.get("required_bench").getAsString()),
                () -> assertEquals(1440.0F, hardenedClay.get("difficulty").getAsFloat()),
                () -> assertEquals("flint", obsidian.get("required_bench").getAsString()),
                () -> assertEquals(1920.0F, obsidian.get("difficulty").getAsFloat()),
                () -> assertEquals("flint", netherrack.get("required_bench").getAsString()),
                () -> assertEquals(1280.0F, netherrack.get("difficulty").getAsFloat()));
    }

    @Test
    void generatedCountsAreExact() throws Exception {
        // Three deepslate ore items, four replacement fish spawn eggs, the clay-golem egg, the INFX bat egg, the
        // Longdead Guardian egg, and 22 stripped-log workbench variants add one item definition each; gravel and
        // furnace blocks add item definitions, while the workbench item definitions reference their block models.
        // Leather items reference vanilla Minecraft models and do not generate InfX model files.
        // The nine carrot-on-a-stick variants add item definitions and flat models each.
        assertEquals(477, jsonCount(GENERATED.resolve("assets/infx/items")));
        assertEquals(518, jsonCount(GENERATED.resolve("assets/infx/models/item")));
        assertEquals(17, jsonCount(GENERATED.resolve("assets/infx/equipment")));
    }

    @Test
    void strippedLogWorkbenchModelsUseTheirOriginalWorkbenchTops() throws Exception {
        for (var workbench : InfXBlocks.STRIPPED_LOG_WORKBENCHES) {
            String prefix = "stripped_" + workbench.wood();
            for (String type : List.of("flint", "obsidian")) {
                JsonObject model = json(GENERATED.resolve(
                        "assets/infx/models/block/" + prefix + "_" + type + "_workbench.json"));
                assertEquals(
                        "infx:block/" + type + "_workbench_top",
                        model.getAsJsonObject("textures").get("up").getAsString());
            }
        }
    }

    @Test
    void basicWorkbenchRecipesAndModelsAreRemoved() {
        for (String path : List.of("flint_workbench", "obsidian_workbench")) {
            assertFalse(Files.exists(GENERATED.resolve("data/infx/recipe/" + path + ".json")));
            assertFalse(Files.exists(STATIC.resolve("assets/infx/blockstates/" + path + ".json")));
            assertFalse(Files.exists(STATIC.resolve("assets/infx/items/" + path + ".json")));
            assertFalse(Files.exists(STATIC.resolve("assets/infx/models/block/" + path + ".json")));
        }
    }

    private static boolean resourceExists(String relativePath) {
        return Files.isRegularFile(STATIC.resolve(relativePath))
                || Files.isRegularFile(GENERATED.resolve(relativePath));
    }

    /** The vanilla crafting table recipes removed for MITE must be restored as INFX timed recipes. */
    @Test
    void restoredVanillaRecipesExistWithMiteCounts() throws Exception {
        record Expectation(String path, String result, int count) {}
        List<Expectation> expectations = List.of(
                new Expectation("stick", "minecraft:stick", 4),
                new Expectation("bowl", "minecraft:bowl", 4),
                new Expectation("white_wool_from_string", "minecraft:white_wool", 1),
                new Expectation("raw_copper_block", "minecraft:raw_copper_block", 1),
                new Expectation("raw_copper_block_to_raw_copper", "minecraft:raw_copper", 9),
                new Expectation("raw_iron_block", "minecraft:raw_iron_block", 1),
                new Expectation("raw_iron_block_to_raw_iron", "minecraft:raw_iron", 9),
                new Expectation("raw_gold_block", "minecraft:raw_gold_block", 1),
                new Expectation("raw_gold_block_to_raw_gold", "minecraft:raw_gold", 9),
                new Expectation("beetroot_soup", "minecraft:beetroot_soup", 1),
                new Expectation("rabbit_stew", "minecraft:rabbit_stew", 1),
                new Expectation("cookie", "minecraft:cookie", 8),
                new Expectation("melon_seeds", "minecraft:melon_seeds", 1),
                new Expectation("wheat_seeds", "minecraft:wheat_seeds", 1),
                new Expectation("white_dye_from_bone_meal", "minecraft:white_dye", 1),
                new Expectation("black_dye_from_ink_sac", "minecraft:black_dye", 1),
                new Expectation("brown_dye_from_cocoa_beans", "minecraft:brown_dye", 1),
                new Expectation("red_dye_from_poppy", "minecraft:red_dye", 1),
                new Expectation("yellow_dye_from_dandelion", "minecraft:yellow_dye", 1),
                new Expectation("blue_dye_from_lapis_lazuli", "minecraft:blue_dye", 1),
                new Expectation("gray_dye", "minecraft:gray_dye", 2),
                new Expectation("light_gray_dye_from_gray_white_dye", "minecraft:light_gray_dye", 2),
                new Expectation("cyan_dye", "minecraft:cyan_dye", 2),
                new Expectation("lime_dye", "minecraft:lime_dye", 2),
                new Expectation("purple_dye", "minecraft:purple_dye", 2),
                new Expectation("magenta_dye_from_purple_and_pink", "minecraft:magenta_dye", 2),
                new Expectation("orange_dye_from_red_yellow", "minecraft:orange_dye", 2),
                new Expectation("pink_dye_from_red_white_dye", "minecraft:pink_dye", 2),
                new Expectation("light_blue_dye_from_blue_white_dye", "minecraft:light_blue_dye", 2));
        for (Expectation expectation : expectations) {
            JsonObject recipe = json(GENERATED.resolve("data/infx/recipe/" + expectation.path + ".json"));
            assertAll(
                    expectation.path,
                    () -> {
                        JsonObject result = recipe.getAsJsonObject("result");
                        assertEquals(expectation.result, result.get("id").getAsString());
                        JsonElement count = result.get("count");
                        assertEquals(expectation.count, count == null ? 1 : count.getAsInt());
                    });
        }
    }

    @Test
    void runeStonesHaveR196NuggetRecipesAndModernBypassesStayDisabled() throws Exception {
        Map<String, Map<String, Object>> runes = Map.of(
                "mithril",
                Map.of("bench", "mithril", "difficulty", 3_200.0F, "nugget", "infx:mithril_nugget"),
                "adamantium",
                Map.of("bench", "adamantium", "difficulty", 12_800.0F, "nugget", "infx:adamantium_nugget"));
        for (var entry : runes.entrySet()) {
            JsonObject recipe = json(GENERATED.resolve(
                    "data/infx/recipe/" + entry.getKey() + "_rune_stone.json"));
            assertAll(
                    entry.getKey() + " rune stone",
                    () -> assertEquals("infx:crafting_shaped", recipe.get("type").getAsString()),
                    () -> assertEquals(entry.getValue().get("bench"), recipe.get("required_bench").getAsString()),
                    () -> assertEquals(
                            (Float) entry.getValue().get("difficulty"), recipe.get("difficulty").getAsFloat()),
                    () -> assertEquals(
                            entry.getValue().get("nugget"),
                            recipe.getAsJsonObject("key").get("N").getAsString()),
                    () -> assertEquals(
                            "minecraft:obsidian",
                            recipe.getAsJsonObject("key").get("O").getAsString()),
                    () -> assertEquals(
                            "infx:" + entry.getKey() + "_rune_stone",
                            recipe.getAsJsonObject("result").get("id").getAsString()));
        }

        for (String disabled : List.of(
                "bundle",
                "blue_bundle",
                "copper_block",
                "copper_chest",
                "copper_ingot",
                "copper_ingot_from_waxed_copper_block",
                "crafter",
                "netherite_block",
                "netherite_horse_armor_smithing",
                "netherite_ingot",
                "netherite_ingot_from_netherite_block",
                "netherite_nautilus_armor_smithing",
                "netherite_scrap",
                "netherite_scrap_from_blasting",
                "netherite_upgrade_smithing_template",
                "raw_copper",
                "raw_copper_block")) {
            JsonObject recipe = json(GENERATED.resolve("data/minecraft/recipe/" + disabled + ".json"));
            assertEquals(
                    "neoforge:never",
                    recipe.getAsJsonArray("neoforge:conditions")
                            .get(0)
                            .getAsJsonObject()
                            .get("type")
                            .getAsString(),
                    disabled);
        }
    }

    @Test
    void runeStoneModelsLootAndPortalSurfaceCoverEveryR196Variant() throws Exception {
        for (String material : List.of("mithril", "adamantium")) {
            String block = material + "_rune_stone";
            JsonObject blockState = json(GENERATED.resolve("assets/infx/blockstates/" + block + ".json"));
            JsonObject variants = blockState.getAsJsonObject("variants");
            JsonObject item = json(GENERATED.resolve("assets/infx/items/" + block + ".json"))
                    .getAsJsonObject("model");
            JsonObject loot = json(GENERATED.resolve("data/infx/loot_table/blocks/" + block + ".json"));

            assertEquals(16, variants.size(), block + " block states");
            assertEquals(16, item.getAsJsonArray("cases").size(), block + " item cases");
            assertEquals("minecraft:block_state", item.get("property").getAsString());
            assertEquals("rune", item.get("block_state_property").getAsString());
            assertEquals("minecraft:copy_state", loot.getAsJsonArray("functions")
                    .get(0)
                    .getAsJsonObject()
                    .get("function")
                    .getAsString());
            assertEquals("rune", loot.getAsJsonArray("functions")
                    .get(0)
                    .getAsJsonObject()
                    .getAsJsonArray("properties")
                    .get(0)
                    .getAsString());

            for (int rune = 0; rune < 16; rune++) {
                assertTrue(variants.has("rune=" + rune), block + " rune=" + rune);
                JsonObject model = json(GENERATED.resolve(
                        "assets/infx/models/block/" + block + "_" + rune + ".json"));
                JsonObject textures = model.getAsJsonObject("textures");
                assertEquals("infx:block/runestones/" + material + "/" + rune,
                        textures.get("side").getAsString());
                assertEquals("minecraft:block/obsidian", textures.get("top").getAsString());
                assertEquals("minecraft:block/obsidian", textures.get("bottom").getAsString());
            }
        }

        JsonObject portal = json(GENERATED.resolve("assets/infx/blockstates/underworld_portal.json"))
                .getAsJsonObject("variants");
        assertEquals("infx:block/underworld_portal_ns",
                portal.getAsJsonObject("axis=x,rune_gate=false").get("model").getAsString());
        assertEquals("infx:block/underworld_portal_ew",
                portal.getAsJsonObject("axis=z,rune_gate=false").get("model").getAsString());
        for (String orientation : List.of("ns", "ew")) {
            JsonObject model = json(GENERATED.resolve(
                    "assets/infx/models/block/underworld_portal_" + orientation + ".json"));
            JsonObject textures = model.getAsJsonObject("textures");
            JsonObject template = json(STATIC.resolve(
                    "assets/infx/models/block/template_tinted_portal_" + orientation + ".json"));
            assertEquals("minecraft:block/nether_portal", textures.get("portal").getAsString());
            assertEquals("minecraft:block/nether_portal", textures.get("particle").getAsString());
            assertEquals("infx:block/template_tinted_portal_" + orientation,
                    model.get("parent").getAsString());
            template.getAsJsonArray("elements")
                    .get(0)
                    .getAsJsonObject()
                    .getAsJsonObject("faces")
                    .entrySet()
                    .forEach(face -> assertEquals(
                            0,
                            face.getValue().getAsJsonObject().get("tintindex").getAsInt(),
                            orientation + " underworld portal face " + face.getKey()
                                    + " must use the destination tint source"));
        }
        assertEquals("infx:block/underworld_portal_runegate_ns",
                portal.getAsJsonObject("axis=x,rune_gate=true").get("model").getAsString());
        assertEquals("infx:block/underworld_portal_runegate_ew",
                portal.getAsJsonObject("axis=z,rune_gate=true").get("model").getAsString());
        for (String orientation : List.of("ns", "ew")) {
            JsonObject model = json(GENERATED.resolve(
                    "assets/infx/models/block/underworld_portal_runegate_" + orientation + ".json"));
            JsonObject textures = model.getAsJsonObject("textures");
            assertEquals("infx:block/runegate", textures.get("portal").getAsString());
            assertEquals("infx:block/runegate", textures.get("particle").getAsString());
            assertEquals("infx:block/template_runegate_portal_" + orientation,
                    model.get("parent").getAsString());
        }
        JsonObject netherPortal = json(GENERATED.resolve("assets/infx/blockstates/nether_portal.json"))
                .getAsJsonObject("variants");
        assertEquals("infx:block/nether_portal_ns",
                netherPortal.getAsJsonObject("axis=x").get("model").getAsString());
        assertEquals("infx:block/nether_portal_ew",
                netherPortal.getAsJsonObject("axis=z").get("model").getAsString());

        JsonObject returnSpawnPortal = json(GENERATED.resolve("assets/infx/blockstates/return_spawn_portal.json"))
                .getAsJsonObject("variants");
        assertEquals("infx:block/underworld_portal_runegate_ns",
                returnSpawnPortal.getAsJsonObject("axis=x").get("model").getAsString());
        assertEquals("infx:block/underworld_portal_runegate_ew",
                returnSpawnPortal.getAsJsonObject("axis=z").get("model").getAsString());
    }

    @Test
    void runegatePortalTemplatesUseDimensionTintSource() throws Exception {
        for (String orientation : List.of("ns", "ew")) {
            JsonObject template = json(STATIC.resolve(
                    "assets/infx/models/block/template_runegate_portal_" + orientation + ".json"));
            template.getAsJsonArray("elements")
                    .get(0)
                    .getAsJsonObject()
                    .getAsJsonObject("faces")
                    .entrySet()
                    .forEach(face -> assertEquals(
                            0,
                            face.getValue().getAsJsonObject().get("tintindex").getAsInt(),
                            orientation + " rune-gate face " + face.getKey() + " must use the dimension tint source"));
        }
    }

    @Test
    void advancementGraphMatchesTwentyFiveStageLineNodes() throws Exception {
        Map<String, String> parents = Map.ofEntries(
                Map.entry("flint_kit", "first_steps"),
                Map.entry("flint_workbench", "flint_kit"),
                Map.entry("first_furnace", "first_steps"),
                Map.entry("copper_workbench", "flint_workbench"),
                Map.entry("iron_age", "copper_workbench"),
                Map.entry("obsidian_furnace", "first_furnace"),
                Map.entry("ancient_metal_age", "iron_age"),
                Map.entry("mithril_age", "ancient_metal_age"),
                Map.entry("adamantium_age", "mithril_age"),
                Map.entry("masterwork", "adamantium_age"),
                Map.entry("leather_armor", "flint_workbench"),
                Map.entry("metal_armor", "copper_workbench"),
                Map.entry("adamantium_armor", "adamantium_age"),
                Map.entry("farming", "copper_workbench"),
                Map.entry("food", "farming"),
                Map.entry("enchanting", "iron_age"),
                Map.entry("bookcase", "enchanting"),
                Map.entry("enlightenment", "bookcase"),
                Map.entry("underworld", "obsidian_furnace"),
                Map.entry("nether", "underworld"),
                Map.entry("nether_forge", "nether"),
                Map.entry("rune_gate", "underworld"),
                Map.entry("the_end", "nether_forge"),
                Map.entry("the_end2", "the_end"));
        Set<String> challenges = Set.of(
                "adamantium_armor",
                "enlightenment",
                "rune_gate",
                "the_end",
                "the_end2");
        Path root = GENERATED.resolve("data/infx/advancement/progression");
        List<Path> files;
        try (Stream<Path> stream = Files.list(root)) {
            files = stream.filter(path -> path.toString().endsWith(".json")).toList();
        }
        assertEquals(25, files.size());
        assertEquals(24, parents.size());

        JsonObject english = json(GENERATED.resolve("assets/infx/lang/en_us.json"));
        JsonObject chinese = json(GENERATED.resolve("assets/infx/lang/zh_cn.json"));
        Set<String> actualNames = new HashSet<>();
        Set<String> actualChallenges = new HashSet<>();
        for (Path file : files) {
            String name = file.getFileName().toString().replaceFirst("\\.json$", "");
            actualNames.add(name);
            JsonObject advancement = json(file);
            if (name.equals("first_steps")) {
                assertFalse(advancement.has("parent"));
            } else {
                assertEquals("infx:progression/" + parents.get(name), advancement.get("parent").getAsString(), name);
            }
            JsonObject display = advancement.getAsJsonObject("display");
            if (display.has("frame") && display.get("frame").getAsString().equals("challenge")) {
                actualChallenges.add(name);
            }
            assertTrue(english.has("advancements.infx." + name + ".title"), name);
            assertTrue(english.has("advancements.infx." + name + ".description"), name);
            assertTrue(chinese.has("advancements.infx." + name + ".title"), name);
            assertTrue(chinese.has("advancements.infx." + name + ".description"), name);
        }
        assertEquals(parents.keySet(), actualNames.stream()
                .filter(name -> !name.equals("first_steps"))
                .collect(Collectors.toSet()));
        assertEquals(challenges, actualChallenges);

        JsonObject enchanting = json(root.resolve("enchanting.json"));
        var alternatives = enchanting.getAsJsonArray("requirements").get(0).getAsJsonArray();
        assertEquals(2, alternatives.size());
        assertTrue(alternatives.toString().contains("emerald_enchanting_table"));
        assertTrue(alternatives.toString().contains("diamond_enchanting_table"));

        JsonObject workbench = json(root.resolve("flint_workbench.json"));
        assertTrue(workbench.getAsJsonObject("criteria").has("crafted_stripped_oak_flint_bench"));
        assertTrue(workbench.getAsJsonObject("criteria").has("crafted_stripped_oak_obsidian_bench"));
        assertEquals(22, workbench.getAsJsonObject("criteria").size());
        JsonObject ironAge = json(root.resolve("iron_age.json"));
        assertEquals(3, ironAge.getAsJsonObject("criteria").size());
        String mixedArmor = Files.readString(root.resolve("metal_armor.json"), UTF_8);
        assertTrue(mixedArmor.contains("infx:copper_chainmail_helmet"));
        assertTrue(mixedArmor.contains("infx:mithril_chainmail_boots"));
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
        for (Catalog.EquipmentEntry entry : InfXItems.catalog().equipmentEntries()) {
            if (entry.key().type() == EquipmentType.BOW) {
                String definition = Files.readString(
                        GENERATED.resolve("assets/infx/items/" + entry.path() + ".json"), UTF_8);
                for (var material : EquipmentType.ARROW.allowedMaterials()) {
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
            } else if (entry.key().type() == EquipmentType.FISHING_ROD) {
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
        for (Catalog.EquipmentEntry entry : InfXItems.catalog().equipmentEntries()) {
            var form = entry.key().type().armorForm();
            if (form == EquipmentType.ArmorForm.NONE) {
                continue;
            }
            String assetPath = entry.key().equipmentAsset().identifier().getPath();
            JsonObject layers = json(GENERATED.resolve("assets/infx/equipment/" + assetPath + ".json"))
                    .getAsJsonObject("layers");
            if (form == EquipmentType.ArmorForm.HORSE) {
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
    void leatherItemModelsReuseVanillaModels() throws Exception {
        for (String piece : List.of("helmet", "chestplate", "leggings", "boots")) {
            String itemPath = "leather_" + piece;
            JsonObject itemDefinition = json(GENERATED.resolve("assets/infx/items/" + itemPath + ".json"));
            JsonObject model = itemDefinition.getAsJsonObject("model");
            JsonObject tint = model.getAsJsonArray("tints").get(0).getAsJsonObject();
            assertAll(
                    itemPath,
                    () -> assertEquals("minecraft:model", model.get("type").getAsString()),
                    () -> assertEquals("minecraft:item/" + itemPath, model.get("model").getAsString()),
                    () -> assertEquals("minecraft:dye", tint.get("type").getAsString()),
                    () -> assertEquals(-6265536, tint.get("default").getAsInt()),
                    () -> assertFalse(Files.exists(GENERATED.resolve("assets/infx/models/item/" + itemPath + ".json"))));
        }
    }

    @Test
    void manifestHasOnlyCatalogOrApprovedDerivedTextures() throws Exception {
        Set<String> destinations = new HashSet<>();
        for (String line : Files.readAllLines(STATIC.resolve("assets/infx/infx_texture_manifest.tsv"), UTF_8)
                .stream()
                .skip(1)
                .toList()) {
            destinations.add(line.split("\t", -1)[2]);
        }
        for (Catalog.Entry entry : InfXItems.catalog().entries()) {
            assertTrue(destinations.remove("textures/item/" + entry.path() + ".png"), entry.path());
        }
        assertTrue(destinations.remove("textures/item/fishing_rod_cast.png"));
        assertTrue(destinations.remove("textures/item/carrot_on_a_stick.png"));
        assertTrue(destinations.removeIf(path -> path.matches(
                "textures/item/(wood|ancient_metal|mithril)_bow/(flint|obsidian|copper|silver|gold|rusted_iron|iron|ancient_metal|mithril|adamantium)_[0-2]\\.png")));
        assertTrue(destinations.removeIf(
                path -> path.matches("textures/item/leather_(helmet|chestplate|leggings|boots)_overlay\\.png")));
        assertTrue(destinations.removeIf(path -> path.startsWith("textures/entity/equipment/")));
        assertTrue(destinations.removeIf(path -> path.matches(
                "textures/block/(flint|obsidian)_workbench_top\\.png"
                        + "|textures/block/(copper|silver|gold|iron|ancient_metal|mithril|adamantium)_workbench_(front|side)\\.png")));
        assertTrue(destinations.removeIf(path -> path.matches(
                "textures/block/(clay|hardened_clay|sandstone|obsidian|netherrack)_furnace_(front|front_on|side|top)\\.png")));
        assertTrue(destinations.remove("textures/block/silver_ore.png"));
        assertTrue(destinations.remove("textures/block/deepslate_silver_ore.png"));
        assertTrue(destinations.remove("textures/block/mithril_ore.png"));
        assertTrue(destinations.remove("textures/block/deepslate_mithril_ore.png"));
        assertTrue(destinations.remove("textures/block/adamantium_ore.png"));
        assertTrue(destinations.remove("textures/block/deepslate_adamantium_ore.png"));
        assertTrue(destinations.removeIf(path -> path.matches("textures/block/blueberry_bush(_picked)?\\.png")));
        assertTrue(destinations.removeIf(path -> path.matches(
                "textures/block/crops/(wheat/(?:[0-7]|blighted/[0-7]|dead/[0-6])"
                        + "|carrots/(?:[0-3]|blighted/[0-3]|dead/[0-2])"
                        + "|potatoes/(?:[0-3]|blighted/[0-3]|dead/[0-2])"
                        + "|beetroot/(?:[0-3]|blighted/[0-3]|dead/[0-3])"
                        + "|onions/(?:[0-4]|blighted/[0-4]|dead/[0-3]))\\.png")));
        assertTrue(destinations.removeIf(path -> path.matches(
                "textures/block/emerald_enchanting_table_(side|top)\\.png")));
        assertTrue(destinations.remove("textures/block/sgravel.png"));
        assertTrue(destinations.remove("textures/block/snow_slab.png"));
        assertTrue(destinations.remove("textures/block/mantle.png"));
        assertTrue(destinations.remove("textures/block/mantle.png.mcmeta"));
        assertTrue(destinations.removeIf(path -> path.matches("textures/environment/celestial/moon_(halo|ring)\\.png")));
        assertTrue(destinations.removeIf(path -> path.matches(
                "textures/block/(silver|ancient_metal|mithril|adamantium)_block\\.png")));
        assertTrue(destinations.removeIf(path -> path.matches(
                "textures/block/anvil/(copper|silver|gold|iron|ancient_metal|mithril|adamantium)/(base|top_damaged_[0-2])\\.png")));
        assertTrue(destinations.removeIf(path -> path.matches(
                "textures/entity/chest/(copper|silver|gold|iron|ancient_metal|mithril|adamantium)\\.png")));
        assertTrue(destinations.removeIf(path -> path.matches(
                "textures/block/runestones/(mithril|adamantium)/(0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15)\\.png")));
        assertTrue(destinations.remove("textures/block/runegate.png"));
        assertTrue(destinations.remove("textures/block/runegate.png.mcmeta"));
        assertTrue(destinations.remove("textures/block/nether_portal.png"));
        assertTrue(destinations.remove("textures/block/nether_portal.png.mcmeta"));
        assertTrue(destinations.removeIf(path -> path.matches(
                "textures/item/(flour|water_bowl|dough|salad|blueberries|blueberry_porridge|milk_bowl|cereal_porridge"
                        + "|chocolate|pumpkin_soup|cream_of_mushroom_soup|onion|vegetable_soup"
                        + "|cream_of_vegetable_soup|chicken_soup|beef_stew|orange|fruit_ice|cheese"
                        + "|mashed_potato|ice_cream|banana|worm|cooked_worm)\\.png")));
        assertTrue(destinations.removeIf(path -> path.matches(
                "textures/item/(cod|salmon|pufferfish|tropical|axolotl|tadpole|powder_snow)_"
                        + "(copper|silver|gold|iron|ancient_metal|mithril|adamantium)_bucket\\.png")));
        assertTrue(destinations.removeIf(path -> path.matches(
                "textures/item/gelatinous_sphere/(green|ochre|crimson|gray|black)\\.png")));
        assertTrue(destinations.removeIf(path -> path.matches(
                "textures/entity/slime/(slime|jelly|blob|ooze|pudding|magmacube)\\.png")));
        assertTrue(destinations.removeIf(path -> path.matches(
                "textures/entity/(ghoul|shadow|wight)(_baby)?\\.png"
                        + "|textures/entity/(fire_elemental|blaze)\\.png"
                        + "|textures/entity/zombie/(revenant(_baby)?|zombie_villager)\\.png"
                        + "|textures/entity/zombie_pigman(_baby)?\\.png"
                        + "|textures/entity/ghast/(ghast|ghast_shooting)\\.png"
                        + "|textures/entity/skeleton/(longdead|longdead_guardian|bone_lord)\\.png"
                        + "|textures/entity/spider/(spider|black_widow|cave_spider|demon_spider|wood_spider|phase_spider)\\.png"
                        + "|textures/entity/creeper/infernal_creeper\\.png"
                        + "|textures/entity/earth_elemental/(earth_elemental(_magma)?_glow"
                        + "|clay/earth_elemental_clay(_hardened)?"
                        + "|end_stone/earth_elemental_end_stone(_magma)?"
                        + "|netherrack/earth_elemental_netherrack(_magma)?"
                        + "|obsidian/earth_elemental_obsidian(_magma)?"
                        + "|stone/earth_elemental_stone(_magma)?)\\.png"
                        + "|textures/entity/silverfish/(netherspawn|copperspine|hoary)\\.png"
                        + "|textures/entity/bat(?:/(vampire|nightwing))?\\.png"
                        + "|textures/entity/hellhound/hellhound\\.png"
                        + "|textures/entity/dire_wolf/(angry|neutral|tame)\\.png")));
        // Sick livestock skins derived from the 26.2 healthy variants (see sickTextureFor).
        assertTrue(destinations.removeIf(path -> path.matches(
                "textures/entity/(cow|pig|chicken)/(cow|pig|chicken)_(temperate|warm|cold)_sick(_baby)?\\.png"
                        + "|textures/entity/sheep/sheep_sick(_baby)?\\.png")));
        assertTrue(destinations.remove("textures/mob_effect/malnutrition.png"));
        assertTrue(destinations.remove("textures/mob_effect/insulin_resistance.png"));
        assertTrue(destinations.remove("textures/mob_effect/witch_curse.png"));
        assertTrue(destinations.removeIf(path -> path.endsWith("_spawn_egg.png")));
        assertTrue(destinations.isEmpty(), () -> "unexpected selected textures " + destinations);
        assertFalse(Files.exists(STATIC.resolve("assets/minecraft")));
        assertFalse(Files.exists(GENERATED.resolve("assets/minecraft")));
    }

    @Test
    void safeAndFoodModelsReferenceTheirImportedMiteTextures() throws Exception {
        for (String material : List.of("copper", "silver", "gold", "iron", "ancient_metal", "mithril", "adamantium")) {
            JsonObject model = json(GENERATED.resolve("assets/infx/models/block/" + material + "_safe.json"));
            JsonObject itemModel = json(GENERATED.resolve("assets/infx/items/" + material + "_safe.json"));
            assertAll(
                    material + " safe",
                    () -> assertTrue(
                            model.has("textures")
                                    && model.getAsJsonObject("textures").has("particle"),
                            "safe block model is particle-only; world uses chest BER"),
                    () -> assertEquals(
                            "minecraft:special",
                            itemModel.getAsJsonObject("model").get("type").getAsString()),
                    () -> assertEquals(
                            "infx:safe",
                            itemModel
                                    .getAsJsonObject("model")
                                    .getAsJsonObject("model")
                                    .get("type")
                                    .getAsString()),
                    () -> assertEquals(
                            "infx:" + material,
                            itemModel
                                    .getAsJsonObject("model")
                                    .getAsJsonObject("model")
                                    .get("texture")
                                    .getAsString()),
                    () -> assertTrue(Files.isRegularFile(
                            STATIC.resolve("assets/infx/textures/entity/chest/" + material + ".png"))));
        }
        for (String food : List.of(
                "flour",
                "water_bowl",
                "dough",
                "salad",
                "blueberries",
                "blueberry_porridge",
                "milk_bowl",
                "cereal_porridge",
                "chocolate",
                "pumpkin_soup",
                "cream_of_mushroom_soup",
                "onion",
                "vegetable_soup",
                "cream_of_vegetable_soup",
                "chicken_soup",
                "beef_stew",
                "orange",
                "fruit_ice",
                "cheese",
                "mashed_potato",
                "ice_cream",
                "banana",
                "worm",
                "cooked_worm")) {
            JsonObject model = json(GENERATED.resolve("assets/infx/models/item/" + food + ".json"));
            assertEquals(
                    "infx:item/" + food,
                    model.getAsJsonObject("textures").get("layer0").getAsString(),
                    food);
        }
    }

    @Test
    void miteHarvestLevelsAndEffectiveToolsCoverR196AndModern262Families() throws Exception {
        Map<Integer, Set<String>> levels = new java.util.LinkedHashMap<>();
        Set<String> directAssignments = new HashSet<>();
        for (int level = 0; level <= 6; level++) {
            Set<String> values = tagValues("requires_harvest_level/" + level);
            levels.put(level, values);
            for (String value : values) {
                if (!value.startsWith("#")) {
                    assertTrue(directAssignments.add(value), value + " is directly assigned to multiple levels");
                }
            }
        }

        assertAll(
                "MITE and 26.2 level representatives",
                () -> assertTrue(levels.get(0).contains("minecraft:coal_block")),
                () -> assertTrue(levels.get(0).contains("#minecraft:rails")),
                () -> assertTrue(levels.get(0).contains("minecraft:infested_stone")),
                () -> assertTrue(levels.get(0).contains("infx:sandstone_furnace")),
                () -> assertTrue(levels.get(1).contains("#c:glass_blocks")),
                () -> assertTrue(levels.get(1).contains("#minecraft:terracotta")),
                () -> assertTrue(levels.get(1).contains("#c:sandstone/slabs")),
                () -> assertTrue(levels.get(1).contains("infx:sgravel")),
                () -> assertFalse(levels.get(1).contains("#c:sandstone/stairs")),
                () -> assertTrue(levels.get(2).contains("infx:silver_ore")),
                () -> assertTrue(levels.get(3).contains("minecraft:copper_bulb")),
                () -> assertTrue(levels.get(3).contains("minecraft:waxed_oxidized_cut_copper_stairs")),
                () -> assertTrue(levels.get(3).contains("minecraft:redstone_block")),
                () -> assertTrue(levels.get(3).contains("infx:mithril_ore")),
                () -> assertTrue(levels.get(3).contains("infx:mithril_rune_stone")),
                () -> assertTrue(levels.get(3).contains("infx:adamantium_rune_stone")),
                () -> assertTrue(levels.get(4).contains("#c:ores/diamond")),
                () -> assertTrue(levels.get(4).contains("infx:ancient_metal_block")),
                () -> assertTrue(levels.get(5).contains("infx:mithril_block")),
                () -> assertTrue(levels.get(6).contains("infx:adamantium_block")));

        Set<String> pickaxe = tagValues("effective_tool/pickaxe");
        Set<String> axe = tagValues("effective_tool/axe");
        Set<String> shovel = tagValues("effective_tool/shovel");
        Set<String> vanillaShovel = tagValuesAt("minecraft", "block/mineable/shovel");
        Set<String> hoe = tagValues("effective_tool/hoe");
        Set<String> cudgel = tagValues("effective_tool/cudgel");
        Set<String> sword = tagValues("effective_tool/sword");
        Set<String> shears = tagValues("effective_tool/shears");
        Set<String> axeHalfSpeed = tagValues("effective_tool/axe_half_speed");
        Set<String> portable = tagValues("portable_hand_harvest");
        assertAll(
                "MITE effective-tool and portability tags",
                () -> assertTrue(pickaxe.contains("#minecraft:mineable/pickaxe")),
                () -> assertTrue(pickaxe.contains("#c:glass_blocks")),
                () -> assertTrue(pickaxe.contains("#minecraft:flower_pots")),
                () -> assertTrue(pickaxe.contains("minecraft:glowstone")),
                () -> assertTrue(axe.contains("#c:sandstone/blocks")),
                () -> assertTrue(axe.contains("#c:sandstone/slabs")),
                () -> assertFalse(axe.contains("#c:sandstone/stairs")),
                () -> assertTrue(axe.contains("#minecraft:terracotta")),
                () -> assertTrue(shovel.contains("#c:glass_panes")),
                () -> assertTrue(vanillaShovel.contains("infx:sgravel")),
                () -> assertTrue(shovel.contains("infx:infested_netherrack")),
                () -> assertTrue(hoe.contains("#minecraft:mineable/shovel")),
                () -> assertTrue(hoe.contains("infx:sandstone_furnace")),
                () -> assertTrue(cudgel.contains("minecraft:glowstone")),
                () -> assertTrue(cudgel.contains("#minecraft:coral_blocks")),
                () -> assertTrue(sword.contains("minecraft:hay_block")),
                () -> assertTrue(shears.contains("minecraft:nether_wart")),
                () -> assertTrue(shears.contains("infx:blueberry_bush")),
                () -> assertTrue(axeHalfSpeed.contains("#c:sandstone/blocks")),
                () -> assertFalse(axeHalfSpeed.contains("#c:sandstone/slabs")),
                () -> assertTrue(portable.contains("minecraft:furnace")),
                () -> assertTrue(portable.contains("#minecraft:anvil")),
                () -> assertTrue(portable.contains("infx:adamantium_safe")));
    }

    private static Set<String> tagValues(String path) throws IOException {
        return tagValuesAt("infx", "block/" + path);
    }

    private static Set<String> tagValuesAt(String namespace, String path) throws IOException {
        JsonObject tag = json(GENERATED.resolve("data/" + namespace + "/tags/" + path + ".json"));
        return tag.getAsJsonArray("values").asList().stream()
                .map(value -> value.isJsonObject()
                        ? value.getAsJsonObject().get("id").getAsString()
                        : value.getAsString())
                .collect(Collectors.toSet());
    }

    private static void assertOverworldOre(String ore, OverworldOreExpectation expected) throws IOException {
        JsonObject configured = json(GENERATED.resolve(
                "data/infx/worldgen/configured_feature/overworld_" + ore + "_ore.json"));
        JsonObject config = configured.getAsJsonObject("config");
        JsonArray targets = config.getAsJsonArray("targets");
        JsonObject stoneTarget = targets.get(0).getAsJsonObject();
        JsonObject deepslateTarget = targets.get(1).getAsJsonObject();
        JsonObject placed = json(GENERATED.resolve(
                "data/infx/worldgen/placed_feature/overworld_" + ore + "_ore.json"));
        JsonArray placement = placed.getAsJsonArray("placement");
        JsonObject frequency = placement.get(0).getAsJsonObject();
        JsonObject height = placement.get(2).getAsJsonObject().getAsJsonObject("height");
        String frequencyField = expected.frequencyType().equals("minecraft:count") ? "count" : "chance";
        assertAll(
                ore,
                () -> assertEquals("minecraft:ore", configured.get("type").getAsString()),
                () -> assertEquals(expected.size(), config.get("size").getAsInt()),
                () -> assertEquals(0.0F, config.get("discard_chance_on_air_exposure").getAsFloat()),
                () -> assertEquals(2, targets.size()),
                () -> assertEquals(expected.stoneState(), stoneTarget.getAsJsonObject("state")
                        .get("Name")
                        .getAsString()),
                () -> assertEquals("minecraft:stone_ore_replaceables", stoneTarget.getAsJsonObject("target")
                        .get("tag")
                        .getAsString()),
                () -> assertEquals(expected.deepslateState(), deepslateTarget.getAsJsonObject("state")
                        .get("Name")
                        .getAsString()),
                () -> assertEquals("minecraft:deepslate_ore_replaceables", deepslateTarget.getAsJsonObject("target")
                        .get("tag")
                        .getAsString()),
                () -> assertEquals("infx:overworld_" + ore + "_ore", placed.get("feature").getAsString()),
                () -> assertEquals(expected.frequencyType(), frequency.get("type").getAsString()),
                () -> assertEquals(expected.frequency(), frequency.get(frequencyField).getAsInt()),
                () -> assertEquals("minecraft:in_square", placement.get(1).getAsJsonObject()
                        .get("type")
                        .getAsString()),
                () -> assertEquals("minecraft:height_range", placement.get(2).getAsJsonObject()
                        .get("type")
                        .getAsString()),
                () -> assertEquals(expected.heightType(), height.get("type").getAsString()),
                () -> assertEquals(expected.minY(), height.getAsJsonObject("min_inclusive")
                        .get("absolute")
                        .getAsInt()),
                () -> assertEquals(expected.maxY(), height.getAsJsonObject("max_inclusive")
                        .get("absolute")
                        .getAsInt()),
                () -> assertEquals("minecraft:biome", placement.get(3).getAsJsonObject()
                        .get("type")
                        .getAsString()));
    }

    private static void assertUnderworldOre(String ore, UnderworldOreExpectation expected) throws IOException {
        JsonObject configured = json(GENERATED.resolve(
                "data/infx/worldgen/configured_feature/underworld_" + ore + ".json"));
        JsonObject config = configured.getAsJsonObject("config");
        JsonArray targets = config.getAsJsonArray("targets");
        JsonObject stoneTarget = targets.get(0).getAsJsonObject();
        JsonObject deepslateTarget = targets.get(1).getAsJsonObject();
        assertAll(
                ore,
                () -> assertEquals(expected.featureType(), configured.get("type").getAsString()),
                () -> assertEquals(expected.size(), config.get("size").getAsInt()),
                () -> assertEquals(0.0F, config.get("discard_chance_on_air_exposure").getAsFloat()),
                () -> assertEquals(2, targets.size()),
                () -> assertEquals(expected.stoneState(), stoneTarget.getAsJsonObject("state")
                        .get("Name")
                        .getAsString()),
                () -> assertEquals("minecraft:block_match", stoneTarget.getAsJsonObject("target")
                        .get("predicate_type")
                        .getAsString()),
                () -> assertEquals("minecraft:stone", stoneTarget.getAsJsonObject("target")
                        .get("block")
                        .getAsString()),
                () -> assertEquals(expected.deepslateState(), deepslateTarget.getAsJsonObject("state")
                        .get("Name")
                        .getAsString()),
                () -> assertEquals("minecraft:block_match", deepslateTarget.getAsJsonObject("target")
                        .get("predicate_type")
                        .getAsString()),
                () -> assertEquals("minecraft:deepslate", deepslateTarget.getAsJsonObject("target")
                        .get("block")
                        .getAsString()));
        assertUnderworldOrePlacement(ore, "low", expected.lowCount(), Underworld.MIN_Y, Underworld.ORE_LOW_MAX_Y_INCLUSIVE,
                expected.size());
        assertUnderworldOrePlacement(ore, "full", expected.fullCount(), Underworld.MIN_Y, Underworld.ORE_MAX_Y_INCLUSIVE,
                expected.size());
    }

    private static void assertUnderworldOrePlacement(
            String ore, String range, int count, int minY, int maxY, int size) throws IOException {
        JsonObject placed = json(GENERATED.resolve(
                "data/infx/worldgen/placed_feature/underworld_" + ore + "_" + range + ".json"));
        JsonArray placement = placed.getAsJsonArray("placement");
        JsonObject frequency = placement.get(0).getAsJsonObject();
        JsonObject height = placement.get(2).getAsJsonObject().getAsJsonObject("height");
        JsonObject configured = json(GENERATED.resolve(
                "data/infx/worldgen/configured_feature/underworld_" + ore + ".json"));
        assertAll(
                ore + " " + range,
                () -> assertEquals("infx:underworld_" + ore, placed.get("feature").getAsString()),
                () -> assertEquals(4, placement.size()),
                () -> assertEquals("minecraft:count", frequency.get("type").getAsString()),
                () -> assertEquals(count, frequency.get("count").getAsInt()),
                () -> assertEquals("minecraft:in_square", placement.get(1).getAsJsonObject()
                        .get("type")
                        .getAsString()),
                () -> assertEquals("minecraft:height_range", placement.get(2).getAsJsonObject()
                        .get("type")
                        .getAsString()),
                () -> assertEquals("minecraft:uniform", height.get("type").getAsString()),
                () -> assertEquals(minY, height.getAsJsonObject("min_inclusive")
                        .get("absolute")
                        .getAsInt()),
                () -> assertEquals(maxY, height.getAsJsonObject("max_inclusive")
                        .get("absolute")
                        .getAsInt()),
                () -> assertEquals("minecraft:biome", placement.get(3).getAsJsonObject()
                        .get("type")
                        .getAsString()),
                () -> assertEquals(size, configured.getAsJsonObject("config").get("size").getAsInt()));
    }

    private record UnderworldOreExpectation(
            String featureType,
            int size,
            int lowCount,
            int fullCount,
            String stoneState,
            String deepslateState) {}

    private record OverworldOreExpectation(
            int size,
            String stoneState,
            String deepslateState,
            String frequencyType,
            int frequency,
            String heightType,
            int minY,
            int maxY) {}

    private static int spawnCount(JsonObject entry, String preferred, String fallback) {
        JsonElement value = entry.get(preferred);
        if (value == null) value = entry.get(fallback);
        return value.getAsInt();
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

    private static int intProviderValue(JsonElement provider) {
        if (provider.isJsonPrimitive()) {
            return provider.getAsInt();
        }
        return provider.getAsJsonObject().get("value").getAsInt();
    }

    private static JsonArray featureStep(JsonObject biome, GenerationStep.Decoration step) {
        JsonArray features = biome.getAsJsonArray("features");
        return step.ordinal() < features.size() ? features.get(step.ordinal()).getAsJsonArray() : new JsonArray();
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

    private static JsonObject objectWithType(JsonElement element, String type) {
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("type") && type.equals(object.get("type").getAsString())) {
                return object;
            }
            for (var entry : object.entrySet()) {
                try {
                    return objectWithType(entry.getValue(), type);
                } catch (IllegalArgumentException ignored) {
                    // Continue through the remaining branches.
                }
            }
        } else if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                try {
                    return objectWithType(child, type);
                } catch (IllegalArgumentException ignored) {
                    // Continue through the remaining elements.
                }
            }
        }
        throw new IllegalArgumentException("No object with type " + type);
    }

    private static boolean hasGradient(
            JsonElement element, int fromY, int toY, double fromValue, double toValue) {
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("type")
                    && "minecraft:y_clamped_gradient".equals(object.get("type").getAsString())
                    && object.get("from_y").getAsInt() == fromY
                    && object.get("to_y").getAsInt() == toY
                    && Double.compare(object.get("from_value").getAsDouble(), fromValue) == 0
                    && Double.compare(object.get("to_value").getAsDouble(), toValue) == 0) {
                return true;
            }
            return object.entrySet().stream()
                    .anyMatch(entry -> hasGradient(entry.getValue(), fromY, toY, fromValue, toValue));
        }
        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                if (hasGradient(child, fromY, toY, fromValue, toValue)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void assertMiteUnderworldProfile(JsonObject terrain) {
        for (int sample = 0; sample < MITE_UNDERWORLD_PROFILE.length - 1; sample++) {
            int fromY = 120 + sample * 8;
            int toY = fromY + 8;
            assertTrue(
                    hasGradient(
                            terrain,
                            fromY,
                            toY,
                            MITE_UNDERWORLD_PROFILE[sample],
                            MITE_UNDERWORLD_PROFILE[sample + 1]),
                    "Missing MITE Underworld profile segment from Y=" + fromY + " to Y=" + toY);
        }
    }

    private static List<String> formatSpecifiers(String translation) {
        return FORMAT_SPECIFIER.matcher(translation).results().map(MatchResult::group).toList();
    }

    private static void assertLanguageKey(JsonObject english, JsonObject chinese, String key) {
        assertAll(
                key,
                () -> assertTrue(english.has(key), "missing en_us"),
                () -> assertTrue(chinese.has(key), "missing zh_cn"));
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
