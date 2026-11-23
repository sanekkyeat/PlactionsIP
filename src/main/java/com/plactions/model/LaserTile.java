package com.plactions.model;

import com.plactions.patterns.behavioral.LaserOnState;
import com.plactions.patterns.behavioral.LaserState;
import java.awt.Graphics2D;

public class LaserTile extends Tile {
    private LaserState state;
    private int tickCounter = 0;
    private static final int TOGGLE_INTERVAL = 90; // Переключение каждые ~1.5 сек

    public LaserTile(int x, int y) {
        super(x, y, null);
        this.state = new LaserOnState();
    }

    public void toggleState() {
        this.state = state.toggle();
    }

    public LaserState getState() {
        return state;
    }

    @Override public boolean isSolid() { return false; }
    @Override public boolean isDeadly() { return state.isDangerous(); }
    @Override public boolean isExit() { return false; }

    @Override
    public void update() {
        tickCounter++;
        if (tickCounter >= TOGGLE_INTERVAL) {
            toggleState();
            tickCounter = 0;
        }
    }

    @Override
    public void render(Graphics2D g, int tileSize) {
        g.setColor(state.getColor());
        int beamWidth = tileSize / 4;
        int offset = (tileSize - beamWidth) / 2;
        g.fillRect(x * tileSize + offset, y * tileSize, beamWidth, tileSize);
    }
}