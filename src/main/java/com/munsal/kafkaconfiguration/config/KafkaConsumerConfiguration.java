package com.munsal.kafkaconfiguration.config;


import com.munsal.kafkaconfiguration.util.KafkaConsumerUtil;
import com.munsal.kafkaconfiguration.model.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfiguration {
    private final KafkaConsumerUtil kafkaConsumerUtil;
    private final KafkaConfiguration kafkaConfiguration;

    @Bean("kafkaListenerContainerFactoryMap")
    public Map<String, ConcurrentKafkaListenerContainerFactory<String, ?>> kafkaListenerContainerFactoryMap(HashMap<String, KafkaTemplate<String,Object>> kafkaProducerTemplateMap) {
        Map<String,ConcurrentKafkaListenerContainerFactory<String,?>> kafkaListenerContainerFactoryMap = new HashMap<>();
        Optional.ofNullable(kafkaConfiguration.getConsumers()).ifPresent(consumerMap -> consumerMap.forEach((key, value) -> {
            Class<?> consumerClass = kafkaConsumerUtil.getDataClass(value);
            ConsumerFactory<String,?> consumerFactory = kafkaConsumerUtil.createConsumerFactory(value,consumerClass);
            kafkaListenerContainerFactoryMap.put(key,kafkaConsumerUtil.createListenerFactory(findSuitableKafkaTemplate(value,kafkaProducerTemplateMap),value,consumerFactory));
        }));
        return kafkaListenerContainerFactoryMap;
    }

    private KafkaTemplate<String,Object> findSuitableKafkaTemplate(Consumer consumer, HashMap<String,KafkaTemplate<String,Object>> kafkaProducerTemplateMap) {
        return Optional.ofNullable(consumer.getErrorProducerBeanName()).map(kafkaProducerTemplateMap::get)
                .orElseGet(() -> {
                    Optional<KafkaTemplate<String,Object>> optionalKafkaTemplate = kafkaProducerTemplateMap
                            .values()
                            .stream()
                            .filter(kafkaTemplate ->
                                kafkaTemplate.getProducerFactory().getConfigurationProperties().get("bootstrap.servers").equals(consumer.getProps().get("bootstrap.servers")))
                            .findFirst();
                    return optionalKafkaTemplate.orElse(null);
                });
    }

}
