package GolfCourseData;

/*
 * Reads old course input format
 * Used for old Phase 2 text files and old GUI input
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

final class LegacyCourseInput {
    private final String[] friction;
    private final String[] target;
    private final String[] start;
    private final String terrainFormula;

    private LegacyCourseInput(String[] friction, String[] target, String[] start, String terrainFormula) {
        this.friction = friction;
        this.target = target;
        this.start = start;
        this.terrainFormula = terrainFormula;
    }

    static LegacyCourseInput fromFile(String filePath) throws Exception {
        Map<String, String> data = new HashMap<>();
        Files.lines(Path.of(filePath)).forEach(line -> {
            String[] parts = line.split("=");
            if (parts.length == 2) {
                data.put(parts[0].trim(), parts[1].trim());
            }
        });

        return new LegacyCourseInput(
            data.get("friction").split(","),
            data.get("target").split(","),
            data.get("start").split(","),
            data.get("height")
        );
    }

    static LegacyCourseInput fromGui(String heightFormula, String[][] inputValuesGUI) {
        return new LegacyCourseInput(
            inputValuesGUI[0],
            inputValuesGUI[1],
            inputValuesGUI[2],
            heightFormula
        );
    }

    String[] friction() {
        return friction;
    }

    String[] target() {
        return target;
    }

    String[] start() {
        return start;
    }

    String terrainFormula() {
        return terrainFormula;
    }
}
