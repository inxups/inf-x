#!/usr/bin/env python3
"""Sync MITE monster textures that differ from vanilla 26.2 into assets/infx.

Pixel audit against minecraft_26.2_client.jar (2026-07): the authorized MITE pack's
zombie/skeleton/creeper(+armor)/enderman(+eyes)/witch/spider_eyes are byte- or
pixel-identical to vanilla 26.2, so they are NOT synced; renderers keep vanilla ids.
What differs and is synced here:

- copies: spider (brighter red eye markings), blaze (bright rod pixels),
  ghast + ghast_shooting (different face art; 64x32 matches the model's declared UV
  size, vanilla 128x64 is just a 2x-res variant), zombie_pigman (humanoid-layout
  64x64 with rotten-flesh hat overlay; replaces the modern piglin-model look).
- expand64x64: ghoul/wight/shadow ship as legacy 64x32, but 26.2's zombie model
  declares a 64x64 texture, so the legacy files render squashed. 26.2's
  HumanoidModel mirrors left limbs from the right-limb UVs again, so the fix is
  just a transparent 64x64 canvas with the legacy art in the top half (same as the
  pack's own revenant/zombie_pigman upgrades).
- baby_uv: 26.2 renders baby zombies with BabyZombieModel, whose chibi UV layout
  matches zombie_baby.png, not the adult sheet. Derive baby sheets for the MITE
  humanoid skins by box-face nearest resampling from the adult layout.
"""

from __future__ import annotations

import hashlib
import struct
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MITE = Path(
    "/Users/inxups/IdeaProjects/mc/inf-x/codex/reference/mite-resource-pack/assets/minecraft/textures/entity"
)
OUT = ROOT / "src/main/resources/assets/infx/textures/entity"
MANIFEST = ROOT / "src/main/resources/assets/infx/mite_texture_manifest.tsv"


def decode_rgba(path: Path):
    data = path.read_bytes()
    assert data[:8] == b"\x89PNG\r\n\x1a\n", path
    i = 8
    w = h = bd = ct = None
    plte = trns = None
    idat = b""
    while i < len(data):
        ln = struct.unpack(">I", data[i : i + 4])[0]
        i += 4
        typ = data[i : i + 4]
        i += 4
        chunk = data[i : i + ln]
        i += ln + 4
        if typ == b"IHDR":
            w, h, bd, ct = struct.unpack(">IIBB", chunk[:10])
        elif typ == b"PLTE":
            plte = chunk
        elif typ == b"tRNS":
            trns = chunk
        elif typ == b"IDAT":
            idat += chunk
        elif typ == b"IEND":
            break
    if bd not in (4, 8):
        raise SystemExit(f"unsupported bit depth {bd} for {path}")
    raw = zlib.decompress(idat)
    if ct == 3:
        bpp = 1
        row_bytes = (w * bd + 7) // 8
    else:
        if bd != 8:
            raise SystemExit(f"unsupported bit depth {bd} color type {ct} for {path}")
        channels = {0: 1, 2: 3, 4: 2, 6: 4}[ct]
        bpp = channels
        row_bytes = w * channels
    prev = bytearray(row_bytes)
    pos = 0
    rows = []

    def paeth(a, b, c):
        p = a + b - c
        pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
        if pa <= pb and pa <= pc:
            return a
        return b if pb <= pc else c

    for _ in range(h):
        f = raw[pos]
        pos += 1
        row = bytearray(raw[pos : pos + row_bytes])
        pos += row_bytes
        out = bytearray(row_bytes)
        for x in range(row_bytes):
            left = out[x - bpp] if x >= bpp else 0
            up = prev[x]
            upleft = prev[x - bpp] if x >= bpp else 0
            v = row[x]
            if f == 0:
                out[x] = v
            elif f == 1:
                out[x] = (v + left) & 255
            elif f == 2:
                out[x] = (v + up) & 255
            elif f == 3:
                out[x] = (v + (left + up) // 2) & 255
            elif f == 4:
                out[x] = (v + paeth(left, up, upleft)) & 255
            else:
                raise SystemExit(f"filter {f} in {path}")
        rows.append(out)
        prev = out
    pixels = []
    for row in rows:
        if ct == 3 and bd == 4:
            for x in range(w):
                byte = row[x // 2]
                idx = (byte >> 4) if (x % 2 == 0) else (byte & 0x0F)
                r, g, b = plte[idx * 3 : idx * 3 + 3]
                a = trns[idx] if trns and idx < len(trns) else 255
                pixels.append((r, g, b, a))
            continue
        for x in range(w):
            if ct == 6:
                r, g, b, a = row[x * 4 : x * 4 + 4]
            elif ct == 2:
                r, g, b = row[x * 3 : x * 3 + 3]
                a = 255
            elif ct == 3:
                idx = row[x]
                r, g, b = plte[idx * 3 : idx * 3 + 3]
                a = trns[idx] if trns and idx < len(trns) else 255
            elif ct == 4:
                r = g = b = row[x * 2]
                a = row[x * 2 + 1]
            else:
                r = g = b = row[x]
                a = 255
            pixels.append((r, g, b, a))
    return w, h, pixels


def write_png_rgba(path: Path, w: int, h: int, pixels):
    raw = bytearray()
    for y in range(h):
        raw.append(0)
        for x in range(w):
            r, g, b, a = pixels[y * w + x]
            raw.extend((r, g, b, a))
    comp = zlib.compress(bytes(raw), 9)

    def chunk(t: bytes, d: bytes) -> bytes:
        return struct.pack(">I", len(d)) + t + d + struct.pack(">I", zlib.crc32(t + d) & 0xFFFFFFFF)

    ihdr = struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", ihdr) + chunk(b"IDAT", comp) + chunk(b"IEND", b""))


TRANSPARENT = (0, 0, 0, 0)


def expand_to_64x64(src: Path):
    w, h, px = decode_rgba(src)
    if (w, h) != (64, 32):
        raise SystemExit(f"{src} expected 64x32, got {w}x{h}")
    out = [TRANSPARENT] * (64 * 64)
    for y in range(32):
        for x in range(64):
            out[y * 64 + x] = px[y * 64 + x]
    return out


# Box-face UV rects: box at (u,v) with size (w,h,d) lays out
# up/down on the top strip and east/north/west/south below it.
def face_rect(u, v, w, h, d, face):
    return {
        "up": (u + d, v, w, d),
        "down": (u + d + w, v, w, d),
        "east": (u, v + d, d, h),
        "north": (u + d, v + d, w, h),
        "west": (u + d + w, v + d, d, h),
        "south": (u + d + w + d, v + d, w, h),
    }[face]


def resample_face(px, x0, y0, sw, sh, dw, dh, flip):
    out = []
    for y in range(dh):
        sy = y0 + (y * sh) // dh
        row = []
        for x in range(dw):
            sx = x0 + (x * sw) // dw
            row.append(px[sy * 64 + sx])
        if flip:
            row.reverse()
        out.append(row)
    return out


# BabyZombieModel UV layout (26.2): chibi boxes sharing one 64x64 sheet.
# (adult_box, baby_box, mirrored) — mirrored parts sample the right-limb box
# with east/west swapped and U flipped, matching CubeListBuilder.mirror().
BABY_PARTS = [
    ((0, 0, 8, 8, 8), (3, 3, 6, 6, 6), False),  # head
    ((32, 0, 8, 8, 8), (35, 3, 6, 6, 6), False),  # hat overlay
    ((16, 16, 8, 12, 4), (16, 16, 4, 5, 2), False),  # body
    ((40, 16, 4, 12, 4), (36, 16, 2, 5, 2), False),  # right arm
    ((40, 16, 4, 12, 4), (28, 16, 2, 5, 2), True),  # left arm
    ((0, 16, 4, 12, 4), (8, 16, 2, 4, 2), False),  # right leg
    ((0, 16, 4, 12, 4), (0, 16, 2, 4, 2), True),  # left leg
]
MIRROR_FACE = {"east": "west", "west": "east"}


def derive_baby(adult_px):
    out = [TRANSPARENT] * (64 * 64)
    for (au, av, aw, ah, ad), (bu, bv, bw, bh, bd), mirrored in BABY_PARTS:
        for face in ("up", "down", "east", "north", "west", "south"):
            src_face = MIRROR_FACE.get(face, face) if mirrored else face
            sx, sy, sw, sh = face_rect(au, av, aw, ah, ad, src_face)
            dx, dy, dw, dh = face_rect(bu, bv, bw, bh, bd, face)
            rows = resample_face(adult_px, sx, sy, sw, sh, dw, dh, mirrored)
            for y in range(dh):
                for x in range(dw):
                    out[(dy + y) * 64 + (dx + x)] = rows[y][x]
    return out


COPIES = [
    ("entity/spider/spider.png", "textures/entity/spider/spider.png"),
    ("entity/blaze.png", "textures/entity/blaze.png"),
    ("entity/ghast/ghast.png", "textures/entity/ghast/ghast.png"),
    ("entity/ghast/ghast_shooting.png", "textures/entity/ghast/ghast_shooting.png"),
    ("entity/zombie_pigman.png", "textures/entity/zombie_pigman.png"),
]

EXPAND = [
    ("entity/ghoul.png", "textures/entity/ghoul.png"),
    ("entity/wight.png", "textures/entity/wight.png"),
    ("entity/shadow.png", "textures/entity/shadow.png"),
]

# adult sheet (post-expansion for legacy ones) -> baby destination
BABIES = [
    ("entity/ghoul.png", "textures/entity/ghoul.png", "textures/entity/ghoul_baby.png"),
    ("entity/wight.png", "textures/entity/wight.png", "textures/entity/wight_baby.png"),
    ("entity/shadow.png", "textures/entity/shadow.png", "textures/entity/shadow_baby.png"),
    ("entity/zombie/revenant.png", None, "textures/entity/zombie/revenant_baby.png"),
    ("entity/zombie_pigman.png", None, "textures/entity/zombie_pigman_baby.png"),
]


def main() -> None:
    entries = []

    for source_rel, dest_rel in COPIES:
        src = MITE.parent / source_rel
        if not src.is_file():
            raise SystemExit(f"missing authorized asset {src}")
        dest = ROOT / "src/main/resources/assets/infx" / dest_rel
        dest.parent.mkdir(parents=True, exist_ok=True)
        dest.write_bytes(src.read_bytes())
        entries.append(("resource-pack", source_rel, dest_rel, sha256(dest)))
        print("copied", dest_rel)

    for source_rel, dest_rel in EXPAND:
        src = MITE.parent / source_rel
        px = expand_to_64x64(src)
        dest = ROOT / "src/main/resources/assets/infx" / dest_rel
        write_png_rgba(dest, 64, 64, px)
        entries.append(("derived", f"mite/{source_rel}+expand64x64", dest_rel, sha256(dest)))
        print("expanded", dest_rel)

    for source_rel, adult_dest, baby_dest in BABIES:
        if adult_dest is None:
            w, h, adult_px = decode_rgba(MITE.parent / source_rel)
            if (w, h) != (64, 64):
                raise SystemExit(f"{source_rel} expected 64x64 adult sheet, got {w}x{h}")
        else:
            w, h, adult_px = decode_rgba(ROOT / "src/main/resources/assets/infx" / adult_dest)
            assert (w, h) == (64, 64), adult_dest
        dest = ROOT / "src/main/resources/assets/infx" / baby_dest
        write_png_rgba(dest, 64, 64, derive_baby(adult_px))
        entries.append(("derived", f"mite/{source_rel}+baby_uv", baby_dest, sha256(dest)))
        print("derived baby", baby_dest)

    lines = MANIFEST.read_text().splitlines()
    header, body = lines[0], lines[1:]
    replaced = {dest for _, _, dest, _ in entries}
    body = [line for line in body if line.split("\t")[2] not in replaced]
    for source_root, source, dest, digest in entries:
        body.append(f"{source_root}\t{source}\t{dest}\t{digest}")
    body.sort(key=lambda line: line.split("\t")[2])
    MANIFEST.write_text(header + "\n" + "\n".join(body) + "\n")
    print("manifest lines", len(body) + 1)


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


if __name__ == "__main__":
    main()
