package GolfCourseData;

import java.nio.file.*;
import java.util.*;

public class CourseInputModule {

    // Reads a file and generates the GeneratedCourse.java source file.     
    public void generateFromFile(String inputPath) throws Exception {
        Map<String, String> data = new HashMap<>();
        Files.lines(Path.of(inputPath)).forEach(line -> {
            String[] parts = line.split("=");
            if (parts.length == 2) data.put(parts[0].trim(), parts[1].trim());
        });
        writeJavaFile(data);
    }

    // This method generates the course from the GUI input
    public void generateFromGUI(String heightFormula, String[][] inputValuesGUI){
        //Continue adding support for GUI
    }


    private void writeJavaFile(Map<String, String> data) throws Exception {
        String[] friction = data.get("friction").split(",");
        String[] target = data.get("target").split(",");
        String[] start = data.get("start").split(",");

        // Create the string for the array contents: {friction, target, start}
        String arrayContent = String.format("{{%s, %s, %s}, {%s, %s, %s}, {%s, %s, 0.0}}",
            friction[0].trim(), friction[1].trim(), friction[2].trim(),
            target[0].trim(), target[1].trim(), target[2].trim(),
            start[0].trim(), start[1].trim()
        );

        String code = "package GolfCourseData;\n\n" +
            "public class GeneratedCourse {\n" +
            "    //This file is constantly rebuild -> changes will not save here\n" +
            "    // first index is the line level of the data starting from line 2\n" +
            "    //{friction}{target}{start} \n" +
            "    public final double[][] courseData = " + arrayContent + ";\n\n" +
            "    public double h(double x, double y) {\n" +
            "        return " + data.get("height").replaceAll("sin", "Math.sin") + ";\n" +
            "    }\n" +
            "    public double[][] courseData() {\n" +
            "        return courseData " + ";\n" +
            "    }\n" +
            "}";

        Files.writeString(Path.of("src/main/java/GolfCourseData/GeneratedCourse.java"), code);
    }
}