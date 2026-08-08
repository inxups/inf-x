#!/usr/bin/env python3
"""Derive per-wood flint workbench top textures.

Composition (16x16):
- base: vanilla stripped_<wood>_log_top / stripped_<wood>_stem_top (the stripped log end
  grain), so the top reads as a stripped log block viewed from above
- overlay: owner-provided flint_workbench_top.png pattern (bench frame with highlight
  gradient + flint knife) composited over the base; transparent pixels keep the wood

Outputs textures/block/flint_workbench_top_<wood>.png for every stripped-log workbench
wood and refreshes their manifest rows (source_root=derived).
"""

from __future__ import annotations

import hashlib
import os
import struct
import zipfile
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CLIENT_JAR = Path(os.environ.get(
    "INFX_26_CLIENT_JAR",
    Path.home() / ".gradle/caches/neoformruntime/artifacts/minecraft_26.1.2_client.jar"))
BLOCK_DIR = ROOT / "src/main/resources/assets/infx/textures/block"
ITEM_DIR = ROOT / "src/main/resources/assets/infx/textures/item"
MANIFEST = ROOT / "src/main/resources/assets/infx/infx_texture_manifest.tsv"
# 项目所有者提供的台面图案(透明背景:立体框 + 燧石小刀)
USER_TOP = Path(os.environ.get(
    "INFX_USER_FLINT_TOP",
    "/Users/inxups/Downloads/原木工作台/flint_workbench_top.png"))

WOODS = ["oak", "spruce", "birch", "jungle", "acacia", "cherry", "pale_oak", "dark_oak",
         "mangrove", "crimson", "warped"]
def decode_png(path: Path):
    data = path.read_bytes()
    pos = 8
    idat = b""
    w = h = bd = ct = None
    pal = []
    while pos < len(data):
        length = struct.unpack(">I", data[pos:pos + 4])[0]
        ctype = data[pos + 4:pos + 8]
        chunk = data[pos + 8:pos + 8 + length]
        if ctype == b"IHDR":
            w, h, bd, ct = struct.unpack(">IIBB", chunk[:10])
        elif ctype == b"PLTE":
            pal = [tuple(chunk[i:i + 3]) for i in range(0, len(chunk), 3)]
        elif ctype == b"IDAT":
            idat += chunk
        pos += 12 + length
    raw = zlib.decompress(idat)
    bytespp = 4 if ct == 6 else (3 if ct == 2 else 1)
    stride = (w * bytespp * bd + 7) // 8
    rows = []
    prev = bytearray(stride)
    for y in range(h):
        f = raw[y * (stride + 1)]
        line = bytearray(raw[y * (stride + 1) + 1:(y + 1) * (stride + 1)])
        if f == 1:
            for i in range(1, stride):
                line[i] = (line[i] + line[i - 1]) & 255
        elif f == 2:
            for i in range(stride):
                line[i] = (line[i] + prev[i]) & 255
        elif f == 3:
            for i in range(stride):
                a = line[i - 1] if i >= 1 else 0
                b = prev[i]
                c = prev[i - 1] if i >= 1 else 0
                line[i] = (line[i] + ((a + b) // 2)) & 255
        elif f == 4:
            for i in range(stride):
                a = line[i - 1] if i >= 1 else 0
                b = prev[i]
                c = prev[i - 1] if i >= 1 else 0
                p = a + b - c
                pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
                pr = a if (pa <= pb and pa <= pc) else (b if pb <= pc else c)
                line[i] = (line[i] + pr) & 255
        prev = line
        rows.append(line)
    return w, h, bd, ct, pal, rows


def raw_px(rows, x, y, bd, ct, pal):
    if ct == 6:
        p = tuple(rows[y][x * 4:(x + 1) * 4])
        return p[:3] if p[3] > 128 else None
    if ct == 2:
        return tuple(rows[y][x * 3:(x + 1) * 3])
    if bd == 8:
        idx = rows[y][x]
    else:  # bitdepth 4, two palette indexes per byte
        b = rows[y][x // 2]
        idx = (b >> 4) & 0xF if x % 2 == 0 else b & 0xF
    return pal[idx] if idx < len(pal) else (255, 0, 255)


def read_vanilla_grid(path_in_jar: str):
    with zipfile.ZipFile(CLIENT_JAR) as z:
        tmp = ROOT / "build/tmp/flint-workbench-top.png"
        tmp.parent.mkdir(parents=True, exist_ok=True)
        tmp.write_bytes(z.read(path_in_jar))
    w, h, bd, ct, pal, rows = decode_png(tmp)
    return [[raw_px(rows, x, y, bd, ct, pal) for x in range(w)] for y in range(h)]


def compose(base, wood):
    img = [row[:] for row in base]
    for (x, y), c in overlay.items():  # 用户图案:不透明像素覆盖原木
        img[y][x] = c
    return img


def encode_png(img, path: Path):
    raw = b""
    for y in range(16):
        raw += b"\x00" + b"".join(bytes((*img[y][x], 255)) for x in range(16))

    def chunk(t, d):
        return struct.pack(">I", len(d)) + t + d + struct.pack(">I", zlib.crc32(t + d) & 0xFFFFFFFF)

    ihdr = struct.pack(">IIBBBBB", 16, 16, 8, 6, 0, 0, 0)
    path.write_bytes(b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", ihdr)
                     + chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b""))


# Overlay pixels from the owner-provided pattern (transparent pixels keep the wood base).
# A single #f606fb placeholder pixel (editor "unfinished" marker) is replaced with its
# left neighbour so the frame gradient stays continuous.
w, h, bd, ct, pal, rows = decode_png(USER_TOP)
overlay = {}
for y in range(h):
    for x in range(w):
        p = raw_px(rows, x, y, bd, ct, pal)
        if p is not None:
            if p[:3] == (0xF6, 0x06, 0xFB):
                left = raw_px(rows, x - 1, y, bd, ct, pal)
                p = left if left is not None else (0xD4, 0xD4, 0xD4)
            overlay[(x, y)] = p

for wood in WOODS:
    stem = "stem" if wood in ("crimson", "warped") else "log"
    base = read_vanilla_grid(f"assets/minecraft/textures/block/stripped_{wood}_{stem}_top.png")
    out = BLOCK_DIR / f"flint_workbench_top_{wood}.png"
    encode_png(compose(base, wood), out)
    print("wrote", out.name)

# Refresh manifest rows (source_root=derived, sorted by destination).
rows_manifest = []
for line in MANIFEST.read_text().splitlines():
    if "\ttextures/block/flint_workbench_top_" in line:
        continue  # stale derived row, replaced below
    rows_manifest.append(line)
new_rows = []
for wood in WOODS:
    stem = "stem" if wood in ("crimson", "warped") else "log"
    dest = f"textures/block/flint_workbench_top_{wood}.png"
    digest = hashlib.sha256((BLOCK_DIR / dest.rsplit("/", 1)[-1]).read_bytes()).hexdigest()
    source = (f"owner-provided:/Users/inxups/Downloads/原木工作台/flint_workbench_top.png"
              f" + vanilla:assets/minecraft/textures/block/stripped_{wood}_{stem}_top.png(composed)")
    new_rows.append(f"derived\t{source}\t{dest}\t{digest}")
header, *body = rows_manifest
inserted = False
out = [header]
for line in body:
    fields = line.split("\t")
    if len(fields) >= 3 and not inserted and fields[2] >= new_rows[0].split("\t")[2]:
        out.extend(new_rows)
        inserted = True
    out.append(line)
if not inserted:
    out.extend(new_rows)
MANIFEST.write_text("\n".join(out) + "\n")
print("manifest updated:", len(out), "lines")
