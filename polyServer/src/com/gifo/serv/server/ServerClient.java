package com.gifo.serv.server;

/**
 * Created by gifo.
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import com.gifo.serv.server.clients.ClientsField; // Импорт класса списка клиентов


// Индивидуальный экземпляр сервера для подключенного клиента
public class ServerClient implements Runnable {

    private Socket client; // текущий подключенный клиент
    private String[] passNames; // список игровых столов на сервере
    boolean checkAlone = false; // состояние отправки уведомлений клиенту, если он один за игровым столом

    // Конструктор класса-потока ServerClient
    public ServerClient(Socket client, String[] passNames) {
        this.client = client;
        this.passNames = passNames;
    }

    // Метод Runnable для запуска в пуле потоков
    public void run() {

        try {
            // инициируем каналы общения в сокете, для сервера
            DataOutputStream out = new DataOutputStream(client.getOutputStream());
            DataInputStream in = new DataInputStream(client.getInputStream());

            // Уведомляем клиента о подключении, в случае, если он ждал очереди на запуск в потоке
            out.writeUTF("connected");
            out.flush();

            // начинаем диалог с подключенным клиентом в цикле, пока сокет не закрыт клиентом
            while (!client.isClosed()) {
                String entry = in.readUTF(); // ожидаем от клиента команд

                /* Язык общения с клиентом

                *** Приём сообщений от клиента
                > 'setMyProfile' - запрос на сохранения имени профиля текущего клиента;
                > 'getGameProfiles' - запрос списка профилей игроков за текущим игровым столом;
                > 'getFieldGames' - запрос списка игровых столов;
                > 'getStatusGames' - запрос количества клиентов за игровыми столами;
                > 'setGameSelect' - запрос на сохранение на сервере выбранного клиентом стола/null;
                > 'getGameSelect' - запрос сохраненного на сервере выбранного клиентом стола/null;
                > 'setGameMove' - запрос на сохранение на сервере сделанного хода;
                > 'endGame' - запрос на выход из игрового стола
                > 'close' - запрос на разрыв соединения с сервером;

                *** Отправка сообщений клиенту  *('ключевое слово' + 'пробел' + 'данные')
                > 'connected' - ответ отправляется клиентам, которые успешно попали на поток;
                > 'disconnected' - ответ отправляется клиентам, если сервер был закрыт;
                > 'fieldGames'* - ответ на запрос 'getFieldGames';
                > 'statusGames'* - ответ на запрос 'getStatusGames';
                > 'gameSelect'* - ответ на запрос 'getGameSelect';
                > 'gameProfiles'* - ответ на запрос 'getGameProfiles';
                > 'newGameMove'* - ответ отправляется клиентам на сделанный ход игроков за столом через 'setGameMove';

                 */

                // Проверяем на геттеры, сеттеры и попытку клиента отключиться от сервера
                if (entry.length()>3) {
                    if (entry.substring(0, 3).equals("get")) {
                        if (entry.equals("getFieldGames")) out.writeUTF("fieldGames " + getLinePassNames());
                        else if (entry.equals("getStatusGames")) out.writeUTF("statusGames " + getStatusPassNames());
                        else if (entry.equals("getGameSelect")) out.writeUTF("gameSelect " + getSelectPassName());
                        else if (entry.equals("getGameProfiles")) out.writeUTF("gameProfiles " + getThisGamePlayers());
                        out.flush();
                    } else if (entry.substring(0, 3).equals("set")) {
                        String[] lines = entry.split(" ");
                        if (lines.length >= 2) {
                            if (lines[0].equals("setMyProfile")) setClientProfile(lines[1]);
                            if (lines[0].equals("setGameSelect")) setSelectPassName(lines[1]);
                            if (lines[0].equals("setGameMove")) sentDataToMyPlayers(lines[1]);
                            if (lines[0].equals("setСheckAlone")) setСheckAlone(lines[1]);
                        }
                    } else if (entry.equals("endGame")) {
                        exitFromThisGameSelect();
                    } else if (entry.equals("close")) {
                        in.close();
                        out.close();
                        clientDestroy();
                    }
                }

                // Если клиент находится один за игровым столом, то ему отправляется статус ожидания 'newGameMove' = -1
                if ((!getSelectPassName().equals("null"))&&(getThisGamePlayers().equals("null"))&&(checkAlone)) {
                    out.writeUTF("newGameMove -1");
                    out.flush();
                }
            }
        } catch (IOException e) { clientDestroy(); }
    }

    // Возвращает по запросу строку со списком всех игровых столов
    private String getLinePassNames() {
        String linePassNames = "";
        for (int i=0; i<passNames.length; i++) {
            linePassNames+=passNames[i];
            if (i<passNames.length-1) linePassNames+=",";
        }
        return linePassNames;
    }

    // Возвращает по запросу строку с количеством игроков по каждому игровому столу
    private String getStatusPassNames() {
        String lineStatusPass = "";
        for (int i = 0; i < passNames.length; i++) {
            int statusPass = 0;
            Iterator<ClientsField> it = ServerManager.clients.iterator();
            while (it.hasNext()) {
                ClientsField com = it.next();
                if (passNames[i].equals(com.getPass())) statusPass++;
            }
            lineStatusPass+=Integer.toString(statusPass);
            if (i<passNames.length-1) lineStatusPass+=",";
        }
        return lineStatusPass;
    }

    // Запрос игрового стола текущего клиента
    private String getSelectPassName() {
        String lineMyPassName = "null";
        ClientsField com = getThisClientProfile();
        if (com!=null) lineMyPassName = com.getPass();
        return lineMyPassName;
    }

    // Возвращает по запросу строку с профилями других подключенных клиентов на текущем игровом столе
    private String getThisGamePlayers() {
        String lineMyPlayers = "";
        String myGame = getSelectPassName();
        ClientsField myClient = getThisClientProfile();
        if (!myGame.equals("null")&&myClient!=null) {
            Iterator<ClientsField> it = ServerManager.clients.iterator();
            while (it.hasNext()) {
                ClientsField com = it.next();
                if (myGame.equals(com.getPass())&&(!com.equals(myClient))) lineMyPlayers+=com.getProfile() + ",";
            }
            if (!lineMyPlayers.equals("")) {
                int lenStr = lineMyPlayers.length();
                if (lineMyPlayers.substring(lenStr - 1).equals(",")) lineMyPlayers = lineMyPlayers.substring(0, lenStr - 1);
            } else lineMyPlayers = "null";
        }
        return lineMyPlayers;
    }

    // Возвращает объект текущего клиента класса ClientsField
    private ClientsField getThisClientProfile() {
        Iterator<ClientsField> it = ServerManager.clients.iterator();
        while (it.hasNext()) {
            ClientsField com = it.next();
            if (client.equals(com.client)) return com;
        }
        return null;
    }

    // Изменение по запросу игрового стола текущего клиента
    private void setSelectPassName(String lineMyPassName) {
        ClientsField com = getThisClientProfile();
        if (com!=null) if (getSelectPassName().equals("null")) {
            int id = -1;
            for (int i=0; i<passNames.length; i++) if (lineMyPassName.equals(passNames[i])) id = i;
            // 2 - это ограничение количества игроков на один игровой стол
            String[] lines = getStatusPassNames().split(",");
            if (id!=-1) if (Integer.parseInt(lines[id])<2) com.setPass(lineMyPassName);
        }
    }

    // Отправка сообщения по запросу всем клиентам (кроме самого клиента, сделавшего ход),
    // которые подключены к игрому столу, что и текущий клиент
    private void sentDataToMyPlayers(String myMove) {
        ClientsField myClient = getThisClientProfile();
        if (myClient!=null) {
            String lineMyPassName = getSelectPassName();
            if (!lineMyPassName.equals("null")) {
                try {
                    Iterator<ClientsField> it = ServerManager.clients.iterator();
                    while (it.hasNext()) {
                        ClientsField com = it.next();
                        if ((com.getPass().equals(lineMyPassName))&&(!com.equals(myClient))) {
                            com.out.writeUTF("newGameMove " + myMove);
                            com.out.flush();
                        }
                    }
                } catch (IOException ignored) {}
            }
        }
    }

    // Присваивает имя профиля текущему клиенту
    private void setClientProfile(String myProfile) {
        ClientsField com = getThisClientProfile();
        if (com!=null) com.setProfile(myProfile);
    }

    // Активирует/деактивирует состояние отправки уведомлений пользователю во время игры о его одиночестве
    private void setСheckAlone(String value) {
        if (value.equals("true")) checkAlone = true;
        else if (value.equals("false")) checkAlone = false;
    }

    // Производит выход клиента из текущего игрового стола
    private void exitFromThisGameSelect() {
        ClientsField com = getThisClientProfile();
        if (com!=null) com.setPass("null");
    }

    // Закрытие клиента-потока в случае, если текущий клиент или сервер были закрыты
    private void clientDestroy() {
        exitFromThisGameSelect();
        try { client.close(); } catch (IOException ignored) {}
        ClientsField.count--;
    }
}
