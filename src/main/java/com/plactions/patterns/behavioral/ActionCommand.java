package com.plactions.patterns.behavioral;

import com.plactions.model.Player;

public interface ActionCommand {
    void execute(Player player);
    void undo(Player player);
    String getName();
}