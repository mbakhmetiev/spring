package ru.geekbrains.SERVER;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Server serv;
    private String nick;

    List<String> blacklist;

    public String getNick() {
        return nick;
    }

    public ClientHandler(Server serv, Socket socket) {
        try {
            this.serv = serv;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.blacklist = new ArrayList<>();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        while (true) {
                            String str = in.readUTF();
                            if(str.startsWith("/auth")) {
                                String[] tokens = str.split(" ");
                                String currentNick = DBServices.getDbService().getNickByLoginAndPass(tokens[1], tokens[2]);
                                if (currentNick != null) {
                                    if (!serv.isNickBusy(currentNick)) {
                                        sendMsg("/authok");
                                        nick = currentNick;
//                                        ClientHandler.this.blacklist = AuthService.getBlackList(nick);
                                        blacklist = DBServices.getDbService().getBlackList(nick);
                                        serv.subscribe(ClientHandler.this);
                                        break;
                                    } else {
                                        sendMsg("User already logged");
                                    }
                                } else {
                                    sendMsg("Wrong login/password");
                                }
                            }
                        }

                        while (true) {
                            String str = in.readUTF();
                            if (str.startsWith("/")) {
                                if (str.equalsIgnoreCase("/end")) {
                                    sendMsg("/clientClose");
                                    break;
                                }
                                if (str.startsWith("/w ")) {
                                    String[] tokens = str.split(" ", 3);
                                    serv.sendPersonalMsg(ClientHandler.this, tokens[1], tokens[2]);
                                }
                                if (str.startsWith("/bladd ")) {
                                    String[] tokens = str.split(" ");
                                    blacklist.add(tokens[1]);
                                    try {
                                        DBServices.getDbService().addToBlacklist(nick, tokens[1]);
                                        sendMsg("User " + tokens[1] + " added to your blacklist");
                                    } catch (SQLException e) {
                                        if (e.getErrorCode() == 1062) {
                                            sendMsg("User" + tokens[1] + " is already in your blacklist");
                                        } else {
                                            sendMsg("Failed to add user " + tokens[1] + " to your blacklist" + "\n" + "Error: " +e.getMessage());
                                        }
                                    }
                                }
                                if (str.startsWith("/blrm")){
                                    String[] tokens = str.split(" ");
                                   if(checkBlackList(tokens[1])) {
                                       blacklist.remove(tokens[1]);
                                       try {
                                           DBServices.getDbService().removeFromBlacklist(nick, tokens[1]);
                                           sendMsg("User" + tokens[1] + " removed from your blacklist");
                                       } catch (SQLException e) {
                                           sendMsg("Failed to remove user " + tokens[1] + " from your blacklist" + "\n" + "Error: " +e.getMessage());
                                       }
                                   } else {
                                     sendMsg("User " + tokens[1] + " is not in your blacklist");
                                       }
                                   }
                                   } else {
                                        serv.broadcastMsg(ClientHandler.this, nick + ": " + str);
                                }
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                } finally {
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        serv.unsubscribe(ClientHandler.this);
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean checkBlackList(String nick) {
        return blacklist.contains(nick);
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
