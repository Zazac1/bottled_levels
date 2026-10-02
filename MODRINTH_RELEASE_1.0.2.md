# Modrinth release — Bottled Levels 1.0.2-1.21.11(Fabric)

## Version type

Release

## Version number

1.0.2-1.21.11(Fabric)

## Version subtitle

1.0.2-1.21.11(Fabric)

## Loader

Fabric

## Game version

1.21.11

## File

`build/libs/bottledlevels-1.0.2+1.21.11.jar`

## Version changelog

### Whole-level storage

- Bottles store and recover whole experience levels while preserving the player's experience-bar progress.
- Sneak-use deposits the maximum possible number of complete levels.
- Drinking recovers every level stored in the bottle.

### Server controls

- Per-world operator commands configure bottle capacity, optional deposit damage, and cooldown duration.
- Cooldowns apply to every Bottled Levels bottle held by the player, including stacks.

### Reliability

- Fixed stack handling so only one bottle is modified per interaction.
- Empty bottles clear their stored mod data and stack with fresh empty bottles.
- Legacy XP-only bottles remain protected from ambiguous conversion.
