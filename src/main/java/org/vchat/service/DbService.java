package org.vchat.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Author: SACHIN
 * Date: 5/21/2016.
 */
public class DbService {

    private Connection connection = null;
    private String driverName = "com.mysql.jdbc.Driver";
    private String userName = "root";
    private String password = "";
    private String url = "jdbc:mysql://localhost:3303/vchat";

    public DbService(){
        try {
            Class.forName(driverName);
            connection = DriverManager.getConnection(url,userName,password);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

}
