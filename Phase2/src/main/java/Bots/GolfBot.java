package Bots;

import java.util.Objects;
import java.util.Optional;

import GolfCourseData.*;
import ShotEngine.*;

//Shared course access and optional hook for the future ML bot implementation
 
public abstract class GolfBot {

    protected final GolfCourse course;
    private final ShotSimulator shotSimulator;

    protected GolfBot(GolfCourse course, ShotSimulator shotSimulator) {
        this.course = Objects.requireNonNull(course, "course"); //We explicitly check that a course is not null
        this.shotSimulator = shotSimulator;
    }

    protected GolfBot(GolfCourse course) {
        this(course, null);
    }

    //FOR THE ML BOT ONLY

    protected Optional<ShotSimulator> shotSimulator() {
        return Optional.ofNullable(shotSimulator);
    }
}
