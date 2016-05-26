package org.vchat.service;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Author: SACHIN
 * Date: 5/26/2016.
 */
public class MessageService {

    static Logger logger = LoggerFactory.getLogger(MessageService.class);

    public static void saveMessage(JsonObject data, Vertx vertx) {
        final int[] sender = {0};
        final int[] receiver = {0};
        vertx.executeBlocking(future -> {
            DbService.getClient().getConnection(conn -> {
                if (conn.failed()) {
                    logger.error("Connection failed with database");
                } else {
                    SQLConnection connection = conn.result();
                    connection.queryWithParams("select id from member where username = ?", new JsonArray().add(data.getString("myId")), memberId -> {
                        ResultSet resultSet = memberId.result();
                        if (resultSet.getNumRows() > 0) {
                            sender[0] = resultSet.getResults().get(0).getInteger(0);

                            connection.queryWithParams("select id from member where username = ?", new JsonArray().add(data.getString("friendId")), friendMemberId -> {
                                ResultSet friendIdResult = friendMemberId.result();
                                if (friendIdResult.getNumRows() > 0) {
                                    receiver[0] = friendIdResult.getResults().get(0).getInteger(0);

                                    Date currentTime = new Date();
                                    SimpleDateFormat formatter = new SimpleDateFormat("Y-M-d h:m:s a");
                                    String currentTimeString = formatter.format(currentTime);
                                    String insertToChat = "INSERT INTO chat(sender,receiver,chatTime,message) values(" + sender[0] + "," + receiver[0] + "," +
                                            "'" + currentTimeString + "','" + data.getString("message") + "')";

                                    connection.execute(insertToChat, storeChat -> {
                                        if (storeChat.failed()) {
                                            DbService.closeConnection(connection);
                                            logger.error("Error while storing the chat data " + storeChat.cause().toString());
                                        } else {
                                            DbService.closeConnection(connection);
                                            data.put("classifier", "noaddress");
                                            future.complete(data);
                                        }
                                    });
                                }else{
                                    DbService.closeConnection(connection);
                                }
                            });
                        }else{
                            DbService.closeConnection(connection);
                        }
                    });
                }
            });

        }, res -> {
            logger.info("Stored Message Successfully"+res.result());
            vertx.eventBus().publish("chat.to.client", res.result());
        });
    }


    public static void sendMessageToClient(JsonObject data,Vertx vertx){
        String myId = data.getString("myId");
        String friendId = data.getString("friendId");
        logger.info("Request Data is "+data);
        final int[] sender = {0};
        final int[] receiver = {0};
        final int[] index = {1};

        vertx.executeBlocking(future->{
            DbService.getClient().getConnection(conn -> {
                if (conn.failed()) {
                    logger.error("Connection failed with database");
                } else {
                    SQLConnection connection = conn.result();
                    connection.queryWithParams("select id from member where username = ?", new JsonArray().add(data.getString("myId")), memberId -> {
                        ResultSet resultSet = memberId.result();
                        if (resultSet.getNumRows() > 0) {
                            sender[0] = resultSet.getResults().get(0).getInteger(0);
                            connection.queryWithParams("select id from member where username = ?", new JsonArray().add(data.getString("friendId")), friendMemberId -> {
                                ResultSet friendIdResult = friendMemberId.result();
                                if (friendIdResult.getNumRows() > 0) {
                                    receiver[0] = friendIdResult.getResults().get(0).getInteger(0);

                                    System.out.println("Sender Id is "+sender[0]);
                                    System.out.println("Receiver Id is "+receiver[0]);

                                    String getAllMessage = "select *from chat where (sender="+sender[0]+" and" +
                                            " receiver="+receiver[0]+") or (sender="+receiver[0]+" and receiver="+sender[0]+")";


                                    connection.query(getAllMessage,allMessage->{

                                        if(allMessage.failed()){
                                            logger.error("Fetch Message Failed");
                                        }else{
                                            ResultSet allMessageSet = allMessage.result();

                                            List<JsonArray> allRow = allMessageSet.getResults();
                                            List<String> messageList = new ArrayList<>();
                                            for(JsonArray row : allRow){
                                                int sendId = row.getInteger(1);
                                                if(sendId==sender[0]){
                                                    messageList.add(myId+": "+row.getString(3));
                                                }else{
                                                    messageList.add(friendId+": "+row.getString(3));
                                                }
                                                if(allMessageSet.getNumRows()== index[0]){
                                                    connection.close(connectionCloser->{
                                                        if(connectionCloser.failed()){
                                                            logger.error("Error while closing Db Connection");
                                                        }else{
                                                            logger.info("Db Connection Closed Successfully.");
                                                        }
                                                    });
                                                    data.put("message", messageList);
                                                    data.put("classifier","fetchMessage");
                                                    future.complete(data);
                                                }
                                                index[0]++;
                                            }
                                        }

                                    });

                                }
                            });
                        }else{
                            connection.close(connectionCloser->{
                                if(connectionCloser.failed()){
                                    logger.error("Error while closing Db Connection");
                                }else{
                                    logger.info("Db Connection Closed Successfully.");
                                }
                            });
                        }
                    });
                }
            });

        },res->{
            logger.info("Response Data is "+res.result());
            vertx.eventBus().publish("chat.to.client", res.result());
        });

    }

}
