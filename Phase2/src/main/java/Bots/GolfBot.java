package Phase2.src.main.java.Bots;

import java.util.Objects;
import java.util.Optional;

import GolfCourseData.CourseRelatedMethods;
import ShotEngine.ShotSimulator;

//Shared course access and optional hook for the future ML bot implementation
 
public abstract class GolfBot {

    protected final CourseRelatedMethods course;
    private final ShotSimulator shotSimulator;

    protected GolfBot(CourseRelatedMethods course, ShotSimulator shotSimulator) {
        this.course = Objects.requireNonNull(course, "course"); //We explicitly check that a course is not null
        this.shotSimulator = shotSimulator;
    }

    protected GolfBot(CourseRelatedMethods course) {
        this(course, null);
    }

    //FOR THE ML BOT ONLY

    protected Optional<ShotSimulator> shotSimulator() {
        return Optional.ofNullable(shotSimulator);
    }
}
