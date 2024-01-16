package net.cursedbreath.bansystemv3.mysql;

import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class MySQLConnectionPool {

    private final String databaseUrl;
    private final String userName;
    private final String password;
    private int maxPoolSize = 0;
    private int connNum = 0;

    private Logger logger;

    Stack<Connection> freePool = new Stack<>();
    Set<Connection> occupiedPool = new HashSet<>();

    private static final String SQL_VERFYCONN = "SELECT 2";

    public MySQLConnectionPool(String databaseUrl, String userName, String password, int maxPoolSize, Logger logger){
        this.databaseUrl = databaseUrl;
        this.userName = userName;
        this.password = password;
        this.maxPoolSize = maxPoolSize;
        this.logger = logger;
    }

    public synchronized Connection getConnection() throws SQLException {

        try {
            Connection conn = null;
            conn = getConnectionFromPool();

            if(conn == null){
                if (isFull()) {
                    logger.error("MySQL connection pool ist full! Connection could not be established");
                    throw new SQLException("The Connection pool is full.");
                }
                conn = createNewConnectionForPool();
            }

            conn = makeAvailable(conn);
            return conn;
        } catch (ClassNotFoundException e) {
            logger.error("MySQL connection could not be established: " + e.getMessage());
            throw new SQLException("The Connection could not be established.");
        }

    }

    private synchronized boolean isFull() {
        return ((freePool.size() == 0) && (connNum >= maxPoolSize));
    }

    private Connection createNewConnection() throws SQLException, ClassNotFoundException {
        Connection conn = null;
        conn = DriverManager.getConnection(databaseUrl, userName, password);
        return conn;
    }

    private Connection createNewConnectionForPool() throws SQLException, ClassNotFoundException {
        Connection conn = createNewConnection();
        connNum++;
        occupiedPool.add(conn);
        return conn;
    }

    public synchronized void returnConnection(Connection conn) throws SQLException {
        if (conn == null){
            throw new NullPointerException();
        }
        if(!occupiedPool.remove(conn)){
            throw new SQLException("The Connection is returned already or it isn't for this pool");
        }
        freePool.push(conn);
    }

    private Connection getConnectionFromPool(){
        Connection conn = null;
        if(freePool.size() > 0){
            conn = freePool.pop();
            occupiedPool.add(conn);
        }
        return conn;
    }

    private Connection makeAvailable(Connection conn) throws SQLException, ClassNotFoundException {
        if(isConnectionAvailable(conn)){
            return conn;
        }

        occupiedPool.remove(conn);
        connNum--;
        conn.close();
        conn = createNewConnection();
        occupiedPool.add(conn);
        connNum++;
        return conn;
    }

    private boolean isConnectionAvailable(Connection conn){
        try (Statement st = conn.createStatement()) {
            st.executeQuery(SQL_VERFYCONN);
            return true;
        } catch (SQLException e){
            return false;
        }
    }

}
