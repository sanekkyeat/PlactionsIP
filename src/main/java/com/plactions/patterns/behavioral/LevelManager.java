package com.plactions.patterns.behavioral;

public class LevelManager implements GameObserver {
    private int currentLevel = 1;
    private String statusMessage = "Level 1 - Use Arrows to move, SPACE for Action, Z for Undo";
    private boolean levelResetNeeded = false;
    private boolean nextLevelNeeded = false;

    @Override
    public void onPlayerDied() {
        statusMessage = "YOU DIED! Press R to Restart";
        levelResetNeeded = true;
    }

    @Override
    public void onLevelCompleted() {
        statusMessage = "LEVEL " + currentLevel + " COMPLETED!";
        currentLevel++;
        nextLevelNeeded = true;
    }

    @Override
    public void onOutOfActions() {
        statusMessage = "OUT OF ACTIONS! Press Z to Undo or R to Restart";
    }

    public String getStatusMessage() { return statusMessage; }
    public int getCurrentLevel() { return currentLevel; }

    public boolean isLevelResetNeeded() { return levelResetNeeded; }
    public void clearResetFlag() { levelResetNeeded = false; }

    public boolean isNextLevelNeeded() { return nextLevelNeeded; }
    public void clearNextLevelFlag() { nextLevelNeeded = false; }

    public void resetStatus() {
        statusMessage = "Level " + currentLevel + " - Arrows: Move | SPACE: Act | Z: Undo | R: Reset";
    }
}