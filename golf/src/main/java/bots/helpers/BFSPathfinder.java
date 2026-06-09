package bots.helpers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * BFS on the course grid
 * 
 * returns the shortest cell path from start to goal
 * or an empty list if no path is found
 */
public final class BFSPathfinder {

    // up, right, down, left matrix
    private static final int[][] NEIGHBORS = {
        { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 }
    };

    private BFSPathfinder() {}

    // The one that finds the shortest cell path from start to goal
    public static List<int[]> find(CourseGrid grid, int startCol, int startRow, int goalCol, int goalRow) {
        if (!grid.isPassable(startCol, startRow) || !grid.isPassable(goalCol, goalRow)) {
            return new ArrayList<>();
        }

        int cols = grid.getCols();
        int rows = grid.getRows();
        boolean[][] visited = new boolean[cols][rows];
        int[][] parentCol = new int[cols][rows];
        int[][] parentRow = new int[cols][rows];

        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                parentCol[c][r] = -1;
                parentRow[c][r] = -1;
            }
        }

        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[] { startCol, startRow });
        visited[startCol][startRow] = true;

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int col = cur[0];
            int row = cur[1];

            if (col == goalCol && row == goalRow) {
                return rebuildPath(parentCol, parentRow, startCol, startRow, goalCol, goalRow);
            }

            for (int[] step : NEIGHBORS) {
                int nc = col + step[0];
                int nr = row + step[1];
                if (nc < 0 || nc >= cols || nr < 0 || nr >= rows) continue;
                if (visited[nc][nr]) continue;
                if (!grid.isPassable(nc, nr)) continue;

                visited[nc][nr] = true;
                parentCol[nc][nr] = col;
                parentRow[nc][nr] = row;
                queue.add(new int[] { nc, nr });
            }
        }

        return new ArrayList<>();
    }

    // walks from the parents to the start back from the goal,
    // then flips it so it reads start -> goal
    private static List<int[]> rebuildPath(int[][] parentCol, int[][] parentRow,
            int startCol, int startRow, int goalCol, int goalRow) {

        ArrayList<int[]> reversed = new ArrayList<>();
        int col = goalCol;
        int row = goalRow;

        while (col != -1 && row != -1) {
            reversed.add(new int[] { col, row });
            if (col == startCol && row == startRow) break;
            int pc = parentCol[col][row];
            int pr = parentRow[col][row];
            col = pc;
            row = pr;
        }

        ArrayList<int[]> path = new ArrayList<>();
        for (int i = reversed.size() - 1; i >= 0; i--) {
            path.add(reversed.get(i));
        }
        return path;
    }
}
