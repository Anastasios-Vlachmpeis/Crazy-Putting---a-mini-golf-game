package GolfCourseData;

import java.nio.file.*;
import java.util.*;

public class CourseInputModule {

    /**
     * Reads a file and generates the GolfCourse.java source file.
     */
    public void generateFromFile(String inputPath) throws Exception {
        Map<String, String> data = new HashMap<>();
        Files.lines(Path.of(inputPath)).forEach(line -> {
            String[] parts = line.split("=");
            if (parts.length == 2) data.put(parts[0].trim(), parts[1].trim());
        });
        writeJavaFile(data);
    }

    /**
     * Allows for manual input (e.g., from a UI or Scanner)
     */
    public void generateFromMap(Map<String, String> data) throws Exception {
        writeJavaFile(data);
    }

    private void writeJavaFile(Map<String, String> data) throws Exception {
        String[] target = data.get("target").split(",");
        String[] start = data.get("start").split(",");

        String code = "package Systems;\n\n" +
            "public class GolfCourse {\n" +
            "    public final double friction = " + data.get("friction") + ";\n" +
            "    public final double targetX = " + target[0].trim() + ";\n" +
            "    public final double targetY = " + target[1].trim() + ";\n" +
            "    public final double targetR = " + target[2].trim() + ";\n" +
            "    public final double startX = " + start[0].trim() + ";\n" +
            "    public final double startY = " + start[1].trim() + ";\n\n" +
            "    public double h(double x, double y) {\n" +
            "        return " + data.get("height") .replaceAll("sin", "Math.sin") + ";\n" +
            "    }\n" +
            "}";

        Files.createDirectories(Path.of("src/Systems/"));
        Files.writeString(Path.of("src/main/java/GolfCourseData/GolfCourseFromFile.java"), code);
    }
}