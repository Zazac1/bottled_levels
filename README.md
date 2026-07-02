Bottled Levels
==============

Un mod Fabric pour **Minecraft 1.21.11** qui ajoute une bouteille d'XP rechargeable pouvant stocker jusqu'à 30 niveaux.

Features
--------
- 🧪 Bouteille avec **7 stades visuels** selon le remplissage (0 → 30 niveaux)
- 📥 **Clic droit maintenu (Shift)** pour remplir la bouteille — draine les points XP niveau par niveau
- 🍶 **Boire** la bouteille pour récupérer l'XP stockée
- 📦 Bouteilles du même niveau **stackables**
- 🔄 Une bouteille vide est rendue dans l'inventaire après utilisation
- 🎵 Effets sonores au remplissage, à la consommation, et quand la bouteille atteint son maximum
- 🔨 **Craft** : 4× Lapis Lazuli autour d'une Glass Bottle
- ⚙️ **Config ModMenu** — mode boisson, niveau max, stackable on/off

Build & Installation
--------------------
```
.\gradlew.bat build
```
Placer le JAR depuis `build\libs\` dans le dossier `mods` de Fabric.

Lancement dev :
```
.\gradlew.bat runClient
```

Compatibilité
-------------
| Dépendance     | Version        |
|----------------|----------------|
| Minecraft      | 1.21.11        |
| Fabric Loader  | 0.19.3         |
| Fabric API     | 0.141.4+1.21.11|
| ModMenu        | 17.0.0 (opt.)  |

Licence & Distribution
----------------------
- Code : MIT (voir fichier `LICENSE`)
- Assets : inclus dans le repository
- Inclusion dans des modpacks autorisée — merci de créditer **Zazac1**

Liens
-----
- 🐛 Issues : https://github.com/Zazac1/bottled_levels/issues
- 💻 Source : https://github.com/Zazac1/bottled_levels
