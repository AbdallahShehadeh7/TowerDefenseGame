import java.io.*;
import java.util.ArrayList;

public class ScoreManager {
    private ArrayList<Integer> scores = new ArrayList<>();

    public void addScore(int score) {
        scores.add(score);
        bubbleSortDescending();
    }

    public void bubbleSortDescending() {
        for (int i = 0; i < scores.size() - 1; i++) {
            for (int j = 0; j < scores.size() - i - 1; j++) {
                if (scores.get(j) < scores.get(j + 1)) {
                    int temp = scores.get(j);
                    scores.set(j, scores.get(j + 1));
                    scores.set(j + 1, temp);
                }
            }
        }
    }

    public int getBestScore() {
        if (scores.isEmpty()) return 0;
        return scores.get(0);
    }

    public void saveScores() {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter("scores.txt"));
            for (int score : scores) {
                writer.println(score);
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("Could not save scores.");
        }
    }

    public void loadScores() {
        scores.clear();

        try {
            BufferedReader reader = new BufferedReader(new FileReader("scores.txt"));
            String line;

            while ((line = reader.readLine()) != null) {
                scores.add(Integer.parseInt(line));
            }

            reader.close();
            bubbleSortDescending();
        } catch (IOException e) {
            System.out.println("No scores file found yet.");
        }
    }
}