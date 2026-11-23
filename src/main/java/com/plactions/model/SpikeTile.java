package com.plactions.model;

import java.awt.Color;
import java.awt.Graphics2D;

public class SpikeTile extends Tile {
    public SpikeTile(int x, int y) {
        super(x, y, Color.WHITE);
    }

    @Override public boolean isSolid() { return false; }
    @Override public boolean isDeadly() { return true; }
    @Override public boolean isExit() { return false; }

    @Override
    public void render(Graphics2D g, int tileSize) {
        g.setColor(color);
        int px = x * tileSize;
        int py = y * tileSize;
        int[] xPoints = {px, px + tileSize / 2, px + tileSize};
        int[] yPoints = {py + tileSize, py + tileSize / 3, py + tileSize};
        g.fillPolygon(xPoints, yPoints, 3);
    }
}