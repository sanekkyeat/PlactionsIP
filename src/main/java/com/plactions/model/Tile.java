package com.plactions.model;

import com.plactions.patterns.structural.LevelComponent;
import java.awt.Color;
import java.awt.Graphics2D;

public abstract class Tile implements LevelComponent {
    protected int x;
    protected int y;
    protected Color color;

    public Tile(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public abstract boolean isSolid();
    public abstract boolean isDeadly();
    public abstract boolean isExit();

    @Override
    public void update() {
        // По умолчанию тайл статичен
    }

    @Override
    public void render(Graphics2D g, int tileSize) {
        g.setColor(color);
        g.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
    }
}