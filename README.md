Revamped XP Bottle
==================

Description
-----------
Ajoute des bouteilles d'expérience réutilisables pouvant stocker jusqu'à 30 niveaux.

Build & Installation
--------------------
- Build: .\gradlew.bat build
- JAR: build\libs\ (placer le JAR dans le dossier mods de Fabric)

Configuration
-------------
- Vérifie fabric.mod.json (id, version, authors, contact).

Licence & Distribution
----------------------
- Code: MIT (fichier LICENSE)
- Assets: CC BY 4.0 (src/main/resources/assets/revamped_xp_bottle/LICENSE-assets.md)

Redistribution / Modpacks
-------------------------
Inclusion dans des modpacks autorisée. Merci de créditer l'auteur (Zazac1) et de respecter les guidelines officielles des add-ons Minecraft: https://www.minecraft.net/en-us/addons/

Pousser sur GitHub (sécurisé)
---------------------------
Ne jamais coller ton PAT dans un chat public.
Méthodes recommandées (exécuter localement dans PowerShell):

1) Avec l'outil officiel GitHub CLI (recommandé):
   - Installer: winget install --id GitHub.cli (ou choco install gh)
   - Auth: echo "TON_PAT_ICI" | gh auth login --with-token
   - Puis (une fois authentifié): git push -u origin main

2) Méthode alternative (temporaire, moins sûre):
   - Dans PowerShell: $token = Read-Host -AsSecureString "Entre ton PAT"; $b=[Runtime.InteropServices.Marshal]::SecureStringToBSTR($token); $plain=[Runtime.InteropServices.Marshal]::PtrToStringAuto($b)
   - git remote set-url origin "https://$plain@github.com/Zazac1/RevampedXpBottle.git"; git push -u origin main
   - Puis: git remote set-url origin "https://github.com/Zazac1/RevampedXpBottle.git"

Après t'être authentifié localement (avec gh auth login), dis "prêt" et je ferai le push depuis cette machine.

Contact
-------
https://github.com/Zazac1/RevampedXpBottle
