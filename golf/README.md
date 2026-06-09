# Crazy Putting Golf Project

This folder contains the playable Crazy Putting game. It includes the Phase 2 base implementation and the Phase 3 extensions.

## Requirements

- JDK 17
- Apache Maven 3.8+
- JavaFX dependencies are loaded through Maven

## Run The Game

From this folder:

```bash
mvn javafx:run
```

Default entry class:

```text
app.Launcher
```

The launcher opens the main menu for singleplayer, multiplayer against a bot, settings, and the course builder.

## Compile Check

```bash
mvn compile
```

At the time of writing, this compiles successfully.

## Main Features

- Physics-based putting simulation using a 4D state vector `(x, y, vx, vy)`
- Terrain height functions with slope-based acceleration
- Static and kinetic friction
- Euler, Runge-Kutta, and Verlet solvers
- Water and out-of-bounds handling
- Sand pits, trees, and walls
- Tree and wall collision handling
- Basic 2D GUI from Phase 2
- Advanced launcher, settings screen, HUD, aiming controls, and 3D scene
- Course builder for dimensions, terrain functions, hills, valleys, Perlin noise, obstacles, and ball/target placement
- JSON save/load presets
- Several bot players and experiment classes

## Project Structure

```text
src/main/java/
├── app/              Main launcher and settings screen
├── bots/             Bot players and bot helper classes
├── domain/           Course data, terrain, and obstacles
├── engine/           Game state, rules, turns, scoring, and recovery logic
├── experiments/      Experiment entry points for bots, motion, and solvers
├── persistence/      Course loading and saving
├── physics/          Golf ODE and shot simulation
├── solvers/          Euler, Runge-Kutta, and Verlet solvers
└── ui/               JavaFX user interfaces
```

## Package Map

- `app` - main launcher and settings screen
- `domain.course` - `GolfCourse`, bounds, ball, and hill data
- `domain.terrain` - terrain height formulas, Perlin noise, and random terrain generation
- `domain.obstacles` - sand, trees, walls, water, obstacle management, and placement checks
- `persistence` - JSON save/load and old Phase 2 input support
- `engine` - game manager, turn handling, stroke counts, rules, penalties, and shot results
- `physics` - `GolfODE` and shot simulation
- `solvers` - Euler, Runge-Kutta, and Verlet solvers
- `bots` - bot strategies such as SimpleBot, MLBot, HillBot, ManhattanBot, and NewtonBot
- `ui.game` - advanced game screen
- `ui.game.scene` - 3D scene, terrain rendering, camera, obstacles, HUD, and aiming
- `ui.builder` - course builder and preview
- `ui.simple` - original/simple Phase 2 GUI
- `experiments` - experiment classes for bots, motion, and solvers

## Resources

Images and presets are stored in:

```text
src/main/resources/
├── images/
└── Presets/
    ├── Phase2Format/
    └── Phase3Format/
```

The JSON presets are used by the course builder and can include terrain settings, start and target positions, hills, sand pits, trees, walls, and course dimensions.

## Phase 2 vs Phase 3

| Area | Phase 2 base | Phase 3 extension |
|------|--------------|-------------------|
| Physics | 4D ball state, terrain slopes, friction | obstacle-aware simulation refinements |
| Solvers | Euler and Runge-Kutta | Verlet solver |
| Course input | text/manual course input | JSON presets and visual course builder |
| GUI | simple JavaFX interface | launcher, HUD, 3D scene, settings, builder |
| Terrain | height function | hills, valleys, Perlin noise, editable dimensions |
| Obstacles | water and basic hazards | sand friction, trees, walls, placement tools |
| Bots | simple bot ideas | multiple bot strategies and experiments |

## Notes

The project folder is called `golf`, so the Java packages do not also start with `golf`. For example, the game engine package is `engine`, not `golf.engine`.
