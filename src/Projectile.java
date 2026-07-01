import java.awt.*;

public class Projectile {
    private double x;
    private double y;
    private Enemy target;
    private int damage;
    private double speed;
    private Color color;

    public Projectile(double x, double y, Enemy target, int damage, Color color) {
        this.x = x;
        this.y = y;
        this.target = target;
        this.damage = damage;
        this.color = color;
        this.speed = 9;
    }

    public void move() {
        if (target == null || !target.isAlive()) return;

        double dx = target.getX() - x;
        double dy = target.getY() - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            x += dx / distance * speed;
            y += dy / distance * speed;
        }
    }

    public boolean hitTarget() {
        if (target == null) return false;

        double dx = target.getX() - x;
        double dy = target.getY() - y;
        return Math.sqrt(dx * dx + dy * dy) < 10;
    }

    public void damageTarget() {
        if (target != null) {
            target.takeDamage(damage);
        }
    }

    public Enemy getTarget() { return target; }
    public double getX() { return x; }
    public double getY() { return y; }
    public Color getColor() { return color; }
}