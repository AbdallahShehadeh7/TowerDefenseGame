import java.io.*;
import java.util.ArrayList;

public class SaveManager {
    public void saveGame(int money, int lives, int wave, ArrayList<Tower> towers) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter("savegame.txt"));

            writer.println(money);
            writer.println(lives);
            writer.println(wave);

            for (Tower tower : towers) {
                writer.println(tower.getName() + "," + tower.getRow() + "," + tower.getCol() + "," + tower.getLevel());
            }

            writer.close();
        } catch (IOException e) {
            System.out.println("Could not save game.");
        }
    }

    public int[] loadStats() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("savegame.txt"));

            int money = Integer.parseInt(reader.readLine());
            int lives = Integer.parseInt(reader.readLine());
            int wave = Integer.parseInt(reader.readLine());

            reader.close();
            return new int[]{money, lives, wave};
        } catch (Exception e) {
            return null;
        }
    }
}