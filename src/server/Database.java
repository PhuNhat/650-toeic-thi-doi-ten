package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Database {

    String drivername = "com.mysql.cj.jdbc.Driver";
    String connectionURL;
    String username = "root";
    String password = "root";
    Statement stmt = null;
    ResultSet rs = null;
    Connection conn;
    String tableName;

    public Database(String mysqlUrl, int nodeId) {
        this.tableName = "server" + nodeId;
        
        // Parse MYSQL_URL and extract credentials if present
        try {
            // Handle both formats: mysql://user:pass@host:port/db and jdbc:mysql://host:port/db
            if (mysqlUrl == null || mysqlUrl.isEmpty()) {
                mysqlUrl = "jdbc:mysql://localhost:3306/db" + nodeId;
            }
            
            if (mysqlUrl.startsWith("mysql://")) {
                // Convert Railway format to JDBC format
                mysqlUrl = "jdbc:" + mysqlUrl;
            }
            
            // Parse URL to extract credentials if embedded
            if (mysqlUrl.contains("@")) {
                // Format: jdbc:mysql://user:password@host:port/database
                String userPart = mysqlUrl.substring(mysqlUrl.indexOf("://") + 3, mysqlUrl.indexOf("@"));
                if (userPart.contains(":")) {
                    this.username = userPart.substring(0, userPart.indexOf(":"));
                    this.password = userPart.substring(userPart.indexOf(":") + 1);
                    // Remove credentials from URL
                    mysqlUrl = mysqlUrl.substring(0, mysqlUrl.indexOf("://") + 3) + mysqlUrl.substring(mysqlUrl.indexOf("@") + 1);
                }
            }
            
            this.connectionURL = mysqlUrl;
            
            // Load MySQL driver - try new one first, then fallback to old
            try {
                Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
                System.out.println("[DB] MySQL driver (com.mysql.cj) loaded");
            } catch (Exception ex1) {
                try {
                    Class.forName("com.mysql.jdbc.Driver").newInstance();
                    System.out.println("[DB] MySQL driver (com.mysql) loaded (legacy)");
                } catch (Exception ex2) {
                    System.err.println("[DB ERROR] Neither MySQL driver found!");
                    throw ex2;
                }
            }
            
            System.out.println("[DB] Connecting to " + connectionURL + " as " + username);
            conn = DriverManager.getConnection(connectionURL, username, password);
            stmt = conn.createStatement();
            System.out.println("[DB] Connected! Table: " + tableName);
            
        } catch (Exception ex) {
            System.err.println("[DB ERROR] SQLException: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void insertData(String id, String content, String time, String status) {
        String sSQL = "INSERT INTO " + tableName + " VALUES ('" + id + "','" + content + "','" + time + "','" + status + "')";
        try {
            stmt.executeUpdate(sSQL);
        } catch (Exception e) {
            System.err.println("[DB ERROR] SQLException: " + e.getMessage());
        }
    }

    public void delData(String id) {
        try {
            String sSQL = "DELETE FROM " + tableName + " WHERE id='" + id + "'";
            stmt.executeUpdate(sSQL);
        } catch (Exception e) {
            System.err.println("[DB ERROR] SQLException: " + e.getMessage());
        }
    }

    public String getAllData() {
        String result = "";
        try {
            rs = stmt.executeQuery("SELECT * FROM " + tableName);
            while (rs.next()) {
                result += rs.getString("id") + " | " + rs.getString("content") + " | " + rs.getString("time") + " | " + rs.getString("status") + "\n";
            }
        } catch (Exception e) {
            System.err.println("[DB ERROR] SQLException: " + e.getMessage());
        }
        return result;
    }
}