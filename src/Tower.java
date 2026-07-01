import java.awt.*;

public abstract class Tower {
    protected int row;
    protected int col;
    protected int x;
    protected int y;
    protected int level;
    protected int range;
    protected int damage;
    protected int cooldown;
    protected int cooldownTimer;
    protected int cost;
    protected Color color;

    public Tower(int row, int col) {
        this.row = row;
        this.col = col;
        this.x = col * Grid.TILE_SIZE + Grid.TILE_SIZE / 2;
        this.y = row * Grid.TILE_SIZE + Grid.TILE_SIZE / 2;
        this.level = 1;
        this.cooldownTimer = 0;
    }

    public void updateCooldown() {
        if (cooldownTimer > 0) {
            cooldownTimer--;
        }
    }

    public boolean canShoot() {
        return cooldownTimer <= 0;
    }

    public Projectile shoot(Enemy target) {
        cooldownTimer = cooldown;
        return new Projectile(x, y, target, damage, color);
    }

    public boolean isInRange(Enemy enemy) {
        double dx = enemy.getX() - x;
        double dy = enemy.getY() - y;
        return Math.sqrt(dx * dx + dy * dy) <= range;
    }

    public void upgrade() {
        if (level >= 3) return;

        level++;
        damage += 12;
        range += 15;
        cooldown = Math.max(8, cooldown - 5);
    }

    public int getUpgradeCost() {
        if (level >= 3) return 0;
        return 40 + level * 30;
    }

    public int getSellValue() {
        return cost / 2 + (level - 1) * 25;
    }

    public abstract String getName();

    public int getRow() { return row; }
    public int getCol() { return col; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getLevel() { return level; }
    public int getRange() { return range; }
    public int getDamage() { return damage; }
    public int getCost() { return cost; }
    public Color getColor() { return color; }
}