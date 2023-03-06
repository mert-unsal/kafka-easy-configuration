# KAFKA CONFIG

#### This project aims to remove all boilerplate codes which we use for configurations kafka consumers and producers. Kafka Config library provides dynamic configuration and necessary kafka util.
#### Furthermore, this library offers non-blocking retry mechanism ( FixedRetry or ExponentialRetry ) and failover recovery mechanism

## INSTALLATION

Copy and paste this inside your pom.xml dependencies block.

```
<dependency>
 <groupId>com.munsal</groupId>
 <artifactId>kafka-easy-configuration</artifactId>
 <version>LATEST</version>
</dependency>
```

## Usage


### Adding `@EnableKafkaConfiguration` annotation on SpringBoot Application

```
@EnableKafkaConfig
@SpringBootApplication
public class InternationalCommissionInvoiceApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(InternationalCommissionInvoiceApiApplication.class, args);
    }
}
```
### Adding `@DependsOnKafkaFactories` on your consumer class
This step is crucial thus we need to wait till our all kafka configuration beans created
```
@DependsOnKafkaFactories
public class MyServiceConsumer {
    @KafkaListener(
            topics = "${kafka-config.consumers['my-service-consumer'].topic}",
            groupId = "${kafka-config.consumers['my-service-consumer'].props[group.id]}",
            containerFactory = "#{kafkaListenerContainerFactoryMap['my-service-consumer']}"
    )
    public void consume(@Payload MyServiceEvent message,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) Integer partition,
                        @Header(KafkaHeaders.OFFSET) Long offset,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
    ...
    }
    
    
     @KafkaListener(
            topics = "${kafka-config.consumers['my-service-consumer'].retry-topic}",
            groupId = "${kafka-config.consumers['my-service-consumer'].props[retry-group.id]}",
            containerFactory = "#{kafkaListenerContainerFactoryMap['my-service-consumer']}"
    )
    public void consumeRetry(@Payload MyServiceEvent message,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) Integer partition,
                        @Header(KafkaHeaders.OFFSET) Long offset,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
    ...                    
    }
}
```

#### Here `@DependsOnKafkaFactories` make your service waiting till your kafka listener bean is ready.It supply you a bean which contains your all kafkaListenerContainerFactory with respect to your yml configuration.
```yml
consumers:
    "[my-service-consumer]":
```
#### this is example usage, you may get the containerFactory with the same key as you provide on your kafka yml, for instance, on this case you will get with ``example-consumer`` keyword

## Creating a Failover Recovery Process which will implements FailoverHandler
```
public interface FailoverHandler {
    void handle(Consumer consumer, ConsumerRecord consumerRecord, Exception exception);
}
```

```
@Service(value = "MyServiceFailoverHandler")
public class MyServiceFailoverHandler implements FailoverHandler {

    @Override
    public void handle(Consumer consumer, ConsumerRecord consumerRecord, Exception exception) {

    }
}
```

### Need to be specified on yml. custom failover recovery bean

```yml
consumers:
    "[example-consumer]":
      failover-handler-bean-name: MyServiceFailoverHandler
      data-class: com.munsal.example.service.api.domain.event.MyServiceEvent
      producer-name: [producer-name]

```
### `producer-name` is crucial for producing error, this is the name of producer you supplied on config file.
### `data-class` is used for casting Kafka Record to Consumer Event Dto

While generating beans which is for kafka configuration, we need to take configs from yml,  
here `kafka-configuration` prefix is important. Here, you can supply consumer or producer as many as you need.

This yml basically contains 2 parts, `producers` and `consumers`

``` yaml
kafka-configuration:
  producers:
    "[producer-name]":
       props:
        "[bootstrap.servers]": ${stretch.kafka.bootstrap.servers}
        ...
  consumers:
    "[consumer-name]":
        "[example-consumer]":
            topic: "topic"
            error-topic: "errorTopic"
            ...
            props:
                ...
```
Under each part, we are going to supply consumer name and its properties. Here, `props` part is using for KafkaConsumerFactory. While creating DefaultKafkaConsumerFactory bean, we are using these
kafka properties. The other configurations under the consumer part, is using for KafkaListenerContainerFactory properties.


## YML Configuration

You are going to provide a yml for your consumers and producers config. An example yml should be looking like;

``` yaml
kafka-config:
  producers:
    "[default]":
      props:
        "[bootstrap.servers]": ${kafka.bootstrap.servers}
        "[batch.size]": 16384
        "[linger.ms]": 5
        "[buffer.memory]": 33554432
        "[key.serializer]": org.apache.kafka.common.serialization.StringSerializer
        "[value.serializer]": org.springframework.kafka.support.serializer.JsonSerializer
        "[acks]": "1"
        "[interceptor.classes]": com.munsal.example.service.api.kafka.interceptor.KafkaProducerInterceptor
        "[default.api.timeout.ms]": 20000
        "[request.timeout.ms]": 20000
  consumers:
    "[my-example-consumer]":
      topic: myexample.topic
      retry-topic: myexample.retry
      error-topic: myexample.error
      failover-handler-bean-name: MyServiceFailoverHandler
      data-class: com.munsal.example.service.api.domain.event.MyServiceEvent
      concurrency: 1
      retry-count: 1
      sync-commit: true
      sync-commit-timeout-second: 5
      retry-type: no_retry
      non-blocking-retry:
        include-topics: migros.case.study.courier.topic
        max-attempts: 1
        is-exponential-retry: true
        multiplier: 2
        backoff-interval-millis: 1000
        retry-on-exception: com.migros.casestudy.exception.BaseException
      props:
        "[group.id]": munsal.example.myservice.group
        "[retry-group.id]": munsal.example.myservice.0.retry.group
        "[interceptor.classes]": com.munsal.example.service.api.kafka.interceptor.KafkaConsumerInterceptor
        "[bootstrap.servers]": ${kafka.bootstrap.servers}
        "[spring.json.trusted.packages]": com.munsal
        "[max.poll.records]": 500
        "[max.poll.interval.ms]": 300000
        "[session.timeout.ms]": 10000
        "[heartbeat.interval.ms]": 3000
        "[enable.auto.commit]": true
        "[auto.offset.reset]": earliest
        "[fetch.max.bytes]": 52428800
        "[fetch.max.wait.ms]": 500
        "[default.api.timeout.ms]": 20000
        "[request.timeout.ms]": 20000
  integration-topics:
    my-service-topic: com.munsal.example.topic
```
