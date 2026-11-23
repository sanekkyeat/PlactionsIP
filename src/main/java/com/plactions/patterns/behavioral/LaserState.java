package com.plactions.patterns.behavioral;

import java.awt.Color;

public interface LaserState {
    boolean isDangerous();
    Color getColor();
    LaserState toggle();
}