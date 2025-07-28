# Persistent Arrows

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.7-green.svg)](https://minecraft.net/)
[![Mod Version](https://img.shields.io/badge/Mod%20Version-1.2-blue.svg)]()
[![Fabric API](https://img.shields.io/badge/Fabric%20API-0.129.0+1.21.7-blue.svg)](https://fabricmc.net/)
[![Fabric Loader](https://img.shields.io/badge/Fabric%20Loader-0.16.14-blue.svg)](https://fabricmc.net/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Ever shot a mob with a poison arrow and watched your arrow disappear when the mob died? This mod fixes that! Your arrows will stay floating where they are, just like in Bedrock Edition.

## The Inspiration

While scrolling YouTube Shorts, I stumbled upon [this fascinating video](https://www.youtube.com/shorts/6ZiADxbTt14) showcasing a unique arrow behavior that caught my attention. The video demonstrated how arrows would persist and float after killing mobs if they were one-shotted - a mechanic that seemed both practical and visually appealing.

However, I lter discovered this intriguing interaction was exclusive to Minecraft: Bedrock Edition.

That's when I decided to take matters into my own hands. This mod was born from the desire to bridge that gap and bring this "unintended feature" from Bedrock Edition to Java Edition. 

## What This Mod Does

**Keeps Your Arrows**: When you kill a mob with a poison, harming, or other potion arrow, the arrow stays floating instead of disappearing

**Smart Detection**: Works with all tipped arrows and lingering potion arrows automatically

**Bubble Columns**: Arrows floating in bubble columns stay tracked even when bouncing around

**Fast & Smooth**: Optimized to prevent lag - no more performance issues!

## Recent Updates

**Fixed Lag Issues**: Completely rewrote the detection system to eliminate performance problems

**Better Bubble Tracking**: Arrows in bubble columns now stay tracked properly when bouncing

**Health-Based Detection**: Now detects when mobs will die based on their health, making arrow respawning much more reliable

## Installation

1. Download and install [Fabric Loader](https://fabricmc.net/use/)
2. Download [Fabric API](https://modrinth.com/mod/fabric-api)
3. Download this mod
4. Put both the Fabric API and this mod in your `mods` folder
5. Launch Minecraft and enjoy persistent arrows!

## Compatibility

- **Minecraft Version**: 1.21.7
- **Mod Loader**: Fabric
- **Dependencies**: Fabric API
- **Server Support**: Yes
- **Client Support**: Yes

## Performance

This mod is designed to be lightweight and efficient:
- No lag spikes or tick freezes
- Minimal memory usage
- Smart cleanup of old arrow data
- Optimized for multiplayer servers

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

*Bringing Bedrock Edition's arrow mechanics to Java Edition!*