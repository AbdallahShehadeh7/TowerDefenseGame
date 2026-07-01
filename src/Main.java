import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Main extends JPanel implements ActionListener, MouseListener {
    static final int TILE = 50, COLS = 16, ROWS = 10, WIDTH = 800, HEIGHT = 500, MAX_WAVE = 10;

    Timer timer = new Timer(16, this);
    ArrayList<Point> path = new ArrayList<>();
    ArrayList<Enemy> enemies = new ArrayList<>();
    ArrayList<Tower> towers = new ArrayList<>();
    ArrayList<Bullet> bullets = new ArrayList<>();

    int money = 140, lives = 8, wave = 0, enemiesLeft = 0, spawnTimer = 0, spawned = 0;
    boolean waveRunning = false, gameOver = false, gameWon = false, paused = false;

    Tower selectedTower = null;
    TowerType selectedType = TowerType.BASIC;

    JButton basicButton = new JButton("Basic $60");
    JButton sniperButton = new JButton("Sniper $90");
    JButton rapidButton = new JButton("Rapid $75");
    JButton upgradeButton = new JButton("Upgrade");
    JButton startButton = new JButton("Start Wave");
    JButton pauseButton = new JButton("Pause");
    JButton resetButton = new JButton("Reset");
    JLabel status = new JLabel();
    JLabel info = new JLabel();

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tower Defense Game");
        Main game = new Main();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(game, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(2, 1));
        JPanel buttons = new JPanel();
        JPanel labels = new JPanel();

        buttons.add(game.basicButton);
        buttons.add(game.sniperButton);
        buttons.add(game.rapidButton);
        buttons.add(game.upgradeButton);
        buttons.add(game.startButton);
        buttons.add(game.pauseButton);
        buttons.add(game.resetButton);

        labels.add(game.status);
        labels.add(game.info);

        bottom.add(buttons);
        bottom.add(labels);
        frame.add(bottom, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public Main() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        addMouseListener(this);
        makePath();

        basicButton.addActionListener(e -> chooseTower(TowerType.BASIC));
        sniperButton.addActionListener(e -> chooseTower(TowerType.SNIPER));
        rapidButton.addActionListener(e -> chooseTower(TowerType.RAPID));
        upgradeButton.addActionListener(e -> upgradeTower());
        startButton.addActionListener(e -> startWave());
        pauseButton.addActionListener(e -> pauseGame());
        resetButton.addActionListener(e -> resetGame());

        resetGame();
        timer.start();
    }

    void makePath() {
        addPath(0,4); addPath(1,4); addPath(2,4); addPath(3,4); addPath(4,4); addPath(5,4);
        addPath(6,4); addPath(6,5); addPath(6,6); addPath(7,6); addPath(8,6); addPath(9,6);
        addPath(10,6); addPath(11,6); addPath(12,6); addPath(12,5); addPath(12,4);
        addPath(13,4); addPath(14,4); addPath(15,4);
    }

    void addPath(int col, int row) {
        path.add(new Point(col * TILE + TILE / 2, row * TILE + TILE / 2));
    }

    void resetGame() {
        money = 140;
        lives = 8;
        wave = 0;
        enemiesLeft = 0;
        spawnTimer = 0;
        spawned = 0;
        waveRunning = false;
        gameOver = false;
        gameWon = false;
        paused = false;
        selectedTower = null;
        selectedType = TowerType.BASIC;

        enemies.clear();
        towers.clear();
        bullets.clear();

        startButton.setEnabled(true);
        pauseButton.setText("Pause");
        updateText();
        repaint();
    }

    void chooseTower(TowerType type) {
        selectedType = type;
        selectedTower = null;
        beep();
        updateText();
    }

    void startWave() {
        if (waveRunning || gameOver || gameWon) return;

        wave++;
        enemiesLeft = 9 + wave * 5;
        spawned = 0;
        spawnTimer = 0;
        waveRunning = true;
        paused = false;
        startButton.setEnabled(false);
        pauseButton.setText("Pause");
        beep();
    }

    void pauseGame() {
        if (!waveRunning || gameOver || gameWon) return;
        paused = !paused;
        pauseButton.setText(paused ? "Resume" : "Pause");
    }

    void upgradeTower() {
        if (selectedTower == null || selectedTower.level >= 3) return;

        int cost = selectedTower.upgradeCost();
        if (money < cost) return;

        money -= cost;
        selectedTower.upgrade();
        beep();
        updateText();
    }

    void updateText() {
        String text = "Money: $" + money + "   Lives: " + lives + "   Wave: " + wave + "/" + MAX_WAVE;
        if (paused) text += "   PAUSED";
        if (gameOver) text += "   GAME OVER";
        if (gameWon) text += "   YOU WIN";
        status.setText(text);

        if (selectedTower == null) {
            info.setText("Build: " + selectedType.name + " $" + selectedType.cost + " | Left click build | Right click sell");
        } else {
            info.setText("Selected: " + selectedTower.type.name + " Lv." + selectedTower.level +
                    " | Damage " + selectedTower.damage +
                    " | Upgrade $" + selectedTower.upgradeCost() +
                    " | Sell $" + selectedTower.sellAmount());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!paused && !gameOver && !gameWon) {
            spawnEnemies();
            updateEnemies();
            updateTowers();
            updateBullets();
            checkWaveEnd();
        }

        updateText();
        repaint();
    }

    void spawnEnemies() {
        if (!waveRunning || enemiesLeft <= 0) return;

        if (spawnTimer > 0) {
            spawnTimer--;
            return;
        }

        enemies.add(createEnemy());
        enemiesLeft--;
        spawned++;
        spawnTimer = Math.max(14, 38 - wave * 2);
    }

    Enemy createEnemy() {
        if (wave >= 4 && spawned % 7 == 0) return new Enemy(path, EnemyType.TANK, wave);
        if (wave >= 2 && spawned % 3 == 0) return new Enemy(path, EnemyType.FAST, wave);
        return new Enemy(path, EnemyType.NORMAL, wave);
    }

    void updateEnemies() {
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            enemy.move();

            if (enemy.reachedEnd) {
                lives--;
                enemies.remove(i);

                if (lives <= 0) {
                    lives = 0;
                    gameOver = true;
                    waveRunning = false;
                    startButton.setEnabled(false);
                    beep();
                }
            } else if (enemy.health <= 0) {
                money += enemy.reward;
                enemies.remove(i);
                beep();
            }
        }
    }

    void updateTowers() {
        for (Tower tower : towers) {
            tower.cooldown--;

            Enemy target = null;
            int bestProgress = -1;

            for (Enemy enemy : enemies) {
                if (tower.distanceTo(enemy) <= tower.range && enemy.targetIndex > bestProgress) {
                    target = enemy;
                    bestProgress = enemy.targetIndex;
                }
            }

            if (target != null && tower.cooldown <= 0) {
                bullets.add(new Bullet(tower.x, tower.y, target, tower.damage, tower.type.bulletColor));
                tower.cooldown = tower.cooldownTime;
            }
        }
    }

    void updateBullets() {
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);

            if (bullet.target.health <= 0 || bullet.target.reachedEnd) {
                bullets.remove(i);
                continue;
            }

            bullet.move();

            if (bullet.hitTarget()) {
                bullet.target.health -= bullet.damage;
                bullets.remove(i);
            }
        }
    }

    void checkWaveEnd() {
        if (waveRunning && enemiesLeft == 0 && enemies.isEmpty()) {
            waveRunning = false;
            money += 30;
            startButton.setEnabled(true);

            if (wave >= MAX_WAVE) {
                gameWon = true;
                startButton.setEnabled(false);
                beep();
            }
        }
    }

    boolean isPathTile(int row, int col) {
        for (Point p : path) {
            if (p.x / TILE == col && p.y / TILE == row) return true;
        }
        return false;
    }

    Tower getTowerAt(int row, int col) {
        for (Tower tower : towers) {
            if (tower.x / TILE == col && tower.y / TILE == row) return tower;
        }
        return null;
    }

    void sellTower(int row, int col) {
        Tower tower = getTowerAt(row, col);
        if (tower == null) return;

        money += tower.sellAmount();
        towers.remove(tower);
        if (selectedTower == tower) selectedTower = null;
        beep();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (gameOver || gameWon) return;

        int col = e.getX() / TILE;
        int row = e.getY() / TILE;

        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) return;

        if (SwingUtilities.isRightMouseButton(e)) {
            sellTower(row, col);
            return;
        }

        Tower tower = getTowerAt(row, col);
        if (tower != null) {
            selectedTower = tower;
            updateText();
            return;
        }

        selectedTower = null;

        if (isPathTile(row, col)) return;
        if (money < selectedType.cost) return;

        towers.add(new Tower(col * TILE + TILE / 2, row * TILE + TILE / 2, selectedType));
        money -= selectedType.cost;
        beep();
    }

    void beep() {
        Toolkit.getDefaultToolkit().beep();
    }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBoard(g2);
        drawTowers(g2);
        drawEnemies(g2);
        drawBullets(g2);
        drawEndScreen(g2);
    }

    void drawBoard(Graphics2D g) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (isPathTile(row, col)) g.setColor(new Color(174, 132, 72));
                else if ((row + col) % 2 == 0) g.setColor(new Color(76, 154, 83));
                else g.setColor(new Color(66, 139, 76));

                g.fillRect(col * TILE, row * TILE, TILE, TILE);
                g.setColor(new Color(255, 255, 255, 28));
                g.drawRect(col * TILE, row * TILE, TILE, TILE);
            }
        }

        g.setColor(new Color(104, 72, 35));
        for (Point p : path) {
            g.drawRect(p.x - TILE / 2 + 4, p.y - TILE / 2 + 4, TILE - 8, TILE - 8);
        }
    }

    void drawTowers(Graphics2D g) {
        for (Tower tower : towers) {
            if (tower == selectedTower) {
                g.setColor(new Color(255, 255, 255, 90));
                g.fillOval(tower.x - tower.range, tower.y - tower.range, tower.range * 2, tower.range * 2);
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(3));
                g.drawOval(tower.x - 23, tower.y - 23, 46, 46);
            }

            g.setColor(new Color(20, 30, 45));
            g.fillOval(tower.x - 21, tower.y - 21, 42, 42);

            g.setColor(tower.type.color);
            g.fillOval(tower.x - 15, tower.y - 15, 30, 30);

            g.setColor(new Color(8, 20, 38));
            g.fillRoundRect(tower.x - 6, tower.y - 31, 12, 29, 6, 6);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("L" + tower.level, tower.x - 8, tower.y + 5);
        }
    }

    void drawEnemies(Graphics2D g) {
        for (Enemy enemy : enemies) {
            g.setColor(enemy.type.darkColor);
            g.fillOval((int) enemy.x - enemy.type.size / 2 - 3, (int) enemy.y - enemy.type.size / 2 - 3,
                    enemy.type.size + 6, enemy.type.size + 6);

            g.setColor(enemy.type.color);
            g.fillOval((int) enemy.x - enemy.type.size / 2, (int) enemy.y - enemy.type.size / 2,
                    enemy.type.size, enemy.type.size);

            g.setColor(Color.BLACK);
            g.drawOval((int) enemy.x - enemy.type.size / 2, (int) enemy.y - enemy.type.size / 2,
                    enemy.type.size, enemy.type.size);

            g.setColor(new Color(40, 40, 40));
            g.fillRect((int) enemy.x - 18, (int) enemy.y - 29, 36, 7);

            g.setColor(new Color(80, 230, 80));
            int healthBar = Math.max(0, enemy.health * 34 / enemy.maxHealth);
            g.fillRect((int) enemy.x - 17, (int) enemy.y - 28, healthBar, 5);
        }
    }

    void drawBullets(Graphics2D g) {
        for (Bullet bullet : bullets) {
            g.setColor(bullet.color);
            g.fillOval((int) bullet.x - 5, (int) bullet.y - 5, 10, 10);
            g.setColor(Color.BLACK);
            g.drawOval((int) bullet.x - 5, (int) bullet.y - 5, 10, 10);
        }
    }

    void drawEndScreen(Graphics2D g) {
        if (!gameOver && !gameWon) return;

        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 46));
        g.drawString(gameWon ? "YOU WIN" : "GAME OVER", gameWon ? 300 : 260, 220);

        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.drawString("Final wave reached: " + wave, 285, 270);
        g.drawString("Click Reset to play again", 280, 305);
    }

    enum TowerType {
        BASIC("Basic", 60, 130, 24, 38, new Color(55, 126, 220), new Color(255, 235, 80)),
        SNIPER("Sniper", 90, 210, 55, 78, new Color(133, 72, 190), new Color(220, 190, 255)),
        RAPID("Rapid", 75, 115, 14, 16, new Color(235, 153, 45), new Color(255, 190, 60));

        String name;
        int cost, range, damage, cooldown;
        Color color, bulletColor;

        TowerType(String name, int cost, int range, int damage, int cooldown, Color color, Color bulletColor) {
            this.name = name;
            this.cost = cost;
            this.range = range;
            this.damage = damage;
            this.cooldown = cooldown;
            this.color = color;
            this.bulletColor = bulletColor;
        }
    }

    enum EnemyType {
        NORMAL(1.0, 1.0, 12, 28, new Color(218, 58, 58), new Color(105, 20, 20)),
        FAST(0.7, 1.65, 16, 24, new Color(235, 205, 55), new Color(126, 96, 10)),
        TANK(2.6, 0.68, 28, 36, new Color(120, 75, 45), new Color(58, 34, 22));

        double healthMultiplier, speedMultiplier;
        int reward, size;
        Color color, darkColor;

        EnemyType(double healthMultiplier, double speedMultiplier, int reward, int size, Color color, Color darkColor) {
            this.healthMultiplier = healthMultiplier;
            this.speedMultiplier = speedMultiplier;
            this.reward = reward;
            this.size = size;
            this.color = color;
            this.darkColor = darkColor;
        }
    }

    static class Enemy {
        ArrayList<Point> path;
        EnemyType type;
        double x, y, speed;
        int health, maxHealth, reward, targetIndex = 1;
        boolean reachedEnd = false;

        Enemy(ArrayList<Point> path, EnemyType type, int wave) {
            this.path = path;
            this.type = type;
            maxHealth = (int) ((78 + wave * 30) * type.healthMultiplier);
            health = maxHealth;
            speed = (1.05 + wave * 0.11) * type.speedMultiplier;
            reward = type.reward;
            x = path.get(0).x;
            y = path.get(0).y;
        }

        void move() {
            if (targetIndex >= path.size()) {
                reachedEnd = true;
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
    }

    static class Tower {
        int x, y, range, damage, cooldownTime, cooldown, level = 1;
        TowerType type;

        Tower(int x, int y, TowerType type) {
            this.x = x;
            this.y = y;
            this.type = type;
            range = type.range;
            damage = type.damage;
            cooldownTime = type.cooldown;
        }

        void upgrade() {
            if (level >= 3) return;
            level++;
            damage += 12 + type.damage / 3;
            range += 18;
            cooldownTime = Math.max(8, cooldownTime - 5);
        }

        int upgradeCost() {
            if (level >= 3) return 0;
            return type.cost / 2 + level * 35;
        }

        int sellAmount() {
            return type.cost / 2 + (level - 1) * 25;
        }

        double distanceTo(Enemy enemy) {
            double dx = enemy.x - x;
            double dy = enemy.y - y;
            return Math.sqrt(dx * dx + dy * dy);
        }
    }

    static class Bullet {
        double x, y, speed = 9;
        Enemy target;
        int damage;
        Color color;

        Bullet(double x, double y, Enemy target, int damage, Color color) {
            this.x = x;
            this.y = y;
            this.target = target;
            this.damage = damage;
            this.color = color;
        }

        void move() {
            double dx = target.x - x;
            double dy = target.y - y;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance > 0) {
                x += dx / distance * speed;
                y += dy / distance * speed;
            }
        }

        boolean hitTarget() {
            double dx = target.x - x;
            double dy = target.y - y;
            return Math.sqrt(dx * dx + dy * dy) < 11;
        }
    }
}