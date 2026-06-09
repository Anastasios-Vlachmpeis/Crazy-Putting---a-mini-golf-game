# Team 17 - Crazy Putting

This repository contains the code for Project 1-2, **Crazy Putting**.

The project is split into two main parts:

| Directory | Contents |
|-----------|----------|
| `Phase1/` | ODE solver explorer with Euler and Runge-Kutta solvers, built-in ODE systems, and JavaFX plots |
| `golf/` | Crazy Putting game, including the Phase 2 base game and Phase 3 advanced extensions |

There is no separate `Phase3/` folder. Phase 3 features are integrated into the `golf/` project because they reuse the same course model, physics engine, solvers, GUI, and bots.

## Requirements

- JDK 17
- Apache Maven 3.8+
- JavaFX dependencies are loaded through Maven

## Running Phase 1

Phase 1 is the ODE solver and visualisation project.

```bash
cd Phase1
mvn javafx:run
```

More details are in:

```text
Phase1/README.md
```

## Running The Golf Game

The golf project is the playable Crazy Putting game.

```bash
cd golf
mvn javafx:run
```

Default entry class:

```text
app.Launcher
```

More details are in:

```text
golf/README.md
```

## Quick Build Check

Compile Phase 1:

```bash
cd Phase1
mvn compile
```

Compile the golf project:

```bash
cd golf
mvn compile
```

At the time of writing, both projects compile successfully. There are no automated test sources configured.

## Authors

Team 17:

- Stan Wouters
- Laurenz Warkentin
- Anastasios Vlachmpeis
- Angel Antonio Perez Gomez
- Damian Volovei
- Lilly Schulze

## License

This project was developed for academic purposes at Maastricht University and is not intended for public distribution.
