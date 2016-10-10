package web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import model.Entry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import repository.EntryRepository;
import repository.cassandraImpl.CassandraEntryRepositoryImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * main Handler test class
 *
 * @Author Legomin Vitaliy
 */
@RunWith(VertxUnitRunner.class)
public class VertxHttpServerVerticleTest {

    private Vertx vertx;
    private HttpClient httpClient;
    private EntryRepository repository = new CassandraEntryRepositoryImpl();
    private UUID uuid1, uuid2;
    private ObjectMapper mapper = new ObjectMapper();

    /**
     * initialization
     *
     * @param context
     */
    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(VertxHttpServerVerticle.class.getName(),
                context.asyncAssertSuccess());
        httpClient = vertx.createHttpClient();

        Entry e1 = new Entry("hello", "title", "2016-10-20", "false");
        uuid1 = repository.postEntry(e1);
        Entry e2 = new Entry("hello1", "title1", "2016-10-20", "true");
        uuid2 = repository.postEntry(e2);
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    /**
     * get entries test
     *
     * @param context
     */
    @Test
    public void testGetEntries(TestContext context) {
        final Async async = context.async();

        httpClient.get(8080, "localhost", "/entries",
                response -> {
                    context.assertTrue(response.statusCode() == 200);
                    response.handler(body -> {
                        context.assertTrue(body.toString().contains("body"));
                        context.assertTrue(body.toString().contains("title"));
                        async.complete();
                    });
                }).end(" ");
    }

    /**
     * test get entry by id
     *
     * @param context
     */
    @Test
    public void testGetEntry(TestContext context) {
        final Async async = context.async();

        httpClient.get(8080, "localhost", "/entries/" + uuid1.toString(),
                response -> {
                    context.assertTrue(response.statusCode() == 200);
                    response.handler(body -> {
                        context.assertTrue(body.toString().contains("hello"));
                        context.assertTrue(body.toString().contains("title"));
                        async.complete();
                    });
                }).end("");

        httpClient.get(8080, "localhost", "/entries/" + UUID.randomUUID().toString(),
                response -> {
                    context.assertTrue(response.statusCode() == 404);
                    response.handler(body -> {
                        async.complete();
                    });
                }).end("");
    }

    /**
     * test post new entry
     *
     * @param context
     */
    @Test
    public void testPostEntry(TestContext context) {
        final Async async = context.async();

        Map<String, String> enc = new HashMap<>();
        enc.put("body", "hello5");
        enc.put("title", "title5");

        try {
            httpClient.post(8080, "localhost", "/entries",
                    response -> {
                        context.assertTrue(response.statusCode() == 200);
                        response.handler(body -> {
                            async.complete();
                        });
                    }).end(mapper.writeValueAsString(enc));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    /**
     * test put entry
     *
     * @param context
     */
    @Test
    public void testPutEntry(TestContext context) {
        final Async async = context.async();

        Map<String, String> enc = new HashMap<>();
        enc.put("body", "hello5");
        enc.put("title", "title5");

        try {
            httpClient.put(8080, "localhost", "/entries/" + uuid1.toString(),
                    response -> {
                        context.assertTrue(response.statusCode() == 200);
                        response.handler(body -> {
                            async.complete();
                        });
                    }).end(mapper.writeValueAsString(enc));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        try {
            httpClient.put(8080, "localhost", "/entries/" + UUID.randomUUID().toString(),
                    response -> {
                        context.assertTrue(response.statusCode() == 404);
                        response.handler(body -> {
                            async.complete();
                        });
                    }).end(mapper.writeValueAsString(enc));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    /**
     * test delete entry
     *
     * @param context
     */
    @Test
    public void testDeleteEntry(TestContext context) {
        final Async async = context.async();

        httpClient.delete(8080, "localhost", "/entries/" + uuid2.toString(),
                response -> {
                    context.assertTrue(response.statusCode() == 200);
                    response.handler(body -> {
                        async.complete();
                    });
                }).end("");

        httpClient.delete(8080, "localhost", "/entries/" + UUID.randomUUID().toString(),
                response -> {
                    context.assertTrue(response.statusCode() == 404);
                    response.handler(body -> {
                        async.complete();
                    });
                }).end("");
    }

    /**
     * test socket
     *
     * @param context
     */
    @Test
    public void testSocket(TestContext context) {
        final Async async = context.async();

        Map<String, String> enc = new HashMap<>();
        enc.put("body", "hello2");
        enc.put("title", "title2");

        EventBus eb = vertx.eventBus();
        eb.consumer("/latest/").handler(message ->
        {
            JsonObject jsonObject = new JsonObject(message.body().toString());
            context.assertEquals(jsonObject.getString("body"), "hello2");
            context.assertEquals(jsonObject.getString("title"), "title2");
            async.complete();
        });

        try {
            httpClient.post(8080, "localhost", "/entries",
                    response -> {
                    }).end(mapper.writeValueAsString(enc));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }


}