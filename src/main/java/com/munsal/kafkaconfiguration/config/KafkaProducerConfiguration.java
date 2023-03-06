package com.munsal.kafkaconfiguration.config;

import com.munsal.kafkaconfiguration.model.Producer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Configuration
public class KafkaProducerConfiguration {
    private final KafkaConfiguration kafkaConfiguration;

    @Bean("kafkaTemplateMap")
    public Map<Integer, KafkaTemplate<String, Object>> kafkaTemplateMap() {
        Map<Integer, KafkaTemplate<String, Object>> kafkaTemplateMap;
        Optional<Map<String, Producer>> optionalMap = Optional.ofNullable(kafkaConfiguration.getProducers());
        if (optionalMap.isPresent()) {
            kafkaTemplateMap = optionalMap.get().entrySet().stream().collect(Collectors.toMap(o -> o.getKey().hashCode(), entry -> generateKafkaTemplate(entry.getValue())));
        } else {
            throw new RuntimeException("You need to supply at least 1 producer valid producer configuration on your yml file.");
        }
        return kafkaTemplateMap;
    }

    private KafkaTemplate<String, Object> generateKafkaTemplate(Producer producer) {
        ProducerFactory<String, Object> producerFactory = new DefaultKafkaProducerFactory<>(producer.getProps());
        return new KafkaTemplate<>(producerFactory);
    }

}
