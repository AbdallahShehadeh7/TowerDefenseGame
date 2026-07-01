import java.awt.*;
import java.util.List;

public class FastEnemy extends Enemy {
    public FastEnemy(List<Point> path, int wave) {
        super(path, wave);
        maxHealth = 55 + wave * 16;
        health = maxHealth;
        speed = 1.9 + wave * 0.12;
        reward = 16;
        color = new Color(235, 205, 55);
        size = 24;
    }

    @Override
    public String getName() {
        return "Fast Enemy";
    }
}