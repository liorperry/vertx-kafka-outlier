package io.vertx.example.kafka.integration;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.example.util.kafka.BasicSampleExtractor;
import io.vertx.example.util.kafka.InMemSamplePersister;
import io.vertx.example.util.kafka.launcher.KafkaServerVertical;
import io.vertx.example.util.kafka.launcher.SimpleKafkaConsumer;
import io.vertx.example.util.kafka.launcher.SimpleKafkaProducer;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.IOException;

import static io.vertx.example.util.VertxInitUtils.*;


@RunWith(VertxUnitRunner.class)
@Ignore
public class SimpleKafkaVerticalTest {

    public static final String LOCALHOST = "localhost";
    static DeploymentOptions options = initDeploymentOptions();
    static Vertx vertx;
    static InMemSamplePersister persister = new InMemSamplePersister();

    @BeforeClass
    public static void setUp(TestContext context) throws IOException {
        int zkPort = getNextAvailablePort();
        int kafkaPort = getNextAvailablePort();
        options.getConfig().put(KafkaServerVertical.ZK_PORT, zkPort);
        options.getConfig().put(KafkaServerVertical.KAFKA_PORT, kafkaPort);

        vertx = Vertx.vertx(initOptions());

        vertx.deployVerticle(new KafkaServerVertical(zkPort,kafkaPort),
                            options,
                            context.asyncAssertSuccess());

        vertx.deployVerticle(new SimpleKafkaConsumer(
                        persister,
                        new BasicSampleExtractor(),
                        "test", zkPort,1000),
                options,
                context.asyncAssertSuccess());

        vertx.deployVerticle(new SimpleKafkaProducer("topic",kafkaPort,1000),
                options,
                context.asyncAssertSuccess());
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }


    @Test
    public void testKafkaMessageProduction(TestContext context) throws InterruptedException {
        Thread.sleep(10000);
        Assert.assertTrue(persister.fetchAll("NORBERT").size()>0 );

    }


}
