import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Main extends JPanel implements ActionListener, MouseListener {
    static final int TILE = 50, COLS = 16, ROWS = 10;
    static final int WIDTH = COLS * TILE, HEIGHT = ROWS * TILE;
    static final int TOWER_COST = 60, SELL_AMOUNT = 35;

    Timer timer = new Timer(16, this);

    ArrayList<Point> path = new ArrayList<>();
    ArrayList<Enemy> enemies = new ArrayList<>();
    ArrayList<Tower> towers = new ArrayList<>();
    ArrayList<Bullet> bullets = new ArrayList<>();

    int money = 110, lives = 8, wave = 0;
    int enemiesLeftToSpawn = 0, spawnTimer = 0;
    boolean waveRunning = false, gameOver = false;

    JButton startButton = new JButton("Start Wave");
    JButton resetButton = new JButton("Reset");
    JLabel status = new JLabel();

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tower Defense Game");
        Main game = new Main();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(game, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setBackground(new Color(232, 236, 230));
        bottom.add(game.status);
        bottom.add(game.startButton);
        bottom.add(game.resetButton);
        frame.add(bottom, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public Main() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        addMouseListener(this);
        makePath();

        startButton.addActionListener(e -> startWave());
        resetButton.addActionListener(e -> resetGame());

        resetGame();
        timer.start();
    }

    void makePath() {
        addPath(0, 4); addPath(1, 4); addPath(2, 4); addPath(3, 4);
        addPath(4, 4); addPath(5, 4); addPath(6, 4); addPath(6, 5);
        addPath(6, 6); addPath(7, 6); addPath(8, 6); addPath(9, 6);
        addPath(10, 6); addPath(11, 6); addPath(12, 6); addPath(12, 5);
        addPath(12, 4); addPath(13, 4); addPath(14, 4); addPath(15, 4);
    }

    void addPath(int col, int row) {
        path.add(new Point(col * TILE + TILE / 2, row * TILE + TILE / 2));
    }

    void resetGame() {
        money = 110;
        lives = 8;
        wave = 0;
        enemiesLeftToSpawn = 0;
        spawnTimer = 0;
        waveRunning = false;
        gameOver = false;

        enemies.clear();
        towers.clear();
        bullets.clear();

        startButton.setEnabled(true);
        updateStatus();
        repaint();
    }

    void startWave() {
        if (waveRunning || gameOver) return;

        wave++;
        enemiesLeftToSpawn = 8 + wave * 4;
        spawnTimer = 0;
        waveRunning = true;
        startButton.setEnabled(false);
    }

    void updateStatus() {
        String text = "Money: $" + money + "   Lives: " + lives + "   Wave: " + wave
                + "   Tower: $" + TOWER_COST + "   Sell: $" + SELL_AMOUNT
                + "   Left click = build   Right click = sell";
        if (gameOver) text += "   GAME OVER";
        status.setText(text);
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if (!gameOver) {
            spawnEnemies();
            updateEnemies();
            updateTowers();
            updateBullets();
            checkWaveFinished();
        }

        updateStatus();
        repaint();
    }

    void spawnEnemies() {
        if (!waveRunning || enemiesLeftToSpawn <= 0) return;

        if (spawnTimer > 0) {
            spawnTimer--;
            return;
        }

        enemies.add(new Enemy(path, 75 + wave * 28, 1.15 + wave * 0.12));
        enemiesLeftToSpawn--;
        spawnTimer = Math.max(20, 42 - wave * 2);
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
                    startButton.setEnabled(false);
                }
            } else if (enemy.health <= 0) {
                money += 12;
                enemies.remove(i);
            }
        }
    }

    void updateTowers() {
        for (Tower tower : towers) {
            tower.cooldown--;

            Enemy target = null;
            for (Enemy enemy : enemies) {
                if (tower.distanceTo(enemy) <= tower.range) {
                    target = enemy;
                    break;
                }
            }

            if (target != null && tower.cooldown <= 0) {
                bullets.add(new Bullet(tower.x, tower.y, target));
                tower.cooldown = 38;
            }
        }
    }

    void updateBullets() {
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.move();

            if (bullet.target.health <= 0 || bullet.target.reachedEnd) {
                bullets.remove(i);
            } else if (bullet.hitTarget()) {
                bullet.target.health -= 22;
                bullets.remove(i);
            }
        }
    }

    void checkWaveFinished() {
        if (waveRunning && enemiesLeftToSpawn == 0 && enemies.isEmpty()) {
            waveRunning = false;
            money += 25;
            startButton.setEnabled(true);
        }
    }

    boolean isPathTile(int row, int col) {
        for (Point p : path) {
            if (p.x / TILE == col && p.y / TILE == row) return true;
        }
        return false;
    }

    boolean hasTower(int row, int col) {
        for (Tower tower : towers) {
            if (tower.x / TILE == col && tower.y / TILE == row) return true;
        }
        return false;
    }

    void sellTower(int row, int col) {
        for (int i = towers.size() - 1; i >= 0; i--) {
            Tower tower = towers.get(i);
            if (tower.x / TILE == col && tower.y / TILE == row) {
                towers.remove(i);
                money += SELL_AMOUNT;
                return;
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (gameOver) return;

        int col = e.getX() / TILE;
        int row = e.getY() / TILE;

        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) return;

        if (SwingUtilities.isRightMouseButton(e)) {
            sellTower(row, col);
            return;
        }

        if (money < TOWER_COST) return;
        if (isPathTile(row, col)) return;
        if (hasTower(row, col)) return;

        towers.add(new Tower(col * TILE + TILE / 2, row * TILE + TILE / 2));
        money -= TOWER_COST;
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

        for (Tower tower : towers) {
            g.setColor(new Color(38, 88, 175, 35));
            g.fillOval(tower.x - tower.range, tower.y - tower.range, tower.range * 2, tower.range * 2);

            g.setColor(new Color(22, 44, 84));
            g.fillOval(tower.x - 20, tower.y - 20, 40, 40);

            g.setColor(new Color(55, 126, 220));
            g.fillOval(tower.x - 15, tower.y - 15, 30, 30);

            g.setColor(new Color(8, 20, 38));
            g.fillRoundRect(tower.x - 6, tower.y - 30, 12, 28, 6, 6);
        }

        for (Enemy enemy : enemies) {
            g.setColor(new Color(105, 20, 20));
            g.fillOval((int) enemy.x - 18, (int) enemy.y - 18, 36, 36);

            g.setColor(new Color(218, 58, 58));
            g.fillOval((int) enemy.x - 14, (int) enemy.y - 14, 28, 28);

            g.setColor(Color.BLACK);
            g.drawOval((int) enemy.x - 16, (int) enemy.y - 16, 32, 32);

            g.setColor(new Color(40, 40, 40));
            g.fillRect((int) enemy.x - 17, (int) enemy.y - 27, 34, 7);

            g.setColor(Color.GREEN);
            int healthBar = Math.max(0, enemy.health * 30 / enemy.maxHealth);
            g.fillRect((int) enemy.x - 15, (int) enemy.y - 26, healthBar, 5);
        }

        for (Bullet bullet : bullets) {
            g.setColor(new Color(255, 245, 120));
            g.fillOval((int) bullet.x - 5, (int) bullet.y - 5, 10, 10);

            g.setColor(new Color(255, 170, 30));
            g.drawOval((int) bullet.x - 5, (int) bullet.y - 5, 10, 10);
        }

        if (gameOver) {
            g.setColor(new Color(0, 0, 0, 160));
            g.fillRect(0, 0, WIDTH, HEIGHT);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 45));
            g.drawString("GAME OVER", 260, 250);
        }
    }

    static class Enemy {
        ArrayList<Point> path;
        double x, y, speed;
        int health, maxHealth;
        int targetIndex = 1;
        boolean reachedEnd = false;

        Enemy(ArrayList<Point> path, int health, double speed) {
            this.path = path;
            this.health = health;
            this.maxHealth = health;
            this.speed = speed;
            this.x = path.get(0).x;
            this.y = path.get(0).y;
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
        int x, y;
        int range = 130;
        int cooldown = 0;

        Tower(int x, int y) {
            this.x = x;
            this.y = y;
        }

        double distanceTo(Enemy enemy) {
            double dx = enemy.x - x;
            double dy = enemy.y - y;
            return Math.sqrt(dx * dx + dy * dy);
        }
    }

    static class Bullet {
        double x, y;
        Enemy target;
        double speed = 8;

        Bullet(double x, double y, Enemy target) {
            this.x = x;
            this.y = y;
            this.target = target;
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
            return Math.sqrt(dx * dx + dy * dy) < 10;
        }
    }
}