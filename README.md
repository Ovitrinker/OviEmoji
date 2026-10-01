# OviEmoji

Emojis in Minecraft chat and on signs, in singleplayer and multiplayer.

- **Author:** Ovitrinker
- **License:** all rights reserved (see `LICENSE`), third-party content see `LICENSE-ASSETS.md`
- **Mod ID:** `oviemoji`, package `ch.ovitrinker.oviemoji`
- **Download:** https://mods.ovitrinker.ch/oviemoji/
- **Texts for CurseForge/Modrinth:** `branding/` (description, form fields, changelog, logo)

## How it works

Only ordinary **shortcodes** like `:smile:` or `:fire:` are sent. The mod turns them into an image
only when displaying them.

| Who | sees |
|---|---|
| Players with OviEmoji | the emoji image |
| Players without the mod | the shortcode as text, e.g. `:smile:` |

The mod is **purely client-side**. The server needs nothing (vanilla, Paper, singleplayer).
Because the message itself isn't changed, there are no problems with chat signing.

**Pasted Unicode emojis** (e.g. 😀 from the clipboard) are converted to shortcodes before
sending. That way players without the mod don't see empty boxes. This applies to normal chat
messages and to `/msg`, `/tell`, `/w`, `/me`, `/say`, `/teammsg` and `/tm`. Other commands are left
untouched. **Received** Unicode emojis, e.g. from a Discord bridge, are shown as images too.

Emojis with skin tones are shown in their yellow base form. Single characters like © or ↔ only
become an image with the emoji selector U+FE0F. Without it they stay ordinary text.

## Usage

- **Emoji button** to the right above the chat input: opens a picker with 10 tabs (recently used
  plus 9 categories). A click inserts the shortcode. The mouse wheel scrolls, and the footer shows
  the shortcode under the mouse. Escape closes the picker first.
- **Autocomplete:** after `:` and at least two letters, e.g. `:fi`, up to 8 suggestions appear.
  Arrow up/down selects, Tab or Enter inserts, Escape hides the list. The colon has to be at the
  start or after a space, so `12:30` doesn't trigger anything. The list stays off in commands
  (`/…`).
- **Signs:** write `:smile:` on a sign or use the emoji button at the bottom right of the sign
  screen. Players with the mod see the image, the editing screen shows the shortcode. Because a
  sign line is only about 90 pixels wide, the picker inserts the **shortest** short name there
  (`:+1:` instead of `:thumbsup:`). If it doesn't fit on the line anymore, nothing happens.

Recently used emojis are stored in `config/oviemoji.json`.

## Technical details

- The emojis are a **bitmap font** (`assets/oviemoji/font/emoji.json`). Each emoji is a character
  from the Unicode private use area starting at U+E000, whose glyph is the coloured Twemoji image
  (atlas `textures/font/emoji.png`, 32 × 32 pixels per cell). That way line wrapping, fading,
  click and hover events work just like with normal text.
- `ChatComponentMixin` replaces shortcodes in every chat line before it is stored.
- `SignTextMixin` wraps the formatting function of `SignText.getRenderMessages`.
- `SignEditScreenMixin` puts the same picker (`gui/EmojiPicker`) on the sign screen. The text field
  is accessed via `SignEditScreenAccessor`, because it is `final` from 26.3 on.
- `ChatScreenMixin` attaches button, picker and suggestions and converts on send.
- `EmojiIndex` and `EmojiText` don't know any Minecraft classes and are tested with JUnit
  (`src/test`).

### Regenerating the emoji data

```
python tools/build_emoji.py
```

The script downloads the names from gemoji and the images from Twemoji 17.0.3 (cached in
`tools/cache/`) and rewrites the atlas, the font and `emoji.tsv`. Current count: 1870 emojis.

## Version matrix

| Build node | Minecraft | Java | Fabric API |
|---|---|---|---|
| `1.21.11` | 1.21.11 | 21 | `0.141.6+1.21.11` |
| `26.2.x` | 26.2 | 25 | `0.157.0+26.2` |
| `26.3.x` | 26.3 | 25 | `0.161.0+26.3` |

The APIs were checked with `javap` against the mapped jars in the Loom cache. Differences:

- `ChatComponent.addMessage`: in 1.21.11 the method is `public (Component, MessageSignature,
  GuiMessageTag)`. From 26.2 on it is `private` and has an additional `GuiMessageSource` parameter.
- Drawing: in 1.21.11 `render(GuiGraphics…)` with `drawString`, from 26.1 on
  `extractRenderState(GuiGraphicsExtractor…)` with `text`. This is wrapped in `compat/Gfx`.
- The same in all versions: `FontDescription.Resource`, `Identifier`, `Style.withShadowColor`,
  `KeyEvent.isUp/isDown/isConfirmation/isCycleFocus/isEscape` and `SignText.getRenderMessages`.

## Building

```
gradlew.bat "1.21.11:build"
gradlew.bat "26.2.x:build"
gradlew.bat "26.3.x:build"
```

Jars: `versions/<node>/build/libs/oviemoji-1.0.0+<version>.jar` (without `-sources`).

Tests: `gradlew.bat "1.21.11:test"`. Dev client: `gradlew.bat "26.3.x:runClient"`.
