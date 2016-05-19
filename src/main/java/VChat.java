import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerResponse;
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
        options.addInboundPermitted(new PermittedOptions().setAddress("chat.to.server.userInfo"));
        options.addOutboundPermitted(new PermittedOptions().setAddress("chat.to.client"));
        options.addOutboundPermitted(new PermittedOptions().setAddress("chat.to.client.userInfo"));
        sockJSHandler.bridge(options);


        router.route("/loginUrl/*").handler(sockJSHandler);
        router.route("/userInfo/*").handler(sockJSHandler);
        router.route("/result/*").handler(this::resultCallback);
        loginListner();
        userInfo();
    }

    public void loginListner(){

        EventBus eb = vertx.eventBus();

        eb.consumer("chat.to.server",message->{

            vertx.executeBlocking(future -> {
                future.complete(OAuth.getAuthorizationUrl());
            }, res -> {
                eb.publish("chat.to.client",res.result());
            });
        });

    }
    public void userInfo(){
        EventBus eb = vertx.eventBus();
        eb.consumer("chat.to.server.userInfo",data->{

        });
    }
    public void resultCallback(RoutingContext routingContext){
        System.out.println("Calling");
        String verifier = routingContext.request().getParam("oauth_verifier");


    }
}