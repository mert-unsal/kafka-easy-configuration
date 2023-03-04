package com.munsal.kafkaconfiguration.config;

import com.munsal.kafkaconfiguration.model.Producer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Configuration
public class KafkaProducerConfiguration {
    private final KafkaConfiguration kafkaConfiguration;

    @Bean("kafkaProducerTemplateMap")
    public Map<String, KafkaTemplate<String,Object>> kafkaTemplateHashMap() {
        return Optional.ofNullable(kafkaConfiguration.getProducers())
                .map(producerMap -> producerMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry-> kafkaTemplate(entry.getValue()))))
                .orElseThrow(() -> new RuntimeException("You need to supply at least 1 producer valid producer configuration on your yml file."));
    }

    private <T> KafkaTemplate<String,T> kafkaTemplate(Producer producer) {
        ProducerFactory<String,T> producerFactory = new DefaultKafkaProducerFactory<>(producer.getProps());
        return new KafkaTemplate<>(producerFactory);
    }

}
