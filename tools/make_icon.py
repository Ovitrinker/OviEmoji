"""Erzeugt die Grafiken des Mods im Terminal-Look von mods.ovitrinker.ch.

Geschrieben werden zwei Dateien aus derselben Zeichnung:
  src/main/resources/assets/oviemoji/icon.png   128x128, liegt im Jar
  branding/oviemoji-logo.png                    512x512, Projektbild fuer CurseForge und Modrinth

Hintergrund, Raster und Rahmen sind dieselben wie beim OviClicker-Icon. Darauf sitzt eine
gruene Chat-Sprechblase mit einem gelben Smiley. Der Smiley ist selbst gezeichnet und
nicht aus Twemoji uebernommen, damit das Logo frei von fremden Grafiken ist.

Aufruf:  python tools/make_icon.py
"""

from pathlib import Path

from PIL import Image, ImageDraw

GRID = 128

BACKGROUND = (5, 8, 6, 255)
GREEN = (61, 240, 122, 255)
GREEN_DARK = (16, 70, 38, 255)
BUBBLE_FILL = (10, 34, 20, 255)
YELLOW = (255, 204, 77, 255)
FACE_DARK = (102, 69, 0, 255)

ROOT = Path(__file__).resolve().parent.parent
TARGETS = [
    (ROOT / "src/main/resources/assets/oviemoji/icon.png", 128),
    (ROOT / "branding/oviemoji-logo.png", 512),
]


class Canvas:
    """Zeichenflaeche, die im 128er-Raster rechnet und ueberabgetastet zeichnet."""

    def __init__(self, size: int, supersample: int) -> None:
        self.side = size * supersample
        self.scale = self.side / GRID
        self.image = Image.new("RGBA", (self.side, self.side), BACKGROUND)
        self.size = size

    def px(self, value: float) -> float:
        return value * self.scale

    def box(self, x0: float, y0: float, x1: float, y1: float) -> list:
        return [self.px(x0), self.px(y0), self.px(x1), self.px(y1)]

    def width(self, value: float) -> int:
        return max(1, int(round(self.px(value))))

    def draw(self) -> ImageDraw.ImageDraw:
        return ImageDraw.Draw(self.image)

    def finish(self, path: Path) -> None:
        path.parent.mkdir(parents=True, exist_ok=True)
        self.image.resize((self.size, self.size), Image.LANCZOS).save(path)


def draw_background(c: Canvas) -> None:
    """Schwarzer Grund mit angedeutetem Terminal-Raster."""
    d = c.draw()
    for i in range(8, GRID, 16):
        d.line([(c.px(i), 0), (c.px(i), c.side)], fill=GREEN_DARK, width=c.width(0.5))
        d.line([(0, c.px(i)), (c.side, c.px(i))], fill=GREEN_DARK, width=c.width(0.5))


def draw_frame(c: Canvas) -> None:
    """Duenner gruener Rahmen mit Eckmarken, wie ein Terminalfenster."""
    d = c.draw()
    inset, corner = c.px(6), c.px(22)
    far = c.side - inset
    d.rectangle([inset, inset, far, far], outline=GREEN_DARK, width=c.width(1))
    for x0, y0, x1, y1 in (
        (inset, inset, inset + corner, inset),
        (inset, inset, inset, inset + corner),
        (far - corner, inset, far, inset),
        (far, inset, far, inset + corner),
        (inset, far, inset + corner, far),
        (inset, far - corner, inset, far),
        (far - corner, far, far, far),
        (far, far - corner, far, far),
    ):
        d.line([(x0, y0), (x1, y1)], fill=GREEN, width=c.width(1.5))


def draw_bubble(c: Canvas) -> None:
    """Chat-Sprechblase mit Zipfel unten links.

    Erst wird die ganze Form gruen gefuellt, dann die um die Randbreite kleinere Form dunkel
    darueber gelegt. So entsteht ein durchgehender Rand ohne Naht zwischen Blase und Zipfel.
    """
    d = c.draw()
    edge = 3
    d.rounded_rectangle(c.box(20, 24, 108, 94), radius=c.px(16), fill=GREEN)
    d.polygon([(c.px(x), c.px(y)) for x, y in ((32, 88), (22, 113), (56, 88))], fill=GREEN)
    d.rounded_rectangle(c.box(20 + edge, 24 + edge, 108 - edge, 94 - edge),
                        radius=c.px(16 - edge), fill=BUBBLE_FILL)
    d.polygon([(c.px(x), c.px(y)) for x, y in ((35.5, 88), (29, 104), (49, 88))], fill=BUBBLE_FILL)


def draw_smiley(c: Canvas) -> None:
    """Gelber Smiley mitten in der Blase."""
    d = c.draw()
    cx, cy, r = 64, 59, 25
    d.ellipse(c.box(cx - r, cy - r, cx + r, cy + r), fill=YELLOW)
    for ex in (cx - 9, cx + 9):
        d.ellipse(c.box(ex - 3.2, cy - 11, ex + 3.2, cy - 1), fill=FACE_DARK)
    d.arc(c.box(cx - 14, cy - 10, cx + 14, cy + 15), start=25, end=155,
          fill=FACE_DARK, width=c.width(3.5))


def render(path: Path, size: int) -> None:
    c = Canvas(size, max(2, round(1024 / size)))
    draw_background(c)
    draw_frame(c)
    draw_bubble(c)
    draw_smiley(c)
    c.finish(path)
    print("geschrieben: %s (%dx%d)" % (path, size, size))


def main() -> None:
    for path, size in TARGETS:
        render(path, size)


if __name__ == "__main__":
    main()
