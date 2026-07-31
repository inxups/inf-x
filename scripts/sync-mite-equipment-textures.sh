#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PACK_TEXTURES="${1:-$ROOT/codex/reference/mite-resource-pack/assets/minecraft/textures}"
SOURCE_TEXTURES="${2:-$ROOT/codex/reference/mite-src/assets/minecraft/textures}"
ITF_REBORN_TEXTURES="${3:-${ITF_REBORN_TEXTURES:-/Users/inxups/Downloads/ITF-Reborn-R196/src/main/resources/assets/miteitfrb/textures}}"
ASSET_ROOT="$ROOT/src/main/resources/assets/infx"
DEST_TEXTURES="$ASSET_ROOT/textures"
MANIFEST="$ASSET_ROOT/infx_texture_manifest.tsv"
ROWS="$(mktemp)"
PREVIOUS_ROWS="$(mktemp)"
trap 'rm -f "$ROWS" "$PREVIOUS_ROWS" "$MANIFEST.tmp"' EXIT

if [[ -f "$MANIFEST" ]]; then
  tail -n +2 "$MANIFEST" > "$PREVIOUS_ROWS"
fi

sync() {
  local source_kind="$1" source_rel="$2" destination_rel="$3" source_root source hash
  case "$source_kind" in
    resource-pack) source_root="$PACK_TEXTURES" ;;
    mite-src) source_root="$SOURCE_TEXTURES" ;;
    itf-reborn) source_root="$ITF_REBORN_TEXTURES" ;;
    *) echo "Unknown source kind: $source_kind" >&2; exit 1 ;;
  esac
  source="$source_root/$source_rel"
  [[ -f "$source" ]] || { echo "Missing approved source: $source" >&2; exit 1; }
  mkdir -p "$DEST_TEXTURES/$(dirname "$destination_rel")"
  cp "$source" "$DEST_TEXTURES/$destination_rel"
  if command -v sha256sum >/dev/null 2>&1; then
    hash="$(sha256sum "$source" | awk '{print $1}')"
  else
    hash="$(shasum -a 256 "$source" | awk '{print $1}')"
  fi
  printf '%s\t%s\t%s\t%s\n' "$source_kind" "$source_rel" "textures/$destination_rel" "$hash" >> "$ROWS"
}

sync resource-pack items/shards/flint.png item/flint_chip.png
sync resource-pack items/shards/obsidian.png item/obsidian_shard.png
sync resource-pack items/shards/emerald.png item/emerald_shard.png
sync resource-pack items/shards/diamond.png item/diamond_shard.png
sync resource-pack items/shards/quartz.png item/nether_quartz_shard.png
sync resource-pack items/shards/glass.png item/glass_shard.png
sync resource-pack items/sinew.png item/sinew.png
sync resource-pack items/manure.png item/manure.png
sync resource-pack items/nuggets/silver.png item/silver_nugget.png
sync resource-pack items/nuggets/mithril.png item/mithril_nugget.png
sync resource-pack items/nuggets/adamantium.png item/adamantium_nugget.png
sync resource-pack items/nuggets/ancient_metal.png item/ancient_metal_nugget.png
sync resource-pack items/ingots/silver.png item/silver_ingot.png
sync resource-pack items/ingots/mithril.png item/mithril_ingot.png
sync resource-pack items/ingots/adamantium.png item/adamantium_ingot.png
sync resource-pack items/ingots/ancient_metal.png item/ancient_metal_ingot.png
sync resource-pack blocks/silver_ore.png block/silver_ore.png
sync resource-pack blocks/mithril_ore.png block/mithril_ore.png
sync resource-pack blocks/adamantium_ore.png block/adamantium_ore.png
for material in silver ancient_metal mithril adamantium; do
  sync resource-pack "blocks/${material}_block.png" "block/${material}_block.png"
done
sync resource-pack blocks/snow.png block/snow_slab.png

# Visible MITE row-crop states. Beetroot comes from the owner-provided ITF Reborn source.
for crop in wheat carrots potatoes; do
  case "$crop" in
    wheat)
      normal_last=7
      dead_last=6
      ;;
    carrots|potatoes)
      normal_last=3
      dead_last=2
      ;;
  esac
  for stage in $(seq 0 "$normal_last"); do
    sync resource-pack "blocks/crops/$crop/$stage.png" "block/crops/$crop/$stage.png"
    sync resource-pack "blocks/crops/$crop/blighted/$stage.png" "block/crops/$crop/blighted/$stage.png"
  done
  for stage in $(seq 0 "$dead_last"); do
    sync resource-pack "blocks/crops/$crop/dead/$stage.png" "block/crops/$crop/dead/$stage.png"
  done
done
for stage in 0 1 2 3; do
  sync itf-reborn "blocks/crops/beetroot/$stage.png" "block/crops/beetroot/$stage.png"
  sync itf-reborn "blocks/crops/beetroot/blighted/$stage.png" "block/crops/beetroot/blighted/$stage.png"
  sync itf-reborn "blocks/crops/beetroot/dead/$stage.png" "block/crops/beetroot/dead/$stage.png"
done

for material in copper silver gold iron ancient_metal mithril adamantium; do
  sync resource-pack "blocks/anvil/$material/base.png" "block/anvil/$material/base.png"
  for stage in 0 1 2; do
    sync resource-pack \
      "blocks/anvil/$material/top_damaged_$stage.png" \
      "block/anvil/$material/top_damaged_$stage.png"
  done
done

# MITE metal safes use 64x64 chest sheets on the chest entity atlas (vanilla BER path).
for material in copper silver gold iron ancient_metal mithril adamantium; do
  sync resource-pack "entity/chest/${material}_single.png" "entity/chest/${material}.png"
done

for material in copper silver gold rusted_iron iron ancient_metal mithril adamantium; do
  sync resource-pack "items/chains/$material.png" "item/${material}_chain.png"
done
for material in copper silver gold ancient_metal mithril adamantium; do
  sync resource-pack "items/coins/$material.png" "item/${material}_coin.png"
done

METALS=(copper silver gold rusted_iron iron ancient_metal mithril adamantium)
SHOVELS=(wood flint obsidian "${METALS[@]}")
ROCK_AND_METAL=(flint obsidian "${METALS[@]}")
FISHING=(flint obsidian copper silver gold iron ancient_metal mithril adamantium)
ARROWS=(flint obsidian copper silver gold rusted_iron iron ancient_metal mithril adamantium)
PLATE=(leather copper silver gold rusted_iron iron ancient_metal mithril adamantium)
HORSE=(copper silver gold iron ancient_metal mithril adamantium)
BOWS=(wood ancient_metal mithril)
PIECES=(helmet chestplate leggings boots)

sync_tool() {
  local material="$1" type="$2" key="${1}_${2}"
  case "$key" in
    wood_shovel)
      sync mite-src items/wood_shovel.png item/wood_shovel.png
      ;;
    iron_pickaxe|iron_shovel|iron_axe|iron_hoe|iron_sword)
      sync resource-pack "items/$key.png" "item/$key.png"
      ;;
    iron_shears)
      sync resource-pack items/shears.png item/iron_shears.png
      ;;
    *)
      sync resource-pack "items/tools/$key.png" "item/$key.png"
      ;;
  esac
}

for material in "${METALS[@]}"; do sync_tool "$material" pickaxe; done
for material in "${SHOVELS[@]}"; do sync_tool "$material" shovel; done
for type in hatchet axe; do
  for material in "${ROCK_AND_METAL[@]}"; do sync_tool "$material" "$type"; done
done
for type in hoe mattock battle_axe war_hammer scythe shears; do
  for material in "${METALS[@]}"; do sync_tool "$material" "$type"; done
done
sync_tool wood cudgel
sync_tool wood club
for material in flint obsidian; do sync_tool "$material" knife; done
for type in sword dagger; do
  for material in "${METALS[@]}"; do sync_tool "$material" "$type"; done
done

for material in "${FISHING[@]}"; do
  sync resource-pack "items/fishing_rods/${material}_uncast.png" "item/${material}_fishing_rod.png"
done
sync resource-pack items/fishing_rod_cast.png item/fishing_rod_cast.png

for material in "${ARROWS[@]}"; do
  sync resource-pack "items/arrows/${material}_arrow.png" "item/${material}_arrow.png"
done

for bow in "${BOWS[@]}"; do
  sync resource-pack "items/bows/$bow/standby.png" "item/${bow}_bow.png"
  for arrow in "${ARROWS[@]}"; do
    for frame in 0 1 2; do
      sync resource-pack \
        "items/bows/$bow/${arrow}_arrow_${frame}.png" \
        "item/${bow}_bow/${arrow}_${frame}.png"
    done
  done
done

for material in "${PLATE[@]}"; do
  for piece in "${PIECES[@]}"; do
    case "$material" in
      leather|iron)
        sync resource-pack "items/${material}_${piece}.png" "item/${material}_${piece}.png"
        ;;
      *)
        sync resource-pack "items/armor/${material}_${piece}.png" "item/${material}_${piece}.png"
        ;;
    esac
    if [[ "$material" == leather ]]; then
      sync resource-pack \
        "items/leather_${piece}_overlay.png" \
        "item/leather_${piece}_overlay.png"
    fi
  done
done

for material in "${METALS[@]}"; do
  for piece in "${PIECES[@]}"; do
    if [[ "$material" == iron ]]; then
      sync resource-pack \
        "items/chainmail_${piece}.png" \
        "item/iron_chainmail_${piece}.png"
    else
      sync resource-pack \
        "items/armor/${material}_chainmail_${piece}.png" \
        "item/${material}_chainmail_${piece}.png"
    fi
  done
done

for material in "${HORSE[@]}"; do
  case "$material" in
    gold|iron)
      sync resource-pack "items/${material}_horse_armor.png" "item/${material}_horse_armor.png"
      ;;
    *)
      sync resource-pack "items/armor/horse/${material}.png" "item/${material}_horse_armor.png"
      ;;
  esac
done

for material in "${PLATE[@]}"; do
  sync resource-pack \
    "models/armor/${material}_layer_1.png" \
    "entity/equipment/humanoid/${material}.png"
  sync resource-pack \
    "models/armor/${material}_layer_1.png" \
    "entity/equipment/humanoid_baby/${material}.png"
  sync resource-pack \
    "models/armor/${material}_layer_2.png" \
    "entity/equipment/humanoid_leggings/${material}.png"
done
sync resource-pack models/armor/leather_layer_1_overlay.png entity/equipment/humanoid/leather_overlay.png
sync resource-pack models/armor/leather_layer_1_overlay.png entity/equipment/humanoid_baby/leather_overlay.png
sync resource-pack models/armor/leather_layer_2_overlay.png entity/equipment/humanoid_leggings/leather_overlay.png

for material in "${METALS[@]}"; do
  source_stem="${material}_chainmail"
  [[ "$material" == iron ]] && source_stem=chainmail
  sync resource-pack \
    "models/armor/${source_stem}_layer_1.png" \
    "entity/equipment/humanoid/${material}_chainmail.png"
  sync resource-pack \
    "models/armor/${source_stem}_layer_1.png" \
    "entity/equipment/humanoid_baby/${material}_chainmail.png"
  sync resource-pack \
    "models/armor/${source_stem}_layer_2.png" \
    "entity/equipment/humanoid_leggings/${material}_chainmail.png"
done

for material in "${HORSE[@]}"; do
  sync resource-pack \
    "entity/horse/armor/horse_armor_${material}.png" \
    "entity/equipment/horse_body/${material}.png"
done

for material in mithril adamantium; do
  for rune in {0..15}; do
    sync resource-pack \
      "blocks/runestones/${material}/${rune}.png" \
      "block/runestones/${material}/${rune}.png"
  done
done
sync resource-pack blocks/runegate.png block/runegate.png
sync resource-pack blocks/runegate.png.mcmeta block/runegate.png.mcmeta
sync resource-pack blocks/mantle.png block/mantle.png
sync resource-pack blocks/mantle.png.mcmeta block/mantle.png.mcmeta
sync resource-pack blocks/portal_nether.png block/nether_portal.png
sync resource-pack blocks/portal_nether.png.mcmeta block/nether_portal.png.mcmeta

sync resource-pack blocks/emerald_enchanting_table_side.png block/emerald_enchanting_table_side.png
sync resource-pack blocks/emerald_enchanting_table_top.png block/emerald_enchanting_table_top.png
sync resource-pack blocks/crafting_table/flint/top.png block/flint_workbench_top.png
sync resource-pack blocks/crafting_table/obsidian/top.png block/obsidian_workbench_top.png
for material in copper silver gold iron ancient_metal mithril adamantium; do
  sync resource-pack \
    "blocks/crafting_table/$material/front.png" \
    "block/${material}_workbench_front.png"
  sync resource-pack \
    "blocks/crafting_table/$material/side.png" \
    "block/${material}_workbench_side.png"
done

for furnace in clay hardened_clay sandstone obsidian netherrack; do
  sync resource-pack \
    "blocks/furnace/$furnace/front_off.png" \
    "block/${furnace}_furnace_front.png"
  sync resource-pack \
    "blocks/furnace/$furnace/front_on.png" \
    "block/${furnace}_furnace_front_on.png"
  sync resource-pack \
    "blocks/furnace/$furnace/side.png" \
    "block/${furnace}_furnace_side.png"
  sync resource-pack \
    "blocks/furnace/$furnace/top.png" \
    "block/${furnace}_furnace_top.png"
done

# Keep every custom R196 food item on its matching authorized MITE icon rather
# than borrowing a visually similar vanilla item.
sync resource-pack items/food/flour.png item/flour.png
sync resource-pack items/bowls/bowl_water.png item/water_bowl.png
sync resource-pack items/food/dough.png item/dough.png
sync resource-pack items/bowls/bowl_salad.png item/salad.png
sync resource-pack items/food/blueberries.png item/blueberries.png
sync resource-pack items/bowls/porridge.png item/blueberry_porridge.png
sync resource-pack blocks/bushes/blueberry.png block/blueberry_bush.png
sync resource-pack blocks/bushes/blueberry_picked.png block/blueberry_bush_picked.png
sync resource-pack items/bowls/bowl_milk.png item/milk_bowl.png
sync resource-pack items/bowls/cereal.png item/cereal_porridge.png
sync resource-pack items/food/chocolate.png item/chocolate.png
sync resource-pack items/bowls/pumpkin_soup.png item/pumpkin_soup.png
sync resource-pack items/bowls/cream_of_mushroom_soup.png item/cream_of_mushroom_soup.png
sync resource-pack items/food/onion.png item/onion.png
sync resource-pack items/bowls/vegetable_soup.png item/vegetable_soup.png
sync resource-pack items/bowls/cream_of_vegetable_soup.png item/cream_of_vegetable_soup.png
sync resource-pack items/bowls/chicken_soup.png item/chicken_soup.png
sync resource-pack items/bowls/beef_stew.png item/beef_stew.png
sync resource-pack items/food/orange.png item/orange.png
sync resource-pack items/bowls/sorbet.png item/fruit_ice.png
sync resource-pack items/food/cheese.png item/cheese.png
sync resource-pack items/bowls/mashed_potato.png item/mashed_potato.png
sync resource-pack items/bowls/ice_cream.png item/ice_cream.png
sync resource-pack items/food/banana.png item/banana.png
sync resource-pack items/food/worm_raw.png item/worm.png
sync resource-pack items/food/worm_cooked.png item/cooked_worm.png

for color in green ochre crimson gray black; do
  sync mite-src "items/gelatinous_sphere/${color}.png" "item/gelatinous_sphere/${color}.png"
done

for cube in slime jelly blob ooze pudding magmacube; do
  sync resource-pack "entity/slime/${cube}.png" "entity/slime/${cube}.png"
done

# R196 new-monster entity skins from the authorized MITE pack.
sync resource-pack entity/ghoul.png entity/ghoul.png
sync resource-pack entity/shadow.png entity/shadow.png
sync resource-pack entity/wight.png entity/wight.png
sync resource-pack entity/zombie/revenant.png entity/zombie/revenant.png
sync resource-pack entity/skeleton/longdead.png entity/skeleton/longdead.png
sync resource-pack entity/skeleton/bone_lord.png entity/skeleton/bone_lord.png
sync resource-pack entity/skeleton/longdead_guardian.png entity/skeleton/longdead_guardian.png
sync resource-pack entity/spider/black_widow.png entity/spider/black_widow.png
sync resource-pack entity/spider/demon_spider.png entity/spider/demon_spider.png
sync resource-pack entity/spider/wood_spider.png entity/spider/wood_spider.png
sync resource-pack entity/spider/phase_spider.png entity/spider/phase_spider.png
sync resource-pack entity/spider/cave_spider.png entity/spider/cave_spider.png
sync resource-pack entity/creeper/infernal_creeper.png entity/creeper/infernal_creeper.png
sync resource-pack entity/fire_elemental.png entity/fire_elemental.png
sync resource-pack \
  entity/earth_elemental/stone/earth_elemental_stone.png \
  entity/earth_elemental/stone/earth_elemental_stone.png
sync resource-pack \
  entity/earth_elemental/stone/earth_elemental_stone_magma.png \
  entity/earth_elemental/stone/earth_elemental_stone_magma.png
sync resource-pack \
  entity/earth_elemental/obsidian/earth_elemental_obsidian.png \
  entity/earth_elemental/obsidian/earth_elemental_obsidian.png
sync resource-pack \
  entity/earth_elemental/obsidian/earth_elemental_obsidian_magma.png \
  entity/earth_elemental/obsidian/earth_elemental_obsidian_magma.png
sync resource-pack \
  entity/earth_elemental/netherrack/earth_elemental_netherrack.png \
  entity/earth_elemental/netherrack/earth_elemental_netherrack.png
sync resource-pack \
  entity/earth_elemental/netherrack/earth_elemental_netherrack_magma.png \
  entity/earth_elemental/netherrack/earth_elemental_netherrack_magma.png
sync resource-pack \
  entity/earth_elemental/end_stone/earth_elemental_end_stone.png \
  entity/earth_elemental/end_stone/earth_elemental_end_stone.png
sync resource-pack \
  entity/earth_elemental/end_stone/earth_elemental_end_stone_magma.png \
  entity/earth_elemental/end_stone/earth_elemental_end_stone_magma.png
sync resource-pack \
  entity/earth_elemental/clay/earth_elemental_clay.png \
  entity/earth_elemental/clay/earth_elemental_clay.png
sync resource-pack \
  entity/earth_elemental/clay/earth_elemental_clay_hardened.png \
  entity/earth_elemental/clay/earth_elemental_clay_hardened.png
sync resource-pack entity/earth_elemental/earth_elemental_glow.png entity/earth_elemental/earth_elemental_glow.png
sync resource-pack \
  entity/earth_elemental/earth_elemental_magma_glow.png \
  entity/earth_elemental/earth_elemental_magma_glow.png
sync resource-pack entity/silverfish/netherspawn.png entity/silverfish/netherspawn.png
sync resource-pack entity/silverfish/copperspine.png entity/silverfish/copperspine.png
sync resource-pack entity/silverfish/hoary.png entity/silverfish/hoary.png
sync resource-pack entity/bat/vampire.png entity/bat/vampire.png
sync resource-pack entity/bat/nightwing.png entity/bat/nightwing.png
sync resource-pack entity/hellhound/hellhound.png entity/hellhound/hellhound.png
sync resource-pack entity/dire_wolf/neutral.png entity/dire_wolf/neutral.png
sync resource-pack entity/dire_wolf/tame.png entity/dire_wolf/tame.png
sync resource-pack entity/dire_wolf/angry.png entity/dire_wolf/angry.png

row_count="$(wc -l < "$ROWS" | tr -d ' ')"
[[ "$row_count" == 638 ]] || { echo "Expected 638 textures, got $row_count" >&2; exit 1; }
{
  printf 'source_root\tsource\tdestination\tsha256\n'
  {
    awk -F $'\t' 'NR == FNR { replaced[$3] = 1; next } !($3 in replaced)' "$ROWS" "$PREVIOUS_ROWS"
    cat "$ROWS"
  } | LC_ALL=C sort -t $'\t' -k3,3
} > "$MANIFEST.tmp"
mv "$MANIFEST.tmp" "$MANIFEST"
printf 'Synchronized %s approved MITE textures\n' "$row_count"
