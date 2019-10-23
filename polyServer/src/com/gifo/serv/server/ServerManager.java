package com.gifo.serv.server;

/**
 * Created by gifo.
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.gifo.serv.server.clients.ClientsField; // Импорт класса списка клиентов


// Менеджер серверов был выделен отдельно в класс для запуска его в параллельном потоке,
// так как единичной отрисовки информации в окне программы недостаточно, поскольку
// если срернуть и развернуть окно обратно, холст окна ощищается, и нужно обновлять его постоянно,
// а ожидание подключения нового клиента к серверу временно прекращает исполнение кода далее...
public class ServerManager implements Runnable {

    ServerSocket server; // текущий сервер
    String[] passNames; // список игровых столов сервера
    static List<ClientsField> clients; // список подключенных клиентов

    // Конструктор класса ServerManager
    public ServerManager(ServerSocket server, String[] passNames) {
        this.server = server;
        this.passNames = passNames;
    }

    // Метод Runnable для запуска в пуле потоков
    public void run() {
        try {
            // Создаём список клиентов (объектов класса ClientsField)
            clients = new ArrayList<ClientsField>();

            // стартуем цикл при условии что серверный сокет не закрыт
            while (!server.isClosed()) {
                    Socket client = server.accept();
                    clients.add(new ClientsField(client));
                    Server.executeIt.execute(new ServerClient(client, passNames));
            }
        } catch (IOException e) { System.out.println("Server-Manager is closed"); }
    }

    // Метод закрывает все клиентские каналы, заодно и прекращает исполнение их в потоке
    public static void closeChannels() {
        Iterator<ClientsField> i = clients.iterator();
        while (i.hasNext()) {
            ClientsField com = i.next();
            try {
                com.out.writeUTF("disconnected");
                com.out.flush();
                com.out.close();
                com.client.close();
            } catch (IOException ignored) {}
        }
    }
}