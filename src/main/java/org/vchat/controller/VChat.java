package org.vchat.controller;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import org.vchat.service.DbService;
import org.vchat.service.FriendService;
import org.vchat.service.MessageService;
import org.vchat.service.UserService;

/**
 * Author: SACHIN
 * Date: 5/6/2016.
 */
public class VChat extends AbstractVerticle{

    private final String twitterUrl = "https://api.twitter.com/oauth/authorize?oauth_token=hDz27wAAAAAAu6pmAAABVMGW3c0";

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new VChat());

    }

    public void start() {
        Router router = Router.router(vertx);
        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
        vertx.executeBlocking(future->{
            DbService.buildConnection(vertx);
            future.complete("Connected to Database");
        },res->{});

        router.route("/").handler(rtx -> {
            HttpServerResponse response = rtx.response();
            response.setChunked(true);
            response.sendFile("webroot/index.html");
        });

        router.route("/Js/*").handler(StaticHandler.create("webroot/Js"));
        router.route("/Css/*").handler(StaticHandler.create("webroot/Css"));
        router.route("/images/*").handler(StaticHandler.create("webroot/images"));


        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        BridgeOptions options = new BridgeOptions();
        options.addInboundPermitted(new PermittedOptions().setAddress("chat.to.server"));
        options.addOutboundPermitted(new PermittedOptions().setAddress("chat.to.client"));
        sockJSHandler.bridge(options);


        router.route("/clientController/*").handler(sockJSHandler);
        router.route("/result/*").handler(this::resultCallback);
        router.route("/logout/*").handler(this::logoutUser);
        router.route("/dashboard/*").handler(rtx->{
            System.out.println("Redirected from script");
            rtx.response().setChunked(true);
            rtx.response().sendFile("webroot/chat.html");
        });
        loginListner();
    }

    public void loginListner(){

        EventBus eb = vertx.eventBus();

        eb.consumer("chat.to.server",message->{
            System.out.println("Getting Request in Client Controller Event Bus");
            JsonObject data = (JsonObject) message.body();
            String classifier = data.getString("classifier");

            if(classifier.equals("loginUri")) {
                System.out.println("Request for Login URI");
                vertx.executeBlocking(future -> {
                    future.complete(OAuth.getOAuthParam());
                }, res -> {
                    System.out.println("Returned Return URI is "+res.result());
                    eb.publish("chat.to.client", res.result());
                });

            }else if(classifier.equals("getUserInfo")){
                System.out.println("Getting Request for User Info");
                vertx.executeBlocking(future -> {
                    JsonObject userInfo = OAuth.getUserInfo(data.getString("tokenKey"),data.getString("tokenKeySecret"),data.getString("verifier"));
                    UserService.registerUser(userInfo.getString("name"),userInfo.getString("screenName"));
                    future.complete(userInfo);
                }, res -> {
                    System.out.println("Returning User Info is "+res.result());
                    eb.publish("chat.to.client", res.result());
                });
            }else if(classifier.equals("fetchFriendList")){
                System.out.println("Fetching Friend List");
                FriendService.getFriendList(data.getString("whom"),vertx);
            }
            else if(classifier.equals("fetchMessage")) {
                System.out.println("Call for fetching Message");
                MessageService.sendMessageToClient(data, vertx);
            }else if(classifier.equals("sendMessage")){
                System.out.println("Storing Message To the Database");
                MessageService.saveMessage(data,vertx);
            }else if(classifier.equals("addFriend")){
                System.out.println("Add Friend Request from User");
                FriendService.addFriend(data,vertx);
            }else if(classifier.equals("acceptRequest")){

            }else if(classifier.equals("rejectRequest")){

            }
        });

    }
    public void resultCallback(RoutingContext routingContext){
        System.out.println("Callback Result");
        String verifier = routingContext.request().getParam("oauth_verifier");
        StringBuilder script = new StringBuilder();
        script.append("<script src=\"https://code.jquery.com/jquery-1.11.2.min.js\"></script>");
        script.append("<script type='text/javascript'>");
        script.append("$(function(){");
        script.append("localStorage.setItem('verifier','").append(verifier).append("');");
        script.append("window.location.href=").append("'http://localhost:8080/dashboard'");
        script.append("});");
        script.append("</script>");
        routingContext.response().putHeader("content-length",String.valueOf(script.toString().length()));
        routingContext.response().putHeader("content-type","text/html");
        routingContext.response().write(script.toString());
        routingContext.response().end();
    }

    public void logoutUser(RoutingContext routingContext){

        System.out.println("Logging Out the User");
        routingContext.response().setChunked(true);
        routingContext.response().sendFile("webroot/index.html");
    }
}
