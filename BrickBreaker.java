import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;

/**
 * Brick Breaker - a simple Java Swing game.
 *
 * Compile: javac BrickBreaker.java
 * Run:     java BrickBreaker
 *
 * Controls:
 *   Left/Right arrows or A/D  - move paddle
 *   Space                     - launch ball / continue
 *   R                         - restart
 */
public class BrickBreaker extends JPanel implements ActionListener, KeyListener {

    // ---- Window / layout constants ----
    static final int W = 800, H = 600;
    static final int PADDLE_W = 110, PADDLE_H = 14, PADDLE_Y = H - 50;
    static final int BALL_SIZE = 14;
    static final int ROWS = 6, COLS = 10;
    static final int BRICK_W = 72, BRICK_H = 24, GAP = 4;
    static final int BRICKS_LEFT = (W - (COLS * BRICK_W + (COLS - 1) * GAP)) / 2;
    static final int BRICKS_TOP = 70;
    static final double PADDLE_SPEED = 9;

    enum State { READY, PLAYING, GAME_OVER, LEVEL_CLEAR }

    // ---- Game state ----
    private final Timer timer = new Timer(16, this); // ~60 FPS
    private State state = State.READY;
    private boolean leftDown, rightDown;

    private double paddleX;
    private double ballX, ballY, ballVX, ballVY;
    private double ballSpeed;

    private int[][] bricks = new int[ROWS][COLS]; // value = hits remaining (0 = empty)
    private int bricksLeft;
    private int score, lives, level;

    public BrickBreaker() {
        setPreferredSize(new Dimension(W, H));
        setBackground(new Color(18, 18, 30));
        setFocusable(true);
        addKeyListener(this);
        newGame();
        timer.start();
    }

    // ---- Setup ----
    private void newGame() {
        score = 0;
        lives = 3;
        level = 1;
        startLevel();
    }

    private void startLevel() {
        ballSpeed = 5.5 + (level - 1) * 0.7;
        buildBricks();
        resetBall();
    }

    private void buildBricks() {
        bricksLeft = 0;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                // Higher levels make the top rows tougher (2 hits)
                int hp = (level > 1 && r < Math.min(level - 1, ROWS)) ? 2 : 1;
                bricks[r][c] = hp;
                bricksLeft++;
            }
        }
    }

    private void resetBall() {
        paddleX = (W - PADDLE_W) / 2.0;
        ballX = paddleX + (PADDLE_W - BALL_SIZE) / 2.0;
        ballY = PADDLE_Y - BALL_SIZE;
        ballVX = 0;
        ballVY = 0;
        state = State.READY;
    }

    private void launchBall() {
        double angle = Math.toRadians(-20 + Math.random() * 40); // slight random angle
        ballVX = ballSpeed * Math.sin(angle);
        ballVY = -ballSpeed * Math.cos(angle);
        state = State.PLAYING;
    }

    // ---- Game loop ----
    @Override
    public void actionPerformed(ActionEvent e) {
        update();
        repaint();
    }

    private void update() {
        // Paddle movement (allowed in READY and PLAYING)
        if (state == State.READY || state == State.PLAYING) {
            if (leftDown) paddleX -= PADDLE_SPEED;
            if (rightDown) paddleX += PADDLE_SPEED;
            paddleX = Math.max(0, Math.min(W - PADDLE_W, paddleX));
        }

        if (state == State.READY) {
            // Ball rides on the paddle until launched
            ballX = paddleX + (PADDLE_W - BALL_SIZE) / 2.0;
            ballY = PADDLE_Y - BALL_SIZE;
            return;
        }
        if (state != State.PLAYING) return;

        ballX += ballVX;
        ballY += ballVY;

        // Walls and ceiling
        if (ballX <= 0) { ballX = 0; ballVX = Math.abs(ballVX); }
        if (ballX + BALL_SIZE >= W) { ballX = W - BALL_SIZE; ballVX = -Math.abs(ballVX); }
        if (ballY <= 0) { ballY = 0; ballVY = Math.abs(ballVY); }

        // Fell below the paddle
        if (ballY > H) {
            lives--;
            if (lives <= 0) state = State.GAME_OVER;
            else resetBall();
            return;
        }

        handlePaddleCollision();
        handleBrickCollision();

        if (bricksLeft == 0) state = State.LEVEL_CLEAR;
    }

    private void handlePaddleCollision() {
        Rectangle2D ball = new Rectangle2D.Double(ballX, ballY, BALL_SIZE, BALL_SIZE);
        Rectangle2D paddle = new Rectangle2D.Double(paddleX, PADDLE_Y, PADDLE_W, PADDLE_H);

        if (ballVY > 0 && ball.intersects(paddle)) {
            // Where the ball hits the paddle decides the bounce angle (-1 .. 1)
            double hit = (ballX + BALL_SIZE / 2.0 - (paddleX + PADDLE_W / 2.0)) / (PADDLE_W / 2.0);
            hit = Math.max(-1, Math.min(1, hit));
            double angle = hit * Math.toRadians(60);
            ballVX = ballSpeed * Math.sin(angle);
            ballVY = -ballSpeed * Math.cos(angle);
            ballY = PADDLE_Y - BALL_SIZE;
        }
    }

    private void handleBrickCollision() {
        Rectangle2D ball = new Rectangle2D.Double(ballX, ballY, BALL_SIZE, BALL_SIZE);

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (bricks[r][c] == 0) continue;

                Rectangle2D brick = brickRect(r, c);
                if (!ball.intersects(brick)) continue;

                // Work out which side was hit by comparing overlap depth
                double overlapX = Math.min(ball.getMaxX(), brick.getMaxX()) - Math.max(ball.getX(), brick.getX());
                double overlapY = Math.min(ball.getMaxY(), brick.getMaxY()) - Math.max(ball.getY(), brick.getY());
                if (overlapX < overlapY) ballVX = -ballVX;
                else ballVY = -ballVY;

                bricks[r][c]--;
                score += 10 * level;
                if (bricks[r][c] == 0) {
                    bricksLeft--;
                    score += 10 * level;
                }
                return; // only one brick per frame
            }
        }
    }

    private Rectangle2D brickRect(int r, int c) {
        return new Rectangle2D.Double(
                BRICKS_LEFT + c * (BRICK_W + GAP),
                BRICKS_TOP + r * (BRICK_H + GAP),
                BRICK_W, BRICK_H);
    }

    // ---- Rendering ----
    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Bricks
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (bricks[r][c] == 0) continue;
                Rectangle2D b = brickRect(r, c);
                Color base = Color.getHSBColor(r / (float) ROWS * 0.8f, 0.65f, 0.95f);
                if (bricks[r][c] > 1) base = base.darker(); // tougher bricks look darker
                g.setColor(base);
                g.fillRoundRect((int) b.getX(), (int) b.getY(), BRICK_W, BRICK_H, 8, 8);
                g.setColor(new Color(255, 255, 255, 60));
                g.fillRoundRect((int) b.getX() + 3, (int) b.getY() + 3, BRICK_W - 6, 6, 4, 4);
            }
        }

        // Paddle
        g.setColor(new Color(90, 200, 250));
        g.fillRoundRect((int) paddleX, PADDLE_Y, PADDLE_W, PADDLE_H, 12, 12);

        // Ball
        g.setColor(Color.WHITE);
        g.fillOval((int) ballX, (int) ballY, BALL_SIZE, BALL_SIZE);

        // HUD
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, 20, 30);
        g.drawString("Level: " + level, W / 2 - 35, 30);
        g.drawString("Lives: " + lives, W - 110, 30);

        // Overlay messages
        switch (state) {
            case READY -> centerText(g, "Press SPACE to launch", "Move with ← → or A / D");
            case GAME_OVER -> centerText(g, "GAME OVER", "Score " + score + "  -  press R to play again");
            case LEVEL_CLEAR -> centerText(g, "LEVEL " + level + " CLEARED!", "Press SPACE for the next level");
            default -> { }
        }
    }

    private void centerText(Graphics2D g, String title, String subtitle) {
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRoundRect(W / 2 - 230, H / 2 - 20, 460, 100, 20, 20);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 28));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (W - fm.stringWidth(title)) / 2, H / 2 + 20);

        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        fm = g.getFontMetrics();
        g.drawString(subtitle, (W - fm.stringWidth(subtitle)) / 2, H / 2 + 55);
    }

    // ---- Input ----
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT, KeyEvent.VK_A -> leftDown = true;
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> rightDown = true;
            case KeyEvent.VK_SPACE -> {
                if (state == State.READY) launchBall();
                else if (state == State.LEVEL_CLEAR) { level++; startLevel(); }
            }
            case KeyEvent.VK_R -> newGame();
            default -> { }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT, KeyEvent.VK_A -> leftDown = false;
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> rightDown = false;
            default -> { }
        }
    }

    @Override public void keyTyped(KeyEvent e) { }

    // ---- Entry point ----
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Brick Breaker");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.add(new BrickBreaker());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
