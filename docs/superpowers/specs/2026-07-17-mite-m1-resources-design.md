# M1 MITE Resource Migration Design

## Goal

Replace the placeholder vanilla textures used by the current M1 InfiniteX content with the matching pixel art from the local MITE 1.6.4 resource-pack reference. The migration is deliberately limited to content already registered by InfiniteX and does not restyle vanilla Minecraft.

## Scope and ownership

- Copy twelve PNG files from the ignored local reference at `codex/reference/mite- resource-pack/` into the `infx` asset namespace.
- Preserve the source PNG bytes and native 16-by-16 resolution. Do not redraw, resize, recolor, or optimize them.
- Do not import the reference pack wholesale. GUI textures, sounds, language files, entities, unrelated items, and vanilla block textures remain excluded.
- Do not add resources below `assets/minecraft`; other resource packs remain free to override vanilla assets independently.
- The project owner confirmed that the selected textures may be copied and distributed with InfiniteX. Because the reference contains no `LICENSE` file or named license, add a factual third-party notice that identifies the source and the selected derived files without inventing license terms.

## File mapping

Legacy plural directories are converted to the modern singular resource paths used by the current NeoForge target.

| Reference source below `assets/minecraft/textures/` | InfiniteX destination below `assets/infx/textures/` |
| --- | --- |
| `items/shards/flint.png` | `item/flint_chip.png` |
| `items/shards/obsidian.png` | `item/obsidian_shard.png` |
| `items/shards/emerald.png` | `item/emerald_shard.png` |
| `items/sinew.png` | `item/sinew.png` |
| `items/nuggets/silver.png` | `item/silver_nugget.png` |
| `items/nuggets/mithril.png` | `item/mithril_nugget.png` |
| `items/nuggets/adamantium.png` | `item/adamantium_nugget.png` |
| `items/tools/flint_hatchet.png` | `item/flint_hatchet.png` |
| `items/tools/copper_pickaxe.png` | `item/copper_pickaxe.png` |
| `blocks/crafting_table/flint/top.png` | `block/flint_workbench_top.png` |
| `blocks/crafting_table/copper/front.png` | `block/copper_workbench_front.png` |
| `blocks/crafting_table/copper/side.png` | `block/copper_workbench_side.png` |

## Model updates

The nine existing item models keep their current generated or handheld parent and change only `textures.layer0` to the corresponding `infx:item/*` texture. The modern item-definition JSON files remain unchanged because they already point at the correct InfiniteX model IDs.

The flint workbench keeps its current cube model. Its upper face changes to `infx:block/flint_workbench_top`; its lower face and four wooden workbench faces continue to use vanilla textures.

The copper workbench keeps a static cube model and does not gain a facing block state. North and south use `infx:block/copper_workbench_front`, east and west use `infx:block/copper_workbench_side`, and the upper and lower faces continue to use the modern vanilla copper-block texture. Its particle texture remains the vanilla copper block. Block item definitions continue to reuse these block models.

No Java registration, menu, recipe, gameplay value, or crafting behavior changes as part of this migration.

## Provenance notice

Add `THIRD_PARTY_NOTICES.md` at the repository root if it does not exist. The notice names the MITE 1.6.4 resource pack, lists the twelve InfiniteX destination files, records that redistribution permission was confirmed by the project owner on 2026-07-17, and states that the source bundle did not include a named license. It must not imply that the remainder of the reference pack is included or licensed by InfiniteX.

## Isolation and compatibility

Work occurs on `codex/m1-mite-resources` in an isolated worktree based on the merged `master`. The main worktree's pre-existing `build.gradle` edit is left untouched. The ignored reference directory remains in the main worktree and is used only as a read-only copy source.

All added runtime assets live under `assets/infx`. This avoids global vanilla overrides and keeps compatibility with resource-pack ordering, other mods, and future Minecraft texture changes.

## Verification

1. Compare every destination PNG against its mapped source with `cmp` to prove byte-for-byte identity.
2. Parse all changed JSON model files and confirm every referenced `infx` texture exists.
3. Run `bash gradlew runData` to validate generated-data integration.
4. Run `bash gradlew build` to validate resources and packaging.
5. Start the development client and inspect the log through resource loading, confirming there are no missing InfiniteX model or texture messages.
6. Confirm `git diff --check` passes and that no file below `assets/minecraft` was added or modified.

## Acceptance criteria

- Every current M1 InfiniteX item displays its corresponding MITE texture from the `infx` namespace.
- Flint and copper workbenches use the selected MITE faces while retaining modern vanilla support faces.
- The twelve committed PNGs are byte-identical to their approved reference files.
- No unrelated MITE asset or vanilla override enters the mod JAR.
- Data generation, build, and client resource loading complete without InfiniteX missing-resource errors.
