package com.arakamitech.arakamimessageserver;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {

    private static final int PORT = 12345;
    private static HashMap<String, ClientHandler> clients = new HashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor iniciado en puerto " + PORT);

        while (true) {
            Socket socket = serverSocket.accept();
            ClientHandler handler = new ClientHandler(socket, clients);
            handler.start();
        }
    }

    static synchronized void broadcastUserList() {
        StringBuilder sb = new StringBuilder("USERLIST:");
        for (String user : clients.keySet()) {
            sb.append(user).append(",");
        }
        String userList = sb.toString();
        for (ClientHandler handler : clients.values()) {
            handler.sendCommand("TEXT", userList);
        }
    }

    static synchronized void addClient(String nickname, ClientHandler handler) {
        clients.put(nickname, handler);
        broadcastUserList();
    }

    static synchronized void removeClient(String nickname) {
        clients.remove(nickname);
        broadcastUserList();
    }

    static synchronized void sendPrivateMessage(String from, String to, String message) {
        ClientHandler handler = clients.get(to);
        if (handler != null) {
            handler.sendCommand("MSGFROM", from + ":" + message);
        }
    }

    static synchronized void sendFile(String from, String to, String filename, long size, DataInputStream in) throws IOException {
        ClientHandler handler = clients.get(to);
        if (handler != null) {
            handler.sendFile(from, filename, size, in);
        }
    }

}
