package com.gifo.screen.controls;

/**
 * Created by gifo.
 */

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import com.gifo.screen.Screen; // Импорт основного класса текущего проекта


public class MyKeyAdapter extends KeyAdapter {

    Screen gfx; // Объект, который реализует методы MyKeyAdapter

    public MyKeyAdapter(Screen obj) {
        gfx = obj;
    }

    // Реализация методов нажатия и отпускания клавиши
    public void keyPressed(KeyEvent e) {
        gfx.keyPressed(e.getKeyCode());
    }
    public void keyReleased(KeyEvent e) { gfx.keyReleased(e.getKeyCode()); }
}
