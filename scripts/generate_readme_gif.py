#!/usr/bin/env python3
"""
Build docs/app-overview.gif from play-assets/screenshots (01–06 PNGs).

Requires: pip install Pillow (see scripts/requirements.txt)

Run after updating screenshots:
  python3 scripts/generate_readme_gif.py
"""

from __future__ import annotations

import sys
from pathlib import Path

try:
    from PIL import Image
except ImportError:
    print("Install Pillow: pip install -r scripts/requirements.txt", file=sys.stderr)
    sys.exit(1)

ROOT = Path(__file__).resolve().parents[1]
SCREEN_DIR = ROOT / "play-assets" / "screenshots"
OUT_PATH = ROOT / "docs" / "app-overview.gif"

FRAMES = [
    "01_home.png",
    "02_ledger.png",
    "03_insights.png",
    "04_budget.png",
    "05_vault.png",
    "06_settings.png",
]

# Readable on GitHub README without enormous file size
MAX_WIDTH = 420
FRAME_MS = 2200
PAD_COLOR = (248, 250, 252)


def resize_cap(im: Image.Image) -> Image.Image:
    im = im.convert("RGB")
    w, h = im.size
    if w <= MAX_WIDTH:
        return im
    nh = max(1, int(h * MAX_WIDTH / w))
    return im.resize((MAX_WIDTH, nh), Image.Resampling.LANCZOS)


def pad_to_canvas(im: Image.Image, cw: int, ch: int) -> Image.Image:
    canvas = Image.new("RGB", (cw, ch), PAD_COLOR)
    x = (cw - im.width) // 2
    y = (ch - im.height) // 2
    canvas.paste(im, (x, y))
    return canvas


def main() -> None:
    resized: list[Image.Image] = []
    for name in FRAMES:
        path = SCREEN_DIR / name
        if not path.is_file():
            print(f"Missing screenshot: {path}", file=sys.stderr)
            sys.exit(1)
        resized.append(resize_cap(Image.open(path)))

    cw = max(f.width for f in resized)
    ch = max(f.height for f in resized)
    frames = [pad_to_canvas(f, cw, ch) for f in resized]

    OUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    frames[0].save(
        OUT_PATH,
        save_all=True,
        append_images=frames[1:],
        duration=FRAME_MS,
        loop=0,
        optimize=True,
    )
    kb = OUT_PATH.stat().st_size / 1024
    print(f"Wrote {OUT_PATH} ({len(frames)} frames, ~{kb:.0f} KiB)")


if __name__ == "__main__":
    main()
