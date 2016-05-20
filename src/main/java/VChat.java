import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;

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
        router = Router.router(vertx);

        router.route().handler(CookieHandler.create());
        SessionStore store = LocalSessionStore.create(vertx);
        SessionHandler sessionHandler = SessionHandler.create(store);


        router.route().handler(sessionHandler);


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
        String verifier = routingContext.request().getParam("oauth_verifier");
//        System.out.println(verifier);

        Session session = routingContext.session();

        System.out.println("test");
        //put some data from the session
        session.put("username", "admin");
        session.put("pass","password");

        //retrive some data from session
        String usr = session.get("username");
        String pass = session.get("pass");

        System.out.println("Username is "+usr +" password is "+ pass);

        HttpServerResponse response = routingContext.response();
        response.setChunked(true);
        JsonObject object = new JsonObject();
        object.put("username",usr);
        object.put("password",pass);
        response.write(object.toString());



    }
}
