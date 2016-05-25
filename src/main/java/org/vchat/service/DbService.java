package org.vchat.service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import org.vchat.controller.VChat;

import javax.activation.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 * Author: SACHIN
 * Date: 5/21/2016.
 */
public class DbService extends AbstractVerticle{
    /* private Connection connection = null; private String driverName = "com.mysql.jdbc.Driver"; private String userName = "root"; private String password = ""; private String url = "jdbc:mysql://localhost:3303/vchat"; public DbService(){ try { Class.forName(driverName); connection = DriverManager.getConnection(url,userName,password); } catch (ClassNotFoundException | SQLException e) { e.printStackTrace(); } } public Connection getConnection() { return connection; } public void setConnection(Connection connection) { this.connection = connection; } */

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new DbService());

    }
    public void start() throws Exception {
        JDBCClient client = JDBCClient.createShared(vertx,new JsonObject()
                .put("url", "jdbc:mysql://localhost:3306/vchat")
                .put("driver_class", "com.mysql.jdbc.Driver")
                .put("user","root")
                .put("password","1122")
                .put("max_pool_size", 10)

        );
        client.getConnection(conn -> {
            if (conn.failed()) {
                System.err.println(conn.cause().getMessage());
                return;
            }
            final SQLConnection connection = conn.result();
            // create a test table
            connection.execute("create table test(id int primary key, name varchar(255))", create -> {
                        if (create.failed()) {
                            System.err.println("Cannot create the table");
                            create.cause().printStackTrace();
                            return;
                        }
                    });
               //insert some test data
                connection.execute("insert into test values (1, 'Hello'), (2, 'World')", insert -> {

                    // query some data with arguments
                    connection.queryWithParams("select * from test where id = ?", new JsonArray().add(2), rs -> {
                        if (rs.failed()) {
                            System.err.println("Cannot retrieve the data from the database");
                            rs.cause().printStackTrace();
                            return;
                        }

                        for (JsonArray line : rs.result().getResults()) {
                            System.out.println(line.encode());
                        }

                        // and close the connection
                        connection.close(done -> {
                            if (done.failed()) {
                                throw new RuntimeException(done.cause());
                            }
                        });
                    });
                });
            });
    }
}





