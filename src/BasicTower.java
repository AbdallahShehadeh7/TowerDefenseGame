import java.awt.*;

public class BasicTower extends Tower {
    public BasicTower(int row, int col) {
        super(row, col);
        range = 130;
        damage = 24;
        cooldown = 36;
        cost = 60;
        color = new Color(55, 126, 220);
    }

    @Override
    public String getName() {
        return "Basic Tower";
    }
}