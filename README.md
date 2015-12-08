# vertx-kafka-outlier
An outlier detection system.

### Publisher
There are data publishers that post sensor data to a message broker - kafka.

There is a data consumer that uses the data and stores to a database a processed form of the data - redis .

### Consumer

The consumer to the message broker that listens to messages on the Kafka system. Messages are JSON strings in the format:

> {
>    "publisher": "publisher-id",
>    "time": "2015-11-03 15:03:30.352",
>    "readings": [ 1, 13, 192, 7, 8, 99, 1014, 4]
> }

The consumer should store the median of the readings with time and publisher id to a database of your choosing - redis

### Web Server

The web server should read display outliers for a given publisher.
It should pull data from the database per publisher.
And display reading and mark outliers for the last N readings.

### REST api:
- Listening for http://{hostname}:8081/kafka/brokers - get kafka brokers

- Listening for http://{hostname}:8081/kafka/all     - get kafka topics, partitions, leaders

- Listening for http://{hostname}:8081/publisher     - get publishers

- Listening for http://{hostname}:8081/publisher/:{publisherId}  - get publisher samples count

- Listening for http://{hostname}:8081/publisher/outlier/:{publisherId}?windowSize=10;outlierFactor=2
                                                        - get publisher outlier based on windowSize sample size + outlier factor

### Outlier calculation:
SimpleDistanceOutlierDetector - outlier calculator for calculating a single reading distance from the given window of sample data set



>outlierFactor = given double factor (default = 2)
>
>windowSize = n
>
>sampleData[i].median = single sample readings median
>
>sampleData[i].distance = abs(sampleData[i].median - mean(sampleData[1..n]))
>
>isOutlier = sampleData[i].distance >  outlierFactor*deviation(sampleData[1..n])

###  The system is build from the next verticals:

 * RedisStarter = an embedded redis vertical for in-mem no SQL storage
 * KafkaServerVertical = an embedded ZK + Kafka vertical [ starts up EmbeddedKafkaServer ]
 * SimpleKafkaProducer = topic specific kafka producer vertical
 * SimpleKafkaConsumer = topic specific kafka consumer vertical
 * OutlierWebServer = simple http rest enabled server accessing the redis to fetch outlier data

###  Main components:
 * Consumer - kafka consumer
 * Prodcuer - kafka producer
 * OutlierDetector - outlier interface
 * SamplePersister - sample data DAO interface

###  Running tests - todo run this in single test uberJar

* Binding ports
>  @todo add vert.x configuration dynamic port support<br>

>    zookeeper:2181<br>
>    kafka:9090<br>
>    web:8081<br>
>    redis:6379<br>

 * start RedisStarter        (start embedded redis )
 * start KafkaServerVertical (starts embedded ZK + Kafka)
 * start SimpleKafkaProducer (start zk topic producer)
 * start SimpleKafkaConsumer (start zk topic consumer)
 * start OutlierWebServer    (start rest web server - connected to redis)
