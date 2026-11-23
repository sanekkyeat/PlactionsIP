package com.plactions.patterns.behavioral;

import java.awt.Color;

public class LaserOffState implements LaserState {
    @Override
    public boolean isDangerous() {
        return false;
    }

    @Override
    public Color getColor() {
        return new Color(80, 0, 0);
    }

    @Override
    public LaserState toggle() {
        return new LaserOnState();
    }
}