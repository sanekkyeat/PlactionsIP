package com.plactions.model;

import java.awt.Color;
import java.awt.Graphics2D;

public class Player {
    public enum PlayerState {
        GROUNDED, IN_AIR, DEAD
    }

    private int x;
    private int y;
    private PlayerState state;

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
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

    public int getX() { return x; }
    public int getY() { return y; }

    public PlayerState getState() { return state; }
    public void setState(PlayerState state) { this.state = state; }

    public void render(Graphics2D g, int tileSize) {
        g.setColor(state == PlayerState.DEAD ? Color.GRAY : Color.WHITE);
        int margin = tileSize / 6;
        int size = tileSize - margin * 2;
        g.fillRoundRect(x * tileSize + margin, y * tileSize + margin, size, size, 8, 8);

        // Глаза персонажа как в Plactions
        if (state != PlayerState.DEAD) {
            g.setColor(Color.BLACK);
            int eyeW = size / 6;
            int eyeH = size / 3;
            g.fillRect(x * tileSize + margin + size / 4, y * tileSize + margin + size / 4, eyeW, eyeH);
            g.fillRect(x * tileSize + margin + size / 2 + 2, y * tileSize + margin + size / 4, eyeW, eyeH);
        }
    }
}