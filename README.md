# Ore Detector Reborn

Simple handheld **ore detectors** for Minecraft **1.21.11** (Fabric, NeoForge & Forge).

Craft a detector, **right-click a surface**, and it scans the blocks *behind* that surface for a
specific ore: a short beep and an action-bar message tell you whether it's there and how much.
A lightweight, no-cheats way to decide where to start digging.

> **Unofficial, updated port of [Ore Detector](https://modrinth.com/mod/ore-detector) by restonic4.**
> This is a community continuation for Minecraft 1.21.11; it is not made by or affiliated with the original author.
> Original mod © restonic4, MIT. Port and expansion by chillpavz, MIT.

## Features

- **Eleven detectors:** Iron, Gold, Diamond, Emerald, Quartz, Copper, Coal, Amethyst, Netherite (Ancient Debris), Lapis Lazuli and Redstone (plus an optional Zinc detector with Create).
- **Directional scanning:** point at the ground to reach deep (16 blocks), or at a wall/ceiling for a
  shorter range (8 blocks), across a 3×3 column.
- **Clear feedback:** an action-bar message tinted to the ore's colour tells you the exact count
  (e.g. *"Detected 4 Iron Ore nearby"*), plus a beep.
- **Durability & repair:** detectors wear down (1 per scan + 1 per ore found), repair in an anvil
  with their material, and support Mending/Unbreaking.
- **In-game config** for reach, column radius, cooldown, durability and volume, all bounded to sane
  limits, on all three loaders (see *Config* below).
- **Optional Create integration:** if [Create Fly](https://modrinth.com/mod/create-fly) is installed,
  a **Zinc Detector** is added automatically.
- **Optional Universal Ores integration:** if [Universal Ores](https://modrinth.com/mod/universal_ores)
  is installed, the Coal, Iron, Gold, Copper, Lapis Lazuli, Redstone, Emerald, Diamond and Quartz
  detectors also pick up its andesite / diorite / granite / tuff / calcite / blackstone / basalt ore
  variants, counted together with the vanilla ore.

## Config

Same six options everywhere, but the screen is built differently per loader because the config-UI
ecosystem is split at this version:

| Loader | Config screen | Reached from |
|---|---|---|
| Fabric | Cloth Config (generated) | Mod Menu |
| NeoForge | Cloth Config (generated) | built-in mods list |
| Forge | hand-written, bundled | mods list Config button |

Cloth Config has no Forge build past 1.21.3, and neither Configured nor Forge Config Screens has a
1.21.11 Forge build, so the Forge module uses Forge's own `ForgeConfigSpec` plus a small screen
included in this mod. Nothing extra to install on Forge.

## Dependencies

| Mod | Fabric | NeoForge | Forge | Notes |
|-----|:---:|:---:|:---:|-------|
| Fabric API | required | — | — | |
| Cloth Config | required | required | — | powers the config screen on Fabric/NeoForge |
| Mod Menu | optional | — | — | adds the config button on Fabric |
| Create Fly | optional | — | — | unlocks the Zinc Detector |
| Universal Ores | optional | — | — | its ore variants are detected too |

Create Fly and Universal Ores are Fabric/Quilt-only mods, so they can only apply to the Fabric build.

## Building

Requires **JDK 24**, *not* 25. This template ships Gradle 8.14.3, which cannot run on JDK 25
(`Unsupported class file major version 69`), and ForgeGradle 6 does not support Gradle 9, so the
wrapper stays where it is. Gradle provisions a JDK 21 toolchain to compile against.

```bash
JAVA_HOME=/c/Program\ Files/Java/jdk-24 ./gradlew build
```

Output jars are in `fabric/build/libs/`, `neoforge/build/libs/` and `forge/build/libs/` (ignore the
`-sources` / `-javadoc` files).

## Credits & License

- Original **Ore Detector** by **restonic4**: https://github.com/restonic4/OreDetector
- Multi-loader project structure based on Jared's [MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template).
- Licensed under the **MIT License** (see `LICENSE`), preserving the original author's copyright.
