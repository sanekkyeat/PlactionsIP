package com.plactions.model;

import java.awt.Color;
import java.awt.Graphics2D;

public class ExitTile extends Tile {
    public ExitTile(int x, int y) {
        super(x, y, Color.CYAN);
    }

    @Override public boolean isSolid() { return false; }
    @Override public boolean isDeadly() { return false; }
    @Override public boolean isExit() { return true; }

    @Override
    public void render(Graphics2D g, int tileSize) {
        g.setColor(color);
        int margin = tileSize / 6;
        g.drawOval(x * tileSize + margin, y * tileSize + margin,
                tileSize - margin * 2, tileSize - margin * 2);
        g.drawOval(x * tileSize + margin * 2, y * tileSize + margin * 2,
                tileSize - margin * 4, tileSize - margin * 4);
    }
}