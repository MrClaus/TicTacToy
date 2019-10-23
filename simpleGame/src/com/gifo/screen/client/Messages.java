package com.gifo.screen.client;

/**
 * Created by gifo.
 */

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;


// Чтение сообщений от сервера в потоке
public class Messages implements Runnable {
    Socket socket;
    public Messages(Socket socket) { this.socket = socket; }
    public void run() {
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            while (!socket.isOutputShutdown()) {
                String st = in.readUTF();
                Client.message = st;
            }
        } catch(IOException e) { Client.started = -2; }
    }
}
