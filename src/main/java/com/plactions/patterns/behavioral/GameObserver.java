package com.plactions.patterns.behavioral;

public interface GameObserver {
    void onPlayerDied();
    void onLevelCompleted();
    void onOutOfActions();
}