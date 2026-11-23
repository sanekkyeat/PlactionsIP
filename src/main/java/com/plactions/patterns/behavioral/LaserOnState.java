package com.plactions.patterns.behavioral;

import java.awt.Color;

public class LaserOnState implements LaserState {
    @Override
    public boolean isDangerous() {
        return true;
    }

    @Override
    public Color getColor() {
        return Color.RED;
    }

    @Override
    public LaserState toggle() {
        return new LaserOffState();
    }
}