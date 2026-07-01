import java.awt.*;
import java.util.List;

public class TankEnemy extends Enemy {
    public TankEnemy(List<Point> path, int wave) {
        super(path, wave);
        maxHealth = 190 + wave * 50;
        health = maxHealth;
        speed = 0.75 + wave * 0.04;
        reward = 28;
        color = new Color(120, 75, 45);
        size = 36;
    }

    @Override
    public String getName() {
        return "Tank Enemy";
    }
}