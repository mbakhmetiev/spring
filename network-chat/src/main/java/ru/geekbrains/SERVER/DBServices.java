package ru.geekbrains.SERVER;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBServices {
    private Connection connection;
    private Statement stmt;

    private static final DBServices dbService = new DBServices();

    private DBServices() {};

    public static DBServices getDbService() {
        return dbService;
    }

    public void connect(DataSource dataSource) {
        try {
            stmt = dataSource.getConnection().createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getNickByLoginAndPass (String login, String pass) throws SQLException {
        String sql = String.format("SELECT nick FROM main WHERE login='%s' AND password='%s'", login, pass);
        ResultSet rs = stmt.executeQuery(sql);
        if(rs.next()) {
            return rs.getString("nick");
        }
        return null;
    }

    public void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getBlackList(String nick) throws SQLException {
        List<String> blacklist = new ArrayList<>();
        String sql = String.format("SELECT nick FROM main m JOIN blacklist bl ON bl.block_user = m.id WHERE bl.src_user = (SELECT id from main WHERE nick = '%s')", nick);
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            blacklist.add(rs.getString("nick"));
        }
        return blacklist;
    }

    public void addToBlacklist(String srcNick, String blockNick) throws SQLException {
        String sql = String.format("INSERT INTO blacklist (src_user, block_user) values ((SELECT id FROM main WHERE nick = '%s'), (SELECT id FROM main WHERE nick = '%s'))", srcNick, blockNick);
        stmt.executeUpdate(sql);
    }

    public void removeFromBlacklist(String srcNick, String blockNick) throws SQLException {
        String sql = String.format("DELETE FROM blacklist WHERE src_user = (SELECT id FROM main WHERE nick = '%s') AND block_user = (SELECT id FROM main WHERE nick = '%s')", srcNick, blockNick);
        stmt.executeUpdate(sql);
    }
}
