package com.plactions.view;

import com.plactions.model.Player;
import com.plactions.model.Tile;
import com.plactions.patterns.behavioral.*;
import com.plactions.patterns.creational.LevelFactory;
import com.plactions.patterns.structural.SoundAdapter;
import com.plactions.patterns.structural.TileGroup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.List;

public class GamePanel extends JPanel {
    private static final int TILE_SIZE = 48;
    private static final int ROWS = 10;
    private static final int COLS = 16;

    // Состояния экранов игры (State Pattern на уровне UI)
    private enum GameScreen {
        MENU, LEVEL_SELECT, PLAYING
    }

    private GameScreen currentScreen = GameScreen.MENU;

    // Состояние главного меню
    private int menuIndex = 0; // 0 = Start, 1 = Sound, 2 = Music

    // Состояние экрана выбора уровней (сетка 6x4 = 24 уровня)
    private static final int TOTAL_SLOTS = 24;
    private int selectedLevelSlot = 1; // 1..24
    private int unlockedLevels = 3;    // Сколько уровней открыто по умолчанию (1..3 пройдены/открыты, 4 текущий)
    private int completedLevels = 0;   // Сколько пройдено в текущей сессии

    private TileGroup levelComposite;
    private Tile[][] grid;
    private Player player;
    private final ActionQueue actionQueue = new ActionQueue();
    private final LevelManager levelManager = new LevelManager();
    private final SoundAdapter soundAdapter = new SoundAdapter();
    private int gravityCounter = 0;

    private record LevelData(String[] map, List<ActionCommand> commands) {}

    private final List<LevelData> levels = List.of(
            // УРОВЕНЬ 1: Базовые прыжки и рывки через шипы
            new LevelData(
                    new String[]{
                            "WWWWWWWWWWWWWWWW",
                            "WAAAAAAAAAAAAAAW",
                            "WAAAAAAAAAAAAEAW",
                            "WAAAAAAAAAAWWWWW",
                            "WAAAAAAAAAAAAAAW",
                            "WAAAAAASSAAAAAAW",
                            "WPAAAAWWWWAAAAAW",
                            "WWWWWWWWWWWWWWWW",
                            "WWWWWWWWWWWWWWWW",
                            "WWWWWWWWWWWWWWWW"
                    },
                    List.of(new JumpCommand(), new DashCommand(1), new JumpCommand(), new DashCommand(1))
            ),
            // УРОВЕНЬ 2: Переключающийся лазер (State ON/OFF)
            new LevelData(
                    new String[]{
                            "WWWWWWWWWWWWWWWW",
                            "WAAAAALAAAAAAAAW",
                            "WAAAAALAAAAAAAAW",
                            "WAAAAALAAAAAAEAW",
                            "WAAAAALAAAAWWWWW",
                            "WPAAAALASSAAAAAW",
                            "WWWWWWWWWWWWWWWW",
                            "WWWWWWWWWWWWWWWW",
                            "WWWWWWWWWWWWWWWW",
                            "WWWWWWWWWWWWWWWW"
                    },
                    List.of(new JumpCommand(), new DashCommand(1), new JumpCommand(), new DashCommand(1))
            ),
            // УРОВЕНЬ 3: Зигзаг со сменой направления (Dash Right -> Dash Left)
            new LevelData(
                    new String[]{
                            "WWWWWWWWWWWWWWWW",
                            "WAEAAAAAAAAAAAAW",
                            "WWWWWAAAAAAAAAAW",
                            "WAAAAAAAAAAAAAAW",
                            "WAAAAAAAWWWWWWWW",
                            "WAAAAAAAAAAAAAAW",
                            "WPAAASSAAAAAAAAW",
                            "WWWWWWWWWWWWWWWW",
                            "WWWWWWWWWWWWWWWW",
                            "WWWWWWWWWWWWWWWW"
                    },
                    List.of(
                            new JumpCommand(), new DashCommand(1),
                            new JumpCommand(), new DashCommand(-1),
                            new JumpCommand(), new DashCommand(-1)
                    )
            ),
            // УРОВЕНЬ 4: Двойной лазерный коридор
            new LevelData(
                    new String[]{
                            "WWWWWWWWWWWWWWWW",
                            "WAAALAAAAAALAAAW",
                            "WAAALAAAAAALAEAW",
                            "WAAALAAAAAALWWWW",
                            "WAAALAAAAAAAAAAW",
                            "WPAALAAASSAAAAAW",
                            "WWWWWWWWWWWWWWWW",
                            "WWWWWWWWWWWWWWWW",
                            "WWWWWWWWWWWWWWWW",
                            "WWWWWWWWWWWWWWWW"
                    },
                    List.of(new JumpCommand(), new DashCommand(1), new JumpCommand(), new DashCommand(1))
            )
    );

    public GamePanel() {
        setPreferredSize(new Dimension(COLS * TILE_SIZE, ROWS * TILE_SIZE + 60));
        setBackground(Color.BLACK);
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleInput(e.getKeyCode());
            }
        });

        // Плавный игровой цикл 60 FPS
        Timer timer = new Timer(16, e -> {
            updateGame();
            repaint();
        });
        timer.start();
    }

    private void loadLevel(int levelNumber) {
        levelComposite = new TileGroup();
        grid = new Tile[ROWS][COLS];
        levelManager.clearResetFlag();
        levelManager.clearNextLevelFlag();
        levelManager.resetStatus();
        gravityCounter = 0;

        LevelData data = levels.get((levelNumber - 1) % levels.size());
        String[] map = data.map();
        int startX = 1, startY = 1;

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                char symbol = map[r].charAt(c);
                if (symbol == 'P') {
                    startX = c;
                    startY = r;
                }
                Tile tile = LevelFactory.createTile(symbol, c, r);
                grid[r][c] = tile;
                levelComposite.add(tile);
            }
        }

        player = new Player(startX, startY);
        player.snapToGrid();
        actionQueue.setCommands(createCommandsForLevel(levelNumber));
    }

    private List<ActionCommand> createCommandsForLevel(int levelNumber) {
        int index = (levelNumber - 1) % levels.size();
        return switch (index) {
            case 0, 1, 3 -> List.of(
                    new JumpCommand(), new DashCommand(1),
                    new JumpCommand(), new DashCommand(1)
            );
            case 2 -> List.of(
                    new JumpCommand(), new DashCommand(1),
                    new JumpCommand(), new DashCommand(-1),
                    new JumpCommand(), new DashCommand(-1)
            );
            default -> List.of(new JumpCommand(), new DashCommand(1));
        };
    }

    private void handleInput(int keyCode) {
        switch (currentScreen) {
            case MENU -> handleMenuInput(keyCode);
            case LEVEL_SELECT -> handleLevelSelectInput(keyCode);
            case PLAYING -> handlePlayingInput(keyCode);
        }
    }

    // --- УПРАВЛЕНИЕ В ГЛАВНОМ МЕНЮ ---
    private void handleMenuInput(int keyCode) {
        if (keyCode == KeyEvent.VK_UP) {
            menuIndex = (menuIndex + 2) % 3;
            soundAdapter.playAction();
        } else if (keyCode == KeyEvent.VK_DOWN) {
            menuIndex = (menuIndex + 1) % 3;
            soundAdapter.playAction();
        } else if (keyCode == KeyEvent.VK_LEFT) {
            if (menuIndex == 1) soundAdapter.setSoundVolume(soundAdapter.getSoundVolume() - 10);
            if (menuIndex == 2) soundAdapter.setMusicVolume(soundAdapter.getMusicVolume() - 10);
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            if (menuIndex == 1) soundAdapter.setSoundVolume(soundAdapter.getSoundVolume() + 10);
            if (menuIndex == 2) soundAdapter.setMusicVolume(soundAdapter.getMusicVolume() + 10);
        } else if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
            if (menuIndex == 0) {
                soundAdapter.playAction();
                currentScreen = GameScreen.LEVEL_SELECT;
            }
        }
    }

    // --- УПРАВЛЕНИЕ НА ЭКРАНЕ ВЫБОРА УРОВНЯ ---
    private void handleLevelSelectInput(int keyCode) {
        int maxSelectable = Math.min(6, unlockedLevels + 1); // 1..6 доступные уровни в демо

        if (keyCode == KeyEvent.VK_ESCAPE) {
            currentScreen = GameScreen.MENU;
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            if (selectedLevelSlot < maxSelectable) selectedLevelSlot++;
        } else if (keyCode == KeyEvent.VK_LEFT) {
            if (selectedLevelSlot > 1) selectedLevelSlot--;
        } else if (keyCode == KeyEvent.VK_DOWN) {
            if (selectedLevelSlot + 6 <= maxSelectable) selectedLevelSlot += 6;
        } else if (keyCode == KeyEvent.VK_UP) {
            if (selectedLevelSlot - 6 >= 1) selectedLevelSlot -= 6;
        } else if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
            soundAdapter.playAction();
            loadLevel(selectedLevelSlot);
            currentScreen = GameScreen.PLAYING;
        }
    }

    // --- УПРАВЛЕНИЕ В ИГРЕ ---
    private void handlePlayingInput(int keyCode) {
        if (keyCode == KeyEvent.VK_ESCAPE) {
            currentScreen = GameScreen.LEVEL_SELECT;
            return;
        }
        if (keyCode == KeyEvent.VK_R) {
            loadLevel(selectedLevelSlot);
            return;
        }
        if (player.getState() == Player.PlayerState.DEAD) return;

        if (keyCode == KeyEvent.VK_LEFT) {
            tryMove(-1, 0);
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            tryMove(1, 0);
        } else if (keyCode == KeyEvent.VK_SPACE) {
            if (actionQueue.executeNext(player)) {
                soundAdapter.playAction();
                gravityCounter = -18;
                clampPlayerPosition();
                checkTileInteractions();
            } else {
                levelManager.onOutOfActions();
            }
        } else if (keyCode == KeyEvent.VK_Z) {
            actionQueue.undoLast(player);
            gravityCounter = -15;
            levelManager.resetStatus();
        }
    }

    private void tryMove(int dx, int dy) {
        int nextX = player.getX() + dx;
        int nextY = player.getY() + dy;
        if (nextX >= 0 && nextX < COLS && nextY >= 0 && nextY < ROWS) {
            if (!grid[nextY][nextX].isSolid()) {
                player.move(dx, dy);
                checkTileInteractions();
            }
        }
    }

    private void clampPlayerPosition() {
        int x = Math.max(1, Math.min(COLS - 2, player.getX()));
        int y = Math.max(1, Math.min(ROWS - 2, player.getY()));
        if (grid[y][x].isSolid()) {
            y = Math.max(1, y - 1);
        }
        player.setPosition(x, y);
    }

    private void updateGame() {
        if (currentScreen != GameScreen.PLAYING) return;

        levelComposite.update();
        player.updateSmoothPosition(); // Плавная анимация 60 FPS

        if (player.getState() == Player.PlayerState.DEAD) return;

        gravityCounter++;
        if (gravityCounter >= 14) {
            gravityCounter = 0;
            int belowY = player.getY() + 1;
            if (belowY < ROWS && !grid[belowY][player.getX()].isSolid()) {
                player.move(0, 1);
                player.setState(Player.PlayerState.IN_AIR);
            } else {
                player.setState(Player.PlayerState.GROUNDED);
            }
        }

        checkTileInteractions();

        if (levelManager.isNextLevelNeeded()) {
            completedLevels = Math.max(completedLevels, selectedLevelSlot);
            unlockedLevels = Math.min(6, Math.max(unlockedLevels, selectedLevelSlot + 1));
            selectedLevelSlot = Math.min(6, selectedLevelSlot + 1);
            loadLevel(selectedLevelSlot);
        }
    }

    private void checkTileInteractions() {
        Tile currentTile = grid[player.getY()][player.getX()];
        if (currentTile.isDeadly()) {
            player.setState(Player.PlayerState.DEAD);
            soundAdapter.playDeath();
            levelManager.onPlayerDied();
        } else if (currentTile.isExit()) {
            soundAdapter.playWin();
            levelManager.onLevelCompleted();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        switch (currentScreen) {
            case MENU -> renderMenu(g2d);
            case LEVEL_SELECT -> renderLevelSelect(g2d);
            case PLAYING -> renderPlaying(g2d);
        }
    }

    // --- ОТРИСОВКА ГЛАВНОГО МЕНЮ (КАК НА СКРИНШОТЕ ОРИГИНАЛА) ---
    private void renderMenu(Graphics2D g) {
        int w = getWidth();
        int h = getHeight();

        // Зелёная рамка по периметру
        g.setColor(new Color(0, 220, 0));
        g.setStroke(new BasicStroke(3));
        g.drawRect(2, 2, w - 5, h - 5);

        // Красный контурный логотип "Plactions"
        Font titleFont = new Font("SansSerif", Font.BOLD, 110);
        FontRenderContext frc = g.getFontRenderContext();
        Shape titleOutline = titleFont.createGlyphVector(frc, "Plactions").getOutline();
        Rectangle bounds = titleOutline.getBounds();

        AffineTransform transform = AffineTransform.getTranslateInstance(
                (w - bounds.width) / 2.0 - bounds.x,
                165
        );
        Shape centeredTitle = transform.createTransformedShape(titleOutline);

        g.setColor(Color.BLACK);
        g.fill(centeredTitle);
        g.setColor(new Color(230, 20, 20));
        g.setStroke(new BasicStroke(4f));
        g.draw(centeredTitle);

        // Пункты меню
        Font menuFont = new Font("Monospaced", Font.BOLD, 34);
        g.setFont(menuFont);

        String[] items = {
                "Start",
                "Sound <" + soundAdapter.getSoundVolume() + ">",
                "Music <" + soundAdapter.getMusicVolume() + ">"
        };

        int startX = 245;
        int startY = 290;
        int lineHeight = 65;

        for (int i = 0; i < items.length; i++) {
            int y = startY + i * lineHeight;
            if (i == menuIndex) {
                // Зелёная стрелка как в оригинале
                g.setColor(new Color(50, 210, 50));
                int ax = startX - 55;
                int ay = y - 14;
                g.fillRect(ax, ay - 5, 16, 10);
                int[] tx = {ax + 16, ax + 32, ax + 16};
                int[] ty = {ay - 13, ay, ay + 13};
                g.fillPolygon(tx, ty, 3);
            }
            g.setColor(Color.WHITE);
            g.drawString(items[i], startX, y);
        }

        // Подсказка управления внизу
        g.setColor(Color.GRAY);
        g.setFont(new Font("Monospaced", Font.PLAIN, 14));
        g.drawString("UP/DOWN: Select  |  LEFT/RIGHT: Volume  |  ENTER/SPACE: Confirm", 110, h - 20);
    }

    // --- ОТРИСОВКА ВЫБОРА УРОВНЕЙ (СЕТКА 6x4 КАК НА СКРИНШОТЕ) ---
    private void renderLevelSelect(Graphics2D g) {
        int w = getWidth();
        int h = getHeight();

        // Зелёная рамка по периметру
        g.setColor(new Color(0, 220, 0));
        g.setStroke(new BasicStroke(3));
        g.drawRect(2, 2, w - 5, h - 5);

        int cols = 6;
        int rows = 4;
        int boxSize = 86;
        int gap = 18;
        int gridW = cols * boxSize + (cols - 1) * gap;
        int gridH = rows * boxSize + (rows - 1) * gap;
        int offsetX = (w - gridW) / 2;
        int offsetY = (h - gridH) / 2 - 10;

        Font numFont = new Font("SansSerif", Font.BOLD, 38);
        g.setFont(numFont);

        for (int i = 0; i < TOTAL_SLOTS; i++) {
            int levelNum = i + 1;
            int r = i / cols;
            int c = i % cols;
            int x = offsetX + c * (boxSize + gap);
            int y = offsetY + r * (boxSize + gap);

            if (levelNum <= completedLevels || levelNum <= 3 && levelNum < selectedLevelSlot) {
                // Зелёный пройденный уровень
                g.setColor(new Color(55, 205, 50));
                g.fillRoundRect(x, y, boxSize, boxSize, 16, 16);
                drawCenteredNumber(g, String.valueOf(levelNum), x, y, boxSize);
            } else if (levelNum <= 6 && levelNum <= unlockedLevels + 1) {
                // Жёлтый открытый уровень
                g.setColor(new Color(235, 235, 20));
                g.fillRoundRect(x, y, boxSize, boxSize, 16, 16);
                drawCenteredNumber(g, String.valueOf(levelNum), x, y, boxSize);
            } else {
                // Красный закрытый уровень с чёрным крестиком X
                g.setColor(new Color(220, 30, 25));
                g.fillRoundRect(x, y, boxSize, boxSize, 16, 16);
                g.setColor(Color.BLACK);
                g.setStroke(new BasicStroke(11f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int pad = 24;
                g.drawLine(x + pad, y + pad, x + boxSize - pad, y + boxSize - pad);
                g.drawLine(x + boxSize - pad, y + pad, x + pad, y + boxSize - pad);
            }

            // Пунктирная чёрная рамка вокруг выбранного уровня (как на 4-ке на фото)
            if (levelNum == selectedLevelSlot) {
                g.setColor(Color.BLACK);
                float[] dash = {12f, 10f};
                g.setStroke(new BasicStroke(5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f));
                g.drawRoundRect(x + 3, y + 3, boxSize - 6, boxSize - 6, 14, 14);
            }
        }

        g.setColor(Color.GRAY);
        g.setFont(new Font("Monospaced", Font.PLAIN, 14));
        g.drawString("ARROWS: Choose Level  |  ENTER/SPACE: Play  |  ESC: Back to Menu", 105, h - 18);
    }

    private void drawCenteredNumber(Graphics2D g, String text, int x, int y, int boxSize) {
        g.setColor(Color.BLACK);
        FontMetrics fm = g.getFontMetrics();
        int tx = x + (boxSize - fm.stringWidth(text)) / 2;
        int ty = y + ((boxSize - fm.getHeight()) / 2) + fm.getAscent();
        g.drawString(text, tx, ty);
    }

    // --- ОТРИСОВКА ИГРОВОГО ПРОЦЕССА ---
    private void renderPlaying(Graphics2D g) {
        levelComposite.render(g, TILE_SIZE);
        player.render(g, TILE_SIZE);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        g.drawString("Level " + selectedLevelSlot + " | " + levelManager.getStatusMessage() + " | ESC: Levels", 15, ROWS * TILE_SIZE + 22);

        StringBuilder queueText = new StringBuilder("Action Queue: ");
        for (ActionCommand cmd : actionQueue.getRemainingCommands()) {
            queueText.append("[").append(cmd.getName()).append("] ");
        }
        if (actionQueue.isEmpty()) {
            queueText.append("(EMPTY)");
        }
        g.setColor(Color.YELLOW);
        g.drawString(queueText.toString(), 15, ROWS * TILE_SIZE + 44);
    }
}