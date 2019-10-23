package com.gifo.screen.menu;

/**
 * Created by gifo.
 */

import java.awt.*;
import com.gifo.WINDOW; // Импорт класса настроек основного окна программы


// Класс графического меню
public class Menu {

    // Экземпляр класса font, через которого проводятся настройки шрифта объекта Menu
    public font font = new font();
    public class font {
        private Font font = new Font("Arial", Font.BOLD, 20);
        public Color color = Color.BLACK;
        public float alpha = 1f;

        // Методы объекта font
        public void set(Font myFont) { font = myFont; }
        Font get() { return font; }
    }

    // Непубличный объект класса selects, ссодержащий в себе параметры списка меню
    selects selects = new selects();
    private class selects {
        int xStart = 0;
        int yStart = 0;
        int step = 32;
        int bound = 0;
        String[] field;
    }

    // Непубличный объект класса cursor, ссодержащий в себе параметры курсора меню
    cursor cursor = new cursor();
    private class cursor {
        int animation = 0;
        int xStart = 0;
        int yStart = 0;
        int step = 32;
        Image pt;

        // Системные переменные cursor для корректного отображения
        boolean enable = true;
        double angle = 0;
        int pointer = 0;
        int bound = 0;
    }

    public int x = 0; // Публичная координата меню X
    public int y = 0; // Публичная координата меню Y
    private Image menu_bg; // Изображение бэкграунд текущего меню
    private int active = 0; // Переменная состояния меню, -1 - возврат к меню, 1 - возврат к выбранному окну, 0 - текущее окно
    private String status = "Menu"; // Активное окно в игре selects.field
    private String escape = ""; // Значение пункта меню, с которого происходит возврат назад

    // Конструктор основного класс Меню
    public Menu(String[] field, Image bg) {
        selects.field = field;
        menu_bg = bg;
    }

    // Метод настройки отображения/поведения списка меню
    public void field(int x, int y, int step) {
        selects.xStart = x;
        selects.yStart = y;
        selects.step = step;
    }

    // Альтернативный метод настройки отображения/поведения списка меню, с учетом высоты списка
    public void field(int x, int y, int step, int bound) {
        selects.xStart = x;
        selects.yStart = y;
        selects.step = step;
        selects.bound = bound;
    }

    // Метод редактор списка-содержания меню
    public void fieldReplace(String[] field) {
        selects.field = field;
        selects.bound = 0;
    }

    // Альтернативный метод редактор списка-содержания меню с учетом высоты списка
    public void fieldReplace(String[] field, int bound) {
        selects.field = field;
        selects.bound = bound;
    }

    // Метод редактор/назначение курсора на текущее меню
    public void pointer(Image pt, int x, int y, int step) {
        cursor.xStart = x;
        cursor.yStart = y;
        cursor.step = step;
        cursor.pt = pt;
    }

    // Альтернативный метод редактор/назначение курсора на текущее меню, с учетом типа анимации
    public void pointer(Image pt, int x, int y, int step, int animation) {
        cursor.animation = animation;
        cursor.xStart = x;
        cursor.yStart = y;
        cursor.step = step;
        cursor.pt = pt;
    }

    // Активация/деактивация курсора в меню (в случае пустого списка)
    public void pointerEnable(boolean isEnable) { cursor.enable = isEnable; }

    // Событие выбора пункта меню
    public void pressENTER() {
        if ((active==0)&&(status.equals("Menu"))&&(cursor.enable)) {
            active = 1;
            cursor.enable = false;
            for (int i=0; i<selects.field.length; i++) { if (cursor.pointer+cursor.bound==i) status =  selects.field[i]; }
        }
    }

    // Событие выхода из пункта меню
    public void pressESCAPE() {
        if ((active==0)&&!(status.equals("Menu"))) {
            active = -1;
            escape = status;
            status = "Menu";
            cursor.enable = true;
        }
    }

    // Событие экстренного выхода из пункта меню
    public void pressESCAPE(int closedStatus) {
        if ((closedStatus==-1)&&!(status.equals("Menu"))) {
            active = -1;
            escape = status;
            status = "Menu";
            cursor.enable = true;
        }
    }

    // Событие пролистывания основного списка меню вверх
    public void pressUP() {
        if (cursor.enable) {
            cursor.pointer--;
            if (selects.bound != 0) {
                if (cursor.pointer < 0) {
                    cursor.pointer++;
                    cursor.bound--;
                }
                if (cursor.pointer + cursor.bound < 0) {
                    cursor.pointer = 0;
                    cursor.bound = 0;
                }
            } else if (cursor.pointer == -1) cursor.pointer = 0;
        }
    }

    // Событие пролистывания основного списка меню вниз
    public void pressDOWN() {
        if (cursor.enable) {
            cursor.pointer++;
            if (selects.bound != 0) {
                if (cursor.pointer > selects.bound - 1) {
                    cursor.pointer--;
                    cursor.bound++;
                }
                if (cursor.pointer + cursor.bound > selects.field.length - 1) {
                    cursor.pointer = selects.bound - 1;
                    cursor.bound = selects.field.length - selects.bound;
                }
            } else if (cursor.pointer == selects.field.length) cursor.pointer = selects.field.length - 1;
        }
    }

    // Методы возвращают Значение наведенного курсора в соответствии со списком меню
    public String getPoint() { return selects.field[cursor.pointer+cursor.bound]; }
    public int getPointID() { return cursor.pointer+cursor.bound; }

    // Методы возвращают Значение выбранного пункта из списка меню
    public String getSelect() { return status; }
    public int getSelectID() {
        int id = -2;
        if (status.equals("Menu")) id = -1;
        for (int i=0; i<selects.field.length; i++) { if (status.equals(selects.field[i])) id = i; }
        return id;
    }

    // Методы возвращают Значение пункта меню, из которого был ранее произведен выход
    public String escapeFrom() { return escape; }
    public int escapeFromID() {
        int id = -1;
        for (int i=0; i<selects.field.length; i++) { if (escape.equals(selects.field[i])) id = i; }
        return id;
    }

    // Отображение графического меню на экран
    public void draw(Graphics2D gel) {

        float alpha = 1f;
        if (active != 0) {
            if (active == 1) x -= 5;
            else x += 5;
            if (x < -WINDOW.W) x = -WINDOW.W;
            else if (x > 0) x = 0;
            if ((x == -WINDOW.W) || (x == 0)) active = 0;
            alpha = (float) (-WINDOW.W - x) / (-WINDOW.W);
        }

        // Отображение бэкграунда меню и курсора
        gel.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        gel.drawImage(menu_bg, x, y, null);

        // Отображение курсора меню
        if (cursor.enable) {
            cursor.angle += 0.01;
            if (cursor.animation == 1) {
                gel.rotate(cursor.angle, x + cursor.xStart + cursor.pt.getWidth(null)/2, cursor.yStart + cursor.pointer * cursor.step + cursor.pt.getHeight(null)/2);
                gel.drawImage(cursor.pt, x + cursor.xStart, cursor.yStart + cursor.pointer * cursor.step, null);
                gel.rotate(-cursor.angle, x + cursor.xStart + cursor.pt.getWidth(null)/2, cursor.yStart + cursor.pointer * cursor.step + cursor.pt.getHeight(null)/2);
            } else if (cursor.animation == 2) {
                gel.drawImage(cursor.pt, x + cursor.xStart + (int) (12 * Math.sin(cursor.angle * 5)), cursor.yStart + cursor.pointer * cursor.step, null);
            } else gel.drawImage(cursor.pt, x + cursor.xStart, cursor.yStart + cursor.pointer * cursor.step, null);
        }

        // Отображения списка меню
        gel.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha*font.alpha));
        gel.setColor(font.color);
        gel.setFont(font.get());
        for (int i=0; i<selects.field.length; i++) gel.drawString(selects.field[i], x + selects.xStart, selects.yStart + (i - cursor.bound) * selects.step);

        // После отрисовки меню ставим прозрачность отображения по умолчанию
        gel.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }
}
