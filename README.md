# Ore Detector Reborn

Simple handheld **ore detectors** for Minecraft **1.20 and 1.20.1** (Fabric & Forge).

Craft a detector, **right-click a surface**, and it scans the blocks *behind* that surface for a
specific ore: a short beep and an action-bar message tell you whether it's there and how much.
A lightweight, no-cheats way to decide where to start digging.

> **Unofficial, updated port of [Ore Detector](https://modrinth.com/mod/ore-detector) by restonic4.**
> This is a community continuation for Minecraft 1.20.x; it is not made by or affiliated with the original author.
> Original mod © restonic4, MIT. Port and expansion by chillpavz, MIT.

## Features

- **Eleven detectors:** Iron, Gold, Diamond, Emerald, Quartz, Copper, Coal, Amethyst, Netherite (Ancient Debris), Lapis Lazuli and Redstone (plus an optional Zinc detector with Create).
- **Directional scanning:** point at the ground to reach deep (16 blocks), or at a wall/ceiling for a
  shorter range (8 blocks), across a 3×3 column.
- **Clear feedback:** an action-bar message tinted to the ore's colour tells you the exact count
  (e.g. *"Detected 4 Iron Ore nearby"*), plus a beep.
- **Durability & repair:** detectors wear down (1 per scan + 1 per ore found), repair in an anvil
  with their material, and support Mending/Unbreaking.
- **Per-detector cooldowns:** using one detector never puts the rest on cooldown.
- **In-game config** for reach, column radius, cooldown, durability and volume, on both loaders.

## One jar, two Minecraft versions, and NeoForge for free

A single build per loader covers **1.20 and 1.20.1**. The upper bound stops below 1.20.2, which moved
the data-pack folders and rewrote networking.

**There is no NeoForge module.** At this Minecraft version NeoForge still uses the
`net.minecraftforge` package names and the `forge` mod id, so the Forge jar runs on it unchanged,
Create itself ships one file tagged for both loaders here. The version ranges accept NeoForge's
47.1.x builds, so the Forge download is also the NeoForge download.

## Config

Cloth Config on both loaders: 11.1.136 ships one build per loader covering both game versions.
On Fabric the button comes from Mod Menu; on Forge it's the Config button in the mods list.

## Dependencies

| Mod | Fabric | Forge | Notes |
|-----|:---:|:---:|-------|
| Fabric API | required | — | |
| Cloth Config | required | required | powers the config screen |
| Mod Menu | optional | — | adds the config button on Fabric |
| Create | — | optional | unlocks the Zinc Detector |
| Universal Ores | optional | — | its ore variants are detected too |

The optional mods land on opposite loaders here: at 1.20.1 **Create is Forge/NeoForge only**, while
**Universal Ores is Fabric/Quilt only**. Create has no 1.20 build at all, so the Zinc Detector is a
1.20.1 feature; Universal Ores covers both versions.

## Building

Requires a JDK that Gradle 8.11 supports. **JDK 22** is what this was built with. Gradle provisions
a JDK 17 toolchain to compile against. Forge uses ModDevGradle (`legacyForge`), not ForgeGradle.

```bash
JAVA_HOME=/c/Program\ Files/Java/jdk-22 ./gradlew build
```

Output jars are in `fabric/build/libs/` and `forge/build/libs/` (ignore the `-sources` / `-javadoc`
files).

## Credits & License

- Original **Ore Detector** by **restonic4**: https://github.com/restonic4/OreDetector
- Multi-loader project structure based on Jared's [MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template).
- Licensed under the **MIT License** (see `LICENSE`), preserving the original author's copyright.
