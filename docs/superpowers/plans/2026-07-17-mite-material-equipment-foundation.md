# MITE Material and Equipment Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Register and render the complete approved R196 catalog of 33 raw materials and 204 equipment items, with exact baseline formulas, modern item behavior, stable lookup APIs, translations, tags, and auditable MITE textures.

**Architecture:** Immutable `R196Material`, `R196RawItem`, `R196EquipmentType`, and `R196EquipmentKey` definitions form the sole content catalog. `R196Catalog` registers that catalog through NeoForge deferred holders, while focused item subclasses adapt modern tool actions, shearing, fishing, bows, and arrows without allowing vanilla constructors to overwrite R196 components. Catalog-driven data providers generate tags, language, item definitions, models, and equipment assets; a committed texture manifest and generated-resource test suite make packaging fail closed.

**Tech Stack:** Java 25, Minecraft 26.2, NeoForge 26.2.0.21-beta, NeoForge ModDev 2.0.141, JUnit Jupiter 5.13.4, Minecraft data components and item-model dispatch, Gradle data generation and GameTest.

---

## Scope boundary

This plan implements milestone 1 from `docs/superpowers/specs/2026-07-17-mite-material-equipment-foundation-design.md`. It intentionally does not add ores, world generation, mob drops, metallurgy, recipes, repair execution, coin exchange, manure fertilization, silver damage, corrosion, projectile recovery odds, reinforced-bow velocity, fixed-point armor, or adamantium lava immunity. The catalog stores the metadata needed by those later milestones.

The catalog must finish with exactly:

- 33 mod-owned raw-material items;
- 96 tools;
- 33 weapons and ammunition items;
- 68 player-armor pieces;
- 7 horse-armors;
- 237 catalog-owned items total, plus the two existing workbench block items outside the catalog.

The only approved texture-source exception is `wood_shovel.png`: the resource-pack overlay does not contain that inherited vanilla bitmap, so its byte-identical R196 base asset comes from `codex/reference/mite-src/assets`. The provenance manifest records that distinct source root. All other catalog bitmaps come from `codex/reference/mite- resource-pack/assets`.

## File map

### Create

- `src/main/java/com/pixulse/infx/material/R196Quality.java`: ordered R196 quality ceiling values.
- `src/main/java/com/pixulse/infx/material/R196Material.java`: immutable material profiles, flags, repair identity, and material formulas.
- `src/main/java/com/pixulse/infx/material/R196RawItem.java`: the exact 33 raw-item definitions and coin XP metadata.
- `src/main/java/com/pixulse/infx/item/R196EquipmentCategory.java`: catalog category ordering.
- `src/main/java/com/pixulse/infx/item/R196MiningFamily.java`: block tags and component rules for each mining family.
- `src/main/java/com/pixulse/infx/item/R196UseAction.java`: delegation to modern axe, shovel, and hoe interactions.
- `src/main/java/com/pixulse/infx/item/R196EquipmentType.java`: the approved equipment matrix and type behavior values.
- `src/main/java/com/pixulse/infx/item/R196EquipmentKey.java`: validated `(material, type)` keys and all per-item formulas.
- `src/main/java/com/pixulse/infx/item/R196Catalog.java`: ordered definitions, deferred-holder registration, reverse lookup, and fail-fast accessors.
- `src/main/java/com/pixulse/infx/item/R196ItemProperties.java`: direct data-component construction for tools, bows, arrows, armor, and horse armor.
- `src/main/java/com/pixulse/infx/item/R196ShearsItem.java`: standard shearing plus R196 hardness wear.
- `src/main/java/com/pixulse/infx/item/R196FishingRodItem.java`: standard cast/retrieve behavior with catalog identity.
- `src/main/java/com/pixulse/infx/item/R196BowItem.java`: standard bow behavior and synchronized nocked-arrow material state.
- `src/main/java/com/pixulse/infx/item/R196ArrowItem.java`: material arrow entity creation, damage, dispensing, and pickup identity.
- `src/main/java/com/pixulse/infx/registry/ModDataComponents.java`: the persistent/networked `nocked_arrow_material` String component.
- `src/main/java/com/pixulse/infx/data/ModLanguageProvider.java`: English and Simplified Chinese generated language.
- `src/main/java/com/pixulse/infx/data/ModModelProvider.java`: ordinary, fishing-rod, and material-arrow bow models.
- `src/main/java/com/pixulse/infx/data/ModEquipmentAssetProvider.java`: humanoid, leggings, baby, and horse equipment layers.
- `src/main/java/com/pixulse/infx/gametest/ModEquipmentGameTests.java`: representative runtime behavior and regression GameTests.
- `src/test/resources/r196/catalog-paths.txt`: independent golden list of all 237 catalog IDs.
- `src/test/java/com/pixulse/infx/material/R196MaterialTest.java`: exact source profile and material-formula tests.
- `src/test/java/com/pixulse/infx/material/R196RawItemTest.java`: raw count, ID, name, material, and coin-value tests.
- `src/test/java/com/pixulse/infx/item/R196EquipmentTypeTest.java`: allowed matrices, category counts, type values, and exclusions.
- `src/test/java/com/pixulse/infx/item/R196EquipmentKeyTest.java`: durability, mining, melee, wear, bow, arrow, fishing, and armor formulas.
- `src/test/java/com/pixulse/infx/item/R196CatalogTest.java`: golden manifest, uniqueness, reverse lookups, aliases, and ownership.
- `src/test/java/com/pixulse/infx/client/R196GeneratedResourceTest.java`: generated model, language, equipment, texture, and packaging contracts.
- `src/test/java/com/pixulse/infx/client/R196TextureProvenanceTest.java`: committed PNG hash, dimensions, source comparison, and namespace rules.
- `scripts/sync-mite-equipment-textures.sh`: deterministic selected-asset copier and SHA-256 manifest writer.
- `src/main/resources/assets/infx/mite_texture_manifest.tsv`: source-to-destination provenance for exactly 393 PNG destinations.
- 384 additional selected PNG destinations below `src/main/resources/assets/infx/textures` (nine selected catalog textures already exist).

### Modify

- `src/main/java/com/pixulse/infx/InfiniteX.java`: register the data component and equipment GameTests.
- `src/main/java/com/pixulse/infx/item/R196ToolItem.java`: replace the one-rate final class with catalog-driven mining, use, and wear behavior.
- `src/main/java/com/pixulse/infx/registry/ModItems.java`: register the catalog and retain the nine existing public aliases.
- `src/main/java/com/pixulse/infx/registry/ModCreativeTabs.java`: emit deterministic catalog order rather than deferred-register order.
- `src/main/java/com/pixulse/infx/tag/ModTags.java`: add material, repair-material, and equipment-type tag factories.
- `src/main/java/com/pixulse/infx/data/ModDataGenerators.java`: register language, model, and equipment providers.
- `src/main/java/com/pixulse/infx/data/ModItemTagsProvider.java`: generate catalog-driven material, repair, type, arrow, and harvest tags.
- `THIRD_PARTY_NOTICES.md`: replace the twelve-file notice with the complete selected-asset and approval statement.
- `build.gradle`: add a generated-resource JUnit suite that is excluded from the initial unit-test pass.
- `.github/workflows/build.yml`: run data generation in a separate Gradle invocation before the build.

### Delete after replacement

- `src/main/java/com/pixulse/infx/registry/ModToolProperties.java`: superseded by catalog-driven `R196ItemProperties`.
- `src/main/resources/assets/infx/lang/en_us.json`
- `src/main/resources/assets/infx/lang/zh_cn.json`
- the nine catalog item definitions in `src/main/resources/assets/infx/items`: `flint_chip`, `sinew`, `obsidian_shard`, `emerald_shard`, `silver_nugget`, `mithril_nugget`, `adamantium_nugget`, `flint_hatchet`, and `copper_pickaxe`;
- the same nine static item models in `src/main/resources/assets/infx/models/item`.

The two workbench item definitions and both workbench block models remain static in `src/main/resources`.

### Task 1: Lock the twelve R196 material profiles

**Files:**

- Create: `src/main/java/com/pixulse/infx/material/R196Quality.java`
- Create: `src/main/java/com/pixulse/infx/material/R196Material.java`
- Test: `src/test/java/com/pixulse/infx/material/R196MaterialTest.java`

- [ ] **Step 1: Write the failing quality and profile tests**

Create `R196MaterialTest.java` with table-driven assertions for every source value and every flag. The table is authoritative and must be literal test data, not values read back from the enum:

```java
package com.pixulse.infx.material;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.harvest.HarvestTier;
import java.util.List;
import org.junit.jupiter.api.Test;

class R196MaterialTest {
    private record Expected(
            R196Material material,
            String path,
            float durability,
            int enchantability,
            R196Quality quality,
            float damage,
            float efficiency,
            HarvestTier tier,
            float plateProtection,
            float horseProtection) {}

    private static final List<Expected> EXPECTED = List.of(
            new Expected(R196Material.LEATHER, "leather", 1, 10, R196Quality.FINE, 0, 0, null, 2, 0),
            new Expected(R196Material.WOOD, "wood", .5F, 10, R196Quality.FINE, 0, 1, null, 0, 0),
            new Expected(R196Material.FLINT, "flint", 1, 0, R196Quality.FINE, 1, 1.25F, HarvestTier.FLINT, 0, 0),
            new Expected(R196Material.OBSIDIAN, "obsidian", 2, 0, R196Quality.FINE, 2, 1.5F, HarvestTier.COPPER, 0, 0),
            new Expected(R196Material.GOLD, "gold", 4, 50, R196Quality.SUPERB, 2, 1.75F, HarvestTier.COPPER, 6, 3),
            new Expected(R196Material.COPPER, "copper", 4, 30, R196Quality.EXCELLENT, 3, 1.75F, HarvestTier.COPPER, 7, 4),
            new Expected(R196Material.SILVER, "silver", 4, 30, R196Quality.EXCELLENT, 3, 1.75F, HarvestTier.COPPER, 7, 4),
            new Expected(R196Material.RUSTED_IRON, "rusted_iron", 4, 0, R196Quality.POOR, 2, 1.25F, HarvestTier.COPPER, 6, 0),
            new Expected(R196Material.IRON, "iron", 8, 30, R196Quality.MASTERWORK, 4, 2, HarvestTier.IRON, 8, 5),
            new Expected(R196Material.ANCIENT_METAL, "ancient_metal", 16, 40, R196Quality.MASTERWORK, 4, 2, HarvestTier.ANCIENT_METAL, 8, 5),
            new Expected(R196Material.MITHRIL, "mithril", 64, 100, R196Quality.LEGENDARY, 5, 2.5F, HarvestTier.MITHRIL, 9, 6),
            new Expected(R196Material.ADAMANTIUM, "adamantium", 256, 40, R196Quality.LEGENDARY, 6, 3, HarvestTier.ADAMANTIUM, 10, 7));

    @Test
    void profilesMatchR196() {
        assertEquals(12, R196Material.values().length);
        for (Expected expected : EXPECTED) {
            R196Material actual = expected.material();
            assertAll(actual.path(),
                    () -> assertEquals(expected.path(), actual.path()),
                    () -> assertEquals(expected.durability(), actual.durabilityMultiplier()),
                    () -> assertEquals(expected.enchantability(), actual.enchantability()),
                    () -> assertEquals(expected.quality(), actual.maximumQuality()),
                    () -> assertEquals(expected.damage(), actual.materialDamage()),
                    () -> assertEquals(expected.efficiency(), actual.harvestEfficiency()),
                    () -> assertEquals(expected.tier(), actual.harvestTier().orElse(null)),
                    () -> assertEquals(expected.plateProtection(), actual.plateProtection()),
                    () -> assertEquals(expected.horseProtection(), actual.horseProtection()));
        }
    }

    @Test
    void flagsAndRepairPeersAreExplicit() {
        assertTrue(R196Material.FLINT.has(R196Material.Flag.ROCKY));
        assertTrue(R196Material.OBSIDIAN.has(R196Material.Flag.ROCKY));
        assertTrue(R196Material.SILVER.has(R196Material.Flag.SILVER));
        assertTrue(R196Material.RUSTED_IRON.has(R196Material.Flag.RUSTED));
        assertTrue(R196Material.ADAMANTIUM.has(R196Material.Flag.LAVA_SAFE));
        assertTrue(R196Material.COPPER.has(R196Material.Flag.METAL));
        assertFalse(R196Material.WOOD.has(R196Material.Flag.METAL));
        assertEquals("iron", R196Material.RUSTED_IRON.repairMaterialPath());
        assertEquals("flint", R196Material.FLINT.repairMaterialPath());
    }

    @Test
    void materialFormulasCoverLowMiddleAndHighTiers() {
        assertEquals(400, R196Material.FLINT.toolDurability(1));
        assertEquals(9_600, R196Material.IRON.toolDurability(3));
        assertEquals(512_000, R196Material.ADAMANTIUM.toolDurability(5));
        assertEquals(2.5F, R196Material.FLINT.miningSpeed(.5F));
        assertEquals(8.0F, R196Material.IRON.miningSpeed(1.0F));
        assertEquals(12.0F, R196Material.ADAMANTIUM.miningSpeed(1.0F));
        assertEquals(3.0F, R196Material.FLINT.meleeDamage(2.0F));
        assertEquals(6.0F, R196Material.IRON.meleeDamage(2.0F));
        assertEquals(8.0F, R196Material.ADAMANTIUM.meleeDamage(2.0F));
    }
}
```

- [ ] **Step 2: Run the focused test and verify the red state**

Run:

```bash
bash gradlew test --tests com.pixulse.infx.material.R196MaterialTest
```

Expected: compilation fails because `R196Quality` and `R196Material` do not exist.

- [ ] **Step 3: Add the quality enum and material enum**

`R196Quality` must contain, in ascending order:

```java
package com.pixulse.infx.material;

public enum R196Quality {
    POOR,
    FINE,
    EXCELLENT,
    SUPERB,
    MASTERWORK,
    LEGENDARY
}
```

Implement `R196Material` as an enum in this exact creative order:

```java
LEATHER, WOOD, FLINT, OBSIDIAN, GOLD, COPPER, SILVER,
RUSTED_IRON, IRON, ANCIENT_METAL, MITHRIL, ADAMANTIUM
```

Use these fields and helpers; enum constants use the literal values from the failing test:

```java
private final String path;
private final String englishNoun;
private final String englishEquipmentPrefix;
private final String chinesePrefix;
private final float durabilityMultiplier;
private final int enchantability;
private final R196Quality maximumQuality;
private final float materialDamage;
private final float harvestEfficiency;
private final Optional<HarvestTier> harvestTier;
private final TagKey<Block> incorrectForDrops;
private final String repairMaterialPath;
private final float plateProtection;
private final float horseProtection;
private final Set<Flag> flags;

public enum Flag { METAL, ROCKY, SILVER, RUSTED, LAVA_SAFE }

public int toolDurability(int components) {
    return (int) (4.0F * components * durabilityMultiplier * 100.0F);
}

public float miningSpeed(float typeMultiplier) {
    return 4.0F * typeMultiplier * harvestEfficiency;
}

public float meleeDamage(float typeBaseDamage) {
    return typeBaseDamage + materialDamage;
}
```

Use `BlockTags.INCORRECT_FOR_WOODEN_TOOL` for wood and flint, `INCORRECT_FOR_COPPER_TOOL` for obsidian/gold/copper/silver/rusted iron, `INCORRECT_FOR_IRON_TOOL` for iron, and `INCORRECT_FOR_NETHERITE_TOOL` for ancient metal/mithril/adamantium. Leather retains the wooden tag but never appears in a mining-tool combination. `repairMaterialPath` is `leather`, `wood`, `flint`, `obsidian`, the material path for every normal metal, and `iron` for rusted iron.

Flag sets are exact: flint and obsidian are `ROCKY`; gold, copper, silver, rusted iron, iron, ancient metal, mithril, and adamantium are `METAL`; silver additionally has `SILVER`; rusted iron additionally has `RUSTED`; adamantium additionally has `LAVA_SAFE`; leather and wood have no flags.

English noun/prefix pairs are `Wood/Wooden` and `Gold/Golden`; every other material uses the same noun and equipment prefix. Chinese prefixes are `皮革`, `木`, `燧石`, `黑曜石`, `金`, `铜`, `银`, `锈铁`, `铁`, `远古金属`, `秘银`, and `艾德曼`.

- [ ] **Step 4: Re-run the focused test**

Run the same command. Expected: all three tests pass and Gradle reports `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit the material profiles**

```bash
git add src/main/java/com/pixulse/infx/material src/test/java/com/pixulse/infx/material/R196MaterialTest.java
git commit -m "feat: define R196 material profiles"
```

### Task 2: Define all 33 raw-material items

**Files:**

- Create: `src/main/java/com/pixulse/infx/material/R196RawItem.java`
- Test: `src/test/java/com/pixulse/infx/material/R196RawItemTest.java`

- [ ] **Step 1: Write the failing raw catalog test**

Create a test that independently pins the exact IDs and coin values:

```java
package com.pixulse.infx.material;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class R196RawItemTest {
    private static final List<String> PATHS = List.of(
            "flint_chip", "obsidian_shard", "emerald_shard", "diamond_shard", "nether_quartz_shard", "glass_shard",
            "sinew", "manure",
            "silver_nugget", "mithril_nugget", "adamantium_nugget", "ancient_metal_nugget",
            "silver_ingot", "mithril_ingot", "adamantium_ingot", "ancient_metal_ingot",
            "copper_chain", "silver_chain", "gold_chain", "rusted_iron_chain", "iron_chain", "ancient_metal_chain", "mithril_chain", "adamantium_chain",
            "copper_coin", "silver_coin", "gold_coin", "ancient_metal_coin", "mithril_coin", "adamantium_coin",
            "creeper_frags", "infernal_creeper_frags", "netherspawn_frags");

    @Test
    void rawCatalogHasExactApprovedIds() {
        assertEquals(PATHS, List.of(R196RawItem.values()).stream().map(R196RawItem::path).toList());
        assertEquals(33, R196RawItem.values().length);
        Set<String> unique = List.of(R196RawItem.values()).stream().map(R196RawItem::path).collect(Collectors.toSet());
        assertEquals(33, unique.size());
    }

    @Test
    void coinXpIsStoredWithoutImplementingExchange() {
        assertEquals(5, R196RawItem.COPPER_COIN.coinXp());
        assertEquals(25, R196RawItem.SILVER_COIN.coinXp());
        assertEquals(100, R196RawItem.GOLD_COIN.coinXp());
        assertEquals(500, R196RawItem.ANCIENT_METAL_COIN.coinXp());
        assertEquals(2_500, R196RawItem.MITHRIL_COIN.coinXp());
        assertEquals(10_000, R196RawItem.ADAMANTIUM_COIN.coinXp());
        assertTrue(R196RawItem.FLINT_CHIP.material().contains(R196Material.FLINT));
        assertTrue(R196RawItem.CREEPER_FRAGS.material().isEmpty());
    }
}
```

- [ ] **Step 2: Verify the test fails to compile**

Run:

```bash
bash gradlew test --tests com.pixulse.infx.material.R196RawItemTest
```

Expected: compilation fails because `R196RawItem` does not exist.

- [ ] **Step 3: Implement the raw enum with literal bilingual names**

Use the following immutable shape:

```java
public enum R196RawItem {
    FLINT_CHIP("flint_chip", "Flint Chip", "燧石碎片", Kind.SHARD, R196Material.FLINT, 0),
    OBSIDIAN_SHARD("obsidian_shard", "Obsidian Shard", "黑曜石碎片", Kind.SHARD, R196Material.OBSIDIAN, 0),
    EMERALD_SHARD("emerald_shard", "Emerald Shard", "绿宝石碎片", Kind.SHARD, null, 0),
    DIAMOND_SHARD("diamond_shard", "Diamond Shard", "钻石碎片", Kind.SHARD, null, 0),
    NETHER_QUARTZ_SHARD("nether_quartz_shard", "Nether Quartz Shard", "下界石英碎片", Kind.SHARD, null, 0),
    GLASS_SHARD("glass_shard", "Glass Shard", "玻璃碎片", Kind.SHARD, null, 0),
    SINEW("sinew", "Sinew", "皮筋", Kind.BINDING, null, 0),
    MANURE("manure", "Manure", "肥料", Kind.FERTILIZER, null, 0),
    SILVER_NUGGET("silver_nugget", "Silver Nugget", "银粒", Kind.NUGGET, R196Material.SILVER, 0),
    MITHRIL_NUGGET("mithril_nugget", "Mithril Nugget", "秘银粒", Kind.NUGGET, R196Material.MITHRIL, 0),
    ADAMANTIUM_NUGGET("adamantium_nugget", "Adamantium Nugget", "艾德曼粒", Kind.NUGGET, R196Material.ADAMANTIUM, 0),
    ANCIENT_METAL_NUGGET("ancient_metal_nugget", "Ancient Metal Nugget", "远古金属粒", Kind.NUGGET, R196Material.ANCIENT_METAL, 0),
    SILVER_INGOT("silver_ingot", "Silver Ingot", "银锭", Kind.INGOT, R196Material.SILVER, 0),
    MITHRIL_INGOT("mithril_ingot", "Mithril Ingot", "秘银锭", Kind.INGOT, R196Material.MITHRIL, 0),
    ADAMANTIUM_INGOT("adamantium_ingot", "Adamantium Ingot", "艾德曼锭", Kind.INGOT, R196Material.ADAMANTIUM, 0),
    ANCIENT_METAL_INGOT("ancient_metal_ingot", "Ancient Metal Ingot", "远古金属锭", Kind.INGOT, R196Material.ANCIENT_METAL, 0),
    COPPER_CHAIN("copper_chain", "Copper Chain", "铜锁链", Kind.CHAIN, R196Material.COPPER, 0),
    SILVER_CHAIN("silver_chain", "Silver Chain", "银锁链", Kind.CHAIN, R196Material.SILVER, 0),
    GOLD_CHAIN("gold_chain", "Golden Chain", "金锁链", Kind.CHAIN, R196Material.GOLD, 0),
    RUSTED_IRON_CHAIN("rusted_iron_chain", "Rusted Iron Chain", "锈铁链", Kind.CHAIN, R196Material.RUSTED_IRON, 0),
    IRON_CHAIN("iron_chain", "Iron Chain", "铁锁链", Kind.CHAIN, R196Material.IRON, 0),
    ANCIENT_METAL_CHAIN("ancient_metal_chain", "Ancient Metal Chain", "远古金属锁链", Kind.CHAIN, R196Material.ANCIENT_METAL, 0),
    MITHRIL_CHAIN("mithril_chain", "Mithril Chain", "秘银锁链", Kind.CHAIN, R196Material.MITHRIL, 0),
    ADAMANTIUM_CHAIN("adamantium_chain", "Adamantium Chain", "艾德曼锁链", Kind.CHAIN, R196Material.ADAMANTIUM, 0),
    COPPER_COIN("copper_coin", "Copper Coin", "铜币", Kind.COIN, R196Material.COPPER, 5),
    SILVER_COIN("silver_coin", "Silver Coin", "银币", Kind.COIN, R196Material.SILVER, 25),
    GOLD_COIN("gold_coin", "Gold Coin", "金币", Kind.COIN, R196Material.GOLD, 100),
    ANCIENT_METAL_COIN("ancient_metal_coin", "Ancient Metal Coin", "远古金属币", Kind.COIN, R196Material.ANCIENT_METAL, 500),
    MITHRIL_COIN("mithril_coin", "Mithril Coin", "秘银币", Kind.COIN, R196Material.MITHRIL, 2_500),
    ADAMANTIUM_COIN("adamantium_coin", "Adamantium Coin", "艾德曼币", Kind.COIN, R196Material.ADAMANTIUM, 10_000),
    CREEPER_FRAGS("creeper_frags", "Creeper Frags", "苦力怕碎片", Kind.MONSTER_FRAG, null, 0),
    INFERNAL_CREEPER_FRAGS("infernal_creeper_frags", "Infernal Creeper Frags", "地狱苦力怕碎片", Kind.MONSTER_FRAG, null, 0),
    NETHERSPAWN_FRAGS("netherspawn_frags", "Netherspawn Frags", "下界虫碎片", Kind.MONSTER_FRAG, null, 0);

    public enum Kind { SHARD, BINDING, FERTILIZER, NUGGET, INGOT, CHAIN, COIN, MONSTER_FRAG }

    private final String path;
    private final String englishName;
    private final String chineseName;
    private final Kind kind;
    private final Optional<R196Material> material;
    private final int coinXp;

    R196RawItem(String path, String englishName, String chineseName, Kind kind,
            R196Material material, int coinXp) {
        this.path = path;
        this.englishName = englishName;
        this.chineseName = chineseName;
        this.kind = kind;
        this.material = Optional.ofNullable(material);
        this.coinXp = coinXp;
    }

    public String path() { return path; }
    public String englishName() { return englishName; }
    public String chineseName() { return chineseName; }
    public Kind kind() { return kind; }
    public Optional<R196Material> material() { return material; }
    public int coinXp() { return coinXp; }

    public String translationKey() {
        return "item.infx." + path;
    }
}
```

Use these exact names:

| Path | English | Simplified Chinese |
| --- | --- | --- |
| `flint_chip` | Flint Chip | 燧石碎片 |
| `obsidian_shard` | Obsidian Shard | 黑曜石碎片 |
| `emerald_shard` | Emerald Shard | 绿宝石碎片 |
| `diamond_shard` | Diamond Shard | 钻石碎片 |
| `nether_quartz_shard` | Nether Quartz Shard | 下界石英碎片 |
| `glass_shard` | Glass Shard | 玻璃碎片 |
| `sinew` | Sinew | 皮筋 |
| `manure` | Manure | 肥料 |
| `silver_nugget`, `mithril_nugget`, `adamantium_nugget`, `ancient_metal_nugget` | Silver/Mithril/Adamantium/Ancient Metal Nugget | 银/秘银/艾德曼/远古金属粒 |
| `silver_ingot`, `mithril_ingot`, `adamantium_ingot`, `ancient_metal_ingot` | Silver/Mithril/Adamantium/Ancient Metal Ingot | 银/秘银/艾德曼/远古金属锭 |
| eight chains | Copper, Silver, Golden, Rusted Iron, Iron, Ancient Metal, Mithril, Adamantium Chain | 铜锁链、银锁链、金锁链、锈铁链、铁锁链、远古金属锁链、秘银锁链、艾德曼锁链 |
| six coins | Copper, Silver, Gold, Ancient Metal, Mithril, Adamantium Coin | 铜币、银币、金币、远古金属币、秘银币、艾德曼币 |
| `creeper_frags` | Creeper Frags | 苦力怕碎片 |
| `infernal_creeper_frags` | Infernal Creeper Frags | 地狱苦力怕碎片 |
| `netherspawn_frags` | Netherspawn Frags | 下界虫碎片 |

Associate shards with FLINT or OBSIDIAN only when those two equipment materials exist. Associate every nugget, ingot, chain, and coin with its material. All other definitions expose `Optional.empty()`. Only coin constants have non-zero `coinXp`.

- [ ] **Step 4: Run both material test classes**

```bash
bash gradlew test --tests 'com.pixulse.infx.material.*'
```

Expected: five tests pass.

- [ ] **Step 5: Commit the raw definitions**

```bash
git add src/main/java/com/pixulse/infx/material/R196RawItem.java src/test/java/com/pixulse/infx/material/R196RawItemTest.java
git commit -m "feat: define R196 raw material catalog"
```

### Task 3: Lock the equipment matrix and golden catalog

**Files:**

- Create: `src/main/java/com/pixulse/infx/item/R196EquipmentCategory.java`
- Create: `src/main/java/com/pixulse/infx/item/R196MiningFamily.java`
- Create: `src/main/java/com/pixulse/infx/item/R196UseAction.java`
- Create: `src/main/java/com/pixulse/infx/item/R196EquipmentType.java`
- Create: `src/main/java/com/pixulse/infx/item/R196EquipmentKey.java`
- Create: `src/test/resources/r196/catalog-paths.txt`
- Test: `src/test/java/com/pixulse/infx/item/R196EquipmentTypeTest.java`
- Test: `src/test/java/com/pixulse/infx/item/R196EquipmentKeyTest.java`

- [ ] **Step 1: Add the independent 237-ID golden file**

Create `src/test/resources/r196/catalog-paths.txt` with exactly these lines and no comments:

```text
flint_chip
obsidian_shard
emerald_shard
diamond_shard
nether_quartz_shard
glass_shard
sinew
manure
silver_nugget
mithril_nugget
adamantium_nugget
ancient_metal_nugget
silver_ingot
mithril_ingot
adamantium_ingot
ancient_metal_ingot
copper_chain
silver_chain
gold_chain
rusted_iron_chain
iron_chain
ancient_metal_chain
mithril_chain
adamantium_chain
copper_coin
silver_coin
gold_coin
ancient_metal_coin
mithril_coin
adamantium_coin
creeper_frags
infernal_creeper_frags
netherspawn_frags
leather_helmet
leather_chestplate
leather_leggings
leather_boots
wood_shovel
wood_cudgel
wood_club
wood_bow
flint_shovel
flint_hatchet
flint_axe
flint_fishing_rod
flint_knife
flint_arrow
obsidian_shovel
obsidian_hatchet
obsidian_axe
obsidian_fishing_rod
obsidian_knife
obsidian_arrow
gold_pickaxe
gold_shovel
gold_hatchet
gold_axe
gold_hoe
gold_mattock
gold_battle_axe
gold_war_hammer
gold_scythe
gold_shears
gold_fishing_rod
gold_sword
gold_dagger
gold_arrow
gold_helmet
gold_chestplate
gold_leggings
gold_boots
gold_chainmail_helmet
gold_chainmail_chestplate
gold_chainmail_leggings
gold_chainmail_boots
gold_horse_armor
copper_pickaxe
copper_shovel
copper_hatchet
copper_axe
copper_hoe
copper_mattock
copper_battle_axe
copper_war_hammer
copper_scythe
copper_shears
copper_fishing_rod
copper_sword
copper_dagger
copper_arrow
copper_helmet
copper_chestplate
copper_leggings
copper_boots
copper_chainmail_helmet
copper_chainmail_chestplate
copper_chainmail_leggings
copper_chainmail_boots
copper_horse_armor
silver_pickaxe
silver_shovel
silver_hatchet
silver_axe
silver_hoe
silver_mattock
silver_battle_axe
silver_war_hammer
silver_scythe
silver_shears
silver_fishing_rod
silver_sword
silver_dagger
silver_arrow
silver_helmet
silver_chestplate
silver_leggings
silver_boots
silver_chainmail_helmet
silver_chainmail_chestplate
silver_chainmail_leggings
silver_chainmail_boots
silver_horse_armor
rusted_iron_pickaxe
rusted_iron_shovel
rusted_iron_hatchet
rusted_iron_axe
rusted_iron_hoe
rusted_iron_mattock
rusted_iron_battle_axe
rusted_iron_war_hammer
rusted_iron_scythe
rusted_iron_shears
rusted_iron_sword
rusted_iron_dagger
rusted_iron_arrow
rusted_iron_helmet
rusted_iron_chestplate
rusted_iron_leggings
rusted_iron_boots
rusted_iron_chainmail_helmet
rusted_iron_chainmail_chestplate
rusted_iron_chainmail_leggings
rusted_iron_chainmail_boots
iron_pickaxe
iron_shovel
iron_hatchet
iron_axe
iron_hoe
iron_mattock
iron_battle_axe
iron_war_hammer
iron_scythe
iron_shears
iron_fishing_rod
iron_sword
iron_dagger
iron_arrow
iron_helmet
iron_chestplate
iron_leggings
iron_boots
iron_chainmail_helmet
iron_chainmail_chestplate
iron_chainmail_leggings
iron_chainmail_boots
iron_horse_armor
ancient_metal_pickaxe
ancient_metal_shovel
ancient_metal_hatchet
ancient_metal_axe
ancient_metal_hoe
ancient_metal_mattock
ancient_metal_battle_axe
ancient_metal_war_hammer
ancient_metal_scythe
ancient_metal_shears
ancient_metal_fishing_rod
ancient_metal_sword
ancient_metal_dagger
ancient_metal_bow
ancient_metal_arrow
ancient_metal_helmet
ancient_metal_chestplate
ancient_metal_leggings
ancient_metal_boots
ancient_metal_chainmail_helmet
ancient_metal_chainmail_chestplate
ancient_metal_chainmail_leggings
ancient_metal_chainmail_boots
ancient_metal_horse_armor
mithril_pickaxe
mithril_shovel
mithril_hatchet
mithril_axe
mithril_hoe
mithril_mattock
mithril_battle_axe
mithril_war_hammer
mithril_scythe
mithril_shears
mithril_fishing_rod
mithril_sword
mithril_dagger
mithril_bow
mithril_arrow
mithril_helmet
mithril_chestplate
mithril_leggings
mithril_boots
mithril_chainmail_helmet
mithril_chainmail_chestplate
mithril_chainmail_leggings
mithril_chainmail_boots
mithril_horse_armor
adamantium_pickaxe
adamantium_shovel
adamantium_hatchet
adamantium_axe
adamantium_hoe
adamantium_mattock
adamantium_battle_axe
adamantium_war_hammer
adamantium_scythe
adamantium_shears
adamantium_fishing_rod
adamantium_sword
adamantium_dagger
adamantium_arrow
adamantium_helmet
adamantium_chestplate
adamantium_leggings
adamantium_boots
adamantium_chainmail_helmet
adamantium_chainmail_chestplate
adamantium_chainmail_leggings
adamantium_chainmail_boots
adamantium_horse_armor
```

- [ ] **Step 2: Write failing matrix and exclusion tests**

Create `R196EquipmentTypeTest.java`. Keep the allowed sets literal so a defect in the production matrix cannot reproduce itself in the assertion:

```java
package com.pixulse.infx.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.material.R196Material;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class R196EquipmentTypeTest {
    private static final Set<R196Material> METALS = EnumSet.of(
            R196Material.COPPER, R196Material.SILVER, R196Material.GOLD, R196Material.RUSTED_IRON,
            R196Material.IRON, R196Material.ANCIENT_METAL, R196Material.MITHRIL, R196Material.ADAMANTIUM);

    @Test
    void matrixHasExactCategoryCounts() {
        Map<R196EquipmentCategory, Long> counts = R196EquipmentKey.all().stream()
                .collect(java.util.stream.Collectors.groupingBy(key -> key.type().category(), java.util.stream.Collectors.counting()));
        assertEquals(96L, counts.get(R196EquipmentCategory.TOOL));
        assertEquals(33L, counts.get(R196EquipmentCategory.WEAPON));
        assertEquals(36L, counts.get(R196EquipmentCategory.PLATE_ARMOR));
        assertEquals(32L, counts.get(R196EquipmentCategory.CHAIN_ARMOR));
        assertEquals(7L, counts.get(R196EquipmentCategory.HORSE_ARMOR));
        assertEquals(204, R196EquipmentKey.all().size());
    }

    @Test
    void representativeAllowedSetsMatchTheApprovedMatrix() {
        assertEquals(METALS, R196EquipmentType.PICKAXE.allowedMaterials());
        assertEquals(EnumSet.of(R196Material.FLINT, R196Material.OBSIDIAN), R196EquipmentType.KNIFE.allowedMaterials());
        assertEquals(EnumSet.of(R196Material.WOOD, R196Material.ANCIENT_METAL, R196Material.MITHRIL), R196EquipmentType.BOW.allowedMaterials());
        assertEquals(EnumSet.of(R196Material.LEATHER, R196Material.COPPER, R196Material.SILVER, R196Material.GOLD,
                R196Material.RUSTED_IRON, R196Material.IRON, R196Material.ANCIENT_METAL, R196Material.MITHRIL,
                R196Material.ADAMANTIUM), R196EquipmentType.HELMET.allowedMaterials());
        assertFalse(R196EquipmentType.HORSE_ARMOR.allows(R196Material.RUSTED_IRON));
        assertFalse(R196EquipmentType.FISHING_ROD.allows(R196Material.RUSTED_IRON));
    }

    @Test
    void illegalKeysFailWithTheOffendingCombination() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> new R196EquipmentKey(R196Material.WOOD, R196EquipmentType.PICKAXE));
        assertTrue(error.getMessage().contains("wood_pickaxe"));
    }

    @Test
    void excludedArtifactsCannotBeRepresented() {
        Set<String> paths = R196EquipmentKey.all().stream().map(R196EquipmentKey::path).collect(java.util.stream.Collectors.toSet());
        assertFalse(paths.contains("iron_knife"));
        assertFalse(paths.contains("stone_dagger"));
        assertFalse(paths.contains("chip_flint_knife"));
        assertFalse(paths.stream().anyMatch(path -> path.startsWith("diamond_")));
        assertFalse(paths.stream().anyMatch(path -> path.contains("carrot_on_a_stick")));
        assertEquals(204, paths.size());
    }

    @Test
    void specialtyFactoriesAreDeclaredByType() {
        assertEquals(R196EquipmentType.FactoryKind.SHEARS, R196EquipmentType.SHEARS.factoryKind());
        assertEquals(R196EquipmentType.FactoryKind.FISHING_ROD, R196EquipmentType.FISHING_ROD.factoryKind());
        assertEquals(R196EquipmentType.FactoryKind.BOW, R196EquipmentType.BOW.factoryKind());
        assertEquals(R196EquipmentType.FactoryKind.ARROW, R196EquipmentType.ARROW.factoryKind());
        assertEquals(R196EquipmentType.FactoryKind.PLAIN, R196EquipmentType.HELMET.factoryKind());
        assertEquals(R196EquipmentType.FactoryKind.ORDINARY, R196EquipmentType.PICKAXE.factoryKind());
    }
}
```

- [ ] **Step 3: Write failing formula tests for validated keys**

Create `R196EquipmentKeyTest.java`:

```java
package com.pixulse.infx.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.pixulse.infx.material.R196Material;
import java.util.Map;
import org.junit.jupiter.api.Test;

class R196EquipmentKeyTest {
    private static R196EquipmentKey key(R196Material material, R196EquipmentType type) {
        return new R196EquipmentKey(material, type);
    }

    @Test
    void toolFormulasCoverLowMiddleAndHighTiers() {
        assertEquals(400, key(R196Material.FLINT, R196EquipmentType.HATCHET).durability());
        assertEquals(9_600, key(R196Material.IRON, R196EquipmentType.PICKAXE).durability());
        assertEquals(512_000, key(R196Material.ADAMANTIUM, R196EquipmentType.WAR_HAMMER).durability());
        assertEquals(2.5F, key(R196Material.FLINT, R196EquipmentType.HATCHET).miningSpeed());
        assertEquals(8.0F, key(R196Material.IRON, R196EquipmentType.PICKAXE).miningSpeed());
        assertEquals(9.0F, key(R196Material.ADAMANTIUM, R196EquipmentType.WAR_HAMMER).miningSpeed());
        assertEquals(3.0F, key(R196Material.FLINT, R196EquipmentType.HATCHET).meleeDamage());
        assertEquals(6.0F, key(R196Material.IRON, R196EquipmentType.PICKAXE).meleeDamage());
        assertEquals(8.0F, key(R196Material.ADAMANTIUM, R196EquipmentType.WAR_HAMMER).meleeDamage());
    }

    @Test
    void wearAndReachMatchR196TypeProfiles() {
        assertEquals(4.0F / 3.0F, R196EquipmentType.HATCHET.blockDecay());
        assertEquals(.4F, R196EquipmentType.MATTOCK.blockDecay());
        assertEquals(2.0F / 3.0F, R196EquipmentType.WAR_HAMMER.blockDecay());
        assertEquals(133, key(R196Material.FLINT, R196EquipmentType.HATCHET).attackWear());
        assertEquals(75, key(R196Material.COPPER, R196EquipmentType.BATTLE_AXE).attackWear());
        assertEquals(66, key(R196Material.IRON, R196EquipmentType.WAR_HAMMER).attackWear());
        assertEquals(1.0F, R196EquipmentType.SCYTHE.reachBonus());
        assertEquals(.25F, R196EquipmentType.KNIFE.reachBonus());
    }

    @Test
    void modernAttackSpeedAnalogsAreLiteral() {
        Map<R196EquipmentType, Float> expected = Map.ofEntries(
                Map.entry(R196EquipmentType.PICKAXE, -2.8F),
                Map.entry(R196EquipmentType.SHOVEL, -3.0F),
                Map.entry(R196EquipmentType.MATTOCK, -3.0F),
                Map.entry(R196EquipmentType.HATCHET, -3.2F),
                Map.entry(R196EquipmentType.AXE, -3.1F),
                Map.entry(R196EquipmentType.BATTLE_AXE, -3.1F),
                Map.entry(R196EquipmentType.WAR_HAMMER, -3.4F),
                Map.entry(R196EquipmentType.CLUB, -3.4F),
                Map.entry(R196EquipmentType.CUDGEL, -3.4F),
                Map.entry(R196EquipmentType.HOE, -1.0F),
                Map.entry(R196EquipmentType.SCYTHE, -1.0F),
                Map.entry(R196EquipmentType.SWORD, -2.4F),
                Map.entry(R196EquipmentType.DAGGER, -2.4F),
                Map.entry(R196EquipmentType.KNIFE, -2.4F));
        expected.forEach((type, speed) -> assertEquals(speed, type.attackSpeedModifier(), type.path()));
        assertFalse(R196EquipmentType.SHEARS.hasAttackSpeedModifier());
        assertFalse(R196EquipmentType.BOW.hasAttackSpeedModifier());
    }

    @Test
    void bowArrowAndFishingValuesMatchR196() {
        assertEquals(32, key(R196Material.WOOD, R196EquipmentType.BOW).durability());
        assertEquals(64, key(R196Material.ANCIENT_METAL, R196EquipmentType.BOW).durability());
        assertEquals(128, key(R196Material.MITHRIL, R196EquipmentType.BOW).durability());
        assertEquals(3, key(R196Material.FLINT, R196EquipmentType.FISHING_ROD).durability());
        assertEquals(16, key(R196Material.IRON, R196EquipmentType.FISHING_ROD).durability());
        assertEquals(512, key(R196Material.ADAMANTIUM, R196EquipmentType.FISHING_ROD).durability());
        assertEquals(1.0, key(R196Material.FLINT, R196EquipmentType.ARROW).arrowBaseDamage());
        assertEquals(2.5, key(R196Material.IRON, R196EquipmentType.ARROW).arrowBaseDamage());
        assertEquals(3.5, key(R196Material.ADAMANTIUM, R196EquipmentType.ARROW).arrowBaseDamage());
    }

    @Test
    void armorDurabilityAndFractionalProtectionMatchR196() {
        R196EquipmentKey copperHelmet = key(R196Material.COPPER, R196EquipmentType.HELMET);
        R196EquipmentKey copperChainHelmet = key(R196Material.COPPER, R196EquipmentType.CHAINMAIL_HELMET);
        assertEquals(40, copperHelmet.durability());
        assertEquals(20, copperChainHelmet.durability());
        assertEquals(35.0F / 24.0F, copperHelmet.armorProtection());
        assertEquals(25.0F / 24.0F, copperChainHelmet.armorProtection());

        float plateSum = R196EquipmentType.platePieces().stream()
                .map(type -> key(R196Material.MITHRIL, type))
                .map(R196EquipmentKey::armorProtection)
                .reduce(0.0F, Float::sum);
        float chainSum = R196EquipmentType.chainPieces().stream()
                .map(type -> key(R196Material.MITHRIL, type))
                .map(R196EquipmentKey::armorProtection)
                .reduce(0.0F, Float::sum);
        assertEquals(9.0F, plateSum, 1.0E-6F);
        assertEquals(7.0F, chainSum, 1.0E-6F);
        assertEquals(7.0F, key(R196Material.ADAMANTIUM, R196EquipmentType.HORSE_ARMOR).armorProtection());
    }
}
```

- [ ] **Step 4: Run the item tests and verify the red state**

```bash
bash gradlew test --tests 'com.pixulse.infx.item.R196Equipment*Test'
```

Expected: compilation fails because the equipment definition classes do not exist.

- [ ] **Step 5: Add category, mining-family, use-action, and type enums**

Start with these exact ordering enums; later tasks add behavior methods without changing their constants:

```java
public enum R196EquipmentCategory {
    TOOL, WEAPON, PLATE_ARMOR, CHAIN_ARMOR, HORSE_ARMOR
}

public enum R196MiningFamily {
    NONE, PICKAXE, SHOVEL, AXE, HOE, SCYTHE, SWORD, SHEARS
}

public enum R196UseAction {
    NONE, AXE, SHOVEL, HOE, MATTOCK
}
```

Implement `R196EquipmentType` with immutable defensive copies of `EnumSet<R196Material>` and these nested model/form enums:

```java
public enum ModelFamily { GENERATED, HANDHELD, FISHING_ROD, BOW }
public enum ArmorForm { NONE, PLATE, CHAIN, HORSE }
public enum FactoryKind { ORDINARY, SHEARS, FISHING_ROD, BOW, ARROW, PLAIN }
```

The literal type table is:

| Type | Category | Materials | Components | Base damage | Reach | Mining multiplier | Attack speed | Block decay | Attack decay | Mining | Use | Model/form |
| --- | --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | --- | --- | --- |
| Pickaxe | Tool | eight metals | 3 | 2 | .75 | 1 | -2.8 | 1 | 1 | Pickaxe | None | Handheld |
| Shovel | Tool | wood, flint, obsidian + eight metals | 1 | 1 | .75 | 1 | -3.0 | .5 | 1 | Shovel | Shovel | Handheld |
| Hatchet | Tool | flint, obsidian + eight metals | 1 | 2 | .5 | .5 | -3.2 | 4/3 | 4/3 | Axe | Axe | Handheld |
| Axe | Tool | flint, obsidian + eight metals | 3 | 3 | .75 | 1 | -3.1 | 1 | 1 | Axe | Axe | Handheld |
| Hoe | Tool | eight metals | 2 | 1 | .75 | .5 | -1.0 | 2 | 2 | Hoe | Hoe | Handheld |
| Mattock | Tool | eight metals | 4 | 1 | .75 | .75 | -3.0 | .4 | 1 | Shovel | Mattock | Handheld |
| Battle Axe | Tool | eight metals | 4 | 4 | .75 | .75 | -3.1 | 1.25 | .75 | Axe | Axe | Handheld |
| War Hammer | Tool | eight metals | 5 | 2 | .75 | .75 | -3.4 | 2/3 | 2/3 | Pickaxe | None | Handheld |
| Scythe | Tool | eight metals | 2 | 1 | 1 | 1 | -1.0 | 2 | 4 | Scythe | None | Handheld |
| Shears | Tool | eight metals | 2 | 0 | .5 | 1 | no modifier | 1 | 2 | Shears | None | Handheld |
| Fishing Rod | Tool | flint, obsidian, copper, silver, gold, iron, ancient metal, mithril, adamantium | 0 | 0 | 0 | 0 | no modifier | 0 | 0 | None | None | Fishing rod |
| Cudgel | Weapon | wood | 1 | 1 | .25 | .5 | -3.4 | .25 | .25 | None | None | Handheld |
| Club | Weapon | wood | 2 | 2 | .5 | .5 | -3.4 | .25 | .25 | None | None | Handheld |
| Knife | Weapon | flint, obsidian | 1 | 1 | .25 | .5 | -2.4 | 1 | .5 | Sword | None | Handheld |
| Sword | Weapon | eight metals | 2 | 4 | .75 | .5 | -2.4 | 2 | .5 | Sword | None | Handheld |
| Dagger | Weapon | eight metals | 1 | 2 | .5 | .5 | -2.4 | 2 | .5 | Sword | None | Handheld |
| Bow | Weapon | wood, ancient metal, mithril | 0 | 0 | 0 | 0 | no modifier | 0 | 0 | None | None | Bow |
| Arrow | Weapon | flint, obsidian + eight metals | 0 | 0 | 0 | 0 | no modifier | 0 | 0 | None | None | Generated |
| Plate pieces | Plate armor | leather + eight metals | 5/8/7/4 | 0 | 0 | 0 | no modifier | 0 | 0 | None | None | Generated/plate |
| Chain pieces | Chain armor | eight metals | 5/8/7/4 | 0 | 0 | 0 | no modifier | 0 | 0 | None | None | Generated/chain |
| Horse Armor | Horse armor | copper, silver, gold, iron, ancient metal, mithril, adamantium | 0 | 0 | 0 | 0 | no modifier | 0 | 0 | None | None | Generated/horse |

Use paths `battle_axe`, `war_hammer`, `fishing_rod`, `chainmail_helmet`, `chainmail_chestplate`, `chainmail_leggings`, `chainmail_boots`, and `horse_armor`. Armor slots use `ArmorType.HELMET`, `CHESTPLATE`, `LEGGINGS`, and `BOOTS`; store the R196 component counts separately instead of using modern `ArmorType` durability units. Return `Set.copyOf` from `allowedMaterials()`.

Represent “no attack-speed modifier” as `Float.NaN`; `hasAttackSpeedModifier()` returns `!Float.isNaN(attackSpeedModifier)`. `disablesBlockingSeconds()` returns `5.0F` only for hatchet, axe, and battle axe, preserving current modern axe behavior, and `0.0F` for every other type. `platePieces()` returns helmet/chestplate/leggings/boots in that order and `chainPieces()` returns their four chainmail counterparts in the same order.

Set `FactoryKind.SHEARS`, `FISHING_ROD`, `BOW`, and `ARROW` on their namesake types, `PLAIN` on all armor forms, and `ORDINARY` on the remaining melee/mining types. Catalog registration switches only on this field; it must not rebuild a second list of specialty type names.

- [ ] **Step 6: Implement the validated key and formulas**

Use a record with a compact constructor that rejects illegal pairs:

```java
public record R196EquipmentKey(R196Material material, R196EquipmentType type) {
    public R196EquipmentKey {
        Objects.requireNonNull(material, "material");
        Objects.requireNonNull(type, "type");
        if (!type.allows(material)) {
            throw new IllegalArgumentException("Illegal R196 equipment key: " + material.path() + "_" + type.path());
        }
    }

    public String path() {
        return material.path() + "_" + type.path();
    }

    public static List<R196EquipmentKey> all() {
        return Holder.ALL;
    }

    private static final class Holder {
        private static final List<R196EquipmentKey> ALL = Arrays.stream(R196Material.values())
                .flatMap(material -> Arrays.stream(R196EquipmentType.values())
                        .filter(type -> type.allows(material))
                        .map(type -> new R196EquipmentKey(material, type)))
                .toList();
    }
}
```

Implement the formulas exactly:

```java
public int durability() {
    if (type == R196EquipmentType.BOW) {
        return switch (material) {
            case WOOD -> 32;
            case ANCIENT_METAL -> 64;
            case MITHRIL -> 128;
            default -> throw new IllegalStateException("Illegal bow key " + path());
        };
    }
    if (type == R196EquipmentType.FISHING_ROD) {
        return (int) (2.0F * material.durabilityMultiplier()) + (material == R196Material.FLINT ? 1 : 0);
    }
    if (type.armorForm() == ArmorForm.PLATE) {
        return (int) (type.durabilityComponents() * material.durabilityMultiplier() * 2.0F);
    }
    if (type.armorForm() == ArmorForm.CHAIN) {
        return (int) (type.durabilityComponents() * material.durabilityMultiplier());
    }
    if (type == R196EquipmentType.ARROW || type.armorForm() == ArmorForm.HORSE) {
        return 0;
    }
    return material.toolDurability(type.durabilityComponents());
}

public float miningSpeed() { return material.miningSpeed(type.miningMultiplier()); }
public float meleeDamage() { return material.meleeDamage(type.baseDamage()); }
public int attackWear() { return Math.max((int) (100.0F * type.attackDecay()), 1); }
public double arrowBaseDamage() { return .5D + material.materialDamage() * .5D; }

public float armorProtection() {
    return switch (type.armorForm()) {
        case PLATE -> type.durabilityComponents() * material.plateProtection() / 24.0F;
        case CHAIN -> type.durabilityComponents() * (material.plateProtection() - 2.0F) / 24.0F;
        case HORSE -> material.horseProtection();
        case NONE -> 0.0F;
    };
}
```

`englishName()` concatenates the equipment prefix and type name with four source-language exceptions: horse armor uses the material noun (`Gold Horse Armor`), every fishing rod is `Fishing Rod`, wood bow is `Bow`, and copper pickaxe is `InfiniteX Copper Pickaxe`. `chineseName()` concatenates the Chinese material prefix and type suffix except every fishing rod is `钓鱼竿`, wood bow is `弓`, and copper pickaxe is `InfiniteX 铜镐`. Ancient-metal and mithril bows retain their material prefixes. Chain type names are `Chain Helmet`, `Chain Chestplate`, `Chain Leggings`, and `Chain Boots`; Chinese suffixes begin with `锁链`. Add `translationKey()` returning `item.infx.` plus `path()` and `equipmentAsset()` returning `infx:{material}` for plate/horse or `infx:{material}_chainmail` for chain.

- [ ] **Step 7: Run the focused tests and validate the golden count**

```bash
bash gradlew test --tests 'com.pixulse.infx.item.R196Equipment*Test'
wc -l src/test/resources/r196/catalog-paths.txt
```

Expected: tests pass; `wc` prints `237` for the golden file.

- [ ] **Step 8: Commit the equipment definitions**

```bash
git add src/main/java/com/pixulse/infx/item/R196EquipmentCategory.java \
  src/main/java/com/pixulse/infx/item/R196MiningFamily.java \
  src/main/java/com/pixulse/infx/item/R196UseAction.java \
  src/main/java/com/pixulse/infx/item/R196EquipmentType.java \
  src/main/java/com/pixulse/infx/item/R196EquipmentKey.java \
  src/test/java/com/pixulse/infx/item/R196EquipmentTypeTest.java \
  src/test/java/com/pixulse/infx/item/R196EquipmentKeyTest.java \
  src/test/resources/r196/catalog-paths.txt
git commit -m "feat: define R196 equipment matrix"
```

### Task 4: Build direct R196 item properties and register the catalog

**Files:**

- Create: `src/main/java/com/pixulse/infx/item/R196ItemProperties.java`
- Create: `src/main/java/com/pixulse/infx/item/R196Catalog.java`
- Modify: `src/main/java/com/pixulse/infx/item/R196MiningFamily.java`
- Modify: `src/main/java/com/pixulse/infx/registry/ModItems.java`
- Modify: `src/main/java/com/pixulse/infx/tag/ModTags.java`
- Delete: `src/main/java/com/pixulse/infx/registry/ModToolProperties.java`
- Test: `src/test/java/com/pixulse/infx/item/R196CatalogTest.java`

- [ ] **Step 1: Write the failing golden-catalog and ownership tests**

Create `R196CatalogTest.java` with no bootstrap-dependent `.get()` calls; holder IDs and item factories can be checked before a game registry event:

```java
package com.pixulse.infx.item;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.registry.ModItems;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class R196CatalogTest {
    private static R196Catalog catalog() {
        return ModItems.catalog();
    }

    private static List<String> goldenPaths() throws IOException, URISyntaxException {
        return Files.readAllLines(
                Path.of(R196CatalogTest.class.getResource("/r196/catalog-paths.txt").toURI()), UTF_8);
    }

    @Test
    void catalogMatchesTheIndependentGoldenManifest() throws Exception {
        List<String> actual = catalog().entries().stream().map(R196Catalog.Entry::path).toList();
        assertEquals(goldenPaths(), actual);
        assertEquals(237, actual.size());
        assertEquals(237, new HashSet<>(actual).size());
        assertEquals(33, catalog().rawEntries().size());
        assertEquals(204, catalog().equipmentEntries().size());
    }

    @Test
    void allEquipmentIsModOwnedButExactVanillaRawCurrencyIsReused() {
        assertTrue(catalog().equipmentEntries().stream().allMatch(entry -> entry.id().getNamespace().equals("infx")));
        assertSame(Items.COPPER_NUGGET, catalog().reusedRaw("copper_nugget"));
        assertSame(Items.GOLD_NUGGET, catalog().reusedRaw("gold_nugget"));
        assertSame(Items.IRON_NUGGET, catalog().reusedRaw("iron_nugget"));
        assertSame(Items.COPPER_INGOT, catalog().reusedRaw("copper_ingot"));
        assertSame(Items.GOLD_INGOT, catalog().reusedRaw("gold_ingot"));
        assertSame(Items.IRON_INGOT, catalog().reusedRaw("iron_ingot"));
        assertThrows(IllegalArgumentException.class, () -> catalog().reusedRaw("silver_ingot"));
    }

    @Test
    void aliasesPreserveTheNineExistingRegistryIds() {
        assertSame(ModItems.FLINT_CHIP, catalog().raw("flint_chip").holder());
        assertSame(ModItems.SINEW, catalog().raw("sinew").holder());
        assertSame(ModItems.OBSIDIAN_SHARD, catalog().raw("obsidian_shard").holder());
        assertSame(ModItems.EMERALD_SHARD, catalog().raw("emerald_shard").holder());
        assertSame(ModItems.SILVER_NUGGET, catalog().raw("silver_nugget").holder());
        assertSame(ModItems.MITHRIL_NUGGET, catalog().raw("mithril_nugget").holder());
        assertSame(ModItems.ADAMANTIUM_NUGGET, catalog().raw("adamantium_nugget").holder());
        assertSame(ModItems.FLINT_HATCHET, catalog().equipment(R196Material.FLINT, R196EquipmentType.HATCHET).holder());
        assertSame(ModItems.COPPER_PICKAXE, catalog().equipment(R196Material.COPPER, R196EquipmentType.PICKAXE).holder());
    }

    @Test
    void missingLookupsFailWithTheRequestedIdentity() {
        IllegalArgumentException raw = assertThrows(IllegalArgumentException.class, () -> catalog().raw("iron_coin"));
        assertTrue(raw.getMessage().contains("iron_coin"));
        IllegalArgumentException equipment = assertThrows(
                IllegalArgumentException.class,
                () -> catalog().equipment(R196Material.WOOD, R196EquipmentType.PICKAXE));
        assertTrue(equipment.getMessage().contains("wood_pickaxe"));
        assertFalse(catalog().entries().stream().anyMatch(entry -> entry.path().contains("diamond_helmet")));
    }
}
```

- [ ] **Step 2: Run the catalog test and confirm the red state**

```bash
bash gradlew test --tests com.pixulse.infx.item.R196CatalogTest
```

Expected: compilation fails because `R196Catalog` does not exist.

- [ ] **Step 3: Implement direct property composition**

First add the repair tag identity required during property construction:

```java
public static TagKey<Item> repairMaterial(R196Material material) {
    return create("repair_materials/" + material.path());
}
```

Create `R196ItemProperties` with one public entry point:

```java
public static Item.Properties forEquipment(R196EquipmentKey key, Item.Properties properties)
```

Dispatch by type to `tool`, `bow`, `arrow`, `armor`, or `horseArmor`. Do not construct a vanilla `ArmorMaterial`: its defense map is integer-valued and cannot express R196 fractional values.

For tools and weapons, build the components directly:

```java
private static Item.Properties tool(R196EquipmentKey key, Item.Properties properties) {
    R196EquipmentType type = key.type();
    ItemAttributeModifiers.Builder attributes = ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, key.meleeDamage() - 1.0F,
                            AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND);
    if (type.hasAttackSpeedModifier()) {
        attributes.add(Attributes.ATTACK_SPEED,
                new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, type.attackSpeedModifier(),
                        AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND);
    }
    if (type.reachBonus() != 0.0F) {
        Identifier reachId = InfiniteX.id("tool_reach");
        attributes.add(Attributes.BLOCK_INTERACTION_RANGE,
                new AttributeModifier(reachId, type.reachBonus(), AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND);
        attributes.add(Attributes.ENTITY_INTERACTION_RANGE,
                new AttributeModifier(reachId, type.reachBonus(), AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND);
    }
    return commonDamageable(key, properties)
            .component(DataComponents.TOOL, type.miningFamily().createTool(key))
            .attributes(attributes.build())
            .component(DataComponents.WEAPON, new Weapon(key.attackWear(), type.disablesBlockingSeconds()));
}
```

Subtract `1.0F` from the desired melee damage because players contribute one built-in attack-damage point. `R196MiningFamily.createTool` obtains block holders from `BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK)` and constructs:

- pickaxe/shovel/axe/hoe: deny the material's incorrect-for-drops tag, then mine the corresponding vanilla mineable tag at `key.miningSpeed()`;
- scythe: mine `BlockTags.CROPS` and `BlockTags.MINEABLE_WITH_HOE` at `key.miningSpeed()`;
- sword/knife/dagger: cobweb at 15, `SWORD_INSTANTLY_MINES` at `Float.MAX_VALUE`, and `SWORD_EFFICIENT` at `key.miningSpeed()`;
- shears: the four rules from `ShearsItem.createToolProperties()`, but replace the major/minor speeds with `key.miningSpeed()` while keeping cobweb/extreme at 15;
- none: `new Tool(List.of(), 1.0F, 0, true)` for cudgel/club so `R196ToolItem` can still own hardness wear only when effective; arrows, bows, armor, and fishing rods receive no `TOOL` component.

`commonDamageable` sets durability, enchantability only when above zero, and `repairable(ModTags.Items.repairMaterial(key.material()))`. Raw items remain stack 64. Bow durability comes from the key and repair metadata comes from its material. Arrows are stack 64 and have neither durability nor repair components.

Armor properties use direct fractional attributes:

```java
EquipmentSlot slot = key.type().armorType().orElseThrow().getSlot();
EquipmentSlotGroup group = EquipmentSlotGroup.bySlot(slot);
Identifier armorId = InfiniteX.id("armor." + key.type().path());
ItemAttributeModifiers attributes = ItemAttributeModifiers.builder()
        .add(Attributes.ARMOR,
                new AttributeModifier(armorId, key.armorProtection(), AttributeModifier.Operation.ADD_VALUE),
                group)
        .build();
Equippable equippable = Equippable.builder(slot)
        .setEquipSound(SoundEvents.ARMOR_EQUIP_GENERIC)
        .setAsset(key.equipmentAsset())
        .build();
```

Set durability, attributes, enchantability, repair tag, and `EQUIPPABLE`. Do not add `DYED_COLOR` to an undyed leather stack; the model and equipment asset use the standard leather default color, and a dye recipe adds the component when needed.

Horse armor has no durability, as in modern horse armor. Use `EquipmentSlot.BODY`, `SoundEvents.HORSE_ARMOR`, `EntityTypeTags.CAN_WEAR_HORSE_ARMOR`, `.setDamageOnHurt(false)`, `.setCanBeSheared(true)`, `.setShearingSound(SoundEvents.HORSE_ARMOR_UNEQUIP)`, and direct `Attributes.ARMOR` on `EquipmentSlotGroup.BODY`.

- [ ] **Step 4: Implement ordered deferred registration**

Create `R196Catalog` around package-visible `ModItems.ITEMS`. The public immutable records are:

```java
public sealed interface Entry permits RawEntry, EquipmentEntry {
    String path();
    Identifier id();
    DeferredItem<? extends Item> holder();
    Class<? extends Item> itemClass();
    String englishName();
    String chineseName();
    <T extends Item> DeferredItem<T> holderAs(Class<T> requestedClass);
}

public record RawEntry(R196RawItem definition, DeferredItem<Item> holder) implements Entry {
    @Override public String path() { return definition.path(); }
    @Override public Identifier id() { return holder.getId(); }
    @Override public Class<? extends Item> itemClass() { return Item.class; }
    @Override public String englishName() { return definition.englishName(); }
    @Override public String chineseName() { return definition.chineseName(); }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Item> DeferredItem<T> holderAs(Class<T> requestedClass) {
        if (requestedClass != Item.class) {
            throw new IllegalArgumentException("Wrong item class for " + path() + ": " + requestedClass.getName());
        }
        return (DeferredItem<T>) (DeferredItem<?>) holder;
    }
}

public record EquipmentEntry(
        R196EquipmentKey key,
        DeferredItem<? extends Item> holder,
        Class<? extends Item> itemClass) implements Entry {
    @Override public String path() { return key.path(); }
    @Override public Identifier id() { return holder.getId(); }
    @Override public String englishName() { return key.englishName(); }
    @Override public String chineseName() { return key.chineseName(); }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Item> DeferredItem<T> holderAs(Class<T> requestedClass) {
        if (requestedClass != itemClass) {
            throw new IllegalArgumentException("Wrong item class for " + path() + ": " + requestedClass.getName());
        }
        return (DeferredItem<T>) (DeferredItem<?>) holder;
    }
}
```

Register all 33 raw definitions first, then `R196EquipmentKey.all()` in its material/type order. Use `ITEMS.registerItem(path, factory, properties -> R196ItemProperties.forEquipment(key, properties))`, which ensures NeoForge supplies a properties instance with the registry `ResourceKey<Item>` already set. Factories are:

- `FactoryKind.PLAIN` -> plain `Item`, because `EQUIPPABLE` owns modern armor behavior;
- every other factory kind -> `R196ToolItem` during this registration task, using its existing float constructor and the type's block-decay value.

Tasks 6 and 7 replace the `SHEARS`, `FISHING_ROD`, `BOW`, and `ARROW` switch arms with their final classes after those classes are introduced; the registry paths and catalog keys do not change.

Build immutable maps by path and equipment key. Throw on duplicate `put`. `raw(path)` throws `IllegalArgumentException("Missing R196 raw item: " + path)` and `equipment(material,type)` throws `IllegalArgumentException("Missing R196 equipment: " + material.path() + "_" + type.path())`. `reusedRaw` is an immutable map with keys `copper_nugget`, `gold_nugget`, `iron_nugget`, `copper_ingot`, `gold_ingot`, `iron_ingot`, `flint`, `string`, `leather`, `feather`, `stick`, `obsidian`, `diamond`, `emerald`, `nether_quartz`, and `glass`, mapped to their exact vanilla items.

- [ ] **Step 5: Replace `ModItems` fields with catalog aliases**

`ModItems` must initialize the catalog after the two workbench block items but before alias fields are evaluated:

```java
public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(InfiniteX.MOD_ID);

public static final DeferredItem<BlockItem> FLINT_WORKBENCH = ITEMS.registerSimpleBlockItem(ModBlocks.FLINT_WORKBENCH);
public static final DeferredItem<BlockItem> COPPER_WORKBENCH = ITEMS.registerSimpleBlockItem(ModBlocks.COPPER_WORKBENCH);

private static final R196Catalog CATALOG = R196Catalog.register(ITEMS);

public static final DeferredItem<Item> FLINT_CHIP = CATALOG.raw("flint_chip").holderAs(Item.class);
public static final DeferredItem<Item> SINEW = CATALOG.raw("sinew").holderAs(Item.class);
public static final DeferredItem<Item> OBSIDIAN_SHARD = CATALOG.raw("obsidian_shard").holderAs(Item.class);
public static final DeferredItem<Item> EMERALD_SHARD = CATALOG.raw("emerald_shard").holderAs(Item.class);
public static final DeferredItem<Item> SILVER_NUGGET = CATALOG.raw("silver_nugget").holderAs(Item.class);
public static final DeferredItem<Item> MITHRIL_NUGGET = CATALOG.raw("mithril_nugget").holderAs(Item.class);
public static final DeferredItem<Item> ADAMANTIUM_NUGGET = CATALOG.raw("adamantium_nugget").holderAs(Item.class);
public static final DeferredItem<R196ToolItem> FLINT_HATCHET =
        CATALOG.equipment(R196Material.FLINT, R196EquipmentType.HATCHET).holderAs(R196ToolItem.class);
public static final DeferredItem<R196ToolItem> COPPER_PICKAXE =
        CATALOG.equipment(R196Material.COPPER, R196EquipmentType.PICKAXE).holderAs(R196ToolItem.class);

public static R196Catalog catalog() { return CATALOG; }
```

Implement `holderAs` on entries with a checked `Class<? extends Item>` factory-class field captured during registration; if the requested alias class differs, throw `IllegalArgumentException` containing the item path. This avoids an unchecked field cast hidden in `ModItems`.

Remove `ModToolProperties.java`. Keep `ITEMS.register(modBus)` unchanged.

- [ ] **Step 6: Run catalog and legacy tests**

```bash
bash gradlew test --tests com.pixulse.infx.item.R196CatalogTest
bash gradlew test
```

Expected: the catalog test passes; the full pre-resource unit suite passes. Item subclasses may still be skeletal at this point, but their constructors compile and registration IDs are stable.

- [ ] **Step 7: Commit registration and properties**

```bash
git add src/main/java/com/pixulse/infx/item/R196Catalog.java \
  src/main/java/com/pixulse/infx/item/R196ItemProperties.java \
  src/main/java/com/pixulse/infx/item/R196MiningFamily.java \
  src/main/java/com/pixulse/infx/registry/ModItems.java \
  src/main/java/com/pixulse/infx/tag/ModTags.java \
  src/test/java/com/pixulse/infx/item/R196CatalogTest.java
git rm src/main/java/com/pixulse/infx/registry/ModToolProperties.java
git commit -m "feat: register the complete R196 catalog"
```

### Task 5: Implement ordinary tool actions and exact hardness wear

**Files:**

- Modify: `src/main/java/com/pixulse/infx/item/R196ToolItem.java`
- Modify: `src/main/java/com/pixulse/infx/item/R196UseAction.java`
- Modify: `src/main/java/com/pixulse/infx/item/R196Catalog.java`
- Modify: `src/test/java/com/pixulse/infx/item/R196EquipmentKeyTest.java`

- [ ] **Step 1: Add failing use-delegation and wear tests**

Append these pure assertions to `R196EquipmentKeyTest`:

```java
@Test
void ordinaryToolBehaviorsAreMappedWithoutVanillaConstructorMutation() {
    assertEquals(R196UseAction.AXE, R196EquipmentType.HATCHET.useAction());
    assertEquals(R196UseAction.AXE, R196EquipmentType.AXE.useAction());
    assertEquals(R196UseAction.AXE, R196EquipmentType.BATTLE_AXE.useAction());
    assertEquals(R196UseAction.SHOVEL, R196EquipmentType.SHOVEL.useAction());
    assertEquals(R196UseAction.HOE, R196EquipmentType.HOE.useAction());
    assertEquals(R196UseAction.MATTOCK, R196EquipmentType.MATTOCK.useAction());
    assertEquals(133, key(R196Material.FLINT, R196EquipmentType.HATCHET).damageForBreaking(1.0F));
    assertEquals(20, key(R196Material.COPPER, R196EquipmentType.MATTOCK).damageForBreaking(.5F));
    assertEquals(0, key(R196Material.IRON, R196EquipmentType.PICKAXE).damageForBreaking(0.0F));
}
```

- [ ] **Step 2: Run the focused test and see it fail**

```bash
bash gradlew test --tests com.pixulse.infx.item.R196EquipmentKeyTest
```

Expected: compilation fails because `damageForBreaking` is absent.

- [ ] **Step 3: Add the key-level hardness formula**

```java
public int damageForBreaking(float hardness) {
    return ToolWearCalculator.damageForBreaking(hardness, type.blockDecay());
}
```

Keep this as the one bridge to the existing, already-tested hardness service.

- [ ] **Step 4: Make `R196UseAction` delegate to modern vanilla behavior**

Do not subclass `AxeItem`, `ShovelItem`, or `HoeItem`, because their constructors replace R196 data components. Delegate to the already-registered vanilla singleton only for its virtual behavior method; the `UseOnContext` still carries the InfiniteX stack:

```java
public InteractionResult useOn(UseOnContext context) {
    return switch (this) {
        case NONE -> InteractionResult.PASS;
        case AXE -> Items.IRON_AXE.useOn(context);
        case SHOVEL -> Items.IRON_SHOVEL.useOn(context);
        case HOE -> Items.IRON_HOE.useOn(context);
        case MATTOCK -> {
            InteractionResult shovel = Items.IRON_SHOVEL.useOn(context);
            yield shovel == InteractionResult.PASS ? Items.IRON_HOE.useOn(context) : shovel;
        }
    };
}

public boolean canPerformAction(ItemInstance stack, ItemAbility ability) {
    return switch (this) {
        case NONE -> false;
        case AXE -> Items.IRON_AXE.canPerformAction(stack, ability);
        case SHOVEL -> Items.IRON_SHOVEL.canPerformAction(stack, ability);
        case HOE -> Items.IRON_HOE.canPerformAction(stack, ability);
        case MATTOCK -> Items.IRON_SHOVEL.canPerformAction(stack, ability)
                || Items.IRON_HOE.canPerformAction(stack, ability);
    };
}
```

This preserves modern stripping/scraping/wax removal, pathing/dousing, tilling, criteria, sounds, events, and NeoForge abilities without registering fake helper items.

- [ ] **Step 5: Replace `R196ToolItem` with key-driven behavior**

Remove `final` and replace the float constructor with:

```java
private final R196EquipmentKey key;

public R196ToolItem(R196EquipmentKey key, Properties properties) {
    super(properties);
    this.key = key;
}

public R196EquipmentKey key() { return key; }

@Override
public InteractionResult useOn(UseOnContext context) {
    return key.type().useAction().useOn(context);
}

@Override
public boolean canPerformAction(ItemInstance stack, ItemAbility ability) {
    return key.type().useAction().canPerformAction(stack, ability);
}
```

`mineBlock` applies hardness wear only when `getDestroySpeed(stack, state) > 1.0F`, matching R196's effective-tool guard. `postHurtEnemy` does not damage the stack: the `WEAPON.itemDamagePerAttack` component already applies `key.attackWear()` once after a successful hit. This prevents double damage.

Update the ordinary catalog factory from `new R196ToolItem(type.blockDecay(), properties)` to `new R196ToolItem(key, properties)` and keep the recorded item class as `R196ToolItem.class`.

- [ ] **Step 6: Run focused and full tests**

```bash
bash gradlew test --tests com.pixulse.infx.item.R196EquipmentKeyTest
bash gradlew test
```

Expected: both commands pass.

- [ ] **Step 7: Commit ordinary behavior**

```bash
git add src/main/java/com/pixulse/infx/item/R196ToolItem.java \
  src/main/java/com/pixulse/infx/item/R196UseAction.java \
  src/main/java/com/pixulse/infx/item/R196EquipmentKey.java \
  src/main/java/com/pixulse/infx/item/R196Catalog.java \
  src/test/java/com/pixulse/infx/item/R196EquipmentKeyTest.java
git commit -m "feat: add R196 tool actions and wear"
```

### Task 6: Add material shears and fishing rods

**Files:**

- Create: `src/main/java/com/pixulse/infx/item/R196ShearsItem.java`
- Create: `src/main/java/com/pixulse/infx/item/R196FishingRodItem.java`
- Modify: `src/main/java/com/pixulse/infx/item/R196ToolItem.java`
- Modify: `src/main/java/com/pixulse/infx/item/R196Catalog.java`
- Test: `src/test/java/com/pixulse/infx/item/R196CatalogTest.java`

- [ ] **Step 1: Add failing factory-family assertions**

Append:

```java
@Test
void specialtyFactoriesAreNotCollapsedToPlainItems() {
    assertEquals(R196ShearsItem.class,
            catalog().equipment(R196Material.COPPER, R196EquipmentType.SHEARS).itemClass());
    assertEquals(R196FishingRodItem.class,
            catalog().equipment(R196Material.FLINT, R196EquipmentType.FISHING_ROD).itemClass());
    assertEquals(R196ToolItem.class,
            catalog().equipment(R196Material.COPPER, R196EquipmentType.PICKAXE).itemClass());
}
```

Run the catalog test. Expected: compilation fails because the two specialty classes do not exist.

- [ ] **Step 2: Extract one shared hardness-wear helper**

Add this package-visible method to `R196ToolItem`:

```java
static void applyMiningWear(
        R196EquipmentKey key, ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity owner) {
    if (!level.isClientSide() && state.getDestroySpeed(level, pos) != 0.0F
            && stack.getDestroySpeed(state) > 1.0F) {
        ToolWearApplication.afterHarvestSnapshot(
                state.getDestroySpeed(level, pos),
                key.type().blockDecay(state),
                damage -> stack.hurtAndBreak(damage, owner, EquipmentSlot.MAINHAND));
    }
}
```

Have ordinary `mineBlock` call this method. Add `R196EquipmentType.blockDecay(BlockState)` so scythes return `.5F` for `BlockTags.CROPS` and knives return `.5F` for `BlockTags.SWORD_EFFICIENT`; all other states return the type's stored decay.

- [ ] **Step 3: Implement shears without losing modern shear hooks**

```java
public final class R196ShearsItem extends ShearsItem {
    private final R196EquipmentKey key;

    public R196ShearsItem(R196EquipmentKey key, Properties properties) {
        super(properties);
        this.key = key;
    }

    public R196EquipmentKey key() { return key; }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity owner) {
        R196ToolItem.applyMiningWear(key, stack, level, state, pos, owner);
        return stack.has(DataComponents.TOOL);
    }
}
```

Inherit `interactLivingEntity`, `useOn`, and `canPerformAction` from `ShearsItem`; those paths call NeoForge `IShearable`, modern trim actions, sounds, criteria, and game events. Do not call `super.mineBlock`, which would add a second fixed durability point.

- [ ] **Step 4: Implement fishing rods as a thin identity subclass**

```java
public final class R196FishingRodItem extends FishingRodItem {
    private final R196EquipmentKey key;

    public R196FishingRodItem(R196EquipmentKey key, Properties properties) {
        super(properties);
        this.key = key;
    }

    public R196EquipmentKey key() { return key; }
}
```

`FishingRodItem` already implements cast, retrieve, enchantment luck/lure, sounds, stats, vibration, and standard retrieve wear without overwriting supplied properties.

- [ ] **Step 5: Run tests and commit**

```bash
bash gradlew test --tests com.pixulse.infx.item.R196CatalogTest
bash gradlew test
git add src/main/java/com/pixulse/infx/item/R196ShearsItem.java \
  src/main/java/com/pixulse/infx/item/R196FishingRodItem.java \
  src/main/java/com/pixulse/infx/item/R196ToolItem.java \
  src/main/java/com/pixulse/infx/item/R196EquipmentType.java \
  src/main/java/com/pixulse/infx/item/R196Catalog.java \
  src/test/java/com/pixulse/infx/item/R196CatalogTest.java
git commit -m "feat: add R196 shears and fishing rods"
```

### Task 7: Add material bows, arrows, and nocked-arrow state

**Files:**

- Create: `src/main/java/com/pixulse/infx/registry/ModDataComponents.java`
- Create: `src/main/java/com/pixulse/infx/item/R196BowItem.java`
- Create: `src/main/java/com/pixulse/infx/item/R196ArrowItem.java`
- Modify: `src/main/java/com/pixulse/infx/InfiniteX.java`
- Modify: `src/main/java/com/pixulse/infx/item/R196Catalog.java`
- Test: `src/test/java/com/pixulse/infx/item/R196CatalogTest.java`

- [ ] **Step 1: Add failing projectile factory assertions**

Append to `R196CatalogTest`:

```java
@Test
void projectileFactoriesRetainCatalogIdentity() {
    assertEquals(R196BowItem.class,
            catalog().equipment(R196Material.WOOD, R196EquipmentType.BOW).itemClass());
    assertEquals(R196ArrowItem.class,
            catalog().equipment(R196Material.ADAMANTIUM, R196EquipmentType.ARROW).itemClass());
}
```

Run the focused test. Expected: compilation fails because the projectile classes are absent.

- [ ] **Step 2: Register a serializable and networked String component**

Implement `ModDataComponents` exactly around NeoForge's specialized register:

```java
public final class ModDataComponents {
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, InfiniteX.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> NOCKED_ARROW_MATERIAL =
            COMPONENTS.registerComponentType("nocked_arrow_material", builder -> builder
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .cacheEncoding());

    private ModDataComponents() {}

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
```

Call `ModDataComponents.register(modBus)` immediately before `ModItems.register(modBus)` in `InfiniteX`.

- [ ] **Step 3: Implement bow state around vanilla firing**

```java
public final class R196BowItem extends BowItem {
    private final R196EquipmentKey key;

    public R196BowItem(R196EquipmentKey key, Properties properties) {
        super(properties);
        this.key = key;
    }

    public R196EquipmentKey key() { return key; }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack bow = player.getItemInHand(hand);
        ItemStack projectile = player.getProjectile(bow);
        if (projectile.getItem() instanceof R196ArrowItem arrow) {
            bow.set(ModDataComponents.NOCKED_ARROW_MATERIAL.get(), arrow.key().material().path());
        } else {
            bow.remove(ModDataComponents.NOCKED_ARROW_MATERIAL.get());
        }
        return super.use(level, player, hand);
    }

    @Override
    public boolean releaseUsing(ItemStack bow, Level level, LivingEntity entity, int remainingTime) {
        try {
            return super.releaseUsing(bow, level, entity, remainingTime);
        } finally {
            bow.remove(ModDataComponents.NOCKED_ARROW_MATERIAL.get());
        }
    }
}
```

The outer item model tests `is_using_item`, so a removed component after release does not affect the standby model. A vanilla/tipped/spectral arrow uses the select model's flint fallback and still fires because bows retain `ItemTags.ARROWS` compatibility.

- [ ] **Step 4: Implement material arrow damage for firing and dispensing**

```java
public final class R196ArrowItem extends ArrowItem {
    private final R196EquipmentKey key;

    public R196ArrowItem(R196EquipmentKey key, Properties properties) {
        super(properties);
        this.key = key;
    }

    public R196EquipmentKey key() { return key; }
    public double baseDamage() { return key.arrowBaseDamage(); }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack stack, LivingEntity owner, ItemStack weapon) {
        Arrow arrow = new Arrow(level, owner, stack.copyWithCount(1), weapon);
        arrow.setBaseDamage(baseDamage());
        return arrow;
    }

    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack stack, Direction direction) {
        Arrow arrow = new Arrow(level, position.x(), position.y(), position.z(), stack.copyWithCount(1), null);
        arrow.pickup = AbstractArrow.Pickup.ALLOWED;
        arrow.setBaseDamage(baseDamage());
        return arrow;
    }
}
```

The `Arrow` constructors copy the supplied one-item stack into the entity, so pickup identity remains the exact catalog arrow. Recovery probabilities remain metadata for milestone 5 and must not be randomized here.

- [ ] **Step 5: Run unit tests and commit**

```bash
bash gradlew test --tests 'com.pixulse.infx.item.R196*Test'
bash gradlew test
git add src/main/java/com/pixulse/infx/registry/ModDataComponents.java \
  src/main/java/com/pixulse/infx/item/R196BowItem.java \
  src/main/java/com/pixulse/infx/item/R196ArrowItem.java \
  src/main/java/com/pixulse/infx/item/R196Catalog.java \
  src/main/java/com/pixulse/infx/InfiniteX.java \
  src/test/java/com/pixulse/infx/item/R196CatalogTest.java
git commit -m "feat: add R196 bows and material arrows"
```

### Task 8: Verify direct armor and horse-armor components

**Files:**

- Modify: `src/main/java/com/pixulse/infx/item/R196ItemProperties.java`
- Test: `src/test/java/com/pixulse/infx/item/R196ItemPropertiesTest.java`

- [ ] **Step 1: Write failing direct-attribute tests**

Expose package-visible pure helpers `armorAttributes(R196EquipmentKey)` and `toolAttributes(R196EquipmentKey)` and create:

```java
package com.pixulse.infx.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pixulse.infx.material.R196Material;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.junit.jupiter.api.Test;

class R196ItemPropertiesTest {
    private static R196EquipmentKey key(R196Material material, R196EquipmentType type) {
        return new R196EquipmentKey(material, type);
    }

    @Test
    void toolAttributesDisplayTheFinalR196Damage() {
        var flintHatchet = R196ItemProperties.toolAttributes(key(R196Material.FLINT, R196EquipmentType.HATCHET));
        var adamantiumHammer = R196ItemProperties.toolAttributes(key(R196Material.ADAMANTIUM, R196EquipmentType.WAR_HAMMER));
        assertEquals(3.0, flintHatchet.compute(Attributes.ATTACK_DAMAGE, 1.0, EquipmentSlot.MAINHAND));
        assertEquals(8.0, adamantiumHammer.compute(Attributes.ATTACK_DAMAGE, 1.0, EquipmentSlot.MAINHAND));
        assertEquals(1.2, flintHatchet.compute(Attributes.ATTACK_SPEED, 4.0, EquipmentSlot.MAINHAND), 1.0E-6);
    }

    @Test
    void fractionalArmorSurvivesComponentConstruction() {
        var helmet = R196ItemProperties.armorAttributes(key(R196Material.COPPER, R196EquipmentType.HELMET));
        var leggings = R196ItemProperties.armorAttributes(key(R196Material.COPPER, R196EquipmentType.CHAINMAIL_LEGGINGS));
        assertEquals(35.0 / 24.0, helmet.compute(Attributes.ARMOR, 0.0, EquipmentSlot.HEAD), 1.0E-6);
        assertEquals(35.0 / 24.0, leggings.compute(Attributes.ARMOR, 0.0, EquipmentSlot.LEGS), 1.0E-6);
    }

    @Test
    void horseArmorUsesBodyAttributes() {
        var armor = R196ItemProperties.armorAttributes(key(R196Material.ADAMANTIUM, R196EquipmentType.HORSE_ARMOR));
        assertEquals(7.0, armor.compute(Attributes.ARMOR, 0.0, EquipmentSlot.BODY));
    }
}
```

- [ ] **Step 2: Run the test and verify it fails before helper extraction**

```bash
bash gradlew test --tests com.pixulse.infx.item.R196ItemPropertiesTest
```

Expected: compilation fails because the two helper methods are not visible.

- [ ] **Step 3: Extract and use the exact helpers**

Move the attribute-builder code already used by `tool`, `armor`, and `horseArmor` into package-visible static methods. Armor modifier IDs must be unique per type (`infx:armor.helmet`, `infx:armor.chainmail_helmet`, and so on) and slot groups must come from the actual armor slot. Do not round `key.armorProtection()`.

Do not add `DAMAGE_RESISTANT` for adamantium, toughness, knockback resistance, or a fixed-point damage hook. Those are deferred. Do not put `DYED_COLOR` on a default leather stack; the equipment asset supplies the undyed leather color and dye recipes add the component only when used.

- [ ] **Step 4: Run tests and commit**

```bash
bash gradlew test --tests com.pixulse.infx.item.R196ItemPropertiesTest
bash gradlew test
git add src/main/java/com/pixulse/infx/item/R196ItemProperties.java \
  src/test/java/com/pixulse/infx/item/R196ItemPropertiesTest.java
git commit -m "test: verify R196 equipment components"
```

### Task 9: Generate catalog tags and deterministic creative ordering

**Files:**

- Modify: `src/main/java/com/pixulse/infx/tag/ModTags.java`
- Modify: `src/main/java/com/pixulse/infx/data/ModItemTagsProvider.java`
- Modify: `src/main/java/com/pixulse/infx/registry/ModCreativeTabs.java`
- Test: `src/test/java/com/pixulse/infx/item/R196CatalogTest.java`

- [ ] **Step 1: Add failing ordering and tag-path assertions**

Append:

```java
@Test
void orderedViewsAreStableForDataGenerationAndCreativeTabs() {
    assertEquals("flint_chip", catalog().rawEntries().getFirst().path());
    assertEquals("netherspawn_frags", catalog().rawEntries().getLast().path());
    assertEquals("leather_helmet", catalog().equipmentEntries().getFirst().path());
    assertEquals("adamantium_horse_armor", catalog().equipmentEntries().getLast().path());
    assertEquals("repair_materials/rusted_iron", ModTags.Items.repairMaterial(R196Material.RUSTED_IRON).location().getPath());
    assertEquals("equipment/war_hammer", ModTags.Items.equipmentType(R196EquipmentType.WAR_HAMMER).location().getPath());
}
```

Run the focused test. Expected: compilation fails because the tag factories are missing.

- [ ] **Step 2: Add stable tag factories**

Under `ModTags.Items`, retain the existing `repairMaterial` method from Task 4 and add:

```java
public static TagKey<Item> material(R196Material material) {
    return create("materials/" + material.path());
}

public static TagKey<Item> equipmentType(R196EquipmentType type) {
    return create("equipment/" + type.path());
}
```

Keep `bindings` and `tool_tier/*` paths unchanged.

- [ ] **Step 3: Rewrite item tags from the catalog**

In `ModItemTagsProvider.addTags`:

1. Keep `infx:bindings` with vanilla string and InfiniteX sinew.
2. Add every equipment key to `infx:materials/{material}` and `infx:equipment/{type}`.
3. Add raw definitions with a material to `infx:materials/{material}`.
4. Fill repair tags exactly:
   - leather -> `Items.LEATHER`;
   - wood -> nested `ItemTags.PLANKS`;
   - flint -> `Items.FLINT`;
   - obsidian -> `Items.OBSIDIAN`;
   - copper/gold/iron -> their vanilla nuggets;
   - rusted iron -> vanilla iron nugget;
   - silver/mithril/adamantium/ancient metal -> the matching catalog nugget.
5. Add all material arrows to `ItemTags.ARROWS`.
6. Add pickaxes, axes/hatchets/battle axes, hoes, shovels/mattocks, and swords/daggers/knives to the corresponding vanilla family tags.
7. Add all damageable equipment to `DURABILITY_ENCHANTABLE`; mining tools to `MINING_ENCHANTABLE`; melee entries to `MELEE_WEAPON_ENCHANTABLE`, `WEAPON_ENCHANTABLE`, and `SHARP_WEAPON_ENCHANTABLE`; fishing rods to `FISHING_ENCHANTABLE`; bows to `BOW_ENCHANTABLE`; player armor to `ARMOR_ENCHANTABLE`, `EQUIPPABLE_ENCHANTABLE`, and the matching slot armor/enchantable tag.
8. For every key with a material harvest tier and a non-`NONE` mining family, add it to `infx:tool_tier/{tier}`.

Use a helper that adds `entry.holder().getKey()` to the appropriate `IntrinsicTagAppender<Item>`. Never call `holder().get()` while constructing tag contents.

- [ ] **Step 4: Replace deferred-register creative ordering**

Replace `.displayItems` with:

```java
.displayItems((parameters, output) -> {
    ModItems.catalog().rawEntries().forEach(entry -> output.accept(entry.holder().value()));
    output.accept(ModItems.FLINT_WORKBENCH.value());
    output.accept(ModItems.COPPER_WORKBENCH.value());
    ModItems.catalog().equipmentEntries().forEach(entry -> output.accept(entry.holder().value()));
})
```

This gives raw components first, preserves flint-before-copper workbenches, then uses the approved material/category order. Do not iterate `ModItems.ITEMS.getEntries()`.

- [ ] **Step 5: Run tests, data generation, and inspect tag counts**

```bash
bash gradlew test --tests com.pixulse.infx.item.R196CatalogTest
bash gradlew runData
find src/generated/resources/data -path '*/tags/item/*.json' -o -path '*/tags/item/*/*.json' | sort
```

Expected: unit tests pass; data generation succeeds; output includes `materials/*`, `repair_materials/*`, `equipment/*`, all six `tool_tier/*` files, `minecraft:arrows`, and vanilla enchantable/family tag additions.

- [ ] **Step 6: Commit tags and creative order**

```bash
git add src/main/java/com/pixulse/infx/tag/ModTags.java \
  src/main/java/com/pixulse/infx/data/ModItemTagsProvider.java \
  src/main/java/com/pixulse/infx/registry/ModCreativeTabs.java \
  src/test/java/com/pixulse/infx/item/R196CatalogTest.java
git commit -m "feat: generate R196 tags and creative order"
```

### Task 10: Generate complete English and Chinese language files

**Files:**

- Create: `src/main/java/com/pixulse/infx/data/ModLanguageProvider.java`
- Modify: `src/main/java/com/pixulse/infx/data/ModDataGenerators.java`
- Delete: `src/main/resources/assets/infx/lang/en_us.json`
- Delete: `src/main/resources/assets/infx/lang/zh_cn.json`
- Test: `src/test/java/com/pixulse/infx/item/R196CatalogTest.java`

- [ ] **Step 1: Add failing name-completeness assertions**

Append:

```java
@Test
void everyDefinitionHasTwoNamesAndApprovedTerminology() {
    for (R196Catalog.Entry entry : catalog().entries()) {
        assertFalse(entry.englishName().isBlank(), entry.path());
        assertFalse(entry.chineseName().isBlank(), entry.path());
    }
    assertEquals("InfiniteX Copper Pickaxe",
            catalog().equipment(R196Material.COPPER, R196EquipmentType.PICKAXE).englishName());
    assertEquals("InfiniteX 铜镐",
            catalog().equipment(R196Material.COPPER, R196EquipmentType.PICKAXE).chineseName());
    assertEquals("Ancient Metal War Hammer",
            catalog().equipment(R196Material.ANCIENT_METAL, R196EquipmentType.WAR_HAMMER).englishName());
    assertEquals("远古金属锁链胸甲",
            catalog().equipment(R196Material.ANCIENT_METAL, R196EquipmentType.CHAINMAIL_CHESTPLATE).chineseName());
    assertEquals("Gold Horse Armor",
            catalog().equipment(R196Material.GOLD, R196EquipmentType.HORSE_ARMOR).englishName());
    assertEquals("Bow", catalog().equipment(R196Material.WOOD, R196EquipmentType.BOW).englishName());
    assertEquals("Fishing Rod",
            catalog().equipment(R196Material.ADAMANTIUM, R196EquipmentType.FISHING_ROD).englishName());
    assertEquals("钓鱼竿",
            catalog().equipment(R196Material.FLINT, R196EquipmentType.FISHING_ROD).chineseName());
}
```

Run the catalog test. Expected: any incomplete type/name mapping fails with its exact path.

- [ ] **Step 2: Implement one locale-parameterized provider**

```java
final class ModLanguageProvider extends LanguageProvider {
    enum Locale {
        EN_US("en_us", Map.ofEntries(
                Map.entry("itemGroup.infx", "InfiniteX"),
                Map.entry("block.infx.flint_workbench", "Flint Workbench"),
                Map.entry("block.infx.copper_workbench", "Copper Workbench"),
                Map.entry("container.infx.flint_workbench", "Flint Workbench"),
                Map.entry("container.infx.copper_workbench", "Copper Workbench"),
                Map.entry("message.infx.workbench_obstructed", "The workbench needs clear space above it"),
                Map.entry("advancements.infx.open_inventory.title", "Taking Stock"),
                Map.entry("advancements.infx.open_inventory.description", "Open your inventory and assess your situation"),
                Map.entry("advancements.infx.stick_picker.title", "Stick Picker"),
                Map.entry("advancements.infx.stick_picker.description", "Find your first stick"),
                Map.entry("advancements.infx.cutting_edge.title", "Cutting Edge"),
                Map.entry("advancements.infx.cutting_edge.description", "Craft a flint hatchet"),
                Map.entry("advancements.infx.mine_wood.title", "Mine Wood"),
                Map.entry("advancements.infx.mine_wood.description", "Use the right tool to harvest a log"),
                Map.entry("advancements.infx.build_work_bench.title", "Build Work Bench"),
                Map.entry("advancements.infx.build_work_bench.description", "Craft a flint workbench"),
                Map.entry("advancements.infx.nuggets.title", "Nuggets"),
                Map.entry("advancements.infx.nuggets.description", "Recover a copper nugget from gravel"),
                Map.entry("advancements.infx.better_tools.title", "Better Tools"),
                Map.entry("advancements.infx.better_tools.description", "Build a copper workbench"),
                Map.entry("advancements.infx.build_pickaxe.title", "Build Pickaxe"),
                Map.entry("advancements.infx.build_pickaxe.description", "Craft an InfiniteX copper pickaxe"))) {
            @Override String name(R196Catalog.Entry entry) { return entry.englishName(); }
        },
        ZH_CN("zh_cn", Map.ofEntries(
                Map.entry("itemGroup.infx", "InfiniteX"),
                Map.entry("block.infx.flint_workbench", "燧石工具台"),
                Map.entry("block.infx.copper_workbench", "铜工具台"),
                Map.entry("container.infx.flint_workbench", "燧石工具台"),
                Map.entry("container.infx.copper_workbench", "铜工具台"),
                Map.entry("message.infx.workbench_obstructed", "工具台上方需要留出空间"),
                Map.entry("advancements.infx.open_inventory.title", "查看物品栏"),
                Map.entry("advancements.infx.open_inventory.description", "打开物品栏，确认眼下的处境"),
                Map.entry("advancements.infx.stick_picker.title", "拾枝者"),
                Map.entry("advancements.infx.stick_picker.description", "找到第一根木棍"),
                Map.entry("advancements.infx.cutting_edge.title", "锋芒初现"),
                Map.entry("advancements.infx.cutting_edge.description", "制作一把燧石短斧"),
                Map.entry("advancements.infx.mine_wood.title", "伐木"),
                Map.entry("advancements.infx.mine_wood.description", "用正确的工具采集原木"),
                Map.entry("advancements.infx.build_work_bench.title", "搭建工具台"),
                Map.entry("advancements.infx.build_work_bench.description", "制作燧石工具台"),
                Map.entry("advancements.infx.nuggets.title", "铜粒"),
                Map.entry("advancements.infx.nuggets.description", "从沙砾中取得一粒铜"),
                Map.entry("advancements.infx.better_tools.title", "更好的工具"),
                Map.entry("advancements.infx.better_tools.description", "搭建铜工具台"),
                Map.entry("advancements.infx.build_pickaxe.title", "制作铜镐"),
                Map.entry("advancements.infx.build_pickaxe.description", "制作 InfiniteX 铜镐"))) {
            @Override String name(R196Catalog.Entry entry) { return entry.chineseName(); }
        };
        final String code;
        final Map<String, String> baseTranslations;
        Locale(String code, Map<String, String> baseTranslations) {
            this.code = code;
            this.baseTranslations = baseTranslations;
        }
        abstract String name(R196Catalog.Entry entry);
    }

    private final Locale locale;

    ModLanguageProvider(PackOutput output, Locale locale) {
        super(output, InfiniteX.MOD_ID, locale.code);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        ModItems.catalog().entries().forEach(entry -> add("item.infx." + entry.path(), locale.name(entry)));
        locale.baseTranslations.forEach(this::add);
    }
}
```

The two literal maps preserve every existing non-catalog string byte-for-byte, including `InfiniteX` capitalization and the established Chinese terminology.

Register both providers:

```java
event.createProvider(output -> new ModLanguageProvider(output, ModLanguageProvider.Locale.EN_US));
event.createProvider(output -> new ModLanguageProvider(output, ModLanguageProvider.Locale.ZH_CN));
```

- [ ] **Step 3: Remove duplicate static locale resources and generate**

```bash
git rm src/main/resources/assets/infx/lang/en_us.json src/main/resources/assets/infx/lang/zh_cn.json
bash gradlew runData
```

Expected: `src/generated/resources/assets/infx/lang/en_us.json` and `zh_cn.json` exist; each has 237 `item.infx.*` catalog entries plus all pre-existing non-catalog keys. Data generation reports no duplicate translation key.

- [ ] **Step 4: Run tests and commit**

```bash
bash gradlew test
git add src/main/java/com/pixulse/infx/data/ModLanguageProvider.java \
  src/main/java/com/pixulse/infx/data/ModDataGenerators.java \
  src/test/java/com/pixulse/infx/item/R196CatalogTest.java
git commit -m "feat: generate R196 translations"
```

### Task 11: Generate ordinary, fishing, bow, and equipment models

**Files:**

- Create: `src/main/java/com/pixulse/infx/data/ModModelProvider.java`
- Create: `src/main/java/com/pixulse/infx/data/ModEquipmentAssetProvider.java`
- Modify: `src/main/java/com/pixulse/infx/data/ModDataGenerators.java`
- Delete: the nine static catalog item definitions and models listed in the file map

- [ ] **Step 1: Implement a catalog-only model provider**

Construct with `super(output, InfiniteX.MOD_ID)`. Override `getKnownBlocks()` with `Stream.empty()` so the existing static workbench blockstates remain authoritative. Override `getKnownItems()` with catalog holders wrapped through `BuiltInRegistries.ITEM.wrapAsHolder(entry.holder().value())`, excluding the two workbench block items.

In `registerModels`, dispatch every equipment entry by `ModelFamily`:

```java
switch (entry.key().type().modelFamily()) {
    case GENERATED -> itemModels.generateFlatItem(entry.holder().value(), ModelTemplates.FLAT_ITEM);
    case HANDHELD -> itemModels.generateFlatItem(entry.holder().value(), ModelTemplates.FLAT_HANDHELD_ITEM);
    case FISHING_ROD -> generateFishingRod(itemModels, entry);
    case BOW -> generateMaterialBow(itemModels, entry);
}
```

Raw items use `FLAT_ITEM`. Leather plate icons use `generateTwoLayerDyedItem`; all other armor icons use flat item models.

For fishing rods, use this helper so every rod has its own cast model but all nine resolve to the one approved cast texture:

```java
private static void generateFishingRod(
        ItemModelGenerators itemModels, R196Catalog.EquipmentEntry entry) {
    Item item = entry.holder().value();
    Identifier normalId = itemModels.createFlatItemModel(item, ModelTemplates.FLAT_HANDHELD_ROD_ITEM);
    Identifier castId = ModelLocationUtils.getModelLocation(item, "_cast");
    ModelTemplates.FLAT_HANDHELD_ROD_ITEM.create(
            castId,
            TextureMapping.layer0(new net.minecraft.client.resources.model.sprite.Material(
                    InfiniteX.id("item/fishing_rod_cast"))),
            itemModels.modelOutput);
    itemModels.itemModelOutput.accept(
            item,
            ItemModelUtils.conditional(
                    new FishingRodCast(),
                    ItemModelUtils.plainModel(castId),
                    ItemModelUtils.plainModel(normalId)));
}
```

- [ ] **Step 2: Generate all 30 arrowhead/pull branches per bow**

For each bow, create its standby `ModelTemplates.BOW` model against `infx:item/{bow_path}`. For every arrow material in `R196EquipmentType.ARROW.allowedMaterials()` and frame `0..2`, create a model ID `infx:item/{bow_path}/{arrow_material}_{frame}` with the same texture ID. Use:

```java
private static Identifier bowModel(
        ItemModelGenerators itemModels, Identifier modelId, Identifier textureId) {
    return ModelTemplates.BOW.create(
            modelId,
            TextureMapping.layer0(new net.minecraft.client.resources.model.sprite.Material(textureId)),
            itemModels.modelOutput);
}
```

Build each arrow's pull range and the complete bow definition:

```java
private static ItemModel.Unbaked pull(
        ItemModel.Unbaked frame0, ItemModel.Unbaked frame1, ItemModel.Unbaked frame2) {
    return ItemModelUtils.rangeSelect(
            new UseDuration(false),
            .05F,
            frame0,
            ItemModelUtils.override(frame1, .65F),
            ItemModelUtils.override(frame2, .9F));
}
```

Then register:

```java
ItemModel.Unbaked nocked = ItemModelUtils.select(
        new ComponentContents<>(ModDataComponents.NOCKED_ARROW_MATERIAL.get()),
        pulls.get(R196Material.FLINT),
        R196EquipmentType.ARROW.allowedMaterials().stream()
                .map(material -> ItemModelUtils.when(material.path(), pulls.get(material)))
                .toList());
itemModels.itemModelOutput.accept(
        bow,
        ItemModelUtils.conditional(ItemModelUtils.isUsingItem(), nocked, standby));
```

Before that final block, build `standby` with `bowModel(itemModels, ModelLocationUtils.getModelLocation(bow), InfiniteX.id("item/" + entry.path()))`, then fill an `EnumMap<R196Material, ItemModel.Unbaked>` by calling `bowModel` three times per arrow material and passing those three plain models to `pull`.

This produces three standby models and exactly 90 material/pull models.

- [ ] **Step 3: Generate the 17 equipment asset definitions**

`ModEquipmentAssetProvider` extends `EquipmentAssetProvider` and overrides `registerModels` without calling `super`. Emit one plate asset for leather plus the eight plate metals, and one chain asset for each of the eight metals.

For a plate asset:

```java
EquipmentClientInfo.Builder builder = EquipmentClientInfo.builder()
        .addHumanoidLayers(InfiniteX.id(material.path()), material == R196Material.LEATHER);
if (material == R196Material.LEATHER) {
    builder.addHumanoidLayers(InfiniteX.id("leather_overlay"), false);
}
if (R196EquipmentType.HORSE_ARMOR.allows(material)) {
    builder.addLayers(EquipmentClientInfo.LayerType.HORSE_BODY,
            new EquipmentClientInfo.Layer(InfiniteX.id(material.path())));
}
output.accept(plateAssetKey(material), builder.build());
```

For chain, use `addHumanoidLayers(InfiniteX.id(material.path() + "_chainmail"))` and the `infx:{material}_chainmail` asset key. `plateAssetKey` and the keys returned by `R196EquipmentKey.equipmentAsset()` must both use `ResourceKey.create(EquipmentAssets.ROOT_ID, InfiniteX.id(path))`.

```java
private static ResourceKey<EquipmentAsset> plateAssetKey(R196Material material) {
    return ResourceKey.create(EquipmentAssets.ROOT_ID, InfiniteX.id(material.path()));
}

private static ResourceKey<EquipmentAsset> chainAssetKey(R196Material material) {
    return ResourceKey.create(
            EquipmentAssets.ROOT_ID, InfiniteX.id(material.path() + "_chainmail"));
}
```

- [ ] **Step 4: Register providers and remove static duplicates**

Add:

```java
event.createProvider(ModModelProvider::new);
event.createProvider(ModEquipmentAssetProvider::new);
```

Delete only the nine catalog `assets/infx/items/*.json` and matching `models/item/*.json`. Keep workbench files.

- [ ] **Step 5: Generate and inspect exact output counts**

```bash
bash gradlew runData
find src/generated/resources/assets/infx/items -type f -name '*.json' | wc -l
find src/generated/resources/assets/infx/models/item -type f -name '*.json' | wc -l
find src/generated/resources/assets/infx/equipment -type f -name '*.json' | wc -l
```

Expected counts: 237 item definitions, 340 item models (237 base + 90 bow pull + 9 fishing cast + 4 leather dyed), and 17 equipment assets.

- [ ] **Step 6: Commit model generation**

```bash
git add src/main/java/com/pixulse/infx/data/ModModelProvider.java \
  src/main/java/com/pixulse/infx/data/ModEquipmentAssetProvider.java \
  src/main/java/com/pixulse/infx/data/ModDataGenerators.java
git add -u src/main/resources/assets/infx/items src/main/resources/assets/infx/models/item
git commit -m "feat: generate R196 client models"
```

### Task 12: Synchronize and pin the selected MITE textures

**Files:**

- Create: `scripts/sync-mite-equipment-textures.sh`
- Create: `src/main/resources/assets/infx/mite_texture_manifest.tsv`
- Create/replace: selected PNGs below `src/main/resources/assets/infx/textures`
- Modify: `THIRD_PARTY_NOTICES.md`
- Test: `src/test/java/com/pixulse/infx/client/R196TextureProvenanceTest.java`

- [ ] **Step 1: Write the failing provenance test**

Create:

```java
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
import java.util.Set;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class R196TextureProvenanceTest {
    private static final Path ROOT = Path.of("").toAbsolutePath();
    private static final Path ASSETS = ROOT.resolve("src/main/resources/assets/infx");
    private static final Path MANIFEST = ASSETS.resolve("mite_texture_manifest.tsv");

    @Test
    void everySelectedDestinationIsUniqueReadableAndHashPinned() throws Exception {
        List<String> lines = Files.readAllLines(MANIFEST, UTF_8);
        assertEquals("source_root\tsource\tdestination\tsha256", lines.getFirst());
        assertEquals(394, lines.size(), "header plus 393 selected destinations");
        Set<String> destinations = new HashSet<>();
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        for (String line : lines.subList(1, lines.size())) {
            String[] fields = line.split("\\t", -1);
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
    void optionalLocalSourcesMatchByteForByte() throws Exception {
        Path reference = ROOT.resolve("codex/reference");
        if (!Files.isDirectory(reference)) {
            reference = Path.of("/Users/inxups/IdeaProjects/mc/inf-x/codex/reference");
        }
        if (!Files.isDirectory(reference)) return;
        for (String line : Files.readAllLines(MANIFEST, UTF_8).subList(1, 394)) {
            String[] fields = line.split("\\t", -1);
            Path sourceRoot = switch (fields[0]) {
                case "resource-pack" -> reference.resolve("mite- resource-pack/assets/minecraft/textures");
                case "mite-src" -> reference.resolve("mite-src/assets/minecraft/textures");
                default -> throw new AssertionError("unknown source root " + fields[0]);
            };
            assertEquals(-1L, Files.mismatch(sourceRoot.resolve(fields[1]), ASSETS.resolve(fields[2])), fields[2]);
        }
    }

    @Test
    void noVanillaNamespaceOrUnapprovedArtifactIsCommitted() throws Exception {
        assertFalse(Files.exists(ROOT.resolve("src/main/resources/assets/minecraft")));
        Set<String> destinations = Files.readAllLines(MANIFEST, UTF_8).stream().skip(1)
                .map(line -> line.split("\\t", -1)[2]).collect(java.util.stream.Collectors.toSet());
        assertFalse(destinations.stream().anyMatch(path -> path.contains("diamond_helmet")));
        assertFalse(destinations.stream().anyMatch(path -> path.contains("iron_knife")));
        assertFalse(destinations.stream().anyMatch(path -> path.contains("stone_dagger")));
        assertFalse(destinations.stream().anyMatch(path -> path.contains("chip_flint_knife")));
        assertFalse(destinations.stream().anyMatch(path -> path.contains("iron_coin")));
    }
}
```

- [ ] **Step 2: Run the test and verify the missing-manifest failure**

```bash
bash gradlew test --tests com.pixulse.infx.client.R196TextureProvenanceTest
```

Expected: FAIL with `NoSuchFileException: /Users/inxups/IdeaProjects/mc/inf-x/src/main/resources/assets/infx/mite_texture_manifest.tsv`.

- [ ] **Step 3: Implement the deterministic synchronizer**

The script must use `set -euo pipefail`, compute the repository root from its own directory, accept optional resource-pack and source-texture roots as arguments, copy with `cp`, and write a sorted TSV through a temporary file. Use this header and `sync` function:

```bash
#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PACK_TEXTURES="${1:-$ROOT/codex/reference/mite- resource-pack/assets/minecraft/textures}"
SOURCE_TEXTURES="${2:-$ROOT/codex/reference/mite-src/assets/minecraft/textures}"
ASSET_ROOT="$ROOT/src/main/resources/assets/infx"
DEST_TEXTURES="$ASSET_ROOT/textures"
MANIFEST="$ASSET_ROOT/mite_texture_manifest.tsv"
ROWS="$(mktemp)"
trap 'rm -f "$ROWS" "$MANIFEST.tmp"' EXIT

if [[ -f "$MANIFEST" ]]; then
  tail -n +2 "$MANIFEST" | while IFS=$'\t' read -r _ _ destination _; do
    [[ -n "$destination" ]] && rm -f "$ASSET_ROOT/$destination"
  done
fi

sync() {
  local source_kind="$1" source_rel="$2" destination_rel="$3" source_root source hash
  case "$source_kind" in
    resource-pack) source_root="$PACK_TEXTURES" ;;
    mite-src) source_root="$SOURCE_TEXTURES" ;;
    *) echo "Unknown source kind: $source_kind" >&2; exit 1 ;;
  esac
  source="$source_root/$source_rel"
  [[ -f "$source" ]] || { echo "Missing approved source: $source" >&2; exit 1; }
  mkdir -p "$DEST_TEXTURES/$(dirname "$destination_rel")"
  cp "$source" "$DEST_TEXTURES/$destination_rel"
  if command -v sha256sum >/dev/null 2>&1; then
    hash="$(sha256sum "$source" | awk '{print $1}')"
  else
    hash="$(shasum -a 256 "$source" | awk '{print $1}')"
  fi
  printf '%s\t%s\t%s\t%s\n' "$source_kind" "$source_rel" "textures/$destination_rel" "$hash" >> "$ROWS"
}
```

Use these exact mappings and loops after `sync`:

```bash
sync resource-pack items/shards/flint.png item/flint_chip.png
sync resource-pack items/shards/obsidian.png item/obsidian_shard.png
sync resource-pack items/shards/emerald.png item/emerald_shard.png
sync resource-pack items/shards/diamond.png item/diamond_shard.png
sync resource-pack items/shards/quartz.png item/nether_quartz_shard.png
sync resource-pack items/shards/glass.png item/glass_shard.png
sync resource-pack items/sinew.png item/sinew.png
sync resource-pack items/manure.png item/manure.png
sync resource-pack items/nuggets/silver.png item/silver_nugget.png
sync resource-pack items/nuggets/mithril.png item/mithril_nugget.png
sync resource-pack items/nuggets/adamantium.png item/adamantium_nugget.png
sync resource-pack items/nuggets/ancient_metal.png item/ancient_metal_nugget.png
sync resource-pack items/ingots/silver.png item/silver_ingot.png
sync resource-pack items/ingots/mithril.png item/mithril_ingot.png
sync resource-pack items/ingots/adamantium.png item/adamantium_ingot.png
sync resource-pack items/ingots/ancient_metal.png item/ancient_metal_ingot.png

for material in copper silver gold rusted_iron iron ancient_metal mithril adamantium; do
  sync resource-pack "items/chains/$material.png" "item/${material}_chain.png"
done
for material in copper silver gold ancient_metal mithril adamantium; do
  sync resource-pack "items/coins/$material.png" "item/${material}_coin.png"
done
sync resource-pack items/frag/creeper.png item/creeper_frags.png
sync resource-pack items/frag/infernal_creeper.png item/infernal_creeper_frags.png
sync resource-pack items/frag/netherspawn.png item/netherspawn_frags.png

METALS=(copper silver gold rusted_iron iron ancient_metal mithril adamantium)
SHOVELS=(wood flint obsidian "${METALS[@]}")
ROCK_AND_METAL=(flint obsidian "${METALS[@]}")
FISHING=(flint obsidian copper silver gold iron ancient_metal mithril adamantium)
ARROWS=(flint obsidian copper silver gold rusted_iron iron ancient_metal mithril adamantium)
PLATE=(leather copper silver gold rusted_iron iron ancient_metal mithril adamantium)
HORSE=(copper silver gold iron ancient_metal mithril adamantium)
BOWS=(wood ancient_metal mithril)
PIECES=(helmet chestplate leggings boots)

sync_tool() {
  local material="$1" type="$2" key="${1}_${2}"
  case "$key" in
    wood_shovel)
      sync mite-src items/wood_shovel.png item/wood_shovel.png
      ;;
    iron_pickaxe|iron_shovel|iron_axe|iron_hoe|iron_sword)
      sync resource-pack "items/$key.png" "item/$key.png"
      ;;
    iron_shears)
      sync resource-pack items/shears.png item/iron_shears.png
      ;;
    *)
      sync resource-pack "items/tools/$key.png" "item/$key.png"
      ;;
  esac
}

for material in "${METALS[@]}"; do sync_tool "$material" pickaxe; done
for material in "${SHOVELS[@]}"; do sync_tool "$material" shovel; done
for type in hatchet axe; do
  for material in "${ROCK_AND_METAL[@]}"; do sync_tool "$material" "$type"; done
done
for type in hoe mattock battle_axe war_hammer scythe shears; do
  for material in "${METALS[@]}"; do sync_tool "$material" "$type"; done
done
sync_tool wood cudgel
sync_tool wood club
for material in flint obsidian; do sync_tool "$material" knife; done
for type in sword dagger; do
  for material in "${METALS[@]}"; do sync_tool "$material" "$type"; done
done

for material in "${FISHING[@]}"; do
  sync resource-pack "items/fishing_rods/${material}_uncast.png" "item/${material}_fishing_rod.png"
done
sync resource-pack items/fishing_rod_cast.png item/fishing_rod_cast.png

for material in "${ARROWS[@]}"; do
  sync resource-pack "items/arrows/${material}_arrow.png" "item/${material}_arrow.png"
done

for bow in "${BOWS[@]}"; do
  sync resource-pack "items/bows/$bow/standby.png" "item/${bow}_bow.png"
  for arrow in "${ARROWS[@]}"; do
    for frame in 0 1 2; do
      sync resource-pack \
        "items/bows/$bow/${arrow}_arrow_${frame}.png" \
        "item/${bow}_bow/${arrow}_${frame}.png"
    done
  done
done

for material in "${PLATE[@]}"; do
  for piece in "${PIECES[@]}"; do
    case "$material" in
      leather|iron)
        sync resource-pack "items/${material}_${piece}.png" "item/${material}_${piece}.png"
        ;;
      *)
        sync resource-pack "items/armor/${material}_${piece}.png" "item/${material}_${piece}.png"
        ;;
    esac
    if [[ "$material" == leather ]]; then
      sync resource-pack \
        "items/leather_${piece}_overlay.png" \
        "item/leather_${piece}_overlay.png"
    fi
  done
done

for material in "${METALS[@]}"; do
  for piece in "${PIECES[@]}"; do
    if [[ "$material" == iron ]]; then
      sync resource-pack \
        "items/chainmail_${piece}.png" \
        "item/iron_chainmail_${piece}.png"
    else
      sync resource-pack \
        "items/armor/${material}_chainmail_${piece}.png" \
        "item/${material}_chainmail_${piece}.png"
    fi
  done
done

for material in "${HORSE[@]}"; do
  case "$material" in
    gold|iron)
      sync resource-pack "items/${material}_horse_armor.png" "item/${material}_horse_armor.png"
      ;;
    *)
      sync resource-pack "items/armor/horse/${material}.png" "item/${material}_horse_armor.png"
      ;;
  esac
done

for material in "${PLATE[@]}"; do
  sync resource-pack \
    "models/armor/${material}_layer_1.png" \
    "entity/equipment/humanoid/${material}.png"
  sync resource-pack \
    "models/armor/${material}_layer_1.png" \
    "entity/equipment/humanoid_baby/${material}.png"
  sync resource-pack \
    "models/armor/${material}_layer_2.png" \
    "entity/equipment/humanoid_leggings/${material}.png"
done
sync resource-pack models/armor/leather_layer_1_overlay.png entity/equipment/humanoid/leather_overlay.png
sync resource-pack models/armor/leather_layer_1_overlay.png entity/equipment/humanoid_baby/leather_overlay.png
sync resource-pack models/armor/leather_layer_2_overlay.png entity/equipment/humanoid_leggings/leather_overlay.png

for material in "${METALS[@]}"; do
  source_stem="${material}_chainmail"
  [[ "$material" == iron ]] && source_stem=chainmail
  sync resource-pack \
    "models/armor/${source_stem}_layer_1.png" \
    "entity/equipment/humanoid/${material}_chainmail.png"
  sync resource-pack \
    "models/armor/${source_stem}_layer_1.png" \
    "entity/equipment/humanoid_baby/${material}_chainmail.png"
  sync resource-pack \
    "models/armor/${source_stem}_layer_2.png" \
    "entity/equipment/humanoid_leggings/${material}_chainmail.png"
done

for material in "${HORSE[@]}"; do
  sync resource-pack \
    "entity/horse/armor/horse_armor_${material}.png" \
    "entity/equipment/horse_body/${material}.png"
done
```

These calls encode the following provenance constraints:

- raw calls for the six `items/shards/{flint,obsidian,emerald,diamond,quartz,glass}.png`, sinew, manure, four missing nuggets, four missing ingots, all eight approved chains, the six approved coins (never iron), and the three `items/frag/*` files;
- `sync_tool(material,type)` defaults to `items/tools/{material}_{type}.png`; special cases are `wood_shovel` from the `mite-src` root's `items/wood_shovel.png`, five standard iron tools from `items/iron_{pickaxe,shovel,axe,hoe,sword}.png`, and iron shears from `items/shears.png`;
- invoke `sync_tool` with the exact allowed material lists for pickaxe, shovel, hatchet, axe, hoe, mattock, battle axe, war hammer, scythe, shears, cudgel, club, knife, sword, and dagger; fishing rods, bows, and arrows are handled separately;
- nine fishing uncast textures map from `items/fishing_rods/{material}_uncast.png` to `item/{material}_fishing_rod.png`, and `items/fishing_rod_cast.png` maps once to `item/fishing_rod_cast.png`;
- ten arrow icons map from `items/arrows/{material}_arrow.png` to `item/{material}_arrow.png`;
- each bow's `items/bows/{wood,ancient_metal,mithril}/standby.png` maps to `item/{material}_bow.png`; every `{arrow}_arrow_{0,1,2}.png` maps to `item/{bow}_bow/{arrow}_{0,1,2}.png`;
- leather plate icons and four overlays come from top-level `items/leather_*`; iron plate icons come from top-level `items/iron_*`; every other plate icon comes from `items/armor/{material}_{piece}.png`;
- iron chain icons come from top-level `items/chainmail_*`; every other chain icon comes from `items/armor/{material}_chainmail_{piece}.png`;
- gold/iron horse icons come from top-level `items/{gold,iron}_horse_armor.png`; the other five come from `items/armor/horse/{material}.png`;
- plate `models/armor/{material}_layer_1.png` maps to both `entity/equipment/humanoid/{material}.png` and `entity/equipment/humanoid_baby/{material}.png`, while layer 2 maps to `humanoid_leggings`; do the same three mappings for leather overlay;
- chain layer sources use `{material}_chainmail_layer_*`, except iron uses `chainmail_layer_*`; map them to `{material}_chainmail.png` under humanoid, humanoid_baby, and leggings;
- seven `entity/horse/armor/horse_armor_{material}.png` files map to `entity/equipment/horse_body/{material}.png`.

After copying, sort rows by destination, prepend the header, atomically move the manifest, and enforce the count:

```bash
row_count="$(wc -l < "$ROWS" | tr -d ' ')"
[[ "$row_count" == 393 ]] || { echo "Expected 393 textures, got $row_count" >&2; exit 1; }
{
  printf 'source_root\tsource\tdestination\tsha256\n'
  LC_ALL=C sort -t $'\t' -k3,3 "$ROWS"
} > "$MANIFEST.tmp"
mv "$MANIFEST.tmp" "$MANIFEST"
printf 'Synchronized %s approved MITE textures\n' "$row_count"
```

- [ ] **Step 4: Run the synchronizer and prove the pinned set**

```bash
bash scripts/sync-mite-equipment-textures.sh \
  '/Users/inxups/IdeaProjects/mc/inf-x/codex/reference/mite- resource-pack/assets/minecraft/textures' \
  '/Users/inxups/IdeaProjects/mc/inf-x/codex/reference/mite-src/assets/minecraft/textures'
bash gradlew test --tests com.pixulse.infx.client.R196TextureProvenanceTest
```

Expected: the script reports 393 selected destinations; all three provenance tests pass.

- [ ] **Step 5: Expand the third-party notice**

Replace `THIRD_PARTY_NOTICES.md` with:

```markdown
# Third-Party Notices

## MITE R196 material and equipment textures

InfiniteX includes a selected material-and-equipment texture set copied byte-for-byte from the locally supplied MITE R196 resource pack described by its metadata as “A Minecraft New Pack For 1.6.4-MITE Version:1.0.3 By Modded MITE Community.” Resource paths and filenames were adapted to the `infx` namespace for modern Minecraft.

`assets/infx/mite_texture_manifest.tsv` is the complete machine-readable list of included source paths, destination paths, and SHA-256 hashes. Every listed file uses the `resource-pack` source root except `items/wood_shovel.png`. That inherited base bitmap is absent from the overlay pack and is copied byte-for-byte from the supplied R196 source asset tree, recorded as the `mite-src` root.

The supplied bundles did not contain a named license. The InfiniteX project owner confirmed on 2026-07-17 that this selected texture set may be copied and distributed with this project. This notice applies only to files listed in the manifest and makes no statement about unselected files in either reference bundle.
```

- [ ] **Step 6: Commit textures, manifest, script, and notice**

```bash
git add scripts/sync-mite-equipment-textures.sh \
  src/main/resources/assets/infx/mite_texture_manifest.tsv \
  src/main/resources/assets/infx/textures \
  src/test/java/com/pixulse/infx/client/R196TextureProvenanceTest.java \
  THIRD_PARTY_NOTICES.md
git commit -m "feat: add approved R196 equipment textures"
```

### Task 13: Enforce generated-resource and clean-CI packaging contracts

**Files:**

- Create: `src/test/java/com/pixulse/infx/client/R196GeneratedResourceTest.java`
- Modify: `build.gradle`
- Modify: `.github/workflows/build.yml`

- [ ] **Step 1: Write the tagged generated-resource tests**

Create the complete class below. Tests read `src/generated/resources` directly, so stale classpath copies cannot conceal a missing data-generation output:

```java
package com.pixulse.infx.client;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pixulse.infx.item.R196Catalog;
import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.registry.ModItems;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("generated-resources")
class R196GeneratedResourceTest {
    private static final Path ROOT = Path.of("").toAbsolutePath();
    private static final Path GENERATED = ROOT.resolve("src/generated/resources");
    private static final Path STATIC = ROOT.resolve("src/main/resources");

    @Test
    void everyCatalogItemHasDefinitionModelAndTwoTranslations() throws Exception {
        JsonObject english = json(GENERATED.resolve("assets/infx/lang/en_us.json"));
        JsonObject chinese = json(GENERATED.resolve("assets/infx/lang/zh_cn.json"));
        for (R196Catalog.Entry entry : ModItems.catalog().entries()) {
            Path definition = GENERATED.resolve("assets/infx/items/" + entry.path() + ".json");
            Path model = GENERATED.resolve("assets/infx/models/item/" + entry.path() + ".json");
            assertAll(entry.path(),
                    () -> assertTrue(Files.isRegularFile(definition), "missing item definition"),
                    () -> assertTrue(Files.isRegularFile(model), "missing base model"),
                    () -> assertTrue(english.has("item.infx." + entry.path()), "missing en_us"),
                    () -> assertTrue(chinese.has("item.infx." + entry.path()), "missing zh_cn"));
        }
    }

    @Test
    void generatedCountsAreExact() throws Exception {
        assertEquals(237, jsonCount(GENERATED.resolve("assets/infx/items")));
        assertEquals(340, jsonCount(GENERATED.resolve("assets/infx/models/item")));
        assertEquals(17, jsonCount(GENERATED.resolve("assets/infx/equipment")));
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
                if (!root.has("textures")) continue;
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
        for (R196Catalog.EquipmentEntry entry : ModItems.catalog().equipmentEntries()) {
            if (entry.key().type() == R196EquipmentType.BOW) {
                String definition = Files.readString(
                        GENERATED.resolve("assets/infx/items/" + entry.path() + ".json"), UTF_8);
                for (var material : R196EquipmentType.ARROW.allowedMaterials()) {
                    assertTrue(definition.contains(material.path()), entry.path() + " missing " + material.path());
                    for (int frame = 0; frame < 3; frame++) {
                        assertTrue(Files.isRegularFile(GENERATED.resolve(
                                "assets/infx/models/item/" + entry.path() + "/" + material.path() + "_" + frame + ".json")));
                    }
                }
            } else if (entry.key().type() == R196EquipmentType.FISHING_ROD) {
                String definition = Files.readString(
                        GENERATED.resolve("assets/infx/items/" + entry.path() + ".json"), UTF_8);
                assertTrue(definition.contains(entry.path() + "_cast"), entry.path());
                assertTrue(Files.isRegularFile(GENERATED.resolve(
                        "assets/infx/models/item/" + entry.path() + "_cast.json")));
            }
        }
    }

    @Test
    void equipmentAssetsExposeEveryRequiredLayer() throws Exception {
        for (R196Catalog.EquipmentEntry entry : ModItems.catalog().equipmentEntries()) {
            var form = entry.key().type().armorForm();
            if (form == R196EquipmentType.ArmorForm.NONE) continue;
            String assetPath = entry.key().equipmentAsset().identifier().getPath();
            JsonObject layers = json(GENERATED.resolve("assets/infx/equipment/" + assetPath + ".json"))
                    .getAsJsonObject("layers");
            if (form == R196EquipmentType.ArmorForm.HORSE) {
                assertTrue(layers.has("horse_body"), entry.path());
            } else {
                assertAll(entry.path(),
                        () -> assertTrue(layers.has("humanoid")),
                        () -> assertTrue(layers.has("humanoid_baby")),
                        () -> assertTrue(layers.has("humanoid_leggings")));
            }
        }
    }

    @Test
    void manifestHasOnlyCatalogOrApprovedDerivedTextures() throws Exception {
        Set<String> destinations = new HashSet<>();
        for (String line : Files.readAllLines(
                STATIC.resolve("assets/infx/mite_texture_manifest.tsv"), UTF_8).subList(1, 394)) {
            destinations.add(line.split("\\t", -1)[2]);
        }
        for (R196Catalog.Entry entry : ModItems.catalog().entries()) {
            assertTrue(destinations.remove("textures/item/" + entry.path() + ".png"), entry.path());
        }
        assertTrue(destinations.remove("textures/item/fishing_rod_cast.png"));
        assertTrue(destinations.removeIf(path -> path.matches(
                "textures/item/(wood|ancient_metal|mithril)_bow/(flint|obsidian|copper|silver|gold|rusted_iron|iron|ancient_metal|mithril|adamantium)_[0-2]\\.png")));
        assertTrue(destinations.removeIf(path -> path.matches(
                "textures/item/leather_(helmet|chestplate|leggings|boots)_overlay\\.png")));
        assertTrue(destinations.removeIf(path -> path.startsWith("textures/entity/equipment/")));
        assertTrue(destinations.isEmpty(), () -> "unexpected selected textures " + destinations);
        assertFalse(Files.exists(STATIC.resolve("assets/minecraft")));
        assertFalse(Files.exists(GENERATED.resolve("assets/minecraft")));
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
}
```

Run before data generation. Expected: FAIL because the generated locale and item-definition files are absent.

- [ ] **Step 2: Split ordinary and generated-resource JUnit passes**

Update Gradle:

```groovy
tasks.named('test', Test).configure {
    useJUnitPlatform {
        excludeTags 'generated-resources'
    }
}

tasks.register('resourceTest', Test) {
    description = 'Validates resources produced by runData.'
    group = 'verification'
    testClassesDirs = sourceSets.test.output.classesDirs
    classpath = sourceSets.test.runtimeClasspath
    useJUnitPlatform {
        includeTags 'generated-resources'
    }
}

tasks.named('check').configure {
    dependsOn tasks.named('resourceTest')
}
```

Do not make `resourceTest` depend on `runData`: `runData` starts a compiled mod and can cause `processResources` to run before generated output exists. The required safe order is two Gradle invocations.

- [ ] **Step 3: Update CI to generate before building**

Replace the one build step with:

```yaml
      - name: Generate catalog resources
        run: ./gradlew runData

      - name: Build and verify with Gradle
        run: ./gradlew build
```

The second invocation sees generated files before `processResources`, and `build -> check -> resourceTest` validates them.

- [ ] **Step 4: Run the same two-invocation sequence used by clean CI**

```bash
bash gradlew test
bash gradlew runData
bash gradlew resourceTest
bash gradlew build
```

Expected: initial unit tests pass without generated resources; data generation succeeds; resource tests pass; build succeeds and packages generated resources.

- [ ] **Step 5: Inspect the built JAR**

```bash
JAR="$(find build/libs -maxdepth 1 -name 'infx-*.jar' -print -quit)"
jar tf "$JAR" | rg '^assets/infx/(items|models/item|equipment|lang|textures)/' | wc -l
if jar tf "$JAR" | rg -q '^assets/minecraft/|^codex/reference/|chip_flint_knife|stone_dagger|iron_knife|iron_coin'; then
  echo 'Unapproved packaged resource detected' >&2
  exit 1
fi
```

Expected: the first command prints a non-zero packaged resource count; the guard exits 0 with no output.

- [ ] **Step 6: Commit resource enforcement**

```bash
git add src/test/java/com/pixulse/infx/client/R196GeneratedResourceTest.java build.gradle .github/workflows/build.yml
git commit -m "test: enforce R196 resource packaging"
```

### Task 14: Add representative equipment GameTests

**Files:**

- Create: `src/main/java/com/pixulse/infx/gametest/ModEquipmentGameTests.java`
- Modify: `src/main/java/com/pixulse/infx/InfiniteX.java`

- [ ] **Step 1: Register a separate GameTest function set**

Use this registration block in `ModEquipmentGameTests`:

```java
private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
        DeferredRegister.create(Registries.TEST_FUNCTION, InfiniteX.MOD_ID);

private static final List<String> TEST_NAMES = List.of(
        "equipment_components",
        "tool_actions_and_wear",
        "harvest_tier_catalog",
        "material_arrows",
        "material_bows",
        "fishing_rods",
        "armor_and_horse_armor");

static {
    TEST_FUNCTIONS.register("equipment_components", () -> ModEquipmentGameTests::equipmentComponents);
    TEST_FUNCTIONS.register("tool_actions_and_wear", () -> ModEquipmentGameTests::toolActionsAndWear);
    TEST_FUNCTIONS.register("harvest_tier_catalog", () -> ModEquipmentGameTests::harvestTierCatalog);
    TEST_FUNCTIONS.register("material_arrows", () -> ModEquipmentGameTests::materialArrows);
    TEST_FUNCTIONS.register("material_bows", () -> ModEquipmentGameTests::materialBows);
    TEST_FUNCTIONS.register("fishing_rods", () -> ModEquipmentGameTests::fishingRods);
    TEST_FUNCTIONS.register("armor_and_horse_armor", () -> ModEquipmentGameTests::armorAndHorseArmor);
}

public static void register(IEventBus modBus) {
    TEST_FUNCTIONS.register(modBus);
    modBus.addListener(ModEquipmentGameTests::registerTests);
}

private static void registerTests(RegisterGameTestsEvent event) {
    Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
            InfiniteX.id("equipment"), new TestEnvironmentDefinition.AllOf());
    for (String name : TEST_NAMES) {
        ResourceKey<Consumer<GameTestHelper>> function =
                ResourceKey.create(Registries.TEST_FUNCTION, InfiniteX.id(name));
        TestData<Holder<TestEnvironmentDefinition<?>>> data = new TestData<>(
                environment,
                Identifier.withDefaultNamespace("empty"),
                200,
                0,
                true,
                Rotation.NONE);
        event.registerTest(function.identifier(), new FunctionGameTestInstance(function, data));
    }
}
```

Call `ModEquipmentGameTests.register(modBus)` after `ModGameTests.register(modBus)` in `InfiniteX`.

- [ ] **Step 2: Test catalog components at runtime**

```java
private static void equipmentComponents(GameTestHelper helper) {
    for (R196Catalog.EquipmentEntry entry : ModItems.catalog().equipmentEntries()) {
        R196EquipmentKey key = entry.key();
        ItemStack stack = entry.holder().value().getDefaultInstance();
        if (key.durability() > 0) {
            helper.assertTrue(stack.getOrDefault(DataComponents.MAX_DAMAGE, 0) == key.durability(),
                    key.path() + " max damage");
        }
        boolean melee = (key.type().category() == R196EquipmentCategory.TOOL
                || key.type().category() == R196EquipmentCategory.WEAPON)
                && key.type() != R196EquipmentType.FISHING_ROD
                && key.type() != R196EquipmentType.BOW
                && key.type() != R196EquipmentType.ARROW;
        if (melee) {
            helper.assertTrue(stack.has(DataComponents.WEAPON), key.path() + " weapon component");
            helper.assertTrue(stack.has(DataComponents.ATTRIBUTE_MODIFIERS), key.path() + " attributes");
        }
        if (key.type().armorForm() != R196EquipmentType.ArmorForm.NONE) {
            helper.assertTrue(stack.has(DataComponents.EQUIPPABLE), key.path() + " equippable component");
        }
        if (key.type() == R196EquipmentType.BOW) {
            helper.assertTrue(stack.getItem() instanceof R196BowItem, key.path() + " bow class");
        } else if (key.type() == R196EquipmentType.ARROW) {
            helper.assertTrue(stack.getItem() instanceof R196ArrowItem, key.path() + " arrow class");
        } else if (key.type() == R196EquipmentType.FISHING_ROD) {
            helper.assertTrue(stack.getItem() instanceof R196FishingRodItem, key.path() + " fishing class");
        }
    }
    helper.succeed();
}
```

- [ ] **Step 3: Test modern actions and hardness wear**

```java
private static void toolActionsAndWear(GameTestHelper helper) {
    ServerPlayer player = createPlayer(helper);
    BlockPos axePos = new BlockPos(1, 1, 1);
    BlockPos shovelPos = new BlockPos(2, 1, 1);
    BlockPos hoePos = new BlockPos(3, 1, 1);
    BlockPos mattockPos = new BlockPos(4, 1, 1);
    BlockPos wearPos = new BlockPos(5, 1, 1);

    useOn(helper, player, axePos, Blocks.OAK_LOG,
            ModItems.catalog().equipment(R196Material.COPPER, R196EquipmentType.AXE).holder().value());
    helper.assertTrue(helper.getBlockState(axePos).is(Blocks.STRIPPED_OAK_LOG), "axe must strip logs");

    useOn(helper, player, shovelPos, Blocks.GRASS_BLOCK,
            ModItems.catalog().equipment(R196Material.COPPER, R196EquipmentType.SHOVEL).holder().value());
    helper.assertTrue(helper.getBlockState(shovelPos).is(Blocks.DIRT_PATH), "shovel must create paths");

    useOn(helper, player, hoePos, Blocks.DIRT,
            ModItems.catalog().equipment(R196Material.COPPER, R196EquipmentType.HOE).holder().value());
    helper.assertTrue(helper.getBlockState(hoePos).is(Blocks.FARMLAND), "hoe must till soil");

    Item mattock = ModItems.catalog()
            .equipment(R196Material.COPPER, R196EquipmentType.MATTOCK).holder().value();
    useOn(helper, player, mattockPos, Blocks.DIRT, mattock);
    helper.assertTrue(helper.getBlockState(mattockPos).is(Blocks.DIRT_PATH), "mattock shovel action");
    useOn(helper, player, mattockPos, Blocks.DIRT_PATH, mattock);
    helper.assertTrue(helper.getBlockState(mattockPos).is(Blocks.FARMLAND), "mattock hoe fallback");

    ItemStack scythe = ModItems.catalog()
            .equipment(R196Material.COPPER, R196EquipmentType.SCYTHE).holder().value().getDefaultInstance();
    helper.assertTrue(scythe.getDestroySpeed(Blocks.WHEAT.defaultBlockState())
                    > scythe.getDestroySpeed(Blocks.STONE.defaultBlockState()),
            "scythe must be efficient against crops");

    var sheep = helper.spawn(EntityTypes.SHEEP, new BlockPos(6, 1, 1));
    ItemStack shears = ModItems.catalog()
            .equipment(R196Material.COPPER, R196EquipmentType.SHEARS).holder().value().getDefaultInstance();
    player.setItemInHand(InteractionHand.MAIN_HAND, shears);
    helper.assertTrue(shears.interactLivingEntity(player, sheep, InteractionHand.MAIN_HAND).consumesAction(),
            "material shears must interact with sheep");
    helper.assertTrue(sheep.isSheared(), "material shears must shear sheep");
    sheep.discard();

    var zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(7, 1, 1));
    ItemStack sword = ModItems.catalog()
            .equipment(R196Material.COPPER, R196EquipmentType.SWORD).holder().value().getDefaultInstance();
    player.setItemInHand(InteractionHand.MAIN_HAND, sword);
    float healthBefore = zombie.getHealth();
    player.attack(zombie);
    helper.assertTrue(zombie.getHealth() < healthBefore, "material sword must deal melee damage");
    helper.assertTrue(sword.getDamageValue()
                    == new R196EquipmentKey(R196Material.COPPER, R196EquipmentType.SWORD).attackWear(),
            "material sword must apply R196 hit wear exactly once");
    zombie.discard();

    helper.setBlock(wearPos, Blocks.OAK_LOG);
    BlockPos absoluteWearPos = helper.absolutePos(wearPos);
    BlockState state = helper.getBlockState(wearPos);
    ItemStack hatchet = ModItems.FLINT_HATCHET.get().getDefaultInstance();
    player.setItemInHand(InteractionHand.MAIN_HAND, hatchet);
    hatchet.mineBlock(helper.getLevel(), state, absoluteWearPos, player);
    int expected = ToolWearCalculator.damageForBreaking(
            state.getDestroySpeed(helper.getLevel(), absoluteWearPos), 4.0F / 3.0F);
    helper.assertTrue(hatchet.getDamageValue() == expected,
            "hardness wear expected " + expected + " but got " + hatchet.getDamageValue());

    removePlayer(player);
    helper.succeed();
}

private static void useOn(GameTestHelper helper, ServerPlayer player, BlockPos relativePos, Block block, Item item) {
    helper.setBlock(relativePos, block);
    helper.setBlock(relativePos.above(), Blocks.AIR);
    player.setItemInHand(InteractionHand.MAIN_HAND, item.getDefaultInstance());
    BlockPos absolute = helper.absolutePos(relativePos);
    BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false);
    InteractionResult result = item.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
    helper.assertTrue(result.consumesAction(), item + " use action must succeed");
}
```

- [ ] **Step 4: Test every harvest tier tag**

```java
private static void harvestTierCatalog(GameTestHelper helper) {
    Map<HarvestTier, R196EquipmentKey> representatives = Map.of(
            HarvestTier.FLINT, new R196EquipmentKey(R196Material.FLINT, R196EquipmentType.HATCHET),
            HarvestTier.COPPER, new R196EquipmentKey(R196Material.COPPER, R196EquipmentType.PICKAXE),
            HarvestTier.IRON, new R196EquipmentKey(R196Material.IRON, R196EquipmentType.PICKAXE),
            HarvestTier.ANCIENT_METAL, new R196EquipmentKey(R196Material.ANCIENT_METAL, R196EquipmentType.PICKAXE),
            HarvestTier.MITHRIL, new R196EquipmentKey(R196Material.MITHRIL, R196EquipmentType.PICKAXE),
            HarvestTier.ADAMANTIUM, new R196EquipmentKey(R196Material.ADAMANTIUM, R196EquipmentType.PICKAXE));

    representatives.forEach((tier, key) -> {
        ItemStack stack = ModItems.catalog().equipment(key.material(), key.type()).holder().value().getDefaultInstance();
        for (HarvestTier candidate : HarvestTier.values()) {
            helper.assertTrue(stack.is(ModTags.Items.toolTier(candidate)) == (candidate == tier),
                    key.path() + " unexpected tier tag " + candidate);
        }
    });
    helper.assertTrue(HarvestTier.ADAMANTIUM.satisfies(HarvestTier.FLINT), "top tier must satisfy flint");
    helper.assertFalse(HarvestTier.FLINT.satisfies(HarvestTier.COPPER), "flint must not satisfy copper");
    helper.succeed();
}
```

- [ ] **Step 5: Test arrows, bows, and fishing**

```java
private static void materialArrows(GameTestHelper helper) {
    ServerPlayer player = createPlayer(helper);
    Vec3 position = player.position();
    for (R196Material material : R196EquipmentType.ARROW.allowedMaterials()) {
        R196EquipmentKey key = new R196EquipmentKey(material, R196EquipmentType.ARROW);
        R196ArrowItem item = (R196ArrowItem) ModItems.catalog().equipment(material, R196EquipmentType.ARROW).holder().value();
        ItemStack stack = item.getDefaultInstance();
        AbstractArrow fired = item.createArrow(helper.getLevel(), stack, player, null);
        AbstractArrow dispensed = (AbstractArrow) item.asProjectile(helper.getLevel(), position, stack, Direction.NORTH);
        helper.assertTrue(fired.getPickupItemStackOrigin().is(item), key.path() + " fired pickup identity");
        helper.assertTrue(dispensed.getPickupItemStackOrigin().is(item), key.path() + " dispensed pickup identity");
        helper.assertTrue(dispensed.pickup == AbstractArrow.Pickup.ALLOWED, key.path() + " dispenser pickup");
        helper.assertTrue(Math.abs(item.baseDamage() - key.arrowBaseDamage()) < 1.0E-9, key.path() + " damage");
    }
    removePlayer(player);
    helper.succeed();
}

private static void materialBows(GameTestHelper helper) {
    ServerPlayer player = createPlayer(helper);
    Item silverArrow = ModItems.catalog()
            .equipment(R196Material.SILVER, R196EquipmentType.ARROW).holder().value();
    for (R196Material material : List.of(R196Material.WOOD, R196Material.ANCIENT_METAL, R196Material.MITHRIL)) {
        R196BowItem bowItem = (R196BowItem) ModItems.catalog()
                .equipment(material, R196EquipmentType.BOW).holder().value();
        ItemStack bow = bowItem.getDefaultInstance();
        player.setItemInHand(InteractionHand.MAIN_HAND, bow);
        player.getInventory().add(silverArrow.getDefaultInstance());
        int before = helper.getLevel().getEntities(EntityTypes.ARROW,
                player.getBoundingBox().inflate(32.0), arrow -> true).size();
        helper.assertTrue(bowItem.use(helper.getLevel(), player, InteractionHand.MAIN_HAND).consumesAction(),
                material.path() + " bow nock");
        helper.assertTrue("silver".equals(bow.get(ModDataComponents.NOCKED_ARROW_MATERIAL.get())),
                material.path() + " nocked model state");
        for (int tick = 0; tick < 20; tick++) {
            player.doTick();
        }
        player.releaseUsingItem();
        int after = helper.getLevel().getEntities(EntityTypes.ARROW,
                player.getBoundingBox().inflate(32.0), arrow -> true).size();
        helper.assertTrue(after == before + 1, material.path() + " bow must spawn one arrow");
        helper.assertTrue(!bow.has(ModDataComponents.NOCKED_ARROW_MATERIAL.get()),
                material.path() + " bow must clear nocked state");
    }
    removePlayer(player);
    helper.succeed();
}

private static void fishingRods(GameTestHelper helper) {
    ServerPlayer player = createPlayer(helper);
    for (R196Material material : List.of(R196Material.FLINT, R196Material.IRON, R196Material.ADAMANTIUM)) {
        R196FishingRodItem rod = (R196FishingRodItem) ModItems.catalog()
                .equipment(material, R196EquipmentType.FISHING_ROD).holder().value();
        player.setItemInHand(InteractionHand.MAIN_HAND, rod.getDefaultInstance());
        helper.assertTrue(rod.use(helper.getLevel(), player, InteractionHand.MAIN_HAND).consumesAction(),
                material.path() + " rod cast");
        helper.assertTrue(player.fishing != null, material.path() + " rod must create hook");
        helper.assertTrue(rod.use(helper.getLevel(), player, InteractionHand.MAIN_HAND).consumesAction(),
                material.path() + " rod retrieve");
        helper.assertTrue(player.fishing == null, material.path() + " retrieve must clear hook");
        helper.assertTrue(player.getMainHandItem().is(rod), material.path() + " rod identity");
    }
    removePlayer(player);
    helper.succeed();
}
```

- [ ] **Step 6: Test fractional player armor and horse armor**

```java
private static void armorAndHorseArmor(GameTestHelper helper) {
    ServerPlayer player = createPlayer(helper);
    equipSet(helper, player, R196Material.MITHRIL, R196EquipmentType.platePieces());
    helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.ARMOR) - 9.0) < 1.0E-6,
            "mithril plate must sum to 9");
    equipSet(helper, player, R196Material.MITHRIL, R196EquipmentType.chainPieces());
    helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.ARMOR) - 7.0) < 1.0E-6,
            "mithril chain must sum to 7");

    var horse = helper.spawn(EntityTypes.HORSE, new BlockPos(6, 1, 1));
    R196EquipmentKey horseKey = new R196EquipmentKey(
            R196Material.ADAMANTIUM, R196EquipmentType.HORSE_ARMOR);
    ItemStack horseArmor = ModItems.catalog()
            .equipment(horseKey.material(), horseKey.type()).holder().value().getDefaultInstance();
    horse.setItemSlot(EquipmentSlot.BODY, horseArmor);
    helper.assertTrue(Math.abs(horse.getAttributeValue(Attributes.ARMOR) - 7.0) < 1.0E-6,
            "adamantium horse armor must add 7");
    Equippable equippable = horseArmor.get(DataComponents.EQUIPPABLE);
    helper.assertTrue(equippable != null && equippable.assetId().orElseThrow().equals(horseKey.equipmentAsset()),
            "horse equipment asset");

    horse.discard();
    removePlayer(player);
    helper.succeed();
}

private static void equipSet(
        GameTestHelper helper,
        ServerPlayer player,
        R196Material material,
        List<R196EquipmentType> pieces) {
    for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
        player.setItemSlot(slot, ItemStack.EMPTY);
    }
    for (R196EquipmentType type : pieces) {
        R196EquipmentKey key = new R196EquipmentKey(material, type);
        ItemStack armor = ModItems.catalog().equipment(material, type).holder().value().getDefaultInstance();
        EquipmentSlot slot = type.armorType().orElseThrow().getSlot();
        player.setItemSlot(slot, armor);
        Equippable equippable = armor.get(DataComponents.EQUIPPABLE);
        helper.assertTrue(equippable != null && equippable.assetId().orElseThrow().equals(key.equipmentAsset()),
                key.path() + " equipment asset");
    }
}
```

Add the player lifecycle helpers once at the end of the class:

```java
private static final AtomicInteger PLAYER_SEQUENCE = new AtomicInteger();

private static ServerPlayer createPlayer(GameTestHelper helper) {
    GameProfile profile = new GameProfile(
            UUID.randomUUID(), "infx-equipment-" + PLAYER_SEQUENCE.incrementAndGet());
    CommonListenerCookie cookie = CommonListenerCookie.createInitial(profile, false);
    ServerPlayer player = new ServerPlayer(
            helper.getLevel().getServer(), helper.getLevel(), profile, cookie.clientInformation());
    Connection connection = new Connection(PacketFlow.SERVERBOUND);
    new EmbeddedChannel(connection);
    helper.getLevel().getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
    player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
    player.getFoodData().setFoodLevel(20);
    Vec3 position = helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(1, 2, 1)));
    player.snapTo(position.x, position.y, position.z, 0.0F, 0.0F);
    return player;
}

private static void removePlayer(ServerPlayer player) {
    if (player.containerMenu != player.inventoryMenu) {
        player.closeContainer();
    }
    player.level().getServer().getPlayerList().remove(player);
}
```

- [ ] **Step 7: Run all GameTests and commit**

```bash
bash gradlew runData
bash gradlew runGameTestServer
```

Expected: all existing crafting/harvest GameTests and all seven new equipment functions pass; the server exits successfully.

```bash
git add src/main/java/com/pixulse/infx/gametest/ModEquipmentGameTests.java src/main/java/com/pixulse/infx/InfiniteX.java
git commit -m "test: cover R196 equipment behavior"
```

### Task 15: Final regression, client inspection, and review

**Files:**

- Modify only files required by concrete failures found in this task.

- [ ] **Step 1: Run the complete required verification sequence**

Before making any completion claim, use `superpowers:verification-before-completion` and run each command as a separate invocation:

```bash
bash gradlew test
bash gradlew runData
bash gradlew resourceTest
bash gradlew runGameTestServer
bash gradlew build
```

Expected: every command exits 0 and reports `BUILD SUCCESSFUL`.

- [ ] **Step 2: Audit exact counts and forbidden IDs**

```bash
test "$(wc -l < src/test/resources/r196/catalog-paths.txt | tr -d ' ')" = 237
test "$(find src/generated/resources/assets/infx/items -type f -name '*.json' | wc -l | tr -d ' ')" = 237
test "$(find src/generated/resources/assets/infx/models/item -type f -name '*.json' | wc -l | tr -d ' ')" = 340
test "$(find src/generated/resources/assets/infx/equipment -type f -name '*.json' | wc -l | tr -d ' ')" = 17
if rg -n 'diamond_(pickaxe|shovel|axe|hoe|sword|helmet|chestplate|leggings|boots)|iron_knife|stone_dagger|chip_flint_knife|iron_coin' \
    src/main src/generated/resources/assets/infx src/test/resources/r196/catalog-paths.txt; then
  echo 'Forbidden catalog artifact found' >&2
  exit 1
fi
```

Expected: all `test` commands exit 0 and the forbidden-ID guard produces no matches.

- [ ] **Step 3: Inspect the development client**

Run:

```bash
bash gradlew runClient
```

In the client, open the InfiniteX tab and check:

- raw items, then flint/copper workbenches, then material-first equipment order;
- one ordinary generated item, one handheld item, every bow material with at least two arrowhead materials through all pull stages, and cast/uncast fishing rods;
- leather, metal plate, metal chain, and horse armor in inventory and equipped;
- resource-loading logs contain no missing model, texture, equipment, or translation errors.

Exit the client normally after the checklist passes.

- [ ] **Step 4: Request code review against the approved design**

Use `superpowers:requesting-code-review`. The reviewer must compare the diff to the design acceptance criteria, check the 237-path golden file, confirm the one source-asset exception, and verify no later-milestone behavior slipped into milestone 1. Resolve every concrete finding and rerun the affected focused command plus the complete sequence from Step 1.

- [ ] **Step 5: Commit only verified review fixes**

If review produced changes:

```bash
git add -A
git commit -m "fix: address R196 foundation review"
```

If review produced no changes, do not create an empty commit. Record the final `git status --short`, the five successful verification commands, catalog counts, and client inspection result in the implementation handoff.
