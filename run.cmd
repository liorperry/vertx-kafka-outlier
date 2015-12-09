REM run maven build
mvn clean install

REM change to target folder
cd  target

set jar=vertx-kafka-outlier-3.1.0-fat.jar
echo %location%/%jar%

echo Redis starting ...
start "Redis" java -cp %location%/%jar% io.vertx.example.util.kafka.launcher.RedisStarter

SLEEP 5

echo Kafka & ZK starting ...
start "Kafka" java -cp  %jar% io.vertx.example.util.kafka.launcher.KafkaServerVertical

SLEEP 5

echo Outlier Web Server starting ...
start "WebServer" java -cp %jar% io.vertx.example.util.kafka.launcher.OutlierWebServer

SLEEP 5

echo Kafka Producer starting ...
start "topic-producer" java -cp %jar% io.vertx.example.util.kafka.launcher.SimpleKafkaProducer norbert frantz ginzburg

SLEEP 10

echo Kafka Consumer starting ...
start "topic-consumer" java -cp %jar% io.vertx.example.util.kafka.launcher.SimpleKafkaConsumer


