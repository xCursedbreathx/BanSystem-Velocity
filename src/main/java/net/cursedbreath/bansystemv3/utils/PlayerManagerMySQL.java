package net.cursedbreath.bansystemv3.utils;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import net.cursedbreath.bansystemv3.BanSystem_Velocity;
import net.cursedbreath.bansystemv3.interfaces.PlayerManager;
import net.cursedbreath.bansystemv3.mysql.MySQLConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerManagerMySQL implements PlayerManager {

    private MySQLConnectionPool connectionPool;

    private ProxyServer proxyServer;

    public PlayerManagerMySQL(MySQLConnectionPool connectionPool, ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
        this.connectionPool = connectionPool;
    }

    /**
     * Checks if a player exists in the database
     * @param name
     * @return
     */
    public boolean playerExists(String name) {

        Connection conn = null;

        try {

            try {

                conn = connectionPool.getConnection();

                PreparedStatement playerExistsStmt = conn.prepareStatement("SELECT * FROM player_data WHERE username = ?");

                playerExistsStmt.setString(1, name);

                ResultSet result = playerExistsStmt.executeQuery();

                while (result.next()) {

                    return true;

                }

                return false;

            } finally {
                if(conn != null) {
                    connectionPool.returnConnection(conn);
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();

        }

        return false;
    }

    /**
     * Checks if a player exists in the database using uuid
     * @param uuid
     * @return
     */
    public boolean playerExists(UUID uuid) {

        Connection conn = null;

        try {

            try {

                conn = connectionPool.getConnection();

                PreparedStatement playerExistsStmt = conn.prepareStatement("SELECT * FROM player_data WHERE uuid = ?");

                playerExistsStmt.setString(1, uuid.toString());

                ResultSet result = playerExistsStmt.executeQuery();

                while (result.next()) {

                    return true;

                }

                return false;

            } finally {
                if(conn != null) {
                    connectionPool.returnConnection(conn);
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();

        }

        return false;

    }

    /**
     * Creates a new Player in the database
     * @param player
     */
    public void createPlayer(Player player) {

        String uuid = player.getUniqueId().toString();
        String name = player.getUsername();

        Connection conn = null;

        try {

            try {

                conn = connectionPool.getConnection();

                PreparedStatement createPlayerStmt = conn.prepareStatement("INSERT INTO player_data (uuid, username) VALUES (?, ?)");

                createPlayerStmt.setString(1, uuid);
                createPlayerStmt.setString(2, name);

                createPlayerStmt.execute();

            } finally {
                if(conn != null) {
                    connectionPool.returnConnection(conn);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * Creates a new Player in the database
     * @param player
     */
    public void updatePlayerName(Player player) {

        String uuid = player.getUniqueId().toString();
        String name = player.getUsername();

        Connection conn = null;

        try {

            try {

                conn = connectionPool.getConnection();

                PreparedStatement createPlayerStmt = conn.prepareStatement("UPDATE player_data SET username = ? WHERE uuid = ?");

                createPlayerStmt.setString(1, name);
                createPlayerStmt.setString(2, uuid);

                createPlayerStmt.executeUpdate();

            } finally {
                if(conn != null) {
                    connectionPool.returnConnection(conn);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * Gets the UUID from the given Playername from the database
     * @param name Playername
     * @return UUID
     */
    public UUID getUUID(String name) {

        Connection conn = null;

        try {

            try {

               conn = connectionPool.getConnection();

                PreparedStatement getUUIDStmt = conn.prepareStatement("SELECT uuid FROM player_data WHERE username = ?");

                getUUIDStmt.setString(1, name);

                ResultSet result = getUUIDStmt.executeQuery();

                while (result.next()) {

                    return UUID.fromString(result.getString("uuid"));

                }

                return null;

            } finally {
                if(conn != null) {
                    connectionPool.returnConnection(conn);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;

    }

    /**
     * Gets the UUID from the given Playername from the database
     * @param uuid Player UUID
     * @return UUID
     */
    public String getName(UUID uuid) {

        Connection conn = null;

        try {

            try {

                conn = connectionPool.getConnection();

                PreparedStatement getUsernameStmt = conn.prepareStatement("SELECT username FROM player_data WHERE uuid = ?");

                getUsernameStmt.setString(1, uuid.toString());

                ResultSet result = getUsernameStmt.executeQuery();

                while(result.next()) {

                    return result.getString("username");

                }

                return null;

            } finally {
                if(conn != null) {
                    connectionPool.returnConnection(conn);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;

    }

}
