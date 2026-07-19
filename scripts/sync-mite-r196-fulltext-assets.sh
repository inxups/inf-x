#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "$0")/.." && pwd)"
reference_root="${MITE_REFERENCE_ROOT:-/Users/inxups/IdeaProjects/mc/inf-x/codex/reference/mite- resource-pack/assets/minecraft}"
asset_root="$project_root/src/main/resources/assets/infx"
manifest="$asset_root/mite_fulltext_manifest.tsv"

if [[ ! -d "$reference_root" ]]; then
  echo "Missing authorized MITE resource pack: $reference_root" >&2
  exit 1
fi

mkdir -p "$asset_root/textures/item" "$asset_root/textures/block" "$asset_root/sounds/records"
printf 'source_root\tsource\tdestination\tsha256\n' > "$manifest"

copy_asset() {
  local source_rel="$1"
  local destination_rel="$2"
  local source="$reference_root/$source_rel"
  local destination="$asset_root/$destination_rel"
  if [[ ! -f "$source" ]]; then
    echo "Missing authorized asset: $source" >&2
    exit 1
  fi
  mkdir -p "$(dirname "$destination")"
  cp "$source" "$destination"
  local digest
  digest="$(shasum -a 256 "$destination" | awk '{print $1}')"
  printf 'resource-pack\t%s\t%s\t%s\n' "$source_rel" "$destination_rel" "$digest" >> "$manifest"
}

materials=(copper silver gold iron ancient_metal mithril adamantium)
kinds=(empty water lava milk stone)
for material in "${materials[@]}"; do
  for kind in "${kinds[@]}"; do
    if [[ "$kind" == "empty" ]]; then
      destination="textures/item/${material}_bucket.png"
    else
      destination="textures/item/${material}_${kind}_bucket.png"
    fi
    copy_asset "textures/items/buckets/${material}/${kind}.png" "$destination"
  done
done

copy_asset "textures/items/bottle_of_disenchanting.png" "textures/item/bottle_of_disenchanting.png"

records=(underworld descent wanderer legends)
for record in "${records[@]}"; do
  copy_asset "textures/items/records/record_${record}.png" "textures/item/record_${record}.png"
  copy_asset "records/imported/${record}.ogg" "sounds/records/${record}.ogg"
done

flowers=(rose orchid allium tulip dahlia daisy)
for flower in "${flowers[@]}"; do
  copy_asset "textures/blocks/flowers/${flower}.png" "textures/block/${flower}.png"
done

copy_asset "textures/blocks/witherwood.png" "textures/block/witherwood.png"
copy_asset "textures/blocks/nether_gravel.png" "textures/block/nether_gravel.png"
copy_asset "textures/blocks/core.png" "textures/block/core.png"
copy_asset "textures/blocks/core.png.mcmeta" "textures/block/core.png.mcmeta"

echo "Synced $(($(wc -l < "$manifest") - 1)) authorized R196 full-text assets."
