package com.geekbrains.server;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public class ClientHandler {
    private String nickname;
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public String getNickname() {
        return nickname;
    }


    public ClientHandler(Server server, Socket socket, ExecutorService executorService) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            executorService.submit(() -> {
                try {
                    while (true) {
                        String msg = in.readUTF();
                        // /auth login1 pass1
                        if (msg.startsWith("/auth ")) {
                            String[] tokens = msg.split("\\s");
                            //String nick = server.getAuthService().getNicknameByLoginAndPassword(tokens[1], tokens[2]);
                            String nick = SqlClient.getNickname(tokens[1], tokens[2]);
                            if (nick != null && !server.isNickBusy(nick)) {
                                sendMsg("/authok " + nick);
                                nickname = nick;
                                server.subscribe(this);
                                break;
                            }
                        }
                    }
                    while (true) {
                        String msg = in.readUTF();
                        if (msg.startsWith("/")) {
                            if (msg.equals("/end")) {
                                sendMsg("/end");
                                break;
                            }
                            if (msg.startsWith("/w ")) {
                                String[] tokens = msg.split("\\s", 3);
                                server.privateMsg(this, tokens[1], tokens[2]);
                            }
                        } else {
                            server.broadcastMsg(nickname + ": " + msg);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    ClientHandler.this.disconnect();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void waitAuthorization(Server server) throws IOException {
        while (true) {
            String msg = in.readUTF();
            if (msg.startsWith("/auth")) {
                String[] tokens = msg.split("\\s");
                String nick = server.getAuthService().getNicknameByLoginAndPassword(tokens[1], tokens[2]);

                if (nick != null && !server.isNickBusy(nick)) {
                    sendMsg("/authok" + nick);
                    nickname = nick;
                    server.subscribe(this);
                    break;
                }
            }
        }
    }

    private void waitMessageOrCommand(Server server, AuthService authService) throws IOException {
        while (true) {
            String msg = in.readUTF();

            if (msg.startsWith("/")) {
                if (msg.equals("/end")) {
                    sendMsg("/end");
                    break;
                }
                checkPrivateMassageCommand(server, msg);
//                checkUpdateNicknameCommand(server, authService, msg);
            } else {
                server.broadcastMsg(nickname + ":" + msg);
            }
        }
    }

    private void checkPrivateMassageCommand(Server server, String msg) {
        if (msg.startsWith("/w")) {
            String[] tokens = msg.split("\\s", 3);
            server.privateMsg(this, tokens[1], tokens[2]);
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        server.unsubscribe(this);
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}