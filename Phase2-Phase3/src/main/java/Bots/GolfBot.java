package Bots;

import java.util.Objects;
import java.util.Optional;

import GolfCourseData.*;
import ShotEngine.*;
import Solvers.Solver;

//Shared course access and optional hook for the future ML bot implementation
public abstract class GolfBot {

    protected final GolfCourse course;
    //protected final ShotSimulator shotSimulator;

    /*
    protected GolfBot(GolfCourse course, ShotSimulator shotSimulator) {
        this.course = Objects.requireNonNull(course, "course"); //We explicitly check that a course is not null
        this.shotSimulator = shotSimulator;
    }
    */
    ////
    protected final Solver solver;
    ////
    protected GolfBot(GolfCourse course, Solver solver) {
    this.course = Objects.requireNonNull(course, "course");
    this.solver = solver;
}

    protected GolfBot(GolfCourse course) {
        this(course, null);
    }

    //FOR THE ML BOT ONLY
    /* 
    protected Optional<ShotSimulator> shotSimulator() {
        return Optional.ofNullable(shotSimulator);
    }
    */
}
