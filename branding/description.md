<!--
Project text for CurseForge and Modrinth. Everything here is also in the README - nothing may
go beyond what the mod actually does.

The "SUMMARY" block goes into the "Summary" field, everything from "DESCRIPTION" on into the
description. The values for the form fields are in branding/platform-settings.md.
-->

## SUMMARY

Emojis in chat and on signs, with an emoji picker and :shortcode: autocomplete. Client-side only: players without the mod simply see :smile: as text.

## DESCRIPTION

# OviEmoji

Emojis for the Minecraft chat and for signs, in singleplayer and on any server.

OviEmoji only sends plain **shortcodes** such as `:smile:` or `:fire:`. The mod turns them into
emoji images when a message is displayed. Nothing about the message itself changes. The server
needs nothing, and nobody is locked out of the chat.

| Player | sees |
|---|---|
| with OviEmoji | the emoji image |
| without the mod | the shortcode as text, e.g. `:smile:` |

## Features

* **1870 emojis** with Twemoji graphics, in 9 categories
* **Emoji picker** — a button just above the chat input opens a panel with tabs for recently used
  emojis and each category. Click an emoji to insert its shortcode, scroll with the mouse wheel,
  and hover to see the shortcode.
* **Autocomplete** — type `:` and at least two letters, for example `:fi`, and up to 8 suggestions
  appear. Arrow keys select, Tab or Enter inserts, and Escape hides the list. It stays out of the
  way in commands and in times like `12:30`.
* **Signs** — write `:heart:` on a sign or hanging sign, or use the same picker on the sign editing
  screen. Players with the mod see the image on the sign.
* **Pasted emojis** — a real emoji like 😀 pasted into the chat is converted to its shortcode before
  sending, so players without the mod don't see an empty box. This applies to chat messages and to
  `/msg`, `/tell`, `/w`, `/me`, `/say`, `/teammsg` and `/tm`. Other commands are left untouched.
* **Received emojis** — real Unicode emojis in incoming messages, for example from a Discord bridge,
  are shown as images too.

## Good to know

* **Client-side only.** The server needs nothing, since only plain text is sent. Tested in
  singleplayer and on a vanilla server. Don't install it on a server.
* Messages stay unmodified, so **chat signing is not affected**.
* Emojis with a skin tone are shown in their default yellow form.
* Sign lines are narrow, so the picker inserts the shortest name there, for example `:+1:` instead
  of `:thumbsup:`.
* While you edit a sign, it shows the shortcode as text. The image appears once you close the
  editor.

## Versions

| Minecraft | Java |
|---|---|
| 1.21.11 | 21 |
| 26.2 | 25 |
| 26.3 | 25 |

Requires **Fabric Loader 0.19.3+** and **Fabric API**.

## Credits

* Emoji graphics: [Twemoji](https://github.com/jdecked/twemoji) by Twitter/X and contributors,
  licensed under [CC-BY 4.0](https://creativecommons.org/licenses/by/4.0/)
* Emoji names and categories: [gemoji](https://github.com/github/gemoji) by GitHub, MIT license
