#!/usr/bin/env python3
"""Derive 26.2-layout sick livestock skins from MITE sick palette mapping.

MITE sick.png is 64x32 (legacy UV). 26.2 cow/pig models use 64x64 with temperate/warm/cold
variants. This script fits a per-channel linear map healthy->sick from MITE, then applies it
to each 26.2 healthy texture so UV layout matches the modern models.
"""

from __future__ import annotations

import hashlib
import os
import struct
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
REFERENCE_ROOT = Path(os.environ.get("INFX_REFERENCE_ROOT", ROOT / "codex/reference"))
if not REFERENCE_ROOT.is_dir():
    REFERENCE_ROOT = Path("/Users/inxups/IdeaProjects/mc/inf-x/codex/reference")
MITE = REFERENCE_ROOT / "mite-resource-pack/assets/minecraft/textures/entity"
SRC_26 = Path(os.environ.get(
    "INFX_26_CLIENT_JAR",
    Path.home() / ".gradle/caches/neoformruntime/artifacts/minecraft_26.2_client.jar"))
TMP_26_ROOT = ROOT / "build/tmp/mite-26.2-textures"
TMP_26 = TMP_26_ROOT / "assets/minecraft/textures/entity"
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
    # Bytes per pixel for filter reconstruction (sub-byte depths still filter on packed bytes).
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


def fit_linear(healthy, sick):
    coeffs = []
    for c in range(3):
        n = sx = sy = sxx = sxy = 0
        for (hr, hg, hb, ha), (sr, sg, sb, sa) in zip(healthy, sick):
            if ha < 16 or sa < 16:
                continue
            x = (hr, hg, hb)[c]
            y = (sr, sg, sb)[c]
            n += 1
            sx += x
            sy += y
            sxx += x * x
            sxy += x * y
        if n == 0:
            coeffs.append((1.0, 0.0))
            continue
        den = n * sxx - sx * sx
        if abs(den) < 1e-6:
            a = 1.0
            b = (sy / n) - (sx / n)
        else:
            a = (n * sxy - sx * sy) / den
            b = (sy - a * sx) / n
        coeffs.append((a, b))
    return coeffs


def apply_map(pixels, coeffs):
    out = []
    for r, g, b, a in pixels:
        if a < 16:
            out.append((r, g, b, a))
            continue
        nr = max(0, min(255, int(round(coeffs[0][0] * r + coeffs[0][1]))))
        ng = max(0, min(255, int(round(coeffs[1][0] * g + coeffs[1][1]))))
        nb = max(0, min(255, int(round(coeffs[2][0] * b + coeffs[2][1]))))
        out.append((nr, ng, nb, a))
    return out


def ensure_26_sources():
    if (TMP_26 / "cow/cow_temperate.png").exists():
        return
    import subprocess

    if not SRC_26.is_file():
        raise SystemExit(f"missing Minecraft 26.2 client asset jar {SRC_26}")
    TMP_26_ROOT.mkdir(parents=True, exist_ok=True)
    subprocess.check_call(
        ["jar", "xf", str(SRC_26)]
        + [
            f"assets/minecraft/textures/entity/{animal}/{name}"
            for animal, names in {
                "cow": [
                    "cow_temperate.png",
                    "cow_warm.png",
                    "cow_cold.png",
                    "cow_temperate_baby.png",
                    "cow_warm_baby.png",
                    "cow_cold_baby.png",
                ],
                "pig": [
                    "pig_temperate.png",
                    "pig_warm.png",
                    "pig_cold.png",
                    "pig_temperate_baby.png",
                    "pig_warm_baby.png",
                    "pig_cold_baby.png",
                ],
                "chicken": [
                    "chicken_temperate.png",
                    "chicken_warm.png",
                    "chicken_cold.png",
                    "chicken_temperate_baby.png",
                    "chicken_warm_baby.png",
                    "chicken_cold_baby.png",
                ],
                "sheep": ["sheep.png", "sheep_baby.png"],
            }.items()
            for name in names
        ],
        cwd=str(TMP_26_ROOT),
    )


PAIRS = {
    "cow": (
        "cow.png",
        [
            "cow_temperate.png",
            "cow_warm.png",
            "cow_cold.png",
            "cow_temperate_baby.png",
            "cow_warm_baby.png",
            "cow_cold_baby.png",
        ],
    ),
    "pig": (
        "pig.png",
        [
            "pig_temperate.png",
            "pig_warm.png",
            "pig_cold.png",
            "pig_temperate_baby.png",
            "pig_warm_baby.png",
            "pig_cold_baby.png",
        ],
    ),
    "chicken": (
        "chicken.png",
        [
            "chicken_temperate.png",
            "chicken_warm.png",
            "chicken_cold.png",
            "chicken_temperate_baby.png",
            "chicken_warm_baby.png",
            "chicken_cold_baby.png",
        ],
    ),
    "sheep": ("sheep.png", ["sheep.png", "sheep_baby.png"]),
}


def out_name(animal: str, source_name: str) -> str:
    if animal == "sheep":
        return "sheep_sick_baby.png" if "baby" in source_name else "sheep_sick.png"
    stem = source_name[:-4]
    if stem.endswith("_baby"):
        return f"{stem[:-5]}_sick_baby.png"
    return f"{stem}_sick.png"


def main() -> None:
    ensure_26_sources()
    entries = []
    for animal, (healthy_name, targets) in PAIRS.items():
        _, _, healthy_px = decode_rgba(MITE / animal / healthy_name)
        _, _, sick_px = decode_rgba(MITE / animal / "sick.png")
        coeffs = fit_linear(healthy_px, sick_px)
        print(animal, "coeffs", coeffs)
        # Remove legacy 64x32 sick.png so it cannot be selected by mistake.
        legacy = OUT / animal / "sick.png"
        if legacy.exists():
            legacy.unlink()
        for target in targets:
            src = TMP_26 / animal / target
            if not src.exists():
                raise SystemExit(f"missing 26.2 texture {src}")
            w, h, px = decode_rgba(src)
            dest = OUT / animal / out_name(animal, target)
            write_png_rgba(dest, w, h, apply_map(px, coeffs))
            digest = hashlib.sha256(dest.read_bytes()).hexdigest()
            rel = f"textures/entity/{animal}/{dest.name}"
            source = f"26.2/{animal}/{target}+mite/{animal}/sick.png"
            entries.append((source, rel, digest))
            print("wrote", rel, w, h, digest[:12])

    lines = MANIFEST.read_text().splitlines()
    header, body = lines[0], lines[1:]
    # drop old single sick destinations
    drop = {
        "textures/entity/cow/sick.png",
        "textures/entity/pig/sick.png",
        "textures/entity/sheep/sick.png",
        "textures/entity/chicken/sick.png",
    }
    replaced = {rel for _, rel, _ in entries}
    body = [line for line in body if line.split("\t")[2] not in drop | replaced]
    for source, rel, digest in entries:
        body.append(f"derived\t{source}\t{rel}\t{digest}")
    body.sort(key=lambda line: line.split("\t")[2])
    MANIFEST.write_text(header + "\n" + "\n".join(body) + "\n")
    print("manifest lines", len(body) + 1)


if __name__ == "__main__":
    main()
