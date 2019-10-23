package com.gifo.screen;

/**
 * Created by gifo.
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import javax.swing.*;
import com.gifo.screen.client.Client; // Класс игрового клиента
import com.gifo.screen.controls.MyKeyAdapter; // Класс отслеживания событий нажатия клавиш
import com.gifo.screen.game.Game; // Класс игры (игровой доски Крестики и Нолики)
import com.gifo.screen.menu.Menu; // Класс Меню
import com.gifo.screen.push.PushInfo; // Класс графических Пуш-уведомлений


// Основной класс Скрин, который запускает и отображает всё действо Игры
public class Screen extends JPanel implements ActionListener {

    // *********** ОБЪЯВЛЕНИЕ НЕОБХОДИМЫХ ПЕРЕМЕННЫХ ***********
    Menu menu, onlineMenu; // список типов меню
    Font fontPush, fontMenu, fontMenuOnline, fontGame1, fontGame2, fontGame3; // список шрифтов
    Image menu_cube, infoMenu, onlineMenu_layer, onlineMenu_layerA, gameScreen, clockWait; // список изображений

    Client client = null; // непосредственно сам клиент, подключенный к серверу
    int selectGameID = -1; // индекс выбранного игрового стола клиентом на сервере
    int enterToConnect = 0; // состояние подключения к серверу игрового клиента
    int getFieldOnline = 0; // состояние параллельного ожидания списка игровых столов от сервера
    private boolean checkConnect = false; // опция проверки связи с сервером
    String[] fieldGamesOnline, statusGamesOnline; // массив игровых столов и их заполненности, получаемый от сервера

    Game game; // объект игровой доски игрового процесса
    int spawn = 0; // очередь хода игрока
    int gameEnterd = 0; // состояние игрового процесса старт/ожидание противника/игра
    boolean isPlayed = false; // состояние активности игрового режима
    int select_tab; // нажатая клавиша в игре/ сделанный ход


    // Конструктор Основного класса
    public Screen(int msFrame) {
        preload();
        setFocusable(true);
        addKeyListener(new MyKeyAdapter(this));
        Timer requestAnimationFrame = new Timer(msFrame, this);
        requestAnimationFrame.start();
    }


    // Подготовка к запуску, создание необходимых объектов, предзагрузка
    private void preload() {

        // Подготавливаем шрифты для игры
        fontPush = new Font("Book Antiqua", Font.BOLD, 14);
        fontMenu = new Font("Bauhaus 93", Font.BOLD, 30);
        fontMenuOnline = new Font("Arial", Font.BOLD, 15);
        fontGame1 = new Font("Georgia", Font.BOLD, 13);
        fontGame2 = new Font("Arial", Font.BOLD, 64);
        fontGame3 = new Font("Broadway", Font.BOLD, 18);

        // Объявление и настройка главного меню
        Image menu_bg = new ImageIcon("res/menu_bg.jpg").getImage();
        Image menu_cursor = new ImageIcon("res/menu_pt.png").getImage();
        String[] menu_field = {"New Game", "Online", "Information", "Exit"};
        menu = new Menu(menu_field, menu_bg);
        menu.pointer(menu_cursor, 238, 23, 35,1);
        menu.field(280,50, 35);
        menu.font.set(fontMenu);
        menu.font.color = Color.ORANGE;
        menu.font.alpha = 0.385f;
        // Анимация к меню
        menu_cube = Toolkit.getDefaultToolkit().createImage("res/menu_anima.gif");

        // Пункт меню - Online
        Image testField_bg = new ImageIcon("res/online_bg.jpg").getImage();
        Image testField_pt = new ImageIcon("res/select_pt.png").getImage();
        String[] testField_field = { "" };
        onlineMenu = new Menu(testField_field, testField_bg);
        onlineMenu.pointer(testField_pt, -106, 67, 20,2);
        onlineMenu.pointerEnable(false);
        onlineMenu.field(144,80, 20,4);
        onlineMenu.font.set(fontMenuOnline);
        onlineMenu.font.color = Color.BLACK;
        onlineMenu.font.alpha = 0.5f;
        // Отображение слоя Online поверх меню Online и загрузка титульной анимации меню
        onlineMenu_layer = new ImageIcon("res/online_layer.png").getImage(); // Изображение поверх данного меню
        onlineMenu_layerA = Toolkit.getDefaultToolkit().createImage("res/online_anima.gif");

        // Пункт - Информация
        infoMenu = new ImageIcon("res/menu_info.jpg").getImage();

        // Экран игрового процесса
        gameScreen = new ImageIcon("res/game.jpg").getImage();
        clockWait = new ImageIcon("res/clock.png").getImage();
        game = new Game(); // создаем игровую доску

        // Создаём push-уведомления в игре
        PushInfo.Settings.position(115,50);
        PushInfo.Settings.textBox(32,2,15,12,5);
        PushInfo.Settings.size(250,50,10);
        PushInfo.Settings.time(400,2800);
        PushInfo.Settings.opacity(0.7f);
        PushInfo.Settings.font(fontPush);
        new PushInfo("Connecting ...",1);
        new PushInfo("Connected to Server!",2);
        new PushInfo("Error connecting to server!",3);
        new PushInfo("Server severed connection ...",4);
        new PushInfo("The game table is full ...",5);
        new PushInfo("You sat at the game table!",6);
        new PushInfo("The game started!",7);
        new PushInfo("You Win! :-)",8);
        new PushInfo("You Lose! :-(",9);
        new PushInfo("Dead heat! :-|",10);
        new PushInfo("The game table was abandoned ...",11);
        new PushInfo("Waiting ...",12);
    }


    // Аналогично requestAnimationFrame из javascript
    public void actionPerformed(ActionEvent e) { repaint(); }


    // Функция отрисовки всего действа, происходящего в классе Screen - эту функцию исполняет repaint()
    public void paint(Graphics gel) {

        // Инициализируем графический 2д модуль и включаем сглаживание для текста
        Graphics2D gfx = (Graphics2D) gel;
        gfx.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );

        // *********** ПРОРИСОВКА ИГРЫ ***********
        // Если активен режим игры, то отображаем его
        gfx.drawImage(gameScreen,0,0,null); // рисуем фон игрового стола
        if (isPlayed) {

            // Отображаем номер текущего выбранного игрового стола
            gfx.setFont(fontGame2);
            gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.20f));
            gfx.drawString("#" + (selectGameID + 1), 28, 164);

            // Отображаем имя нашего профиля
            gfx.setColor(Color.WHITE);
            gfx.setFont(fontGame1);
            gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.70f));
            gfx.drawString(game.getNamePlayer("my"), 92, 27);

            // Если игра стартовала, осуществляем диалог клиентов (игроков в Оффлайн-режиме),
            // иначе - ждем соперника
            // Режим gameEnterd = 1 - ожидание подключения соперника Онлайн
            // Режим gameEnterd = 2 - игра в Онлайн режиме после того, как соперник подключился
            // Режим gameEnterd = 3 - игра в Оффлайн режиме с компьютером
            String msg;
            if (gameEnterd>1) {

                // Параллельно ожидаем хода игрока и меняемся очередью хода
                if (gameEnterd==2) { // для Онлайн-режима
                    msg = client.getMessage();
                    if (msg.indexOf("newGameMove ") != -1) {
                        client.reMessage();
                        String[] tab = msg.split(" ");
                        int tablet = Integer.parseInt(tab[1]);
                        if (tablet == -1) {
                            onlineMenu.pressESCAPE(-1);
                            endGame();
                        } else if (spawn == 0) {
                            game.setTab(tablet - 1, false);
                            spawn = 1;
                        }
                    }
                } else if ((gameEnterd==3)&&(spawn == 0)) { // для Оффлайн-режима
                    int tablet = (int) Math.round (8 * Math.random());
                    if (game.isCorrectTab(tablet)) {
                        game.setTab(tablet, false);
                        spawn = 1;
                    }
                }

                // Отображаем имя профиля противника правильно, чтобы приятно на глаз
                int autoPosXtext = (int) fontGame1.getStringBounds(
                        game.getNamePlayer("enemy"),
                        new FontRenderContext(null, true, false)).getWidth();
                gfx.drawString(game.getNamePlayer("enemy"), 389 - autoPosXtext, 27);

                // Отображаем очки игроков в игровой партии
                int x = game.getWin("my");
                int y = game.getWin("enemy");
                String scores = (x<10 ? ("0" + x) : ("" + x)) + ":" + (y<10 ? ("0" + y) : ("" + y));
                gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                gfx.setFont(fontGame3);
                gfx.setColor(Color.BLACK);
                gfx.drawString(scores, 212,30);
                gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
                gfx.setColor(Color.YELLOW);
                gfx.drawString(scores, 210,28);

                // Очередь хода игроков
                if (spawn==0) gfx.drawImage(clockWait, 365,45,null); // отображаем очередность хода
                else {
                    gfx.drawImage(clockWait, 83,45,null); // отображаем очередность хода
                    if (select_tab!=0) if (game.isCorrectTab(select_tab-1)) {
                        game.setTab(select_tab-1, true);
                        if (gameEnterd==2) client.sentMessage("setGameMove " + select_tab);
                        spawn = 0;
                    }
                }

                // Проверяем на законченность партии и определяем победителя, после чего - начинаем новую партию
                String winner = game.winner();
                if (!winner.equals("none")) {
                    game.win(winner.equals("my") ? true : false, winner.equals("enemy") ? true : false);
                    if (winner.equals("my")) PushInfo.call(8);
                    else if (winner.equals("enemy")) PushInfo.call(9);
                    else if (winner.equals("zero")) PushInfo.call(10);
                    game.restart();
                }

                if (gameEnterd==2) client.sentMessage("/step");
                select_tab = 0;

            } else if (gameEnterd==1) {

                // Проверяем количество игроков за текущим игровым столом, если стол заполнен (= 2),
                // то считываем инфу о сопернике и начинаем игру.
                // Здесь же отдаём первый ход (spawn) тому, кто первый сел за стол
                client.sentMessage("getStatusGames");
                msg = client.messageWaiting("statusGames");
                if (!msg.equals("error")) {
                    String[] line = msg.split(" ");
                    statusGamesOnline = line[1].split(",");
                    if (statusGamesOnline[selectGameID].equals("2")) {
                        client.sentMessage("getGameProfiles");
                        msg = client.messageWaiting("gameProfiles");
                        if (!msg.equals("error")) {
                            String[] toLines = msg.split(" ");
                            game.setNamePlayer("enemy", toLines[1]);
                            PushInfo.call(7);
                            client.sentMessage("setСheckAlone true");
                            gameEnterd = 2;
                        }
                    } else if (statusGamesOnline[selectGameID].equals("1")) spawn = 1;
                } else {
                    onlineMenu.pressESCAPE(-1);
                    endGame();
                }
            }

            // Отрисовка игровой доски
            gfx.setFont(null);
            game.draw(gfx);
        }

        // *********** ПРОРИСОВКА МЕНЮ ***********
        gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        // Считывание активного пункта меню и пункта, с которого произошел выход
        String menu_sel = menu.getSelect();
        String menu_esc = menu.escapeFrom();

        // Отображение пункта меню Онлайн-Режим
        if (menu_sel.equals("Online") || menu_esc.equals("Online") && menu_sel.equals("Menu")) {
            if (!isPlayed) restartOnlineField();
            onlineMenu.draw(gfx);
            gfx.drawImage(onlineMenu_layer, onlineMenu.x, 0, null);
            gfx.drawImage(onlineMenu_layerA, onlineMenu.x+6, 3, this);
        }

        // Отображение пункта меню Информация
        if (menu_sel.equals("Information") || menu_esc.equals("Information") && menu_sel.equals("Menu"))
            gfx.drawImage(infoMenu, 0, 0, null);

        // Отображение основного меню
        menu.draw(gfx);
        gfx.drawImage(menu_cube, menu.x + 35, 30, this); // Титульная анимация основного меню

        // Проверка на связь с сервером
        if (enterToConnect==2||enterToConnect==3) if ((client.getStarted()!=1)||client.getMessage().equals("disconnected")) {
            PushInfo.call(4);
            enterToConnect = 1;
            client.reMessage();
            if ((isPlayed)&&(gameEnterd!=3)) {
                onlineMenu.pressESCAPE(-1);
                endGame();
            }
        }

        // *********** ПРОРИСОВКА УВЕДОМЛЕНИЙ ***********
        PushInfo.view(gfx); // отображение пуш-уведомлений, если они были вызваны
    }


    // Функция получает от сервера список игровых столов и их заполненность, в случае, если клиент создан
    // и на связи с сервером, в противном случае выводит сообщение об ошибке
    // Учитывая, что ответ от сервера может приходить не сразу и слушатель сообщений работает асинхронно
    // параллельным потоком, то функцию реализуем параллельным ожиданием, в зависимости от
    // состояний отправки запросов и полученных ответов
    private void restartOnlineField() {
        if (enterToConnect!=0) {
            String[] wait = {"Please wait..."};
            String[] errS = {"Error: server not found!"};
            String[] errF = {"Error: no correct a client data!"};
            if (enterToConnect == 1) {
                onlineMenu.pointerEnable(false);
                if (client.getStarted() == 1) enterToConnect = 2;
                else if (client.getStarted() == -1) onlineMenu.fieldReplace(errF);
                else if (client.getStarted() == -2) onlineMenu.fieldReplace(errS);
                if ((client.getStarted()<0)&&(checkConnect)) {
                    PushInfo.call(3);
                    checkConnect = false;
                }
            } else if (enterToConnect == 2) {
                onlineMenu.fieldReplace(wait);
                if (client.getMessage().equals("connected")){
                    client.sentMessage("setMyProfile " + client.getClientProfile());
                    enterToConnect = 3;
                    PushInfo.call(2);
                }
            } else if (enterToConnect == 3) {
                String msg = client.getMessage();
                String[] line = msg.split(" ");
                if (getFieldOnline==1) {
                    client.sentMessage("getFieldGames");
                    getFieldOnline = 2;
                } else if (getFieldOnline==2) {
                    if (msg.indexOf("fieldGames")!=-1) {
                        fieldGamesOnline = line[1].split(",");
                        client.sentMessage("getStatusGames");
                        getFieldOnline = 3;
                    }
                } else if (getFieldOnline==3) {
                    if (msg.indexOf("statusGames")!=-1) {
                        statusGamesOnline = line[1].split(",");
                        String[] newField = new String[fieldGamesOnline.length];
                        for (int i=0; i<fieldGamesOnline.length; i++) newField[i] = "# " + (i + 1) + "   [ " + statusGamesOnline[i] + " / 2 ]  " + fieldGamesOnline[i];
                        onlineMenu.fieldReplace(newField, (newField.length>7) ? 7 : newField.length);
                        onlineMenu.pointerEnable(true);
                        getFieldOnline = 0;
                    }
                }
            }
        } else {
            client = new Client("setClient.txt");
            enterToConnect = 1;
            PushInfo.call(1);
        }
    }


    // Метод, который отвечает за выход клиента из игрового режима
    private void endGame() {

        // Если игра была Онлайн - покидаем игровой стол на стороне сервера
        if (gameEnterd==1||gameEnterd==2) {
            client.sentMessage("setСheckAlone false");
            client.sentMessage("endGame");
            checkConnect = true;
        }

        // Завершение игрового процесса
        PushInfo.call(11);
        selectGameID = -1;
        isPlayed = false;
        game.restart();
        game.rescore();
        gameEnterd = 0;
    }


    // Проверка нажатий клавиш
    public void keyReleased(int key) {}
    public void keyPressed(int key) {

        String menu_sel = menu.getSelect(); // выбранный пункт основного меню

        // Проверка нажатий клавиш 1-9 правой цифровой клавиатуры
        // Так как для игровой доски 1 начинается сверху от 7, пересчитываем select_tab
        // нужным образом в случае нажатия
        select_tab = 0;
        for (int i=0; i<9; i++) {
            if (key == (0x61+i)) {
                select_tab = i + 7;
                if (select_tab>12) select_tab-=12;
                if (select_tab>9) select_tab-=6;
                break;
            }
        }

        // *********** ЕСЛИ НАЖАТА КЛАВИША "ВВЕРХ" ***********
        if (key == KeyEvent.VK_UP) {
            if (menu_sel.equals("Menu")) menu.pressUP();
            if (menu_sel.equals("Online")) onlineMenu.pressUP();
        }

        // *********** ЕСЛИ НАЖАТА КЛАВИША "ВНИЗ" ***********
        if (key == KeyEvent.VK_DOWN) {
            if (menu_sel.equals("Menu")) menu.pressDOWN();
            if (menu_sel.equals("Online")) onlineMenu.pressDOWN();
        }

        // *********** ЕСЛИ НАЖАТА КЛАВИША "ЭНТЕР" ***********
        if (key == KeyEvent.VK_ENTER) {

            // Если Энтер нажат в главном меню
            if (menu_sel.equals("Menu")) {
                if (menu.getPoint().equals("New Game")) { // если выбрали пункт Новая Игра
                    menu.pressENTER();
                    game.setNamePlayer("my", "My Player");
                    game.setNamePlayer("enemy", "Computer");
                    PushInfo.call(6);
                    PushInfo.call(7);
                    isPlayed = true;
                    gameEnterd = 3;
                    spawn = 1;
                } else if (menu.getPoint().equals("Online")) { // если выбрали пункт Онлайн
                    if (enterToConnect!=0) checkConnect = true;
                    getFieldOnline = 1;
                    menu.pressENTER();
                } else if (menu.getPoint().equals("Information")) { // если выбрали пункт Информация
                    menu.pressENTER();
                } else if (menu.getPoint().equals("Exit")) {  // если выбрали пункт Выход
                    int result = JOptionPane.showConfirmDialog(this,
                            "Вы хотите покинуть игру?",
                            "Выход из игры",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.ERROR_MESSAGE);
                    if (result == JOptionPane.YES_OPTION) {
                        if (enterToConnect==3) client.sentMessage("close");
                        System.exit(1);
                    }
                }
            }

            // Если Энтер нажат в меню Онлайн - выбора игровых столов
            if (menu_sel.equals("Online")&&(onlineMenu.getSelect().equals("Menu"))&&(enterToConnect==3)) {
                client.sentMessage("setGameSelect " + fieldGamesOnline[onlineMenu.getPointID()]);
                client.sentMessage("getGameSelect");
                String msg = client.messageWaiting("gameSelect");
                if (!msg.equals("error")) {
                    isPlayed = false;
                    String[] lines = msg.split(" ");
                    if (lines[1].equals("null")) PushInfo.call(5);
                    else {
                        onlineMenu.pressENTER();
                        game.setNamePlayer("my", client.getClientProfile());
                        selectGameID = onlineMenu.getPointID();
                        PushInfo.call(6);
                        isPlayed = true;
                        gameEnterd = 1;
                        spawn = 0;
                    }
                }
            }
        }

        // *********** ЕСЛИ НАЖАТА КЛАВИША "ПРОБЕЛ" ***********
        if (key == KeyEvent.VK_SPACE) if (menu_sel.equals("Online")&&(onlineMenu.getSelect().equals("Menu"))) {
            PushInfo.call(12);
            getFieldOnline = 1;
        }

        // *********** ЕСЛИ НАЖАТА КЛАВИША "ЭСКЕЙП" ***********
        if (key == KeyEvent.VK_ESCAPE) {
            if (menu_sel.equals("Information")) menu.pressESCAPE();
            if (menu_sel.equals("Online")&&onlineMenu.getSelect().equals("Menu")) menu.pressESCAPE();
            if (menu_sel.equals("Online")&&!onlineMenu.getSelect().equals("Menu")) {
                onlineMenu.pressESCAPE(-1);
                endGame();
            } else if (menu_sel.equals("New Game")) {
                menu.pressESCAPE(-1);
                endGame();
            }
        }
    }
}
