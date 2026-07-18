# InfiniteX

InfiniteX is a NeoForge 26.2 survival-progression overhaul inspired by the behavior of MITE R196 and rewritten for modern Minecraft APIs.

The current playable progression runs from flint gathering through the first iron pickaxe: flint tools, restricted harvesting, timed tiered-workbench crafting, gravel copper, a cobblestone furnace, iron smelting, and the copper and iron workbenches. Along that path, InfiniteX provides R196-shaped recipes for the flint axe and the core copper/iron pickaxe, shovel, axe, hoe, and sword sets.

The cobblestone furnace enforces the R196 heat-2 ceiling: wood and charcoal handle low-heat cooking, coal can smelt ordinary metal ores, lava and blaze rods exceed the furnace capacity, and the furnace mouth must remain unobstructed.

Clay and sandstone ovens provide the R196 heat-1 alternatives. The clay oven only accepts small inputs and fuels, while the sandstone oven accepts full blocks. Sand is processed four at a time: heat 1 produces sandstone and heat 2 produces glass.

The implementation uses InfiniteX-owned tags and NeoForge events instead of replacing vanilla harvest tags. Third-party tools can opt in with `infx:tool_tier/<tier>` and a correct vanilla `Tool` component.

## Development

```text
bash gradlew test
bash gradlew runData
bash gradlew runGameTestServer
bash gradlew build
```

This project does not redistribute MITE source code. Selected project-owner-approved MITE R196 textures are recorded in `assets/infx/mite_texture_manifest.tsv`.
