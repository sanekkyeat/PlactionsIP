package com.plactions.view;

import com.plactions.model.Player;
import com.plactions.model.Tile;
import com.plactions.patterns.behavioral.*;
import com.plactions.patterns.creational.LevelFactory;
import com.plactions.patterns.structural.SoundAdapter;
import com.plactions.patterns.structural.SoundPlayer;
import com.plactions.patterns.structural.TileGroup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {
    private static final int TILE_SIZE = 48;
    private static final int ROWS = 10;
    private static final int COLS = 16;

    private TileGroup levelComposite;
    private Tile[][] grid;
    private Player player;
    private final ActionQueue actionQueue = new ActionQueue();
    private final LevelManager levelManager = new LevelManager();
    private final SoundPlayer soundPlayer = new SoundAdapter();
    private int gravityCounter = 0;

    // Карты уровней (ровно по 16 символов в каждой строке)
    private final List<String[]> levels = List.of(
            new String[]{
                    "WWWWWWWWWWWWWWWW",
                    "WAAAAAAAAAAAAAAW",
                    "WAAAAAAAAAAAAEAW",
                    "WAAAAAAAAAAWWWWW",
                    "WAAAAAAAAAAAAAAW",
                    "WAAAAAASSSAAAAAW",
                    "WPAAAAWWWWAAAAAW",
                    "WWWWWWWWWWWWWWWW",
                    "WWWWWWWWWWWWWWWW",
                    "WWWWWWWWWWWWWWWW"
            },
            new String[]{
                    "WWWWWWWWWWWWWWWW",
                    "WAAAAAAALAAAAAAW",
                    "WAAAAAAALAAAAEAW",
                    "WAAAAAAALAAWWWWW",
                    "WAAAAAAAAAAAAAAW",
                    "WPAAAASSSSAAAAAW",
                    "WWWWWWWWWWWWWWWW",
                    "WWWWWWWWWWWWWWWW",
                    "WWWWWWWWWWWWWWWW",
                    "WWWWWWWWWWWWWWWW"
            }
    );

    public GamePanel() {
        setPreferredSize(new Dimension(COLS * TILE_SIZE, ROWS * TILE_SIZE + 60));
        setBackground(Color.BLACK);
        setFocusable(true);

        loadLevel(1);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleInput(e.getKeyCode());
            }
        });

        // Игровой таймер (~60 FPS)
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

        String[] map = levels.get((levelNumber - 1) % levels.size());
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

        // Набор доступных команд на уровень (главная фишка Plactions)
        List<ActionCommand> commands = new ArrayList<>();
        commands.add(new JumpCommand());
        commands.add(new DashCommand(1));
        commands.add(new JumpCommand());
        commands.add(new DashCommand(1));
        actionQueue.setCommands(commands);
    }

    private void handleInput(int keyCode) {
        if (keyCode == KeyEvent.VK_R) {
            loadLevel(levelManager.getCurrentLevel());
            return;
        }

        if (player.getState() == Player.PlayerState.DEAD) return;

        if (keyCode == KeyEvent.VK_LEFT) {
            tryMove(-1, 0);
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            tryMove(1, 0);
        } else if (keyCode == KeyEvent.VK_SPACE) {
            // Выполнить следующее действие из очереди Command
            if (actionQueue.executeNext(player)) {
                soundPlayer.playAction();
                clampPlayerPosition();
                checkTileInteractions();
            } else {
                levelManager.onOutOfActions();
            }
        } else if (keyCode == KeyEvent.VK_Z) {
            // Undo последнего действия
            actionQueue.undoLast(player);
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
        levelComposite.update(); // Обновляем лазеры (State) через Composite

        if (player.getState() == Player.PlayerState.DEAD) return;

        // Простая гравитация
        gravityCounter++;
        if (gravityCounter >= 15) {
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
            loadLevel(levelManager.getCurrentLevel());
        }
    }

    private void checkTileInteractions() {
        Tile currentTile = grid[player.getY()][player.getX()];
        if (currentTile.isDeadly()) {
            player.setState(Player.PlayerState.DEAD);
            soundPlayer.playDeath();
            levelManager.onPlayerDied();
        } else if (currentTile.isExit()) {
            soundPlayer.playWin();
            levelManager.onLevelCompleted();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Отрисовка всего уровня через паттерн Composite
        levelComposite.render(g2d, TILE_SIZE);

        // Отрисовка игрока
        player.render(g2d, TILE_SIZE);

        // Нижняя панель интерфейса (очередь команд и статус)
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2d.drawString(levelManager.getStatusMessage(), 15, ROWS * TILE_SIZE + 22);

        StringBuilder queueText = new StringBuilder("Action Queue: ");
        for (ActionCommand cmd : actionQueue.getRemainingCommands()) {
            queueText.append("[").append(cmd.getName()).append("] ");
        }
        if (actionQueue.isEmpty()) {
            queueText.append("(EMPTY)");
        }
        g2d.setColor(Color.YELLOW);
        g2d.drawString(queueText.toString(), 15, ROWS * TILE_SIZE + 44);
    }
}