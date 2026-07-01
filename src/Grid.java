import java.awt.*;
import java.util.*;

public class Grid {
    public static final int ROWS = 10;
    public static final int COLS = 16;
    public static final int TILE_SIZE = 50;

    private Tile[][] tiles;
    private Point start;
    private Point base;

    public Grid() {
        tiles = new Tile[ROWS][COLS];
        buildGrid();
    }

    private void buildGrid() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                tiles[row][col] = new Tile(row, col, Tile.GRASS);
            }
        }

        int[][] path = {
                {4,0},{4,1},{4,2},{4,3},{4,4},{4,5},{4,6},
                {5,6},{6,6},{6,7},{6,8},{6,9},{6,10},{6,11},{6,12},
                {5,12},{4,12},{4,13},{4,14},{4,15}
        };

        for (int[] p : path) {
            tiles[p[0]][p[1]].setType(Tile.PATH);
        }

        start = new Point(0, 4);
        base = new Point(15, 4);
        tiles[4][0].setType(Tile.START);
        tiles[4][15].setType(Tile.BASE);
    }

    public java.util.List<Point> findPathBFS() {
        boolean[][] visited = new boolean[ROWS][COLS];
        Point[][] parent = new Point[ROWS][COLS];
        Queue<Point> queue = new LinkedList<>();

        queue.add(start);
        visited[start.y][start.x] = true;

        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};

        while (!queue.isEmpty()) {
            Point current = queue.remove();

            if (current.equals(base)) {
                return rebuildPath(parent, current);
            }

            for (int i = 0; i < 4; i++) {
                int nr = current.y + dr[i];
                int nc = current.x + dc[i];

                if (isInside(nr, nc) && !visited[nr][nc] && isWalkable(nr, nc)) {
                    visited[nr][nc] = true;
                    parent[nr][nc] = current;
                    queue.add(new Point(nc, nr));
                }
            }
        }

        return new ArrayList<>();
    }

    private java.util.List<Point> rebuildPath(Point[][] parent, Point end) {
        ArrayList<Point> result = new ArrayList<>();
        Point current = end;

        while (current != null) {
            result.add(0, new Point(
                    current.x * TILE_SIZE + TILE_SIZE / 2,
                    current.y * TILE_SIZE + TILE_SIZE / 2
            ));
            current = parent[current.y][current.x];
        }

        return result;
    }

    private boolean isWalkable(int row, int col) {
        int type = tiles[row][col].getType();
        return type == Tile.PATH || type == Tile.START || type == Tile.BASE;
    }

    public int recursiveCountGrass(int row, int col, boolean[][] visited) {
        if (!isInside(row, col) || visited[row][col] || tiles[row][col].getType() != Tile.GRASS) {
            return 0;
        }

        visited[row][col] = true;

        return 1
                + recursiveCountGrass(row - 1, col, visited)
                + recursiveCountGrass(row + 1, col, visited)
                + recursiveCountGrass(row, col - 1, visited)
                + recursiveCountGrass(row, col + 1, visited);
    }

    public boolean isInside(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }

    public Tile getTile(int row, int col) {
        if (!isInside(row, col)) return null;
        return tiles[row][col];
    }

    public Tile[][] getTiles() {
        return tiles;
    }
}