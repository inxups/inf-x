# InfiniteX

InfiniteX is a NeoForge 26.2 survival-progression overhaul inspired by the behavior of MITE R196 and rewritten for modern Minecraft APIs.

The current playable progression runs from flint gathering through the first mithril ingot: flint tools, restricted harvesting, timed tiered-workbench crafting, gravel copper, a cobblestone furnace, iron smelting, the first iron pickaxe, and high-heat mithril metallurgy. Along that path, InfiniteX provides R196-shaped recipes for the flint axe and the core copper/iron pickaxe, shovel, axe, hoe, and sword sets.

The cobblestone furnace enforces the R196 heat-2 ceiling: wood and charcoal handle low-heat cooking, coal can smelt ordinary metal ores, lava and blaze rods exceed the furnace capacity, and the furnace mouth must remain unobstructed.

Clay and sandstone ovens provide the R196 heat-1 alternatives. The clay oven only accepts small inputs and fuels, while the sandstone oven accepts full blocks. Sand is processed four at a time: heat 1 produces sandstone and heat 2 produces glass.

The remaining R196 furnace shells are also available. The large clay oven accepts full blocks but remains capped at heat 1, the obsidian furnace accepts lava at heat 3, and the netherrack furnace is the only furnace that accepts heat-4 blaze rods.

Mithril and adamantium ore blocks now restore the R196 hardness, harvesting, self-drop, and furnace progression. Mithril requires an iron-tier InfiniteX tool and heat 3; it generates in Overworld stone between Y=0 and Y=32 with a low-depth bias and three-block base veins. Adamantium requires a mithril-tier InfiniteX tool and heat 4. Its natural generation remains intentionally disabled until InfiniteX has MITE's Underworld dimension, where R196 originally placed the ore.

The implementation uses InfiniteX-owned tags and NeoForge events instead of replacing vanilla harvest tags. Third-party tools can opt in with `infx:tool_tier/<tier>` and a correct vanilla `Tool` component.

## Development

```text
bash gradlew test
bash gradlew runData
bash gradlew runGameTestServer
bash gradlew build
```

This project does not redistribute MITE source code. Selected project-owner-approved MITE R196 textures are recorded in `assets/infx/mite_texture_manifest.tsv`.
