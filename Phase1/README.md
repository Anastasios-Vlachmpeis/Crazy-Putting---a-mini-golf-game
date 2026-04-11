# Crazy Putting! - Team 17

## Description
This project implements a JavaFX-based simulator for solving and visualising systems of ordinary differential equations. Currently, it supports two numerical solvers: an Euler solver and a fourth-order Runge-Kutta solver. It contains three built-in ODE systems: Lotka-Volterra predator-prey model, the SIR epidemic model, and the FitzHugh-Nagumo neuron model.

## Installation
**Requirements:** Java 17+, Maven

Open a terminal in the project root and run:
```bash
mvn javafx:run
```

## Usage
Select an ODE system (Lotka-Volterra, SIR, or FitzHugh-Nagumo) and a solver (Euler or Runge-Kutta) from the dropdown menus. Set the step size, integration time, and initial conditions in the left panel. The simulation updates in real time, being displayed as soon as it gets the corresponding parameters. Select between Time Series and Phase Space to see the evolution in time of a variable or see them compared.

## Support
For any Question or issues regarding the simulator, you can find us at the scheduled project meetings (Group 17), or reach us by email, if you find it. 

## Authors and acknowledgment
Stan Wouters, Laurenz Warkentin, Anastasios Vlachmpeis, Angel Antonio Perez Gomez, Damian Volovei, Lilly Schulze

## License
This project was developed for academic purposes at Maastricht University and is not intended for public distribution.

## Project status
Phase 1 complete. Phase 2 in progress.
