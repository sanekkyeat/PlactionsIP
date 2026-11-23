package com.plactions.model;

import java.awt.Color;
import java.awt.Graphics2D;

public class AirTile extends Tile {
    public AirTile(int x, int y) {
        super(x, y, Color.BLACK);
    }

    @Override public boolean isSolid() { return false; }
    @Override public boolean isDeadly() { return false; }
    @Override public boolean isExit() { return false; }

    @Override
    public void render(Graphics2D g, int tileSize) {
        // Пустота не перекрывает фон
    }
}