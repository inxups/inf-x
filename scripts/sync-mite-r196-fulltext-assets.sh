#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "$0")/.." && pwd)"
reference_root="${MITE_REFERENCE_ROOT:-/Users/inxups/IdeaProjects/mc/inf-x/codex/reference/mite-resource-pack/assets/minecraft}"
mite_client_assets_root="${MITE_CLIENT_ASSETS_ROOT:-/Users/inxups/mc/mite/MITE-R196-FMLv3.4.2/.minecraft/assets/virtual/legacy}"
asset_root="$project_root/src/main/resources/assets/infx"
manifest="$asset_root/infx_fulltext_manifest.tsv"

if [[ ! -d "$reference_root" ]]; then
  echo "Missing authorized MITE resource pack: $reference_root" >&2
  exit 1
fi

if [[ ! -d "$mite_client_assets_root" ]]; then
  echo "Missing authorized MITE 1.6.4 client assets: $mite_client_assets_root" >&2
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

copy_client_asset() {
  local source_rel="$1"
  local destination_rel="$2"
  local source="$mite_client_assets_root/$source_rel"
  local destination="$asset_root/$destination_rel"
  if [[ ! -f "$source" ]]; then
    echo "Missing authorized MITE 1.6.4 client asset: $source" >&2
    exit 1
  fi
  mkdir -p "$(dirname "$destination")"
  cp "$source" "$destination"
  local digest
  digest="$(shasum -a 256 "$destination" | awk '{print $1}')"
  printf 'mite-client-assets\t%s\t%s\t%s\n' "$source_rel" "$destination_rel" "$digest" >> "$manifest"
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

copy_asset "textures/blocks/witherwood.png" "textures/block/witherwood.png"
copy_asset "textures/blocks/nether_gravel.png" "textures/block/nether_gravel.png"
copy_asset "textures/blocks/core.png" "textures/block/core.png"
copy_asset "textures/blocks/core.png.mcmeta" "textures/block/core.png.mcmeta"

# R196 mob sounds from authorized imported.mob pack.
copy_asset "sound/imported/mob/ghoul/say1.ogg" "sounds/mob/ghoul/say1.ogg"
copy_asset "sound/imported/mob/ghoul/say2.ogg" "sounds/mob/ghoul/say2.ogg"
copy_asset "sound/imported/mob/ghoul/hurt1.ogg" "sounds/mob/ghoul/hurt1.ogg"
copy_asset "sound/imported/mob/ghoul/hurt2.ogg" "sounds/mob/ghoul/hurt2.ogg"
copy_asset "sound/imported/mob/ghoul/death.ogg" "sounds/mob/ghoul/death.ogg"
copy_asset "sound/imported/mob/wight/say1.ogg" "sounds/mob/wight/say1.ogg"
copy_asset "sound/imported/mob/wight/say2.ogg" "sounds/mob/wight/say2.ogg"
copy_asset "sound/imported/mob/wight/hurt1.ogg" "sounds/mob/wight/hurt1.ogg"
copy_asset "sound/imported/mob/wight/hurt2.ogg" "sounds/mob/wight/hurt2.ogg"
copy_asset "sound/imported/mob/wight/death.ogg" "sounds/mob/wight/death.ogg"
copy_asset "sound/imported/mob/shadow/say1.ogg" "sounds/mob/shadow/say1.ogg"
copy_asset "sound/imported/mob/shadow/say2.ogg" "sounds/mob/shadow/say2.ogg"
copy_asset "sound/imported/mob/shadow/say3.ogg" "sounds/mob/shadow/say3.ogg"
copy_asset "sound/imported/mob/shadow/hurt1.ogg" "sounds/mob/shadow/hurt1.ogg"
copy_asset "sound/imported/mob/shadow/hurt2.ogg" "sounds/mob/shadow/hurt2.ogg"
copy_asset "sound/imported/mob/shadow/death1.ogg" "sounds/mob/shadow/death1.ogg"
copy_asset "sound/imported/mob/shadow/death2.ogg" "sounds/mob/shadow/death2.ogg"
copy_asset "sound/imported/mob/invisiblestalker/say1.ogg" "sounds/mob/invisiblestalker/say1.ogg"
copy_asset "sound/imported/mob/invisiblestalker/say2.ogg" "sounds/mob/invisiblestalker/say2.ogg"
copy_asset "sound/imported/mob/invisiblestalker/say3.ogg" "sounds/mob/invisiblestalker/say3.ogg"
copy_asset "sound/imported/mob/invisiblestalker/hurt1.ogg" "sounds/mob/invisiblestalker/hurt1.ogg"
copy_asset "sound/imported/mob/invisiblestalker/hurt2.ogg" "sounds/mob/invisiblestalker/hurt2.ogg"
copy_asset "sound/imported/mob/invisiblestalker/death.ogg" "sounds/mob/invisiblestalker/death.ogg"
copy_asset "sound/imported/mob/demonspider/say1.ogg" "sounds/mob/demonspider/say1.ogg"
copy_asset "sound/imported/mob/demonspider/say2.ogg" "sounds/mob/demonspider/say2.ogg"
copy_asset "sound/imported/mob/demonspider/say3.ogg" "sounds/mob/demonspider/say3.ogg"
copy_asset "sound/imported/mob/demonspider/hurt1.ogg" "sounds/mob/demonspider/hurt1.ogg"
copy_asset "sound/imported/mob/demonspider/hurt2.ogg" "sounds/mob/demonspider/hurt2.ogg"
copy_asset "sound/imported/mob/demonspider/death.ogg" "sounds/mob/demonspider/death.ogg"
copy_asset "sound/imported/mob/hellhound/say1.ogg" "sounds/mob/hellhound/say1.ogg"
copy_asset "sound/imported/mob/hellhound/say2.ogg" "sounds/mob/hellhound/say2.ogg"
copy_asset "sound/imported/mob/hellhound/say3.ogg" "sounds/mob/hellhound/say3.ogg"
copy_asset "sound/imported/mob/hellhound/hurt1.ogg" "sounds/mob/hellhound/hurt1.ogg"
copy_asset "sound/imported/mob/hellhound/hurt2.ogg" "sounds/mob/hellhound/hurt2.ogg"
copy_asset "sound/imported/mob/hellhound/death.ogg" "sounds/mob/hellhound/death.ogg"
copy_asset "sound/imported/mob/hellhound/breath.ogg" "sounds/mob/hellhound/breath.ogg"
copy_asset "sound/imported/mob/witch/cackle1.ogg" "sounds/mob/witch/cackle1.ogg"
copy_asset "sound/imported/mob/witch/cackle2.ogg" "sounds/mob/witch/cackle2.ogg"
copy_asset "sound/imported/mob/witch/cackle3.ogg" "sounds/mob/witch/cackle3.ogg"
copy_asset "sound/imported/mob/witch/hurt.ogg" "sounds/mob/witch/hurt.ogg"
copy_asset "sound/imported/mob/witch/death.ogg" "sounds/mob/witch/death.ogg"
copy_asset "sound/imported/random/sizzle.ogg" "sounds/random/sizzle.ogg"
copy_client_asset "sound/random/fizz.ogg" "sounds/random/fizz.ogg"

echo "Synced $(($(wc -l < "$manifest") - 1)) authorized R196 full-text assets."
