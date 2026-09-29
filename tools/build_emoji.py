"""
Erzeugt die Emoji-Daten der Mod aus zwei freien Quellen:

- Namen, Kategorien und Stichwoerter aus gemoji (GitHub, MIT-Lizenz)
  https://github.com/github/gemoji
- Bilder aus Twemoji (jdecked-Fortsetzung, Grafiken CC-BY 4.0)
  https://github.com/jdecked/twemoji

Ergebnis (alles unter src/main/resources/assets/oviemoji/):

- textures/font/emoji.png  ein Atlas mit allen Emojis, 32 x 32 Pixel pro Zelle
- font/emoji.json          die Bitmap-Schrift, die jedem Emoji ein Zeichen aus dem
                           privaten Unicode-Bereich ab U+E000 zuordnet
- emoji.tsv                die Liste, die die Mod beim Start liest

Heruntergeladene Bilder landen in tools/cache/ und werden beim naechsten Lauf
wiederverwendet. Aufruf:  python tools/build_emoji.py
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

CELL = 32            # Pixel pro Emoji im Atlas
COLUMNS = 64         # Emojis pro Atlaszeile
FIRST_CODEPOINT = 0xE000

# Kurzname pro gemoji-Kategorie, so wie ihn die Mod fuer die Reiter der Auswahl kennt
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
    """Twemoji benennt Dateien nach den Codepunkten; ohne ZWJ faellt U+FE0F weg."""
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
        print(f"Ohne Twemoji-Bild, ausgelassen: {', '.join(missing)}")

    rows = (len(kept) + COLUMNS - 1) // COLUMNS
    atlas = Image.new("RGBA", (COLUMNS * CELL, rows * CELL), (0, 0, 0, 0))
    lines = ["# codepunkt\tunicode\tkategorie\tkurznamen\tstichwoerter"]
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

    # Leere Zellen der letzten Zeile mit U+0000 auffuellen, das die Schrift ueberspringt
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
    print(f"{len(kept)} Emojis, Atlas {atlas.width} x {atlas.height}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
