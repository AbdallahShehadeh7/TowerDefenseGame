import java.awt.*;
import java.util.List;

public class WaveManager {
    private int enemiesLeft;
    private int spawnTimer;
    private int spawnedThisWave;

    public void startWave(int wave) {
        enemiesLeft = 8 + wave * 5;
        spawnTimer = 0;
        spawnedThisWave = 0;
    }

    public Enemy trySpawnEnemy(int wave, List<Point> path, int lives) {
        if (enemiesLeft <= 0) return null;

        if (spawnTimer > 0) {
            spawnTimer--;
            return null;
        }

        Enemy enemy;

        if (wave >= 4 && spawnedThisWave % 7 == 0) {
            enemy = new TankEnemy(path, wave);
        } else if (wave >= 2 && spawnedThisWave % 3 == 0) {
            enemy = new FastEnemy(path, wave);
        } else {
            enemy = new NormalEnemy(path, wave);
        }

        if (lives > 5 && wave >= 3) {
            enemy.takeDamage(-20);
        }

        enemiesLeft--;
        spawnedThisWave++;
        spawnTimer = Math.max(14, 40 - wave * 2);
        return enemy;
    }

    public boolean finishedSpawning() {
        return enemiesLeft <= 0;
    }
}