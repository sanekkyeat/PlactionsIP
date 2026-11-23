package com.plactions.patterns.structural;

import java.awt.Toolkit;

public class SoundAdapter implements SoundPlayer {
    @Override
    public void playAction() {
        Toolkit.getDefaultToolkit().beep();
    }

    @Override
    public void playDeath() {
        Toolkit.getDefaultToolkit().beep();
    }

    @Override
    public void playWin() {
        Toolkit.getDefaultToolkit().beep();
    }
}