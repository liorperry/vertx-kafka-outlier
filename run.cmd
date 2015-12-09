REM * Binding ports
REM >  @todo add vert.x configuration dynamic port support<br>
REM >    zookeeper:2181<br>
REM >    kafka:9090<br>
REM >    web:8081<br>
REM >    redis:6379<br>

REM run maven build
mvn clean install

REM change to target folder
cd  target

set jar=vertx-kafka-outlier-3.1.0-fat.jar
echo %location%/%jar%

echo 'Redis starting ...'
start "Redis" java -cp %location%/%jar% io.vertx.example.util.kafka.launcher.RedisStarter

SLEEP 5

echo Kafka & ZK starting ... args[zkPort kafkaPort]
start "Kafka" java -cp  %jar% io.vertx.example.util.kafka.launcher.KafkaServerVertical

SLEEP 5

echo Outlier Web Server starting ... args[webPort zkPort kafkaPort]
start "WebServer" java -cp %jar% io.vertx.example.util.kafka.launcher.OutlierWebServer

SLEEP 5

echo Kafka Producer starting ... args[kafkaPort]
start "topic-producer" java -cp %jar% io.vertx.example.util.kafka.launcher.SimpleKafkaProducer norbert frantz ginzburg

SLEEP 10

echo Kafka Consumer starting ... args[zkPort]
start "topic-consumer" java -cp %jar% io.vertx.example.util.kafka.launcher.SimpleKafkaConsumer


