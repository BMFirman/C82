package com.chip8.emulator;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

class InputHandler implements KeyListener {

    private static final boolean[] keys = new boolean[256];

    public InputHandler(Component c) {
        c.addKeyListener(this);
    }

    public boolean isKeyDown(int keyCode) {
        if (keyCode > 0 && keyCode < 256) {
            return keys[keyCode];
        }

        return false;
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() > 0 && e.getKeyCode() < 256) {
            keys[e.getKeyCode()] = true;
        }
    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() > 0 && e.getKeyCode() < 256) {
            keys[e.getKeyCode()] = false;
        }
    }

    public void keyTyped(KeyEvent e) {}
}