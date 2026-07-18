# InfiniteX

InfiniteX is a NeoForge 26.2 survival-progression overhaul inspired by the behavior of MITE R196 and rewritten for modern Minecraft APIs.

The current playable progression runs from flint gathering through the first iron pickaxe: flint tools, restricted harvesting, timed tiered-workbench crafting, gravel copper, the copper workbench and pickaxe, a cobblestone furnace, iron smelting, the iron workbench, and the InfiniteX iron pickaxe.

The implementation uses InfiniteX-owned tags and NeoForge events instead of replacing vanilla harvest tags. Third-party tools can opt in with `infx:tool_tier/<tier>` and a correct vanilla `Tool` component.

## Development

```text
bash gradlew test
bash gradlew runData
bash gradlew runGameTestServer
bash gradlew build
```

This project does not redistribute MITE source code. Selected project-owner-approved MITE R196 textures are recorded in `assets/infx/mite_texture_manifest.tsv`.
