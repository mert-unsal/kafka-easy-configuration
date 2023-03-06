package com.munsal.kafkaconfiguration.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.munsal.kafkaconfiguration.SpringContext;
import com.munsal.kafkaconfiguration.kafka.FailoverHandler;
import com.munsal.kafkaconfiguration.model.Consumer;
import com.munsal.kafkaconfiguration.model.retry.RetryType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.mapping.DefaultJackson2JavaTypeMapper;
import org.springframework.kafka.support.mapping.Jackson2JavaTypeMapper;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Component;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.FixedBackOff;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumerUtil implements JsonUtil {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final SpringContext springContext;

    public <T> ConsumerFactory<String, T> createConsumerFactory(Consumer consumer, Class<T> classT) {
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        ErrorHandlingDeserializer<String> keyDeserializer = new ErrorHandlingDeserializer<>(new StringDeserializer());
        var jsonDeserializer = new JsonDeserializer<>(classT, OBJECT_MAPPER);
        jsonDeserializer.setTypeMapper(typeMapper);
        var valueDeserializer = new ErrorHandlingDeserializer<>(jsonDeserializer);
        return new DefaultKafkaConsumerFactory<>(consumer.getProps(), keyDeserializer, valueDeserializer);
    }

    //TODO: It may replaced with dead Letter
    public void handleFailover(KafkaTemplate<String, Object> kafkaTemplate, Consumer consumer, ConsumerRecord record, Exception exception) {
        try {
            Optional.ofNullable(consumer.getFailoverHandlerBeanName()).ifPresentOrElse(failoverHandlerBeanName ->
                            springContext.getBean(failoverHandlerBeanName, FailoverHandler.class).handle(consumer, record, exception),
                    () -> Optional.ofNullable(consumer.getErrorTopic()).ifPresent(
                            errorTopic -> Optional.ofNullable(kafkaTemplate).ifPresentOrElse(
                                    kafkaTemplate1 -> kafkaTemplate1.send(errorTopic, (Optional.ofNullable(record.key()).orElse(-1)).toString(), record.value()),
                                    () -> log.error("There is no kafka template to produce message to error topic")))
            );
        } catch (Exception e) {
            log.error("Consumer failover has an error, exception : {}", e.getMessage());
        }
    }


    public Class<?> getDataClass(Consumer consumer) {
        try {
            return Class.forName(consumer.getDataClass());
        } catch (ClassNotFoundException classNotFoundException) {
            log.error("Please supply `data-class` inside of your relevant kafka.yml");
            return null;
        }
    }

    public <T> ConcurrentKafkaListenerContainerFactory<String, ?> createListenerFactory(KafkaTemplate<String, Object> suitableKafkaTemplate, Consumer consumer, ConsumerFactory<String, T> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        Optional.ofNullable(consumer.getAckMode()).ifPresent(value -> factory.getContainerProperties().setAckMode(value));
        Optional.ofNullable(consumer.getAsyncAcks()).ifPresent(value -> factory.getContainerProperties().setAsyncAcks(value));
        Optional.ofNullable(consumer.getAckTime()).ifPresent(value -> factory.getContainerProperties().setAckTime(value));
        Optional.ofNullable(consumer.getAckCount()).ifPresent(value -> factory.getContainerProperties().setAckCount(value));
        Optional.ofNullable(consumer.getShutdownTimeout()).ifPresent(value -> factory.getContainerProperties().setShutdownTimeout(value));
        Optional.ofNullable(consumer.getIdleEventInterval()).ifPresent(value -> factory.getContainerProperties().setIdleEventInterval(value));
        Optional.ofNullable(consumer.getIdlePartitionEventInterval()).ifPresent(value -> factory.getContainerProperties().setIdlePartitionEventInterval(value));
        Optional.ofNullable(consumer.getIdleBeforeDataMultiplier()).ifPresent(value -> factory.getContainerProperties().setIdleBeforeDataMultiplier(value));
        Optional.ofNullable(consumer.getLogContainerConfig()).ifPresent(value -> factory.getContainerProperties().setLogContainerConfig(value));
        Optional.ofNullable(consumer.getMissingTopicsFatal()).ifPresent(value -> factory.getContainerProperties().setMissingTopicsFatal(value));
        Optional.ofNullable(consumer.getIdleBetweenPolls()).ifPresent(value -> factory.getContainerProperties().setIdleBetweenPolls(value));
        Optional.ofNullable(consumer.getMicrometerEnabled()).ifPresent(value -> factory.getContainerProperties().setMicrometerEnabled(value));
        Optional.ofNullable(consumer.getDeliveryAttemptHeader()).ifPresent(value -> factory.getContainerProperties().setDeliveryAttemptHeader(value));
        Optional.ofNullable(consumer.getCheckDeserExWhenKeyNull()).ifPresent(value -> factory.getContainerProperties().setCheckDeserExWhenKeyNull(value));
        Optional.ofNullable(consumer.getCheckDeserExWhenValueNull()).ifPresent(value -> factory.getContainerProperties().setCheckDeserExWhenValueNull(value));
        Optional.ofNullable(consumer.getMonitorInterval()).ifPresent(value -> factory.getContainerProperties().setMonitorInterval(value));
        Optional.ofNullable(consumer.getNoPollThreshold()).ifPresent(value -> factory.getContainerProperties().setNoPollThreshold(value));
        Optional.ofNullable(consumer.getCommitRetries()).ifPresent(value -> factory.getContainerProperties().setCommitRetries(value));
        Optional.ofNullable(consumer.getSubBatchPerPartition()).ifPresent(value -> factory.getContainerProperties().setSubBatchPerPartition(value));
        Optional.ofNullable(consumer.getStopContainerWhenFenced()).ifPresent(value -> factory.getContainerProperties().setStopContainerWhenFenced(value));
        Optional.ofNullable(consumer.getStopImmediate()).ifPresent(value -> factory.getContainerProperties().setStopImmediate(value));
        Optional.ofNullable(consumer.getClientId()).ifPresent(value -> factory.getContainerProperties().setClientId(value));
        Optional.ofNullable(consumer.getSyncCommitTimeoutSecond()).ifPresent(value -> factory.getContainerProperties().setSyncCommitTimeout(Duration.ofSeconds(value)));
        Optional.ofNullable(consumer.getSyncCommit()).ifPresent(value -> factory.getContainerProperties().setSyncCommits(value));
        Optional.ofNullable(consumer.getFixTxOffsets()).ifPresent(value -> factory.getContainerProperties().setFixTxOffsets(value));
        Optional.ofNullable(consumer.getPollTimeout()).ifPresent(value -> factory.getContainerProperties().setPollTimeout(value));
        Optional.ofNullable(consumer.getMissingTopicAlertEnable()).ifPresent(value -> factory.getContainerProperties().setMissingTopicsFatal(value));
        Optional.ofNullable(consumer.getConcurrency()).ifPresent(factory::setConcurrency);
        Optional.ofNullable(consumer.getPhase()).ifPresent(factory::setPhase);
        Optional.ofNullable(consumer.getAutoStartup()).ifPresent(factory::setAutoStartup);
        Optional.ofNullable(consumer.getBatchListener()).ifPresent(factory::setBatchListener);
        Optional.ofNullable(consumer.getAckDiscarded()).ifPresent(factory::setAckDiscarded);
        if (Objects.equals(consumer.getRetryType(), RetryType.BLOCKING_RETRY)) {
            Optional.ofNullable(consumer.getBlockingRetry()).ifPresent(blockingRetry -> {
                BackOff backoff;
                if (Objects.equals(blockingRetry.getIsExponentialRetry(), true)) {
                    ExponentialBackOffWithMaxRetries exponentialBackOffWithMaxRetries = new ExponentialBackOffWithMaxRetries(Optional.ofNullable(blockingRetry.getRetryCount()).orElse(0));
                    exponentialBackOffWithMaxRetries.setInitialInterval(Optional.ofNullable(blockingRetry.getBackoffIntervalMillis()).orElse(1000));
                    exponentialBackOffWithMaxRetries.setMultiplier(Optional.ofNullable(blockingRetry.getMultiplier()).orElse(2D));
                    exponentialBackOffWithMaxRetries.setMaxInterval(Optional.ofNullable(blockingRetry.getMaxInterval()).orElse(1000_000L));
                    backoff = exponentialBackOffWithMaxRetries;
                } else {
                    backoff = new FixedBackOff(Optional.of(blockingRetry.getBackoffIntervalMillis()).orElse(50), Optional.of(blockingRetry.getRetryCount()).orElse(0));
                }
                factory.setCommonErrorHandler(new DefaultErrorHandler(((consumerRecord, exception) -> handleFailover(suitableKafkaTemplate, consumer, consumerRecord, exception)), backoff));
            });
        } else if(Objects.equals(consumer.getRetryType(), RetryType.NO_RETRY)) {
            factory.setCommonErrorHandler(
                    new DefaultErrorHandler(((consumerRecord, exception) -> handleFailover(suitableKafkaTemplate, consumer, consumerRecord, exception)),
                            new FixedBackOff(50, 0)));
        }
        return factory;
    }
}