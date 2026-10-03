# Emotecraft FA+ Player Fix

Client-side NeoForge addon for Minecraft 1.21.1 that makes Emotecraft limb bends render correctly with the **Fresh Animations: Player Extension** resource pack. It also applies bends to the second skin layer and armor.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1
- Emotecraft 2.4 or newer for NeoForge
- Fresh Animations and Fresh Animations: Player Extension resource packs
- Entity Model Features is recommended for the Fresh Animations packs

Install the addon JAR in the profile's `mods` folder and enable the resource packs in Minecraft. This addon is client-side.

## Build from source

Requires JDK 21.

```powershell
./gradlew.bat remapJar
```

The distributable JAR is written to `build/libs/`.

## License and acknowledgements

Licensed under GNU GPL v3. This project is an independent compatibility addon and is not affiliated with or endorsed by the Emotecraft, Fresh Animations, or NeoForge teams. The Fresh Animations packs are not included.

The compatibility approach is based on the open-source [Emotecraft Fix Animate Texture](https://github.com/) project; this NeoForge port includes adjustments for the outer player skin layer and armor.
