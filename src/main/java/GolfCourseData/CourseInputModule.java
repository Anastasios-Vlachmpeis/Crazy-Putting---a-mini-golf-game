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

        String[] friction = data.get("friction").split(",");
        String[] target = data.get("target").split(",");
        String[] start = data.get("start").split(",");

        // Create the string for the array contents: {friction, target, start}
        String arrayContent = String.format("{{%s, %s, %s}, {%s, %s, %s}, {%s, %s, 0.0}}",
            friction[0].trim(), friction[1].trim(), friction[2].trim(),
            target[0].trim(), target[1].trim(), target[2].trim(),
            start[0].trim(), start[1].trim()
        );

        String formula = data.get("height")
            .replaceAll("sin", "Math.sin")
            .replaceAll("cos", "Math.cos")
            .replaceAll("tan", "Math.tan");

        //Only formula and arrayContent gets used for 
        writeJavaFile(formula, arrayContent);
    }

    // This method generates the course from the GUI input
    public void generateFromGUI(String heightFormula, String[][] inputValuesGUI) throws Exception{

        heightFormula = heightFormula
            .replaceAll("sin", "Math.sin")
            .replaceAll("cos", "Math.cos")
            .replaceAll("tan", "Math.tan");

        //Create the string for the array contents: {friction, target, start}
        String arrayContent = String.format("{{%s, %s, %s}, {%s, %s, %s}, {%s, %s, 0.0}}",
            inputValuesGUI[0][0], inputValuesGUI[0][1], inputValuesGUI[0][2],
            inputValuesGUI[1][0], inputValuesGUI[1][1], inputValuesGUI[1][2],
            inputValuesGUI[2][0], inputValuesGUI[2][1]
        );

        writeJavaFile(heightFormula, arrayContent);
    }

    private void writeJavaFile(String formula, String arrayContent) throws Exception {
        

        String code = "package GolfCourseData;\n\n" +
            "public class GeneratedCourse {\n" +
            "    //This file is constantly rebuild -> changes will not save here\n" +
            "    //{friction}{target}{start} \n" +
            "    public final double[][] courseData = " + arrayContent + ";\n\n" +
            "    public double h(double x, double y) {\n" +
            "        return " + formula + ";\n" +
            "    }\n" +
            "    public double[][] courseData() {\n" +
            "        return courseData" + ";\n" +
            "    }\n" +
            "}";

        Files.writeString(Path.of("src/main/java/GolfCourseData/GeneratedCourse.java"), code);
    }
}