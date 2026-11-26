package com.plactions.patterns.structural;

import java.awt.Toolkit;

public class SoundAdapter implements SoundPlayer {
    private int soundVolume = 100;
    private int musicVolume = 50;

    public int getSoundVolume() { return soundVolume; }
    public void setSoundVolume(int volume) {
        this.soundVolume = Math.max(0, Math.min(100, volume));
    }

    public int getMusicVolume() { return musicVolume; }
    public void setMusicVolume(int volume) {
        this.musicVolume = Math.max(0, Math.min(100, volume));
    }

    @Override
    public void playAction() {
        if (soundVolume > 0) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    @Override
    public void playDeath() {
        if (soundVolume > 0) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    @Override
    public void playWin() {
        if (soundVolume > 0) {
            Toolkit.getDefaultToolkit().beep();
        }
    }
}