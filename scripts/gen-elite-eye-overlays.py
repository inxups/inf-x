#!/usr/bin/env python3
"""Generate emissive eye-overlay textures for the three elite mobs.

The body sheets already paint red eyes (pixels ae1a1a / f40000) but on the lit
body layer, so they don't glow. These RGBA overlays place bright red at the
exact same UVs (guaranteeing alignment) on a transparent canvas; the renderer
draws them with RenderTypes.eyes() (fullbright), so the eyes glow permanently.

Eye UVs were located by decoding each body sheet and reading the existing red
eye pixels (see scripts/_inspect_eye_uv.py for the dump that pinned them down).
All three mobs are adults in practice (revenant forces setBaby(false); hellhound
breeding yields DIRE_WOLF; skeletons never spawn as babies), so only the adult UV
layout is needed.
"""

from __future__ import annotations

import struct
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
TEX = ROOT / "src/main/resources/assets/infx/textures/entity"

# (texture rel path, width, height, [(x, y), ...]) eye pixels for the ADULT sheet.
EYES = {
    "hellhound/hellhound_eyes.png": (64, 32, [(4, 6), (5, 6), (8, 6), (9, 6)]),
    "zombie/revenant_eyes.png": (64, 64, [(9, 12), (10, 12), (13, 12), (14, 12)]),
    "skeleton/bone_lord_eyes.png": (64, 32, [(9, 12), (10, 12), (13, 12), (14, 12)]),
}
# body texture for each overlay, used to sanity-check that the eye pixels are red-ish
BODY = {
    "hellhound/hellhound_eyes.png": "hellhound/hellhound.png",
    "zombie/revenant_eyes.png": "zombie/revenant.png",
    "skeleton/bone_lord_eyes.png": "skeleton/bone_lord.png",
}
EYE_COLOR = (255, 0, 0, 255)  # pure red, fullbright via RenderTypes.eyes()
TRANSPARENT = (0, 0, 0, 0)


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


def main() -> None:
    for rel, (w, h, eye_px) in EYES.items():
        body = TEX / BODY[rel]
        bw, bh, bpx = decode_rgba(body)
        assert (bw, bh) == (w, h), f"{body.name} is {bw}x{bh}, expected {w}x{h}"
        # sanity: every eye pixel on the body sheet should already be red-ish
        for x, y in eye_px:
            r, g, b, a = bpx[y * w + x]
            assert a != 0 and r > g + 20 and r > b + 20, (
                f"{body.name} eye pixel ({x},{y}) isn't red-ish: #{r:02x}{g:02x}{b:02x} a={a}"
            )
        out = [TRANSPARENT] * (w * h)
        for x, y in eye_px:
            out[y * w + x] = EYE_COLOR
        dest = TEX / rel
        write_png_rgba(dest, w, h, out)
        print("wrote", dest.relative_to(ROOT), f"({len(eye_px)} eye pixels)")


if __name__ == "__main__":
    main()
