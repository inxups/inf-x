#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PACK_TEXTURES="${1:-$ROOT/codex/reference/mite- resource-pack/assets/minecraft/textures}"
SOURCE_TEXTURES="${2:-$ROOT/codex/reference/mite-src/assets/minecraft/textures}"
ASSET_ROOT="$ROOT/src/main/resources/assets/infx"
DEST_TEXTURES="$ASSET_ROOT/textures"
MANIFEST="$ASSET_ROOT/mite_texture_manifest.tsv"
ROWS="$(mktemp)"
trap 'rm -f "$ROWS" "$MANIFEST.tmp"' EXIT

if [[ -f "$MANIFEST" ]]; then
  tail -n +2 "$MANIFEST" | while IFS=$'\t' read -r _ _ destination _; do
    [[ -n "$destination" ]] && rm -f "$ASSET_ROOT/$destination"
  done
fi

sync() {
  local source_kind="$1" source_rel="$2" destination_rel="$3" source_root source hash
  case "$source_kind" in
    resource-pack) source_root="$PACK_TEXTURES" ;;
    mite-src) source_root="$SOURCE_TEXTURES" ;;
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

for material in copper silver gold iron ancient_metal mithril adamantium; do
  sync resource-pack "blocks/anvil/$material/base.png" "block/anvil/$material/base.png"
  for stage in 0 1 2; do
    sync resource-pack \
      "blocks/anvil/$material/top_damaged_$stage.png" \
      "block/anvil/$material/top_damaged_$stage.png"
  done
done

# MITE stores each metal safe as a chest texture sheet.  The block model maps
# the sheet's original 64x64 UV layout, so retain the complete source image.
for material in copper silver gold iron ancient_metal mithril adamantium; do
  sync resource-pack "entity/chest/${material}_single.png" "block/safe/${material}.png"
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

row_count="$(wc -l < "$ROWS" | tr -d ' ')"
[[ "$row_count" == 538 ]] || { echo "Expected 538 textures, got $row_count" >&2; exit 1; }
{
  printf 'source_root\tsource\tdestination\tsha256\n'
  LC_ALL=C sort -t $'\t' -k3,3 "$ROWS"
} > "$MANIFEST.tmp"
mv "$MANIFEST.tmp" "$MANIFEST"
printf 'Synchronized %s approved MITE textures\n' "$row_count"
