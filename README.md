# OviEmoji

Emojis im Minecraft-Chat und auf Schildern, im Einzelspieler wie im Mehrspieler.

- **Autor:** Ovitrinker
- **Lizenz:** Ovitrinker (siehe `LICENSE`), fremde Inhalte siehe `LICENSE-ASSETS.md`
- **Mod-ID:** `oviemoji`, Package `ch.ovitrinker.oviemoji`
- **Download:** https://mods.ovitrinker.ch/oviemoji/
- **Texte für CurseForge/Modrinth:** `branding/` (Beschreibung, Formularfelder, Changelog, Logo)

## Wie es funktioniert

Verschickt werden nur gewöhnliche **Kurzcodes** wie `:smile:` oder `:fire:`. Erst beim Anzeigen
macht die Mod daraus ein Bild.

| Wer | sieht |
|---|---|
| Spieler mit OviEmoji | das Emoji-Bild |
| Spieler ohne Mod | den Kurzcode als Text, z. B. `:smile:` |

Die Mod ist **rein clientseitig**. Der Server braucht nichts (Vanilla, Paper, Einzelspieler).
Weil die Nachricht selbst nicht verändert wird, gibt es keine Probleme mit der Chat-Signierung.

**Eingefügte Unicode-Emojis** (etwa 😀 aus der Zwischenablage) wandelt die Mod vor dem Senden in
Kurzcodes um. So sehen Spieler ohne Mod keine leeren Kästchen. Das gilt für normale
Chatnachrichten und für `/msg`, `/tell`, `/w`, `/me`, `/say`, `/teammsg` und `/tm`. Andere Befehle
bleiben unangetastet. **Empfangene** Unicode-Emojis, etwa von einer Discord-Brücke, zeigt die Mod
ebenfalls als Bild an.

Emojis mit Hautton werden in der gelben Grundform gezeigt. Einzelne Zeichen wie © oder ↔ werden
nur mit dem Emoji-Selektor U+FE0F zum Bild. Ohne ihn bleiben sie gewöhnlicher Text.

## Bedienung

- **Emoji-Knopf** rechts über dem Chat-Eingabefeld: Er öffnet ein Auswahlfenster mit 10 Reitern
  (zuletzt benutzt plus 9 Kategorien). Ein Klick fügt den Kurzcode ein. Das Mausrad blättert,
  und die Fusszeile zeigt den Kurzcode unter der Maus. Escape schliesst zuerst das Fenster.
- **Autovervollständigung:** Nach `:` und mindestens zwei Buchstaben, etwa `:fi`, erscheinen bis
  zu 8 Vorschläge. Pfeil hoch/runter wählt, Tab oder Enter setzt ein, Escape blendet die Liste
  aus. Der Doppelpunkt muss am Anfang oder nach einem Leerzeichen stehen, damit `12:30` nichts
  auslöst. In Befehlen (`/…`) bleibt die Liste aus.
- **Schilder:** `:smile:` auf ein Schild schreiben oder den Emoji-Knopf unten rechts im
  Schild-Bildschirm benutzen. Spieler mit Mod sehen das Bild, der Bearbeitungsbildschirm zeigt den
  Kurzcode. Weil eine Schildzeile nur etwa 90 Pixel breit ist, fügt die Auswahl dort den
  **kürzesten** Kurznamen ein (`:+1:` statt `:thumbsup:`). Passt er nicht mehr in die Zeile,
  passiert nichts.

Die zuletzt benutzten Emojis stehen in `config/oviemoji.json`.

## Technik

- Die Emojis sind eine **Bitmap-Schrift** (`assets/oviemoji/font/emoji.json`). Jedes Emoji ist
  ein Zeichen aus dem privaten Unicode-Bereich ab U+E000, dessen Glyphe das farbige Twemoji-Bild
  ist (Atlas `textures/font/emoji.png`, 32 × 32 Pixel pro Zelle). Damit funktionieren
  Zeilenumbruch, Ausblenden, Klick- und Hover-Ereignisse wie bei normalem Text.
- `ChatComponentMixin` ersetzt Kurzcodes in jeder Chatzeile vor dem Speichern.
- `SignTextMixin` umhüllt die Formatierfunktion von `SignText.getRenderMessages`.
- `SignEditScreenMixin` setzt dasselbe Auswahlfenster (`gui/EmojiPicker`) auf den
  Schild-Bildschirm. Das Textfeld kommt über `SignEditScreenAccessor`, weil es ab 26.3 `final` ist.
- `ChatScreenMixin` hängt Knopf, Auswahlfenster und Vorschläge an und wandelt beim Senden um.
- `EmojiIndex` und `EmojiText` kennen keine Minecraft-Klassen und sind mit JUnit getestet
  (`src/test`).

### Emoji-Daten neu erzeugen

```
python tools/build_emoji.py
```

Das Skript lädt die Namen aus gemoji und die Bilder aus Twemoji 17.0.3 (Cache in `tools/cache/`)
und schreibt Atlas, Schrift und `emoji.tsv` neu. Stand: 1870 Emojis.

## Versionsmatrix

| Build-Knoten | Minecraft | Java | Fabric API |
|---|---|---|---|
| `1.21.11` | 1.21.11 | 21 | `0.141.6+1.21.11` |
| `26.2.x` | 26.2 | 25 | `0.157.0+26.2` |
| `26.3.x` | 26.3 | 25 | `0.161.0+26.3` |

Die APIs wurden per `javap` gegen die gemappten Jars im Loom-Cache geprüft. Unterschiede:

- `ChatComponent.addMessage`: In 1.21.11 ist die Methode `public (Component, MessageSignature,
  GuiMessageTag)`. Ab 26.2 ist sie `private` und hat zusätzlich einen `GuiMessageSource`-Parameter.
- Zeichnen: In 1.21.11 `render(GuiGraphics…)` mit `drawString`, ab 26.1
  `extractRenderState(GuiGraphicsExtractor…)` mit `text`. Das ist in `compat/Gfx` gekapselt.
- Gleich in allen Versionen: `FontDescription.Resource`, `Identifier`, `Style.withShadowColor`,
  `KeyEvent.isUp/isDown/isConfirmation/isCycleFocus/isEscape` und `SignText.getRenderMessages`.

## Bauen

```
cd "C:\dev\Minecraft Mods\OviEmoji"
gradlew.bat "1.21.11:build"
gradlew.bat "26.2.x:build"
gradlew.bat "26.3.x:build"
```

Jars: `versions/<Knoten>/build/libs/oviemoji-1.0.0+<Version>.jar` (ohne `-sources`).

Tests: `gradlew.bat "1.21.11:test"`. Entwicklungsclient: `gradlew.bat "26.3.x:runClient"`.
