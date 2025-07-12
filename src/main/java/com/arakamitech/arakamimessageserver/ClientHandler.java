package com.arakamitech.arakamimessageserver;

import java.io.*;
import java.net.*;
import java.util.HashMap;

public class ClientHandler extends Thread {

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickname;
    private HashMap<String, ClientHandler> clients;

    public ClientHandler(Socket socket, HashMap<String, ClientHandler> clients) throws IOException {
        this.socket = socket;
        this.clients = clients;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    public void sendCommand(String cmd, String msg) {
        try {
            out.writeUTF(cmd);
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            System.out.println("Error enviando comando a " + nickname);
        }
    }

    public void sendFile(String from, String filename, long size, DataInputStream senderIn) throws IOException {
        out.writeUTF("SENDFILE");
        out.writeUTF(from);
        out.writeUTF(filename);
        out.writeLong(size);

        byte[] buffer = new byte[4096];
        long remaining = size;
        while (remaining > 0) {
            int read = senderIn.read(buffer, 0, (int) Math.min(buffer.length, remaining));
            out.write(buffer, 0, read);
            remaining -= read;
        }
        out.flush();
    }

    public void run() {
        try {
            out.writeUTF("SUBMITNICK");
            nickname = in.readUTF();
            ChatServer.addClient(nickname, this);

            while (true) {
                String cmd = in.readUTF();

                if ("MSGTO".equals(cmd)) {
                    String to = in.readUTF();
                    String msg = in.readUTF();
                    ChatServer.sendPrivateMessage(nickname, to, msg);
                } else if ("SENDFILE".equals(cmd)) {
                    String to = in.readUTF();
                    String filename = in.readUTF();
                    long size = in.readLong();
                    ChatServer.sendFile(nickname, to, filename, size, in);
                }
            }
        } catch (IOException e) {
            System.out.println("Cliente desconectado: " + nickname);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
            }
            ChatServer.removeClient(nickname);
        }
    }
}
