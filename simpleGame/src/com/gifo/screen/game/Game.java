package com.gifo.screen.game;

/**
 * Created by gifo.
 */

import javax.swing.*;
import java.awt.*;


// Класс игровой партии (игровой доски)
public class Game {

    static int x_start = 160; // координата X начала прорисовки игровой доски (крестики и нолики)
    static int y_start = 60; // координата Y начала прорисовки игровой доски (крестики и нолики)
    static int span_table = 64; // дистанция в пикселях между крестиками и ноликами на игровой доске
    private int[] data = {0, 0, 0, 0, 0, 0, 0, 0, 0}; // дата-сет игровой доски

    // Загружаем графические объекты игровой доски: крестик и нолик
    Image tic = new ImageIcon("res/tic.png").getImage();
    Image tac = new ImageIcon("res/tac.png").getImage();

    // Инициализируем своего игрока и соперника
    Player myPlayer = new Player();
    Player myEnemy = new Player();

    // Класс Игрок
    class Player {

        // Переменные класса (имя и количество побед игрока)
        String name = "";
        int wins = 0;

        // Чтение и запись побед игрока
        int getWins() { return wins; }
        void setWin(boolean i) { if (i) wins++; }

        // Чтение и запись имени игрока
        String getName() { return name.equals("") ? "Unnamed" : name; }
        void setName(String name) { this.name = (name.length()>10) ? name.substring(0, 10) : name; }
    }

    // Метод записывает в текущую игровую сессию имя игрока
    public void setNamePlayer(String player, String namePlayer) {
        if (player.equals("my")) myPlayer.setName(namePlayer);
        else if (player.equals("enemy")) myEnemy.setName(namePlayer);
    }

    // Метод возвращает имя игрока из текущей игровой сессии
    public String getNamePlayer(String player) {
        if (player.equals("my")) return myPlayer.getName();
        else if (player.equals("enemy")) return myEnemy.getName();
        else return "null";
    }

    // Метод обнуляет данные игровой доски
    public void restart() { for (int i=0; i<data.length; i++) data[i] = 0; }

    // Метод обнуляет данные набранных очков
    public void rescore() {
        myEnemy.wins = 0;
        myPlayer.wins = 0;
    }

    // Метод проверяет корректность хода игрока, чтобы не было записи текущего хода поверх уже сделанных
    public boolean isCorrectTab(int id) {
        if ((id>=0)&&(id<data.length)) return (data[id]!=0) ? false : true;
        else return false;
    }

    // Метод записывает на игровую доску ход (если true - то ваш, иначе - ход противника)
    public void setTab(int id, boolean i) { if ((id>=0)&&(id<data.length)) data[id] = i ? 3 : 5; }

    // Метод обновляет результаты текущей партии (записывает очки игры текущему игроку и сопернику)
    public void win(boolean i, boolean j) {
        myPlayer.setWin(i);
        myEnemy.setWin(j);
    }

    // Метод считывает количество побед в игре текущей партии
    public int getWin(String player) {
        if (player.equals("my")) return myPlayer.getWins();
        else if (player.equals("enemy")) return myEnemy.getWins();
        else return -1;
    }

    // Метод возвращает true, если текущая партия игры закончена
    public boolean isEndParty() {
        for (int i=0; i<data.length; i++) if (data[i]==0) return false;
        return true;
    }

    // Метод определяет победителя в случае, если он определён или партия закончена
    public String winner() {
        int res1 = data[0] + data[4] + data[8];
        int res2 = data[2] + data[4] + data[6];
        if (res1 == 9) return "my"; else if (res1 == 15) return "enemy";
        if (res2 == 9) return "my"; else if (res2 == 15) return "enemy";
        for (int i=0; i<data.length/3; i++) {
            res1 = data[i] + data[i + 3] + data[i + 6];
            res2 = data[i * 3] + data[i * 3 + 1] + data[i * 3 + 2];
            if (res1 == 9) return "my"; else if (res1 == 15) return "enemy";
            if (res2 == 9) return "my"; else if (res2 == 15) return "enemy";
        }
        if (isEndParty()) return "zero";
        else return "none";
    }

    // Отображение игровой доски на экран
    public void draw(Graphics2D gfx) {
        gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        for (int i=0; i<data.length; i++) {
            if (data[i]==3) gfx.drawImage(tic, x_start + (i%3)*span_table,y_start+(i/3)*span_table,null);
            else if (data[i]==5) gfx.drawImage(tac, x_start + (i%3)*span_table,y_start+(i/3)*span_table,null);
        }
    }
}
