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
        router.route("/").handler(rtx -> {
            HttpServerResponse response = rtx.response();
            response.setChunked(true);
            response.sendFile("webroot/index.html");
        });

        router.route("/Js/*").handler(StaticHandler.create("webroot/Js"));
        router.route("/Css/*").handler(StaticHandler.create("webroot/Css"));


        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        BridgeOptions options = new BridgeOptions();
        options.addInboundPermitted(new PermittedOptions().setAddress("chat.to.server"));
        options.addOutboundPermitted(new PermittedOptions().setAddress("chat.to.client"));
        sockJSHandler.bridge(options);


        router.route("/clientController/*").handler(sockJSHandler);
        router.route("/result/*").handler(this::resultCallback);
        router.route("/logout/*").handler(this::logoutUser);
        eventBusListener();
    }

    public void eventBusListener(){

        EventBus eb = vertx.eventBus();

        eb.consumer("chat.to.server",message->{

            JsonObject data = (JsonObject) message.body();
            String classifier = data.getString("classifier");
            if(classifier.equals("loginUri")) {
                vertx.executeBlocking(future -> {
                    future.complete(OAuth.getOAuthParam());
                }, res -> {
                    eb.publish("chat.to.client", res.result());
                });
            }else if(classifier.equals("fetchMessage")){

            }else if(classifier.equals("sendMessage")){

            }else if(classifier.equals("sendFriendRequest")){

            }else if(classifier.equals("acceptRequest")){

            }else if(classifier.equals("rejectRequest")){

            }
        });

    }
    public void resultCallback(RoutingContext routingContext){
        System.out.println("Callback Result");
        /*String verifier = routingContext.request().getParam("oauth_verifier");*/
        routingContext.response().setChunked(true);
        routingContext.response().sendFile("webroot/dashboard.html");

    }

    public void logoutUser(RoutingContext routingContext){

        System.out.println("Logging Out the User");
    }
}
