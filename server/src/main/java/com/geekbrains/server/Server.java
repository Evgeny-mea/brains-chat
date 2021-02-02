package com.geekbrains.server;

import com.sun.xml.internal.fastinfoset.vocab.Vocabulary;

import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {
    private boolean shownIoErrors = false;
    private Vector<ClientHandler> clients;
    private AuthService authService;
    private ExecutorService executorService;



    public AuthService getAuthService() {
        return authService;
    }

    public Server() {
        clients = new Vector<>();
        authService = new SimpleAuthService();
        executorService = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            System.out.println("Сервер запущен на порту 8189");
            while (true) {
                SqlClient.connect();
                Socket socket = serverSocket.accept();
                new ClientHandler(this, socket, executorService);
                System.out.println("Подключился новый клиент");

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
        System.out.println("Сервер завершил свою работу");
    }

    public void broadcastMsg(String msg) {
        for (ClientHandler o : clients) {
            o.sendMsg(msg);
            wrtMsgToLogFile(msg);

        }
    }
    private void wrtMsgToLogFile(String msg) {
        try (FileWriter out = new FileWriter("log.txt", true)) {
            out.write(msg + "\n");
            out.flush();
        } catch (IOException e) {
            if (!shownIoErrors) {
                shownIoErrors = true;
                //showException(Thread.currentThread(), e);
            }
        }
    }
    public void privateMsg(ClientHandler sender, String receiverNick, String msg) {
        if (sender.getNickname().equals(receiverNick)) {
            sender.sendMsg("заметка для себя: " + msg);
            return;
        }
        for (ClientHandler o : clients) {
            if (o.getNickname().equals(receiverNick)) {
                o.sendMsg("от " + sender.getNickname() + ": " + msg);
                sender.sendMsg("для " + receiverNick + ": " + msg);
                return;
            }
        }
        sender.sendMsg("Клиент " + receiverNick + " не найден");
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastClientsList();
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientsList();
    }

    public boolean isNickBusy(String nickname) {
        for (ClientHandler o : clients) {
            if (o.getNickname().equals(nickname)) {
                return true;
            }
        }
        return false;
    }

    public void broadcastClientsList() {
        StringBuilder sb = new StringBuilder(15 * clients.size());
        sb.append("/clients ");
        // '/clients '
        for (ClientHandler o : clients) {
            sb.append(o.getNickname()).append(" ");
        }
        // '/clients nick1 nick2 nick3 '
        sb.setLength(sb.length() - 1);
        // '/clients nick1 nick2 nick3'
        String out = sb.toString();
        for (ClientHandler o : clients) {
            o.sendMsg(out);
        }
    }
}
