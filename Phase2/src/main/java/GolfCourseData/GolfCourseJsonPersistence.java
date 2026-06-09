package GolfCourseData;

/*
 * Saves and loads a GolfCourse as JSON
 * Also supports older JSON files where obstacles were saved differently
 */

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import GolfCourseData.Obstacles.Sand;
import GolfCourseData.Obstacles.Tree;
import GolfCourseData.Obstacles.Wall;

final class GolfCourseJsonPersistence {
    private GolfCourseJsonPersistence() {
    }

    static void save(GolfCourse course, String filePath) throws Exception {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (Writer writer = Files.newBufferedWriter(Path.of(filePath))) {
            gson.toJson(course, writer);
        }
    }

    static LoadedCourse load(String filePath) throws Exception {
        Gson gson = new Gson();
        String json = Files.readString(Path.of(filePath));

        try (Reader reader = new java.io.StringReader(json)) {
            GolfCourse course = gson.fromJson(reader, GolfCourse.class);
            return new LoadedCourse(course, loadLegacyObstacles(json));
        }
    }

    private static LegacyObstacles loadLegacyObstacles(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray legacyObstacles = root.getAsJsonArray("obstacles");
        if (legacyObstacles == null) {
            return LegacyObstacles.empty();
        }

        List<Sand> sandPits = new ArrayList<>();
        List<Tree> trees = new ArrayList<>();
        List<Wall> walls = new ArrayList<>();

        for (JsonElement element : legacyObstacles) {
            JsonObject obstacle = element.getAsJsonObject();
            String type = obstacle.get("type").getAsString();
            double x1 = obstacle.get("x1").getAsDouble();
            double y1 = obstacle.get("y1").getAsDouble();

            switch (type) {
                case "SAND" -> sandPits.add(new Sand(x1, y1, obstacle.get("radius").getAsDouble()));
                case "TREE" -> trees.add(new Tree(x1, y1, obstacle.get("radius").getAsDouble()));
                case "WALL" -> walls.add(new Wall(
                    x1,
                    y1,
                    obstacle.get("x2").getAsDouble(),
                    obstacle.get("y2").getAsDouble(),
                    obstacle.get("thickness").getAsDouble(),
                    1.0
                ));
                default -> {
                }
            }
        }

        return new LegacyObstacles(sandPits, trees, walls);
    }

    record LoadedCourse(GolfCourse course, LegacyObstacles legacyObstacles) {
    }

    record LegacyObstacles(List<Sand> sandPits, List<Tree> trees, List<Wall> walls) {
        static LegacyObstacles empty() {
            return new LegacyObstacles(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }

        boolean hasObstacles() {
            return !sandPits.isEmpty() || !trees.isEmpty() || !walls.isEmpty();
        }
    }
}
