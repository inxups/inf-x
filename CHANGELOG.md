# Changelog

## 0t7
### Project Direction
- MITE alignment is abandoned: development now benchmarks against vanilla 26.1.2 / NeoForge behaviour while keeping all existing behaviour changes. MITE wording was removed from code comments, test text and identifiers (provenance manifests, resource paths, NBT keys and the vanilla Smite enchantment are intentionally unchanged).

## Bug Fixes
- Strays, parched skeletons, bogged skeletons and wither skeletons no longer spawn bare-handed: these un-replaced skeleton variants trade the vanilla bow for an InfX iron sword (zombies and drowned stay bare, and natural vanilla skeleton spawns are still replaced by the INFX skeleton).
- Every InfX carrot-on-a-stick and warped-fungus-on-a-stick material variant now controls and boosts its mount again: vanilla 26.1 pigs and striders only accept the exact vanilla `carrot_on_a_stick`/`warped_fungus_on_a_stick` as their control item, so riding with an InfX stick in hand neither steered the mount nor triggered the boost; the check now accepts every InfX stick (covering vanilla pigs and striders as well as the INFX pig replacement).
- InfX carrot-on-a-stick and warped-fungus-on-a-stick items are now held with the vanilla rod pose (`handheld_rod` model) instead of the flat generated-item pose.
- Pigs and horses (including the INFX replacement types) can now equip saddles again, and horses can equip horse armor; the horse inventory saddle and armor slots are visible again (26.1 gates equipping by entity-type tags).
- Tamed wolves and dire wolves can now equip wolf armor and repair it with armadillo scutes (26.1 restricts wolf armor to the exact minecraft:wolf type, so the replacement entities handle the equip branch themselves).
- Feeding bones to a wolf/dire wolf during its taming cooldown no longer consumes them.
- Breeding inheritance fixes: warm (tropical) pigs breed warm piglets; cow and chicken coat variants, sheep wool colors, and horse coats/markings are inherited again; foals inherit the parents' health/speed/jump attributes; tamed wolf/dire-wolf pups inherit the coat, sound variant, collar, ownership and tame state; dire wolves now breed dire wolves instead of vanilla wolves.
- Llamas and donkeys (including mules) now follow the untamed remount and feed intervals like horses: 4000 ticks after being thrown off they refuse to be mounted again, and 4000 ticks after accepting food they refuse further feedings.
- Livestock now forage dropped breeding food: they walk to and eat edible items on the ground whenever their food meter is not full (previously they only path-find when desperately hungry and the passive pickup radius was too small); ground eating restores the food meter.

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
