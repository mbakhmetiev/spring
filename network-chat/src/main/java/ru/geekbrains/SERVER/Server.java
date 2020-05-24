package ru.geekbrains.SERVER;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Vector;

@Component
public class Server {
    private Vector<ClientHandler> clients;

    public Server(int port, DataSource dataSource) throws SQLException {
            clients = new Vector<>();
            ServerSocket server = null;
            Socket socket = null;

            try {
                DBServices.getDbService().connect(dataSource);

                server = new ServerSocket(port);
                System.out.println("Server launched");

                while (true) {
                    socket = server.accept();
                    System.out.println("Client connected");
                    new ClientHandler(this, socket);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            DBServices.getDbService().disconnect();
        }

    public void sendPersonalMsg(ClientHandler from, String nickTo, String msg) {
            for(ClientHandler o : clients) {
                if (o.getNick().equals(nickTo)) {
                    o.sendMsg("from " + from.getNick() + ":" + msg);
                    from.sendMsg("to " + nickTo + ": " + msg);
                    return;
                }
            }
            from.sendMsg("User " + nickTo + " not found in active chat");
    }

    public void broadcastMsg(ClientHandler from, String msg) {
        for (ClientHandler o: clients) {
            if (!o.checkBlackList(from.getNick()))
                o.sendMsg(msg);
        }
    }

    public boolean isNickBusy(String nick) {
            for(ClientHandler o : clients) {
                if (o.getNick().equals(nick)) {
                    return true;
                }
            }
            return false;
    }

    public void broadcastClientsList() {
        StringBuilder sb = new StringBuilder();
        sb.append("/clientslist ");
        for (ClientHandler o : clients) {
            sb.append(o.getNick() + " ");
        }
        String out = sb.toString();
        for (ClientHandler o : clients) {
            o.sendMsg(out);
        }
    }

    public void subscribe(ClientHandler client) {
        clients.add(client);
        broadcastClientsList();
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client);
        broadcastClientsList();
    }
}
