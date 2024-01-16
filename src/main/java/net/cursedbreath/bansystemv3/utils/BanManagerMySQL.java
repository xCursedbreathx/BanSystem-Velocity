package net.cursedbreath.bansystemv3.utils;

import com.velocitypowered.api.proxy.ProxyServer;
import net.cursedbreath.bansystemv3.interfaces.BanManager;
import net.cursedbreath.bansystemv3.mysql.MySQLConnectionPool;
import net.cursedbreath.bansystemv3.pluginmessaging.SendStatusUpdate;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class BanManagerMySQL implements BanManager {

    private static MySQLConnectionPool connectionPool;

    private static ProxyServer proxyServer;

    private static Logger logger;

    public BanManagerMySQL(MySQLConnectionPool connectionPool, ProxyServer proxyServer, Logger logger) {
        BanManagerMySQL.connectionPool = connectionPool;
        BanManagerMySQL.proxyServer = proxyServer;
        BanManagerMySQL.logger = logger;
    }



    public void punishPlayer(UUID player, UUID creator, String reason, String punishment, long duration) {

        Connection conn = null;

        try {

            try {

                conn = connectionPool.getConnection();

                PreparedStatement punishPlayerStmt = conn.prepareStatement("INSERT INTO active_punishment_infos (duration, banned, creator, reason, punishment) VALUES (?, ?, ?, ?, ?)");

                punishPlayerStmt.setLong(1, duration);
                punishPlayerStmt.setString(2, player.toString());
                punishPlayerStmt.setString(3, creator.toString());
                punishPlayerStmt.setString(4, reason);
                punishPlayerStmt.setString(5, punishment);

                punishPlayerStmt.execute();

            } finally {
                if(conn != null) {
                    connectionPool.returnConnection(conn);
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();

        }

    }

    @Override
    public boolean isBanned(UUID playerUniqueID) {

        Connection conn = null;
        try {

                try {

                    conn = connectionPool.getConnection();

                    PreparedStatement isBannedStmt = conn.prepareStatement("SELECT * FROM active_punishment_infos WHERE banned = ? AND punishment = ?");

                    isBannedStmt.setString(1, playerUniqueID.toString());
                    isBannedStmt.setString(2, "BAN");

                    ResultSet result = isBannedStmt.executeQuery();

                    return result.next();

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

    @Override
    public boolean isMuted(UUID playerUniqueID) {

        Connection conn = null;
        try {

            try {

                conn = connectionPool.getConnection();

                PreparedStatement isBannedStmt = conn.prepareStatement("SELECT * FROM active_punishment_infos WHERE banned = ? AND punishment = ?");

                isBannedStmt.setString(1, playerUniqueID.toString());
                isBannedStmt.setString(2, "CHAT");

                ResultSet result = isBannedStmt.executeQuery();

                return result.next();

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


    public ResultSet getPunishmentInformations(UUID player) {

        Connection conn = null;
        try {
            try {

                    conn = connectionPool.getConnection();

                    PreparedStatement getPunishmentInfosStmt = conn.prepareStatement("SELECT * FROM active_punishment_infos WHERE banned = ?");

                    getPunishmentInfosStmt.setString(1, player.toString());

                    getPunishmentInfosStmt.execute();

                    return getPunishmentInfosStmt.getResultSet();

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


    public ResultSet getPunishmentHistory(UUID player) {

        Connection conn = null;
        try {
            try {

                conn = connectionPool.getConnection();

                PreparedStatement getPunishmentHistoryStmt = conn.prepareStatement("SELECT * FROM punishment_log WHERE banned = ? ORDER BY id DESC LIMIT 10");

                getPunishmentHistoryStmt.setString(1, player.toString());

                return getPunishmentHistoryStmt.executeQuery();

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


    public ResultSet getAllBannedNames() {

        Connection conn = null;

        try {

            try {

                conn = connectionPool.getConnection();

                PreparedStatement getAllBannedNamesStmt = conn.prepareStatement("SELECT * FROM active_punishment_infos WHERE punishment = ?");

                getAllBannedNamesStmt.setString(1, "BAN");

                return getAllBannedNamesStmt.executeQuery();

            } finally {
                if(conn != null) {
                    connectionPool.returnConnection(conn);
                }

            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }


    public int repeatedPunishCounter(UUID player, String reason) {

        Connection conn = null;

        try {

            try {

                conn = connectionPool.getConnection();

                PreparedStatement repeatedPunishCounterStmt = conn.prepareStatement("SELECT * FROM punishment_log WHERE banned = ? AND reason = ?");

                repeatedPunishCounterStmt.setString(1, player.toString());

                repeatedPunishCounterStmt.setString(2, reason);

                repeatedPunishCounterStmt.execute();

                ResultSet resultSet = repeatedPunishCounterStmt.getResultSet();

                int counter = 0;

                while(resultSet.next()) {
                    counter++;
                }

                return counter;

            } finally {
                if(conn != null) {
                    connectionPool.returnConnection(conn);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;

    }


    public void removePunishment(UUID player) {

        Connection conn = null;

        try {

            try {

                conn = connectionPool.getConnection();

                PreparedStatement autoRemovePunishmentStmt = conn.prepareStatement("DELETE FROM active_punishment_infos WHERE banned = ?");

                autoRemovePunishmentStmt.setString(1, player.toString());

                autoRemovePunishmentStmt.execute();

                SendStatusUpdate.sendStatusUpdate("status", player.toString(), false);


            } finally {
                if(conn != null) {
                    connectionPool.returnConnection(conn);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


}
