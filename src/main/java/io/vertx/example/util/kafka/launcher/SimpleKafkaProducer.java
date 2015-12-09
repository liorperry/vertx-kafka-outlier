package io.vertx.example.util.kafka.launcher;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.example.util.kafka.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.vertx.example.util.kafka.launcher.KafkaTestUtils.createJson;
import static io.vertx.example.util.VertxInitUtils.initDeploymentOptions;


public class SimpleKafkaProducer extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(SimpleKafkaConsumer.class);

    private ExecutorService executor = Executors.newFixedThreadPool(10);

    private int sampleFrequence = 3000;
    private Producer producer;
    private Properties producerProperties;
    private String topic;
    private final int kafkaPort;
    private String[] publishers;

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DeploymentOptions options = initDeploymentOptions();
        vertx.deployVerticle(new SimpleKafkaProducer("test", 9090, 3000, args), options);
    }

    public SimpleKafkaProducer(String topic, int kafkaPort, int sampleFrequence, String... publishers) {
        this.publishers = publishers;
        try {
            this.sampleFrequence = sampleFrequence;
            this.kafkaPort = kafkaPort;
            this.topic = topic;
            producerProperties = new Properties();
            producerProperties.load(getClass().getClassLoader().getResourceAsStream("producer.properties"));
            producerProperties.setProperty("bootstrap.servers", String.format("localhost:%d", kafkaPort));
            producerProperties.put("group.id", "test-group");
            producer = new Producer(producerProperties);
        } catch (IOException ioe) {
            logger.error("configuration error", ioe);
            throw new RuntimeException(ioe);
        }
    }

    public void start(Future<Void> fut) {
        Arrays.asList(publishers).forEach(s ->
                vertx.setPeriodic(sampleFrequence, event -> {
                    System.out.println("sending message to kafka:" + kafkaPort);
                    executor.submit(() -> {
                        producer.send(topic, "message", createJson(s, 1, 15).getJsonObject(0).encode());
                    });
                }));
        fut.complete();
    }


    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        producer.shutdown();
        stopFuture.complete();
    }
}
