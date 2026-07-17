# M1 Copper Loop Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:executing-plans` and follow test-driven development. Steps use checkbox syntax for tracking.

**Goal:** Build the first survival progression loop through an InfiniteX copper pickaxe.

**Architecture:** Data tags and NeoForge events enforce harvesting; custom recipes and menus implement timed crafting; two conditional Mixins connect the player inventory. Content is registered in focused modules and generated resources replace only explicit vanilla recipes.

**Tech Stack:** Java 25, NeoForge 26.2.0.21-beta, Gradle 9.6.1, Mixin, JUnit Jupiter, GameTest.

---

### Task 1: Test harness and bootstrap

- [ ] Add JUnit Jupiter 5.13.4 and `useJUnitPlatform()`.
- [ ] Remove MDK example registrations and metadata.
- [ ] Create focused item, block, menu, recipe, loot, event, and client registries.
- [ ] Run `bash gradlew test` and `bash gradlew build`.

### Task 2: Harvesting and tools

- [ ] Write failing tests for tier order and tool damage calculations.
- [ ] Implement `HarvestTier`, tag constants, and pure harvest policy.
- [ ] Register the flint hatchet and InfiniteX copper pickaxe with R196 speed, reach, durability, and hardness-based wear.
- [ ] Enforce restrictions through `BreakBlockEvent` and add GameTests.

### Task 3: Gravel resources

- [ ] Write failing deterministic tests for every R196 random branch.
- [ ] Register early material items and the global loot modifier.
- [ ] Preserve unrelated loot, replace vanilla gravel/flint, and return gravel for the diamond branch.

### Task 4: Timed recipes

- [ ] Write failing tests for the period formula and state transitions.
- [ ] Implement shaped/shapeless InfiniteX recipes with `required_bench` and `difficulty`.
- [ ] Implement the server-authoritative timed menu state and completion service.
- [ ] Add the two conditional Mixins for player-inventory recipe resolution and result-slot clicks.

### Task 5: Workbenches and progression data

- [ ] Register flint/copper workbench blocks, menus, screens, models, loot, and translations.
- [ ] Generate all hand/workbench recipes at the approved difficulty values.
- [ ] Disable only approved vanilla recipe IDs with `neoforge:never`.
- [ ] Add the early personal advancement chain.

### Task 6: Verification

- [ ] Run `bash gradlew test`.
- [ ] Run `bash gradlew runData` and review generated resources.
- [ ] Run `bash gradlew runGameTestServer`.
- [ ] Run `bash gradlew build`.
- [ ] Review `git diff`, placeholders, Mixin scope, and compatibility boundaries.
