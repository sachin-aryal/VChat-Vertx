package org.vchat.service;

import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import org.vchat.Model.Member;

/**
 * Author: SACHIN
 * Date: 5/25/2016.
 */
public class UserService {
    static Logger logger = LoggerFactory.getLogger(UserService.class);

    public static void registerUser(String name,String userName){
        DbService.getClient().getConnection(conn->{
            if (conn.failed()) {
                logger.error(conn.cause().getMessage());
                return;
            }
            final SQLConnection connection = conn.result();
            connection.queryWithParams("select username from member where username=?",new JsonArray().add(userName),res->{
                if (res.failed()) {
                    logger.error("Cannot retrieve the data from the database");
                    DbService.closeConnection(connection);
                }else {
                    if (res.result().getNumRows() > 0) {
                        connection.updateWithParams("update member set status=? where username=?",
                                new JsonArray().add(1).add(userName), update -> {
                                    if (update.failed()) {
                                        logger.error("Updating Status of User Failed.");
                                        DbService.closeConnection(connection);
                                    } else {
                                        logger.info("Updated Successfully");
                                        DbService.closeConnection(connection);
                                    }
                                });
                    } else {
                        connection.execute("INSERT INTO member(username,name,status) values('" + userName + "','" + name + "',1)", insert -> {
                            if (insert.failed()) {
                                logger.error("Failed Registering user.");
                                DbService.closeConnection(connection);
                            } else {
                                logger.info("User Created Successfully");
                                DbService.closeConnection(connection);
                            }
                        });
                    }
                }
            });

        });

    }

    public static void getMemberId(Member member){
        DbService.getClient().getConnection(conn->{
            if(conn.failed()){
                logger.error("Failed Connection to database");
            }else{
                SQLConnection connection = conn.result();
                connection.queryWithParams("select id from member where username = ?",new JsonArray().add(member.getUserName()),memberId->{
                    ResultSet resultSet =  memberId.result();
                    if(resultSet.getNumRows()>0) {
                        int uId = resultSet.getResults().get(0).getInteger(0);
                        member.setUserId(uId);
                        DbService.closeConnection(connection);
                    }else{
                        DbService.closeConnection(connection);
                    }
                });
            }

        });
    }


}
