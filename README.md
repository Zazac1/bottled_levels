Bottled Levels
==============

A Fabric mod for **Minecraft 1.21.11** that adds a refillable bottle able to store whole experience levels.

Features
--------
- 🧪 Bottle with **10 visual stages** based on stored levels and the world capacity
- 📥 **Sneak + right-click** to store the maximum possible number of whole levels in one transfer
- 🍶 **Drink** the bottle to retrieve every stored whole level
- 📊 The player's experience-bar percentage is preserved during both transfers
- 📦 Bottles at the same level are **stackable**
- 🔄 Empty bottles remain reusable and stack correctly
- 🎵 Sound effects on fill, drink, and when the bottle reaches its maximum
- 🔨 **Craft** with 4× Lapis Lazuli around a Glass Bottle; the recipe unlocks after obtaining either ingredient
- ⚙️ World settings are managed by server commands

Build & Installation
--------------------
```
.\gradlew.bat build
```
Place `build\libs\bottledlevels-1.0.3+1.21.11.jar` into your Fabric `mods` folder.

Dev launch:
```
.\gradlew.bat runClient
```

World configuration
-------------------
Operators can configure each world/server independently:

```
/bottledlevels
/bottledlevels capacity <levels>
/bottledlevels damage <true|false>
/bottledlevels damage amount <health-points>
/bottledlevels cooldown <seconds>
```

The commands require operator permission in both single-player and multiplayer. The configuration is saved as `bottled_levels.json` alongside that world's `level.dat`. Cooldown defaults to 5 seconds; set it to `0` to disable it.

Compatibility
-------------
| Dependency     | Version         |
|----------------|-----------------|
| Minecraft      | 1.21.11         |
| Fabric Loader  | 0.19.3          |
| Fabric API     | 0.141.4+1.21.11 |

License & Distribution
----------------------
- Code: MIT (see `LICENSE` file)
- Assets: included in the repository
- Inclusion in modpacks is allowed — please credit **Zazac1**

Links
-----
- 🐛 Issues: https://github.com/Zazac1/bottled_levels/issues
- 💻 Source: https://github.com/Zazac1/bottled_levels
