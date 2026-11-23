package com.plactions.patterns.structural;

import java.awt.Graphics2D;

public interface LevelComponent {
    void render(Graphics2D g, int tileSize);
    void update();
}