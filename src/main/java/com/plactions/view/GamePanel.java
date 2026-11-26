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

    // Вспомогательный класс для хранения карты уровня и набора команд для него
    private record LevelData(String[] map, List<ActionCommand> commands) {}

    private final List<LevelData> levels = List.of(
            // УРОВЕНЬ 1: Учимся перепрыгивать шипы и забираться на уступ
            // Прохождение: подойти к стене -> SPACE (JUMP) -> SPACE (DASH ->) -> пройти вправо -> SPACE (JUMP) -> SPACE (DASH ->) -> выход
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
                    List.of(
                            new JumpCommand(),
                            new DashCommand(1),
                            new JumpCommand(),
                            new DashCommand(1)
                    )
            ),

            // УРОВЕНЬ 2: Лазер (State ON/OFF) + шипы
            // Прохождение: дождаться пока лазер станет тёмным (OFF) -> пройти через него -> перепрыгнуть шипы к выходу
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
                    List.of(
                            new JumpCommand(),
                            new DashCommand(1),
                            new JumpCommand(),
                            new DashCommand(1)
                    )
            ),

            // УРОВЕНЬ 3: Подъём зигзагом (используем рывок вправо и рывок влево)
            // Прохождение: забраться на 1-й этаж вправо -> развернуться и забраться на 2-й этаж влево к выходу
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
                            new JumpCommand(),
                            new DashCommand(1),
                            new JumpCommand(),
                            new DashCommand(-1),
                            new JumpCommand(),
                            new DashCommand(-1)
                    )
            )
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

        // Создаём свежие экземпляры команд для уровня
        actionQueue.setCommands(createCommandsForLevel(levelNumber));
    }

    private List<ActionCommand> createCommandsForLevel(int levelNumber) {
        int index = (levelNumber - 1) % levels.size();
        return switch (index) {
            case 0, 1 -> List.of(
                    new JumpCommand(),
                    new DashCommand(1),
                    new JumpCommand(),
                    new DashCommand(1)
            );
            case 2 -> List.of(
                    new JumpCommand(),
                    new DashCommand(1),
                    new JumpCommand(),
                    new DashCommand(-1),
                    new JumpCommand(),
                    new DashCommand(-1)
            );
            default -> List.of(new JumpCommand(), new DashCommand(1));
        };
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
            if (actionQueue.executeNext(player)) {
                soundPlayer.playAction();
                // Даём игроку зависнуть в воздухе на ~0.5 сек, чтобы успеть нажать Dash после Jump
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
        levelComposite.update();

        if (player.getState() == Player.PlayerState.DEAD) return;

        // Гравитация с задержкой после прыжка/рывка
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

        levelComposite.render(g2d, TILE_SIZE);
        player.render(g2d, TILE_SIZE);

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