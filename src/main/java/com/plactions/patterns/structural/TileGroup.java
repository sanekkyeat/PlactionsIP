package com.plactions.patterns.structural;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

public class TileGroup implements LevelComponent {
    private final List<LevelComponent> children = new ArrayList<>();

    public void add(LevelComponent component) {
        children.add(component);
    }

    public void remove(LevelComponent component) {
        children.remove(component);
    }

    public List<LevelComponent> getChildren() {
        return children;
    }

    @Override
    public void render(Graphics2D g, int tileSize) {
        for (LevelComponent component : children) {
            component.render(g, tileSize);
        }
    }

    @Override
    public void update() {
        for (LevelComponent component : children) {
            component.update();
        }
    }
}