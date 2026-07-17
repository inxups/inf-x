# InfiniteX

InfiniteX is a NeoForge 26.2 survival-progression overhaul inspired by the behavior of MITE R196 and rewritten for modern Minecraft APIs.

The current M1 milestone implements the first playable loop: flint gathering and a flint hatchet, restricted log and stone harvesting, timed hand/workbench crafting, gravel metal resources, a copper workbench, and the InfiniteX copper pickaxe.

The implementation uses InfiniteX-owned tags and NeoForge events instead of replacing vanilla harvest tags. Third-party tools can opt in with `infx:tool_tier/<tier>` and a correct vanilla `Tool` component.

## Development

```text
bash gradlew test
bash gradlew runData
bash gradlew runGameTestServer
bash gradlew build
```

This project does not redistribute MITE source code or textures.
