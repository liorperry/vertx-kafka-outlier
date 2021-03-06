package io.vertx.example.util.kafka.launcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.example.util.VertxInitUtils;
import io.vertx.example.util.kafka.*;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import kafka.javaapi.TopicMetadataRequest;
import kafka.javaapi.consumer.SimpleConsumer;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.*;

public class OutlierWebServer extends AbstractVerticle {
    private Map<String,OutlierDetector> detectors;
    private SamplePersister persister;
    private int httpPort;
    private ZooKeeper zk;
    private SimpleConsumer consumer;

    public static void main(String[] args) throws IOException {
        Vertx vertx = Vertx.vertx();
        DeploymentOptions options = VertxInitUtils.initDeploymentOptions();
        SamplePersister persister = new RedisSamplePersister(new JedisPool(),new BasicSampleExtractor());
        List<OutlierDetector> detectors = new ArrayList<>();
        SimpleDistanceOutlierDetector detector1 = new SimpleDistanceOutlierDetector(persister);
        ComplexDistanceOutlierDetector detector2 = new ComplexDistanceOutlierDetector(persister);
        detectors.add(detector1);
        detectors.add(detector2);
        int webPort = 8081;
        int kafkaPort = 9090;
        int zkPort = 2181;

        if (args.length == 3) {
            try {
                kafkaPort = Integer.valueOf(args[0]);
                zkPort = Integer.valueOf(args[1]);
                webPort = Integer.valueOf(args[2]);
            } catch (NumberFormatException e) {
                System.out.println(e.getMessage());
            }
        }
        vertx.deployVerticle(new OutlierWebServer(detectors,persister, webPort,zkPort,kafkaPort), options);
    }

    public OutlierWebServer(List<OutlierDetector> detectors,SamplePersister persister, int httpPort,int zkPort,int kafkaPort) throws IOException {
        this.detectors = toMap(detectors);
        this.persister = persister;
        this.httpPort = httpPort;
        this.zk = new ZooKeeper("localhost:"+zkPort, 10000, null);
        consumer = new SimpleConsumer("localhost",kafkaPort,100000,64 * 1024, "test");
    }

    private Map<String, OutlierDetector> toMap(List<OutlierDetector> detectors) {
        Map<String, OutlierDetector> map = new HashMap<>();
        for (OutlierDetector detector : detectors) {
            map.put(detector.getName(),detector);
        }
        return map;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        if(detectors.isEmpty()) {
            throw new RuntimeException("Outlier Detectors List not initiated");
        }

        Router router = Router.router(vertx);
        //http server
        HttpServer httpServer = vertx.createHttpServer();
        router.route().handler(BodyHandler.create());
        router.get("/publisher").handler(this::publishersList);
        router.get("/publisher/:publisherId").handler(this::publisherInfo);
        router.get("/publisher/outlier/:publisherId").handler(this::outlier);
        router.get("/publisher/outlierCompare/:publisherId").handler(this::outlierCompare);
        router.get("/kafka/all").handler(this::kafkaInfo);
        router.get("/kafka/brokers").handler(this::brokersList);

        System.out.println("Listening for http://{hostname}:" + httpPort + "/kafka/brokers");
        System.out.println("Listening for http://{hostname}:" + httpPort + "/kafka/all");
        System.out.println("Listening for http://{hostname}:" + httpPort + "/publisher");
        System.out.println("Listening for http://{hostname}:" + httpPort + "/publisher/:{publisherId}");
        System.out.println("Listening for http://{hostname}:" + httpPort + "/publisher/outlier/:{publisherId}?windowSize=10;outlierFactor=2;outlierName={simple|complex}");
        System.out.println("Listening for http://{hostname}:" + httpPort + "/publisher/outlierCompare/:{publisherId}?windowSize=10;outlierFactor=2");

        httpServer.requestHandler(router::accept).listen(httpPort, result -> {
            if (result.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(result.cause());
            }
        });
    }

    private void kafkaInfo(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        List<String> topics = new ArrayList<>();
        TopicMetadataRequest req = new TopicMetadataRequest(topics);
        kafka.javaapi.TopicMetadataResponse resp = consumer.send(req);
        List<kafka.javaapi.TopicMetadata> metadatas =  resp.topicsMetadata();
        StringBuilder builder = new StringBuilder();
        for (kafka.javaapi.TopicMetadata item : metadatas) {
            System.out.println("Topic: " + item.topic());
            builder.append("Topic: ").append(item.topic());
            for (kafka.javaapi.PartitionMetadata part: item.partitionsMetadata() ) {
                String replicas = ""; String isr = "";
                for (kafka.cluster.Broker replica: part.replicas() ) {
                    replicas += " " + replica.host();
                }
                for (kafka.cluster.Broker replica: part.isr() ) {
                    isr += " " + replica.host();
                }
                System.out.println( "    Partition: " +   part.partitionId()  + ": Leader: " + part.leader().host() + " Replicas:[" + replicas + "] ISR:[" + isr + "]");
                builder.append("    Partition: ").append(part.partitionId()).append(": Leader: ").append(part.leader().host()).append(" Replicas:[").append(replicas).append("] ISR:[").append(isr).append("]");
            }
        }
        response.putHeader("content-type", "application/json").end(builder.toString());
    }

    private void brokersList(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        try {
            List<String> ids = zk.getChildren("/brokers/ids", false);
            List<Map> brokerList = new ArrayList<>();
            ObjectMapper objectMapper = new ObjectMapper();

            for (String id : ids) {
                Map map = objectMapper.readValue(zk.getData("/brokers/ids/" + id, false, null), Map.class);
                brokerList.add(map);
            }
            JsonArray array = new JsonArray(brokerList);
            response.putHeader("content-type", "application/json").end(array.encodePrettily());
        } catch (KeeperException e) {
            e.printStackTrace();
            sendError(500,response);
        } catch (InterruptedException e) {
            e.printStackTrace();
            sendError(500, response);
        } catch (IOException e) {
            e.printStackTrace();
            sendError(500, response);
        }
    }

    private void publisherInfo(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        String publisherId = routingContext.request().getParam("publisherId");
        if (publisherId == null) {
            sendError(400, response);
        }
        Set<String> publishers = persister.getPublishers();
        if(!publishers.contains(publisherId)){
            sendError(400, response);
        }
        long size = persister.fetchLength(publisherId);

        Map<String,Object> data = new HashMap<>();
        data.put("publisher",publisherId);
        data.put("sampleSize",Long.toString(size));
        response.putHeader("content-type", "application/json").end(new JsonObject(data).encodePrettily());
    }


    private void publishersList(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        Set<String> publishers = persister.getPublishers();
        JsonArray array = new JsonArray(new ArrayList<>(publishers));
        response.putHeader("content-type", "application/json").end(array.encodePrettily());
    }

    private void outlier(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        String publisherId = routingContext.request().getParam("publisherId");
        String outlierName = routingContext.request().getParam("outlierName");
        if (publisherId == null) {
            sendError(400, response);
        }
        Optional<String> windowSize = Optional.ofNullable(routingContext.request().getParam("windowSize"));
        Optional<String> outlierFactor = Optional.ofNullable(routingContext.request().getParam("outlierFactor"));

        if(!detectors.containsKey(outlierName) || outlierName==null){
            //first detector on list as default detector
            outlierName = detectors.values().iterator().next().getName();
//            sendError(400, response);
        }

        OutlierDetector detector = detectors.get(outlierName);
        List<SampleData> outliers = detector.getOutlier(publisherId, Integer.valueOf(windowSize.orElse("10")), Optional.of(Double.valueOf(outlierFactor.orElse("2"))));
        JsonArray array = new JsonArray(outliers);
        response.putHeader("content-type", "application/json").end(array.encodePrettily());
    }

    private void outlierCompare(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        String publisherId = routingContext.request().getParam("publisherId");
        if (publisherId == null) {
            sendError(400, response);
        }
        Optional<String> windowSize = Optional.ofNullable(routingContext.request().getParam("windowSize"));

        JsonArray array = new JsonArray();
        for (OutlierDetector detector : detectors.values()) {
            List<SampleData> outlier = detector.getOutlier(publisherId, Integer.valueOf(windowSize.orElse("10")), Optional.empty());
            array.add(toJson(detector.getName(),outlier.size()));
        }
        response.putHeader("content-type", "application/json").end(array.encodePrettily());
    }


    private void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end();
    }


    public static JsonObject toJson(String name,int outliers) {
        Map<String,Object> map = new HashMap<>();
        map.put("DetectorName",name);
        map.put("Irregulars#",outliers);
        return new JsonObject(map);
    }

}