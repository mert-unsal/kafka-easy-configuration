package com.munsal.kafkaconfiguration.kafka;

import com.munsal.kafkaconfiguration.model.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface FailoverHandler {
    void handle(Consumer consumer, ConsumerRecord consumerRecord, Exception exception);
}
