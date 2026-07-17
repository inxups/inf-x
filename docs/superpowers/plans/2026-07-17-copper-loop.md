# M1 Copper Loop Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:executing-plans` and follow test-driven development. Steps use checkbox syntax for tracking.

**Goal:** Build the first survival progression loop through an InfiniteX copper pickaxe.

**Architecture:** Data tags and NeoForge events enforce harvesting; custom recipes and menus implement timed crafting; two conditional Mixins connect the player inventory. Content is registered in focused modules and generated resources replace only explicit vanilla recipes.

**Tech Stack:** Java 25, NeoForge 26.2.0.21-beta, Gradle 9.6.1, Mixin, JUnit Jupiter, GameTest.

---

### Task 1: Test harness and bootstrap

- [x] Add JUnit Jupiter 5.13.4 and `useJUnitPlatform()`.
- [x] Remove MDK example registrations and metadata.
- [x] Create focused item, block, menu, recipe, loot, event, and client registries.
- [x] Run `bash gradlew test` and `bash gradlew build`.

### Task 2: Harvesting and tools

- [x] Write failing tests for tier order and tool damage calculations.
- [x] Implement `HarvestTier`, tag constants, and pure harvest policy.
- [x] Register the flint hatchet and InfiniteX copper pickaxe with R196 speed, reach, durability, and hardness-based wear.
- [x] Enforce restrictions through `BreakBlockEvent` and add GameTests.

### Task 3: Gravel resources

- [x] Write failing deterministic tests for every R196 random branch.
- [x] Register early material items and the global loot modifier.
- [x] Preserve unrelated loot, replace vanilla gravel/flint, and return gravel for the diamond branch.

### Task 4: Timed recipes

- [x] Write failing tests for the period formula and state transitions.
- [x] Implement shaped/shapeless InfiniteX recipes with `required_bench` and `difficulty`.
- [x] Implement the server-authoritative timed menu state and completion service.
- [x] Add the two conditional Mixins for player-inventory recipe resolution and result-slot clicks.

### Task 5: Workbenches and progression data

- [x] Register flint/copper workbench blocks, menus, screens, models, loot, and translations.
- [x] Generate all hand/workbench recipes at the approved difficulty values.
- [x] Disable only approved vanilla recipe IDs with `neoforge:never`.
- [x] Add the early personal advancement chain.

### Task 6: Verification

- [x] Run `bash gradlew test`.
- [x] Run `bash gradlew runData` and review generated resources.
- [x] Run `bash gradlew runGameTestServer`.
- [x] Run `bash gradlew build`.
- [x] Review `git diff`, placeholders, Mixin scope, and compatibility boundaries.
