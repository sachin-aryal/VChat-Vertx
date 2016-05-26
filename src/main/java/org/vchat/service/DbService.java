package org.vchat.service;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;


/**
 * Author: SACHIN
 * Date: 5/21/2016.
 */
public class DbService {
    private static JDBCClient client = null;
    static Logger logger = LoggerFactory.getLogger(DbService.class);

    public static void buildConnection(Vertx vertx) {
        client = JDBCClient.createShared(vertx, new JsonObject()
                .put("url", "jdbc:mysql://localhost:3306/vchat")
                .put("driver_class", "com.mysql.jdbc.Driver")
                .put("user", "root")
                .put("password", "")
                .put("max_pool_size", 10)

        );
        System.out.println("Jdbc Client Initialized Successfully");
    }

    public static JDBCClient getClient() {
        return client;
    }

    public static void closeConnection(SQLConnection connection){
        connection.close(connectionCloser->{
            if(connectionCloser.failed()){
                logger.error("Error while closing Db Connection");
            }else{
                logger.info("Db Connection Closed Successfully");
            }
        });
    }
}






