package com.gifo.screen.push;

/**
 * Created by gifo.
 */

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;


// Класс графических Push-уведомлений
public class PushInfo {

    private static java.util.List<PushInfo> pushes = new ArrayList<PushInfo>(); // список созданных объектов PushInfo

    // Статичные переменные класса, которые отвечают за общие настройки отображения уведомлений
    private static int x = 0; // координата X появления push
    private static int y = 0; // координата Y появления push
    private static int width = 128; // ширина push
    private static int height = 32; // высота push
    private static int padding = 4; // дистанция между push-уведомлениями на экране
    private static int waking = 1000; // время появления push
    private static float firstWaking;
    private static int timeout = 1000; // время исчезания push
    private static float opacity = 1f; // прозрачность push
    private static int messageWidth = 16; // ширина textBox-а в символах
    private static int messageHeight = 1; // высота textBox-а в символах
    private static int messageDX = 0; // координата X textBox-а относительно push
    private static int messageDY = 0; // координата Y textBox-а относительно push
    private static int span = 12; // межстрочный интервал textBox-а в пикселях
    private static Font font = null; // шрифт текстового сообщения push

    private static boolean goQueue = true; // отвечает за отправку вызова push в исполнение/ожидание
    private boolean isRegistred = false; // true - в случае, если вызов push отправлен в ожидание

    private int queue = 0; // переменная очереди сообщения в списке вызванных
    private int status = 0; // переменная состояния отображения сообщения
    private long statusOne, statusTwo, statusThis; // сопутствуют status-у
    private Color textCOLOR = Color.BLACK; // цвет текста push
    private Color pushCOLOR = Color.WHITE; // цвет рамки push
    private BufferedImage push = null; // воссозданный push
    private String msg = ""; // текстовое сообщение push
    private int ID; // индивидуальный номер push

    // Альтернативный конструктор класс уведомлений, если необходимо придать индивидуальность push-сообщению
    public PushInfo(String message, int ID, Color textCOLOR, Color pushCOLOR) {
        this.msg = message;
        this.ID = ID;
        this.textCOLOR = textCOLOR;
        this.pushCOLOR = pushCOLOR;
        createPush();
        pushes.add(this);
    }

    // Основной конструктор класса push-уведомлений
    public PushInfo(String message, int ID) {
        this.msg = message;
        this.ID = ID;
        createPush();
        pushes.add(this);
    }

    // Воссоздаёт изображение push-уведомления по текущим настройкам
    private void createPush() {

        // Создаём графический объект пуш-сообщения
        this.push = new BufferedImage(PushInfo.width, PushInfo.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D  gfx = this.push.createGraphics();
        gfx.setColor(this.pushCOLOR);
        gfx.fillRect(0,0,PushInfo.width,PushInfo.height);

        // Выводим текст уведомления в форму пуш-сообщения
        String line = "";
        gfx.setFont(PushInfo.font);
        gfx.setColor(this.textCOLOR);
        int heightFont = gfx.getFontMetrics().getHeight();
        gfx.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
        if (this.msg.length()>PushInfo.messageWidth*PushInfo.messageHeight) {
            for (int i=0; i<PushInfo.messageHeight; i++) {
                if (i!=PushInfo.messageHeight-1) line = msg.substring(i * PushInfo.messageWidth, (i + 1) * PushInfo.messageWidth);
                else line = msg.substring(i * PushInfo.messageWidth, (i + 1) * PushInfo.messageWidth - 4) + " ...";
                gfx.drawString(line,PushInfo.messageDX,PushInfo.messageDY  + heightFont + i*PushInfo.span);
            }
        } else {
            int ost = this.msg.length()%PushInfo.messageWidth;
            int crat = this.msg.length()/PushInfo.messageWidth;
            for (int i=0;i<=crat;i++) {
                if (i!=crat) line = msg.substring(i * PushInfo.messageWidth, (i + 1) * PushInfo.messageWidth);
                else if (ost>0) line = msg.substring(i * PushInfo.messageWidth, i * PushInfo.messageWidth + ost);
                if (i!=crat||ost>0) gfx.drawString(line,PushInfo.messageDX,PushInfo.messageDY + heightFont + i*PushInfo.span);
            }
        }
    }

    // Выводит воссозданные push-уведомления на экран в порядке очереди
    private void draw(Graphics2D g) {
        if (this.status==1) {
            this.status=2;
            this.statusOne = System.currentTimeMillis();
        } else if (this.status==2) {
            this.statusThis = System.currentTimeMillis();
            long delta= this.statusThis-this.statusOne;
            if (delta>PushInfo.waking) {
                this.status=3;
                PushInfo.goQueue = true;
                this.statusTwo = System.currentTimeMillis();
            } else {
                PushInfo.firstWaking = (float) delta/PushInfo.waking;
                int active_h = (int) (PushInfo.height * PushInfo.firstWaking);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, PushInfo.opacity));
                g.drawImage(this.push.getScaledInstance(PushInfo.width, (active_h == 0) ? 1 : active_h, Image.SCALE_SMOOTH),
                            PushInfo.x,
                            PushInfo.y,
                            null);
            }
        } else if (this.status==3) {
            this.statusThis = System.currentTimeMillis();
            long delta= this.statusThis-this.statusTwo;
            if (delta>PushInfo.timeout) {
                this.status = 0;
                this.queue = 0;
            } else {
                float k = 1f - (float) delta/PushInfo.timeout;
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, PushInfo.opacity * k));
                g.drawImage(this.push,
                            PushInfo.x,
                            (int) (PushInfo.y + (this.queue - 2 + PushInfo.firstWaking) * (PushInfo.height + PushInfo.padding)),
                            null);
            }
        }
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    // Статичный класс настроект объекта PushInfo
    public static class Settings {

        // Стартовая позиция, откуда появляется пуш-уведомление
        public static void position(int x, int y) {
            PushInfo.x = x;
            PushInfo.y = y;
        }

        // Размеры ширины и высоты пуш-прямоугольника
        public static void size(int width, int height, int padding) {
            PushInfo.width = width;
            PushInfo.height = height;
            PushInfo.padding = padding;
        }

        public static void textBox(int width, int height, int span, int dX, int dY) {
            PushInfo.messageWidth = width;
            PushInfo.messageHeight = height;
            PushInfo.messageDX = dX;
            PushInfo.messageDY = dY;
            PushInfo.span = span;
        }

        // Время, в течение которого появляется и за которое исчезает уведомление
        public static void time(int waking, int timeout) {
            PushInfo.waking = waking;
            PushInfo.timeout = timeout;
        }

        public static void opacity(float alpha) { PushInfo.opacity = alpha; } // общая прозрачность уведомления
        public static void font(Font font) { PushInfo.font = font; } // шрифт текста уведомления
    }

    // Статичная функция вызова извне созданных push по ID
    public static void call(int ID) {
        PushInfo push = getPush(ID);
        if (push!=null) if (push.queue+push.status==0) {
            if (PushInfo.goQueue) {
                push.isRegistred = false;
                push.status = 1;
                push.queue = 1;
                Iterator<PushInfo> it = pushes.iterator();
                while (it.hasNext()) {
                    PushInfo get = it.next();
                    if ((!get.equals(push))&&(get.queue!=0)) get.queue++;
                }
                PushInfo.firstWaking = 0f;
                PushInfo.goQueue = false;
            } else push.isRegistred = true;
        }
    }

    // Статичная функция отображения push
    public static void view(Graphics2D gfx) {
            Iterator<PushInfo> it = pushes.iterator();
            while (it.hasNext()) {
                PushInfo push = it.next();
                if (push.isRegistred) call(push.ID);
                push.draw(gfx);
            }
    }

    // Статичная вспомогательная функция, возвращает объект push по ID
    public static PushInfo getPush(int ID) {
        Iterator<PushInfo> it = pushes.iterator();
        while (it.hasNext()) {
            PushInfo push = it.next();
            if (push.ID==ID) return push;
        }
        return null;
    }

    // Статичная функция, возвращает true, если push с заданным ID - существует, иначе - false
    // Может вызываться извне, например, когда генерируем ID для новых push-уведомлений
    public static boolean isID(int ID) {
        PushInfo push = getPush(ID);
        return (push!=null) ? true : false;
    }
}
