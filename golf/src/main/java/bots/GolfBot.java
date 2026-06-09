package bots;

import java.util.Objects;
import java.util.Optional;

import domain.course.*;
import physics.*;
import solvers.Solver;

//Shared course access and optional hook for the future ML bot implementation
public abstract class GolfBot {

    protected final GolfCourse course;
    protected final Solver solver;
    
    protected GolfBot(GolfCourse course, Solver solver) {
    this.course = Objects.requireNonNull(course, "course");
    this.solver = solver;
}

    protected GolfBot(GolfCourse course) {
        this(course, null);
    }

    //For multiplayer gamemode
    public abstract double[] shoot();

    //FOR THE ML BOT ONLY
    /* 
    protected Optional<ShotSimulator> shotSimulator() {
        return Optional.ofNullable(shotSimulator);
    }
    */
}
