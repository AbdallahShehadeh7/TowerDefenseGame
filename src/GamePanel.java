import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Comparator;

public class GamePanel extends JPanel implements ActionListener, MouseListener {
    private Grid grid = new Grid();
    private java.util.List<Point> enemyPath = grid.findPathBFS();

    private ArrayList<Enemy> enemies = new ArrayList<>();
    private ArrayList<Tower> towers = new ArrayList<>();
    private ArrayList<Projectile> projectiles = new ArrayList<>();

    private WaveManager waveManager = new WaveManager();
    private SaveManager saveManager = new SaveManager();
    private ScoreManager scoreManager = new ScoreManager();

    private Timer timer = new Timer(16, this);

    private int money = 140;
    private int lives = 8;
    private int wave = 0;
    private int score = 0;
    private final int maxWave = 10;

    private boolean waveRunning = false;
    private boolean gameOver = false;
    private boolean gameWon = false;
    private boolean paused = false;

    private String selectedTowerType = "Basic";
    private Tower selectedTower = null;

    private JButton basicButton = new JButton("Basic $60");
    private JButton sniperButton = new JButton("Sniper $90");
    private JButton rapidButton = new JButton("Rapid $75");
    private JButton upgradeButton = new JButton("Upgrade");
    private JButton startButton = new JButton("Start Wave");
    private JButton pauseButton = new JButton("Pause");
    private JButton saveButton = new JButton("Save");
    private JButton loadButton = new JButton("Load");
    private JButton resetButton = new JButton("Reset");

    private JLabel statusLabel = new JLabel();
    private JLabel infoLabel = new JLabel();

    public GamePanel() {
        setLayout(new BorderLayout());

        JPanel canvasHolder = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGame((Graphics2D) g);
            }
        };

        canvasHolder.setPreferredSize(new Dimension(Grid.COLS * Grid.TILE_SIZE, Grid.ROWS * Grid.TILE_SIZE));
        canvasHolder.addMouseListener(this);
        add(canvasHolder, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(2, 1));
        JPanel buttons = new JPanel();
        JPanel labels = new JPanel();

        buttons.add(basicButton);
        buttons.add(sniperButton);
        buttons.add(rapidButton);
        buttons.add(upgradeButton);
        buttons.add(startButton);
        buttons.add(pauseButton);
        buttons.add(saveButton);
        buttons.add(loadButton);
        buttons.add(resetButton);

        labels.add(statusLabel);
        labels.add(infoLabel);

        bottom.add(buttons);
        bottom.add(labels);
        add(bottom, BorderLayout.SOUTH);

        basicButton.addActionListener(e -> selectedTowerType = "Basic");
        sniperButton.addActionListener(e -> selectedTowerType = "Sniper");
        rapidButton.addActionListener(e -> selectedTowerType = "Rapid");
        upgradeButton.addActionListener(e -> upgradeSelectedTower());
        startButton.addActionListener(e -> startWave());
        pauseButton.addActionListener(e -> togglePause());
        saveButton.addActionListener(e -> saveManager.saveGame(money, lives, wave, towers));
        loadButton.addActionListener(e -> loadGame());
        resetButton.addActionListener(e -> resetGame());

        scoreManager.loadScores();
        updateLabels();
        timer.start();
    }

    private void startWave() {
        if (waveRunning || gameOver || gameWon) return;

        wave++;
        waveManager.startWave(wave);
        waveRunning = true;
        paused = false;
        startButton.setEnabled(false);
    }

    private void togglePause() {
        if (!waveRunning) return;

        paused = !paused;
        pauseButton.setText(paused ? "Resume" : "Pause");
    }

    private void resetGame() {
        money = 140;
        lives = 8;
        wave = 0;
        score = 0;
        waveRunning = false;
        gameOver = false;
        gameWon = false;
        paused = false;
        selectedTower = null;

        enemies.clear();
        towers.clear();
        projectiles.clear();

        grid = new Grid();
        enemyPath = grid.findPathBFS();

        startButton.setEnabled(true);
        pauseButton.setText("Pause");
    }

    private void loadGame() {
        int[] stats = saveManager.loadStats();

        if (stats != null) {
            money = stats[0];
            lives = stats[1];
            wave = stats[2];
            enemies.clear();
            projectiles.clear();
            waveRunning = false;
            startButton.setEnabled(true);
        }
    }

    private void upgradeSelectedTower() {
        if (selectedTower == null) return;

        int cost = selectedTower.getUpgradeCost();

        if (money >= cost && selectedTower.getLevel() < 3) {
            money -= cost;
            selectedTower.upgrade();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!paused && !gameOver && !gameWon) {
            updateGame();
        }

        updateLabels();
        repaint();
    }

    private void updateGame() {
        if (waveRunning) {
            Enemy newEnemy = waveManager.trySpawnEnemy(wave, enemyPath, lives);

            if (newEnemy != null) {
                enemies.add(newEnemy);
            }
        }

        updateEnemies();
        sortEnemiesByDanger();
        updateTowers();
        updateProjectiles();
        checkWaveFinished();
    }

    private void updateEnemies() {
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            enemy.move();

            if (enemy.hasReachedBase()) {
                lives--;
                enemies.remove(i);

                if (lives <= 0) {
                    lives = 0;
                    gameOver = true;
                    waveRunning = false;
                    scoreManager.addScore(score);
                    scoreManager.saveScores();
                }
            } else if (!enemy.isAlive()) {
                money += enemy.getReward();
                score += enemy.getReward() * 10;
                enemies.remove(i);
            }
        }
    }

    private void sortEnemiesByDanger() {
        enemies.sort(Comparator.comparingInt(Enemy::getDangerScore).reversed());
    }

    private void updateTowers() {
        for (Tower tower : towers) {
            tower.updateCooldown();

            Enemy target = findTargetForTower(tower);

            if (target != null && tower.canShoot()) {
                projectiles.add(tower.shoot(target));
            }
        }
    }

    private Enemy findTargetForTower(Tower tower) {
        for (Enemy enemy : enemies) {
            if (tower.isInRange(enemy)) {
                return enemy;
            }
        }

        return null;
    }

    private void updateProjectiles() {
        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Projectile projectile = projectiles.get(i);
            Enemy target = projectile.getTarget();

            if (target == null || !target.isAlive() || target.hasReachedBase()) {
                projectiles.remove(i);
                continue;
            }

            projectile.move();

            if (projectile.hitTarget()) {
                projectile.damageTarget();
                projectiles.remove(i);
            }
        }
    }

    private void checkWaveFinished() {
        if (waveRunning && waveManager.finishedSpawning() && enemies.isEmpty()) {
            waveRunning = false;
            money += 35;
            startButton.setEnabled(true);

            boolean[][] visited = new boolean[Grid.ROWS][Grid.COLS];
            int grassTiles = grid.recursiveCountGrass(0, 0, visited);
            score += grassTiles;

            if (wave >= maxWave) {
                gameWon = true;
                scoreManager.addScore(score);
                scoreManager.saveScores();
                startButton.setEnabled(false);
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (gameOver || gameWon) return;

        int col = e.getX() / Grid.TILE_SIZE;
        int row = e.getY() / Grid.TILE_SIZE;

        Tile tile = grid.getTile(row, col);
        if (tile == null) return;

        if (SwingUtilities.isRightMouseButton(e)) {
            sellTower(tile);
            return;
        }

        if (tile.hasTower()) {
            selectedTower = tile.getTower();
            return;
        }

        buildTower(tile);
    }

    private void buildTower(Tile tile) {
        if (!tile.canBuild()) return;

        Tower tower;

        if (selectedTowerType.equals("Sniper")) {
            tower = new SniperTower(tile.getRow(), tile.getCol());
        } else if (selectedTowerType.equals("Rapid")) {
            tower = new RapidTower(tile.getRow(), tile.getCol());
        } else {
            tower = new BasicTower(tile.getRow(), tile.getCol());
        }

        if (money < tower.getCost()) return;

        money -= tower.getCost();
        towers.add(tower);
        tile.setTower(tower);
    }

    private void sellTower(Tile tile) {
        if (!tile.hasTower()) return;

        Tower tower = tile.getTower();
        money += tower.getSellValue();
        towers.remove(tower);
        tile.setTower(null);

        if (selectedTower == tower) {
            selectedTower = null;
        }
    }

    private void updateLabels() {
        statusLabel.setText("Money: $" + money + " | Lives: " + lives + " | Wave: " + wave + "/" + maxWave +
                " | Score: " + score + " | Best: " + scoreManager.getBestScore());

        if (selectedTower == null) {
            infoLabel.setText("Build: " + selectedTowerType + " | Left click build/select | Right click sell");
        } else {
            infoLabel.setText("Selected: " + selectedTower.getName() +
                    " Lv." + selectedTower.getLevel() +
                    " Damage: " + selectedTower.getDamage() +
                    " Range: " + selectedTower.getRange() +
                    " Upgrade: $" + selectedTower.getUpgradeCost());
        }
    }

    private void drawGame(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawGrid(g);
        drawTowers(g);
        drawEnemies(g);
        drawProjectiles(g);
        drawOverlay(g);
    }

    private void drawGrid(Graphics2D g) {
        Tile[][] tiles = grid.getTiles();

        for (int row = 0; row < Grid.ROWS; row++) {
            for (int col = 0; col < Grid.COLS; col++) {
                Tile tile = tiles[row][col];

                if (tile.getType() == Tile.GRASS) {
                    g.setColor((row + col) % 2 == 0 ? new Color(76, 154, 83) : new Color(66, 139, 76));
                } else if (tile.getType() == Tile.START) {
                    g.setColor(new Color(70, 170, 210));
                } else if (tile.getType() == Tile.BASE) {
                    g.setColor(new Color(190, 70, 70));
                } else {
                    g.setColor(new Color(174, 132, 72));
                }

                g.fillRect(col * Grid.TILE_SIZE, row * Grid.TILE_SIZE, Grid.TILE_SIZE, Grid.TILE_SIZE);
                g.setColor(new Color(255, 255, 255, 35));
                g.drawRect(col * Grid.TILE_SIZE, row * Grid.TILE_SIZE, Grid.TILE_SIZE, Grid.TILE_SIZE);
            }
        }
    }

    private void drawTowers(Graphics2D g) {
        for (Tower tower : towers) {
            if (tower == selectedTower) {
                g.setColor(new Color(255, 255, 255, 90));
                g.fillOval(tower.getX() - tower.getRange(), tower.getY() - tower.getRange(),
                        tower.getRange() * 2, tower.getRange() * 2);
            }

            g.setColor(new Color(20, 30, 45));
            g.fillOval(tower.getX() - 21, tower.getY() - 21, 42, 42);

            g.setColor(tower.getColor());
            g.fillOval(tower.getX() - 15, tower.getY() - 15, 30, 30);

            g.setColor(Color.WHITE);
            g.drawString("L" + tower.getLevel(), tower.getX() - 8, tower.getY() + 5);
        }
    }

    private void drawEnemies(Graphics2D g) {
        for (Enemy enemy : enemies) {
            g.setColor(enemy.getColor());
            g.fillOval((int) enemy.getX() - enemy.getSize() / 2, (int) enemy.getY() - enemy.getSize() / 2,
                    enemy.getSize(), enemy.getSize());

            g.setColor(Color.BLACK);
            g.drawOval((int) enemy.getX() - enemy.getSize() / 2, (int) enemy.getY() - enemy.getSize() / 2,
                    enemy.getSize(), enemy.getSize());

            g.setColor(Color.DARK_GRAY);
            g.fillRect((int) enemy.getX() - 18, (int) enemy.getY() - 28, 36, 6);

            g.setColor(Color.GREEN);
            g.fillRect((int) enemy.getX() - 18, (int) enemy.getY() - 28,
                    (int) (36 * enemy.getHealthPercent()), 6);
        }
    }

    private void drawProjectiles(Graphics2D g) {
        for (Projectile projectile : projectiles) {
            g.setColor(projectile.getColor());
            g.fillOval((int) projectile.getX() - 5, (int) projectile.getY() - 5, 10, 10);
        }
    }

    private void drawOverlay(Graphics2D g) {
        if (!gameOver && !gameWon) return;

        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, Grid.COLS * Grid.TILE_SIZE, Grid.ROWS * Grid.TILE_SIZE);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 45));
        g.drawString(gameWon ? "YOU WIN" : "GAME OVER", gameWon ? 300 : 260, 230);

        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.drawString("Final Score: " + score, 310, 280);
        g.drawString("Press Reset to play again", 275, 315);
    }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}