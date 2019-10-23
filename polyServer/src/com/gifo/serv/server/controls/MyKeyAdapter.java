package com.gifo.serv.server.controls;

/**
 * Created by gifo.
 */

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import com.gifo.serv.server.Server;


public class MyKeyAdapter extends KeyAdapter {

    Server gfx; // Объект, который реализует методы MyKeyAdapter

    public MyKeyAdapter(Server obj) {
        gfx = obj;
    }

    // Реализация методов нажатия и отпускания клавиши
    public void keyPressed(KeyEvent e) {
        gfx.keyPressed(e.getKeyCode());
    }
    public void keyReleased(KeyEvent e) { gfx.keyReleased(e.getKeyCode()); }
}