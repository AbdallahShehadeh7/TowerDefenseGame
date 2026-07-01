import java.awt.*;
import java.util.List;

public abstract class Enemy {
    protected List<Point> path;
    protected double x;
    protected double y;
    protected double speed;
    protected int health;
    protected int maxHealth;
    protected int reward;
    protected int targetIndex;
    protected boolean reachedBase;
    protected Color color;
    protected int size;

    public Enemy(List<Point> path, int wave) {
        this.path = path;
        this.targetIndex = 1;
        this.reachedBase = false;

        Point start = path.get(0);
        this.x = start.x;
        this.y = start.y;
    }

    public void move() {
        if (targetIndex >= path.size()) {
            reachedBase = true;
            return;
        }

        Point target = path.get(targetIndex);
        double dx = target.x - x;
        double dy = target.y - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance <= speed) {
            x = target.x;
            y = target.y;
            targetIndex++;
        } else {
            x += dx / distance * speed;
            y += dy / distance * speed;
        }
    }

    public void takeDamage(int damage) {
        health -= damage;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public double getHealthPercent() {
        return Math.max(0, health / (double) maxHealth);
    }

    public int getDangerScore() {
        return targetIndex * 100 + health;
    }

    public abstract String getName();

    public double getX() { return x; }
    public double getY() { return y; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getReward() { return reward; }
    public int getTargetIndex() { return targetIndex; }
    public boolean hasReachedBase() { return reachedBase; }
    public Color getColor() { return color; }
    public int getSize() { return size; }
}