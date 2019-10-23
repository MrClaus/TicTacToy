package com.gifo.serv.server;

/**
 * Created by gifo.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.gifo.serv.server.clients.ClientsField; // Импорт класса списка клиентов
import com.gifo.serv.server.controls.MyKeyAdapter; // Класс отслеживания событий нажатия клавиш


// Основной класс Сервер, который запускает многопользовательский сервер с множеством игровых столов
public class Server extends JPanel implements ActionListener {

    Font font = new Font("Arial", Font.BOLD, 15); // системный шрифт отображения текста

    Image bgScreen; // фоновое изображение
    ServerSocket server; // наш основной сервер
    int port = 0; // значение порта запускаемого сервера
    String[] passNames; // список игровых столов на сервере

    // IP для подключения к серверу - локальный и внешний
    // Примечание - если ваш ПК подключен к сети через роутер, то запущенный вами сервер
    // будет доступен только клиентам в вашей локальной сети роутера по адресу 'Internet local IP'
    // Если же ваш ПК подключен к сети кабельно напрямую без роутера, то ваш сервер
    // будет доступен всем клиентам удалённо по адресу 'Internet public IP'
    String ipLOC, ipPUB;

    // Инициализируем исполнитель параллельных потоков (один - на серверный менеджер,
    // остальные 32 на игровые столы, так как максимальное количество столов - 16)
    // Если количество подключенных клиентов к серверу больше 32, то клиенты встанут в очередь
    // на ожидание ответа от сервера, и как только количество клиентов на сервере станет меньше 32,
    // клиенты, которые стояли в очередь на поток успешно подключатся к серверу.
    static ExecutorService executeIt = Executors.newFixedThreadPool(33);

    // Конструктор основного класса
    public Server(String setFile) {
        if (getInfoStartServer(setFile)) {
            start();
            setFocusable(true);
            addKeyListener(new MyKeyAdapter(this));
            Timer runFrame = new Timer(500, this);
            runFrame.start();
        } else System.exit(1);
    }

    // Функция проверяет наличие сет-файла сервера/корректность его данных
    // Если всё хорошо, возвращает истину, и конструктор запускает сервер
    public boolean getInfoStartServer(String setFile) {
        boolean isPassNames = false;
        boolean isInfoServer = false;
        try {
            String strLine;
            FileInputStream fstream = new FileInputStream(setFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            while ((strLine = br.readLine()) != null) {
                String[] lines = strLine.split(":");
                String[] toLines = lines[1].split("/");
                if (lines[0].equals("port")) port = Integer.parseInt(toLines[0]);
                else if (lines[0].equals("field")) {
                    int countPass = toLines.length;
                    if (countPass > 16) countPass = 16;
                    passNames = new String[countPass];
                    for (int i=0; i<passNames.length; i++) passNames[i] = toLines[i];
                    isPassNames = true;
                }
                if (port!=0&&(isPassNames)) { isInfoServer = true; }
            }
        } catch (IOException e) { System.exit(2); }
        return isInfoServer;
    }

    // Запуск сервера
    public void start() {
        try {
            // Стартуем сервер и менеджер
            server = new ServerSocket(port);
            executeIt.execute(new ServerManager(server, passNames));

            // Получаем публичный ip для доступа к серверу
            try {
                URL whatismyip = new URL("http://checkip.amazonaws.com");
                BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
                ipPUB = in.readLine();
            } catch (IOException e) { ipPUB = "no network connect !"; }

            // Получаем локальный ip для доступа к серверу
            String[] getLocalIP = ("" + InetAddress.getLocalHost()).split("/");
            ipLOC = getLocalIP[1];

        } catch (IOException e) { System.exit(2); }
        bgScreen = new ImageIcon("res/started.jpg").getImage();
    }

    // Метод ActionListener-а, запускается с заданной переодичностью таймера
    public void actionPerformed(ActionEvent e) { repaint(); }

    // Отображение основной информации о запущенном сервере на экране
    public void paint(Graphics gel) {
        if (!server.isClosed()) {
            Graphics2D gfx = (Graphics2D) gel;
            gfx.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
            gfx.setColor(Color.LIGHT_GRAY);
            gfx.setFont(font);
            gfx.drawImage(bgScreen, 0, 0, null);
            gfx.drawString("Server started !!!", 10,20);
            gfx.drawString("Internet local IP:  " + ipLOC, 10,37);
            gfx.drawString("Internet public IP:  " + ipPUB, 10,54);
            gfx.drawString("Port number:  " + port, 10,71);
            gfx.drawString("Clients:  " + ClientsField.count, 10,88);
        }
    }

    // Реализация методов нажатия/отпускания клавиш MyKeyAdapter-а
    public void keyReleased(int key) {}
    public void keyPressed(int key) {
        // По нажатию клавиши Эскейп открываем диалоговое окно "Остановить сервер"
        // Если выбор пал на "Да", то программа останавливается, сервер закрывается
        if (key == KeyEvent.VK_ESCAPE) {
            int result = JOptionPane.showConfirmDialog(this,
                    "Вы хотите остановить сервер?",
                    "Закрытие сервера",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE);
            if (result == JOptionPane.YES_OPTION) closedServer();
        }
    }

    // Закрытие всех параллельных потоков, выход из программы
    public void closedServer() {
        ServerManager.closeChannels();
        try {server.close();} catch (IOException e) { System.out.println("Server closed with error"); }
        executeIt.shutdown();
        try {
            executeIt.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            System.out.println("Server is closed");
            System.exit(1);
        } catch (InterruptedException e) { System.exit(2); }
    }
}
