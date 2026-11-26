package com.plactions.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class Player {
    public enum PlayerState {
        GROUNDED, IN_AIR, DEAD
    }

    private int x;
    private int y;
    private double renderX;
    private double renderY;
    private PlayerState state;

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.renderX = startX;
        this.renderY = startY;
        this.state = PlayerState.GROUNDED;
    }

    public void move(int dx, int dy) {
        if (state != PlayerState.DEAD) {
            this.x += dx;
            this.y += dy;
        }
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void snapToGrid() {
        this.renderX = this.x;
        this.renderY = this.y;
    }

    public void updateSmoothPosition() {
        // Плавная интерполяция (Lerp) для 60 FPS
        renderX += (x - renderX) * 0.35;
        renderY += (y - renderY) * 0.35;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public PlayerState getState() { return state; }
    public void setState(PlayerState state) { this.state = state; }

    public void render(Graphics2D g, int tileSize) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(state == PlayerState.DEAD ? Color.GRAY : Color.WHITE);

        int margin = tileSize / 6;
        int size = tileSize - margin * 2;
        int px = (int) Math.round(renderX * tileSize) + margin;
        int py = (int) Math.round(renderY * tileSize) + margin;

        g.fillRoundRect(px, py, size, size, 10, 10);

        // Глаза персонажа как в Plactions
        if (state != PlayerState.DEAD) {
            g.setColor(Color.BLACK);
            int eyeW = Math.max(4, size / 6);
            int eyeH = Math.max(8, size / 3);
            g.fillRoundRect(px + size / 4, py + size / 4, eyeW, eyeH, 3, 3);
            g.fillRoundRect(px + size / 2 + 2, py + size / 4, eyeW, eyeH, 3, 3);
        }
    }
}