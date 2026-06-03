package Bots.helpers;

import java.util.List;

import GolfCourseData.GolfCourse;

/**
 * Works out where Manhattan/Newton bot should aim for its next shot
 * 
 * If the hole is in a straight clear line it aims at the hole, otherwise it runs
 * BFS around the walls and aims at the farthest corner it can still reach in one line
 */
public final class WaypointPlanner {

    // how close the ball needs to get before a corner counts as reached
    public static final double WAYPOINT_REACH_RADIUS = 2.0;

    private WaypointPlanner() {}

    // returns { aimX, aimY, reachRadius }
    public static double[] activeAim(GolfCourse course) {
        double[] ball = course.getStartPosition();
        double[] hole = course.getTargetXYR();

        CourseGrid grid = new CourseGrid(course);

        // straight shot at the hole is possible, no need to route around anything
        if (lineClear(grid, ball[0], ball[1], hole[0], hole[1])) {
            return new double[] { hole[0], hole[1], hole[2] };
        }

        int[] startCell = grid.worldToCell(ball[0], ball[1]);
        int[] goalCell = grid.worldToCell(hole[0], hole[1]);

        // if an end is blocked there is nothing to plan, we let the bot aim at the hole anyway
        if (!grid.isPassable(startCell[0], startCell[1]) || !grid.isPassable(goalCell[0], goalCell[1])) {
            return new double[] { hole[0], hole[1], hole[2] };
        }

        List<int[]> path = BFSPathfinder.find(grid, startCell[0], startCell[1], goalCell[0], goalCell[1]);
        if (path.size() < 2) {
            return new double[] { hole[0], hole[1], hole[2] };
        }

        // aim at the farthest cell on the route that is still a straight clear line from the ball
        for (int i = path.size() - 1; i >= 1; i--) {
            double[] cell = grid.cellToWorld(path.get(i)[0], path.get(i)[1]);
            if (lineClear(grid, ball[0], ball[1], cell[0], cell[1])) {
                return new double[] { cell[0], cell[1], WAYPOINT_REACH_RADIUS };
            }
        }

        // the next cell along is always reachable, use it so we still make progress
        double[] next = grid.cellToWorld(path.get(1)[0], path.get(1)[1]);
        return new double[] { next[0], next[1], WAYPOINT_REACH_RADIUS };
    }

    // checks every point along the line is passable by sampling small steps
    private static boolean lineClear(CourseGrid grid, double x0, double y0, double x1, double y1) {
        double dist = Math.hypot(x1 - x0, y1 - y0);
        int samples = (int) Math.ceil(dist / (grid.getCellSize() * 0.5));
        if (samples < 1) samples = 1;

        for (int s = 0; s <= samples; s++) {
            double t = (double) s / samples;
            double x = x0 + (x1 - x0) * t;
            double y = y0 + (y1 - y0) * t;
            if (!grid.isPassableWorld(x, y)) return false;
        }
        return true;
    }
}
