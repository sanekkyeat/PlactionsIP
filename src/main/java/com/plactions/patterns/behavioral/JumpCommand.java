package com.plactions.patterns.behavioral;

import com.plactions.model.Player;

public class JumpCommand implements ActionCommand {
    private int prevX;
    private int prevY;

    @Override
    public void execute(Player player) {
        prevX = player.getX();
        prevY = player.getY();
        player.setState(Player.PlayerState.IN_AIR);
        player.move(0, -2); // Прыжок на 2 клетки вверх
    }

    @Override
    public void undo(Player player) {
        player.setPosition(prevX, prevY);
        player.setState(Player.PlayerState.GROUNDED);
    }

    @Override
    public String getName() {
        return "JUMP";
    }
}