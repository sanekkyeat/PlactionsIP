package com.plactions.patterns.behavioral;

import com.plactions.model.Player;

public class DashCommand implements ActionCommand {
    private final int direction; // -1 влево, +1 вправо
    private int prevX;
    private int prevY;

    public DashCommand(int direction) {
        this.direction = direction;
    }

    @Override
    public void execute(Player player) {
        prevX = player.getX();
        prevY = player.getY();
        player.move(direction * 2, 0); // Рывок на 2 клетки
    }

    @Override
    public void undo(Player player) {
        player.setPosition(prevX, prevY);
    }

    @Override
    public String getName() {
        return direction > 0 ? "DASH →" : "DASH ←";
    }
}