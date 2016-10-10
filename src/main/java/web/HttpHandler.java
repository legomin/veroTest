package web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.EntryDto;
import io.vertx.core.json.DecodeException;
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
     * get paging list of entries
     *
     * @param context
     */
    public void handleGetEntries(RoutingContext context) {

        String pagingState;
        try {
            JsonObject jsonObject = context.getBodyAsJson();
            pagingState = jsonObject.getString("pagingState");
        } catch (DecodeException e) {
            pagingState = null;
        }

        try {
            String res = mapper.writeValueAsString(repository.getEntries(pagingState));
            context.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(200)
                    .end(res);
        } catch (JsonProcessingException e) {
            context.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(404)
                    .end("");
            e.printStackTrace();

        }
    }

    /**
     * get entry by entry's id
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
     * post new entry
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

        if (!entry.getPrivateEntry()) {
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
        }
        context.response()
                .setStatusCode(200)
                .end(uuid.toString());
    }

    /**
     * delete entry by entryid
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
     * put entry
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

}