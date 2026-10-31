# Random Teleport (Minecraft Java 26.2 / Fabric)

A lightweight, server-side Fabric mod that adds random safe teleportation in the Overworld and Nether. There is deliberately no `/home` command or home system: players must travel to explore and establish places themselves.

## Requirements
- Minecraft Java Edition **26.2**
- Fabric Loader **0.19.3 or newer**
- Fabric API **0.154.2+26.2 or newer for 26.2**
- Java **25** to build

## Commands
- `/rtp` — random safe location in the Overworld.
- `/rtp overworld` — same, explicitly selecting the Overworld.
- `/rtp nether` — random safe location in the Nether.

There is no End RTP and no home/return teleport feature.

## Multiplayer installation
This mod registers commands and performs teleportation on the server, so install the built JAR in the **server's** `mods` folder alongside Fabric API. Players do not need to install it on their clients.

## Build
Use Java 25 and Gradle 9.5.1, then run:

```bash
gradle build
```

The GitHub Actions workflow in `.github/workflows/build.yml` builds the JAR and uploads it as an artifact. You can also upload this project to a GitHub repository and use **Actions → Build Fabric Mod → Run workflow**.

## Notes
- RTP chooses random X/Z positions inside the selected dimension's world border and random Y levels, so Overworld results can be underground or in caves.
- The safety check requires a solid floor, two blocks of clear space, no fluid in the player space, and excludes common damaging floor blocks. It tries up to 350 random locations and reports if it cannot find one.
- This is an initial lightweight implementation; test it on a copy of any important world before using it on a live server.
