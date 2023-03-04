package com.munsal.kafkaconfiguration.config;


import com.munsal.kafkaconfiguration.SpringContext;
import com.munsal.kafkaconfiguration.model.Consumer;
import com.munsal.kafkaconfiguration.util.KafkaConsumerUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.retrytopic.RetryTopicConfigurationBuilder;

import java.util.*;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfiguration {
    private final KafkaConsumerUtil kafkaConsumerUtil;
    private final KafkaConfiguration kafkaConfiguration;
    private final SpringContext springContext;

    @Bean("kafkaListenerContainerFactoryMap")
    public Map<String, ConcurrentKafkaListenerContainerFactory<String, ?>> kafkaListenerContainerFactoryMap(Map<String, KafkaTemplate<String,Object>> kafkaProducerTemplateMap) {
        Map<String,ConcurrentKafkaListenerContainerFactory<String,?>> kafkaListenerContainerFactoryMap = new HashMap<>();
        Optional.ofNullable(kafkaConfiguration.getConsumers()).ifPresent(consumerMap -> consumerMap.forEach((key, value) -> {
            Class<?> consumerClass = kafkaConsumerUtil.getDataClass(value);
            ConsumerFactory<String,?> consumerFactory = kafkaConsumerUtil.createConsumerFactory(value,consumerClass);
            KafkaTemplate<String, Object> kafkaTemplate = findSuitableKafkaTemplate(value, (HashMap<String, KafkaTemplate<String, Object>>) kafkaProducerTemplateMap);
            RetryTopicConfigurationBuilder retryTopicConfigurationBuilder = RetryTopicConfigurationBuilder
                    .newInstance();
            if(Objects.equals(value.getIsExponentialRetry(),true)) {
                retryTopicConfigurationBuilder.exponentialBackoff(
                        Optional.ofNullable(value.getBackoffIntervalMillis()).orElse(250),
                        Optional.ofNullable(value.getMultiplier()).orElse(2),
                        Optional.ofNullable(value.getMaxInterval()).orElse(15000L)
                );
            } else {
                retryTopicConfigurationBuilder.fixedBackOff(Optional.ofNullable(value.getBackoffIntervalMillis()).orElse(250));
            }
            retryTopicConfigurationBuilder.maxAttempts(5).includeTopics(List.of(value.getTopic()));
            if(Objects.nonNull(value.getRetryOn()) && Objects.nonNull(value.getNotRetryOn())) {
                throw new IllegalArgumentException("Please use supply only retry-on filed or only not-retry-on field on the your kafka yml");
            }
            if(Objects.nonNull(value.getRetryOn())) {
                retryTopicConfigurationBuilder.retryOn(value.getRetryOn());
            }
            if(Objects.nonNull(value.getNotRetryOn())) {
                retryTopicConfigurationBuilder.notRetryOn(value.getNotRetryOn());
            }
            springContext.addBean(new StringBuilder(key).append("-retry-configuration").toString(), retryTopicConfigurationBuilder.create(kafkaTemplate));
            kafkaListenerContainerFactoryMap.put(key,kafkaConsumerUtil.createListenerFactory(kafkaTemplate,value,consumerFactory));
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
