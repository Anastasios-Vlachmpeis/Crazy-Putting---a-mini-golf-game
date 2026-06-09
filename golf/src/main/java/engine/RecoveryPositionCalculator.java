package engine;

/*
 * Calculates safe positions after bad shots
 * Finds where to place the ball after water or out-of-bounds shots
 */

import domain.course.GolfCourse;

final class RecoveryPositionCalculator {
    private RecoveryPositionCalculator() {
    }

    static RecoveryPositions calculate(GolfCourse course, double[][] trajectory, double lastSafeX, double lastSafeY) {
        double[] waterRecovery = calculateWaterRecovery(course, trajectory, lastSafeX, lastSafeY);
        double[] edgeRecovery = calculateEdgeRecovery(course, trajectory, lastSafeX, lastSafeY);
        return new RecoveryPositions(waterRecovery[0], waterRecovery[1], edgeRecovery[0], edgeRecovery[1]);
    }

    private static double[] calculateWaterRecovery(GolfCourse course, double[][] trajectory, double lastSafeX, double lastSafeY) {
        if (trajectory == null || trajectory.length < 2) {
            return new double[] {lastSafeX, lastSafeY};
        }

        for (int i = 1; i < trajectory.length; i++) {
            double x = trajectory[i][1];
            double y = trajectory[i][2];

            if (course.isWater(x, y)) {
                return setRecoveryAwayFromWater(
                    course,
                    trajectory[i - 1][1],
                    trajectory[i - 1][2],
                    x,
                    y
                );
            }
        }

        return new double[] {lastSafeX, lastSafeY};
    }

    private static double[] setRecoveryAwayFromWater(
        GolfCourse course,
        double safeX,
        double safeY,
        double waterX,
        double waterY
    ) {
        double dx = safeX - waterX;
        double dy = safeY - waterY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < 0.0001) {
            return new double[] {safeX, safeY};
        }

        double clearance = 0.5;
        double candidateX = safeX + (dx / distance) * clearance;
        double candidateY = safeY + (dy / distance) * clearance;

        if (course.isWater(candidateX, candidateY)) {
            return new double[] {safeX, safeY};
        }
        return new double[] {candidateX, candidateY};
    }

    private static double[] calculateEdgeRecovery(GolfCourse course, double[][] trajectory, double lastSafeX, double lastSafeY) {
        if (trajectory == null || trajectory.length < 2) {
            return new double[] {lastSafeX, lastSafeY};
        }

        for (int i = 1; i < trajectory.length; i++) {
            double previousX = trajectory[i - 1][1];
            double previousY = trajectory[i - 1][2];
            double currentX = trajectory[i][1];
            double currentY = trajectory[i][2];

            if (!isOutOfBounds(course, previousX, previousY) && isOutOfBounds(course, currentX, currentY)) {
                return setEdgeRecoveryFromBoundary(course, previousX, previousY, currentX, currentY);
            }
        }

        return new double[] {lastSafeX, lastSafeY};
    }

    private static boolean isOutOfBounds(GolfCourse course, double x, double y) {
        double[] boundaries = course.getSize();
        return x < boundaries[0] || x > boundaries[1] || y < boundaries[2] || y > boundaries[3];
    }

    private static double[] setEdgeRecoveryFromBoundary(
        GolfCourse course,
        double previousX,
        double previousY,
        double currentX,
        double currentY
    ) {
        double dx = currentX - previousX;
        double dy = currentY - previousY;
        double[] size = course.getSize();
        double contactFraction = 1.0;

        if (dx < 0.0) {
            contactFraction = Math.min(contactFraction, (size[0] - previousX) / dx);
        } else if (dx > 0.0) {
            contactFraction = Math.min(contactFraction, (size[1] - previousX) / dx);
        }

        if (dy < 0.0) {
            contactFraction = Math.min(contactFraction, (size[2] - previousY) / dy);
        } else if (dy > 0.0) {
            contactFraction = Math.min(contactFraction, (size[3] - previousY) / dy);
        }

        contactFraction = Math.max(0.0, Math.min(1.0, contactFraction));
        return new double[] {
            previousX + dx * contactFraction,
            previousY + dy * contactFraction
        };
    }

    record RecoveryPositions(double waterX, double waterY, double edgeX, double edgeY) {
    }
}
