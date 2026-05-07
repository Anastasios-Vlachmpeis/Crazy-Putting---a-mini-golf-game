# Team 17 ~ Crazy Putting

Course project split across Maven modules: **Phase 1** is an interactive **ODE solver explorer** (JavaFX charts). **Phase 2** is **Crazy Putting**, a **JavaFX** mini-golf simulation with terrain height, friction, numerical integration, and bots. **Phase 3** is reserved for future work.

## Prerequisites

- **JDK 17**
- **Apache Maven** 3.8+
- JavaFX is pulled in as Maven dependencies (OpenJFX 21)

Each phase uses the **javafx-maven-plugin** to launch the desktop app.

## Repository layout

| Directory | Contents |
|-----------|----------|
| `Phase1/` | Generic ODE models (Euler / Runge–Kutta), time-series and phase-space plots |
| `Phase2/` | Golf course model, physics (`GolfODE`), shot simulation, GUI, bots |
| `Phase3/` | Placeholder module (`pom.xml` only for the moment) |


## Phase 1 ~ ODE explorer

Phase 1 builds a JavaFX UI to pick an **ODE system** (factory-driven models under `Systems/`), **solver**, step size, and integration horizon, and to view **time series** and **phase space**.

**Entry class in source:** `Main.GUI_phase1`

To run Phase 1 via Maven :

- Run `Main.GUI_phase1` from your IDE after importing the Maven project.

If compilation fails on Windows set the compiler encoding to UTF-8 in `pom.xml` or ensure source files are saved as UTF-8.

## Phase 2 ~ main application

From `Phase2/`:

```bash
mvn javafx:run
```

Default **main class**: `Main.GUI_phase2`

### What Phase 2 includes

- **Terrain**: height as a function of \((x,y)\); friction parameters; start and target (hole).
- **Numerical integration**: ODE system for the ball on the surface (`Systems.GolfODE`), with solvers under `Solvers/` (e.g. Euler, Runge–Kutta).
- **GUI** (`GUI/`): course controls, shot controls (`ShotPanel`), dual canvas rendering (`GameCanvas`), terrain coloring (`ColorTerrain`).
- **Shot engine** (`ShotEngine/`): physics-based shot simulation.
- **Bots** (`Bots/`): abstract `GolfBot`, rule-based `SimpleBot`, and `MLBot` scaffolding.
- **Course data** (`GolfCourseData/`): `GolfCourse`, generated-course support, example text format in `Course1.txt`.

Example course file keys (`Course1.txt`): `height`, `friction`, `target`, `start` ~ used to describe the surface and game layout.

## Phase 3

- **In Progress**

## Package map (Phase 2)

- `Main/` - application entry (`GUI_phase2`)
- `GUI/` - layout and interaction with the simulation view
- `GolfCourseData/` - course definitions and related data
- `Physics/` - ball and course profile types
- `Systems/` - `GolfODE` and integration with solvers
- `Solvers/` - numerical methods
- `ShotEngine/` - shot simulation pipeline
- `Bots/` - automated players
- `Experiment/` - commented / experimental harnesses