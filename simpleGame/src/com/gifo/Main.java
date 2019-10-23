package com.gifo;

/**
 * Created by gifo.
 */

import javax.swing.*;
import com.gifo.screen.Screen; // Импорт основного класса текущего проекта


public class Main {

    public static void main (String[] args) {
        JFrame frame = new JFrame(WINDOW.title);
        frame.setIconImage(new ImageIcon(WINDOW.icon).getImage());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setSize(WINDOW.W,WINDOW.H);
        frame.add(new Screen(5));
        frame.setVisible(true);
    }
}