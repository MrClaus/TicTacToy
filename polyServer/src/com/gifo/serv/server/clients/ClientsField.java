package com.gifo.serv.server.clients;

/**
 * Created by gifo.
 */

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


// Класс для работы со списком клиента вне запущенных потоков
public class ClientsField {

    public Socket client; // объект клиента
    public DataOutputStream out; // объект для отправки сообщений клиенту
    public static int count = 0; // Количество подключенных клиентов к серверу
    private String game = "null"; // значение выбранного игрового стола клиентом на сервере
    private String profile = ""; // имя профиля подключившегося клиента

    // Конструктор класса списка Клиентов
    public ClientsField(Socket client) {
        this.client = client;
        try {
            out = new DataOutputStream(this.client.getOutputStream());
            count++;
        } catch (IOException e) { System.out.println("Бывает и такое..."); }
    }

    // Чтение и запись информации о выбранном игровом столе
    public void setPass(String st) { game = st; }
    public String getPass() { return game; }

    // Чтение и запись информации о игровом профиле клиента
    public void setProfile(String st) { profile = st; }
    public String getProfile() { return profile; }
}
