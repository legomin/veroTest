package web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
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
 * Created by vitas on 02.10.16.
 */
@RunWith(VertxUnitRunner.class)
public class VertxHttpServerVerticleTest {

    private Vertx vertx;
    private HttpClient httpClient;
    private EntryRepository repository = new CassandraEntryRepositoryImpl();
    private UUID uuid1, uuid2;
    private ObjectMapper mapper = new ObjectMapper();


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

    @Test
    public void testGetEnytries(TestContext context) {
        final Async async = context.async();

        httpClient.get(8080, "localhost", "/entries",
                response -> {
                    context.assertTrue(response.statusCode() == 200);
                    response.handler(body -> {
                        context.assertTrue(body.toString().contains("body"));
                        context.assertTrue(body.toString().contains("title"));
                        async.complete();
                    });
                }).end("");
    }

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
                        //context.assertTrue(body.toString().contains("404"));
                        async.complete();
                    });
                }).end("");
    }

    @Test
    public void testPostEnytry(TestContext context) {
        final Async async = context.async();

        Map<String, String> enc = new HashMap<>();
        enc.put("body", "hello5");
        enc.put("title", "title5");

        try {
            vertx.createHttpClient().post(8080, "localhost", "/entries",
                    response -> {
                        context.assertTrue(response.statusCode() == 200);
                        response.handler(body -> {
                            //context.assertTrue(body.toString().contains("pagingState"));
                            //context.assertTrue(body.toString().contains("pagingList"));
                            async.complete();
                            System.out.println();
                        });
                    }).end(mapper.writeValueAsString(enc));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

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
                            //context.assertTrue(body.toString().contains("success"));
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
                            //context.assertTrue(body.toString().contains("404"));
                            async.complete();
                        });
                    }).end(mapper.writeValueAsString(enc));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDeleteEntry(TestContext context) {
        final Async async = context.async();

        httpClient.delete(8080, "localhost", "/entries/" + uuid2.toString(),
                response -> {
                    context.assertTrue(response.statusCode() == 200);
                    response.handler(body -> {
                        //context.assertTrue(body.toString().contains("success"));
                        async.complete();
                    });
                }).end("");

        httpClient.delete(8080, "localhost", "/entries/" + UUID.randomUUID().toString(),
                response -> {
                    context.assertTrue(response.statusCode() == 404);
                    response.handler(body -> {
                        //context.assertTrue(body.toString().contains("404"));
                        async.complete();
                    });
                }).end("");
    }

}