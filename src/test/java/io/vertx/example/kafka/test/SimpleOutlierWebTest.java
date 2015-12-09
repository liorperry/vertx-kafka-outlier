package io.vertx.example.kafka.test;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.example.util.kafka.launcher.KafkaTestUtils;
import io.vertx.example.util.kafka.launcher.OutlierWebServer;
import io.vertx.example.util.VertxInitUtils;
import io.vertx.example.util.kafka.InMemSamplePersister;
import io.vertx.example.util.kafka.SimpleDistanceOutlierDetector;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(VertxUnitRunner.class)
public class SimpleOutlierWebTest {

    public static final String LOCALHOST = "localhost";
    static DeploymentOptions options = VertxInitUtils.initDeploymentOptions();
    static Vertx vertx;

    @BeforeClass
    public static void setUp(TestContext context) throws IOException {
        vertx = Vertx.vertx(VertxInitUtils.initOptions());
        InMemSamplePersister persister = new InMemSamplePersister();
        prePopulateData(persister);
        vertx.deployVerticle(new OutlierWebServer(new SimpleDistanceOutlierDetector(persister), persister,8081,2181,9090),
                options,context.asyncAssertSuccess());
    }

    public static void prePopulateData(InMemSamplePersister persister) {
        persister.persist(KafkaTestUtils.create("norbert", 100, 15));
        persister.persist(KafkaTestUtils.create("ginzburg", 300, 10));
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }


    @Test
    public void testPublisher(TestContext context) {
        Async async = context.async();
        HttpClient client = vertx.createHttpClient();
        client.getNow(8081, LOCALHOST, "/publisher", resp -> {
            resp.bodyHandler(body -> {
                System.out.println("/publisher" + ":" + resp.statusCode() + " [" + body.toString() + "]");
                context.assertTrue(body.toString().contains("norbert"));
                context.assertTrue(body.toString().contains("ginzburg"));
//                client.close();
                async.complete();
            });
        });
    }

    @Test
    public void testPublisherOutlier(TestContext context) {
        Async async = context.async();
        HttpClient client = vertx.createHttpClient();
        client.getNow(8081, LOCALHOST, "/publisher/outlier/ginzburg?windowSize=100;outlierFactor=0.2", resp -> {
            resp.bodyHandler(body -> {
                System.out.println("/publisher/outlier/ginzburg?windowSize=100;outlierFactor=0.2" + ":" + resp.statusCode() + " [" + body.toString() + "]");
                JsonArray result = new JsonArray(body.toString());
                context.assertTrue(result.size() > 0);
//                client.close();
                async.complete();
            });
        });
    }

    @Test
    public void testPublisherFetchSize(TestContext context) {
        Async async = context.async();
        HttpClient client = vertx.createHttpClient();
        client.getNow(8081, LOCALHOST, "/publisher/norbert", resp -> {
            resp.bodyHandler(body -> {
                System.out.println("/publisher/norbert" + ":" + resp.statusCode() + " [" + body.toString() + "]");
                context.assertTrue(body.toString().contains("norbert"));
                context.assertEquals(Integer.valueOf(new JsonObject(body.toString()).getString("sampleSize")),100);
//                client.close();
                async.complete();
            });
        });
    }



}
