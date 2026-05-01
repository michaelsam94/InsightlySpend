#!/usr/bin/env python3
"""
Generate Play Console graphics and optional launcher mipmap PNGs.

Default (no --input): renders the same wallet + coin + tracker artwork as the
app adaptive icon (ic_launcher_*), on the matching blue diagonal gradient.

With --input: uses your PNG logo instead (legacy workflow).

Outputs (default: play-assets/generated/):
  - play_store_icon_512.png      — Store listing icon (512 × 512)
  - feature_graphic_1024x500.png — Feature graphic (1024 × 500)

Requires: pip install -r scripts/requirements.txt
"""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

try:
    from PIL import Image, ImageDraw
except ImportError:
    print("Install Pillow: pip install -r scripts/requirements.txt", file=sys.stderr)
    sys.exit(1)

# Matches app/src/main/res/drawable/ic_launcher_background.xml (approx.)
DEFAULT_PRIMARY = "#1E88E5"
DEFAULT_SECONDARY = "#0D47A1"

MIPMAP_SIZES: dict[str, int] = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}


def hex_to_rgb(h: str) -> tuple[int, int, int]:
    h = h.strip().lstrip("#")
    if len(h) != 6:
        raise ValueError(f"Expected #RRGGBB, got {h!r}")
    return (
        int(h[0:2], 16),
        int(h[2:4], 16),
        int(h[4:6], 16),
    )


def diagonal_gradient_rgba(
    size: tuple[int, int],
    c0: tuple[int, int, int],
    c1: tuple[int, int, int],
) -> Image.Image:
    """Same diagonal direction as VectorDrawable linear gradient start (0,0) → end (max,max)."""
    w, h = size
    img = Image.new("RGBA", (w, h))
    px = img.load()
    assert px is not None
    for yy in range(h):
        for xx in range(w):
            t = (xx / max(w - 1, 1) + yy / max(h - 1, 1)) / 2.0
            r = int(c0[0] + (c1[0] - c0[0]) * t)
            g = int(c0[1] + (c1[1] - c0[1]) * t)
            b = int(c0[2] + (c1[2] - c0[2]) * t)
            px[xx, yy] = (r, g, b, 255)
    return img


def render_brand_app_icon(
    size: int,
    primary: tuple[int, int, int],
    secondary: tuple[int, int, int],
) -> Image.Image:
    """
    Matches drawable/ic_launcher_foreground.xml layout in 108×108 viewport,
    scaled to `size`. Background uses the same gradient as ic_launcher_background.
    """
    img = diagonal_gradient_rgba((size, size), primary, secondary)
    draw = ImageDraw.Draw(img)
    s = size / 108.0

    wr = (30 * s, 42 * s, 78 * s, 78 * s)
    rr = max(1.0, 4.0 * s)
    draw.rounded_rectangle(wr, radius=rr, fill=(255, 255, 255, 255))

    flap = [(34 * s, 42 * s), (54 * s, 30 * s), (74 * s, 42 * s)]
    draw.polygon(flap, fill=(227, 242, 253, 255))

    lw = max(1, int(round(1.8 * s)))
    draw.line(
        [(38 * s, 54 * s), (70 * s, 54 * s)],
        fill=(144, 202, 249, 255),
        width=lw,
    )

    cx, cy = 69 * s, 50 * s
    ro = 9 * s
    ri = 4 * s
    ow = max(2, int(round(2.5 * s)))
    draw.ellipse(
        [cx - ro, cy - ro, cx + ro, cy + ro],
        outline=(255, 255, 255, 255),
        width=ow,
    )
    draw.ellipse(
        [cx - ri, cy - ri, cx + ri, cy + ri],
        fill=(255, 255, 255, 255),
    )

    tw = max(2, int(round(3.5 * s)))
    pts_flat = [
        34 * s,
        70 * s,
        46 * s,
        62 * s,
        56 * s,
        64 * s,
        68 * s,
        52 * s,
        82 * s,
        42 * s,
    ]
    draw.line(pts_flat, fill=(255, 255, 255, 255), width=tw, joint="curve")

    dr = 3.5 * s
    bx = 82 * s
    by = 42 * s
    draw.ellipse(
        [bx - dr, by - dr, bx + dr, by + dr],
        fill=(255, 255, 255, 255),
    )
    return img


def load_rgba(path: Path) -> Image.Image:
    img = Image.open(path).convert("RGBA")
    return img


def fit_center(canvas: Image.Image, icon: Image.Image) -> None:
    x = (canvas.width - icon.width) // 2
    y = (canvas.height - icon.height) // 2
    if icon.mode == "RGBA":
        canvas.paste(icon, (x, y), icon)
    else:
        canvas.paste(icon, (x, y))


def scale_to_square(icon: Image.Image, size: int) -> Image.Image:
    icon = icon.copy()
    icon.thumbnail((size, size), Image.Resampling.LANCZOS)
    canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    fit_center(canvas, icon)
    return canvas


def composite_on_white(rgba: Image.Image) -> Image.Image:
    bg = Image.new("RGB", rgba.size, (255, 255, 255))
    if rgba.mode == "RGBA":
        bg.paste(rgba, mask=rgba.split()[3])
    else:
        bg.paste(rgba)
    return bg


def make_feature_graphic(
    icon_rgba: Image.Image,
    primary: tuple[int, int, int],
    secondary: tuple[int, int, int],
) -> Image.Image:
    w, h = 1024, 500
    base = diagonal_gradient_rgba((w, h), primary, secondary).convert("RGB")

    max_logo_h = 360
    logo = icon_rgba.copy()
    if logo.height > max_logo_h:
        ratio = max_logo_h / logo.height
        logo = logo.resize(
            (max(1, int(logo.width * ratio)), max_logo_h),
            Image.Resampling.LANCZOS,
        )
    lx = (w - logo.width) // 2
    ly = (h - logo.height) // 2
    out = base.convert("RGBA")
    out.paste(logo, (lx, ly), logo)
    return out.convert("RGB")


def write_launcher_res_from_image(icon_rgba: Image.Image, project_root: Path) -> None:
    res = project_root / "app" / "src" / "main" / "res"
    for folder, px in MIPMAP_SIZES.items():
        out_dir = res / folder
        out_dir.mkdir(parents=True, exist_ok=True)
        sq = scale_to_square(icon_rgba, px)
        rgb = composite_on_white(sq)
        rgb.save(out_dir / "ic_launcher.png", format="PNG")
        rgb.save(out_dir / "ic_launcher_round.png", format="PNG")


def write_launcher_res_brand(
    primary: tuple[int, int, int],
    secondary: tuple[int, int, int],
    project_root: Path,
) -> None:
    res = project_root / "app" / "src" / "main" / "res"
    for folder, px in MIPMAP_SIZES.items():
        out_dir = res / folder
        out_dir.mkdir(parents=True, exist_ok=True)
        img = render_brand_app_icon(px, primary, secondary)
        rgb = composite_on_white(img)
        rgb.save(out_dir / "ic_launcher.png", format="PNG")
        rgb.save(out_dir / "ic_launcher_round.png", format="PNG")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--input",
        type=Path,
        default=None,
        help="Optional logo PNG. If omitted, renders built-in brand icon matching the app.",
    )
    parser.add_argument(
        "--out",
        type=Path,
        default=Path("play-assets/generated"),
        help="Output directory",
    )
    parser.add_argument(
        "--primary",
        default=DEFAULT_PRIMARY,
        help="Gradient start (#RRGGBB); default matches ic_launcher_background",
    )
    parser.add_argument(
        "--secondary",
        default=DEFAULT_SECONDARY,
        help="Gradient end (#RRGGBB)",
    )
    parser.add_argument(
        "--write-launcher-res",
        action="store_true",
        help="Also write app/src/main/res/mipmap-*/ic_launcher*.png (legacy fallback)",
    )
    parser.add_argument(
        "--project-root",
        type=Path,
        default=Path.cwd(),
        help="Project root when using --write-launcher-res",
    )
    args = parser.parse_args()

    primary = hex_to_rgb(args.primary)
    secondary = hex_to_rgb(args.secondary)

    if args.input is not None:
        if not args.input.is_file():
            print(f"Missing input file: {args.input}", file=sys.stderr)
            sys.exit(1)
        icon_512 = scale_to_square(load_rgba(args.input), 512)
        use_brand_mipmaps = False
    else:
        icon_512 = render_brand_app_icon(512, primary, secondary)
        use_brand_mipmaps = True

    args.out.mkdir(parents=True, exist_ok=True)
    icon_512.save(args.out / "play_store_icon_512.png", format="PNG")

    fg = make_feature_graphic(icon_512, primary, secondary)
    fg.save(args.out / "feature_graphic_1024x500.png", format="PNG")

    if args.write_launcher_res:
        root = args.project_root.resolve()
        if use_brand_mipmaps:
            write_launcher_res_brand(primary, secondary, root)
        else:
            write_launcher_res_from_image(load_rgba(args.input), root)

    print(f"Wrote:\n  {(args.out / 'play_store_icon_512.png').resolve()}\n  {(args.out / 'feature_graphic_1024x500.png').resolve()}")
    if args.input is None:
        print("(Built-in brand artwork — matches adaptive launcher icon.)")
    if args.write_launcher_res:
        print("Wrote legacy mipmap PNGs under app/src/main/res/mipmap-*/")


if __name__ == "__main__":
    main()
