import java.awt.*;

public class SniperTower extends Tower {
    public SniperTower(int row, int col) {
        super(row, col);
        range = 220;
        damage = 60;
        cooldown = 80;
        cost = 90;
        color = new Color(135, 80, 190);
    }

    @Override
    public String getName() {
        return "Sniper Tower";
    }
}