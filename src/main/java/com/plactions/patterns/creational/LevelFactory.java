package com.plactions.patterns.creational;

import com.plactions.model.*;

public class LevelFactory {
    public static Tile createTile(char symbol, int x, int y) {
        return switch (symbol) {
            case 'W' -> new WallTile(x, y);
            case 'S' -> new SpikeTile(x, y);
            case 'E' -> new ExitTile(x, y);
            case 'L' -> new LaserTile(x, y);
            case 'A', 'P' -> new AirTile(x, y); // На стартовой позиции игрока под ним воздух
            default -> throw new IllegalArgumentException("Unknown tile symbol: " + symbol);
        };
    }
}