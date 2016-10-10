package web;

/**
 * Vertx Http server class
 *
 * @Author Legomin Vitaliy
 */
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

public class VertxHttpServerVerticle extends AbstractVerticle {

    private HttpServer httpServer = null;

    @Override
    public void start() throws Exception {
        httpServer = vertx.createHttpServer();

        HttpHandler handler = new HttpHandler();
        Router router = Router.router(vertx);

        router.route().consumes("application/json");
        router.route().produces("application/json");

        router.route("/latest/").handler(SockJSHandler.create(vertx));

        router.route().handler(BodyHandler.create());

        router.get("/entries").handler(handler::handleGetEntries);
        router.get("/entries/:entryid").handler(handler::handleGetEntry);
        router.post("/entries").handler(handler::handlePostEntry);
        router.put("/entries/:entryid").handler(handler::handlePutEntry);
        router.delete("/entries/:entryid").handler(handler::handleDeleteEntry);

        httpServer.requestHandler(router::accept);

        httpServer.listen(8080);

    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle("web.VertxHttpServerVerticle");
    }

}