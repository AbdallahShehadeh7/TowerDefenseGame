import java.awt.*;
import java.util.List;

public class NormalEnemy extends Enemy {
    public NormalEnemy(List<Point> path, int wave) {
        super(path, wave);
        maxHealth = 80 + wave * 25;
        health = maxHealth;
        speed = 1.1 + wave * 0.08;
        reward = 12;
        color = new Color(220, 60, 60);
        size = 28;
    }

    @Override
    public String getName() {
        return "Normal Enemy";
    }
}