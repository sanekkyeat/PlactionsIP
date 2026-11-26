package com.plactions;

import com.plactions.model.*;
import com.plactions.patterns.behavioral.*;
import com.plactions.patterns.creational.LevelFactory;
import com.plactions.patterns.structural.TileGroup;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlactionsPatternsTest {

    @Test
    void testLevelFactoryCreatesCorrectTiles() {
        assertInstanceOf(WallTile.class, LevelFactory.createTile('W', 0, 0));
        assertInstanceOf(SpikeTile.class, LevelFactory.createTile('S', 1, 0));
        assertInstanceOf(ExitTile.class, LevelFactory.createTile('E', 2, 0));
        assertInstanceOf(LaserTile.class, LevelFactory.createTile('L', 3, 0));
        assertInstanceOf(AirTile.class, LevelFactory.createTile('A', 4, 0));
    }

    @Test
    void testLaserStateToggle() {
        LaserTile laser = new LaserTile(2, 2);
        assertTrue(laser.isDeadly(), "Laser should start in ON (dangerous) state");

        laser.toggleState();
        assertFalse(laser.isDeadly(), "Laser should be safe after toggling to OFF");

        laser.toggleState();
        assertTrue(laser.isDeadly(), "Laser should be dangerous again after toggling back to ON");
    }

    @Test
    void testCommandQueueExecuteAndUndo() {
        Player player = new Player(5, 5);
        ActionQueue queue = new ActionQueue();
        queue.setCommands(List.of(new JumpCommand(), new DashCommand(1)));

        assertTrue(queue.executeNext(player));
        assertEquals(3, player.getY());

        assertTrue(queue.executeNext(player));
        assertEquals(7, player.getX());
        assertTrue(queue.isEmpty());

        assertTrue(queue.undoLast(player));
        assertEquals(5, player.getX());
        assertFalse(queue.isEmpty());
    }

    @Test
    void testCompositeTileGroup() {
        TileGroup group = new TileGroup();
        LaserTile laser = new LaserTile(1, 1);
        group.add(new WallTile(0, 0));
        group.add(laser);

        assertEquals(2, group.getChildren().size());
        for (int i = 0; i < 90; i++) {
            group.update();
        }
        assertFalse(laser.isDeadly());
    }

    @Test
    void testLevelManagerObserver() {
        LevelManager manager = new LevelManager();
        assertEquals(1, manager.getCurrentLevel());

        manager.onPlayerDied();
        assertTrue(manager.isLevelResetNeeded());

        manager.onLevelCompleted();
        assertEquals(2, manager.getCurrentLevel());
        assertTrue(manager.isNextLevelNeeded());
    }
}