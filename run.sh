#!/usr/bin/env bash
#* Binding ports
#>  @todo add vert.x configuration dynamic port support<br>
#>    zookeeper:2181<br>
#>    kafka:9090<br>
#>    web:8081<br>
#>    redis:6379<br>

# run maven build
mvn clean install

zkPort=2180
kafkaPort=9092
webPort=8082
# change to target folder
cd target

echo 'Redis starting ... '
java -cp vertx-kafka-outlier-3.1.0-fat.jar io.vertx.example.util.kafka.launcher.RedisStarter &

sleep 5

echo 'Kafka & ZK starting ...args[zkPort kafkaPort] $zkPort $kafkaPort '
java -cp vertx-kafka-outlier-3.1.0-fat.jar io.vertx.example.util.kafka.launcher.KafkaServerVertical   &

sleep 5

echo 'Outlier Web Server starting ...args[webPort zkPort kafkaPort] $webPort $zkPort $kafkaPort '
java -cp vertx-kafka-outlier-3.1.0-fat.jar io.vertx.example.util.kafka.launcher.OutlierWebServer &

sleep 5

echo 'Kafka Producer starting ...args[kafkaPort list of publishers ...] $kafkaPort '
java -cp vertx-kafka-outlier-3.1.0-fat.jar io.vertx.example.util.kafka.launcher.SimpleKafkaProducer norbert frantz ginzburg &

sleep 10

echo 'Kafka Consumer starting ...args[zkPort] $zkPort '
java -cp vertx-kafka-outlier-3.1.0-fat.jar io.vertx.example.util.kafka.launcher.SimpleKafkaConsumer &


