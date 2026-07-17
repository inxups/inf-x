# M1 Copper Loop Design

## Goal

Implement the first playable InfiniteX progression slice on NeoForge 26.2:

`sticks/leather -> flint and sinew -> flint hatchet -> logs -> flint workbench -> 36 copper nuggets -> four copper ingots -> copper workbench -> InfiniteX copper pickaxe`.

## Compatibility boundaries

- Harvest restrictions are declared with `infx` tags and enforced with NeoForge events. Vanilla harvest tags and Minecraft JAR resources are not replaced.
- A modded tool is accepted only when its `Tool` component can harvest the target and it belongs to an appropriate `infx:tool_tier/*` tag.
- Vanilla copper nuggets and ingots are shared resources. InfiniteX owns the MITE-style tools and workbenches.
- Only named vanilla recipe IDs are disabled. Recipes from other mods are never scanned or removed.
- InfiniteX loads after NeoForge so its same-ID `neoforge:never` resources also override NeoForge's c-tag rewrites of vanilla equipment recipes.
- Two narrow Mixins connect timed crafting to the player inventory. Custom workbenches use normal mod-owned menus. No Coremod is used.

## Harvest behavior

Survival players cannot break blocks in `infx:restricted_harvest` unless both the tool capability and the minimum material tier pass. Creative players retain vanilla behavior. Logs require flint; pickaxe-mineable blocks require copper, with vanilla `Tool` rules continuing to enforce higher requirements.

## Timed crafting

InfiniteX recipes use shaped and shapeless serializers under one recipe type. Each recipe has a required bench and positive difficulty. Clicking or shift-clicking the preview starts server-authoritative crafting. It repeats while ingredients remain, pauses at zero food, and resets on recipe/menu changes. Finished items enter the inventory or drop at the player's feet.

The period is:

```text
base = difficulty < 25 ? 25
     : difficulty <= 100 ? round(difficulty)
     : round((difficulty - 100)^0.8) + 100

period = max(25, floor(base / (1 + level*0.02 + benchBonus)))
```

Bench bonuses are hand `0.0`, flint `0.2`, and copper `0.3`.

## Content

The milestone adds flint chips, sinew, obsidian/emerald shards, silver/mithril/adamantium nuggets, a flint hatchet, flint and copper workbenches, and an InfiniteX copper pickaxe. Models reference vanilla textures only. Gravel uses the R196 nested random algorithm; the removed diamond branch returns gravel.

## Excluded

Player health/food caps, nutrition, quality, furnace tiers, world generation, world-first advancements, and creature rewrites are later milestones.
