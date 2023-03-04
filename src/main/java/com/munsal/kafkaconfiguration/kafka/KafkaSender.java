package com.munsal.kafkaconfiguration.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Component
public class KafkaSender {
    private final Map<String, KafkaTemplate<String,Object>> kafkaTemplateHashMap;

    public <T> void send(String kafkaTemplateName, String topic, String key, T event) {
        Optional.ofNullable(kafkaTemplateHashMap.get(kafkaTemplateName)).ifPresentOrElse(kafkaTemplate -> {
            try {
                kafkaTemplate.send(topic,key,event);
            } catch (Exception exception) {
              log.error("Sending kafka message is failed with the fallowing exception : {}, topic : {}, key: {}, event: {}",exception.getMessage(),topic,key,event);
              throw new RuntimeException(exception);
            }
        },()-> log.error("KafkaTemplate could not found for kafkaTemplateName : {}", kafkaTemplateName));
    }
}
