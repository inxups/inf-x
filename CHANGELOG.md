# Changelog


## 0t11
## New Features

- Ported the MITE spawner mechanics:
  - **Lifetime (stop after 15 kills):** Block spawners in the Overworld, Nether, and End permanently stop spawning after a cumulative total of 15 mobs spawned by that spawner have been killed. `SpawnerLifetime` persists the counter through the `infx:spawner_kills` attachment. `BaseSpawnerMixin` gates spawning in `serverTick`, while `SpawnerEvents` records kills through `LivingDeathEvent`. Only player kills count; environmental deaths and deaths from wearing mobs down do not. Controlled by `mobs.spawnerLifetime` (default: `true`). Minecart spawners are unaffected because they have no position marker.
  - **Depth-layered mob selection:** Overworld dungeon spawners select mobs based on generation depth (`WorldGenDungeons.pickMobSpawner`): surface-level zombies, ghouls, skeletons, and spiders; wights become available at y ≤ 32; demon spiders at y ≤ 16; hellhounds only at y ≤ 0. `MonsterRoomFeatureMixin` rewrites the type according to y during generation. The `monster_room_mobs` datamap also includes weighted entries for all seven depth-based mobs as a fallback, so any dungeon can potentially spawn them. Controlled by `mobs.spawnerDepthLayering` (default: `true`).
  - Existing `allowSpawnerLight` already exempts spawner spawning from light-level checks while still preventing sunlight ignition. Existing `equipForWorldAge` already handles tension equipment through `FinalizeSpawnEvent` for spawner-origin mobs; no additional changes were needed.
  - Spawner mining already uses modern vanilla `requiresCorrectToolForDrops` and requires a proper pickaxe. Experience drops of 15–44 already match MITE, so no increase was made.

- Completed MITE spider web projection:
  - When a target is within 8 blocks and reachable by ray tracing, spiders predict the target's movement 10 ticks ahead and fire a web.
  - After hitting an entity or block, the web is placed permanently in a replaceable position.
  - Demon spiders and burning spiders fire flaming webs and ignite adjacent blocks according to the rules, subject to `mobGriefing`.
  - Phase Spider pursuit now advances randomly by 1–4 nodes along the unfinished path.

- Completed Bone King and Wither Skeleton support:
  - Added `infx_wither_skeleton` with wither-inflicting melee attacks, fire/lava immunity, a poor-quality iron sword, bone repair, and Bone King inspiration every 20 ticks.
  - Structure generation, spawn eggs, and dispenser spawning replace the relevant vanilla entity with this entity. The Nether's natural spawn pool remains vanilla.
  - Bone King summoning now uses a persistent per-dimension roster, with a maximum of six entries and a 9,600-tick protection period against natural despawning. An expired entry releases its slot with a 5% chance only when the mob is at full health and has no target. Legacy counters in old saves no longer block summoning.

- Completed cactus and light-seeking mechanics:
  - A cactus column tracks 0–127 kills through its bottom sand block.
  - The top segment decays on random ticks; removing the bottom cactus resets the counter.
  - A creeper damaged by a cactus with a kill count greater than one has a 50% chance to receive a 120-tick fuse window.
  - Shadows and Invisible Stalkers search every 40 and 200 ticks, respectively, within a 16×4 area for reachable light sources and dismantle them.

- Piglin MITE hostility:
  - Gold armor no longer pacifies piglins. `PiglinAiMixin` gates `isWearingSafeArmor`, `canAdmire`, and `admireGoldItem`.
  - Giving or picking up gold ingots no longer triggers bartering.
  - When a player kills a piglin, there is a 25% chance to award one payout from the vanilla barter table, filtered through the shared progression rules, including iron-equipment conversion and prohibited-item removal.
  - Controlled by `mobs.piglinHostility`.

- Phantom spawning is now moon-phase based:
  - Phantoms spawn only in waves of 1–2 during Blood Moon or Phantom Moon nights.
  - Daytime and ordinary nights always reject spawning.
  - Controlled by `mobs.phantomMoonSpawns`.

- Hoglin MITE attributes:
  - Health increased from 40 to 50.
  - Melee attack increased from 6 to 7.

- Ghasts cannot naturally spawn within 48 blocks of a player. Source verification confirmed that the distance is measured from players, not from other ghasts.

- Phase Spider spawn weight increased from 5 to 40. This approximates MITE's reliability from 64 spawn attempts; modern spawning determines attempt count before mob type selection, so a literal port is not possible.

- Hostile spawn density near strong dungeons is increased:
  - The nearby-player hostile cap is multiplied by `1 + stronghold proximity factor`.
  - `findNearestMapStructure` results are cached per player for 200 ticks, matching MITE's `WorldServer.getStrongholdProximity`:2498.

- Day/night timing now matches MITE:
  - The moon-phase night window is `[13000, 23000)`.
  - This gives 14 hours of day and 10 hours of night, matching MITE's adjusted 5000/19000 values.
  - Undead sunlight burning now has a hard MITE daytime gate in `Mob.isSunBurnTick`.

- Blood Moon spawn density is multiplied by 1.5:
  - The nearby-player hostile cap is increased 1.5× on Blood Moon nights, including both global and local mob caps.
  - MITE's apparent 8→12 chunk “spawn radius” is actually a nearby-player mob-density threshold; the search area remains 17×17 chunks.

- Depth-based spawn density:
  - The nearby-player hostile cap scales as `8×(1+(64-y)/32)`.
  - y=64 gives 1×, y=32 gives 2×, and y=0 gives 3×.
  - Deeper areas therefore allow more hostile mobs to accumulate.

- Daily spawn-rate correction:
  - Each Overworld day can randomly set the hostile natural-spawn rate to ×0.5, ×2, or ×0.
  - `SpawnRateTracker` persists the value as `SavedData`.
  - Blood Moons and thunderstorms force the modifier back to 1.0.
  - This matches MITE's `calcEffectiveHostileMobSpawningRateModifier`:612–659.

- Nightly spawning cadence:
  - Each position rolls for hostile natural spawning every tick using `y<60: 0.1` or `y≥60: 0.17`, multiplied by the rate modifier.
  - This reproduces MITE's 10%/17% per-tick rhythm.

- Thunderstorm exposed-light checking was verified as already covered by vanilla's thunderstorm branch in `Monster.isDarkEnoughToSpawn`; this was a false positive and required no implementation.

- Universal fire transfer:
  - A burning hostile mob with no main-hand weapon has a difficulty×0.3 chance to ignite its melee target for `2×difficulty` seconds.
  - This follows MITE's `EntityMob.attackEntityAsMob`:209–212.

- Zombie villager conversion now has the correct difficulty gate:
  - On Normal difficulty, zombie conversion is skipped 50% of the time.
  - Tool-wielding zombie cancellation and equipment-clearing behavior remain.

- Zombie weapon upgrades:
  - From day 10 onward, rusty short axes become an additional weapon option on the tension curve.
  - This follows MITE's `EntityZombie.addRandomWeapon`.

- Blood Moon lightning frequency is increased fivefold:
  - `ServerLevel.tickThunder` changes the random bound from 100,000 to 20,000.
  - This matches MITE's `WorldServer.java:1310`.

- Blood Moon rain now affects every biome:
  - On Blood Moon days, `Level.precipitationAt` bypasses the hot-biome “no precipitation” gate.
  - Deserts, badlands, and other normally dry biomes can receive rain.
  - Undead daytime burning immunity and crop watering therefore work in every biome.
  - Client rain particles use the same method and follow automatically.

- Blood Moon red eyes:
  - `EntityRenderer.extractRenderState` assigns hostile mobs the MITE enraged glow color `8527390` during Blood Moon nights.
  - In modern Minecraft, `outlineColor` corresponds to the glow-color field from 1.6.4.

- Crop blight expanded to vanilla crops:
  - Wheat, beetroot, and other vanilla crops have a 25% chance to wilt on each random tick during Blood Moon nights.
  - `BlightTracker` persists the state through `SavedData`, and `CropBlock.randomTick` intercepts growth.
  - Wilted crops stop growing, have a 1/64 chance per random tick to die and drop seeds, and can infect adjacent vanilla crops.
  - Bone meal cures blight instead of accelerating growth.
  - Breaking the block clears its tracking state.
  - InfX crops already had this mechanism.

- Enderman Blood Moon frenzy exemption completed:
  - `LivingEntityFrenzySpeedMixin` no longer applies the ×1.2 speed bonus to `InfxEnderman`.
  - The attack-damage exemption was already present, matching MITE's `isFrenzied=false`.

- Creeper block-explosion radius reduced by ×0.715:
  - `Creeper.explodeCreeper` multiplies the explosion-radius parameter for `InfxCreeper`.
  - Block radius changes from 3 to 2.145, and charged radius from 6 to 4.29.
  - Entity damage continues to use `ExplosionRanges` radius 4.4.
  - The 1.1× entity-radius change was already implemented.

- Burning slimes no longer split:
  - `MobSplitEvent` cancels splitting for burning slimes.
  - A burning slime disappears directly when it dies.

- Completed zombie-family MITE mechanics:
  1. Baby zombies are disabled: vanilla's 5% baby-zombie roll is cancelled, including jockeys after dismounting.
  2. Rare drops: a player kill has approximately a 2.5% chance to drop one copper, silver, gold, or iron nugget, using `(5+looting×2)/200`.
  3. Food seeking: vanilla zombies walk toward dropped raw meat tagged `#minecraft:meat` and eat it. This does not heal them and has a 400-tick cooldown.
  4. Villager conversion: zombies holding digging tools refuse to convert villagers. After conversion, the killer zombie's equipment within 5 blocks is cleared. Vanilla conversion, profession inheritance, and anti-cure behavior remain.
  5. Block digging: zombies prioritize the column below the target's feet, down to the digger's feet, before digging blocks that obstruct line of sight.

- Blood Moon frenzy completed:
  1. Bone King inspiration and Blood Moon frenzy stack. Skeletons inspired by the Bone King gain an additional +100% base melee attack on Blood Moon nights; previously the two effects were mutually exclusive at +50% each.
  2. Monsters move at ×1.2 speed during Blood Moon nights through `LivingEntity.getSpeed`.
  3. Ranged cooldowns are reduced by ×0.67: skeleton bows and witch projectiles go from 60 to 40 ticks, or approximately 26 ticks when Bone King inspiration also applies.
  4. Door breaking is twice as fast: `BreakDoorGoal.getDoorBreakTime` is halved, covering ghouls, Invisible Stalkers, and hard-difficulty vanilla zombies.

- Moonlight brightness table customized to MITE:
  - Overworld regional-difficulty moonlight brightness is set to 0.6 for Blood Moon, 1.0 for Harvest Moon, and 1.1 for Blue Moon/Moon Dog.
  - Other phases use `moon phase factor×0.5+0.75`, giving 1.25 at full moon and 0.75 at new moon.
  - This overrides the vanilla phase table in `ServerLevel.getMoonBrightness`.

- Blood Moon thunderstorm behavior fixed:
  - Blood Moon days force a thunderstorm starting at noon (tick 6000) for 13,000 ticks, matching MITE's `World.java:8675`.
  - This replaces the former 6,000-tick daytime trigger.
  - Combined with the daytime hard gate, undead do not burn during the Blood Moon daytime window.

- Monster cap changed to MITE's 50 mobs per player:
  - `MobCategory.MONSTER.getMaxInstancesPerChunk` changes from 70 to 50.

- Completed skeleton-family MITE mechanics:
  - Injured skeletons move toward dropped bones within 16 blocks and eat them, restoring 50% of maximum health.
  - Bone pickup has a 400-tick cooldown.
  - Skeletons are immune to cactus damage.
  - Wither Skeletons use InfX iron swords instead of vanilla stone swords. The sword is poor quality, while equipment drops still follow vanilla's 8.5% rule.
  - Ancient Corpse Guardians dynamically switch equipment: bow to an ancient-metal dagger when the target is closer than 5 blocks, and back to a bow when using melee weapons and the target is farther than 6 blocks. The check runs every 10 ticks.

- Zombie-system refactor:
  - The `infx_zombie` replacement entity was removed. Vanilla zombies now receive MITE behavior directly through NeoForge events.
  - `FinalizeSpawnEvent` injects the smart-zombie state: 1/8 of zombies are smart at birth, and any zombie hit by a player becomes smart. The state persists as `infx.is_smart`.
  - Vanilla attributes are aligned to melee attack 5 and armor 0.
  - Zombie digging is driven by `EntityTickEvent.Post`: 300×block hardness ticks per hit, 10 hits per block; Blood Moon frenzy halves the time; valid tools accelerate digging by `1+strVsBlock×0.5`; smart or frenzied zombies can dig bare-handed; soft blocks use a whitelist, with liquid, cactus, and feet-block gates.
  - Burning zombies seek the nearest log within 16 blocks every 40 ticks and ignite it when a player is near the tree canopy.
  - `LivingDamageEvent` makes a zombie smart when it is hit.
  - Ordinary zombie drops are restored to the vanilla loot table.

- Added the MITE Tension difficulty system:
  - Tension reads vanilla `chunk.inhabitedTime` and the moon-phase factor from `DimensionType.MOON_BRIGHTNESS_PER_PHASE`.
  - Formula: `clamp(inhabitedTime/3,600,000,0,1)×(hard?1:0.75)+moon phase×0.25`.
  - Hard difficulty caps at 1.5.
  - `mobs.tensionEnabled` can disable the system and fall back to the old day-based curve.
  - Equipment, enchantment, and boss probabilities use tension.
  - Equipment chance is `15%×tension`; enchantment chance is `10%×tension`, with level `5+tension×rand(18)`.
  - Dirt-element mining cooldowns and spider potion probabilities also use tension.

- Invisible Stalkers, Ghouls, Shadows, Wights, and Revenants were split from `InfxZombie` variants into independent entity classes:
  - `InvisibleStalker`, `Ghoul`, `Shadow`, `Wight`, and `Revenant` all extend `Zombie`.
  - They share `InfxZombieBase`.
  - Each retains its own attributes, sounds, immunities, and behavior.
  - The shared base initially disabled babies, underwater conversion, and reinforcements.

- Removed the R196 prefix from the monster system:
  - Entity NBT keys now use the `infx.` namespace, such as `infx.is_smart`, `infx.summoned_troops`, and `infx.phase_evasions`.
  - Method names were renamed to remove R196, for example `checkR196MonsterSpawnRules` became `checkMonsterSpawnRules`.

- Added symmetric client/server dev-mode validation:
  - During login configuration, the client and server exchange the `devMode` setting.
  - A mismatch disconnects the client before entering the world, with an explanatory message.
  - Integrated servers and LAN worlds with cheats remain compatible because the server-side configuration switch is still off; ordinary clients can join them.

- Dev mode configuration migration:
  - The switch moved out of the `development` sections of `config/infx/infx-common.json` and `config/infx/infx-client.json`.
  - It is now stored in `config/infx/infx-devmode.json`, using `server.devMode` and `client.devMode`, both defaulting to false.
  - Any old `development.testMode` field is ignored. Existing configurations must be re-enabled in the new file.

- “Test mode” was renamed to “dev mode” throughout:
  - Class names, configuration keys, network handshake payloads, and language keys now use `testMode` → `devMode`.
  - The configuration path changed from `infx-testmode.json` to `infx-devmode.json`.
  - Existing `testMode` fields are ignored and must be recreated as `devMode`.

## Bug Fixes

- **Swift Sneak slot corrected:** `minecraft:swift_sneak` was incorrectly registered under `FOOT_ARMOR_ENCHANTABLE`/`FEET` (boots), contrary to vanilla 26.1 and MITE, which use leggings. It now uses `LEG_ARMOR_ENCHANTABLE`/`LEGS`. GameTests now assert that boots do not support it and leggings do.

- **Vanishing and Binding Curse registration restored:** `minecraft:vanishing_curse` (`VANISHING_ENCHANTABLE`/`ANY`) and `minecraft:binding_curse` (`EQUIPPABLE_ENCHANTABLE`/`ARMOR`) were missing from `InfXEnchantments.ALL`. Because the source tags used `replace:true`, the vanilla definitions were erased and could not be obtained normally. Both are now registered and included in the source pool. As treasure enchantments, they remain excluded from the enchanting table but can appear in loot and trades.

- **Vanilla armor protection now works:** `EquipmentBehaviors.typedProtectionPoints`, `protectionBonus`, and `mundaneArmorPoints` previously required `InfXItems.catalog().equipment(stack)` to be non-null, so vanilla armor was skipped. The new `pieceBaseProtection` helper uses `armorProtection` for InfX equipment and the vanilla ARMOR attribute value for vanilla armor. Non-player paths also convert vanilla armor using an effective coefficient of 1.0 to the 0.5× scale.

- **Vanilla equipment now enters `infx:enchantable/*` target tags:** `ModItemTagsProvider` previously iterated only over InfX catalog equipment. `addVanillaEnchantmentTargets` now references vanilla item tags for pickaxes, shovels, axes, hoes, swords, and armor, covering durability, efficiency, fortune, silk touch, tree felling, harvesting, fertility, penetration, free movement, chest armor, and all sword-related tags.

- **Club/Mattock/War Hammer inheritance completed:** `isDurabilityEnchantable` now includes `MATTOCK`, `WAR_HAMMER`, and `CLUB`. `addR196EnchantmentTags` now includes Club for Stun, Knockback, and Looting, matching MITE inheritance: Club extends Cudgel, Mattock extends Shovel, and War Hammer extends Pickaxe.

- **Vampirism now covers knives and daggers:** The old check only accepted `SWORD || SCYTHE`, unlike MITE's `instanceof ItemSword`. `KNIFE || DAGGER` were added, while silver and mithril exclusions remain.

- **Thorns now supports chainmail chestplates and uses first-match behavior:** `CHAINMAIL_CHESTPLATE` was added to `INFX_THORNS_ENCHANTABLE`, matching MITE's acceptance of all `ItemCuirass`. `applyThorns` now uses the first qualifying piece in HEAD → CHEST → LEGS → FEET order instead of the highest enchantment level.

- **Feather Falling now uses first-match behavior:** The fall branch of `typedProtectionPoints` previously summed all four pieces. It now returns the first piece with Feather Falling, matching MITE. This mainly affects command/NBT-created multi-piece enchantments.

- **Enchanted books no longer lose additional enchantments:** The book branch of `EnchantmentSelector.select` previously returned only one random entry and silently discarded the rest. It now returns all entries, up to three, matching MITE's `EnchantmentHelper.addRandomEnchantment`.

- **Looting now affects equipment drops and has complete incompatibility rules:** `minecraft:looting` lacked an `EQUIPMENT_DROPS` component, so non-InfX custom drops received no Looting bonus. It now adds `AddValue(perLevel(0.01F))` under the attacker-is-player condition, matching vanilla 26.1. Looting and Silk Touch are now mutually exclusive.

- **Vanilla bows now provide Precision/Recovery/Poisoning data:** These enchantments were previously written only by `InfxBowItem.shootProjectile`. `BowItemProjectileMixin` now redirects `BowItem.shootProjectile`'s `shootFromRotation` call, covering all bows. `InfxBowItem` now only adds its material speed multiplier.

- **Off-hand bows and crossbows no longer read the main hand:** `InfxBowItem.shootProjectile` and `CrossbowItemProjectileMixin` now use `getUseItem()`, which correctly identifies the active item during `releaseUsing`. If empty, they fall back to the main-hand item.

- **Baiting now follows the MITE bite-probability model:** `infx:baiting` previously redirected `timeUntilLured` and shortened the countdown by ×0.9 per level. MITE modifies the `chance_in` denominator and applies modifiers in a specific order. The ×9/10-per-level effect now lives in `FishingRules.lureDelay`'s `chance_in` path, in the order day/night → Blue Moon → rain → Baiting → bait. `baitingLureDelay` was renamed to `baitingBiteChance`. Vanilla's countdown structure differs from MITE's per-check model, but the expected bite-rate increase is equivalent.

- **Butchering formulas aligned with MITE:**
  - Horse, donkey, and mule previously shared `1+rand.nextInt(1+butchering)+rand.nextInt(2)`, which over-rewarded donkeys and mules.
  - Only ordinary horses now receive the extra `rand.nextInt(2)`, matching MITE's `EntityHorse.dropFewItems`.
  - Spider-eye drops now include an independent `rand.nextInt(3)==0` branch and no longer require that no spider eye has already been generated.
  - Cow, pig, and sheep Butchering bonus meat now consistently uses `Livestock.isWell`. Unhealthy livestock produce no extra meat, independent of listener ordering.

- **Exploded diamond and emerald ore now drop shards:** Diamond ore and emerald ore, including deepslate variants, previously used vanilla loot tables after explosions and dropped full gems. `PhysicsEvents.convertExplodedBlock` now emits one `infx:diamond_shard` or `infx:emerald_shard` per explosion, with quantity 1 and chance 1.0, matching MITE. Stone material does not trigger explosion loss or zeroing. Lapis, Nether quartz, and coal ore remain future work.

- **Silver armor tooltip corrected:** `EquipmentBehaviors.addQualityTooltip` incorrectly displayed “+25% damage to undead” for every silver item, including armor, chainmail, and horse armor. That bonus is provided only by a silver main-hand weapon or silver arrow through `MobDamageRules.hasSilverAspect`. Non-armor tools, weapons, and arrows now show the undead-damage line; armor shows the negative-effect resistance line.

- **Surface lava lakes removed:** The `remove_surface_lava_lakes` biome modifier removes `minecraft:lake_lava_surface` from every Overworld biome at the LAKES step. `NoiseBasedAquiferMixin` only suppresses exposed aquifer lava and cannot affect the placed `lake_lava_surface` feature. Together, both changes remove surface lava lakes while preserving underground lava lakes and aquifer lava.

- **MITE river seagrass fixed:** `desert_river`, `jungle_river`, and `swamp_river` reused vanilla river generation settings, including `seagrass_river`, but `SeagrassFeature` used the `OCEAN_FLOOR` heightmap and often missed the water column in shallow or narrow rivers. `InfXSeagrassFeature` now scans downward from the surface until it finds the riverbed, stopping at the first non-water block to avoid decorating cave water. `remove_river_seagrass` and `add_river_seagrass` replace the feature in the three MITE river biomes with `infx:river_seagrass`, count 48, using the `WORLD_SURFACE` heightmap. Vanilla rivers and frozen rivers retain their original feature.

- **Free Movement now covers cobweb slowdown:** `applyFreeMovementResistance` previously replaced only the `MOVEMENT_SPEED` attribute effects from Slowness and Paralysis. Modern `WebBlock` uses `Entity.makeStuckInBlock` and `stuckSpeedMultiplier` instead. `EntityStuckInBlockMixin`, together with `EntityAccessor`, now intercepts this method and softens each movement component toward 1.0 according to free-movement resistance: `base+(1-base)×resistance`, matching MITE's `getSpeedBoostVsSlowDown`.

- **InfX zombie-family water conversion restored:** The `InfxZombieBase.convertsInWater()=false` suppression was removed. Ghoul, Wight, Revenant, Shadow, and Invisible Stalker now convert into vanilla drowned after spending 600 ticks in water. This restores vanilla behavior, at the cost of losing their InfX traits after conversion.

- **Disarming exemptions added:** The `disarming` enchantment no longer affects witches, villagers, wandering traders, or drowned holding a trident. The exemptions are enforced in `EnchantmentEvents.disarm`.

- **Zombie boss double-roll fixed:** The event-level boss roll was removed. Vanilla `Zombie.handleAttributes` already performs the native `difficultyModifier×0.05` boss roll, giving 2–5× health and +0.5–0.75 knockback resistance. The old double roll could produce up to 7× health (140 HP) and resistance of at least 1. Boss selection is now handled only by vanilla.

- **Dev-mode handshake no longer conflicts with cheat-enabled integrated worlds:** Integrated servers, including singleplayer and LAN hosts, skip the raw configuration comparison because both sides read the same file. A client with dev mode enabled and a server switch disabled is intentional for cheat-enabled worlds. Dedicated servers still perform the normal comparison.

- **Blood Moon thunderstorm duration fixed:** Forced storms now end at 19:00 (noon plus 13,000 ticks). They are no longer reset every 200 ticks to another 13,000 ticks, which previously extended the storm into the following noon.

- **Normal zombie conversion no longer receives the 50% penalty twice:** The duplicate event implementation was removed. Vanilla `Zombie.killedEntity` already skips conversion 50% of the time on Normal, restoring a net 50% conversion rate instead of 25%.

- **Duplicate equipment rolls removed:** The extra `equipForWorldAge` call after `FinalizeSpawnEvent` was removed. Equipment probability is no longer squared from `0.15T` to `1-(1-0.15T)^2`.

- **Configuration gates completed:**
  - `bloodMoonFrenzy` now controls Blood Moon frenzy speed, door breaking, red eyes, and ranged cooldowns through `isBloodMoonFrenzied`.
  - Lightning ×5 and all-biome rain are gated by `world.moonEvents`.
  - Crop blight has a new `world.bloodMoonBlight` switch.
  - The 50-mob cap is gated by `mobs.enabled`.

- **Ghoul slowdown weakened:** The hit slowdown amplifier was reduced from 5 (90%) to 2 (45%), preventing near-total immobilization.

- **`zombifiesVillagers` hook connected:** The base class now overrides `convertVillagerToZombieVillager` and gates conversion according to its return value. Invisible Stalkers therefore never convert villagers, while other new mobs retain default behavior.

- **Wither Skeleton duplicate equipment source removed:** The no-quality iron-sword branch in `VanillaMobEquipmentMixin` was removed because `WitherSkeletonDropsMixin` immediately replaced it with a poor-quality iron sword.

- **Enderman pearl and eye pickup changed:** Endermen now teleport to safe landing spots with two blocks of headroom instead of actively pathfinding to pick up ender pearls or eyes. Random teleportation and damage-triggered teleportation remain.

- **Phase Spider evasion corrected:** Only non-fall, non-fire, and non-poison damage consumes evasions. Failed teleports also consume an evasion. When there is no attacker, the spider's own position is used as the threat point.

- Blood Moon frenzy mining, mining cooldowns, and Dirt Element door-breaking/mining now all use `isBloodMoonFrenzied` configuration gating. Bone King inspiration is unaffected by this setting.

- Zombie block digging now uses the exact MITE formula instead of a player-speed approximation. Dirt takes approximately 75 seconds per block, Blood Moon frenzy halves the time, and valid tools provide significant acceleration.

- Test-mode LAN/singleplayer cheat permissions fixed:
  - LAN “Allow Cheats” was controlled by the client-side `development.testMode`, while server permission tightening used the independent server-side value.
  - Integrated worlds with cheats enabled, including LAN worlds published with cheats and singleplayer worlds created with commands enabled, are now treated as valid test-mode worlds and grant operator permissions.
  - Normal mode still forces the option lock closed every tick.

## Compatibility

- **Modern enchantment gaps filled:** `luck_of_the_sea`, `lure`, `depth_strider`, `multishot`, `quick_charge`, `piercing`, `frost_walker`, and `soul_speed` were missing from `InfXEnchantments.ALL` and were erased by the `replace:true` source tags. They are now included without re-bootstrapping, reusing vanilla definitions and effects.
  - Non-treasure entries can appear at the enchanting table.
  - `frost_walker`, `soul_speed`, and both curses retain vanilla treasure behavior and are available only through loot, trades, or mob equipment.

- **Jade sick-state display:** Pointing Jade at a sick InfX cow, chicken, sheep, or pig now shows “Sick” in the entity tooltip. It uses the livestock wellness state already synchronized to clients.

- **Spawn tables no longer wipe third-party entries wholesale:**
  - In the Overworld, `SpawnsBiomeModifier` selectively removes only the vanilla entries that InfX re-adds: six monster types, livestock/mounts, fish, bats, and squids.
  - Entries added by other mods during the ADD stage are preserved.
  - Nether and End behavior retains exact replacement semantics.
  - `mobs.wipeOtherSpawnTables` (default: `false`) restores the old wholesale-wipe behavior.
  - Pure vanilla results remain unchanged, and all GameTests pass.

- **Vanilla entity replacement exemptions:**
  - Third-party mods can add an entity type to the datapack tag `infx:keep_vanilla_entity` (`data/infx/tags/entity_type/keep_vanilla_entity.json`, with `"replace": false`).
  - InfX will then leave that entity as vanilla, allowing other mods' changes to vanilla attributes, AI, and drops to work again.
  - `mobs.replaceVanillaMobs` (default: `true`) can disable all 26 vanilla replacements at once.

- **Source-pool split:**
  - `InfXEnchantments` now defines `TABLE` as `ALL` minus the four treasure-only entries, plus a `TREASURE_ONLY` collection.
  - `IN_ENCHANTING_TABLE` writes `TABLE`; all other source tags write `ALL`.
  - `EnchantmentSelector.candidateKeys` now uses `TABLE`.
  - The enchanting table can no longer roll curses, Soul Speed, or Frost Walker.

## Balance

- **Fire Aspect aligned with MITE:** `Ignite(LevelBasedValue.constant(1.0F))` was replaced with `perLevel(4.0F)`, giving 4 seconds at level I and 8 seconds at level II, matching MITE's `setFire(fire_aspect*4)` and vanilla 26.1.

- **Sharpness and Slaughter now add +1 damage per level:** Vanilla Sharpness changed from `perLevel(1.0F, 0.5F)` (level V = +3) to `perLevel(1.0F, 1.0F)` (level V = +5). `SLAUGHTER_DAMAGE_PER_EXTRA_LEVEL` changed from 0.75 to 1.0, also giving +5 at level V and matching MITE.

## Balance Adjustments

- Unified spawning control:
  - Added `world/SpawnGate` as the single authority for whether spawning occurs, what spawns, and how many can spawn.
  - Events and six spawning mixins for capacity, cadence, depth, patrols, and daytime burning now act as thin delegates.
  - Entity replacement, placement predicates, phantom waves, spawner light/population limits, despawn prevention, witch-cancellation exemptions, Blood Moon frenzy, and piglin-hostility predicates are also routed through `SpawnGate`.

- Moon-phase spawn-gating configuration migrated:
  - Added `mobs.moonSpawnGating` (default: `true`) for moon-phase spawn gates.
  - `world.moonEvents` now controls weather only.
  - Disabling `world.enabled` no longer disables spawn gates; the main switch is now `mobs.enabled`.

- Ordinary zombie weapon probability now follows tension instead of the day curve:
  - It uses `15%×tension`, capped at approximately 22.5%.
  - Long-inhabited chunks still progressively increase monster pressure through tension.

---

# 0v5
### New Features
- Added dirt blocks to the "Sliding" and "Collapse" dirt tags.
- Gravity-affected dirt now inherits all block behaviors set to slide/fall.
- Removed fragile unit tests that hardcoded adjustable attack speeds and quality thresholds.
- Added delayed collapse and slope sliding mechanics for dirt with specific tags, including falling-block landing reservation handling and configurable collapse delay time.
- Migrated default food configs, furnace fuel heat values, gelatinous dissolving properties, and mob spawn rules to reloadable `data/infx` datapack resources; fuel and corrosive-ground classifications now use data tags.
- Removed fragile test assertions that hardcoded registry, directory, and resource-list counts.
- Integrated the Jupiter config system: server common config is saved to `config/infx/infx-common.json`, editable in-game by OPs on a config screen and synced to clients; client display preferences are saved to `config/infx/infx-client.json`. Survival, progression, production, world, and entity rules can now be controlled separately, along with food hints and special moon phase rendering.
- Development mode is now enabled via Jupiter config: server `devMode` controls vanilla server management, permission, and world rule allowances; client `devMode` controls F3, world creation, brightness, LAN commands, and dev hints. It no longer reads the JVM's `infx.testMode` parameter, nor requires client and server switches to match.
- The "test mode" terminology is uniformly renamed to "dev mode" (development mode): class names, config keys, network handshake payloads, and language keys all change from `testMode` to `devMode`, and the config file path changes from `infx-testmode.json` to `infx-devmode.json`. Existing `testMode` config fields are ignored; re-enable the switch under `devMode` in the new file.
- Added server datapack rule reloads: `food_profiles`, `fuel_heat`, `harvest_rules`, `gelatinous_dissolving`, and `mob_spawn_rules`. Rules support exact item/block IDs and tag targets, overriding stably by priority, target specificity, and rule ID, while preserving existing INFX strategy when rules are absent. Entity spawn rules take effect after the original placement type and base spawn checks pass, with configurable overworld height, probability, ceiling, and floor block conditions.
- Added raw silver, raw mithril, and raw adamantite ore items with corresponding textures. Silver ore / deepslate silver ore / mithril ore / deepslate mithril ore / adamantite ore / deepslate adamantite ore no longer drop the block itself but drop 1 corresponding raw ore (affected by Fortune; Silk Touch still drops the ore block). Smelting recipes for the three ores changed to raw ore → ingot (XP and duration unchanged: silver 15 / mithril 40 / adamantite 100, duration 200); the ore blocks themselves are no longer smeltable.
- Shears right-click silk harvest now overrides vanilla "shears-only" drop blocks: vines, glow lichen (per attached face), seagrass, short grass, fern, nether sprouts, hanging roots, dripleaf, tall grass / large fern / tall seagrass, and other blocks that previously couldn't drop their own block form can now be harvested by right-click (tall grass / large fern / tall seagrass drop 2 of their short variants). Cobwebs are harvested by right-click as the cobweb block, while left-click still drops string (other tools drop nothing on break). To this end, drop calls now use a vanilla shears stack enchanted with Silk Touch, hitting the vanilla `match_tool items:minecraft:shears` loot condition branch.
- Pale Garden vegetation included in the shears valid tag: pale hanging moss, pale moss block, pale moss carpet, and fallen leaves can be harvested by right-click with shears and quickly broken by left-click; carpet types (`#wool_carpets`) added to left-click quick-break range.
- Mushroom growth mechanics aligned with MITE: brown/red mushrooms now have 0–3 four-stage growth progress (`growth`); random ticks changed to 1% spread (max 5 of the same type within 9×3×9, 4-step random walk, new individuals start at stage 0, brown mushrooms on mycelium only spread at 1/4 probability); brown mushrooms have a 2% chance to naturally become giant; brown mushrooms have a 7% chance to convert wet, fertilized farmland below into mycelium (removing the instant "planting converts to mycelium" conversion). Dung ripening changed to a 50% chance to advance one stage (consumed even on failure, only applies to legal targets): clicking a brown mushroom (mycelium below + indoor) or red mushroom (grass block below + open sky) consumes the dung and ripens it; clicking a mushroom on regular farmland only fertilizes the farmland below (redirect trap, does not ripen the mushroom); clicking mycelium/grass block itself forwards to the mushroom above. Bone meal no longer works on mushrooms (only dung can ripen them). Mycelium self-maintenance changed to MITE behavior: becomes dirt when covered by water, 75% chance to skip the random tick, in dark (≤13) + indoor has a 1/256 chance to grow a brown mushroom above (≤2 mushrooms within 9×5×9), otherwise 8 attempts to spread to dirt/grass/farmland; in bright open-air daylight it reverts to dirt. Behavior changes: surface mycelium on overworld mushroom islands degrades to dirt in daylight (MITE-expected behavior); dung on illegal targets (brown mushroom not on mycelium / red mushroom not on grass) is no longer consumed; mushrooms planted on farmland no longer instantly convert to mycelium.
### Balance Adjustments
- Reduced blueberry generation.
- Pumpkin decomposition now yields only one seed.
- Shears durability cost for shearing wool changed from 1 to MITE's 50 per use (128 uses for iron shears); shearable entities like mooshrooms are likewise affected.
- Increased interaction range by 0.25.
- A substantial increase in the quantity of minerals formed.
### Bug Fixes
- Fixed fertilizer being repeatedly eaten/consumed.
- Fixed leather armor being unable to be repaired with rubber bands in the crafting grid: added the `infx:repair` recipe type and the `infx:leather_repair` recipe — place 1 damaged leather armor piece + 1 rubber band in a 2×2 crafting grid to repair (each rubber band repairs 2 durability), preserving original quality/color/enchantments. Repair doesn't consume XP or reroll quality, and can be crafted in the inventory (hands-level).
- Fixed water flow destroying grass plants and dropping wheat seeds: short grass / tall grass / fern / large fern destroyed by water flow (or bucket dumping, source-less explosions) no longer drop wheat seeds; player harvesting and livestock trampling are unaffected.
- Fixed JEI not showing the transfer items (+) button on mod workbench screens: previously each recipe tier was registered to JEI's same transfer key by multiple workbenches, overwriting each other so only the highest-tier workbench worked and all others lacked the + button. Now each recipe tier registers only one transfer handler independent of the workbench menu type, gated by workbench tier — workbenches below the recipe's requirement hide the + button (e.g., the copper workbench doesn't show the transfer button for mithril recipes), while compliant workbenches (including downward-compatible ones) transfer normally.
- Fixed smelting XP: charcoal smelting fixed at 0 XP (deterministically 0, replacing vanilla's 0.15 random fractional rounding); mineral smelting XP changed to fixed per-item values — gold (incl. deepslate gold ore/nether gold ore/raw gold) 20, iron (incl. deepslate iron ore/raw iron) 10, lapis lazuli 20, diamond 30, redstone 20, emerald 20, copper (incl. deepslate copper ore/raw copper) 10, silver 15, mithril 40, adamantite 100, nether quartz 10. Covers all vanilla mineral smelting recipes and the vanilla `minecraft:charcoal` recipe, with output and duration unchanged.
- Fixed shears left-click being unable to break hand-mineable blocks like torches: for non-shears-tag blocks that don't require the correct tool (torch/redstone torch/lantern/button/lever/rail/dirt/sand/planks, etc.), shears now allow breaking per hand behavior — hardness-0 blocks break instantly without durability cost; blocks requiring tools (stone/ore/log, etc.) still cannot be broken with shears, consistent with MITE's and swords' existing semantics.

---

## 0t9
### New Features
- Metal Anvils now support enchantment merging: Equipment or tools can consume enchanted books to acquire enchantments. Merging rules mirror the vanilla anvil: identical enchantments of the same level on the same side upgrade by one level (capped at the enchantment's maximum); if levels differ, the higher level is kept; enchantments incompatible with the target or conflicting with existing ones are skipped; the result remains the original piece of equipment. Merging does not consume XP or repair materials; instead, the anvil sustains wear based on enchantment levels (enchanted book costs are halved, meaning 4 points of wear per level for InfX enchantments), and the enchanted book is fully consumed. This can be combined with free renaming in a single operation. Enchanted books cannot be merged with other enchanted books.
### Balance Adjustments
- Surface-exposed lava lakes no longer generate in the Overworld: Previously, vanilla-style lava lakes were generated within aquifers via the `lava` density function (with surfaces at y ≤ -10). A fluid type check (`Aquifer$NoiseBasedAquifer.computeFluidType`) has now been injected; if a lake's surface aligns with the terrain surface and is exposed to the sky, it is converted to water. Lava lakes, springs, and deep lava seas fully enclosed within underground caves remain unaffected, and Nether generation remains unchanged.
- Adjusted movement speeds for InfX-replaced mobs to match Minecraft 26.1.2 standards: Standard InfX Skeletons revert to the vanilla skeleton speed of `0.25`; standard Spiders and Cave Spiders revert to the vanilla spider speed of `0.30`; Enderman chase speed is adjusted to the vanilla value of `0.30 + 0.15 = 0.45` (replacing the anomalous `6.50`). The standalone Demon Spider retains its custom increased speed of `0.375`. - Zombie pursuit mining speed adjusted to match the player: Mining time is now calculated using the vanilla player mining formula (coefficients of 100 for bare hands and 30 for correct tools; progress advances every 20 ticks based on tool speed and block hardness), replacing the previous fixed formula that was roughly 2.5 times faster than the player's; mining dirt/gravel by hand takes about 3 seconds, while mining stone with a pickaxe takes about 1 second.
- General pursuit mining is now exclusive to InfX zombie variants: Only zombies (including variants such as Invisible Stalkers, Ghouls, Shadows, Wraiths, and Wights) will mine through obstacles blocking their line of sight; other hostile mobs like skeletons, spiders, and creepers no longer engage in general mining behavior. Earth Elementals retain their unique mining AI.
- Warhammer attack speed changed to 1.0.
- Vanilla recipes overridden using matching IDs: `minecraft:bone_meal` now crafts 1 bone meal from 1 bone, and `minecraft:melon_seeds` now crafts 1 melon seed from 4 melon slices; `minecraft:bread` is disabled via `neoforge:never`, meaning the vanilla recipe of crafting bread from 3 wheat is no longer available.
- Protection enchantments I/II/III/IV now increase the armor piece's base protection by 12.5%, 25%, 37.5%, and 50% respectively; the bonus no longer diminishes as armor durability decreases. - Water bowls are now required for recipes involving bowl-based foods.
- Adjusted the impact of tool tiers on tool quality.
- Adjusted Respawn Anchor hardness to match that of Obsidian.
- Added fertilizer as a fuel source and redefined the burn time for dead leaves.
- Fixed furnace cooking XP to specific values ​​per item: Porkchop (3), Beef (4), Mutton (2), Chicken (3), Salmon (4), Cod (3), and Rabbit (3); this replaces the vanilla uniform random value of 0.35. This change overrides 7 vanilla smelting recipes (output and duration remain unchanged), while unlisted meats yield no XP when cooked (consistent with potatoes; campfire/smoker recipes are unaffected).
- Changed the falling behavior of dirt (and sand, gravel, farmland, etc.) to match vanilla gravity mechanics: falling dirt no longer damages entities at the landing site like anvils do; instead, it behaves like vanilla sand and gravel—falling as a block and either placing itself or dropping as an item upon landing, without damaging entities.
- Changed the skeletons riding Skeleton Trap Horses (thunderstorm traps) to INFX Skeletons: the trap spawns skeletons directly using its specific `TRIGGERED` spawn method (bypassing global spawn replacement, which only affects natural spawns). Riders now spawn according to the standard INFX Skeleton profile—75% carry wooden bows, 25% carry melee weapons that scale with world age, and they have 6 points of max health—while the trap's inherent iron helmet and enchanted gear logic remain unchanged; the trap horses themselves remain vanilla Skeleton Horses. ### Bug Fixes
- Fixed vanilla behaviors lost after replacing the food bar rendering: restored the vanilla "jitter" animation when the hunger level is depleted (at 0)—where each icon segment randomly jitters ±1 pixel vertically on ticks satisfying `guiTicks % (food*3+1) == 0`, with jitter frequency increasing as hunger drops; the food bar is now hidden when riding mounts with health bars (horses, donkeys, mules, camels, etc.) to prevent overlapping with the mount's health bar and misaligning HUD elements below.
- Fixed an issue where empty bowls were not returned after crafting with milk/water bowls: recipes for cheese (4 milk bowls), dough (water bowl), and cake (milk bowl variant) previously did not return bowls; consumed milk/water bowls are now returned as empty bowls (added new recipe types `infx:shapeless_returning` and `infx:shaped_returning`); recipes where the output is already a bowl-based food (e.g., soups/porridges) do not return empty bowls, preventing bowl duplication exploits.
- Fixed an issue where JEI could not transfer items or fill recipes for manual crafting (the 2×2 player inventory crafting grid): JEI's built-in handler for player crafting grids only covered the vanilla `minecraft:crafting` recipe type, leaving INFX's `infx:hand_crafting` category without a corresponding handler (causing silent failures and "No Recipe Transfer handler for container class InventoryMenu" log errors); a matching recipe transfer handler has now been registered for the inventory's 2×2 crafting grid, with a notification ("Recipe too large to craft in 2×2 grid") displayed for recipes exceeding the 2×2 layout.
- Aligned InfX mob tracking distances with Minecraft 26.1.2 standards: restored baseline `FOLLOW_RANGE` values ​​for entities; ensured that player activity, "lit" players, and target propagation no longer bypass the entity's own tracking range; and fixed spherical distance detection for bats, squids, and special monsters. - Fixed an issue where, if hunger ran out while mining, the client-side progress would complete but the block wouldn't actually break until the player ate; the server now accumulates actual mining progress tick-by-tick instead of recalculating the total mining time based on the updated current speed.
- Fixed an issue in single-player worlds where client-side recipe rule synchronization or the exit process would overwrite the server's `recipe_rules` state, causing explicit tier settings to silently revert to name-based inference; server and client rule sets are now fully isolated. The Flint Workbench retains the `hand` rule and remains craftable in the player's 2x2 inventory grid; a warning containing the recipe ID and fallback tier is logged if a built-in INFX recipe lacks explicit rules.
- Fixed the missing sound effect when Phase Spiders dodge.
- Restored the vanilla crafting recipe for the Spyglass; standardized the stack limit to 8 for various wooden display racks, stained glass, and concrete powders, ensuring vanilla recipes yielding 6 or 8 items display and craft correctly.
- Adjusted Simplified Chinese names for enchantments: "Piercing" (穿透) changed to "Armor-Piercing" (穿甲), and "Swiftness" (迅捷) changed to "Rapid Draw" (快速拉弦).
- Fertilizer and Fertile Soil can no longer be applied repeatedly.
- Eggs of any variety can now be thrown.
- Wanderers, Scorched Skeletons, and Swamp Skeletons now use the same spawn profile as standard INFX skeletons: 75% carry a wooden bow, and 25% carry a melee weapon that upgrades based on the world's day count (Short Wooden Club for days 1–9, Wooden Club for days 10–19, Rusted Iron Dagger for days 20–31, and Rusted Iron Sword from day 32 onwards); maximum health is standardized to 6 points. Wither Skeletons retain the INFX Iron Sword and original drop rules. - Zombified Piglins and Wither Skeletons no longer guarantee a weapon drop: The "guaranteed drop at full durability" logic has been reverted (both the `dropCustomDeathLoot` override and the supplementary drop event handling have been removed). The system now follows standard vanilla equipment drop rules—an 8.5% base drop rate, active only upon player kills, with random durability loss applied to dropped weapons. Weapons of "Poor" quality (gold) or "InfX" (iron sword) carried upon spawning remain unchanged.
- Fixed an issue where loose terrain blocks would only drop the bottom-most block: Previously, when `tryFall` created a falling entity, it cleared the block and triggered neighbor updates within the same call; the re-entrancy protection flag `updatingGravity` suppressed the "support lost" notification for the dirt block above, causing only the bottom block to fall while the rest remained floating. This flag has been removed in favor of a block-by-block consumption approach that naturally cascades upwards (each block triggers a drop only once, ensuring the chain terminates). A new regression test, `infx_loose_terrain_cascade`, has been added: destroying the supporting stone causes a stack of three dirt blocks to all convert into falling entities and land in a stack.
### Other
- Removed blocking configuration.
- The food bar is now rendered based on current capacity (matching the health bar style): it displays `ceil(current_max / 2)` slots, scaling from 3 to 10 slots based on level. It retains vanilla textures and right-side layout (switching to a specific icon during the Hunger effect) while accurately representing capacity levels that the fixed 10-slot vanilla bar could not show.
- Eating eligibility is now aligned with the food bar: the ability to eat depends solely on the nutrition layer (the food bar itself)—eating is allowed if the bar isn't full and disallowed if it is full (i.e., nutrition has reached the current level's maximum), regardless of remaining saturation. Exceptions allowing eating to cure nutritional deficiencies remain. This resolves the previous inconsistency where one could eat while the food bar appeared full but saturation was zero; the saturation layer is now relegated to a purely internal buffer (though it is still prioritized during eating and consumption). - Sprinting status is now directly indicated by the hunger bar: sprinting requires nutritional reserves, so when the player is starving (nutrition ≤ 0.0001), the hunger bar is forced to display as empty; this eliminates the discrepancy where a player could not sprint despite having one segment of the bar remaining.
- Verified that picking up smelted items from a furnace using the Left Mouse Button (LMB) grants smelting experience, consistent with the Shift-click method: experience is released as an orb when the item is removed. The LMB pickup triggers via `FurnaceResultSlot.onTake`, while the Shift-click path triggers via `onQuickCraft`. A regression test, `furnace_click_experience`, has been added to verify player experience gain across all scenarios (covering vanilla vs. InfX furnaces and both LMB and Shift-click methods).

---

## 0v3
### New Features
- Overworld river variants now generate according to their biome:
    - Desert rivers generate in desert and badlands regions.
    - Jungle rivers generate in jungle regions.
    - Swamp rivers replace riverbeds within swamp biomes, while the rest of the swamp terrain remains unchanged.
    - Temperate, cold, and dry regions retain vanilla rivers instead of using rough temperature/humidity thresholds.
- Gems and quartz can now be right-clicked to exchange them for experience, using the same mechanic as coins. Each exchange consumes one item:
    - Diamond: 500 XP
    - Emerald: 250 XP
    - Lapis Lazuli: 50 XP
    - Nether Quartz: 25 XP  
      The exchange plays the experience-orb pickup sound and releases upward-floating enchantment particles based on the XP amount.
- Metal anvils now support naming. The interface includes a vanilla-style name field. Naming is free and does not reduce anvil durability. Items can be named, have custom names cleared, or renamed while being repaired.
- Tool table tops now use separate materials for each wood type. The flint and obsidian tool tables each have dedicated tops for 11 stripped logs/stems: oak, spruce, birch, jungle, acacia, cherry, pale oak, dark oak, mangrove, crimson, and warped.
- Crossbows now support the Precision and Recovery enchantments, using the same mechanics as bows. Precision reduces bolt spread, while Recovery increases the chance of retrieving fired projectiles. These enchantments still work with INFX bows.
- Replaced the textures for the `sgravel` block and revenants with new owner-provided materials. The baby revenant texture remains unchanged.
- Restored crafting recipes replaced by vanilla recipes:
    - Cake: flour + sugar + egg + milk; either a bucket or milk bowl may be used, and the egg slot accepts all three vanilla eggs.
    - Cookie: 2 wheat + cocoa beans → 8 cookies.
    - Golden apple: restored to the gold-nugget recipe.
    - Compass and clock: restored to iron-nugget/gold-nugget recipes surrounding redstone.
    - Brick block: 1 sand + 8 bricks → 2 brick blocks.
    - Nether brick block: 1 soul sand + 8 nether bricks → 2 nether brick blocks.  
      The corresponding vanilla recipes have been disabled.
- Restored the recipe for 1 wheat → 1 wheat seed.
- Added a 2×2 recipe: 4 cobblestone → 2 stone. The vanilla stone-smelting recipe remains available.
### Bug Fixes
- Fixed metal anvil collision and selection boxes when facing east or west. Previously, both orientations used the same north-south shape, causing the top plate, steps, and pillars to be misaligned with the visible model. The east-west shape is now derived by rotating the north-south shape 90 degrees around its center, ensuring all four orientations match the model.
- Zombified piglins and wither skeletons now drop the poor-quality weapons they carry. Gold swords, axes, and pickaxes carried by zombified piglins, as well as InfX iron swords carried by wither skeletons, are marked as `Poor`. They spawn carrying these weapons and are guaranteed to drop them on death. Vanilla 26.1.2 removed the equipment-drop mechanism, so these weapons previously neither dropped nor had Poor quality.
- Sweet berry bushes can now only be planted in taiga and taiga-variant biomes (`#minecraft:is_taiga`). Their fruiting rate now matches blueberries: a 2.5% chance per random tick, requiring full sky brightness, replacing the vanilla 20% chance at light level 9.
- Increased the maximum kelp stack size from 8 to 16, matching sugar cane, bamboo, and other standing plants. Dried kelp already stacked to 16.
- Furnace-smelted potatoes no longer grant experience. The vanilla `baked_potato` smelting recipe is overridden with the same output and cooking time but zero XP. Campfire and smoker recipes are unaffected.
- Brown mushrooms can now only be planted on moist, fertilized farmland at light levels below 8. They cannot be planted on other soil types, such as dirt or grass. When planted on moist, fertilized farmland, they convert it into mycelium.
- Manure fertilization now only grows brown mushrooms planted on mycelium. Fertilizing brown mushrooms on other blocks still consumes the manure but does not grow them. Growth chances are now 1/3 for brown mushrooms and 1/5 for red mushrooms, replacing the previous 100% chance whenever sufficient space was available.

---

## 0t7
### Balance Changes
- The stack limit for mod metal ingots (silver, mithril, adamant, and ancient metal ingots) has been reduced from 16 to 8, matching the vanilla stack limit for iron, copper, gold, and netherite ingots. Other mod raw materials (metal nuggets, fragments, chains, coins, etc.) remain stackable to 16.
- Attack speed adjustments: short wooden sticks and all daggers are now 2.5; all short axes are 1.2; wooden clubs are 1.6; flint knives and obsidian knives are 3.0.
### New Features
- Added Jade compatibility
- Mod leather armor (leather helmets, chestplates, leggings, and boots) can now be dyed in a 2×2 crafting grid with any dye using `crafting_dye` recipes. The recipes have a difficulty of 50 and require no tools. Color mixing follows vanilla leather armor rules. Dyed mod leather armor can be washed in a water-filled cauldron by adding it to the vanilla `cauldron_can_remove_dye` item tag. Vanilla leather armor dyeing recipes remain disabled.
- The “Allow Cheats” switch when sharing a world to LAN can now only be enabled in test mode (`-Dinfx.testMode=true`). In normal mode, it is forcibly disabled and cannot be toggled; its tooltip explains why. LAN worlds can no longer be opened with cheats enabled.
### Bug Fixes
- Vanilla crossbows now have 64 durability instead of 465, can be repaired with iron nuggets in an anvil, and have an enchantability of 30. With a fully upgraded diamond enchanting table, the maximum enchantment cost is 5,300 XP.
- Newly added carrot and warped-fungus fishing rods now have the same durability as regular fishing rods made from the same material—for example, iron: 16, mithril: 128, and adamant: 512—instead of being fixed at 25 or 100.
- Pillager patrols now use the same conditions as village generation: day 60 and iron-tier tools having been crafted somewhere in the world, replacing vanilla’s five-day threshold.
- Bamboo and resin clumps now stack to 16 instead of 4.
- The Diamond Enchanting Table recipe now uses mithril materials: Mithril Workbench + Mithril Ingot + Obsidian + Book. It no longer requires a Mithril Workbench while using only diamonds.
- Feeding baby animals no longer consumes food or accelerates their growth. Babies can now grow only over time. Golden dandelions’ age-locking function is unaffected.
- Added the server configuration option `toolsBlockAttacks`, enabled by default. When disabled, tools no longer have a blocking component, and right-clicking with tools will not block attacks like a shield. Changes require a restart.
- Copper ore and deepslate copper ore now initially drop only 1 raw copper; Fortune can still increase the yield, replacing vanilla’s 2–5.
- Blueberry bushes and sweet berry bushes can no longer be grown with bone meal.
- The base survival-mode interaction distance for blocks and entities has been unified at 2.5 blocks. The block distance was previously 2.25 blocks. Tool and weapon reach bonuses now stack correctly through attributes instead of being bypassed by hardcoded interaction distances.
- Swords can now mine blocks that are normally mineable by hand, such as dirt and sand, but still cannot mine blocks requiring the correct tool, such as stone and ores.
- The slimeball in the lead recipe has been replaced with an INFX slimeball from the gel-ball tag; any color is accepted.
- Crafting various metal and stone buckets back into empty buckets no longer returns an extra bucket. Stone buckets no longer provide a container remainder; previously, one stone bucket produced two empty buckets.
- Equipment dropped into fire or lava now loses durability: 1 durability per fire damage and 4 per lava damage. It burns up only when its durability reaches zero. Fire-immune equipment, such as netherite and adamant equipment, is unaffected.
- INFX arrows can now be fired normally by dispensers instead of merely being ejected.
- Bowls thrown into water now become water bowls, just like buckets.
- Fixed doors becoming uncraftable after vanilla recipes were restored. All 14 direct-crafting recipes for wooden, bamboo, iron, and copper doors retain vanilla’s six-material layout, but their output has been reduced from 3 doors to 1, matching the single-stack limit of INFX door items.
- Fixed 67 recipe groups—including rails, scaffolding, shelves, metal-ingot uncrafting, and colored blocks—becoming uncraftable because their outputs exceeded INFX stack limits. The result slot now stores a legal single-stack quantity, the client displays the vanilla total, and completing the recipe delivers the full vanilla output split into valid stacks.

---

## 0t6
## Bug Fixes
- Swords, scythes, axes, and all other melee weapons no longer carry the `ATTACK_RANGE` component. Melee reach now follows the 1.5-block rule used by bare hands and vanilla weapons, or 5 blocks in Creative mode. Client-side targeting also follows the vanilla path again: while mounted, the crosshair no longer targets the mount, and attacks no longer accidentally hit it.
- Bare-handed attacks and all items without `ATTACK_RANGE` now use a 1.5-block melee range, or 5 blocks in Creative mode, and this works correctly for client-side left-click attacks. Base block and entity interaction reach is also 1.5 blocks; tools with reach bonuses still extend it.
- Sword and scythe sweep attacks no longer trigger while mounted, sprinting, or performing a critical jump attack. They now match the vanilla 26.1.2 `isSweepAttack` conditions: only grounded, non-sprinting, non-mounted normal attacks can sweep.
- Updated the Earth Element spawn egg texture.
- Reverted the Earth Element entity texture and emissive texture to the pre-replacement MITE vanilla versions. The source list and SHA-256 hashes were synchronized accordingly; equipment textures are unaffected.
- During player default-attribute creation, a Mixin now directly sets the base block and entity interaction reach to 1.5 blocks. Tool-provided reach bonuses continue to stack through the vanilla attribute system, and Creative mode remains at 5 blocks.
- Strays, Parched, Bogged, and Wither Skeletons no longer spawn empty-handed. These variants without INFX replacements no longer use vanilla bows and instead carry INFX Iron Swords. Zombies and Drowned remain unarmed, while naturally spawned vanilla Skeletons are replaced by INFX Skeletons.
- Carrot-on-a-stick and warped-fungus-on-a-stick variants made from all materials can now properly control and accelerate mounts, including vanilla and INFX Pigs and Striders. Vanilla 26.1 previously recognized only the exact vanilla items.
- Carrot-on-a-stick and warped-fungus-on-a-stick items now use the vanilla `handheld_rod` model, instead of being held like ordinary items.
- Pigs and horses, including INFX replacement entities, can now properly equip saddles. Horses can also equip horse armor, and the saddle and horse-armor slots are visible again in horse inventories.
- Tamed Wolves and Dread Wolves can equip wolf armor and repair it with scutes. Vanilla 26.1 restricts wolf armor to the exact `minecraft:wolf` type, so replacement entities now handle this separately.
- Feeding bones to Wolves or Dread Wolves during the taming cooldown no longer consumes the bones.
- Fixed breeding inheritance: Warm Pig breeding continues to produce Warm Pigs; warm, cold, and temperate Cow variants, Chicken variants, Sheep wool colors, and Horse coat colors and markings are now inherited by offspring. Foals also inherit their parents’ health, speed, and jump attributes. Offspring of tamed Wolves and Dread Wolves inherit the variant, sounds, collar color, taming status, and owner. Breeding Dread Wolves now produces Dread Wolves instead of vanilla Wolves.
- Llamas and Donkeys, including Mules, now follow the same untamed mounting and feeding cooldown rules as Horses. After throwing off a rider, they refuse mounting for 4,000 ticks; after accepting food, they refuse further feeding for 4,000 ticks.
- Livestock now actively seek out breeding food dropped on the ground whenever their food value is not full. Previously, they only pathfound for food when extremely hungry, and their passive pickup range was too small. Eating from the ground now restores their food value.
- Removed the redundant `InfiniteX` prefix from the Copper Pickaxe display name, restoring it to `Copper Pickaxe`.
- Lava buckets made from different metals can now interact with cauldrons. Empty buckets can draw lava from lava cauldrons, and lava buckets can fill empty cauldrons, including other cauldron variants, matching the vanilla 26.1.2 lava-bucket rules.
- Ender Pearl teleportation no longer deals 5 points of teleport damage. Vanilla reports this damage with the `fall` death message, causing players to mistake it for fall damage.
- Fixed Phase Spider eyes not glowing by adding a green emissive eye layer. The original red emissive layer had been removed without a replacement; the green eyes now glow like vanilla Spider eyes.
- Fixed tamed Dread Wolves continuing to follow players while sitting. Dread Wolves and Hellhounds shared the `Enemy` marker, causing generic enemy rules—such as targeting players in lit areas within 48 blocks, pathfinding toward the brightest block, and flanking or digging toward targets—to bypass the sitting state and directly control navigation. Dread Wolves, whether tamed or not, no longer participate in these generic enemy rules, so sitting Dread Wolves will not follow players or light sources.

---

## 0t5-0v2
## Bug Fixes
- Swords with Fire Aspect can now right-click to ignite TNT and unlit campfires, including soul campfires. Each use consumes 1 durability.
- Enchanting-table bookshelf detection now supports corner positions (±2, ±2), matching modern vanilla behavior. Emerald and diamond enchanting tables still reach full power at 50/100.
- Metal safe opening and closing sounds now use the vanilla copper chest sounds.
- Saddle crafting now produces 1 saddle, matching MITE: 5 leather + 2 iron nuggets.
- Restored the lead recipe: string/sinew + slimeball produces 2 leads.
- Added nine warped-fungus fishing-rod variants with different metal hooks. They are crafted from the corresponding metal fishing rod and warped fungus, and can be dismantled back into fishing rods. Hook-material tooltips and Creative tabs are synchronized.
- Water bottles enchanted at an enchanting table now produce Bottles o’ Enchanting instead of Bottles of Disenchanting. Bottles of Disenchanting remain craftable.
- Thrown Bottles o’ Enchanting now always grant 200 experience instead of the vanilla random 3–11 XP.
- Added a recipe for enchanted golden apples: golden apple + Bottle o’ Enchanting.
- Blocks destroyed by explosions now drop MITE-specific items: wool drops string, wood drops sticks, bricks drop 1 brick, lapis lazuli blocks have a 50% chance to drop 9 lapis lazuli, stone and end stone drop cobblestone, coal blocks have a 50% chance to drop 9 coal, and terracotta and netherrack drop nothing. Cobblestone and mossy cobblestone now drop gravel when destroyed by any explosion, not only TNT.
- Metal buckets and bowls now interact correctly with cauldrons. Empty buckets can draw water from full water cauldrons, lowering the level by 3; water buckets can refill them by 3 levels. Empty bowls draw 1 level of water, and water bowls return 1 level.
- Full buckets of water, lava, milk, or stone, as well as fish buckets and powder snow buckets, now return the corresponding empty bucket as a crafting remainder according to MITE rules. Stone-bucket dismantling recipes also return an empty bucket.
- Water placed from metal buckets containing entities, such as fish buckets, now behaves like ordinary bucket water: after 16 ticks it becomes flowing water and dissipates.
- Paid source-liquid placement using Ctrl + 100 XP now plays MITE’s XP-drain sound (`level_drain`).
- Phase Spiders no longer render the vanilla red glowing-eye layer over their own green eyes.
- Bricks, nether bricks, and resin bricks can now be thrown by right-clicking. They consume 1 item, deal 2 throwing damage, and break glass panes on impact. The MITE stack limit of 8 is unchanged.
- Blue and brown eggs from vanilla 1.26.2 can now be eaten like regular eggs. Their MITE food values are satiation 1, nutrition 3, and protein.
- Throwing slimeballs at sheep, including black and gray acid balls, immediately corrodes and removes their wool. Slime monsters now have the same effect in melee attacks.
- Beetroot soup and rabbit stew recipes now use water bowls instead of ordinary bowls.
- Removed the sandstone/red sandstone-to-glass smelting recipes. Instead, 4 sand or 4 red sand can be batch-smelted: wood-level heat produces sandstone/red sandstone, while coal-level heat or higher produces glass.
- Flint and steel durability is now 16, matching MITE.
- Torches, soul torches, redstone torches, and copper torches can now be used as furnace fuel. Each burns for 800 ticks.
- Creative-mode players now have a 5-block reach for blocks and entities.
- Fences, bars, and various walls now have a 1-block collision height for ordinary entities and can be jumped over. Minecart collision height is unchanged.
- Obsidian furnaces, netherrack furnaces, stone furnaces, blast furnaces, and smokers now drop themselves when broken bare-handed.
- Failed wolf/dire-wolf taming attempts now trigger a 5-second feeding cooldown. The worst result can also probabilistically make the wolf hostile, except during blue-moon nights.
- Hellhounds can no longer be fed bones or tamed. Their maximum health remains 20.
- Vanilla wolves, including those spawned by spawn eggs or commands, now also drop 1 leather when killed.
- Wild horses now enter a 4,000-tick feeding cooldown after each food item. Tamed horses are unaffected.
- Zombie Pigmen have been restored as modern Zombified Piglins, using the Piglin model and vanilla textures. Their name is now “Zombified Piglin.”
- Spawners in simple Underground World dungeons no longer generate Ancient Skeleton Lords and now generate MITE’s Ancient Corpse Guards.
- Creepers and Hell Creepers no longer gain equipment based on world age.
- Swords and scythes now declare vanilla sweeping capability, allowing real-player attacks to trigger sweeping attacks.
- Strays, Bogged, Charred Skeletons, and other vanilla skeleton variants no longer spawn with vanilla weapons or armor, nor drop vanilla arrows or tipped arrows when killed. MITE material-arrow drops are unchanged.
- Spider webs, obsidian, and crying obsidian now all have MITE obsidian hardness: `8.0`.
- Fixed shears incorrectly shearing beds, bamboo—including bamboo shoots—and sugar cane when right-clicked. These blocks remain normally effective targets for shears.
- Restored the MITE stick recipe: 2 vertically arranged planks produce 4 sticks.
- Fixed Protection enchantment differences. Protection-family incompatibility, critical armor durability, non-player armor factors, and maximum-level calculations for Fire Protection and Blast Protection now follow MITE rules.
- Resistance now converts to armor value according to MITE mechanics: Resistance I and II add 5 and 10 armor points respectively, while the duplicated vanilla percentage damage reduction is removed.
- Sticks and bones now use MITE melee behavior again. Successful survival-mode hits have a 1/50 chance for sticks and a 1/100 chance for bones to be consumed. Creative-mode attacks never consume them.
- Fixed default component registration for sticks and bones. Their melee attack reach is now 2.0 blocks, or 5.0 blocks in Creative mode, without changing their block or entity interaction reach.
- Fixed vanilla swords being unable to receive Sharpness.
- Netherrack, Crimson Nylium, and Warped Nylium now require an iron pickaxe or better to mine.
- Fixed Poison dealing damage immediately when first applied. The first server tick no longer causes damage; subsequent damage still occurs at MITE’s `100 >> level` tick interval.
- Adjusted the Slaying enchantment: it cannot apply to swords and is mutually exclusive with Sharpness, Smite, and Bane of Arthropods. Level I adds 1 damage, each subsequent level adds 0.75 damage, and the maximum level is V.
- When players receive the Hunger effect, the food bar now switches to the dedicated Hunger icon and returns to the normal icon when the effect ends.
- Replaced 139 block, entity, and item textures.
- Replaced the entity textures for the Earth Element’s clay, cobblestone, end stone, netherrack, and obsidian forms, as well as its lava form.
- Replaced the entity equipment textures for forged metals and chainmail, including baby-entity equipment layers.
- Added the red sandstone-to-glass smelting recipe to match the sandstone-to-glass recipe.
- Restored the glass bottle recipe: 3 glass produce 3 glass bottles.
- Removed Ancient Metal Ingot drops from Ancient Corpses, Ancient Corpse Guards, and Ancient Bone Kings. Their Ancient Metal equipment drops remain.
- Wolves, dire wolves, and hellhounds once again drop MITE experience: 5, 10, and 15 XP respectively. Other animals still provide no experience.
- Added nine shapeless dismantling recipes for metal/material arrows: 1 arrow produces 1 nugget or fragment, matching MITE dismantling rules.
- Fertilizer use on farmland and crops is now consumed on both sides. The client provides arm-swing feedback and ensures the request reaches the server, while the server records farmland fertility.
- Fixed golden apples and water bottles not activating the level options on the right side of the enchanting table. Conversion options now activate correctly according to their cost and currency.
- Donkeys and mules now drop 1–3 beef on death, matching horses. The Looting enchantment also applies to them.
- Onions can now be planted directly as seeds. Mature onions yield 2 items, with a 25% chance to yield 1 additional item.
- Sheep wool drop chance on death has been reduced from 100% to MITE’s 50%. Sheep hide remains at 50%.
- Vanilla goats can now be milked with empty buckets, including empty metal buckets, and share the cow’s daily limit of 4 units of milk.
- Pigs can now eat brown mushrooms for breeding, temptation, and nearby foraging.
- Corrected the anvil recipe to match MITE: a full row of metal blocks on top, 1 ingot in the center of the middle row, and 3 ingots across the bottom row.
- Added nine material fishing-rod recipes using a nugget/fragment, a stick, and string in the MITE fishing-rod pattern. Added the corresponding carrot-on-a-stick items and recipes; carrot-on-a-stick items can be dismantled back into fishing rods.
- Zombies, husks, drowned, zombie villagers, skeletons, strays, and wither skeletons no longer spawn with vanilla weapons or armor. Zombies instead spawn unequipped, with world-age equipment assigned separately by the existing system. Skeletons now use MITE wooden bows, with a 25% chance to use sticks.
- Overworld structure loot tables no longer contain Ancient Metal items. These items can still be found in Nether and Underground World loot.
- Animals killed by Fire Aspect or other fire damage now drop their corresponding cooked meat: beef, porkchops, chicken, or mutton.
- Azalea bushes and flowering azalea bushes now have 0.02 hardness, matching MITE shrubs.
- Stained glass and stained glass panes now drop glass shards like ordinary glass: 6 shards from glass blocks and 1 shard from glass panes.
- Ashen soil, mycelium, dirt paths, clay, and rooted dirt are no longer gravity blocks and no longer collapse when suspended.
- Bone meal no longer grows saplings and is not consumed in that case. Other block-use functions, such as growing grass and seagrass in water, remain available.
- Fishing mechanics have been completed: rods can only be cast while standing in a boat, on horseback, or on dry ground; casting while in water is impossible. Bite wait time now varies with MITE’s day/night cycle, with dawn and dusk being fastest. Rain and earthworm bait halve the wait time.
- Weapons and tools now receive MITE’s height advantage when attacking targets below the player. For every 0.5 blocks the target is more than 0.5 blocks below the player, attack reach increases by `(height difference - 0.5) × 0.5`, up to +1 block. Targets above the player reduce reach symmetrically.
- Restored deleted vanilla basic recipes: bowls, using 3 planks to produce 4 bowls, and wool, using 4 string to produce 1 wool.
- Restored the 16 basic and mixed dye recipes: bone meal for white, ink sacs for black, plants for their respective colors, and two- or three-color combinations.
- Restored the two-way 3×3 crafting and dismantling recipes for blocks of raw copper, raw iron, and raw gold.
- Restored recipes for beetroot soup, rabbit stew, cookies producing 8 items, melon seeds, and wheat seeds.
- Short dead grass, tall dead grass, shrubs, firefly bushes, weeping vines, and twisting vines now have 0.02 hardness.
- Left-clicking with shears now only intercepts blocks on which shears are ineffective. Leaves, wool, plants, cobwebs, and similar blocks can be broken with shears by left-clicking.
- In Creative mode, right-clicking with shears can now correctly harvest block drops without consuming durability.
- In Creative mode, holding a sword no longer allows ordinary blocks to be broken. Swords retain their ability to break blocks they are effective against.
- Cobwebs can be broken by hand or with swords but no longer drop anything. Only shears make them drop 1 string.
- Carving pumpkins with shears now drops only 1 pumpkin seed.
- Rotten flesh has an 80% chance, and raw chicken a 30% chance, to apply MITE Poison I for 200 ticks when eaten. Their Hunger effect is retained.
- Poison damage now occurs every `100 >> level` ticks, matching MITE. Death by poison now displays the dedicated message “Died of Poison.”
- Item entities dropped on lit campfires now cook at 1 progress per tick and become cooked at 100 progress. Cooked food no longer burns on campfires.
- The Chinese display name for milk buckets has been unified from “牛奶” to “奶,” such as “Iron Milk Bucket.”
- Restored the vanilla Sharpness, Sweeping Edge, and Swift Sneak enchantments. Swords and scythes can receive Sharpness and Sweeping Edge, while boots can receive Swift Sneak.
- Sword and scythe attacks now inherently sweep for 50% damage. Each level of Sweeping Edge adds 25%.
- Silver-weapon tooltips now always display their +25% damage against undead. Each piece of silver armor reduces the duration of negative effects by 15%.
- Warhammers and short wooden clubs/wooden clubs now deal +2 damage to skeleton-type creatures, with this bonus described in their tooltips.

---

## 0v1
### Progress
- Added a new advancement tree.
### World
- The underground world may be bigger and more complex now.
- Ancient cities may now also appear at the bottom of the deep dark in the underground world.
- Cave spiders now also spawn in lush caves.
- Loot chests have been reset/reshuffled.
- Gravel can now only be mined with a flint shovel or better, and replaces sand generation in some biomes.
- Woodland mansions now require any online player to have reached 100,000 XP.
- Ocean monuments now require any player to have entered a Nether fortress.
- The six types of overworld ruined portals now require any player to have entered the Nether.
- Shipwrecks and beached shipwrecks now require any player to have killed a guardian.
- Pillager outposts now share the day-60 requirement with villages, plus the condition that an iron-tier tool has been crafted worldwide.
### Commands
- `/infx` shows all newly added commands.
### Items
- Crafting flint/obsidian now requires stripped logs.
- Metal coins can be used directly with a right-click.
### Server
- Permissions can only be fully opened in test mode.

---

## 0t1-0t4
- Basically finished re-implementing MITE content.
