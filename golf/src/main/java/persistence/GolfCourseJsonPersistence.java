package persistence;

/*
 * Saves and loads a GolfCourse as JSON
 * Also supports older JSON files where obstacles were saved differently
 */

import java.io.Reader;
import java.io.Writer;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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

import domain.course.GolfCourse;
import domain.obstacles.Sand;
import domain.obstacles.Tree;
import domain.obstacles.Wall;

public final class GolfCourseJsonPersistence {
    private GolfCourseJsonPersistence() {
    }

    public static void save(GolfCourse course, String filePath) throws Exception {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Path path = Path.of(filePath);
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (Writer writer = Files.newBufferedWriter(path)) {
            gson.toJson(course, writer);
        }
    }

    public static LoadedCourse load(String filePath) throws Exception {
        return loadJson(Files.readString(Path.of(filePath)));
    }

    public static LoadedCourse loadResource(String resourcePath) throws Exception {
        try (InputStream inputStream = GolfCourseJsonPersistence.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Course preset resource not found: " + resourcePath);
            }
            String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return loadJson(json);
        }
    }

    private static LoadedCourse loadJson(String json) throws Exception {
        Gson gson = new Gson();

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

    public record LoadedCourse(GolfCourse course, LegacyObstacles legacyObstacles) {
    }

    public record LegacyObstacles(List<Sand> sandPits, List<Tree> trees, List<Wall> walls) {
        static LegacyObstacles empty() {
            return new LegacyObstacles(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }

        public boolean hasObstacles() {
            return !sandPits.isEmpty() || !trees.isEmpty() || !walls.isEmpty();
        }
    }
}
