package com.gifo.screen.client;

/**
 * Created by gifo.
 */

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;


// Создание клиента и подключение его к серверу на основе клиентских настроект
public class Client {

    String myPROFILE; // имя профиля клиента
    String serverIP; // значение ip сервера, к которому клиент подключается
    int serverPORT; // значение порта сервера, к которому клиент подключается по ip

    public Socket socket; // объект - подключаемый клиент
    static int started = 0; // переменная состояния подключения клиента
    static String message = ""; // хранит значение полученного сообщения от сервера
    private DataOutputStream out; // объект записи данных на подключенный сервер

    public Client(String setClient) { if (getInfoStartClient(setClient)) start(); }

    // Проверяет корректность передаваемого конструктору сет-файла и загружает клиентские настройки
    public boolean getInfoStartClient(String setFile) {
        boolean isInfoClient = false;
        serverIP = "";
        serverPORT = 0;
        myPROFILE = "";
        try {
            String strLine;
            FileInputStream fstream = new FileInputStream(setFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            while ((strLine = br.readLine()) != null) {
                String[] lines = strLine.split(":");
                String[] toLines = lines[1].split("/");
                if (lines[0].equals("my-profile")) myPROFILE = toLines[0];
                else if (lines[0].equals("server-port")) serverPORT = Integer.parseInt(toLines[0]);
                else if (lines[0].equals("server-ip")) serverIP = toLines[0];
                if (serverPORT!=0&&serverIP.length()!=0&&myPROFILE.length()!=0) isInfoClient = true;
            }
        } catch (IOException e) { started = -1; }
        return isInfoClient;
    }

    // Подключение к серверу
    public void start() {
        try {
            socket = new Socket(serverIP, serverPORT);
            started = 1;
            out = new DataOutputStream(socket.getOutputStream());
            Thread client = new Thread(new Messages(socket));
            client.start();
        } catch (UnknownHostException e) { started = -2; } catch (IOException e) { started = -2; }
    }

    // Методы возвращают имя профиля, ip адрес и номер порта сервера
    public String getClientProfile() { return myPROFILE; }
    public String getServerHost() { return serverIP; }
    public int getServerPort() { return serverPORT; }

    // Возвращает состояние подключения клиента к серверу
    public int getStarted() { return started; }

    // Чтение и отправка сообщений от клиента к серверу, обнуление значения полученного сообщения
    public void reMessage() { message = ""; } // обнуление сообщения
    public String getMessage() { return message; }
    public void sentMessage(String msg) {
        if (!socket.isOutputShutdown()) {
            try {
                out.writeUTF(msg);
                out.flush();
            } catch (IOException e) { started = -2; }
        } else started = -2;
    }

    // В отличие от метода getMessage, который считывает напрямую текущее состояние сообщения у клиента
    // метод messageWaiting ставит текущий поток в ожидание, пока не получит определенный ответ от сервера
    // или связь с ним не будет разорвана!
    // Рекомендуется использовать после отправки единичного запроса, и когда все ответы на предыдущие
    // запросы были получены.
    public String messageWaiting(String answer) {
        String msg = "";
        while ((msg.indexOf(answer + " ")==-1)&&(getStarted()==1)) msg = getMessage();
        if (msg.indexOf(answer + " ")!=-1) return msg;
        else return "error"; // возвращает в случае, если цикл ожидания был прерван разрывом соединения с сервером
    }
}
