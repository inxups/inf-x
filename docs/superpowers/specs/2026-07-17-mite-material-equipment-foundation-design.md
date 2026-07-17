# MITE Material and Equipment Foundation Design

## Status

This design was approved in conversation on 2026-07-17. It defines the first
milestone of the larger goal to make every in-scope R196 material, tool,
weapon, armor set, and acquisition path playable in survival.

## Goal

Create one authoritative, testable catalog for the active MITE R196 material
and equipment set, then use it to register every in-scope InfiniteX item with
basic modern behavior and complete client assets.

This milestone establishes content and behavior foundations. It deliberately
does not make the higher tiers obtainable in survival yet. Later milestones
will add sources, metallurgy, workstations, recipes, repair, special effects,
and progression integration.

## Program decomposition

The complete user goal is split into five dependent milestones:

1. Material and equipment catalog, registration, basic behavior, and assets.
2. Material sources: ores, storage blocks, world generation, gravel branches,
   livestock products, and monster drops.
3. Metallurgy: heat levels, tiered furnaces, workbenches, and metal anvils.
4. Survival recipes: conversions, equipment manufacture, and tiered repair.
5. Behavior and progression integration: special tool actions, blocking,
   projectile recovery, silver and adamantium traits, fixed-point armor,
   vanilla bypass prevention, and final balance tests.

Each milestone receives its own design, plan, implementation, and verification
cycle. Completing this first milestone is not completion of the overall goal.

## Authoritative baseline

The content baseline is the locally supplied R196 source, resource pack, and
`codex/reference/MITE_R196_玩法改动总览.md`. When decompiled remnants or unused
textures conflict with the overview's enabled/disabled audit, the overview and
the decisions approved in this design control scope.

The user selected the following boundaries:

- Include active tools, weapons, player armor, and horse armor.
- Include rusted-iron equipment that exists as monster equipment or loot even
  when it has no normal crafting recipe.
- Include all in-scope non-food raw materials, including shards, metal forms,
  chains, coins, sinew, and manure.
- Include the diamond shard, but never add diamond equipment.
- Exclude disabled or unfinished artifacts.
- Ultimately provide full survival acquisition, but implement that through the
  later milestones above.

Registering the diamond shard supersedes the older no-diamond boundary only
for this item. This milestone gives it no recipe, loot source, ore, or tool
family. Its survival source must be explicitly designed in milestone 2.

## Scope

### Included now

- Immutable material, item-form, and equipment-type definitions.
- A complete allowed material/equipment matrix.
- InfiniteX registration for every catalog-owned item.
- Stable lookup APIs for later tags, recipes, loot, and advancements.
- R196 durability, enchantability, harvest efficiency, material damage,
  reach, and baseline armor values.
- Basic modern mining, use, melee, bow, arrow, fishing, armor, and horse-armor
  behavior.
- Complete English and Simplified Chinese display names.
- Item definitions, item models, equipment assets, and MITE-derived textures.
- Material, repair, tool-family, and harvest-tier tags.
- Deterministic creative-tab ordering.
- Catalog, formula, resource, GameTest, provenance, and packaging validation.

### Explicitly deferred

- Ores, storage blocks, world generation, and mob or livestock drops.
- Furnace heat, smelting, workbench tiers, anvils, and survival recipes.
- Item quality generation, quality XP costs, and quality durability scaling.
- Fixed-point damage reduction and armor degradation below half durability.
- Tool blocking, area harvesting, special crop handling, and custom
  enchantment interactions.
- Silver bonus damage, rust behavior, corrosion, and adamantium lava immunity.
- Material-arrow recovery chances and bow velocity bonuses.
- Coin XP exchange and manure fertilization behavior.
- Global changes to vanilla item stack limits or vanilla equipment properties.

### Excluded from this program slice

- Metal knives; only flint and obsidian knives are active.
- Stone dagger and the `chip_flint_knife` texture-only artifact.
- Iron coin and other texture-only coin variants.
- Creeper, infernal-creeper, and netherspawn fragment items.
- Buckets, doors, functional blocks, and material carrot-on-a-stick variants.
- Thrown web and bottle of disenchanting, which are miscellaneous functional
  items rather than raw materials or equipment in the approved boundary.
- Food, bowls, crops, records, books, and unrelated MITE items.
- Diamond tools, weapons, armor, and horse armor.
- Any wholesale vanilla texture override.

## Architecture

### `R196Material`

An immutable material profile owns:

- stable path and English/Chinese display fragments;
- durability multiplier and enchantability;
- stored maximum quality for the later quality milestone;
- material damage and harvest-efficiency multiplier;
- InfiniteX harvest tier, when the material can harvest blocks;
- repair ingredient/tag reference;
- flags such as metal, rocky, silver, rusted iron, or lava-safe.

The flags are data only in this milestone unless a basic item component needs
them. Later systems consume the same profile instead of rebuilding material
switch statements.

### `R196EquipmentType`

An immutable equipment-type profile owns:

- stable path and display-name pattern;
- item category and factory;
- allowed materials;
- durability component count;
- base damage, reach, mining rules, and wear profile;
- modern attack-speed analog;
- model family and texture naming rule;
- armor slot, chain/plate form, or horse-armor data where applicable.

### `R196EquipmentCatalog`

The catalog uses `(material, equipmentType)` as its key. It is the only list
used to register equipment and to generate tags, models, language, creative
entries, and the later recipe/loot references.

It exposes non-null lookups that either return the requested holder or fail
with a message containing the missing key. It also exposes ordered views by
material and category. Maps and lists are immutable once registration setup
finishes.

### Compatibility aliases

Existing public holders such as `ModItems.FLINT_HATCHET` and
`ModItems.COPPER_PICKAXE` remain available as aliases into the catalog. Their
registry IDs do not change. Current recipes, advancements, tags, loot logic,
and GameTests therefore continue to compile without a flag-day migration.

Future code should query the catalog rather than add one public field for every
new combination.

## Item ownership

InfiniteX reuses a vanilla raw item only when it is the exact modern equivalent
and global replacement would create needless duplicate currencies. It owns all
equipment, including leather-, gold-, and iron-based equipment, because R196
durability and combat properties differ from vanilla.

### Reused vanilla raw items

- copper, gold, and iron nuggets;
- copper, gold, and iron ingots;
- flint, string, leather, feather, sticks, and obsidian;
- diamond, emerald, nether quartz, and glass where later conversions require
  the full item or block.

Reused vanilla items retain modern stack limits and default components. This
milestone uses a modern stack limit of 64 for newly registered raw materials as
well, avoiding inconsistent inventory rules. R196's smaller ingot and chain
stacks belong to a later global inventory/balance decision.

### InfiniteX raw-material items

| Group | Registry paths | Count |
| --- | --- | ---: |
| Shards | `flint_chip`, `obsidian_shard`, `emerald_shard`, `diamond_shard`, `nether_quartz_shard`, `glass_shard` | 6 |
| Bindings/fertilizer | `sinew`, `manure` | 2 |
| Missing nuggets | `silver_nugget`, `mithril_nugget`, `adamantium_nugget`, `ancient_metal_nugget` | 4 |
| Missing ingots | `silver_ingot`, `mithril_ingot`, `adamantium_ingot`, `ancient_metal_ingot` | 4 |
| Chains | copper, silver, gold, rusted iron, iron, ancient metal, mithril, and adamantium `*_chain` | 8 |
| Coins | copper, silver, gold, ancient metal, mithril, and adamantium `*_coin` | 6 |
| **Total** |  | **30** |

Coins store their R196 XP values in catalog metadata for milestone 5, but they
do not grant, consume, or exchange XP in this milestone. Manure is a normal
material item until its production and fertilization behavior are designed.

## Material profiles

The following values come from R196's `EnumEquipmentMaterial`, `Material`, and
`ItemTool` logic. Maximum quality is stored but not applied yet.

| Material | Durability | Enchantability | Max quality | Material damage | Harvest efficiency | InfiniteX harvest tier |
| --- | ---: | ---: | --- | ---: | ---: | --- |
| Leather | 1 | 10 | Fine | — | — | — |
| Wood | 0.5 | 10 | Fine | 0 | 1.0 | — |
| Flint | 1 | 0 | Fine | 1 | 1.25 | Flint |
| Obsidian | 2 | 0 | Fine | 2 | 1.5 | Copper |
| Rusted iron | 4 | 0 | Poor | 2 | 1.25 | Copper |
| Copper | 4 | 30 | Excellent | 3 | 1.75 | Copper |
| Silver | 4 | 30 | Excellent | 3 | 1.75 | Copper |
| Gold | 4 | 50 | Superb | 2 | 1.75 | Copper |
| Iron | 8 | 30 | Masterwork | 4 | 2.0 | Iron |
| Ancient metal | 16 | 40 | Masterwork | 4 | 2.0 | Ancient metal |
| Mithril | 64 | 100 | Legendary | 5 | 2.5 | Mithril |
| Adamantium | 256 | 40 | Legendary | 6 | 3.0 | Adamantium |

Gold intentionally remains at the copper harvest tier. Obsidian also maps to
the copper tier even though it is a rocky rather than metal material.

## Equipment matrices

Registry paths use `{material}_{type}`. Examples are
`ancient_metal_war_hammer`, `silver_chainmail_helmet`, and
`adamantium_horse_armor`. The wood key yields paths such as `wood_shovel` and
`wood_bow`.

### Tools

| Type | Allowed materials | Count |
| --- | --- | ---: |
| Pickaxe | copper, silver, gold, rusted iron, iron, ancient metal, mithril, adamantium | 8 |
| Shovel | wood, flint, obsidian, copper, silver, gold, rusted iron, iron, ancient metal, mithril, adamantium | 11 |
| Hatchet | flint, obsidian, copper, silver, gold, rusted iron, iron, ancient metal, mithril, adamantium | 10 |
| Axe | flint, obsidian, copper, silver, gold, rusted iron, iron, ancient metal, mithril, adamantium | 10 |
| Hoe | copper, silver, gold, rusted iron, iron, ancient metal, mithril, adamantium | 8 |
| Mattock | copper, silver, gold, rusted iron, iron, ancient metal, mithril, adamantium | 8 |
| Battle axe | copper, silver, gold, rusted iron, iron, ancient metal, mithril, adamantium | 8 |
| War hammer | copper, silver, gold, rusted iron, iron, ancient metal, mithril, adamantium | 8 |
| Scythe | copper, silver, gold, rusted iron, iron, ancient metal, mithril, adamantium | 8 |
| Shears | copper, silver, gold, rusted iron, iron, ancient metal, mithril, adamantium | 8 |
| Fishing rod | flint, obsidian, copper, silver, gold, iron, ancient metal, mithril, adamantium | 9 |
| **Total** |  | **96** |

### Weapons and ammunition

| Type | Allowed materials | Count |
| --- | --- | ---: |
| Cudgel | wood | 1 |
| Club | wood | 1 |
| Knife | flint, obsidian | 2 |
| Sword | copper, silver, gold, rusted iron, iron, ancient metal, mithril, adamantium | 8 |
| Dagger | copper, silver, gold, rusted iron, iron, ancient metal, mithril, adamantium | 8 |
| Bow | wood, ancient metal, mithril | 3 |
| Arrow | flint, obsidian, copper, silver, gold, rusted iron, iron, ancient metal, mithril, adamantium | 10 |
| **Total** |  | **33** |

### Armor

Each player-armor row expands to helmet, chestplate, leggings, and boots.

| Form | Allowed materials | Count |
| --- | --- | ---: |
| Plate armor | leather, copper, silver, gold, rusted iron, iron, ancient metal, mithril, adamantium | 36 |
| Chainmail armor | copper, silver, gold, rusted iron, iron, ancient metal, mithril, adamantium | 32 |
| Horse armor | copper, silver, gold, iron, ancient metal, mithril, adamantium | 7 |
| **Total** |  | **75** |

The catalog therefore owns 204 equipment items and 30 raw-material items, for
234 catalog-owned mod items. Nine already exist in the current project (seven
raw items plus the flint hatchet and copper pickaxe), so this milestone adds
225 new registry entries while preserving those nine IDs.

## Basic properties and behavior

### Tool formulas

Tool maximum durability is:

```text
4 × durability components × material durability multiplier × 100
```

Base mining speed is:

```text
4 × tool-type mining multiplier × material harvest-efficiency multiplier
```

Melee damage is:

```text
tool-type base damage + material damage
```

Implementation accounts for the player's built-in attack-damage attribute so
the displayed final damage, not merely the modifier, matches the formula.

| Type | Durability components | Base damage | Reach bonus | Mining multiplier |
| --- | ---: | ---: | ---: | ---: |
| Pickaxe | 3 | 2 | 0.75 | 1.0 |
| Shovel | 1 | 1 | 0.75 | 1.0 |
| Hatchet | 1 | 2 | 0.5 | 0.5 |
| Axe | 3 | 3 | 0.75 | 1.0 |
| Hoe | 2 | 1 | 0.75 | 0.5 |
| Mattock | 4 | 1 | 0.75 | 0.75 |
| Battle axe | 4 | 4 | 0.75 | 0.75 |
| War hammer | 5 | 2 | 0.75 | 0.75 |
| Scythe | 2 | 1 | 1.0 | 1.0 |
| Shears | 2 | 0 | 0.5 | 1.0 |
| Sword | 2 | 4 | 0.75 | 0.5 |
| Dagger | 1 | 2 | 0.5 | 0.5 |
| Knife | 1 | 1 | 0.25 | 0.5 |
| Club | 2 | 2 | 0.5 | 0.5 |
| Cudgel | 1 | 1 | 0.25 | 0.5 |

Per-block and per-hit wear retain the R196 type multipliers through the
existing hardness-scaled wear service. The profile includes special cases such
as faster hatchet wear and reduced mattock/war-hammer wear. Quality and
enchantment-dependent wear modifiers are deferred.

### Modern attack speed

R196 predates the attack-cooldown system, so there is no source value to copy.
Each type stores an explicit modern combat analog:

- pickaxe uses the iron-pickaxe `-2.8` attack-speed modifier;
- shovel and mattock use the iron-shovel `-3.0` modifier;
- axe and battle axe use the iron-axe `-3.1` modifier;
- hatchet preserves the current InfiniteX `-3.2` modifier;
- war hammer, club, and cudgel use the mace `-3.4` modifier;
- hoe and scythe use the iron-hoe `-1.0` modifier;
- sword, dagger, and knife use the iron-sword `-2.4` modifier;
- shears add no special attack-speed modifier.

The implementation copies the numeric defaults into catalog fields and tests
them. It does not read mutable vanilla item stacks at runtime. The existing
copper-pickaxe `-2.8` cadence remains unchanged through the pickaxe analog.

### Mining and item use

- Pickaxes and war hammers use pickaxe mining rules.
- Shovels use shovel mining rules and path creation.
- Axes, hatchets, and battle axes use axe mining and modern axe interactions.
- Hoes till soil; mattocks combine shovel mining with hoe use.
- Scythes mine plant/crop tags efficiently without area harvesting yet.
- Material shears perform standard shearing and shears-effective mining.
- Flint and obsidian knives are efficient against their R196 soft targets.
- Harvest-tier item tags are generated from the material profile and continue
  to work with the existing InfiniteX harvest event.

### Bows, arrows, and fishing rods

- Wood, mithril, and ancient-metal bows use standard modern bow input and a
  20-tick full draw.
- Their maximum durability is 32, 128, and 64 respectively, matching R196.
- Every catalog arrow can be selected, fired, dispensed, and picked up as its
  own item.
- Arrow base damage is `0.5 + materialDamage × 0.5` before bow/enchantment
  modifiers.
- Recovery probability, silver bonus damage, and reinforced-bow velocity
  bonuses remain stored metadata until milestone 5.
- All nine fishing rods use normal modern cast/retrieve behavior. Durability is
  `2 × hookMaterialDurability`, plus one for flint, matching R196.

### Armor

Plate durability is:

```text
slot components × material durability multiplier × 2
```

Chainmail durability omits the final `× 2`. Slot components are helmet 5,
chestplate 8, leggings 7, and boots 4.

Full-set R196 protection values are leather 2, rusted iron 6, copper 7,
silver 7, gold 6, iron/ancient metal 8, mithril 9, and adamantium 10. Chainmail
subtracts 2 from its material's full-set value. Each piece receives its
`slotComponents / 24` share so a complete set sums to the source value.

Horse-armor protection is gold 3, copper/silver 4, iron/ancient metal 5,
mithril 6, and adamantium 7.

This milestone equips these values through modern item components and uses
modern Minecraft's damage-reduction calculation. Replacing that calculation
with R196 fixed-point armor is a milestone-5 behavior change.

### Repair metadata

Every repairable item receives a material repair tag now. Metal equipment tags
point to the corresponding nugget, while leather, wood, flint, and obsidian use
their approved material item/tag. The later anvil milestone owns repair amount,
anvil-tier checks, XP rules, and whether vanilla-anvil repair must be disabled.

## Resources and data flow

The catalog drives all derived registration and resource outputs:

```text
material/type catalog
  ├─ ModItems registration
  ├─ material, repair, tool-family, and harvest-tier tags
  ├─ item definitions and item models
  ├─ equipment asset definitions
  ├─ en_us and zh_cn language files
  └─ deterministic creative-tab entries
```

### Static and generated resources

- Approved PNG files live in `src/main/resources/assets/infx/textures`.
- Item definitions, models, equipment JSON, language, and tags are data
  generated into `src/generated/resources`.
- Existing InfiniteX language entries move into language providers so main and
  generated resources never provide the same locale path.
- Raw items and armor icons use generated-item models.
- Handheld tools and weapons use handheld models.
- Fishing rods use the modern cast-state item model.
- Bows select their nocked arrow material and then range-dispatch across the
  three pull frames, preserving all ten R196 arrowhead displays for each bow.
- Legacy armor layers map to modern humanoid and humanoid-leggings equipment
  layers. Horse textures map to the modern equestrian equipment layer.

### Language terminology

English names follow the R196 English resource pack. Simplified Chinese keeps
the terminology already established by InfiniteX and the project overview:

- hatchet `短斧`, mattock `鹤嘴锄`, battle axe `战斧`, war hammer `战锤`, and
  scythe `镰刀`;
- cudgel `短木棒`, club `木棒`, dagger `匕首`, and knife `小刀`;
- rusted iron `锈铁`, ancient metal `远古金属`, mithril `秘银`, and adamantium
  `艾德曼`;
- chainmail piece names use `锁链`, and horse armor uses `马铠`.

The existing `燧石短斧` and `InfiniteX 铜镐` names remain unchanged. Catalog
templates generate every other combination from these fixed fragments.

### Texture migration

Only textures required by the approved catalog are copied from
`codex/reference/mite- resource-pack`. PNG bytes and dimensions remain
unchanged; only the destination namespace/path is modernized.

No file is added below `assets/minecraft`. Reused vanilla raw materials use
their current vanilla assets. Disabled metal knives, stone dagger, iron coin,
material carrot-on-a-stick, and unrelated pack assets are not copied.

`THIRD_PARTY_NOTICES.md` receives the complete expanded destination list and
records the project owner's 2026-07-17 approval of this selected material and
equipment asset set. A machine-readable SHA-256 manifest provides an auditable
mapping from selected source to destination.

### Creative ordering

The InfiniteX tab remains one tab in this milestone. Entries are deterministic:

1. raw materials and components;
2. material tier in this exact order: leather, wood, flint, obsidian, gold,
   copper, silver, rusted iron, iron, ancient metal, mithril, adamantium;
3. tools;
4. weapons and ammunition;
5. plate armor, chainmail, and horse armor.

Existing workbenches remain present and retain their relative early-game
placement.

## Failure handling

Catalog setup and data generation fail immediately for:

- duplicate registry paths or duplicate `(material, type)` keys;
- an illegal material/equipment combination;
- a missing repair ingredient or required tag;
- a missing compatibility alias;
- a missing English or Chinese name;
- a missing model, texture, equipment asset, or bow-state branch;
- an expected texture whose SHA-256 differs from the approved source;
- an unexpected disabled item in the catalog.

Lookups never return `null` and never silently substitute another material.
Errors include the offending registry path or catalog key.

The ignored local reference pack is not required to compile the mod or run CI.
Committed destination hashes remain verifiable from the manifest. A local
source-to-destination comparison runs when the reference directory is present.

## Testing and verification

### Unit tests

- Check every material value and harvest-tier mapping.
- Check durability, mining speed, damage, reach, armor, bow, arrow, and fishing
  formulas at low, middle, and high material tiers.
- Compare the catalog against an independent golden manifest of all 234
  expected registry paths.
- Check exact tool, weapon, armor, and raw-item counts.
- Check ID uniqueness, allowed matrices, exclusions, and compatibility aliases.
- Check vanilla raw-item reuse and mod ownership of all equipment.

### Resource tests

- Every catalog-owned item has an item definition, model, and two translations.
- Every model reference resolves to a committed texture.
- Every armor item resolves to the correct modern equipment asset and layer.
- Every bow has standby plus all material/pull combinations.
- Every fishing rod has cast and uncast models.
- Every provenance entry resolves and matches its committed SHA-256.
- No runtime resource exists below `assets/minecraft`.

### GameTests

Representative GameTests cover each behavior family and every harvest tier:

- correct and incorrect mining, harvest-tier enforcement, and hardness wear;
- hoe, mattock, axe-family, scythe, and shears interactions;
- melee damage and durability;
- each material-arrow family, bow firing, pickup identity, and dispensing;
- fishing-rod cast/retrieve behavior;
- plate and chainmail equipment and summed base protection;
- horse-armor equipping and protection;
- existing flint-to-copper timed-crafting and progression scenarios.

Pure client presentation is also checked in a development client because
server GameTests cannot prove rendered model correctness.

### Required commands

Run, in order:

```text
bash gradlew test
bash gradlew runData
bash gradlew runGameTestServer
bash gradlew build
```

Then start the development client and inspect resource-loading logs and the
InfiniteX creative tab. Finally inspect the built JAR to confirm that it
contains every expected catalog asset, no `assets/minecraft` entry, no local
reference source, and no unapproved texture.

## Acceptance criteria

- The catalog contains exactly 234 approved mod-owned items: 30 raw materials
  and 204 equipment items.
- All 96 tools, 33 weapons/ammunition items, 68 player-armor pieces, and 7
  horse-armors are registered and basic-usable.
- All material and type formulas match the approved R196 source mapping.
- All catalog items have complete InfiniteX models, MITE-derived approved
  textures where owned, and English/Chinese names.
- Existing item IDs, copper-loop recipes, advancements, harvest behavior, and
  tests remain compatible.
- No excluded artifact, unrelated MITE asset, or vanilla-namespace override is
  packaged.
- Unit tests, data generation, GameTests, build, client resource loading, and
  JAR inspection pass.
