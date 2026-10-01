"""
Generates the mod's emoji data from two free sources:

- names, categories and keywords from gemoji (GitHub, MIT license)
  https://github.com/github/gemoji
- images from Twemoji (jdecked continuation, graphics CC-BY 4.0)
  https://github.com/jdecked/twemoji

Output (all under src/main/resources/assets/oviemoji/):

- textures/font/emoji.png  an atlas with all emojis, 32 x 32 pixels per cell
- font/emoji.json          the bitmap font that maps each emoji to a character from the
                           Unicode private use area starting at U+E000
- emoji.tsv                the list the mod reads on startup

Downloaded images go to tools/cache/ and are reused on the next run.
Usage:  python tools/build_emoji.py
"""

import concurrent.futures
import json
import pathlib
import sys
import urllib.request

from PIL import Image

ROOT = pathlib.Path(__file__).resolve().parent.parent
CACHE = ROOT / "tools" / "cache"
ASSETS = ROOT / "src" / "main" / "resources" / "assets" / "oviemoji"

GEMOJI_URL = "https://raw.githubusercontent.com/github/gemoji/master/db/emoji.json"
TWEMOJI_VERSION = "17.0.3"
TWEMOJI_URL = "https://cdn.jsdelivr.net/gh/jdecked/twemoji@" + TWEMOJI_VERSION + "/assets/72x72/{}.png"

CELL = 32            # pixels per emoji in the atlas
COLUMNS = 64         # emojis per atlas row
FIRST_CODEPOINT = 0xE000

# Short name per gemoji category, as the mod knows it for the picker tabs
CATEGORIES = {
    "Smileys & Emotion": "smileys",
    "People & Body": "people",
    "Animals & Nature": "nature",
    "Food & Drink": "food",
    "Travel & Places": "travel",
    "Activities": "activities",
    "Objects": "objects",
    "Symbols": "symbols",
    "Flags": "flags",
}


def fetch(url: str) -> bytes | None:
    try:
        with urllib.request.urlopen(url, timeout=30) as response:
            return response.read()
    except urllib.error.HTTPError as error:
        if error.code == 404:
            return None
        raise


def twemoji_names(emoji: str) -> list[str]:
    """Twemoji names files after the code points; without ZWJ, U+FE0F is dropped."""
    points = [f"{ord(c):x}" for c in emoji]
    full = "-".join(points)
    stripped = "-".join(p for p in points if p != "fe0f")
    return [stripped, full] if "200d" not in points else [full, stripped]


def load_image(emoji: str) -> Image.Image | None:
    CACHE.mkdir(parents=True, exist_ok=True)
    for name in dict.fromkeys(twemoji_names(emoji)):
        cached = CACHE / f"{name}.png"
        if not cached.exists():
            data = fetch(TWEMOJI_URL.format(name))
            if data is None:
                continue
            cached.write_bytes(data)
        return Image.open(cached).convert("RGBA")
    return None


def main() -> int:
    gemoji_cache = CACHE / "gemoji.json"
    CACHE.mkdir(parents=True, exist_ok=True)
    if not gemoji_cache.exists():
        gemoji_cache.write_bytes(fetch(GEMOJI_URL))
    entries = json.loads(gemoji_cache.read_text(encoding="utf-8"))

    with concurrent.futures.ThreadPoolExecutor(max_workers=16) as pool:
        images = list(pool.map(lambda e: load_image(e["emoji"]), entries))

    kept = [(e, img) for e, img in zip(entries, images) if img is not None]
    missing = [e["aliases"][0] for e, img in zip(entries, images) if img is None]
    if missing:
        print(f"No Twemoji image, skipped: {', '.join(missing)}")

    rows = (len(kept) + COLUMNS - 1) // COLUMNS
    atlas = Image.new("RGBA", (COLUMNS * CELL, rows * CELL), (0, 0, 0, 0))
    lines = ["# codepoint\tunicode\tcategory\tshortnames\tkeywords"]
    chars = []
    for index, (entry, image) in enumerate(kept):
        glyph = image.resize((CELL, CELL), Image.Resampling.LANCZOS)
        atlas.paste(glyph, ((index % COLUMNS) * CELL, (index // COLUMNS) * CELL))
        codepoint = FIRST_CODEPOINT + index
        chars.append(chr(codepoint))
        unicode_hex = " ".join(f"{ord(c):X}" for c in entry["emoji"])
        lines.append("\t".join([
            f"{codepoint:X}",
            unicode_hex,
            CATEGORIES[entry["category"]],
            ",".join(entry["aliases"]),
            ",".join(entry["tags"]),
        ]))

    # Fill empty cells of the last row with U+0000, which the font skips
    chars += ["\u0000"] * (rows * COLUMNS - len(chars))
    font = {"providers": [{
        "type": "bitmap",
        "file": "oviemoji:font/emoji.png",
        "height": 8,
        "ascent": 7,
        "chars": ["".join(chars[r * COLUMNS:(r + 1) * COLUMNS]) for r in range(rows)],
    }]}

    (ASSETS / "textures" / "font").mkdir(parents=True, exist_ok=True)
    (ASSETS / "font").mkdir(parents=True, exist_ok=True)
    atlas.save(ASSETS / "textures" / "font" / "emoji.png", optimize=True)
    (ASSETS / "font" / "emoji.json").write_text(json.dumps(font, indent=2), encoding="utf-8")
    (ASSETS / "emoji.tsv").write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"{len(kept)} emojis, atlas {atlas.width} x {atlas.height}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
