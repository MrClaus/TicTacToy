package com.gifo.serv;

/**
 * Created by gifo.
 */

import javax.swing.*;
import com.gifo.serv.server.Server; // Импорт основного класса текущего проекта


public class Main {

    public static void main (String[] args) {
        JFrame frame = new JFrame(WINDOW.title);
        frame.setIconImage(new ImageIcon(WINDOW.icon).getImage());
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setResizable(false);
        frame.setSize(WINDOW.W,WINDOW.H);
        frame.add(new Server("setServer.txt"));
        frame.setVisible(true);
    }
}