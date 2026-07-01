import java.awt.*;

public class Tile {
    public static final int GRASS = 0;
    public static final int PATH = 1;
    public static final int START = 2;
    public static final int BASE = 3;

    private int row;
    private int col;
    private int type;
    private Tower tower;

    public Tile(int row, int col, int type) {
        this.row = row;
        this.col = col;
        this.type = type;
        this.tower = null;
    }

    public boolean canBuild() {
        return type == GRASS && tower == null;
    }

    public boolean hasTower() {
        return tower != null;
    }

    public Rectangle getBounds(int tileSize) {
        return new Rectangle(col * tileSize, row * tileSize, tileSize, tileSize);
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public int getType() { return type; }
    public Tower getTower() { return tower; }

    public void setType(int type) { this.type = type; }
    public void setTower(Tower tower) { this.tower = tower; }
}