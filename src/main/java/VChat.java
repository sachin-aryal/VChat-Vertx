import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

/**
 * Author: SACHIN
 * Date: 5/6/2016.
 */
public class VChat extends AbstractVerticle{

    private final String consumerKey = "8n4Fm4FVUX289HbhwIavKV59V";
    private final String consumerSecret = "vVkl6RfKPEgsff3Kz8SngDOaQ9gDfmnyk93EPwwmpJJgffhrOw";

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new VChat());
    }

    public void start(){
        Router router = Router.router(vertx);
        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
        router.route("/").handler(rtx->{
            HttpServerResponse response = rtx.response();
            response.setChunked(true);
            response.sendFile("webroot/index.html");
        });

        router.route("/Js/*").handler(StaticHandler.create("webroot/Js"));
        router.route("/Css/*").handler(StaticHandler.create("webroot/Css"));


        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        BridgeOptions options = new BridgeOptions();
        options.addInboundPermitted(new PermittedOptions().setAddress("client-to-server"));
        options.addOutboundPermitted(new PermittedOptions().setAddress("server-to-client"));
        sockJSHandler.bridge(options);


        router.route("/loginUrl/*").handler(sockJSHandler);

        sockJSHandler.bridge(options);

        EventBus eb = vertx.eventBus();

        eb.consumer("client-to-server").handler(sockJSHand->{
            System.out.println("Received Message "+sockJSHand.body());
            vertx.executeBlocking(future -> {
                future.complete(OAuth.getAuthorizationUrl());
            }, res -> {
                
            });
        });

    }
}
