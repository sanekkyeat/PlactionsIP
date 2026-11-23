package com.plactions.model;

import java.awt.Color;
import java.awt.Graphics2D;

public class WallTile extends Tile {
    public WallTile(int x, int y) {
        super(x, y, new Color(0, 220, 0));
    }

    @Override public boolean isSolid() { return true; }
    @Override public boolean isDeadly() { return false; }
    @Override public boolean isExit() { return false; }

    @Override
    public void render(Graphics2D g, int tileSize) {
        g.setColor(Color.BLACK);
        g.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
        g.setColor(color);
        g.drawRect(x * tileSize, y * tileSize, tileSize - 1, tileSize - 1);
    }
}