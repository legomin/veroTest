package web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.EntryDto;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import model.Entry;
import repository.EntryRepository;
import repository.cassandraImpl.CassandraEntryRepositoryImpl;

import java.util.UUID;

/**
 * Http server's request handler class
 *
 * @Author Legomin Vitaliy
 */
public class HttpHandler  {

    private EntryRepository repository = new CassandraEntryRepositoryImpl();
    private ObjectMapper mapper = new ObjectMapper();

    /**
     *
     * @param context
     */
    public void handleGetEntries(RoutingContext context) {
        try {
            String res = mapper.writeValueAsString(repository.getEntries(null));
            context.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(200)
                    .end(res);
        } catch (JsonProcessingException e) {
            context.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(404)
                    .end();
            e.printStackTrace();
        }
    }

    /**
     *
     * @param context
     */
    public void handleGetEntry(RoutingContext context) {

        String secretKey = context.request().getParam("entryid");
        try {
            Entry entry = repository.getEntry(UUID.fromString(secretKey));
            if (entry == null) {
                context.response()
                        .putHeader("content-type", "application/json")
                        .setStatusCode(404)
                        .end();
            }
            else {
                context.response()
                        .putHeader("content-type", "application/json")
                        .setStatusCode(200)
                        .end(mapper.writeValueAsString(entry));
            }
        } catch (JsonProcessingException e) {
            context.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(404)
                    .end();
            e.printStackTrace();
        }
    }

    /**
     *
     * @param context
     */
    public void handlePostEntry(RoutingContext context) {

        JsonObject jsonObject = context.getBodyAsJson();
        String body = jsonObject.getString("body");
        if (body == null) {
            context.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(404)
                    .end();
        }
        Entry entry = new Entry(body,
                jsonObject.getString("title"),
                jsonObject.getString("expires"),
                jsonObject.getString("private"));

        UUID uuid = repository.postEntry(entry);

        try {
            context.vertx().eventBus().publish("/latest/", mapper.writeValueAsString(new EntryDto(
                    entry.getBody(),
                    entry.getTitle(),
                    entry.getExpires(),
                    entry.getCreationDate()
            )));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        context.response()
                .setStatusCode(200)
                .end(uuid.toString());
    }

    /**
     *
     * @param context
     */
    public void handleDeleteEntry(RoutingContext context) {

        String secretKey = context.request().getParam("entryid");
        int res = repository.deleteEntry(UUID.fromString(secretKey));
        if (res == -1) {
            context.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(404)
                    .end();
        }
        else {
            context.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(200)
                    .end(res+"");
        }
    }

    /**
     *
     * @param context
     */
    public void handlePutEntry(RoutingContext context) {

        JsonObject jsonObject = context.getBodyAsJson();
        String body = jsonObject.getString("body");
        if (body == null) {
            context.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(404)
                    .end();
        }
        String secretKey = context.request().getParam("entryid");

        Entry entry = new Entry(body,
                jsonObject.getString("title"),
                jsonObject.getString("expires"),
                jsonObject.getString("private"));

        entry.setId(UUID.fromString(secretKey));
        int res = repository.putEntry(entry);
        if (res == -1) {
            context.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(404)
                    .end();
        }
        else {
            context.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(200)
                    .end(res+"");

        }
 }










/*    public void handle(RoutingContext routingContext) {
        System.out.println("incoming request!");
        HttpServerRequest request = routingContext.request();

        String uri = request.uri();
        //if (uri.contains("/entries/")) {
            MultiMap params = request.params();

            if (request.method() == HttpMethod.GET) {
                if (params.contains("secret")) {
                    try {
                        Entry entry = repository.getEntry(UUID.fromString(params.get("secret")));
                        if (entry == null) {
                            request.response().setStatusCode(404).end("404 not found by secret");
                        }
                        else {
                            request.response().end(mapper.writeValueAsString(entry));
                        }
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    try {
                        String res = mapper.writeValueAsString(repository.getEntries(null));
                        request.response().end(res);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
            }
            else if (request.method() == HttpMethod.POST) {
                if (params.get("body") == null) {
                    request.response().setStatusCode(404).end("404 Bad request");
                }
                //TODO exired date, isPrivate
                Entry entry = new Entry(params.get("body"),
                        params.get("title"),
                        null,
                        null);
                UUID uuid = repository.postEntry(entry);

                request.response().end(uuid.toString());
                try {
                    routingContext.vertx().eventBus().publish("/latest/", mapper.writeValueAsString(new EntryDto(
                            entry.getBody(),
                            entry.getTitle(),
                            entry.getExpires(),
                            entry.getCreationDate()
                    )));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                routingContext.response()
                        .setStatusCode(200)
                        .end();
            }
            else if (request.method() == HttpMethod.PUT) {
                if (params.get("secret") == null) {
                    request.response().setStatusCode(404).end("404 Bad request");
                }
                //TODO exired date, isPrivate
                Entry entry = new Entry(params.get("body"),
                        params.get("title"),
                        null,
                        null);
                entry.setId(UUID.fromString(params.get("secret")));
                int res = repository.putEntry(entry);
                if (res == -1) {
                    request.response().setStatusCode(404).end("404 not found by secret");
                }
                else {
                    request.response().end("success");
                }
            }
            else if (request.method() == HttpMethod.DELETE) {
                if (params.get("secret") == null) {
                    request.response().setStatusCode(404).end("404 Bad request");
                }
                int res = repository.deleteEntry(UUID.fromString(params.get("secret")));
                if (res == -1) {
                    request.response().setStatusCode(404).end("404 not found by secret");
                }
                else {
                    request.response().end("success");
                }
            }
        //}
        //else {
        //    request.response().setStatusCode(404).end("404 Bad request");
        //}

    }
*/

}
