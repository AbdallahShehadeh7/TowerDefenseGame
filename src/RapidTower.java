import java.awt.*;

public class RapidTower extends Tower {
    public RapidTower(int row, int col) {
        super(row, col);
        range = 115;
        damage = 14;
        cooldown = 15;
        cost = 75;
        color = new Color(235, 150, 45);
    }

    @Override
    public String getName() {
        return "Rapid Tower";
    }
}