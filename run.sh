#!/usr/bin/env bash

# run maven build
mvn clean install

# change to target folder
cd target

echo 'Redis starting ...'
java -cp vertx-kafka-outlier-3.1.0-fat.jar io.vertx.example.util.kafka.launcher.RedisStarter &

sleep 5

echo 'Kafka & ZK starting ...'
java -cp vertx-kafka-outlier-3.1.0-fat.jar io.vertx.example.util.kafka.launcher.KafkaServerVertical &

sleep 5

echo 'Outlier Web Server starting ...'
java -cp vertx-kafka-outlier-3.1.0-fat.jar io.vertx.example.util.kafka.launcher.OutlierWebServer &

sleep 5

echo 'Kafka Producer starting ...'
java -cp vertx-kafka-outlier-3.1.0-fat.jar io.vertx.example.util.kafka.launcher.SimpleKafkaProducer norbert frantz ginzburg &

sleep 10

echo 'Kafka Consumer starting ...'
java -cp vertx-kafka-outlier-3.1.0-fat.jar io.vertx.example.util.kafka.launcher.SimpleKafkaConsumer &


